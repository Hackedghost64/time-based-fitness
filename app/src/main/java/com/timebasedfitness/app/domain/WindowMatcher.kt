package com.timebasedfitness.app.domain

import com.timebasedfitness.app.data.model.CategorySelection
import java.time.LocalTime

object WindowMatcher {

    fun isInWindow(now: LocalTime, startTime: LocalTime, endTime: LocalTime): Boolean {
        return TimeWindow(startTime, endTime).contains(now)
    }

    fun getMatchingCategories(
        now: LocalTime,
        selections: List<CategorySelection>
    ): List<CategorySelection> {
        return selections
            .filter { it.isEnabled && isInWindow(now, it.startTime, it.endTime) }
            .sortedBy { it.startTime }
    }

    fun getNextUpcoming(
        now: LocalTime,
        selections: List<CategorySelection>
    ): CategorySelection? {
        val enabled = selections.filter { it.isEnabled }
        if (enabled.isEmpty()) return null

        val upcomingToday = enabled
            .filter { it.startTime.isAfter(now) }
            .minByOrNull { it.startTime }

        return upcomingToday ?: enabled.minByOrNull { it.startTime }
    }
}
