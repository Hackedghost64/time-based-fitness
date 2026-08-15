package com.timebasedfitness.app.domain

import com.timebasedfitness.app.data.model.Category
import com.timebasedfitness.app.data.model.CompletionLog
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class StreakCalculatorTest {

    @Test
    fun calculateStreak_emptyLogs_returnsZero() {
        val logs = emptyList<CompletionLog>()
        val today = LocalDate.of(2026, 8, 15)

        assertEquals(0, StreakCalculator.calculateStreak(logs, today))
    }

    @Test
    fun calculateStreak_consecutiveDays_returnsCorrectCount() {
        val today = LocalDate.of(2026, 8, 15)
        val logs = listOf(
            CompletionLog(today, Category.MORNING, Instant.now()),
            CompletionLog(today.minusDays(1), Category.WORKOUT, Instant.now()),
            CompletionLog(today.minusDays(2), Category.EVENING, Instant.now())
        )

        assertEquals(3, StreakCalculator.calculateStreak(logs, today))
    }

    @Test
    fun calculateStreak_brokenStreak_stopsAtGap() {
        val today = LocalDate.of(2026, 8, 15)
        val logs = listOf(
            CompletionLog(today, Category.MORNING, Instant.now()),
            CompletionLog(today.minusDays(2), Category.WORKOUT, Instant.now())
        )

        assertEquals(1, StreakCalculator.calculateStreak(logs, today))
    }

    @Test
    fun calculateStreak_yesterdayCompletedButNotToday_maintainsStreak() {
        val today = LocalDate.of(2026, 8, 15)
        val logs = listOf(
            CompletionLog(today.minusDays(1), Category.WORKOUT, Instant.now()),
            CompletionLog(today.minusDays(2), Category.EVENING, Instant.now())
        )

        assertEquals(2, StreakCalculator.calculateStreak(logs, today))
    }
}
