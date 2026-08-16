package com.timebasedfitness.app.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.timebasedfitness.app.data.model.Category
import com.timebasedfitness.app.data.model.CategorySelection
import com.timebasedfitness.app.data.repository.CategoryRepository
import com.timebasedfitness.app.domain.WindowMatcher
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object WidgetSnapshot {
    private const val PREFS = "widget_snapshot"
    private const val KEY_SELECTIONS = "saved_selections"
    private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)

    suspend fun update(context: Context, selections: List<CategorySelection>) {
        saveSelections(context, selections)
        NowRoutineWidget().updateAll(context)
    }

    fun compute(context: Context, now: LocalTime = LocalTime.now()): Pair<String, String> {
        val selections = loadSelections(context)
        return computeFromSelections(selections, now)
    }

    fun computeFromSelections(selections: List<CategorySelection>, now: LocalTime = LocalTime.now()): Pair<String, String> {
        val active = WindowMatcher.getMatchingCategories(now, selections).firstOrNull()
        val next = WindowMatcher.getNextUpcoming(now, selections)

        return when {
            active != null -> {
                active.category.displayName to "Routine ready now"
            }
            next != null -> {
                "Next: ${next.category.displayName}" to "Starts at ${next.startTime.format(timeFormatter)}"
            }
            else -> {
                "Nothing scheduled" to "Open the app to set a routine"
            }
        }
    }

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
