package com.monkeycode.aiscreen.core.domain

import com.monkeycode.aiscreen.core.model.Action
import com.monkeycode.aiscreen.core.model.ActionResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExecuteActionUseCase @Inject constructor(
    private val serviceBridge: AccessibilityServiceBridge
) {
    companion object {
        const val MAX_CONSECUTIVE_FAILURES = 3
    }

    operator fun invoke(actions: List<Action>): Flow<ActionResult> = flow {
        var consecutiveFailures = 0

        for ((index, action) in actions.withIndex()) {
            if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
                emit(
                    ActionResult.Skipped(
                        actionIndex = index,
                        reason = "Paused after $MAX_CONSECUTIVE_FAILURES consecutive failures"
                    )
                )
                continue
            }

            try {
                val result = serviceBridge.performAction(action)
                emit(result)

                when (result) {
                    is ActionResult.Success -> consecutiveFailures = 0
                    is ActionResult.Failure -> consecutiveFailures++
                    is ActionResult.NeedsConfirmation -> {
                        emit(result)
                    }
                    is ActionResult.Skipped -> {}
                }
            } catch (e: Exception) {
                consecutiveFailures++
                emit(ActionResult.Failure(index, "Exception: ${e.message}"))
            }
        }
    }
}
