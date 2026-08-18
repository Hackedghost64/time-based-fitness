package com.timebasedfitness.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.timebasedfitness.app.data.model.Category
import com.timebasedfitness.app.data.model.CategorySelection
import com.timebasedfitness.app.data.repository.CompletionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/** Cadence + cap for the per-window "nudge" reminders. */
data class NudgePolicy(
    val intervalMinutes: Int = 10,
    val maxPerWindow: Int = 6
) {
    init {
        require(intervalMinutes in 1..120) { "intervalMinutes out of range: $intervalMinutes" }
        require(maxPerWindow in 0..48) { "maxPerWindow out of range: $maxPerWindow" }
    }
}

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val completionRepository: CompletionRepository
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun reschedule(
        selections: List<CategorySelection>,
        policy: NudgePolicy = NudgePolicy()
    ) {
        cancelAll()
        selections.filter { it.isEnabled }.forEach { selection ->
            scheduleNextReminder(selection, policy)
        }
    }

    /**
     * Schedule the next wake-up for [selection]. Pre-checks completion via the
     * injected repository so a completed routine stops firing, and consults the
     * daily nudge counter to cap re-fires at `policy.maxPerWindow`.
     */
    fun scheduleNextReminder(
        selection: CategorySelection,
        policy: NudgePolicy = NudgePolicy(),
        now: LocalDateTime = LocalDateTime.now(),
        zoneId: ZoneId = ZoneId.systemDefault(),
        isCompletedInWindow: Boolean? = null
    ) {
        if (!selection.isEnabled) return
        val today = now.toLocalDate()
        val completed = isCompletedInWindow
            ?: kotlinx.coroutines.runBlocking {
                completionRepository.isCompletedInCurrentWindow(
                    category = selection.category,
                    selectionEnd = selection.endTime,
                    date = today,
                    now = now.toLocalTime()
                )
            }
        val count = completionRepository.nudgeCounter(selection.category, today)
        val pending = pendingIntentFor(selection)
        val triggerMillis = calculateNextNudgeMillis(
            selection = selection,
            policy = policy,
            now = now,
            zoneId = zoneId,
            alreadyFiredCount = count,
            isCompletedInWindow = completed
        )
        setAlarm(triggerMillis, pending)
    }

    /** Called after a wake-up actually fires a notification — bumps the daily cap. */
    fun recordNudgeFire(category: Category, date: LocalDate = LocalDate.now()) {
        completionRepository.incrementNudgeCounter(category, date)
    }

    private fun setAlarm(triggerMillis: Long, pending: PendingIntent) {
        if (alarmManager == null) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pending)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pending)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pending)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerMillis, pending)
        }
    }

    private fun pendingIntentFor(selection: CategorySelection): PendingIntent {
        val intent = Intent(context, RoutineReminderReceiver::class.java)
            .putExtra(EXTRA_CATEGORY, selection.category.name)
        return PendingIntent.getBroadcast(
            context,
            selection.category.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun cancelAll() {
        Category.entries.forEach { category ->
            val intent = Intent(context, RoutineReminderReceiver::class.java).putExtra(EXTRA_CATEGORY, category.name)
            PendingIntent.getBroadcast(
                context,
                category.ordinal,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )?.let {
                alarmManager?.cancel(it)
                it.cancel()
            }
        }
    }

    companion object {
        const val EXTRA_CATEGORY = "category"

        /**
         * Compute the next reminder time. Pure, testable.
         *
         * Behaviour:
         *  - Before today's window → fire at startTime.
         *  - Inside window, not yet capped, not yet completed → fire `intervalMinutes`
         *    from now, capped at endTime.
         *  - Inside window but cap reached → fire tomorrow's startTime.
         *  - Already completed this window → fire tomorrow's startTime.
         *  - After endTime → fire tomorrow's startTime.
         */
        fun calculateNextNudgeMillis(
            selection: CategorySelection,
            policy: NudgePolicy,
            now: LocalDateTime,
            zoneId: ZoneId,
            alreadyFiredCount: Int,
            isCompletedInWindow: Boolean
        ): Long {
            val today = now.toLocalDate()
            val nowTime = now.toLocalTime()
            val startDt = today.atTime(selection.startTime)
            val endDt = today.atTime(selection.endTime)

            if (nowTime.isBefore(selection.startTime)) {
                return startDt.atZone(zoneId).toInstant().toEpochMilli()
            }

            if (isCompletedInWindow || !nowTime.isBefore(selection.endTime)) {
                return startDt.plusDays(1).atZone(zoneId).toInstant().toEpochMilli()
            }

            if (alreadyFiredCount >= policy.maxPerWindow) {
                return startDt.plusDays(1).atZone(zoneId).toInstant().toEpochMilli()
            }

            val candidate = now.plusMinutes(policy.intervalMinutes.toLong())
            val capped = if (candidate.toLocalTime().isAfter(selection.endTime)) endDt else candidate
            return capped.atZone(zoneId).toInstant().toEpochMilli()
        }

        /** Original "next start time" math — used by callers that want a hard reset. */
        fun calculateNextTriggerMillis(
            startTime: LocalTime,
            now: LocalDateTime = LocalDateTime.now(),
            zoneId: ZoneId = ZoneId.systemDefault()
        ): Long {
            var target = now.toLocalDate().atTime(startTime.hour, startTime.minute)
            if (!target.isAfter(now)) {
                target = target.plusDays(1)
            }
            return target.atZone(zoneId).toInstant().toEpochMilli()
        }
    }
}
