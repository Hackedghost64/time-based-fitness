package com.timebasedfitness.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.timebasedfitness.app.data.repository.CategoryRepository
import com.timebasedfitness.app.data.repository.CompletionRepository

import android.util.Log

@AndroidEntryPoint
class RescheduleReceiver : BroadcastReceiver() {
    @Inject lateinit var categoryRepository: CategoryRepository
    @Inject lateinit var scheduler: NotificationScheduler
    @Inject lateinit var completionRepository: CompletionRepository

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val selections = categoryRepository.categorySelections.first()
                // Clear stale nudge counters on reboot / time-zone / package upgrade so
                // the new window starts fresh.
                completionRepository.clearNudgeCountersForToday()
                scheduler.reschedule(selections)
            }.onFailure { e ->
                Log.e("RescheduleReceiver", "Failed to reschedule reminders on broadcast: ${intent.action}", e)
            }
            pending.finish()
        }
    }
}
