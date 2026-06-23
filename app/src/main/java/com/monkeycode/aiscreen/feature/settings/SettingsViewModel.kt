package com.monkeycode.aiscreen.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monkeycode.aiscreen.core.data.repository.SettingsRepository
import com.monkeycode.aiscreen.core.domain.ValidateLLMConfigUseCase
import com.monkeycode.aiscreen.core.model.LLMConfig
import com.monkeycode.aiscreen.core.model.OperationMode
import com.monkeycode.aiscreen.core.model.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val baseUrl: String = "",
    val apiKey: String = "",
    val modelName: String = "",
    val maxTokens: String = "4096",
    val temperature: String = "0.7",
    val operationMode: OperationMode = OperationMode.SUGGESTION,
    val isSaving: Boolean = false,
    val isValidating: Boolean = false,
    val validationResult: ValidationResult? = null,
    val error: String? = null,
    val isLoaded: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val validateLLMConfigUseCase: ValidateLLMConfigUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val config = settingsRepository.llmConfig.first()
            val mode = settingsRepository.operationMode.first()

            _uiState.value = _uiState.value.copy(
                baseUrl = config?.baseUrl ?: "",
                apiKey = config?.apiKey ?: "",
                modelName = config?.modelName ?: "",
                maxTokens = config?.maxTokens?.toString() ?: "4096",
                temperature = config?.temperature?.toString() ?: "0.7",
                operationMode = mode,
                isLoaded = true
            )
        }
    }

    fun onBaseUrlChange(value: String) {
        _uiState.value = _uiState.value.copy(baseUrl = value)
    }

    fun onApiKeyChange(value: String) {
        _uiState.value = _uiState.value.copy(apiKey = value)
    }

    fun onModelNameChange(value: String) {
        _uiState.value = _uiState.value.copy(modelName = value)
    }

    fun onMaxTokensChange(value: String) {
        _uiState.value = _uiState.value.copy(maxTokens = value)
    }

    fun onTemperatureChange(value: String) {
        _uiState.value = _uiState.value.copy(temperature = value)
    }

    fun onOperationModeChange(mode: OperationMode) {
        viewModelScope.launch {
            settingsRepository.saveOperationMode(mode)
            _uiState.value = _uiState.value.copy(operationMode = mode)
        }
    }

    fun saveConfig() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)

            try {
                val config = LLMConfig(
                    baseUrl = _uiState.value.baseUrl.trim(),
                    apiKey = _uiState.value.apiKey.trim(),
                    modelName = _uiState.value.modelName.trim(),
                    maxTokens = _uiState.value.maxTokens.toIntOrNull() ?: 4096,
                    temperature = _uiState.value.temperature.toFloatOrNull() ?: 0.7f
                )

                if (!config.isValid) {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = "请填写完整的 LLM 配置信息"
                    )
                    return@launch
                }

                settingsRepository.saveLLMConfig(config)
                _uiState.value = _uiState.value.copy(isSaving = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "保存失败"
                )
            }
        }
    }

    fun validateConnection() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isValidating = true, validationResult = null)

            try {
                val config = LLMConfig(
                    baseUrl = _uiState.value.baseUrl.trim(),
                    apiKey = _uiState.value.apiKey.trim(),
                    modelName = _uiState.value.modelName.trim(),
                    maxTokens = _uiState.value.maxTokens.toIntOrNull() ?: 4096,
                    temperature = _uiState.value.temperature.toFloatOrNull() ?: 0.7f
                )

                val result = validateLLMConfigUseCase(config)
                _uiState.value = _uiState.value.copy(
                    isValidating = false,
                    validationResult = result
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isValidating = false,
                    error = e.message ?: "验证失败"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
