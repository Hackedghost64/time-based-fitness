package com.timebasedfitness.app.domain

import com.timebasedfitness.app.data.model.Category
import com.timebasedfitness.app.data.model.CompletionLog
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class StreakCalculatorBestStreakTest {

    private fun log(date: LocalDate) = CompletionLog(
        date = date,
        category = Category.WORKOUT,
        completedAt = Instant.now()
    )

    @Test
    fun calculateBestStreak_emptyList_returnsZero() {
        assertEquals(0, StreakCalculator.calculateBestStreak(emptyList()))
    }

    @Test
    fun calculateBestStreak_consecutiveDays_returnsCorrectMaxStreak() {
        val today = LocalDate.of(2026, 8, 18)
        val logs = listOf(
            log(today.minusDays(10)),
            log(today.minusDays(9)),
            log(today.minusDays(8)), // 3-day streak
            log(today.minusDays(5)),
            log(today.minusDays(4)),
            log(today.minusDays(3)),
            log(today.minusDays(2)),
            log(today.minusDays(1))  // 5-day streak
        )

        val best = StreakCalculator.calculateBestStreak(logs)
        assertEquals(5, best)
    }

    @Test
    fun calculateBestStreak_multipleSameDayLogs_doesNotArtificiallyInflateStreak() {
        val today = LocalDate.of(2026, 8, 18)
        val logs = listOf(
            log(today.minusDays(1)),
            log(today.minusDays(1)),
            log(today)
        )

        val best = StreakCalculator.calculateBestStreak(logs)
        assertEquals(2, best)
    }
}
