package com.timebasedfitness.app.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.timebasedfitness.app.data.model.Category
import com.timebasedfitness.app.data.model.CategorySelection
import com.timebasedfitness.app.data.repository.CategoryRepository
import com.timebasedfitness.app.domain.WindowMatcher
import org.json.JSONArray
import org.json.JSONObject
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Sealed model that the home-screen widget renders. Kept pure & testable so the
 * Glance composable switches on it. The Active / Next branches carry everything
 * the widget needs to draw — no further DB lookups inside `provideContent`.
 */
sealed class WidgetState {
    data class Active(
        val category: Category,
        val title: String,
        val completed: Int,
        val total: Int,
        val minutesRemaining: Int
    ) : WidgetState()

    data class Next(
        val category: Category,
        val title: String,
        val startsAt: LocalTime,
        val streak: Int
    ) : WidgetState()

    data object Idle : WidgetState()
}

object WidgetSnapshot {
    private const val PREFS = "widget_snapshot"
    private const val KEY_SELECTIONS = "saved_selections"
    val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)

    suspend fun update(context: Context, selections: List<CategorySelection>) {
        saveSelections(context, selections)
        NowRoutineWidget().updateAll(context)
    }

    fun compute(
        context: Context,
        now: LocalTime = LocalTime.now(),
        streak: Int = 0,
        totalSteps: Int? = null,
        completedSteps: Int? = null,
        routineTitle: String? = null,
        zone: ZoneId = ZoneId.systemDefault()
    ): WidgetState {
        val selections = loadSelections(context)
        return computeFromSelections(
            selections = selections,
            now = now,
            streak = streak,
            totalSteps = totalSteps,
            completedSteps = completedSteps,
            routineTitle = routineTitle,
            zone = zone
        )
    }

    fun computeFromSelections(
        selections: List<CategorySelection>,
        now: LocalTime = LocalTime.now(),
        streak: Int = 0,
        totalSteps: Int? = null,
        completedSteps: Int? = null,
        routineTitle: String? = null,
        @Suppress("UNUSED_PARAMETER") zone: ZoneId = ZoneId.systemDefault()
    ): WidgetState {
        val active = WindowMatcher.getMatchingCategories(now, selections).firstOrNull()
        if (active != null) {
            val windowMinutes = Duration.between(active.startTime, active.endTime).toMinutes().toInt().coerceAtLeast(1)
            val elapsed = Duration.between(active.startTime, now).toMinutes().toInt().coerceAtLeast(0)
            val remaining = (windowMinutes - elapsed).coerceAtLeast(0)
            return WidgetState.Active(
                category = active.category,
                title = routineTitle ?: active.category.displayName,
                completed = completedSteps ?: 0,
                total = totalSteps ?: 0,
                minutesRemaining = remaining
            )
        }
        val next = WindowMatcher.getNextUpcoming(now, selections)
        if (next != null) {
            return WidgetState.Next(
                category = next.category,
                title = routineTitle ?: next.category.displayName,
                startsAt = next.startTime,
                streak = streak
            )
        }
        return WidgetState.Idle
    }

    /**
     * Resolve the current weekday key with explicit zone, falling back to "MONDAY" if
     * the system zone is unreliable. Exposed for callers that want to key their own
     * content map by day.
     */
    fun currentDayKey(zone: ZoneId = ZoneId.systemDefault()): String =
        runCatching { LocalDate.now(zone).dayOfWeek.name }.getOrDefault("MONDAY")

    private fun saveSelections(context: Context, selections: List<CategorySelection>) {
        val jsonArray = JSONArray()
        selections.forEach { sel ->
            val obj = JSONObject()
            obj.put("category", sel.category.name)
            obj.put("isEnabled", sel.isEnabled)
            obj.put("startTime", sel.startTime.toString())
            obj.put("endTime", sel.endTime.toString())
            jsonArray.put(obj)
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_SELECTIONS, jsonArray.toString())
            .apply()
    }

    fun loadSelections(context: Context): List<CategorySelection> {
        val jsonStr = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_SELECTIONS, null) ?: return CategoryRepository.getDefaultSelections()
        return runCatching {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<CategorySelection>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val category = Category.valueOf(obj.getString("category"))
                val isEnabled = obj.getBoolean("isEnabled")
                val startTime = LocalTime.parse(obj.getString("startTime"))
                val endTime = LocalTime.parse(obj.getString("endTime"))
                list.add(CategorySelection(category, isEnabled, startTime, endTime))
            }
            if (list.isEmpty()) CategoryRepository.getDefaultSelections() else list
        }.getOrDefault(CategoryRepository.getDefaultSelections())
    }
}
