package com.monkeycode.aiscreen.core.model

import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class AnalysisResultTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun should_deserialize_full_analysis_result() {
        val jsonString = """
        {
            "screenDescription": "当前界面是微信聊天列表页",
            "keyElements": [
                {"elementIndex": 0, "label": "搜索框", "description": "顶部的搜索输入框"},
                {"elementIndex": 5, "label": "第一个聊天", "description": "列表第一项"}
            ],
            "suggestionText": "您可以点击搜索框查找联系人",
            "actions": [
                {"type": "CLICK", "elementIndex": 0},
                {"type": "INPUT_TEXT", "elementIndex": 0, "text": "张三"}
            ]
        }
        """.trimIndent()

        val result = json.decodeFromString<AnalysisResult>(jsonString)
        assertEquals("当前界面是微信聊天列表页", result.screenDescription)
        assertEquals(2, result.keyElements.size)
        assertEquals("搜索框", result.keyElements[0].label)
        assertEquals("顶部的搜索输入框", result.keyElements[0].description)
        assertEquals(2, result.actions.size)
        assertTrue(result.actions[0] is Action.Click)
        assertEquals(0, (result.actions[0] as Action.Click).elementIndex)
        assertTrue(result.actions[1] is Action.InputText)
        assertEquals("张三", (result.actions[1] as Action.InputText).text)
    }

    @Test
    fun should_deserialize_result_with_empty_actions() {
        val jsonString = """
        {
            "screenDescription": "当前界面显示天气信息",
            "keyElements": [],
            "suggestionText": "这是今天的天气情况",
            "actions": []
        }
        """.trimIndent()

        val result = json.decodeFromString<AnalysisResult>(jsonString)
        assertEquals("当前界面显示天气信息", result.screenDescription)
        assertTrue(result.keyElements.isEmpty())
        assertTrue(result.actions.isEmpty())
    }

    @Test
    fun should_deserialize_result_with_swipe_action() {
        val jsonString = """
        {
            "screenDescription": "列表可向下滚动查看更多",
            "keyElements": [],
            "suggestionText": "向下滑动查看更多内容",
            "actions": [
                {"type": "SWIPE", "startX": 540, "startY": 1800, "endX": 540, "endY": 400}
            ]
        }
        """.trimIndent()

        val result = json.decodeFromString<AnalysisResult>(jsonString)
        assertEquals(1, result.actions.size)
        assertTrue(result.actions[0] is Action.Swipe)
        val swipe = result.actions[0] as Action.Swipe
        assertEquals(540, swipe.startX)
        assertEquals(1800, swipe.startY)
    }

    @Test
    fun should_serialize_analysis_result_to_json() {
        val result = AnalysisResult(
            screenDescription = "测试界面",
            keyElements = listOf(UIElementReference(0, "按钮", "点击按钮")),
            suggestionText = "点击按钮执行操作",
            actions = listOf(Action.Click(0))
        )

        val jsonString = json.encodeToString(AnalysisResult.serializer(), result)
        assertTrue(jsonString.contains("\"screenDescription\":\"测试界面\""))
        assertTrue(jsonString.contains("\"elementIndex\":0"))
        assertTrue(jsonString.contains("\"type\":\"CLICK\""))
    }

    @Test
    fun should_roundtrip_analysis_result() {
        val original = AnalysisResult(
            screenDescription = "界面描述",
            keyElements = listOf(
                UIElementReference(0, "输入框", "文本输入"),
                UIElementReference(1, "确认按钮", "提交表单")
            ),
            suggestionText = "请输入内容后点击确认",
            actions = listOf(
                Action.InputText(0, "hello"),
                Action.Click(1)
            )
        )

        val serialized = json.encodeToString(AnalysisResult.serializer(), original)
        val deserialized = json.decodeFromString<AnalysisResult>(serialized)

        assertEquals(original.screenDescription, deserialized.screenDescription)
        assertEquals(original.keyElements.size, deserialized.keyElements.size)
        assertEquals(original.actions.size, deserialized.actions.size)
        assertEquals(original.keyElements[0].label, deserialized.keyElements[0].label)
        assertTrue(deserialized.actions[0] is Action.InputText)
        assertTrue(deserialized.actions[1] is Action.Click)
    }
}
