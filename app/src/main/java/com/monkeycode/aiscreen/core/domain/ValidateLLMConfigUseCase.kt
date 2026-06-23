package com.monkeycode.aiscreen.core.domain

import com.monkeycode.aiscreen.core.data.repository.LLMRepository
import com.monkeycode.aiscreen.core.model.LLMConfig
import com.monkeycode.aiscreen.core.model.ValidationResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ValidateLLMConfigUseCase @Inject constructor(
    private val llmRepository: LLMRepository
) {
    suspend operator fun invoke(config: LLMConfig): ValidationResult {
        if (!config.isValid) {
            return ValidationResult(
                success = false,
                message = "Configuration is incomplete: baseUrl, apiKey, and modelName are required"
            )
        }

        val result = llmRepository.validateConnection(config)

        return if (result.isSuccess) {
            ValidationResult(
                success = true,
                message = result.getOrDefault("Connection successful"),
                modelName = config.modelName
            )
        } else {
            ValidationResult(
                success = false,
                message = result.exceptionOrNull()?.message ?: "Connection failed"
            )
        }
    }
}
