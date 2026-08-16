package com.timebasedfitness.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.timebasedfitness.app.data.model.CategorySelection
import java.time.LocalDateTime
import java.time.ZoneId
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationScheduler @Inject constructor(@ApplicationContext private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun reschedule(selections: List<CategorySelection>) {
        cancelAll()
        selections.filter { it.isEnabled }.forEach { selection ->
            val trigger = nextTrigger(selection.startTime.hour, selection.startTime.minute)
            val intent = Intent(context, RoutineReminderReceiver::class.java).putExtra(EXTRA_CATEGORY, selection.category.name)
            val pending = PendingIntent.getBroadcast(
                context, selection.category.ordinal, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                trigger,
                AlarmManager.INTERVAL_DAY,
                pending
            )
        }
    }

    fun cancelAll() {
        CategorySelectionCategories.values.forEach { categoryOrdinal ->
            val intent = Intent(context, RoutineReminderReceiver::class.java)
            PendingIntent.getBroadcast(context, categoryOrdinal, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)?.let {
                alarmManager.cancel(it)
                it.cancel()
            }
        }
    }

    private fun nextTrigger(hour: Int, minute: Int): Long {
        val now = LocalDateTime.now()
        var target = now.toLocalDate().atTime(hour, minute)
        if (!target.isAfter(now)) target = target.plusDays(1)
        return target.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private object CategorySelectionCategories {
        val values = 0..3
    }

    companion object {
        const val EXTRA_CATEGORY = "category"
    }
}
