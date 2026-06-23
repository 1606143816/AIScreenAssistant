package com.monkeycode.aiscreen.core.serializer

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import com.monkeycode.aiscreen.core.model.SerializedUITree
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UITreeSerializerTest {

    private lateinit var serializer: UITreeSerializer

    @Before
    fun setUp() {
        serializer = UITreeSerializer()
    }

    @Test
    fun should_extract_root_package_name() {
        val rootNode = mockk<AccessibilityNodeInfo>()
        every { rootNode.packageName } returns "com.example.app"
        every { rootNode.childCount } returns 0
        every { rootNode.text } returns null
        every { rootNode.contentDescription } returns null
        every { rootNode.className } returns "android.widget.FrameLayout"
        every { rootNode.isClickable } returns false
        every { rootNode.isLongClickable } returns false
        every { rootNode.isEditable } returns false
        every { rootNode.isScrollable } returns false
        every { rootNode.isFocusable } returns false
        every { rootNode.isImportantForAccessibility } returns true
        every { rootNode.viewIdResourceName } returns null
        every { rootNode.isPassword } returns false
        every { rootNode.isEnabled } returns true
        every { rootNode.isFocused } returns false
        every { rootNode.isCheckable } returns false
        every { rootNode.getBoundsInScreen() } returns Rect(0, 0, 1080, 1920)

        val tree = serializer.serialize(rootNode)

        assertEquals("com.example.app", tree.packageName)
    }

    @Test
    fun should_include_interactive_nodes() {
        val rootNode = mockk<AccessibilityNodeInfo>()
        val buttonNode = mockk<AccessibilityNodeInfo>()

        setupRootNode(rootNode)
        every { rootNode.childCount } returns 1
        every { rootNode.getChild(0) } returns buttonNode

        every { buttonNode.packageName } returns "com.example.app"
        every { buttonNode.className } returns "android.widget.Button"
        every { buttonNode.text } returns "Submit"
        every { buttonNode.contentDescription } returns null
        every { buttonNode.hintText } returns null
        every { buttonNode.viewIdResourceName } returns "com.example:id/submitBtn"
        every { buttonNode.isClickable } returns true
        every { buttonNode.isLongClickable } returns false
        every { buttonNode.isEditable } returns false
        every { buttonNode.isScrollable } returns false
        every { buttonNode.isFocusable } returns true
        every { buttonNode.isImportantForAccessibility } returns true
        every { buttonNode.isPassword } returns false
        every { buttonNode.isEnabled } returns true
        every { buttonNode.isFocused } returns false
        every { buttonNode.isCheckable } returns false
        every { buttonNode.childCount } returns 0
        every { buttonNode.getChild(any<Int>()) } returns null
        every { buttonNode.getBoundsInScreen() } returns Rect(100, 200, 300, 350)
        every { buttonNode.childCount } returns 0
        every { buttonNode.getChild(any<Int>()) } returns null

        val tree = serializer.serialize(rootNode)

        assertEquals(1, tree.elements.size)
        assertEquals("Submit", tree.elements[0].text)
        assertEquals("android.widget.Button", tree.elements[0].className)
        assertTrue(tree.elements[0].isClickable)
    }

    @Test
    fun should_filter_decorative_empty_nodes() {
        val rootNode = mockk<AccessibilityNodeInfo>()
        val decorNode = mockk<AccessibilityNodeInfo>()

        setupRootNode(rootNode)
        every { rootNode.childCount } returns 1
        every { rootNode.getChild(0) } returns decorNode

        every { decorNode.packageName } returns "com.example.app"
        every { decorNode.className } returns "android.view.View"
        every { decorNode.text } returns null
        every { decorNode.contentDescription } returns null
        every { decorNode.hintText } returns null
        every { decorNode.viewIdResourceName } returns null
        every { decorNode.isClickable } returns false
        every { decorNode.isLongClickable } returns false
        every { decorNode.isEditable } returns false
        every { decorNode.isScrollable } returns false
        every { decorNode.isFocusable } returns false
        every { decorNode.isImportantForAccessibility } returns false
        every { decorNode.isPassword } returns false
        every { decorNode.isEnabled } returns true
        every { decorNode.isFocused } returns false
        every { decorNode.isCheckable } returns false
        every { decorNode.childCount } returns 0
        every { decorNode.getChild(any<Int>()) } returns null
        every { decorNode.getBoundsInScreen() } returns Rect(0, 0, 100, 100)
        every { decorNode.childCount } returns 0
        every { decorNode.getChild(any<Int>()) } returns null

        val tree = serializer.serialize(rootNode)

        assertEquals(0, tree.elements.size)
    }

    @Test
    fun should_detect_password_field() {
        val rootNode = mockk<AccessibilityNodeInfo>()
        val passwordNode = mockk<AccessibilityNodeInfo>()

        setupRootNode(rootNode)
        every { rootNode.childCount } returns 1
        every { rootNode.getChild(0) } returns passwordNode

        every { passwordNode.packageName } returns "com.example.app"
        every { passwordNode.className } returns "android.widget.EditText"
        every { passwordNode.text } returns "secret123"
        every { passwordNode.contentDescription } returns null
        every { passwordNode.hintText } returns null
        every { passwordNode.viewIdResourceName } returns "com.example:id/passwordField"
        every { passwordNode.isClickable } returns true
        every { passwordNode.isLongClickable } returns false
        every { passwordNode.isEditable } returns true
        every { passwordNode.isScrollable } returns false
        every { passwordNode.isFocusable } returns true
        every { passwordNode.isImportantForAccessibility } returns true
        every { passwordNode.isPassword } returns true
        every { passwordNode.isEnabled } returns true
        every { passwordNode.isFocused } returns false
        every { passwordNode.isCheckable } returns false
        every { passwordNode.childCount } returns 0
        every { passwordNode.getChild(any<Int>()) } returns null
        every { passwordNode.getBoundsInScreen() } returns Rect(100, 200, 500, 280)

        val tree = serializer.serialize(rootNode)

        assertEquals(1, tree.elements.size)
        assertTrue(tree.elements[0].isPassword)
        assertEquals("secret123", tree.elements[0].text)
    }

    @Test
    fun should_assign_sequential_indices() {
        val rootNode = mockk<AccessibilityNodeInfo>()

        setupRootNode(rootNode)
        every { rootNode.childCount } returns 2
        val child1 = createMockNode("Button A", isClickable = true)
        val child2 = createMockNode("Button B", isClickable = true)
        every { rootNode.getChild(0) } returns child1
        every { rootNode.getChild(1) } returns child2

        val tree = serializer.serialize(rootNode)

        assertEquals(2, tree.elements.size)
        assertEquals(0, tree.elements[0].index)
        assertEquals(1, tree.elements[1].index)
    }

    @Test
    fun should_track_depth_correctly() {
        val rootNode = mockk<AccessibilityNodeInfo>()
        val child = mockk<AccessibilityNodeInfo>()
        val grandchild = mockk<AccessibilityNodeInfo>()

        setupRootNode(rootNode)
        every { rootNode.childCount } returns 1
        every { rootNode.getChild(0) } returns child

        every { child.packageName } returns "com.example.app"
        every { child.className } returns "android.widget.LinearLayout"
        every { child.text } returns null
        every { child.contentDescription } returns "Container"
        every { child.hintText } returns null
        every { child.viewIdResourceName } returns "com.example:id/container"
        every { child.isClickable } returns false
        every { child.isLongClickable } returns false
        every { child.isEditable } returns false
        every { child.isScrollable } returns false
        every { child.isFocusable } returns false
        every { child.isImportantForAccessibility } returns true
        every { child.isPassword } returns false
        every { child.isEnabled } returns true
        every { child.isFocused } returns false
        every { child.isCheckable } returns false
        every { child.childCount } returns 1
        every { child.getChild(0) } returns grandchild
        every { child.getChild(any<Int>()) } returns null
        every { child.getBoundsInScreen() } returns Rect(0, 0, 100, 100)

        every { grandchild.packageName } returns "com.example.app"
        every { grandchild.className } returns "android.widget.TextView"
        every { grandchild.text } returns "Hello"
        every { grandchild.contentDescription } returns null
        every { grandchild.hintText } returns null
        every { grandchild.viewIdResourceName } returns null
        every { grandchild.isClickable } returns false
        every { grandchild.isLongClickable } returns false
        every { grandchild.isEditable } returns false
        every { grandchild.isScrollable } returns false
        every { grandchild.isFocusable } returns false
        every { grandchild.isImportantForAccessibility } returns true
        every { grandchild.isPassword } returns false
        every { grandchild.isEnabled } returns true
        every { grandchild.isFocused } returns false
        every { grandchild.isCheckable } returns false
        every { grandchild.childCount } returns 0
        every { grandchild.getChild(any<Int>()) } returns null
        every { grandchild.getBoundsInScreen() } returns Rect(0, 100, 200, 200)

        val tree = serializer.serialize(rootNode)

        assertEquals(2, tree.elements.size)
        assertEquals(1, tree.elements[0].depth)
        assertEquals(2, tree.elements[1].depth)
    }

    @Test
    fun should_serialize_to_json() {
        val rootNode = mockk<AccessibilityNodeInfo>()
        val buttonNode = mockk<AccessibilityNodeInfo>()

        setupRootNode(rootNode)
        every { rootNode.childCount } returns 1
        every { rootNode.getChild(0) } returns buttonNode

        every { buttonNode.packageName } returns "com.example.app"
        every { buttonNode.className } returns "android.widget.Button"
        every { buttonNode.text } returns "OK"
        every { buttonNode.contentDescription } returns null
        every { buttonNode.hintText } returns null
        every { buttonNode.viewIdResourceName } returns null
        every { buttonNode.isClickable } returns true
        every { buttonNode.isLongClickable } returns false
        every { buttonNode.isEditable } returns false
        every { buttonNode.isScrollable } returns false
        every { buttonNode.isFocusable } returns true
        every { buttonNode.isImportantForAccessibility } returns true
        every { buttonNode.isPassword } returns false
        every { buttonNode.isEnabled } returns true
        every { buttonNode.isFocused } returns false
        every { buttonNode.isCheckable } returns false
        every { buttonNode.childCount } returns 0
        every { buttonNode.getChild(any<Int>()) } returns null
        every { buttonNode.getBoundsInScreen() } returns Rect(50, 50, 150, 100)

        val tree = serializer.serialize(rootNode)
        val json = serializer.toJson(tree)

        assertTrue(json.contains("com.example.app"))
        assertTrue(json.contains("OK"))
        assertTrue(json.contains("Button"))
    }

    @Test
    fun should_include_content_description_nodes() {
        val rootNode = mockk<AccessibilityNodeInfo>()
        val imageNode = mockk<AccessibilityNodeInfo>()

        setupRootNode(rootNode)
        every { rootNode.childCount } returns 1
        every { rootNode.getChild(0) } returns imageNode

        every { imageNode.packageName } returns "com.example.app"
        every { imageNode.className } returns "android.widget.ImageView"
        every { imageNode.text } returns null
        every { imageNode.contentDescription } returns "Profile picture"
        every { imageNode.hintText } returns null
        every { imageNode.viewIdResourceName } returns "com.example:id/profilePic"
        every { imageNode.isClickable } returns false
        every { imageNode.isLongClickable } returns false
        every { imageNode.isEditable } returns false
        every { imageNode.isScrollable } returns false
        every { imageNode.isFocusable } returns false
        every { imageNode.isImportantForAccessibility } returns true
        every { imageNode.isPassword } returns false
        every { imageNode.isEnabled } returns true
        every { imageNode.isFocused } returns false
        every { imageNode.isCheckable } returns false
        every { imageNode.childCount } returns 0
        every { imageNode.getChild(any<Int>()) } returns null
        every { imageNode.getBoundsInScreen() } returns Rect(0, 0, 48, 48)

        val tree = serializer.serialize(rootNode)

        assertEquals(1, tree.elements.size)
        assertEquals("Profile picture", tree.elements[0].contentDescription)
        assertFalse(tree.elements[0].isClickable)
    }

    // Helper methods

    private fun setupRootNode(rootNode: AccessibilityNodeInfo) {
        every { rootNode.packageName } returns "com.example.app"
        every { rootNode.className } returns "android.widget.FrameLayout"
        every { rootNode.text } returns null
        every { rootNode.contentDescription } returns null
        every { rootNode.hintText } returns null
        every { rootNode.viewIdResourceName } returns "android:id/content"
        every { rootNode.isClickable } returns false
        every { rootNode.isLongClickable } returns false
        every { rootNode.isEditable } returns false
        every { rootNode.isScrollable } returns false
        every { rootNode.isFocusable } returns false
        every { rootNode.isImportantForAccessibility } returns false
        every { rootNode.isPassword } returns false
        every { rootNode.isEnabled } returns true
        every { rootNode.isFocused } returns false
        every { rootNode.isCheckable } returns false
        every { rootNode.getBoundsInScreen() } returns Rect(0, 0, 1080, 1920)
        every { rootNode.getChild(any<Int>()) } returns null
    }

    private fun createMockNode(text: String, isClickable: Boolean): AccessibilityNodeInfo {
        val node = mockk<AccessibilityNodeInfo>()
        every { node.packageName } returns "com.example.app"
        every { node.className } returns "android.widget.Button"
        every { node.text } returns text
        every { node.contentDescription } returns null
        every { node.hintText } returns null
        every { node.viewIdResourceName } returns null
        every { node.isClickable } returns isClickable
        every { node.isLongClickable } returns false
        every { node.isEditable } returns false
        every { node.isScrollable } returns false
        every { node.isFocusable } returns isClickable
        every { node.isImportantForAccessibility } returns true
        every { node.isPassword } returns false
        every { node.isEnabled } returns true
        every { node.isFocused } returns false
        every { node.isCheckable } returns false
        every { node.childCount } returns 0
        every { node.getChild(any<Int>()) } returns null
        every { node.getBoundsInScreen() } returns Rect(0, 0, 100, 50)
        return node
    }
}
