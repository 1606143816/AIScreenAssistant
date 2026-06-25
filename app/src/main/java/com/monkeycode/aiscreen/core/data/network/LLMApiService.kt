package com.monkeycode.aiscreen.core.data.network

import com.monkeycode.aiscreen.core.model.AnalysisResult
import com.monkeycode.aiscreen.core.model.LLMConfig
import com.monkeycode.aiscreen.core.model.Message
import com.monkeycode.aiscreen.core.model.SerializedUITree
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LLMApiService @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    companion object {
        val systemPrompt = """
你是一个 Android 手机操作助手。用户会提供当前界面的 UI 树结构（JSON 格式），
包含每个元素的类型、文字、边界坐标和交互属性。

你的任务是：
1. 理解用户的意图
2. 描述当前界面的功能和布局
3. 如果用户想要执行操作，给出精确的操作指令序列

输出格式（严格 JSON）：
{
  "screenDescription": "当前界面的自然语言描述，说明界面是什么、有哪些功能区域",
  "keyElements": [
    {"elementIndex": <int>, "label": "元素标签", "description": "元素描述和用途"}
  ],
  "suggestionText": "给用户的操作建议文本，告诉用户可以做什么",
  "actions": [
    {"type": "CLICK", "elementIndex": <int>},
    {"type": "INPUT_TEXT", "elementIndex": <int>, "text": "要输入的文本"},
    {"type": "SWIPE", "startX": <int>, "startY": <int>, "endX": <int>, "endY": <int>},
    {"type": "PRESS_BACK"},
    {"type": "SCROLL_FORWARD", "elementIndex": <int>},
    {"type": "SCROLL_BACKWARD", "elementIndex": <int>},
    {"type": "OPEN_APP", "packageName": "com.example.app"}
  ]
}

注意事项：
- elementIndex 必须与 UI 树 elements 数组的下标严格对应
- 如果用户只是询问信息而不需要操作，actions 可以为空数组
- 密码字段的文本已被替换为 "[已隐藏]"，请勿尝试读取
- 界面元素坐标以像素为单位
- 返回的必须是合法的 JSON，不要包含任何额外的解释文字
        """.trimIndent()

        const val MAX_TIMEOUT_SECONDS = 30L
    }

    @Serializable
    data class ChatMessage(
        val role: String,
        val content: String
    )

    @Serializable
    data class ChatRequest(
        val model: String,
        val messages: List<ChatMessage>,
        @SerialName("max_tokens") val maxTokens: Int = 4096,
        val temperature: Float = 0.7f
    )

    @Serializable
    data class ChatResponse(
        val choices: List<Choice> = emptyList(),
        val error: ChatError? = null
    )

    @Serializable
    data class Choice(
        val message: ChoiceMessage = ChoiceMessage("", "")
    )

    @Serializable
    data class ChoiceMessage(
        val role: String,
        val content: String
    )

    @Serializable
    data class ChatError(
        val message: String,
        val type: String? = null,
        val code: String? = null
    )

    suspend fun analyze(
        uiTree: SerializedUITree,
        userPrompt: String,
        conversationHistory: List<Message>,
        config: LLMConfig
    ): Result<AnalysisResult> {
        return try {
            val requestBody = buildChatRequest(uiTree, userPrompt, conversationHistory, config)
            val response = executeRequest(config, requestBody)
            parseResponse(response)
        } catch (e: IOException) {
            Result.failure(LLMException.NetworkError("Network error: ${e.message}", e))
        } catch (e: LLMException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(LLMException.UnknownError("Unexpected error: ${e.message}", e))
        }
    }

    suspend fun validateConnection(config: LLMConfig): Result<String> {
        return try {
            val requestBody = buildJsonObject {
                put("model", config.modelName)
                putJsonArray("messages") {
                    +buildJsonObject {
                        put("role", "user")
                        put("content", "ping")
                    }
                }
                put("max_tokens", 5)
            }

            val jsonString = json.encodeToString(requestBody)
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val request = Request.Builder()
                .url(config.chatCompletionsUrl)
                .post(jsonString.toRequestBody(mediaType))
                .header("Authorization", "Bearer ${config.apiKey}")
                .header("Content-Type", "application/json")
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success("Connection successful, model ${config.modelName} is available")
            } else {
                val code = response.code
                val body = response.body?.string() ?: ""
                if (code == 401 || code == 403) {
                    Result.failure(LLMException.AuthenticationError("Authentication failed: HTTP $code"))
                } else {
                    Result.failure(LLMException.ApiError("API error: HTTP $code", code, body))
                }
            }
        } catch (e: IOException) {
            Result.failure(LLMException.NetworkError("Cannot reach API endpoint", e))
        }
    }

    private fun buildChatRequest(
        uiTree: SerializedUITree,
        userPrompt: String,
        conversationHistory: List<Message>,
        config: LLMConfig
    ): String {
        val messages = mutableListOf<ChatMessage>()

        messages.add(ChatMessage(role = "system", content = systemPrompt))

        for (msg in conversationHistory) {
            messages.add(
                ChatMessage(
                    role = if (msg.role.name == "USER") "user" else "assistant",
                    content = msg.content
                )
            )
        }

        val uiTreeJson = json.encodeToString(uiTree)
        val userContent = "当前界面 UI 树:\n$uiTreeJson\n\n用户指令: $userPrompt"
        messages.add(ChatMessage(role = "user", content = userContent))

        val request = ChatRequest(
            model = config.modelName,
            messages = messages,
            maxTokens = config.maxTokens,
            temperature = config.temperature
        )

        return json.encodeToString(request)
    }

    private suspend fun executeRequest(config: LLMConfig, requestBody: String): String {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val request = Request.Builder()
            .url(config.chatCompletionsUrl)
            .post(requestBody.toRequestBody(mediaType))
            .header("Authorization", "Bearer ${config.apiKey}")
            .header("Content-Type", "application/json")
            .build()

        val response = okHttpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val code = response.code
            when (code) {
                401, 403 -> throw LLMException.AuthenticationError("Authentication failed: HTTP $code")
                429 -> throw LLMException.RateLimitError("Rate limit exceeded")
                500, 502, 503 -> throw LLMException.ServerError("Server error: HTTP $code")
                else -> throw LLMException.ApiError("API error: HTTP $code", code, responseBody)
            }
        }

        return responseBody
    }

    private fun parseResponse(responseBody: String): Result<AnalysisResult> {
        return try {
            val chatResponse = json.decodeFromString<ChatResponse>(responseBody)

            if (chatResponse.choices.isEmpty()) {
                return Result.failure(LLMException.EmptyResponseError("No choices in response"))
            }

            val content = chatResponse.choices[0].message.content

            val analysisResult = try {
                json.decodeFromString<AnalysisResult>(content)
            } catch (e: Exception) {
                val jsonStart = content.indexOf('{')
                val jsonEnd = content.lastIndexOf('}') + 1
                if (jsonStart >= 0 && jsonEnd > jsonStart) {
                    val extracted = content.substring(jsonStart, jsonEnd)
                    json.decodeFromString<AnalysisResult>(extracted)
                } else {
                    throw LLMException.ParseError("Failed to parse LLM response as AnalysisResult JSON", content)
                }
            }

            Result.success(analysisResult)
        } catch (e: LLMException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(LLMException.ParseError("Failed to parse API response", responseBody, e))
        }
    }
}

sealed class LLMException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    class NetworkError(message: String, cause: Throwable? = null) : LLMException(message, cause)
    class AuthenticationError(message: String) : LLMException(message)
    class RateLimitError(message: String) : LLMException(message)
    class ServerError(message: String) : LLMException(message)
    class ApiError(message: String, val httpCode: Int, val responseBody: String? = null) :
        LLMException(message)

    class EmptyResponseError(message: String) : LLMException(message)
    class ParseError(message: String, val rawResponse: String? = null, cause: Throwable? = null) :
        LLMException(message, cause)

    class UnknownError(message: String, cause: Throwable? = null) : LLMException(message, cause)
}
