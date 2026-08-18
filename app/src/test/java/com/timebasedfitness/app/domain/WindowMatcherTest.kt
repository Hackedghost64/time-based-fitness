package com.timebasedfitness.app.domain

import com.timebasedfitness.app.data.model.Category
import com.timebasedfitness.app.data.model.CategorySelection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class WindowMatcherTest {

    @Test
    fun isInWindow_standardWindow_withinRange() {
        val now = LocalTime.of(8, 0)
        val start = LocalTime.of(6, 0)
        val end = LocalTime.of(9, 0)

        assertTrue(WindowMatcher.isInWindow(now, start, end))
    }

    @Test
    fun isInWindow_standardWindow_outsideRange() {
        val now = LocalTime.of(10, 0)
        val start = LocalTime.of(6, 0)
        val end = LocalTime.of(9, 0)

        assertFalse(WindowMatcher.isInWindow(now, start, end))
    }

    @Test
    fun isInWindow_midnightCrossing_withinRangeBeforeMidnight() {
        val now = LocalTime.of(23, 0)
        val start = LocalTime.of(22, 0)
        val end = LocalTime.of(2, 0)

        assertTrue(WindowMatcher.isInWindow(now, start, end))
    }

    @Test
    fun isInWindow_midnightCrossing_withinRangeAfterMidnight() {
        val now = LocalTime.of(1, 0)
        val start = LocalTime.of(22, 0)
        val end = LocalTime.of(2, 0)

        assertTrue(WindowMatcher.isInWindow(now, start, end))
    }

    @Test
    fun isInWindow_midnightCrossing_outsideRange() {
        val now = LocalTime.of(12, 0)
        val start = LocalTime.of(22, 0)
        val end = LocalTime.of(2, 0)

        assertFalse(WindowMatcher.isInWindow(now, start, end))
    }

    @Test
    fun getMatchingCategories_returnsSortedEnabledCategories() {
        val selections = listOf(
            CategorySelection(Category.WORKOUT, isEnabled = true, startTime = LocalTime.of(17, 0), endTime = LocalTime.of(19, 0)),
            CategorySelection(Category.MORNING, isEnabled = true, startTime = LocalTime.of(6, 0), endTime = LocalTime.of(9, 0)),
            CategorySelection(Category.MEALS, isEnabled = false, startTime = LocalTime.of(6, 0), endTime = LocalTime.of(9, 0))
        )

        val matching = WindowMatcher.getMatchingCategories(LocalTime.of(7, 0), selections)

        assertEquals(1, matching.size)
        assertEquals(Category.MORNING, matching.first().category)
    }

    @Test
    fun getNextUpcoming_returnsEarliestFutureCategoryToday() {
        val selections = listOf(
            CategorySelection(Category.MORNING, isEnabled = true, startTime = LocalTime.of(6, 0), endTime = LocalTime.of(9, 0)),
            CategorySelection(Category.WORKOUT, isEnabled = true, startTime = LocalTime.of(17, 0), endTime = LocalTime.of(19, 0))
        )

        val next = WindowMatcher.getNextUpcoming(LocalTime.of(12, 0), selections)

        assertEquals(Category.WORKOUT, next?.category)
    }

    @Test
    fun getNextUpcoming_wrapsToEarliestTomorrow() {
        val selections = listOf(
            CategorySelection(Category.MORNING, isEnabled = true, startTime = LocalTime.of(6, 0), endTime = LocalTime.of(9, 0)),
            CategorySelection(Category.WORKOUT, isEnabled = true, startTime = LocalTime.of(17, 0), endTime = LocalTime.of(19, 0))
        )

        val next = WindowMatcher.getNextUpcoming(LocalTime.of(20, 0), selections)

        assertEquals(Category.MORNING, next?.category)
    }

    // Tests verifying TimeWindow integration consistency
    @Test
    fun `WindowMatcher uses TimeWindow internally - overnight consistency`() {
        // This test verifies that WindowMatcher delegates to TimeWindow correctly
        val overnightWindow = TimeWindow(LocalTime.of(22, 0), LocalTime.of(2, 0))
        
        // Both should agree on window containment
        listOf(
            LocalTime.of(23, 0),
            LocalTime.of(0, 0),
            LocalTime.of(1, 0),
            LocalTime.of(22, 0),
            LocalTime.of(2, 0)
        ).forEach { time ->
            val matcherResult = WindowMatcher.isInWindow(time, LocalTime.of(22, 0), LocalTime.of(2, 0))
            val timeWindowResult = overnightWindow.contains(time)
            assertEquals("Mismatch at $time", timeWindowResult, matcherResult)
        }
    }

    @Test
    fun `WindowMatcher uses TimeWindow internally - normal consistency`() {
        val normalWindow = TimeWindow(LocalTime.of(6, 0), LocalTime.of(9, 0))
        
        listOf(
            LocalTime.of(6, 0),
            LocalTime.of(7, 30),
            LocalTime.of(9, 0),
            LocalTime.of(5, 59),
            LocalTime.of(9, 1)
        ).forEach { time ->
            val matcherResult = WindowMatcher.isInWindow(time, LocalTime.of(6, 0), LocalTime.of(9, 0))
            val timeWindowResult = normalWindow.contains(time)
            assertEquals("Mismatch at $time", timeWindowResult, matcherResult)
        }
    }
}
