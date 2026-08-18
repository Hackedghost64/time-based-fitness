package com.timebasedfitness.app.notifications

import com.timebasedfitness.app.data.model.Category
import com.timebasedfitness.app.data.model.CategorySelection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class NotificationSchedulerTest {

    private val zoneId = ZoneId.of("UTC")

    @Test
    fun calculateNextTriggerMillis_whenStartTimeIsLaterToday_schedulesForToday() {
        val now = LocalDateTime.of(2026, 8, 16, 8, 0, 0)
        val startTime = LocalTime.of(9, 30) // 1.5 hours later today

        val triggerMillis = NotificationScheduler.calculateNextTriggerMillis(startTime, now, zoneId)
        val expected = LocalDateTime.of(2026, 8, 16, 9, 30, 0).atZone(zoneId).toInstant().toEpochMilli()

        assertEquals(expected, triggerMillis)
    }

    @Test
    fun calculateNextTriggerMillis_whenStartTimeHasPassedToday_schedulesForTomorrow() {
        val now = LocalDateTime.of(2026, 8, 16, 10, 0, 0)
        val startTime = LocalTime.of(8, 0) // already passed today

        val triggerMillis = NotificationScheduler.calculateNextTriggerMillis(startTime, now, zoneId)
        val expected = LocalDateTime.of(2026, 8, 17, 8, 0, 0).atZone(zoneId).toInstant().toEpochMilli()

        assertEquals(expected, triggerMillis)
    }

    @Test
    fun calculateNextTriggerMillis_atExactStartTime_schedulesForTomorrow() {
        val now = LocalDateTime.of(2026, 8, 16, 8, 0, 0)
        val startTime = LocalTime.of(8, 0) // current time

        val triggerMillis = NotificationScheduler.calculateNextTriggerMillis(startTime, now, zoneId)
        val expected = LocalDateTime.of(2026, 8, 17, 8, 0, 0).atZone(zoneId).toInstant().toEpochMilli()

        assertEquals(expected, triggerMillis)
    }

    // ---------- Nudge policy -----------------------------------------------------

    private fun workoutAt(start: LocalTime, end: LocalTime) =
        CategorySelection(Category.WORKOUT, isEnabled = true, startTime = start, endTime = end)

    @Test
    fun nudge_beforeWindow_schedulesForStartTime() {
        val now = LocalDateTime.of(2026, 8, 16, 6, 0, 0)
        val sel = workoutAt(LocalTime.of(8, 0), LocalTime.of(10, 0))
        val millis = NotificationScheduler.calculateNextNudgeMillis(
            selection = sel,
            policy = NudgePolicy(intervalMinutes = 10, maxPerWindow = 6),
            now = now,
            zoneId = zoneId,
            alreadyFiredCount = 0,
            isCompletedInWindow = false
        )
        val expected = LocalDateTime.of(2026, 8, 16, 8, 0, 0).atZone(zoneId).toInstant().toEpochMilli()
        assertEquals(expected, millis)
    }

    @Test
    fun nudge_insideWindow_firesIntervalFromNow() {
        val now = LocalDateTime.of(2026, 8, 16, 9, 30, 0)
        val sel = workoutAt(LocalTime.of(9, 0), LocalTime.of(10, 0))
        val millis = NotificationScheduler.calculateNextNudgeMillis(
            selection = sel,
            policy = NudgePolicy(intervalMinutes = 10, maxPerWindow = 6),
            now = now,
            zoneId = zoneId,
            alreadyFiredCount = 1,
            isCompletedInWindow = false
        )
        // 09:30 + 10 min = 09:40, endTime = 10:00 → cap not exceeded.
        val expected = LocalDateTime.of(2026, 8, 16, 9, 40, 0).atZone(zoneId).toInstant().toEpochMilli()
        assertEquals(expected, millis)
    }

    @Test
    fun nudge_insideWindow_capsAtEndTime() {
        val now = LocalDateTime.of(2026, 8, 16, 9, 55, 0)
        val sel = workoutAt(LocalTime.of(9, 0), LocalTime.of(10, 0))
        val millis = NotificationScheduler.calculateNextNudgeMillis(
            selection = sel,
            policy = NudgePolicy(intervalMinutes = 10, maxPerWindow = 6),
            now = now,
            zoneId = zoneId,
            alreadyFiredCount = 4,
            isCompletedInWindow = false
        )
        // 09:55 + 10 min = 10:05 past end → cap at endTime.
        val expected = LocalDateTime.of(2026, 8, 16, 10, 0, 0).atZone(zoneId).toInstant().toEpochMilli()
        assertEquals(expected, millis)
    }

    @Test
    fun nudge_insideWindow_stopsOnceCapReached() {
        val now = LocalDateTime.of(2026, 8, 16, 9, 30, 0)
        val sel = workoutAt(LocalTime.of(9, 0), LocalTime.of(10, 0))
        val millis = NotificationScheduler.calculateNextNudgeMillis(
            selection = sel,
            policy = NudgePolicy(intervalMinutes = 10, maxPerWindow = 6),
            now = now,
            zoneId = zoneId,
            alreadyFiredCount = 6,
            isCompletedInWindow = false
        )
        val expected = LocalDateTime.of(2026, 8, 17, 9, 0, 0).atZone(zoneId).toInstant().toEpochMilli()
        assertEquals(expected, millis)
    }

    @Test
    fun nudge_stopsOnCompletion() {
        val now = LocalDateTime.of(2026, 8, 16, 9, 30, 0)
        val sel = workoutAt(LocalTime.of(9, 0), LocalTime.of(10, 0))
        val millis = NotificationScheduler.calculateNextNudgeMillis(
            selection = sel,
            policy = NudgePolicy(),
            now = now,
            zoneId = zoneId,
            alreadyFiredCount = 0,
            isCompletedInWindow = true
        )
        val expected = LocalDateTime.of(2026, 8, 17, 9, 0, 0).atZone(zoneId).toInstant().toEpochMilli()
        assertEquals(expected, millis)
    }

    @Test
    fun nudge_afterWindowEnd_schedulesTomorrowStart() {
        val now = LocalDateTime.of(2026, 8, 16, 11, 0, 0)
        val sel = workoutAt(LocalTime.of(9, 0), LocalTime.of(10, 0))
        val millis = NotificationScheduler.calculateNextNudgeMillis(
            selection = sel,
            policy = NudgePolicy(),
            now = now,
            zoneId = zoneId,
            alreadyFiredCount = 0,
            isCompletedInWindow = false
        )
        val expected = LocalDateTime.of(2026, 8, 17, 9, 0, 0).atZone(zoneId).toInstant().toEpochMilli()
        assertEquals(expected, millis)
    }

    @Test
    fun nudgePolicy_defaultsAre10And6() {
        val p = NudgePolicy()
        assertEquals(10, p.intervalMinutes)
        assertEquals(6, p.maxPerWindow)
        assertTrue(p.intervalMinutes in 1..120)
    }
}
