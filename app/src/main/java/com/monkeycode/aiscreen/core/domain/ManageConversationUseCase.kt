package com.monkeycode.aiscreen.core.domain

import com.monkeycode.aiscreen.core.data.repository.ConversationRepository
import com.monkeycode.aiscreen.core.model.Conversation
import com.monkeycode.aiscreen.core.model.ConversationSummary
import com.monkeycode.aiscreen.core.model.Message
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ManageConversationUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository
) {
    fun getHistory(): Flow<List<ConversationSummary>> {
        return conversationRepository.getHistory()
    }

    suspend fun getConversation(id: String): Conversation? {
        return conversationRepository.getConversation(id)
    }

    suspend fun createConversation(title: String): Conversation {
        val now = System.currentTimeMillis()
        val conversation = Conversation(
            id = UUID.randomUUID().toString(),
            title = title,
            createdAt = now,
            updatedAt = now,
            messages = emptyList()
        )
        conversationRepository.createConversation(conversation)
        return conversation
    }

    suspend fun appendMessage(conversationId: String, message: Message) {
        conversationRepository.appendMessage(conversationId, message)
    }

    suspend fun delete(id: String) {
        conversationRepository.delete(id)
    }
}
