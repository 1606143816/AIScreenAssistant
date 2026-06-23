package com.monkeycode.aiscreen.core.model

import android.graphics.Rect
import kotlinx.serialization.Serializable

@Serializable
data class SerializedUITree(
    val packageName: String,
    val activityName: String? = null,
    val elements: List<UIElement>,
    val timestamp: Long
)

@Serializable
data class UIElement(
    val index: Int,
    val resourceId: String? = null,
    val className: String,
    val text: String? = null,
    val contentDescription: String? = null,
    val hint: String? = null,
    val bounds: SerializableRect,
    val isClickable: Boolean = false,
    val isLongClickable: Boolean = false,
    val isEditable: Boolean = false,
    val isPassword: Boolean = false,
    val isChecked: Boolean? = null,
    val isScrollable: Boolean = false,
    val isFocused: Boolean = false,
    val isEnabled: Boolean = true,
    val childCount: Int = 0,
    val depth: Int = 0
)

@Serializable
data class SerializableRect(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
) {
    val width: Int get() = right - left
    val height: Int get() = bottom - top
    val centerX: Int get() = left + width / 2
    val centerY: Int get() = top + height / 2

    companion object {
        fun fromRect(rect: Rect): SerializableRect {
            return SerializableRect(rect.left, rect.top, rect.right, rect.bottom)
        }
    }

    fun toRect(): Rect {
        return Rect(left, top, right, bottom)
    }
}
