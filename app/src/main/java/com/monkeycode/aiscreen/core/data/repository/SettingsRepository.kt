package com.monkeycode.aiscreen.core.data.repository

import com.monkeycode.aiscreen.core.data.datastore.SettingsDataStore
import com.monkeycode.aiscreen.core.model.LLMConfig
import com.monkeycode.aiscreen.core.model.OperationMode
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) {
    val llmConfig: Flow<LLMConfig?> = settingsDataStore.llmConfig

    val operationMode: Flow<OperationMode> = settingsDataStore.operationMode

    suspend fun saveLLMConfig(config: LLMConfig) {
        settingsDataStore.saveLLMConfig(config)
    }

    suspend fun saveOperationMode(mode: OperationMode) {
        settingsDataStore.saveOperationMode(mode)
    }
}
