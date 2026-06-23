package com.monkeycode.aiscreen.core.model

import kotlinx.serialization.Serializable

@Serializable
data class AnalysisResult(
    val screenDescription: String,
    val keyElements: List<UIElementReference>,
    val suggestionText: String,
    val actions: List<Action>
)

@Serializable
data class UIElementReference(
    val elementIndex: Int,
    val label: String,
    val description: String
)
