package com.monkeycode.aiscreen.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.monkeycode.aiscreen.core.data.local.dao.ConversationDao
import com.monkeycode.aiscreen.core.data.local.dao.MessageDao
import com.monkeycode.aiscreen.core.data.local.entity.ConversationEntity
import com.monkeycode.aiscreen.core.data.local.entity.MessageEntity
import com.monkeycode.aiscreen.core.data.local.entity.UITreeRecordEntity

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        UITreeRecordEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
}
