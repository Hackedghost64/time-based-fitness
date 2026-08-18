package com.timebasedfitness.app.ui.routine

import com.timebasedfitness.app.data.content.RoutineContent
import com.timebasedfitness.app.data.model.RoutineStep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutineDetailViewModelTimerGateTest {

    private val timerRoutine = RoutineContent(
        title = "Timed routine",
        steps = listOf(RoutineStep("Plank", durationSeconds = 60))
    )

    @Test
    fun requestTimerStepToggle_whenTimerRunning_doesNotToggleAndRequestsConfirmation() {
        val state = RoutineDetailUiState(
            routineContent = timerRoutine,
            activeTimer = ActiveTimer(stepIndex = 0, remainingSeconds = 30, totalSeconds = 60, isRunning = true)
        )

        val result = state.requestTimerStepToggle(0)

        assertFalse(result.checkedSteps.contains(0))
        assertEquals(0, result.pendingTimerStepIndex)
        assertEquals(30, result.pendingTimerRemainingSeconds)
    }

    @Test
    fun requestTimerStepToggle_whenTimerFinished_togglesImmediately() {
        val state = RoutineDetailUiState(
            routineContent = timerRoutine,
            activeTimer = ActiveTimer(stepIndex = 0, remainingSeconds = 0, totalSeconds = 60, isRunning = false)
        )

        val result = state.requestTimerStepToggle(0)

        assertTrue(result.checkedSteps.contains(0))
        assertNull(result.pendingTimerStepIndex)
    }

    @Test
    fun confirmTimerStepOverride_togglesThePendingStep() {
        val state = RoutineDetailUiState(
            routineContent = timerRoutine,
            pendingTimerStepIndex = 0,
            pendingTimerRemainingSeconds = 30
        )

        val result = state.confirmTimerStepOverride()

        assertTrue(result.checkedSteps.contains(0))
        assertNull(result.pendingTimerStepIndex)
    }

    @Test
    fun cancelTimerStepOverride_keepsTheStepUnchecked() {
        val state = RoutineDetailUiState(
            routineContent = timerRoutine,
            pendingTimerStepIndex = 0,
            pendingTimerRemainingSeconds = 30
        )

        val result = state.cancelTimerStepOverride()

        assertFalse(result.checkedSteps.contains(0))
        assertNull(result.pendingTimerStepIndex)
        assertEquals(0, result.pendingTimerRemainingSeconds)
    }

    @Test
    fun restTimer_initializesCorrectlyInUiState() {
        val rest = RestTimer(remainingSeconds = 60, totalSeconds = 60, isRunning = true)
        val state = RoutineDetailUiState(
            routineContent = timerRoutine,
            restTimer = rest,
            isSoundEnabled = true
        )

        assertEquals(60, state.restTimer?.remainingSeconds)
        assertTrue(state.restTimer?.isRunning == true)
        assertTrue(state.isSoundEnabled)
    }
}
