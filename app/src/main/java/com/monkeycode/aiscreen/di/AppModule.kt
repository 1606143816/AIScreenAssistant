package com.monkeycode.aiscreen.di

import android.content.Context
import androidx.room.Room
import com.monkeycode.aiscreen.core.data.local.AppDatabase
import com.monkeycode.aiscreen.core.data.local.dao.ConversationDao
import com.monkeycode.aiscreen.core.data.local.dao.MessageDao
import com.monkeycode.aiscreen.core.domain.AccessibilityServiceBridge
import com.monkeycode.aiscreen.service.accessibility.AIAccessibilityService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "ai_screen_assistant.db"
        ).build()
    }

    @Provides
    fun provideConversationDao(database: AppDatabase): ConversationDao {
        return database.conversationDao()
    }

    @Provides
    fun provideMessageDao(database: AppDatabase): MessageDao {
        return database.messageDao()
    }

    @Provides
    @Singleton
    fun provideAccessibilityServiceBridge(): AccessibilityServiceBridge {
        return AIAccessibilityService.getInstance()
            ?: throw IllegalStateException("AccessibilityService is not running")
    }
}
