package com.monkeycode.aiscreen.feature.conversation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
    import com.monkeycode.aiscreen.core.domain.AnalyzeScreenUseCase
import com.monkeycode.aiscreen.core.domain.ExecuteActionUseCase
import com.monkeycode.aiscreen.core.domain.ManageConversationUseCase
import com.monkeycode.aiscreen.core.domain.ProcessVoiceInputUseCase
import com.monkeycode.aiscreen.core.model.Action
import com.monkeycode.aiscreen.core.model.ActionResult
import com.monkeycode.aiscreen.core.model.AnalysisResult
import com.monkeycode.aiscreen.core.model.Message
import com.monkeycode.aiscreen.core.model.MessageRole
import com.monkeycode.aiscreen.core.model.OperationMode
import com.monkeycode.aiscreen.core.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ConversationUiState(
    val messages: List<MessageUiItem> = emptyList(),
    val isAnalyzing: Boolean = false,
    val currentOutput: String = "",
    val operationMode: OperationMode = OperationMode.SUGGESTION,
    val error: String? = null,
    val conversationId: String? = null,
    val currentApp: String? = null,
    val actionResults: List<ActionResult> = emptyList()
)

data class MessageUiItem(
    val id: String,
    val role: MessageRole,
    val content: String,
    val analysisResult: AnalysisResult? = null,
    val timestamp: Long = System.currentTimeMillis()
)

sealed class ConversationEvent {
    data class SendMessage(val text: String) : ConversationEvent()
    data class ActionsExecuted(val results: List<ActionResult>) : ConversationEvent()
    data object ToggleMode : ConversationEvent()
    data object ClearError : ConversationEvent()
    data object StartVoiceInput : ConversationEvent()
    data class VoiceResult(val text: String) : ConversationEvent()
    data class VoiceError(val error: String) : ConversationEvent()
}

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val analyzeScreenUseCase: AnalyzeScreenUseCase,
    private val executeActionUseCase: ExecuteActionUseCase,
    private val manageConversationUseCase: ManageConversationUseCase,
    private val settingsRepository: SettingsRepository,
    private val processVoiceInputUseCase: ProcessVoiceInputUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationUiState())
    val uiState: StateFlow<ConversationUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ConversationEvent>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            settingsRepository.operationMode.first().let { mode ->
                _uiState.value = _uiState.value.copy(operationMode = mode)
            }
        }
        viewModelScope.launch {
            settingsRepository.operationMode.collect { mode ->
                _uiState.value = _uiState.value.copy(operationMode = mode)
            }
        }
    }

    fun onEvent(event: ConversationEvent) {
        when (event) {
            is ConversationEvent.SendMessage -> sendMessage(event.text)
            is ConversationEvent.ActionsExecuted -> onActionsExecuted(event.results)
            is ConversationEvent.ToggleMode -> toggleMode()
            is ConversationEvent.ClearError -> clearError()
            is ConversationEvent.StartVoiceInput -> startVoiceInput()
            is ConversationEvent.VoiceResult -> sendMessage(event.text)
            is ConversationEvent.VoiceError -> {
                _uiState.value = _uiState.value.copy(error = event.error)
            }
        }
    }

    private fun sendMessage(text: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAnalyzing = true,
                currentOutput = "",
                error = null
            )

            val userMessage = MessageUiItem(
                id = UUID.randomUUID().toString(),
                role = MessageRole.USER,
                content = text
            )

            val state = _uiState.value
            val convoId = state.conversationId ?: run {
                val title = if (text.length > 20) text.take(20) + "..." else text
                val conversation = manageConversationUseCase.createConversation(title)
                conversation.id.also { id ->
                    _uiState.value = _uiState.value.copy(conversationId = id)
                }
            }

            _uiState.value = _uiState.value.copy(
                messages = _uiState.value.messages + userMessage,
                conversationId = convoId
            )

            persistMessage(convoId, userMessage)

            val history = _uiState.value.messages.map {
                Message(
                    id = it.id,
                    role = it.role,
                    content = it.content,
                    timestamp = it.timestamp
                )
            }

            val result = analyzeScreenUseCase(userPrompt = text, conversationHistory = history)

            result.fold(
                onSuccess = { analysis ->
                    val assistantMessage = MessageUiItem(
                        id = UUID.randomUUID().toString(),
                        role = MessageRole.ASSISTANT,
                        content = analysis.suggestionText,
                        analysisResult = analysis
                    )

                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + assistantMessage,
                        isAnalyzing = false,
                        currentOutput = analysis.suggestionText,
                        currentApp = analysis.screenDescription
                    )

                    persistMessage(convoId, assistantMessage)

                    if (_uiState.value.operationMode == OperationMode.AUTONOMOUS &&
                        analysis.actions.isNotEmpty()) {
                        executeActions(analysis.actions)
                    }
                },
                onFailure = { throwable ->
                    val errorMessage = MessageUiItem(
                        id = UUID.randomUUID().toString(),
                        role = MessageRole.ASSISTANT,
                        content = "分析失败: ${throwable.message}"
                    )

                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + errorMessage,
                        isAnalyzing = false,
                        error = throwable.message
                    )

                    persistMessage(convoId, errorMessage)
                }
            )
        }
    }

    private suspend fun persistMessage(conversationId: String, message: MessageUiItem) {
        manageConversationUseCase.appendMessage(
            conversationId,
            Message(
                id = message.id,
                role = message.role,
                content = message.content,
                analysisResult = message.analysisResult,
                timestamp = message.timestamp
            )
        )
    }

    private fun executeActions(actions: List<com.monkeycode.aiscreen.core.model.Action>) {
        viewModelScope.launch {
            executeActionUseCase(actions).collect { result ->
                val currentResults = _uiState.value.actionResults + result
                _uiState.value = _uiState.value.copy(actionResults = currentResults)
            }
        }
    }

    private fun onActionsExecuted(results: List<ActionResult>) {
        _uiState.value = _uiState.value.copy(actionResults = results)
    }

    private fun toggleMode() {
        viewModelScope.launch {
            val newMode = when (_uiState.value.operationMode) {
                OperationMode.SUGGESTION -> OperationMode.AUTONOMOUS
                OperationMode.AUTONOMOUS -> OperationMode.SUGGESTION
            }
            settingsRepository.saveOperationMode(newMode)
        }
    }

    fun startVoiceInput() {
        processVoiceInputUseCase.startListening(
            onResult = { text ->
                viewModelScope.launch {
                    onEvent(ConversationEvent.VoiceResult(text))
                }
            },
            onError = { error ->
                viewModelScope.launch {
                    onEvent(ConversationEvent.VoiceError(error))
                }
            },
            onPartialResult = { partial ->
                viewModelScope.launch {
                    _uiState.value = _uiState.value.copy(currentOutput = partial)
                }
            }
        )
    }

    private fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
