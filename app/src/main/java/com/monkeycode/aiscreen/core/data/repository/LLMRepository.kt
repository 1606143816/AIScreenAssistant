package com.monkeycode.aiscreen.core.data.repository

import com.monkeycode.aiscreen.core.data.network.LLMApiService
import com.monkeycode.aiscreen.core.data.network.NetworkMonitor
import com.monkeycode.aiscreen.core.data.network.LLMException
import com.monkeycode.aiscreen.core.model.AnalysisResult
import com.monkeycode.aiscreen.core.model.LLMConfig
import com.monkeycode.aiscreen.core.model.Message
import com.monkeycode.aiscreen.core.model.SerializedUITree
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

data class QueuedRequest(
    val uiTree: SerializedUITree,
    val userPrompt: String,
    val history: List<Message>,
    val config: LLMConfig
)

@Singleton
class LLMRepository @Inject constructor(
    private val llmApiService: LLMApiService,
    private val networkMonitor: NetworkMonitor
) {
    private val requestQueue = mutableListOf<QueuedRequest>()
    private val queueMutex = Mutex()
    private var retryCount = 0
    private val maxRetries = 3
    private val retryDelayMs = 2000L

    suspend fun analyze(
        uiTree: SerializedUITree,
        userPrompt: String,
        conversationHistory: List<Message>,
        config: LLMConfig
    ): Result<AnalysisResult> {
        if (!networkMonitor.isCurrentlyConnected()) {
            queueRequest(uiTree, userPrompt, conversationHistory, config)
            return Result.failure(LLMException.NetworkError("No network connection, request queued"))
        }

        return executeWithRetry(uiTree, userPrompt, conversationHistory, config)
    }

    suspend fun validateConnection(config: LLMConfig): Result<String> {
        return llmApiService.validateConnection(config)
    }

    suspend fun retryQueuedRequests(): List<Result<AnalysisResult>> {
        val results = mutableListOf<Result<AnalysisResult>>()
        queueMutex.withLock {
            val requests = requestQueue.toList()
            requestQueue.clear()

            for (request in requests) {
                val result = llmApiService.analyze(
                    request.uiTree,
                    request.userPrompt,
                    request.history,
                    request.config
                )
                results.add(result)
            }
        }
        return results
    }

    fun hasQueuedRequests(): Boolean {
        return requestQueue.isNotEmpty()
    }

    val queuedCount: Int get() = requestQueue.size

    private suspend fun executeWithRetry(
        uiTree: SerializedUITree,
        userPrompt: String,
        history: List<Message>,
        config: LLMConfig
    ): Result<AnalysisResult> {
        var lastError: Throwable? = null

        for (attempt in 1..maxRetries) {
            val result = llmApiService.analyze(uiTree, userPrompt, history, config)
            if (result.isSuccess) return result

            val error = result.exceptionOrNull()
            if (error is LLMException.AuthenticationError || error is LLMException.ParseError) {
                return result
            }

            lastError = error
            if (attempt < maxRetries) {
                kotlinx.coroutines.delay(retryDelayMs * attempt)
            }
        }

        return Result.failure(
            lastError ?: LLMException.UnknownError("Max retries reached")
        )
    }

    private suspend fun queueRequest(
        uiTree: SerializedUITree,
        userPrompt: String,
        history: List<Message>,
        config: LLMConfig
    ) {
        queueMutex.withLock {
            if (requestQueue.size >= 10) {
                requestQueue.removeAt(0)
            }
            requestQueue.add(
                QueuedRequest(uiTree, userPrompt, history, config)
            )
        }
    }
}
