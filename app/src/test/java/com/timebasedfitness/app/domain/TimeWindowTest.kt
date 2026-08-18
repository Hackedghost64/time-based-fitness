package com.timebasedfitness.app.domain

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime
import java.time.LocalTime

class TimeWindowTest {

    @Test
    fun `normal window contains time inside range`() {
        val window = TimeWindow(LocalTime.of(6, 0), LocalTime.of(9, 0))
        assertTrue(window.contains(LocalTime.of(7, 0)))
        assertTrue(window.contains(LocalTime.of(6, 0)))
        assertTrue(window.contains(LocalTime.of(9, 0)))
    }

    @Test
    fun `normal window does not contain time outside range`() {
        val window = TimeWindow(LocalTime.of(6, 0), LocalTime.of(9, 0))
        assertFalse(window.contains(LocalTime.of(5, 59)))
        assertFalse(window.contains(LocalTime.of(9, 1)))
        assertFalse(window.contains(LocalTime.of(22, 0)))
    }

    @Test
    fun `overnight window contains time inside range`() {
        val window = TimeWindow(LocalTime.of(22, 0), LocalTime.of(2, 0))
        assertTrue(window.contains(LocalTime.of(23, 0)))
        assertTrue(window.contains(LocalTime.of(22, 0)))
        assertTrue(window.contains(LocalTime.of(2, 0)))
        assertTrue(window.contains(LocalTime.of(1, 0)))
        assertTrue(window.contains(LocalTime.of(0, 0)))
    }

    @Test
    fun `overnight window does not contain time outside range`() {
        val window = TimeWindow(LocalTime.of(22, 0), LocalTime.of(2, 0))
        assertFalse(window.contains(LocalTime.of(21, 59)))
        assertFalse(window.contains(LocalTime.of(2, 1)))
        assertFalse(window.contains(LocalTime.of(12, 0)))
    }

    @Test
    fun `overnight window startDateTime is yesterday when now is before start`() {
        val window = TimeWindow(LocalTime.of(22, 0), LocalTime.of(2, 0))
        val now = LocalDateTime.of(2024, 1, 15, 10, 0) // 10:00 AM, before window starts
        val start = window.startDateTime(now)
        assertEquals(LocalDateTime.of(2024, 1, 14, 22, 0), start)
    }

    @Test
    fun `overnight window startDateTime is today when now is in window`() {
        val window = TimeWindow(LocalTime.of(22, 0), LocalTime.of(2, 0))
        val now = LocalDateTime.of(2024, 1, 15, 23, 0) // 11:00 PM, in window
        val start = window.startDateTime(now)
        assertEquals(LocalDateTime.of(2024, 1, 15, 22, 0), start)
    }

    @Test
    fun `overnight window endDateTime is today when now is in window`() {
        val window = TimeWindow(LocalTime.of(22, 0), LocalTime.of(2, 0))
        val now = LocalDateTime.of(2024, 1, 15, 23, 0) // 11:00 PM, in window
        val end = window.endDateTime(now)
        assertEquals(LocalDateTime.of(2024, 1, 16, 2, 0), end)
    }

    @Test
    fun `overnight window endDateTime is tomorrow when now is before start`() {
        val window = TimeWindow(LocalTime.of(22, 0), LocalTime.of(2, 0))
        val now = LocalDateTime.of(2024, 1, 15, 10, 0) // 10:00 AM, before window
        val end = window.endDateTime(now)
        assertEquals(LocalDateTime.of(2024, 1, 16, 2, 0), end)
    }

    @Test
    fun `normal window startDateTime and endDateTime are today`() {
        val window = TimeWindow(LocalTime.of(6, 0), LocalTime.of(9, 0))
        val now = LocalDateTime.of(2024, 1, 15, 7, 0)
        assertEquals(LocalDateTime.of(2024, 1, 15, 6, 0), window.startDateTime(now))
        assertEquals(LocalDateTime.of(2024, 1, 15, 9, 0), window.endDateTime(now))
    }

    @Test
    fun `remainingDuration returns correct value`() {
        val window = TimeWindow(LocalTime.of(6, 0), LocalTime.of(9, 0))
        val now = LocalDateTime.of(2024, 1, 15, 7, 0)
        val duration = window.remainingDuration(now)
        assertNotNull(duration)
        assertEquals(120, duration!!.toMinutes())
    }

    @Test
    fun `remainingDuration returns null when window has ended`() {
        val window = TimeWindow(LocalTime.of(6, 0), LocalTime.of(9, 0))
        val now = LocalDateTime.of(2024, 1, 15, 10, 0)
        assertNull(window.remainingDuration(now))
    }

    @Test
    fun `nextOccurrence for normal window before start is today`() {
        val window = TimeWindow(LocalTime.of(6, 0), LocalTime.of(9, 0))
        val now = LocalDateTime.of(2024, 1, 15, 5, 0)
        val next = window.nextOccurrence(now)
        assertEquals(LocalDateTime.of(2024, 1, 15, 6, 0), next)
    }

    @Test
    fun `nextOccurrence for normal window after start is tomorrow`() {
        val window = TimeWindow(LocalTime.of(6, 0), LocalTime.of(9, 0))
        val now = LocalDateTime.of(2024, 1, 15, 7, 0)
        val next = window.nextOccurrence(now)
        assertEquals(LocalDateTime.of(2024, 1, 16, 6, 0), next)
    }

    @Test
    fun `nextOccurrence for overnight window before start is today`() {
        val window = TimeWindow(LocalTime.of(22, 0), LocalTime.of(2, 0))
        val now = LocalDateTime.of(2024, 1, 15, 10, 0)
        val next = window.nextOccurrence(now)
        assertEquals(LocalDateTime.of(2024, 1, 15, 22, 0), next)
    }

    @Test
    fun `nextOccurrence for overnight window in window is tomorrow`() {
        val window = TimeWindow(LocalTime.of(22, 0), LocalTime.of(2, 0))
        val now = LocalDateTime.of(2024, 1, 15, 23, 0)
        val next = window.nextOccurrence(now)
        assertEquals(LocalDateTime.of(2024, 1, 16, 22, 0), next)
    }

    @Test
    fun `hasEnded returns true after window ends`() {
        val window = TimeWindow(LocalTime.of(6, 0), LocalTime.of(9, 0))
        val now = LocalDateTime.of(2024, 1, 15, 10, 0)
        assertTrue(window.hasEnded(now))
    }

    @Test
    fun `hasEnded returns false during window`() {
        val window = TimeWindow(LocalTime.of(6, 0), LocalTime.of(9, 0))
        val now = LocalDateTime.of(2024, 1, 15, 7, 0)
        assertFalse(window.hasEnded(now))
    }

    @Test
    fun `hasNotStarted returns true before window starts`() {
        val window = TimeWindow(LocalTime.of(6, 0), LocalTime.of(9, 0))
        val now = LocalDateTime.of(2024, 1, 15, 5, 0)
        assertTrue(window.hasNotStarted(now))
    }

    @Test
    fun `hasNotStarted returns false during window`() {
        val window = TimeWindow(LocalTime.of(6, 0), LocalTime.of(9, 0))
        val now = LocalDateTime.of(2024, 1, 15, 7, 0)
        assertFalse(window.hasNotStarted(now))
    }

    @Test
    fun `isOvernight flag is correct`() {
        assertTrue(TimeWindow(LocalTime.of(22, 0), LocalTime.of(2, 0)).isOvernight)
        assertFalse(TimeWindow(LocalTime.of(6, 0), LocalTime.of(9, 0)).isOvernight)
    }
}
