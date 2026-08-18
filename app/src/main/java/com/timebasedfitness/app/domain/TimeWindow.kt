package com.timebasedfitness.app.domain

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Canonical representation of a time window that correctly handles midnight-crossing intervals.
 * This is the single source of truth for all time-window semantics in the application.
 *
 * Examples:
 * - Normal window: 06:00–09:00 (start <= end)
 * - Overnight window: 22:00–02:00 (start > end, spans midnight)
 */
data class TimeWindow(
    val startTime: LocalTime,
    val endTime: LocalTime
) {
    /**
     * Returns true if this window spans midnight (e.g., 22:00–02:00).
     * For normal daytime windows (e.g., 06:00–09:00), returns false.
     */
    val isOvernight: Boolean get() = startTime > endTime

    /**
     * Checks if the given [now] time falls within this window.
     * Correctly handles both normal and overnight windows.
     */
    fun contains(now: LocalTime): Boolean {
        return if (isOvernight) {
            // Overnight: start <= now OR now <= end (e.g., 22:00–02:00)
            !now.isBefore(startTime) || !now.isAfter(endTime)
        } else {
            // Normal: start <= now <= end (e.g., 06:00–09:00)
            !now.isBefore(startTime) && !now.isAfter(endTime)
        }
    }

    /**
     * Resolves the actual start [LocalDateTime] for a given [now].
     * For overnight windows, if [now] is after the end time, the start is today.
     * Otherwise, the start is yesterday (since the window started last night).
     */
    fun startDateTime(now: LocalDateTime, zoneId: ZoneId = ZoneId.systemDefault()): LocalDateTime {
        val today = now.toLocalDate()
        val startToday = today.atTime(startTime)

        return if (isOvernight) {
            if (now.toLocalTime().isBefore(startTime)) {
                // If before start time, the most recent window started yesterday
                startToday.minusDays(1)
            } else {
                // Otherwise it started today
                startToday
            }
        } else {
            startToday
        }
    }

    /**
     * Resolves the actual end [LocalDateTime] for a given [now].
     * For overnight windows, if [now] is before the start time, the end is today.
     * Otherwise, the end is tomorrow.
     */
    fun endDateTime(now: LocalDateTime, zoneId: ZoneId = ZoneId.systemDefault()): LocalDateTime {
        val today = now.toLocalDate()
        val endToday = today.atTime(endTime)

        return if (isOvernight) {
            if (now.toLocalTime().isBefore(endTime)) {
                // If in the morning part of overnight window, ends today
                endToday
            } else {
                // Otherwise ends tomorrow
                endToday.plusDays(1)
            }
        } else {
            endToday
        }
    }

    /**
     * Calculates the remaining duration from [now] until the window ends.
     * Returns null if the window has already ended.
     */
    fun remainingDuration(now: LocalDateTime): java.time.Duration? {
        val end = endDateTime(now)
        return if (now.isBefore(end)) {
            java.time.Duration.between(now, end)
        } else {
            null
        }
    }

    /**
     * Returns the next occurrence of this window's start time from [now].
     * Useful for scheduling the first notification of a window.
     */
    fun nextOccurrence(now: LocalDateTime): LocalDateTime {
        val today = now.toLocalDate()
        val startToday = today.atTime(startTime)

        return when {
            now.isBefore(startToday) -> startToday
            isOvernight && now.toLocalTime().isAfter(endTime) -> startToday.plusDays(1)
            else -> startToday.plusDays(1)
        }
    }

    /**
     * Returns true if the window has already ended for the given [now].
     */
    fun hasEnded(now: LocalDateTime): Boolean {
        return now.isAfter(endDateTime(now))
    }

    /**
     * Returns true if the window has not yet started for the given [now].
     */
    fun hasNotStarted(now: LocalDateTime): Boolean {
        return now.isBefore(startDateTime(now))
    }
}
