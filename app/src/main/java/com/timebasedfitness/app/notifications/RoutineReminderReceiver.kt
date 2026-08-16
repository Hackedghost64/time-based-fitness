package com.timebasedfitness.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.timebasedfitness.app.MainActivity
import com.timebasedfitness.app.R
import com.timebasedfitness.app.data.local.CategorySelectionDao
import com.timebasedfitness.app.data.local.CompletionLogDao
import com.timebasedfitness.app.data.model.Category
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class RoutineReminderReceiver : BroadcastReceiver() {
    @Inject lateinit var completionLogDao: CompletionLogDao
    @Inject lateinit var categorySelectionDao: CategorySelectionDao
    @Inject lateinit var scheduler: NotificationScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val categoryStr = intent.getStringExtra(NotificationScheduler.EXTRA_CATEGORY) ?: return
        val category = runCatching { Category.valueOf(categoryStr) }.getOrNull() ?: return

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Reschedule next occurrence so alarms do not drift with DST
                val selections = categorySelectionDao.getAllCategorySelectionsSync()
                val currentSel = selections.find { it.category == category }
                if (currentSel != null && currentSel.isEnabled) {
                    scheduler.scheduleNextReminder(currentSel)
                }

                // 2. Check if already completed today - if so, suppress reminder notification
                val isCompletedToday = completionLogDao.getCountForDate(category, LocalDate.now()) > 0
                if (isCompletedToday) {
                    return@launch
                }

                // 3. Post notification
                createChannel(context)
                if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    return@launch
                }

                val openIntent = PendingIntentFactory.openApp(context, category.name)
                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(context.getString(R.string.alert_ready))
                    .setContentText(context.getString(R.string.notification_description))
                    .setContentIntent(openIntent)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .build()
                NotificationManagerCompat.from(context).notify(category.ordinal + 100, notification)
            } finally {
                pending.finish()
            }
        }
    }

    private fun createChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Routine reminders", NotificationManager.IMPORTANCE_DEFAULT)
        )
    }

    companion object {
        const val CHANNEL_ID = "routine_reminders"
    }
}

private object PendingIntentFactory {
    fun openApp(context: Context, category: String): android.app.PendingIntent {
        val intent = Intent(context, MainActivity::class.java).putExtra(NotificationScheduler.EXTRA_CATEGORY, category)
        return android.app.PendingIntent.getActivity(
            context,
            category.hashCode(),
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
    }
}
