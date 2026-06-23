package com.monkeycode.aiscreen.core.domain

import com.monkeycode.aiscreen.core.model.SerializedUITree
import com.monkeycode.aiscreen.core.model.UIElement
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilterSensitiveNodesUseCase @Inject constructor() {

    companion object {
        const val REPLACEMENT_TEXT = "[已隐藏]"
    }

    operator fun invoke(tree: SerializedUITree): SerializedUITree {
        val filteredElements = tree.elements.map { element ->
            if (element.isPassword && !element.text.isNullOrBlank()) {
                element.copy(text = REPLACEMENT_TEXT)
            } else {
                element
            }
        }

        return tree.copy(elements = filteredElements)
    }
}
