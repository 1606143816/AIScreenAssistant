package com.monkeycode.aiscreen.core.data.repository

import com.monkeycode.aiscreen.core.data.local.dao.ConversationDao
import com.monkeycode.aiscreen.core.data.local.dao.MessageDao
import com.monkeycode.aiscreen.core.data.local.entity.ConversationEntity
import com.monkeycode.aiscreen.core.data.local.entity.MessageEntity
import com.monkeycode.aiscreen.core.model.Conversation
import com.monkeycode.aiscreen.core.model.ConversationSummary
import com.monkeycode.aiscreen.core.model.Message
import com.monkeycode.aiscreen.core.model.MessageRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationRepository @Inject constructor(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao
) {
    fun getHistory(): Flow<List<ConversationSummary>> {
        return conversationDao.getRecent(20).combine(
            conversationDao.countFlow()
        ) { conversations, _ ->
            conversations
        }.map { entities ->
            entities.map { entity ->
                ConversationSummary(
                    id = entity.id,
                    title = entity.title,
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt,
                    lastMessage = null,
                    lastAppPackage = null
                )
            }
        }
    }

    suspend fun getConversation(id: String): Conversation? {
        val entity = conversationDao.getById(id) ?: return null
        val messages = messageDao.getByConversationId(id)
        return Conversation(
            id = entity.id,
            title = entity.title,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            messages = messages.map { it.toDomainModel() }
        )
    }

    suspend fun createConversation(conversation: Conversation) {
        conversationDao.insert(
            ConversationEntity(
                id = conversation.id,
                title = conversation.title,
                createdAt = conversation.createdAt,
                updatedAt = conversation.updatedAt
            )
        )
        val messageEntities = conversation.messages.map { it.toEntity(conversation.id) }
        if (messageEntities.isNotEmpty()) {
            messageDao.insertAll(messageEntities)
        }
    }

    suspend fun appendMessage(conversationId: String, message: Message) {
        messageDao.insert(message.toEntity(conversationId))
        conversationDao.update(
            id = conversationId,
            title = "",
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun delete(id: String) {
        messageDao.deleteByConversationId(id)
        conversationDao.delete(id)
    }

    suspend fun enforceCacheLimit(maxRecords: Int = 20) {
        val count = conversationDao.count()
        if (count > maxRecords) {
            val toDelete = count - maxRecords
            conversationDao.deleteOldest(toDelete)
        }
    }

    private fun Message.toEntity(conversationId: String): MessageEntity {
        return MessageEntity(
            id = id,
            conversationId = conversationId,
            role = role.name,
            content = content,
            analysisResultJson = analysisResult?.let { kotlinx.serialization.json.Json.encodeToString(it) },
            timestamp = timestamp
        )
    }

    private fun MessageEntity.toDomainModel(): Message {
        return Message(
            id = id,
            role = try { MessageRole.valueOf(role) } catch (e: Exception) { MessageRole.ASSISTANT },
            content = content,
            analysisResult = analysisResultJson?.let {
                kotlinx.serialization.json.Json.decodeFromString(it)
            },
            timestamp = timestamp
        )
    }
}
