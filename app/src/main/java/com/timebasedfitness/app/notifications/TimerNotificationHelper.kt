package com.timebasedfitness.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.timebasedfitness.app.MainActivity
import com.timebasedfitness.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    init {
        createChannel()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Active Routine Timers",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live countdown for workout and meditation timers"
                setShowBadge(false)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun showTimerNotification(stepTitle: String, remainingSeconds: Int, categoryName: String) {
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        val timeStr = "%02d:%02d".format(minutes, seconds)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("category", categoryName)
        }
        val pending = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⏱ $timeStr • $stepTitle")
            .setContentText("Tap to return to your routine")
            .setContentIntent(pending)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        notificationManager?.notify(NOTIFICATION_ID, notification)
    }

    fun dismiss() {
        notificationManager?.cancel(NOTIFICATION_ID)
    }

    companion object {
        const val CHANNEL_ID = "active_routine_timers"
        const val NOTIFICATION_ID = 9001
    }
}
