package com.monkeycode.aiscreen.core.data.network

import com.monkeycode.aiscreen.core.model.Action
import com.monkeycode.aiscreen.core.model.AnalysisResult
import com.monkeycode.aiscreen.core.model.LLMConfig
import com.monkeycode.aiscreen.core.model.Message
import com.monkeycode.aiscreen.core.model.MessageRole
import com.monkeycode.aiscreen.core.model.UIElement
import com.monkeycode.aiscreen.core.model.SerializableRect
import com.monkeycode.aiscreen.core.model.SerializedUITree
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import kotlinx.coroutines.runBlocking

class LLMApiServiceTest {

    private lateinit var mockServer: MockWebServer
    private lateinit var apiService: LLMApiService
    private val json = Json { ignoreUnknownKeys = true }

    @Before
    fun setUp() {
        mockServer = MockWebServer()
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        apiService = LLMApiService(client)
    }

    @After
    fun tearDown() {
        mockServer.shutdown()
    }

    @Test
    fun should_parse_valid_analysis_result_response() = runBlocking {
        val responseJson = """
        {
            "choices": [
                {
                    "message": {
                        "role": "assistant",
                        "content": "{\"screenDescription\":\"微信聊天列表\",\"keyElements\":[{\"elementIndex\":0,\"label\":\"搜索\",\"description\":\"搜索框\"}],\"suggestionText\":\"点击搜索\",\"actions\":[{\"type\":\"CLICK\",\"elementIndex\":0}]}"
                    }
                }
            ]
        }
        """.trimIndent()

        mockServer.enqueue(MockResponse().setBody(responseJson).setResponseCode(200))
        mockServer.start()

        val config = LLMConfig(
            baseUrl = mockServer.url("/").toString().trimEnd('/'),
            apiKey = "sk-test",
            modelName = "gpt-4o"
        )

        val uiTree = SerializedUITree(
            packageName = "com.test",
            elements = listOf(
                UIElement(
                    index = 0, className = "Button", text = "搜索",
                    bounds = SerializableRect(0, 0, 100, 50),
                    isClickable = true, depth = 0
                )
            ),
            timestamp = 1000
        )

        val result = apiService.analyze(uiTree, "搜索", emptyList(), config)

        assertTrue(result.isSuccess)
        val analysis = result.getOrNull()
        assertNotNull(analysis)
        assertEquals("微信聊天列表", analysis?.screenDescription)
        assertEquals(1, analysis?.keyElements?.size)
        assertEquals(1, analysis?.actions?.size)
        assertTrue(analysis?.actions?.get(0) is Action.Click)
    }

    @Test
    fun should_extract_json_from_text_wrapped_response() = runBlocking {
        val responseJson = """
        {
            "choices": [
                {
                    "message": {
                        "role": "assistant",
                        "content": "好的，这是我的分析：\n{\"screenDescription\":\"设置页面\",\"keyElements\":[],\"suggestionText\":\"这是设置页\",\"actions\":[]}"
                    }
                }
            ]
        }
        """.trimIndent()

        mockServer.enqueue(MockResponse().setBody(responseJson).setResponseCode(200))
        mockServer.start()

        val config = LLMConfig(
            baseUrl = mockServer.url("/").toString().trimEnd('/'),
            apiKey = "sk-test",
            modelName = "gpt-4o"
        )

        val uiTree = SerializedUITree(
            packageName = "com.test",
            elements = emptyList(),
            timestamp = 1000
        )

        val result = apiService.analyze(uiTree, "这是什么", emptyList(), config)
        assertTrue(result.isSuccess)
        assertEquals("设置页面", result.getOrNull()?.screenDescription)
    }

    @Test
    fun should_handle_authentication_error() = runBlocking {
        mockServer.enqueue(MockResponse().setResponseCode(401).setBody("{\"error\":{\"message\":\"Invalid API key\"}}"))
        mockServer.start()

        val config = LLMConfig(
            baseUrl = mockServer.url("/").toString().trimEnd('/'),
            apiKey = "bad-key",
            modelName = "gpt-4o"
        )

        val uiTree = SerializedUITree("com.test", elements = emptyList(), timestamp = 1000)
        val result = apiService.analyze(uiTree, "test", emptyList(), config)

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is LLMException.AuthenticationError)
    }

    @Test
    fun should_handle_rate_limit_error() = runBlocking {
        mockServer.enqueue(MockResponse().setResponseCode(429))
        mockServer.start()

        val config = LLMConfig(
            baseUrl = mockServer.url("/").toString().trimEnd('/'),
            apiKey = "sk-test",
            modelName = "gpt-4o"
        )

        val uiTree = SerializedUITree("com.test", elements = emptyList(), timestamp = 1000)
        val result = apiService.analyze(uiTree, "test", emptyList(), config)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is LLMException.RateLimitError)
    }

    @Test
    fun should_handle_server_error() = runBlocking {
        mockServer.enqueue(MockResponse().setResponseCode(500))
        mockServer.start()

        val config = LLMConfig(
            baseUrl = mockServer.url("/").toString().trimEnd('/'),
            apiKey = "sk-test",
            modelName = "gpt-4o"
        )

        val uiTree = SerializedUITree("com.test", elements = emptyList(), timestamp = 1000)
        val result = apiService.analyze(uiTree, "test", emptyList(), config)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is LLMException.ServerError)
    }

    @Test
    fun should_encode_conversation_history_in_request() = runBlocking {
        val responseJson = """
        {"choices":[{"message":{"role":"assistant","content":"{\"screenDescription\":\"Test\",\"keyElements\":[],\"suggestionText\":\"\",\"actions\":[]}"}}]}
        """.trimIndent()
        mockServer.enqueue(MockResponse().setBody(responseJson).setResponseCode(200))
        mockServer.start()

        val config = LLMConfig(
            baseUrl = mockServer.url("/").toString().trimEnd('/'),
            apiKey = "sk-test",
            modelName = "gpt-4o"
        )

        val uiTree = SerializedUITree("com.test", elements = emptyList(), timestamp = 1000)

        val history = listOf(
            Message("1", MessageRole.USER, "Hello", null, 1000),
            Message("2", MessageRole.ASSISTANT, "Hi there", null, 2000)
        )

        val result = apiService.analyze(uiTree, "Continue", history, config)

        assertTrue(result.isSuccess)

        val recordedRequest = mockServer.takeRequest()
        val requestBody = recordedRequest.body.readUtf8()
        assertTrue(requestBody.contains("\"role\":\"system\""))
        assertTrue(requestBody.contains("\"role\":\"user\""))
        assertTrue(requestBody.contains("\"role\":\"assistant\""))
        assertTrue(requestBody.contains("Continue"))
    }

    @Test
    fun should_validate_connection_successfully() = runBlocking {
        mockServer.enqueue(MockResponse().setResponseCode(200))
        mockServer.start()

        val config = LLMConfig(
            baseUrl = mockServer.url("/").toString().trimEnd('/'),
            apiKey = "sk-test",
            modelName = "gpt-4o"
        )

        val result = apiService.validateConnection(config)
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.contains("available") == true)
    }
}
