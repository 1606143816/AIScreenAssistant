package com.monkeycode.aiscreen.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monkeycode.aiscreen.core.domain.ManageConversationUseCase
import com.monkeycode.aiscreen.core.model.Conversation
import com.monkeycode.aiscreen.core.model.ConversationSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val conversations: List<ConversationSummary> = emptyList(),
    val selectedConversation: Conversation? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val manageConversationUseCase: ManageConversationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            manageConversationUseCase.getHistory().collect { list ->
                _uiState.value = _uiState.value.copy(conversations = list)
            }
        }
    }

    fun selectConversation(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val conversation = manageConversationUseCase.getConversation(id)
                _uiState.value = _uiState.value.copy(
                    selectedConversation = conversation,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "加载失败"
                )
            }
        }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            try {
                manageConversationUseCase.delete(id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "删除失败"
                )
            }
        }
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedConversation = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
