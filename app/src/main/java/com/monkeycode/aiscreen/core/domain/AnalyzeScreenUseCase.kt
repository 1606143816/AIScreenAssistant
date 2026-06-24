package com.monkeycode.aiscreen.core.domain

import com.monkeycode.aiscreen.core.data.repository.LLMRepository
import com.monkeycode.aiscreen.core.data.repository.SettingsRepository
import com.monkeycode.aiscreen.core.model.AnalysisResult
import com.monkeycode.aiscreen.core.model.Message
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyzeScreenUseCase @Inject constructor(
    private val readUITreeUseCase: ReadUITreeUseCase,
    private val filterSensitiveNodesUseCase: FilterSensitiveNodesUseCase,
    private val llmRepository: LLMRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(
        userPrompt: String,
        conversationHistory: List<Message>
    ): Result<AnalysisResult> {
        val uiTree = readUITreeUseCase().getOrElse {
            return Result.failure(it)
        }

        val filteredTree = filterSensitiveNodesUseCase(uiTree)

        val config = settingsRepository.llmConfig.first()
            ?: return Result.failure(IllegalStateException("LLM configuration not set"))

        return llmRepository.analyze(filteredTree, userPrompt, conversationHistory, config)
    }
}
