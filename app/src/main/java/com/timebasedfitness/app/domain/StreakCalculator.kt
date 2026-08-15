package com.timebasedfitness.app.domain

import com.timebasedfitness.app.data.model.CompletionLog
import java.time.LocalDate

object StreakCalculator {

    fun calculateStreak(
        logs: List<CompletionLog>,
        today: LocalDate = LocalDate.now()
    ): Int {
        if (logs.isEmpty()) return 0

        val completedDates = logs.map { it.date }.toSet()
        var streak = 0
        var checkDate = today

        // If today isn't completed yet, check if yesterday was completed to keep current streak alive
        if (!completedDates.contains(today)) {
            checkDate = today.minusDays(1)
        }

        while (completedDates.contains(checkDate)) {
            streak++
            checkDate = checkDate.minusDays(1)
        }

        return streak
    }
}
