package com.monkeycode.aiscreen.core.model

data class LLMConfig(
    val baseUrl: String,
    val apiKey: String,
    val modelName: String,
    val maxTokens: Int = 4096,
    val temperature: Float = 0.7f
) {
    val chatCompletionsUrl: String
        get() = baseUrl.trimEnd('/') + "/v1/chat/completions"

    val isValid: Boolean
        get() = baseUrl.isNotBlank() && apiKey.isNotBlank() && modelName.isNotBlank()
}

data class ValidationResult(
    val success: Boolean,
    val message: String,
    val modelName: String? = null
)
