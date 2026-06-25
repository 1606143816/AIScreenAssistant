package com.monkeycode.aiscreen.core.domain

import com.monkeycode.aiscreen.core.data.repository.LLMRepository
import com.monkeycode.aiscreen.core.data.repository.SettingsRepository
import com.monkeycode.aiscreen.core.model.AnalysisResult
import com.monkeycode.aiscreen.core.model.LLMConfig
import com.monkeycode.aiscreen.core.model.SerializedUITree
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AnalyzeScreenUseCaseTest {

    private lateinit var readUITreeUseCase: ReadUITreeUseCase
    private lateinit var filterUseCase: FilterSensitiveNodesUseCase
    private lateinit var llmRepository: LLMRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: AnalyzeScreenUseCase

    @Before
    fun setUp() {
        readUITreeUseCase = mockk()
        filterUseCase = FilterSensitiveNodesUseCase()
        llmRepository = mockk()
        settingsRepository = mockk()
        useCase = AnalyzeScreenUseCase(readUITreeUseCase, filterUseCase, llmRepository, settingsRepository)
    }

    @Test
    fun should_return_failure_when_ui_tree_read_fails() = runBlocking {
        every { readUITreeUseCase.invoke() } returns Result.failure(Exception("Read failed"))

        every { settingsRepository.llmConfig } returns flowOf(
            LLMConfig("url", "key", "model")
        )

        val result = useCase("test prompt", emptyList())

        assertTrue(result.isFailure)
        assertEquals("Read failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun should_return_failure_when_config_not_set() = runBlocking {
        val uiTree = SerializedUITree(packageName = "com.test", elements = emptyList(), timestamp = 1000)
        every { readUITreeUseCase.invoke() } returns Result.success(uiTree)
        every { settingsRepository.llmConfig } returns flowOf(null)

        val result = useCase("test prompt", emptyList())

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("configuration") == true)
    }

    @Test
    fun should_call_llm_repository_with_filtered_tree() = runBlocking {
        val uiTree = SerializedUITree(packageName = "com.test", elements = emptyList(), timestamp = 1000)
        val config = LLMConfig("url", "key", "model")
        val expectedResult = AnalysisResult("desc", emptyList(), "suggestion", emptyList())

        every { readUITreeUseCase.invoke() } returns Result.success(uiTree)
        every { settingsRepository.llmConfig } returns flowOf(config)
        coEvery { llmRepository.analyze(uiTree, "test", emptyList(), config) } returns Result.success(expectedResult)

        val result = useCase("test", emptyList())

        assertTrue(result.isSuccess)
        assertEquals(expectedResult, result.getOrNull())
    }
}
