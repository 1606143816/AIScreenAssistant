package com.monkeycode.aiscreen.feature.conversation

import com.monkeycode.aiscreen.core.data.repository.LLMRepository
import com.monkeycode.aiscreen.core.data.repository.SettingsRepository
import com.monkeycode.aiscreen.core.domain.AnalyzeScreenUseCase
import com.monkeycode.aiscreen.core.domain.ExecuteActionUseCase
import com.monkeycode.aiscreen.core.domain.FilterSensitiveNodesUseCase
import com.monkeycode.aiscreen.core.domain.ManageConversationUseCase
import com.monkeycode.aiscreen.core.domain.ProcessVoiceInputUseCase
import com.monkeycode.aiscreen.core.domain.ReadUITreeUseCase
import com.monkeycode.aiscreen.core.domain.AccessibilityServiceBridge
import com.monkeycode.aiscreen.core.model.Action
import com.monkeycode.aiscreen.core.model.AnalysisResult
import com.monkeycode.aiscreen.core.model.Conversation
import com.monkeycode.aiscreen.core.model.OperationMode
import com.monkeycode.aiscreen.core.model.SerializedUITree
import com.monkeycode.aiscreen.core.model.UIElement
import com.monkeycode.aiscreen.core.model.SerializableRect
import com.monkeycode.aiscreen.core.model.ActionResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConversationFlowIntegrationTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun fullConversationFlow_analyzeAndExecuteActions() = runTest {
        val bridge = mockk<AccessibilityServiceBridge>()
        val llmRepository = mockk<LLMRepository>()
        val uiTreeSerializer = mockk<com.monkeycode.aiscreen.core.serializer.UITreeSerializer>()

        val sampleTree = SerializedUITree(
            packageName = "com.example.app",
            activityName = "MainActivity",
            elements = listOf(
                UIElement(
                    index = 0,
                    className = "android.widget.Button",
                    text = "登录",
                    contentDescription = null,
                    hint = null,
                    bounds = SerializableRect(100, 500, 200, 560),
                    isClickable = true,
                    isLongClickable = false,
                    isEditable = false,
                    isPassword = false,
                    isChecked = false,
                    isScrollable = false,
                    isFocused = false,
                    isEnabled = true,
                    childCount = 0,
                    depth = 2,
                    resourceId = "com.example.app:id/login_button"
                )
            ),
            timestamp = System.currentTimeMillis()
        )

        val sampleResult = AnalysisResult(
            screenDescription = "登录页面",
            keyElements = listOf(
                com.monkeycode.aiscreen.core.model.UIElementReference(
                    elementIndex = 0,
                    label = "登录按钮",
                    description = "点击进行登录"
                )
            ),
            suggestionText = "点击登录按钮即可进入应用",
            actions = listOf(Action.Click(elementIndex = 0))
        )

        every { bridge.isEnabled() } returns true
        every { bridge.readUITree() } returns sampleTree
        coEvery { bridge.performAction(any()) } returns ActionResult.Success(0, "ok")
        every { uiTreeSerializer.serialize(any()) } returns sampleTree

        val readUITree = ReadUITreeUseCase(bridge, uiTreeSerializer)
        val filterSensitive = FilterSensitiveNodesUseCase()

        val settingsRepository = mockk<SettingsRepository>()
        every { settingsRepository.operationMode } returns flowOf(OperationMode.AUTONOMOUS)
        every { settingsRepository.llmConfig } returns flowOf(
            com.monkeycode.aiscreen.core.model.LLMConfig(
                baseUrl = "https://api.openai.com",
                apiKey = "sk-test",
                modelName = "gpt-4o"
            )
        )

        coEvery { llmRepository.analyze(any(), any(), any(), any()) } returns Result.success(sampleResult)

        val analyzeScreen = AnalyzeScreenUseCase(readUITree, filterSensitive, llmRepository, settingsRepository)
        val executeAction = ExecuteActionUseCase(bridge)

        val manageConversation = mockk<ManageConversationUseCase>()
        coEvery { manageConversation.createConversation(any()) } returns Conversation(
            id = "1",
            title = "登录页面",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            messages = emptyList()
        )
        coEvery { manageConversation.appendMessage(any(), any()) } returns Unit

        val voiceInput = mockk<ProcessVoiceInputUseCase>()

        val viewModel = ConversationViewModel(
            analyzeScreenUseCase = analyzeScreen,
            executeActionUseCase = executeAction,
            manageConversationUseCase = manageConversation,
            settingsRepository = settingsRepository,
            processVoiceInputUseCase = voiceInput
        )

        assertEquals(OperationMode.AUTONOMOUS, viewModel.uiState.value.operationMode)
        assertTrue(viewModel.uiState.value.messages.isEmpty())

        viewModel.onEvent(ConversationEvent.SendMessage("帮我登录"))

        delay(200)

        val state = viewModel.uiState.value
        assertFalse(state.isAnalyzing)
        assertTrue(state.messages.any { it.role == com.monkeycode.aiscreen.core.model.MessageRole.USER })
        assertTrue(state.messages.any { it.role == com.monkeycode.aiscreen.core.model.MessageRole.ASSISTANT })
        assertEquals("登录页面", state.currentApp)
    }

    @Test
    fun toggleMode_persistsToDataStore() = runTest {
        val settingsRepository = mockk<SettingsRepository>()
        every { settingsRepository.operationMode } returns flowOf(OperationMode.SUGGESTION)
        every { settingsRepository.llmConfig } returns flowOf(null)
        coEvery { settingsRepository.saveOperationMode(any()) } returns Unit

        val viewModel = ConversationViewModel(
            analyzeScreenUseCase = mockk(),
            executeActionUseCase = mockk(),
            manageConversationUseCase = mockk(),
            settingsRepository = settingsRepository,
            processVoiceInputUseCase = mockk()
        )

        assertEquals(OperationMode.SUGGESTION, viewModel.uiState.value.operationMode)

        viewModel.onEvent(ConversationEvent.ToggleMode)
    }

    @Test
    fun clearError_removesErrorFromState() = runTest {
        val settingsRepository = mockk<SettingsRepository>()
        every { settingsRepository.operationMode } returns flowOf(OperationMode.SUGGESTION)
        every { settingsRepository.llmConfig } returns flowOf(null)

        val viewModel = ConversationViewModel(
            analyzeScreenUseCase = mockk(),
            executeActionUseCase = mockk(),
            manageConversationUseCase = mockk(),
            settingsRepository = settingsRepository,
            processVoiceInputUseCase = mockk()
        )

        viewModel.onEvent(ConversationEvent.VoiceError("识别错误"))
        assertNotNull(viewModel.uiState.value.error)

        viewModel.onEvent(ConversationEvent.ClearError)
        assertNull(viewModel.uiState.value.error)
    }
}
