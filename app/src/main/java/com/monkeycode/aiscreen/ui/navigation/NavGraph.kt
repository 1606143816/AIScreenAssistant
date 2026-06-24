package com.monkeycode.aiscreen.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.monkeycode.aiscreen.feature.conversation.ConversationViewModel
import com.monkeycode.aiscreen.feature.history.HistoryViewModel
import com.monkeycode.aiscreen.feature.settings.SettingsViewModel
import com.monkeycode.aiscreen.ui.screen.ConversationScreen
import com.monkeycode.aiscreen.ui.screen.HistoryScreen
import com.monkeycode.aiscreen.ui.screen.HomeScreen
import com.monkeycode.aiscreen.ui.screen.SettingsScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    accessibilityEnabled: Boolean,
    llmConfigured: Boolean,
    isOnline: Boolean = true,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                accessibilityEnabled = accessibilityEnabled,
                llmConfigured = llmConfigured,
                isOnline = isOnline,
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                },
                onNavigateToConversation = {
                    navController.navigate(Screen.Conversation.route)
                }
            )
        }

        composable(Screen.Conversation.route) {
            val viewModel: ConversationViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()
            ConversationScreen(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            val viewModel: SettingsViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()
            SettingsScreen(
                uiState = uiState,
                onBaseUrlChange = viewModel::onBaseUrlChange,
                onApiKeyChange = viewModel::onApiKeyChange,
                onModelNameChange = viewModel::onModelNameChange,
                onMaxTokensChange = viewModel::onMaxTokensChange,
                onTemperatureChange = viewModel::onTemperatureChange,
                onOperationModeChange = viewModel::onOperationModeChange,
                onSave = viewModel::saveConfig,
                onValidate = viewModel::validateConnection,
                onClearError = viewModel::clearError,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.History.route) {
            val viewModel: HistoryViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()
            HistoryScreen(
                uiState = uiState,
                onSelectConversation = viewModel::selectConversation,
                onDeleteConversation = viewModel::deleteConversation,
                onClearSelection = viewModel::clearSelection,
                onClearError = viewModel::clearError,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
