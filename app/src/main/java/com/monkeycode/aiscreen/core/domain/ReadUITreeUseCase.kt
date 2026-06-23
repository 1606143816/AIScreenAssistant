package com.monkeycode.aiscreen.core.domain

import com.monkeycode.aiscreen.core.model.SerializedUITree
import com.monkeycode.aiscreen.core.serializer.UITreeSerializer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadUITreeUseCase @Inject constructor(
    private val serviceBridge: AccessibilityServiceBridge,
    private val serializer: UITreeSerializer
) {
    operator fun invoke(): Result<SerializedUITree> {
        if (!serviceBridge.isEnabled()) {
            return Result.failure(
                IllegalStateException("Accessibility service is not enabled")
            )
        }

        return try {
            val tree = serviceBridge.readUITree()
                ?: return Result.failure(
                    IllegalStateException("Failed to read UI tree")
                )
            Result.success(tree)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
