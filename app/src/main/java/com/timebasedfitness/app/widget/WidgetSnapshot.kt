package com.timebasedfitness.app.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.timebasedfitness.app.data.model.CategorySelection
import com.timebasedfitness.app.domain.WindowMatcher
import java.time.LocalTime

object WidgetSnapshot {
    private const val PREFS = "widget_snapshot"
    private const val TITLE = "title"
    private const val SUBTITLE = "subtitle"

    suspend fun update(context: Context, selections: List<CategorySelection>) {
        val now = LocalTime.now()
        val active = WindowMatcher.getMatchingCategories(now, selections).firstOrNull()
        val next = WindowMatcher.getNextUpcoming(now, selections)
        val title: String
        val subtitle: String
        if (active != null) {
            title = active.category.displayName
            subtitle = "Routine ready now"
        } else if (next != null) {
            title = "Next: ${next.category.displayName}"
            subtitle = next.startTime.toString()
        } else {
            title = "Nothing scheduled"
            subtitle = "Open the app to set a routine"
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(TITLE, title).putString(SUBTITLE, subtitle).apply()
        NowRoutineWidget().updateAll(context)
    }

    fun read(context: Context): Pair<String, String> = context
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .let { it.getString(TITLE, "Time-Based Fitness")!! to it.getString(SUBTITLE, "Open to see your routine")!! }
}
