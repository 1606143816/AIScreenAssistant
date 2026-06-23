package com.monkeycode.aiscreen.core.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class ActionTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun should_serialize_click_action_to_json() {
        val action = Action.Click(elementIndex = 3)
        val jsonString = json.encodeToString<Action>(action)
        assertTrue(jsonString.contains("\"type\":\"CLICK\""))
        assertTrue(jsonString.contains("\"elementIndex\":3"))
    }

    @Test
    fun should_deserialize_click_action_from_json() {
        val jsonString = """{"type":"CLICK","elementIndex":3}"""
        val action = json.decodeFromString<Action>(jsonString)
        assertTrue(action is Action.Click)
        assertEquals(3, (action as Action.Click).elementIndex)
    }

    @Test
    fun should_serialize_input_text_action_to_json() {
        val action = Action.InputText(elementIndex = 0, text = "hello")
        val jsonString = json.encodeToString<Action>(action)
        assertTrue(jsonString.contains("\"type\":\"INPUT_TEXT\""))
        assertTrue(jsonString.contains("\"text\":\"hello\""))
    }

    @Test
    fun should_deserialize_input_text_action_from_json() {
        val jsonString = """{"type":"INPUT_TEXT","elementIndex":0,"text":"hello"}"""
        val action = json.decodeFromString<Action>(jsonString)
        assertTrue(action is Action.InputText)
        val inputAction = action as Action.InputText
        assertEquals(0, inputAction.elementIndex)
        assertEquals("hello", inputAction.text)
    }

    @Test
    fun should_serialize_swipe_action_to_json() {
        val action = Action.Swipe(startX = 0, startY = 100, endX = 0, endY = 500)
        val jsonString = json.encodeToString<Action>(action)
        assertTrue(jsonString.contains("\"type\":\"SWIPE\""))
        assertTrue(jsonString.contains("\"startY\":100"))
        assertTrue(jsonString.contains("\"endY\":500"))
    }

    @Test
    fun should_deserialize_swipe_action_from_json() {
        val jsonString = """{"type":"SWIPE","startX":0,"startY":100,"endX":0,"endY":500,"duration":300}"""
        val action = json.decodeFromString<Action>(jsonString)
        assertTrue(action is Action.Swipe)
        val swipeAction = action as Action.Swipe
        assertEquals(100, swipeAction.startY)
        assertEquals(500, swipeAction.endY)
        assertEquals(300, swipeAction.duration)
    }

    @Test
    fun should_serialize_press_back_action_to_json() {
        val action = Action.PressBack
        val jsonString = json.encodeToString<Action>(action)
        assertTrue(jsonString.contains("\"type\":\"PRESS_BACK\""))
    }

    @Test
    fun should_deserialize_press_back_action_from_json() {
        val jsonString = """{"type":"PRESS_BACK"}"""
        val action = json.decodeFromString<Action>(jsonString)
        assertTrue(action is Action.PressBack)
    }

    @Test
    fun should_serialize_open_app_action_to_json() {
        val action = Action.OpenApp(packageName = "com.example.app")
        val jsonString = json.encodeToString<Action>(action)
        assertTrue(jsonString.contains("\"type\":\"OPEN_APP\""))
        assertTrue(jsonString.contains("\"packageName\":\"com.example.app\""))
    }

    @Test
    fun should_deserialize_open_app_action_from_json() {
        val jsonString = """{"type":"OPEN_APP","packageName":"com.example.app"}"""
        val action = json.decodeFromString<Action>(jsonString)
        assertTrue(action is Action.OpenApp)
        assertEquals("com.example.app", (action as Action.OpenApp).packageName)
    }

    @Test
    fun should_roundtrip_all_action_types() {
        val actions = listOf<Action>(
            Action.Click(0),
            Action.LongClick(1),
            Action.InputText(2, "test"),
            Action.Swipe(0, 0, 100, 100),
            Action.PressBack,
            Action.ScrollForward(5),
            Action.ScrollBackward(5),
            Action.OpenApp("com.test")
        )

        for (action in actions) {
            val serialized = json.encodeToString<Action>(action)
            val deserialized = json.decodeFromString<Action>(serialized)
            assertEquals(action.type, deserialized.type)
        }
    }
}
