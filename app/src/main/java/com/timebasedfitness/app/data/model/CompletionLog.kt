package com.timebasedfitness.app.data.model

import androidx.room.Entity
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "completion_logs",
    primaryKeys = ["date", "category"]
)
data class CompletionLog(
    val date: LocalDate,
    val category: Category,
    val completedAt: Instant
)
