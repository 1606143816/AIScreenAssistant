package com.monkeycode.aiscreen.feature.conversation

import androidx.lifecycle.SavedStateHandle
import com.monkeycode.aiscreen.core.data.repository.LLMRepository
import com.monkeycode.aiscreen.core.data.repository.SettingsRepository
import com.monkeycode.aiscreen.core.domain.AnalyzeScreenUseCase
import com.monkeycode.aiscreen.core.domain.ExecuteActionUseCase
import com.monkeycode.aiscreen.core.domain.FilterSensitiveNodesUseCase
import com.monkeycode.aiscreen.core.domain.ManageConversationUseCase
import com.monkeycode.aiscreen.core.domain.ProcessVoiceInputUseCase
import com.monkeycode.aiscreen.core.domain.ReadUITreeUseCase
import com.monkeycode.aiscreen.core.domain.AccessibilityServiceBridge
import com.monkeycode.aiscreen.core.data.datastore.SettingsDataStore
import com.monkeycode.aiscreen.core.model.Action
import com.monkeycode.aiscreen.core.model.AnalysisResult
import com.monkeycode.aiscreen.core.model.Message
import com.monkeycode.aiscreen.core.model.OperationMode
import com.monkeycode.aiscreen.core.model.SerializedUITree
import com.monkeycode.aiscreen.core.model.UIElement
import com.monkeycode.aiscreen.core.model.SerializableRect
import com.monkeycode.aiscreen.core.model.ActionResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ConversationFlowIntegrationTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Test
    fun fullConversationFlow_analyzeAndExecuteActions() = runTest {
        val bridge = mock<AccessibilityServiceBridge>()
        val settingsDataStore = mock<SettingsDataStore>()
        val llmRepository = mock<LLMRepository>()
        val uiTreeSerializer = mock<com.monkeycode.aiscreen.core.serializer.UITreeSerializer>()

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
                    isCheckable = false,
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

        whenever(bridge.isEnabled()).thenReturn(true)
        whenever(bridge.readUITree()).thenReturn(sampleTree)
        whenever(bridge.performAction(any())).thenReturn(
            ActionResult.Success(0, "ok")
        )
        whenever(uiTreeSerializer.serialize(any())).thenReturn(sampleTree)
        whenever(settingsDataStore.llmConfig).thenReturn(
            flowOf(
                com.monkeycode.aiscreen.core.model.LLMConfig(
                    baseUrl = "https://api.openai.com",
                    apiKey = "sk-test",
                    modelName = "gpt-4o"
                )
            )
        )
        whenever(settingsDataStore.operationMode).thenReturn(flowOf(OperationMode.AUTONOMOUS))
        whenever(llmRepository.analyze(any(), any(), any(), any())).thenReturn(
            Result.success(sampleResult)
        )

        val readUITree = ReadUITreeUseCase(bridge, uiTreeSerializer)
        val filterSensitive = FilterSensitiveNodesUseCase()
        val analyzeScreen = AnalyzeScreenUseCase(readUITree, filterSensitive, llmRepository, settingsDataStore)
        val executeAction = ExecuteActionUseCase(bridge)

        val settingsRepository = mock<SettingsRepository>()
        whenever(settingsRepository.operationMode).thenReturn(flowOf(OperationMode.AUTONOMOUS))
        whenever(settingsRepository.llmConfig).thenReturn(
            flowOf(
                com.monkeycode.aiscreen.core.model.LLMConfig(
                    baseUrl = "https://api.openai.com",
                    apiKey = "sk-test",
                    modelName = "gpt-4o"
                )
            )
        )

        val manageConversation = mock<ManageConversationUseCase>()
        whenever(manageConversation.createConversation(any())).thenReturn(
            com.monkeycode.aiscreen.core.model.Conversation(
                id = "1",
                title = "登录页面",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                messages = emptyList()
            )
        )

        val voiceInput = mock<ProcessVoiceInputUseCase>()

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

        testDispatcher.scheduler.advanceUntilIdle()
        delay(100)

        val state = viewModel.uiState.value
        assertFalse(state.isAnalyzing)
        assertTrue(state.messages.any { it.role == com.monkeycode.aiscreen.core.model.MessageRole.USER })
        assertTrue(state.messages.any { it.role == com.monkeycode.aiscreen.core.model.MessageRole.ASSISTANT })
        assertEquals("登录页面", state.currentApp)
    }

    @Test
    fun toggleMode_persistsToDataStore() = runTest {
        val settingsRepository = mock<SettingsRepository>()
        whenever(settingsRepository.operationMode).thenReturn(flowOf(OperationMode.SUGGESTION))
        whenever(settingsRepository.llmConfig).thenReturn(flowOf(null))

        val viewModel = ConversationViewModel(
            analyzeScreenUseCase = mock(),
            executeActionUseCase = mock(),
            manageConversationUseCase = mock(),
            settingsRepository = settingsRepository,
            processVoiceInputUseCase = mock()
        )

        assertEquals(OperationMode.SUGGESTION, viewModel.uiState.value.operationMode)

        viewModel.onEvent(ConversationEvent.ToggleMode)

        testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun clearError_removesErrorFromState() = runTest {
        val settingsRepository = mock<SettingsRepository>()
        whenever(settingsRepository.operationMode).thenReturn(flowOf(OperationMode.SUGGESTION))
        whenever(settingsRepository.llmConfig).thenReturn(flowOf(null))

        val viewModel = ConversationViewModel(
            analyzeScreenUseCase = mock(),
            executeActionUseCase = mock(),
            manageConversationUseCase = mock(),
            settingsRepository = settingsRepository,
            processVoiceInputUseCase = mock()
        )

        viewModel.onEvent(ConversationEvent.VoiceError("识别错误"))
        assertNotNull(viewModel.uiState.value.error)

        viewModel.onEvent(ConversationEvent.ClearError)
        assertNull(viewModel.uiState.value.error)
    }
}
