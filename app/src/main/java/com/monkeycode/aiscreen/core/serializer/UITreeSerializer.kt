package com.monkeycode.aiscreen.core.serializer

import android.view.accessibility.AccessibilityNodeInfo
import com.monkeycode.aiscreen.core.model.UIElement
import com.monkeycode.aiscreen.core.model.SerializableRect
import com.monkeycode.aiscreen.core.model.SerializedUITree
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class UITreeSerializer {

    private val json = Json {
        prettyPrint = false
        encodeDefaults = false
        ignoreUnknownKeys = true
    }

    fun serialize(rootNode: AccessibilityNodeInfo): SerializedUITree {
        val packageName = rootNode.packageName?.toString() ?: "unknown"
        val elements = mutableListOf<UIElement>()
        val timestamps = System.currentTimeMillis()

        traverseNode(rootNode, elements, depth = 0, shouldIncludeRoot = false)

        return SerializedUITree(
            packageName = packageName,
            activityName = null,
            elements = elements,
            timestamp = timestamps
        )
    }

    fun toJson(tree: SerializedUITree): String {
        return json.encodeToString(tree)
    }

    private fun traverseNode(
        node: AccessibilityNodeInfo,
        elements: MutableList<UIElement>,
        depth: Int,
        shouldIncludeRoot: Boolean
    ) {
        if (shouldIncludeRoot || shouldIncludeNode(node)) {
            val element = createUIElement(node, elements.size, depth)
            elements.add(element)
        }

        val childCount = node.childCount
        for (i in 0 until childCount) {
            val child = node.getChild(i) ?: continue
            traverseNode(child, elements, depth + 1, shouldIncludeRoot = true)
        }
    }

    private fun shouldIncludeNode(node: AccessibilityNodeInfo): Boolean {
        val hasText = !node.text.isNullOrBlank()
        val hasDescription = !node.contentDescription.isNullOrBlank()
        val hasResourceId = !node.viewIdResourceName.isNullOrBlank()
        val isInteractive = node.isClickable || node.isLongClickable ||
                node.isEditable || node.isScrollable || node.isFocusable
        val hasChildren = node.childCount > 0

        if (node.isImportantForAccessibility && (hasText || hasDescription || isInteractive)) {
            return true
        }

        if (isInteractive || (hasResourceId && (hasText || hasDescription || hasChildren))) {
            return true
        }

        if (!hasChildren && (hasText || hasDescription)) {
            return true
        }

        return false
    }

    private fun createUIElement(node: AccessibilityNodeInfo, index: Int, depth: Int): UIElement {
        val bounds = android.graphics.Rect()
        node.getBoundsInScreen(bounds)

        return UIElement(
            index = index,
            resourceId = node.viewIdResourceName,
            className = node.className?.toString() ?: "android.view.View",
            text = node.text?.toString(),
            contentDescription = node.contentDescription?.toString(),
            hint = node.hintText?.toString(),
            bounds = SerializableRect(bounds.left, bounds.top, bounds.right, bounds.bottom),
            isClickable = node.isClickable,
            isLongClickable = node.isLongClickable,
            isEditable = node.isEditable,
            isPassword = node.isPassword,
            isChecked = if (node.isCheckable) node.isChecked else null,
            isScrollable = node.isScrollable,
            isFocused = node.isFocused,
            isEnabled = node.isEnabled,
            childCount = node.childCount,
            depth = depth
        )
    }

    companion object {
        fun getAppNameFromPackage(packageName: String): String {
            return packageName.substringAfterLast('.')
        }
    }
}
