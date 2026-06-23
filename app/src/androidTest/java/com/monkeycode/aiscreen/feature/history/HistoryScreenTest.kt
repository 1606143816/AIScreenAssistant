package com.monkeycode.aiscreen.feature.history

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.monkeycode.aiscreen.ui.screen.HistoryScreen
import org.junit.Rule
import org.junit.Test

class HistoryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptyState_showsPlaceholder() {
        val state = HistoryUiState()
        composeTestRule.setContent {
            HistoryScreen(
                uiState = state,
                onSelectConversation = {},
                onDeleteConversation = {},
                onClearSelection = {},
                onClearError = {},
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("暂无历史记录").assertExists()
        composeTestRule.onNodeWithText("开始一段新对话后，记录将显示在这里").assertExists()
    }

    @Test
    fun errorSnackbar_showsOnError() {
        val state = HistoryUiState(error = "加载失败，请重试")
        composeTestRule.setContent {
            HistoryScreen(
                uiState = state,
                onSelectConversation = {},
                onDeleteConversation = {},
                onClearSelection = {},
                onClearError = {},
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("加载失败，请重试").assertExists()
    }

    @Test
    fun conversationList_rendersSummaryItems() {
        val state = HistoryUiState(
            conversations = listOf(
                com.monkeycode.aiscreen.core.model.ConversationSummary(
                    id = "1",
                    title = "购物页面分析",
                    createdAt = System.currentTimeMillis() - 3600000,
                    updatedAt = System.currentTimeMillis(),
                    lastMessage = "已为您找到最佳优惠",
                    lastAppPackage = "com.taobao.taobao"
                )
            )
        )

        composeTestRule.setContent {
            HistoryScreen(
                uiState = state,
                onSelectConversation = {},
                onDeleteConversation = {},
                onClearSelection = {},
                onClearError = {},
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("购物页面分析").assertExists()
        composeTestRule.onNodeWithText("已为您找到最佳优惠").assertExists()
    }
}
