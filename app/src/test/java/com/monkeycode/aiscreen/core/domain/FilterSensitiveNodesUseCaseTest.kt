package com.monkeycode.aiscreen.core.domain

import com.monkeycode.aiscreen.core.model.SerializableRect
import com.monkeycode.aiscreen.core.model.SerializedUITree
import com.monkeycode.aiscreen.core.model.UIElement
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class FilterSensitiveNodesUseCaseTest {

    private lateinit var useCase: FilterSensitiveNodesUseCase

    @Before
    fun setUp() {
        useCase = FilterSensitiveNodesUseCase()
    }

    @Test
    fun should_replace_password_field_text() {
        val tree = SerializedUITree(
            packageName = "com.test",
            elements = listOf(
                createUIElement(0, "normal text", isPassword = false),
                createUIElement(1, "secret123", isPassword = true),
                createUIElement(2, "another text", isPassword = false)
            ),
            timestamp = 1000
        )

        val filtered = useCase(tree)

        assertEquals("normal text", filtered.elements[0].text)
        assertEquals("[已隐藏]", filtered.elements[1].text)
        assertEquals("another text", filtered.elements[2].text)
    }

    @Test
    fun should_not_replace_non_password_fields() {
        val tree = SerializedUITree(
            packageName = "com.test",
            elements = listOf(
                createUIElement(0, "hello", isPassword = false),
                createUIElement(1, "world", isPassword = false)
            ),
            timestamp = 1000
        )

        val filtered = useCase(tree)

        assertEquals("hello", filtered.elements[0].text)
        assertEquals("world", filtered.elements[1].text)
    }

    @Test
    fun should_handle_empty_password_text() {
        val tree = SerializedUITree(
            packageName = "com.test",
            elements = listOf(
                createUIElement(0, null, isPassword = true),
                createUIElement(1, "", isPassword = true)
            ),
            timestamp = 1000
        )

        val filtered = useCase(tree)

        assertNull(filtered.elements[0].text)
        assertEquals("", filtered.elements[1].text)
    }

    @Test
    fun should_handle_empty_elements_list() {
        val tree = SerializedUITree(
            packageName = "com.test",
            elements = emptyList(),
            timestamp = 1000
        )

        val filtered = useCase(tree)

        assertTrue(filtered.elements.isEmpty())
    }

    @Test
    fun should_preserve_other_properties_after_filtering() {
        val original = createUIElement(0, "secret", isPassword = true, isClickable = true)
        val tree = SerializedUITree(packageName = "com.test", elements = listOf(original), timestamp = 1000)

        val filtered = useCase(tree)

        assertEquals("[已隐藏]", filtered.elements[0].text)
        assertTrue(filtered.elements[0].isClickable)
        assertEquals(original.index, filtered.elements[0].index)
        assertEquals(original.className, filtered.elements[0].className)
    }

    private fun createUIElement(index: Int, text: String?, isPassword: Boolean, isClickable: Boolean = false): UIElement {
        return UIElement(
            index = index,
            className = "android.widget.EditText",
            text = text,
            bounds = SerializableRect(0, 0, 100, 50),
            isClickable = isClickable,
            isPassword = isPassword,
            depth = 0
        )
    }
}
