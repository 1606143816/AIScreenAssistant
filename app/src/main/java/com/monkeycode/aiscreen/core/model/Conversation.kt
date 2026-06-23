package com.monkeycode.aiscreen.core.model

data class Conversation(
    val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val messages: List<Message> = emptyList(),
    val uiTreeRecords: List<UITreeRecord> = emptyList()
)

data class ConversationSummary(
    val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val lastMessage: String?,
    val lastAppPackage: String?
)

data class Message(
    val id: String,
    val role: MessageRole,
    val content: String,
    val analysisResult: AnalysisResult? = null,
    val timestamp: Long
)

enum class MessageRole {
    USER,
    ASSISTANT
}

data class UITreeRecord(
    val id: String,
    val conversationId: String,
    val serializedTree: String,
    val packageName: String,
    val activityName: String?,
    val timestamp: Long
)
