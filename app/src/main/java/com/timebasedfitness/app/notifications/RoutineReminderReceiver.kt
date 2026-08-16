package com.timebasedfitness.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.timebasedfitness.app.MainActivity
import com.timebasedfitness.app.data.model.Category

class RoutineReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val category = intent.getStringExtra(NotificationScheduler.EXTRA_CATEGORY)
            ?.let { runCatching { Category.valueOf(it) }.getOrNull() } ?: return
        createChannel(context)
        if (android.os.Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val openIntent = PendingIntentFactory.openApp(context, category.name)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.timebasedfitness.app.R.drawable.ic_launcher_foreground)
            .setContentTitle("${category.displayName} is ready")
            .setContentText("Open your checklist when you have a moment.")
            .setContentIntent(openIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(category.ordinal + 100, notification)
    }

    private fun createChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(CHANNEL_ID, "Routine reminders", NotificationManager.IMPORTANCE_DEFAULT))
    }

    companion object { const val CHANNEL_ID = "routine_reminders" }
}

private object PendingIntentFactory {
    fun openApp(context: Context, category: String): android.app.PendingIntent {
        val intent = Intent(context, MainActivity::class.java).putExtra(NotificationScheduler.EXTRA_CATEGORY, category)
        return android.app.PendingIntent.getActivity(context, category.hashCode(), intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
    }
}
