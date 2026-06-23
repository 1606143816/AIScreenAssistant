package com.monkeycode.aiscreen.core.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.monkeycode.aiscreen.core.data.datastore.SettingsDataStore
import com.monkeycode.aiscreen.core.model.LLMConfig
import com.monkeycode.aiscreen.core.model.OperationMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class SettingsDataStoreTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val settingsDataStore = SettingsDataStore(context)

    @Test
    fun should_store_and_retrieve_llm_config() = runBlocking {
        val config = LLMConfig(
            baseUrl = "https://api.openai.com",
            apiKey = "sk-test",
            modelName = "gpt-4o",
            maxTokens = 2048,
            temperature = 0.5f
        )

        settingsDataStore.saveLLMConfig(config)

        val retrieved = settingsDataStore.llmConfig.first()
        assertNotNull(retrieved)
        assertEquals("https://api.openai.com", retrieved?.baseUrl)
        assertEquals("sk-test", retrieved?.apiKey)
        assertEquals("gpt-4o", retrieved?.modelName)
        assertEquals(2048, retrieved?.maxTokens)
        assertEquals(0.5f, retrieved?.temperature)
    }

    @Test
    fun should_store_and_retrieve_operation_mode() = runBlocking {
        settingsDataStore.saveOperationMode(OperationMode.AUTONOMOUS)

        val mode = settingsDataStore.operationMode.first()
        assertEquals(OperationMode.AUTONOMOUS, mode)
    }

    @Test
    fun should_default_to_suggestion_mode() = runBlocking {
        settingsDataStore.saveLLMConfig(LLMConfig("u", "k", "m"))

        val mode = settingsDataStore.operationMode.first()
        assertEquals(OperationMode.SUGGESTION, mode)
    }

    @Test
    fun should_overwrite_existing_config() = runBlocking {
        settingsDataStore.saveLLMConfig(LLMConfig("url1", "key1", "model1"))
        settingsDataStore.saveLLMConfig(LLMConfig("url2", "key2", "model2"))

        val config = settingsDataStore.llmConfig.first()
        assertEquals("url2", config?.baseUrl)
        assertEquals("model2", config?.modelName)
    }
}
