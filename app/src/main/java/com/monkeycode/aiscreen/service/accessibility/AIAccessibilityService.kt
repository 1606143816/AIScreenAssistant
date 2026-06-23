package com.monkeycode.aiscreen.service.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.monkeycode.aiscreen.core.domain.AccessibilityServiceBridge
import com.monkeycode.aiscreen.core.model.Action
import com.monkeycode.aiscreen.core.model.ActionResult
import com.monkeycode.aiscreen.core.model.SerializedUITree
import com.monkeycode.aiscreen.core.serializer.UITreeSerializer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

@AndroidEntryPoint
class AIAccessibilityService : AccessibilityService(), AccessibilityServiceBridge {

    @Inject
    lateinit var uiTreeSerializer: UITreeSerializer

    private val _actionResults = MutableSharedFlow<ActionResult>(replay = 0, extraBufferCapacity = 64)
    val actionResults: SharedFlow<ActionResult> = _actionResults.asSharedFlow()

    companion object {
        @Volatile
        private var instance: AIAccessibilityService? = null

        fun getInstance(): AIAccessibilityService? = instance

        val isRunning: Boolean get() = instance != null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
    }

    override fun isEnabled(): Boolean = true

    override fun readUITree(): SerializedUITree? {
        val root = rootInActiveWindow ?: return null
        return try {
            uiTreeSerializer.serialize(root)
        } finally {
            root.recycle()
        }
    }

    override suspend fun performAction(action: Action): ActionResult {
        val resultDeferred = CompletableDeferred<ActionResult>()

        try {
            when (action) {
                is Action.Click -> executeClick(action.elementIndex)
                is Action.LongClick -> executeLongClick(action.elementIndex)
                is Action.InputText -> executeInputText(action.elementIndex, action.text)
                is Action.Swipe -> executeSwipe(action)
                is Action.PressBack -> executeBack()
                is Action.ScrollForward -> executeScrollForward(action.elementIndex)
                is Action.ScrollBackward -> executeScrollBackward(action.elementIndex)
                is Action.OpenApp -> executeOpenApp(action.packageName)
            }

            val result = ActionResult.Success(action.hashCode(), "Action executed")
            _actionResults.tryEmit(result)
            return result
        } catch (e: Exception) {
            val result = ActionResult.Failure(action.hashCode(), e.message ?: "Unknown error")
            _actionResults.tryEmit(result)
            return result
        }
    }

    private fun findNodeByElementIndex(elementIndex: Int): AccessibilityNodeInfo? {
        val root = rootInActiveWindow ?: return null
        val allNodes = mutableListOf<AccessibilityNodeInfo>()
        collectAllNodes(root, allNodes)
        root.recycle()

        return if (elementIndex in allNodes.indices) {
            allNodes[elementIndex]
        } else {
            allNodes.forEach { it.recycle() }
            null
        }
    }

    private fun collectAllNodes(node: AccessibilityNodeInfo, list: MutableList<AccessibilityNodeInfo>) {
        val snapshot = AccessibilityNodeInfo.obtain(node)
        list.add(snapshot)

        val childCount = node.childCount
        for (i in 0 until childCount) {
            val child = node.getChild(i) ?: continue
            collectAllNodes(child, list)
        }
    }

    private fun executeClick(elementIndex: Int) {
        val node = findNodeByElementIndex(elementIndex)
            ?: throw IllegalStateException("Element not found: index $elementIndex")

        if (!node.isClickable) {
            node.recycle()
            throw IllegalStateException("Element at index $elementIndex is not clickable")
        }

        node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        node.recycle()
    }

    private fun executeLongClick(elementIndex: Int) {
        val node = findNodeByElementIndex(elementIndex)
            ?: throw IllegalStateException("Element not found: index $elementIndex")

        node.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)
        node.recycle()
    }

    private fun executeInputText(elementIndex: Int, text: String) {
        val node = findNodeByElementIndex(elementIndex)
            ?: throw IllegalStateException("Element not found: index $elementIndex")

        if (!node.isEditable) {
            node.recycle()
            throw IllegalStateException("Element at index $elementIndex is not editable")
        }

        val arguments = android.os.Bundle()
        arguments.putCharSequence(
            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
            text
        )
        node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        node.recycle()
    }

    private fun executeSwipe(action: Action.Swipe) {
        val path = Path().apply {
            moveTo(action.startX.toFloat(), action.startY.toFloat())
            lineTo(action.endX.toFloat(), action.endY.toFloat())
        }

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, action.duration))
            .build()

        val resultDeferred = CompletableDeferred<Boolean>()
        dispatchGesture(gesture, null, null)
    }

    private fun executeBack() {
        performGlobalAction(GLOBAL_ACTION_BACK)
    }

    private fun executeScrollForward(elementIndex: Int) {
        val node = findNodeByElementIndex(elementIndex)
            ?: throw IllegalStateException("Element not found: index $elementIndex")

        if (!node.isScrollable) {
            node.recycle()
            throw IllegalStateException("Element at index $elementIndex is not scrollable")
        }

        node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
        node.recycle()
    }

    private fun executeScrollBackward(elementIndex: Int) {
        val node = findNodeByElementIndex(elementIndex)
            ?: throw IllegalStateException("Element not found: index $elementIndex")

        if (!node.isScrollable) {
            node.recycle()
            throw IllegalStateException("Element at index $elementIndex is not scrollable")
        }

        node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
        node.recycle()
    }

    private fun executeOpenApp(packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
            ?: throw IllegalStateException("App not found: $packageName")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
