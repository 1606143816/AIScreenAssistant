package com.monkeycode.aiscreen

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.monkeycode.aiscreen.core.data.network.NetworkMonitor
import com.monkeycode.aiscreen.core.data.repository.SettingsRepository
import com.monkeycode.aiscreen.service.accessibility.AIAccessibilityService
import com.monkeycode.aiscreen.service.overlay.OverlayService
import com.monkeycode.aiscreen.ui.navigation.AppNavGraph
import com.monkeycode.aiscreen.ui.navigation.Screen
import com.monkeycode.aiscreen.ui.theme.AIScreenAssistantTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    private var accessibilityEnabled by mutableStateOf(false)
    private var llmConfigured by mutableStateOf(false)
    private var isOnline by mutableStateOf(true)

    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val overlayReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                OverlayService.ACTION_ANALYZE -> {
                    navigateToConversation()
                }
                OverlayService.ACTION_VOICE_INPUT -> {
                    navigateToConversation()
                }
            }
        }
    }

    private var navController: androidx.navigation.NavHostController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerReceiver(
            overlayReceiver,
            IntentFilter().apply {
                addAction(OverlayService.ACTION_ANALYZE)
                addAction(OverlayService.ACTION_VOICE_INPUT)
            },
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        checkStatuses()

        setContent {
            val isNetworkOnline by networkMonitor.isConnected.collectAsStateWithLifecycle(
                initialValue = true
            )

            AIScreenAssistantTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    navController = rememberNavController().also {
                        AppNavGraph(
                            navController = it,
                            accessibilityEnabled = accessibilityEnabled,
                            llmConfigured = llmConfigured,
                            isOnline = isNetworkOnline
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        unregisterReceiver(overlayReceiver)
        super.onDestroy()
    }

    private fun checkStatuses() {
        accessibilityEnabled = AIAccessibilityService.isRunning

        activityScope.launch {
            try {
                val config = settingsRepository.llmConfig.first()
                llmConfigured = config != null && config.isValid
            } catch (e: Exception) {
                llmConfigured = false
            }
        }
    }

    private fun navigateToConversation() {
        navController?.navigate(Screen.Conversation.route) {
            launchSingleTop = true
        }
    }
}
