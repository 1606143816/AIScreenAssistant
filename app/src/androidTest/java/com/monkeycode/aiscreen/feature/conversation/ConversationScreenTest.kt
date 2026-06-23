package com.monkeycode.aiscreen.feature.conversation

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.monkeycode.aiscreen.core.model.AnalysisResult
import com.monkeycode.aiscreen.core.model.MessageRole
import com.monkeycode.aiscreen.core.model.OperationMode
import com.monkeycode.aiscreen.ui.screen.ConversationScreen
import org.junit.Rule
import org.junit.Test

class ConversationScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptyState_showsPlaceholderText() {
        val state = ConversationUiState()
        composeTestRule.setContent {
            ConversationScreen(
                uiState = state,
                onEvent = {},
                onBack = {}
            )
        }

        composeTestRule
            .onNodeWithText("AI 屏幕助手")
            .assertExists()

        composeTestRule
            .onNodeWithText("输入指令或点击麦克风开始语音输入")
            .assertExists()
    }

    @Test
    fun sendingMessage_addsToMessageList() {
        var state = ConversationUiState()

        composeTestRule.setContent {
            ConversationScreen(
                uiState = state,
                onEvent = { event ->
                    if (event is ConversationEvent.SendMessage) {
                        state = state.copy(
                            messages = state.messages + MessageUiItem(
                                id = "1",
                                role = MessageRole.USER,
                                content = event.text
                            )
                        )
                    }
                },
                onBack = {}
            )
        }

        composeTestRule
            .onNodeWithText("输入指令…")
            .performTextInput("帮我看一下当前页面")

        composeTestRule
            .onNodeWithText("发送")
            .performClick()
    }

    @Test
    fun modeSwitch_displayedCorrectly_perMode() {
        val suggestionState = ConversationUiState(operationMode = OperationMode.SUGGESTION)
        composeTestRule.setContent {
            ConversationScreen(
                uiState = suggestionState,
                onEvent = {},
                onBack = {}
            )
        }

        composeTestRule
            .onNodeWithText("建议模式")
            .assertExists()

        val autoState = ConversationUiState(operationMode = OperationMode.AUTONOMOUS)
        composeTestRule.setContent {
            ConversationScreen(
                uiState = autoState,
                onEvent = {},
                onBack = {}
            )
        }

        composeTestRule
            .onNodeWithText("自动模式")
            .assertExists()
    }

    @Test
    fun errorSnackbar_showsWhenError_isPresent() {
        val errorState = ConversationUiState(error = "网络连接失败")
        composeTestRule.setContent {
            ConversationScreen(
                uiState = errorState,
                onEvent = {},
                onBack = {}
            )
        }

        composeTestRule
            .onNodeWithText("网络连接失败")
            .assertExists()
    }

    @Test
    fun loadingIndicator_shows_whenAnalyzing() {
        val loadingState = ConversationUiState(isAnalyzing = true)
        composeTestRule.setContent {
            ConversationScreen(
                uiState = loadingState,
                onEvent = {},
                onBack = {}
            )
        }

        composeTestRule
            .onNodeWithText("AI 正在分析…")
            .assertExists()
    }
}
