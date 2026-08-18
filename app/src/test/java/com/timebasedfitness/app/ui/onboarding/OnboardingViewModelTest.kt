package com.timebasedfitness.app.ui.onboarding

import com.timebasedfitness.app.data.model.Category
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class OnboardingViewModelTest {

    @Test
    fun onboardingStep_transitionsForwardAndBackwardCorrectly() {
        var step = OnboardingStep.WELCOME
        val next = { s: OnboardingStep ->
            when (s) {
                OnboardingStep.WELCOME -> OnboardingStep.GOAL
                OnboardingStep.GOAL -> OnboardingStep.ARCHETYPE
                OnboardingStep.ARCHETYPE -> OnboardingStep.CATEGORIES
                OnboardingStep.CATEGORIES -> OnboardingStep.NOTIFICATIONS
                OnboardingStep.NOTIFICATIONS -> OnboardingStep.READY
                OnboardingStep.READY -> OnboardingStep.READY
            }
        }

        step = next(step)
        assertEquals(OnboardingStep.GOAL, step)

        step = next(step)
        assertEquals(OnboardingStep.ARCHETYPE, step)

        step = next(step)
        assertEquals(OnboardingStep.CATEGORIES, step)
    }

    @Test
    fun scheduleArchetype_earlyBird_configuresExpectedTimes() {
        val archetype = ScheduleArchetype.EARLY_BIRD
        assertEquals(LocalTime.of(5, 0), archetype.morning.first)
        assertEquals(LocalTime.of(7, 30), archetype.morning.second)
        assertEquals(LocalTime.of(6, 0), archetype.workout.first)
        assertEquals(LocalTime.of(7, 30), archetype.workout.second)
    }

    @Test
    fun scheduleArchetype_nightOwl_configuresExpectedTimes() {
        val archetype = ScheduleArchetype.NIGHT_OWL
        assertEquals(LocalTime.of(8, 30), archetype.morning.first)
        assertEquals(LocalTime.of(19, 30), archetype.workout.first)
        assertEquals(LocalTime.of(23, 0), archetype.evening.first)
    }

    @Test
    fun fitnessGoal_hasAllFourDistinctCategories() {
        val goals = FitnessGoal.entries
        assertEquals(4, goals.size)
        assertTrue(goals.contains(FitnessGoal.STRENGTH))
        assertTrue(goals.contains(FitnessGoal.CARDIO))
        assertTrue(goals.contains(FitnessGoal.MOBILITY))
        assertTrue(goals.contains(FitnessGoal.DAILY_ENERGY))
    }
}
