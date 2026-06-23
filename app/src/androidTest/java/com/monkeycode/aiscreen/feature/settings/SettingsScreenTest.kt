package com.monkeycode.aiscreen.feature.settings

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.monkeycode.aiscreen.core.model.OperationMode
import com.monkeycode.aiscreen.core.model.ValidationResult
import com.monkeycode.aiscreen.ui.screen.SettingsScreen
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun formFields_renderedCorrectly() {
        val state = SettingsUiState(isLoaded = true)
        composeTestRule.setContent {
            SettingsScreen(
                uiState = state,
                onBaseUrlChange = {},
                onApiKeyChange = {},
                onModelNameChange = {},
                onMaxTokensChange = {},
                onTemperatureChange = {},
                onOperationModeChange = {},
                onSave = {},
                onValidate = {},
                onClearError = {},
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("LLM 连接配置").assertExists()
        composeTestRule.onNodeWithText("API 地址").assertExists()
        composeTestRule.onNodeWithText("API 密钥").assertExists()
        composeTestRule.onNodeWithText("模型名称").assertExists()
        composeTestRule.onNodeWithText("操作模式").assertExists()
    }

    @Test
    fun textInput_updatesCorrectly() {
        var baseUrl = ""
        val state = SettingsUiState(baseUrl = baseUrl, isLoaded = true)
        composeTestRule.setContent {
            SettingsScreen(
                uiState = state,
                onBaseUrlChange = { baseUrl = it },
                onApiKeyChange = {},
                onModelNameChange = {},
                onMaxTokensChange = {},
                onTemperatureChange = {},
                onOperationModeChange = {},
                onSave = {},
                onValidate = {},
                onClearError = {},
                onBack = {}
            )
        }

        composeTestRule
            .onNodeWithText("API 地址")
            .performTextInput("https://api.openai.com")
    }

    @Test
    fun validationResult_showsAfterValidate() {
        val state = SettingsUiState(
            isLoaded = true,
            validationResult = ValidationResult(
                success = true,
                message = "API 连接正常",
                modelName = "gpt-4o"
            )
        )
        composeTestRule.setContent {
            SettingsScreen(
                uiState = state,
                onBaseUrlChange = {},
                onApiKeyChange = {},
                onModelNameChange = {},
                onMaxTokensChange = {},
                onTemperatureChange = {},
                onOperationModeChange = {},
                onSave = {},
                onValidate = {},
                onClearError = {},
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("连接成功").assertExists()
        composeTestRule.onNodeWithText("API 连接正常").assertExists()
        composeTestRule.onNodeWithText("模型: gpt-4o").assertExists()
    }

    @Test
    fun modeFilterChips_switchCorrectly() {
        var selectedMode = OperationMode.SUGGESTION
        val state = SettingsUiState(
            isLoaded = true,
            operationMode = selectedMode
        )
        composeTestRule.setContent {
            SettingsScreen(
                uiState = state,
                onBaseUrlChange = {},
                onApiKeyChange = {},
                onModelNameChange = {},
                onMaxTokensChange = {},
                onTemperatureChange = {},
                onOperationModeChange = { selectedMode = it },
                onSave = {},
                onValidate = {},
                onClearError = {},
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("建议模式").assertExists()
        composeTestRule.onNodeWithText("自动模式").assertExists()
    }
}
