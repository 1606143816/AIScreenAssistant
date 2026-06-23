package com.monkeycode.aiscreen.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class Action {
    abstract val type: String

    @Serializable
    @SerialName("CLICK")
    data class Click(val elementIndex: Int) : Action() {
        override val type: String = "CLICK"
    }

    @Serializable
    @SerialName("LONG_CLICK")
    data class LongClick(val elementIndex: Int) : Action() {
        override val type: String = "LONG_CLICK"
    }

    @Serializable
    @SerialName("INPUT_TEXT")
    data class InputText(val elementIndex: Int, val text: String) : Action() {
        override val type: String = "INPUT_TEXT"
    }

    @Serializable
    @SerialName("SWIPE")
    data class Swipe(
        val startX: Int,
        val startY: Int,
        val endX: Int,
        val endY: Int,
        val duration: Long = 300
    ) : Action() {
        override val type: String = "SWIPE"
    }

    @Serializable
    @SerialName("PRESS_BACK")
    object PressBack : Action() {
        override val type: String = "PRESS_BACK"
    }

    @Serializable
    @SerialName("SCROLL_FORWARD")
    data class ScrollForward(val elementIndex: Int) : Action() {
        override val type: String = "SCROLL_FORWARD"
    }

    @Serializable
    @SerialName("SCROLL_BACKWARD")
    data class ScrollBackward(val elementIndex: Int) : Action() {
        override val type: String = "SCROLL_BACKWARD"
    }

    @Serializable
    @SerialName("OPEN_APP")
    data class OpenApp(val packageName: String) : Action() {
        override val type: String = "OPEN_APP"
    }
}

sealed class ActionResult {
    data class Success(val actionIndex: Int, val message: String = "") : ActionResult()
    data class Failure(val actionIndex: Int, val error: String) : ActionResult()
    data class Skipped(val actionIndex: Int, val reason: String) : ActionResult()
    data class NeedsConfirmation(val actionIndex: Int, val prompt: String) : ActionResult()
}
