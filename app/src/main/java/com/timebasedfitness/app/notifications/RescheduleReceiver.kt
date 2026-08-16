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

@AndroidEntryPoint
class RescheduleReceiver : BroadcastReceiver() {
    @Inject lateinit var categoryRepository: CategoryRepository
    @Inject lateinit var scheduler: NotificationScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            runCatching { scheduler.reschedule(categoryRepository.categorySelections.first()) }
            pending.finish()
        }
    }
}
