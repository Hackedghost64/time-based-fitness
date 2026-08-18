package com.timebasedfitness.app.data.repository

import com.timebasedfitness.app.data.local.CompletionLogDao
import com.timebasedfitness.app.data.model.Category
import com.timebasedfitness.app.data.model.CompletionLog
import com.timebasedfitness.app.domain.StreakCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class CompletionRepository @Inject constructor(
    private val dao: CompletionLogDao,
    @ApplicationContext private val context: Context
) {
    val completionLogs: Flow<List<CompletionLog>> = dao.getAllLogs()

    val currentStreak: Flow<Int> = dao.getAllLogs().map { logs ->
        StreakCalculator.calculateStreak(logs)
    }

    val recentCompletedDates: Flow<Set<LocalDate>> = dao.getAllLogs().map { logs ->
        logs.map { it.date }.toSet()
    }

    suspend fun logCompletion(category: Category, date: LocalDate = LocalDate.now()) {
        val log = CompletionLog(
            date = date,
            category = category,
            completedAt = Instant.now()
        )
        dao.insertLog(log)
        clearNudgeCounterForToday(category)
    }

    /**
     * Returns true when a category has a completion entry on [date], OR the most-recent
     * completion entry was on [date] and the configured window has ended. Used by the
     * notification receiver and the routine detail screen so a finished routine stays
     * "done" until the window closes.
     */
    suspend fun isCompletedInCurrentWindow(
        category: Category,
        selectionEnd: java.time.LocalTime?,
        date: LocalDate = LocalDate.now(),
        now: java.time.LocalTime = java.time.LocalTime.now()
    ): Boolean {
        if (dao.getCountForDate(category, date) > 0) return true
        val last = getMostRecentForCategorySync(category) ?: return false
        if (last.date != date) return false
        if (selectionEnd == null) return true
        return !now.isBefore(selectionEnd)
    }

    /** Get the most-recent completion log for a category, or null if none. */
    suspend fun getMostRecentForCategory(category: Category): CompletionLog? =
        getMostRecentForCategorySync(category)

    private suspend fun getMostRecentForCategorySync(category: Category): CompletionLog? =
        dao.getAllLogsSync().firstOrNull { it.category == category }

    // ----- Nudge counter (SharedPreferences-backed) ----------------------------
    private val nudgePrefs = context.getSharedPreferences("nudge_counters", Context.MODE_PRIVATE)

    private fun counterKey(category: Category, date: LocalDate): String =
        "${category.name}_${date}_${date.year}"

    /** Increment and return the new count for [category] on [date]. */
    fun incrementNudgeCounter(category: Category, date: LocalDate = LocalDate.now()): Int {
        val key = counterKey(category, date)
        val next = (nudgePrefs.getInt(key, 0)) + 1
        nudgePrefs.edit().putInt(key, next).apply()
        return next
    }

    /** Current count without mutating. */
    fun nudgeCounter(category: Category, date: LocalDate = LocalDate.now()): Int =
        nudgePrefs.getInt(counterKey(category, date), 0)

    /** Reset the counter for [category] on [date] (used when the user completes the routine). */
    fun clearNudgeCounterForToday(category: Category, date: LocalDate = LocalDate.now()) {
        nudgePrefs.edit().remove(counterKey(category, date)).apply()
    }

    /** Wipe today's counters for all categories — called on boot / TZ change / app upgrade. */
    fun clearNudgeCountersForToday(date: LocalDate = LocalDate.now()) {
        val editor = nudgePrefs.edit()
        Category.entries.forEach { cat ->
            editor.remove(counterKey(cat, date))
        }
        editor.apply()
    }
}
