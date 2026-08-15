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

@Singleton
class CompletionRepository @Inject constructor(
    private val dao: CompletionLogDao
) {
    val completionLogs: Flow<List<CompletionLog>> = dao.getAllLogs()

    val currentStreak: Flow<Int> = dao.getAllLogs().map { logs ->
        StreakCalculator.calculateStreak(logs)
    }

    suspend fun logCompletion(category: Category, date: LocalDate = LocalDate.now()) {
        val log = CompletionLog(
            date = date,
            category = category,
            completedAt = Instant.now()
        )
        dao.insertLog(log)
    }
}
