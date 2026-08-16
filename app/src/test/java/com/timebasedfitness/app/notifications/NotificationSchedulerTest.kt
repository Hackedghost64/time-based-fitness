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
}
