package com.monkeycode.aiscreen.core.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.monkeycode.aiscreen.core.model.LLMConfig
import com.monkeycode.aiscreen.core.model.OperationMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    private val context: Context
) {
    private val dataStore: DataStore<Preferences>
        get() = context.settingsDataStore

    companion object {
        val KEY_BASE_URL = stringPreferencesKey("llm_base_url")
        val KEY_API_KEY = stringPreferencesKey("llm_api_key")
        val KEY_MODEL_NAME = stringPreferencesKey("llm_model_name")
        val KEY_MAX_TOKENS = intPreferencesKey("llm_max_tokens")
        val KEY_TEMPERATURE = floatPreferencesKey("llm_temperature")
        val KEY_OPERATION_MODE = stringPreferencesKey("operation_mode")
    }

    val llmConfig: Flow<LLMConfig?> = dataStore.data.map { preferences ->
        val baseUrl = preferences[KEY_BASE_URL] ?: return@map null
        val apiKey = preferences[KEY_API_KEY] ?: return@map null
        val modelName = preferences[KEY_MODEL_NAME] ?: return@map null
        LLMConfig(
            baseUrl = baseUrl,
            apiKey = apiKey,
            modelName = modelName,
            maxTokens = preferences[KEY_MAX_TOKENS] ?: 4096,
            temperature = preferences[KEY_TEMPERATURE] ?: 0.7f
        )
    }

    val operationMode: Flow<OperationMode> = dataStore.data.map { preferences ->
        val modeStr = preferences[KEY_OPERATION_MODE] ?: OperationMode.SUGGESTION.name
        try {
            OperationMode.valueOf(modeStr)
        } catch (e: IllegalArgumentException) {
            OperationMode.SUGGESTION
        }
    }

    suspend fun saveLLMConfig(config: LLMConfig) {
        dataStore.edit { preferences ->
            preferences[KEY_BASE_URL] = config.baseUrl
            preferences[KEY_API_KEY] = config.apiKey
            preferences[KEY_MODEL_NAME] = config.modelName
            preferences[KEY_MAX_TOKENS] = config.maxTokens
            preferences[KEY_TEMPERATURE] = config.temperature
        }
    }

    suspend fun saveOperationMode(mode: OperationMode) {
        dataStore.edit { preferences ->
            preferences[KEY_OPERATION_MODE] = mode.name
        }
    }
}
