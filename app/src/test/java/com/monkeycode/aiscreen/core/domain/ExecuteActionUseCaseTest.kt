package com.monkeycode.aiscreen.core.domain

import com.monkeycode.aiscreen.core.model.Action
import com.monkeycode.aiscreen.core.model.ActionResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ExecuteActionUseCaseTest {

    private lateinit var serviceBridge: AccessibilityServiceBridge
    private lateinit var useCase: ExecuteActionUseCase

    @Before
    fun setUp() {
        serviceBridge = mockk()
        useCase = ExecuteActionUseCase(serviceBridge)
    }

    @Test
    fun should_execute_all_actions_successfully() = runBlocking {
        coEvery { serviceBridge.performAction(any()) } returnsMany listOf(
            ActionResult.Success(0, "clicked"),
            ActionResult.Success(1, "typed"),
            ActionResult.Success(2, "swiped")
        )

        val actions = listOf(
            Action.Click(0),
            Action.InputText(1, "test"),
            Action.Swipe(0, 0, 100, 100)
        )

        val results = useCase(actions).toList()

        assertEquals(3, results.size)
        assertTrue(results.all { it is ActionResult.Success })
    }

    @Test
    fun should_pause_after_consecutive_failures() = runBlocking {
        coEvery { serviceBridge.performAction(any()) } returns ActionResult.Failure(0, "failed")

        val actions = listOf(
            Action.Click(0),
            Action.Click(1),
            Action.Click(2),
            Action.Click(3),
            Action.Click(4)
        )

        val results = useCase(actions).toList()

        assertEquals(5, results.size)
        assertEquals(3, results.take(3).count { it is ActionResult.Failure })
        assertEquals(2, results.drop(3).count { it is ActionResult.Skipped })
        assertEquals(
            "Paused after 3 consecutive failures",
            (results[3] as ActionResult.Skipped).reason
        )
    }

    @Test
    fun should_reset_failure_count_after_success() = runBlocking {
        coEvery { serviceBridge.performAction(any()) } returnsMany listOf(
            ActionResult.Failure(0, "failed"),
            ActionResult.Failure(1, "failed"),
            ActionResult.Success(2, "ok"),
            ActionResult.Success(3, "ok")
        )

        val actions = listOf(
            Action.Click(0),
            Action.Click(1),
            Action.Click(2),
            Action.Click(3)
        )

        val results = useCase(actions).toList()

        assertEquals(4, results.size)
        assertEquals(2, results.take(2).count { it is ActionResult.Failure })
        assertEquals(2, results.drop(2).count { it is ActionResult.Success })
    }

    @Test
    fun should_handle_empty_actions_list() = runBlocking {
        val results = useCase(emptyList()).toList()
        assertTrue(results.isEmpty())
    }

    @Test
    fun should_emit_needs_confirmation_action() = runBlocking {
        coEvery { serviceBridge.performAction(any()) } returns ActionResult.NeedsConfirmation(0, "Confirm payment?")

        val actions = listOf(Action.Click(0))
        val results = useCase(actions).toList()

        assertEquals(1, results.size)
        assertTrue(results[0] is ActionResult.NeedsConfirmation)
        assertEquals("Confirm payment?", (results[0] as ActionResult.NeedsConfirmation).prompt)
    }
}
