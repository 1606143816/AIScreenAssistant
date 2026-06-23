package com.monkeycode.aiscreen.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ui_tree_records",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["conversationId"])]
)
data class UITreeRecordEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val serializedTree: String,
    val packageName: String,
    val activityName: String?,
    val timestamp: Long
)
