package com.timebasedfitness.app.widget

import com.timebasedfitness.app.data.model.Category
import com.timebasedfitness.app.data.model.CategorySelection
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalTime

class WidgetSnapshotTest {

    private val selections = listOf(
        CategorySelection(Category.MORNING, isEnabled = true, startTime = LocalTime.of(6, 0), endTime = LocalTime.of(9, 0)),
        CategorySelection(Category.WORKOUT, isEnabled = true, startTime = LocalTime.of(17, 0), endTime = LocalTime.of(19, 0)),
        CategorySelection(Category.EVENING, isEnabled = false, startTime = LocalTime.of(21, 0), endTime = LocalTime.of(23, 0))
    )

    @Test
    fun computeFromSelections_duringActiveWindow_showsActiveCategory() {
        val now = LocalTime.of(7, 30) // Within Morning (06:00 - 09:00)
        val (title, subtitle) = WidgetSnapshot.computeFromSelections(selections, now)

        assertEquals("Morning", title)
        assertEquals("Routine ready now", subtitle)
    }

    @Test
    fun computeFromSelections_beforeNextWindow_showsNextUpcomingCategory() {
        val now = LocalTime.of(10, 0) // Next is Workout at 17:00
        val (title, subtitle) = WidgetSnapshot.computeFromSelections(selections, now)

        assertEquals("Next: Workout", title)
        assertEquals("Starts at 05:00 PM", subtitle)
    }

    @Test
    fun computeFromSelections_whenNoCategoriesEnabled_showsNothingScheduled() {
        val disabledSelections = selections.map { it.copy(isEnabled = false) }
        val now = LocalTime.of(7, 0)
        val (title, subtitle) = WidgetSnapshot.computeFromSelections(disabledSelections, now)

        assertEquals("Nothing scheduled", title)
        assertEquals("Open the app to set a routine", subtitle)
    }
}
