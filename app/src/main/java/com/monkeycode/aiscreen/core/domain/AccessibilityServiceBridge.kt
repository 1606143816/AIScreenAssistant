package com.monkeycode.aiscreen.core.domain

import com.monkeycode.aiscreen.core.model.Action
import com.monkeycode.aiscreen.core.model.ActionResult
import com.monkeycode.aiscreen.core.model.SerializedUITree

interface AccessibilityServiceBridge {
    fun readUITree(): SerializedUITree?
    suspend fun performAction(action: Action): ActionResult
    fun isEnabled(): Boolean
}
