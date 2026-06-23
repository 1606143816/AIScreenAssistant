package com.monkeycode.aiscreen.di

import com.monkeycode.aiscreen.core.data.local.AppDatabase
import com.monkeycode.aiscreen.core.data.local.ConversationDao
import com.monkeycode.aiscreen.core.data.local.MessageDao
import com.monkeycode.aiscreen.core.data.local.UITreeRecordDao
import com.monkeycode.aiscreen.core.domain.AccessibilityServiceBridge
import com.monkeycode.aiscreen.service.accessibility.AIAccessibilityService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
    fun provideAccessibilityServiceBridge(): AccessibilityServiceBridge {
        return AIAccessibilityService.getInstance()
            ?: throw IllegalStateException("AccessibilityService is not running")
    }
}
