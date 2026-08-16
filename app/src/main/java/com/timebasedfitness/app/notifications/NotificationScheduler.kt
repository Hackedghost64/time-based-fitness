package com.timebasedfitness.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.timebasedfitness.app.data.model.Category
import com.timebasedfitness.app.data.model.CategorySelection
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(@ApplicationContext private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun reschedule(selections: List<CategorySelection>) {
        cancelAll()
        selections.filter { it.isEnabled }.forEach { selection ->
            scheduleNextReminder(selection)
        }
    }

    fun scheduleNextReminder(
        selection: CategorySelection,
        now: LocalDateTime = LocalDateTime.now(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ) {
        if (!selection.isEnabled) return
        val triggerMillis = calculateNextTriggerMillis(selection.startTime, now, zoneId)
        val intent = Intent(context, RoutineReminderReceiver::class.java).putExtra(EXTRA_CATEGORY, selection.category.name)
        val pending = PendingIntent.getBroadcast(
            context,
            selection.category.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setAlarm(triggerMillis, pending)
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
