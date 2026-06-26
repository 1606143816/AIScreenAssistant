package com.monkeycode.aiscreen.core.data.network

import com.monkeycode.aiscreen.core.model.LLMConfig
import com.monkeycode.aiscreen.core.model.SerializedUITree
import com.monkeycode.aiscreen.core.model.UIElement
import com.monkeycode.aiscreen.core.model.SerializableRect
import com.monkeycode.aiscreen.core.model.Action
import com.monkeycode.aiscreen.core.data.repository.LLMRepository
import com.monkeycode.aiscreen.core.data.network.NetworkMonitor
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LLMRepositoryIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var llmRepository: LLMRepository
    private lateinit var llmApiService: LLMApiService

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    private fun createRepository(): LLMRepository {
        val okHttpClient = okhttp3.OkHttpClient.Builder().build()
        llmApiService = LLMApiService(okHttpClient)
        val mockNetworkMonitor = mockk<NetworkMonitor> {
            every { isConnected } returns flowOf(true)
            every { isCurrentlyConnected() } returns true
        }
        return LLMRepository(llmApiService, mockNetworkMonitor)
    }

    @Test
    fun analyze_returnsResult_whenServerResponds200() = runTest {
        val sampleResponse = """
            {
                "screenDescription": "微信聊天界面",
                "keyElements": [
                    {"elementIndex": 0, "label": "输入框", "description": "文本输入区域"}
                ],
                "suggestionText": "您可以点击输入框开始输入文字",
                "actions": [
                    {"type": "CLICK", "elementIndex": 0}
                ]
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""
                    {
                        "choices": [{
                            "message": {
                                "content": ${"\""}${escapeJson(sampleResponse)}${"\""}
                            }
                        }]
                    }
                """.trimIndent())
        )

        llmRepository = createRepository()

        val uiTree = createSampleUITree()
        val config = LLMConfig(
            baseUrl = mockWebServer.url("/").toString(),
            apiKey = "test-key",
            modelName = "gpt-4o"
        )

        val result = llmRepository.analyze(uiTree, "分析这个页面", emptyList(), config)

        assertTrue(result.isSuccess)
        val analysis = result.getOrThrow()
        assertEquals("微信聊天界面", analysis.screenDescription)
        assertEquals(1, analysis.actions.size)
        assertTrue(analysis.actions[0] is Action.Click)
        assertEquals(0, (analysis.actions[0] as Action.Click).elementIndex)
    }

    @Test
    fun analyze_returnsFailure_whenServerReturns401() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setBody("{\"error\": {\"message\": \"Invalid API key\"}}")
        )

        llmRepository = createRepository()

        val uiTree = createSampleUITree()
        val config = LLMConfig(
            baseUrl = mockWebServer.url("/").toString(),
            apiKey = "invalid-key",
            modelName = "gpt-4o"
        )

        val result = llmRepository.analyze(uiTree, "测试", emptyList(), config)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is LLMException.AuthenticationError)
    }

    @Test
    fun analyze_returnsFailure_whenServerReturns429() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(429)
                .setBody("{\"error\": {\"message\": \"Rate limit exceeded\"}}")
        )

        llmRepository = createRepository()

        val uiTree = createSampleUITree()
        val config = LLMConfig(
            baseUrl = mockWebServer.url("/").toString(),
            apiKey = "test-key",
            modelName = "gpt-4o"
        )

        val result = llmRepository.analyze(uiTree, "测试", emptyList(), config)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is LLMException.RateLimitError)
    }

    @Test
    fun analyze_retriesOnServerError() = runTest {
        repeat(2) {
            mockWebServer.enqueue(
                MockResponse().setResponseCode(500)
            )
        }
        val successResponse = """
            {
                "choices": [{
                    "message": {
                        "content": {"screenDescription":"ok","keyElements":[],"suggestionText":"test","actions":[]}
                    }
                }]
            }
        """.trimIndent()
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(successResponse)
        )

        llmRepository = createRepository()

        val uiTree = createSampleUITree()
        val config = LLMConfig(
            baseUrl = mockWebServer.url("/").toString(),
            apiKey = "test-key",
            modelName = "gpt-4o"
        )

        val result = llmRepository.analyze(uiTree, "测试", emptyList(), config)

        assertTrue(result.isSuccess)
        assertEquals(3, mockWebServer.requestCount)
    }

    private fun createSampleUITree(): SerializedUITree {
        return SerializedUITree(
            packageName = "com.tencent.mm",
            activityName = "com.tencent.mm.ui.LauncherUI",
            elements = listOf(
                UIElement(
                    index = 0,
                    className = "android.widget.EditText",
                    text = "输入消息",
                    contentDescription = null,
                    hint = null,
                    bounds = SerializableRect(0, 100, 1080, 200),
                    isClickable = true,
                    isLongClickable = true,
                    isEditable = true,
                    isPassword = false,
                    isChecked = false,
                    isScrollable = false,
                    isFocused = false,
                    isEnabled = true,
                    childCount = 0,
                    depth = 3,
                    resourceId = "com.tencent.mm:id/input"
                ),
                UIElement(
                    index = 1,
                    className = "android.widget.Button",
                    text = "发送",
                    contentDescription = null,
                    hint = null,
                    bounds = SerializableRect(900, 100, 1000, 200),
                    isClickable = true,
                    isLongClickable = false,
                    isEditable = false,
                    isPassword = false,
                    isChecked = false,
                    isScrollable = false,
                    isFocused = false,
                    isEnabled = true,
                    childCount = 0,
                    depth = 3,
                    resourceId = "com.tencent.mm:id/send"
                )
            ),
            timestamp = System.currentTimeMillis()
        )
    }

    private fun escapeJson(input: String): String {
        return input
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
    }
}
