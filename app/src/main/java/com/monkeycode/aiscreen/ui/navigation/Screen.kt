package com.monkeycode.aiscreen.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Conversation : Screen("conversation")
    data object Settings : Screen("settings")
    data object History : Screen("history")
}
