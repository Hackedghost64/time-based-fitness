package com.timebasedfitness.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalTime

@Entity(tableName = "category_selections")
data class CategorySelection(
    @PrimaryKey val category: Category,
    val isEnabled: Boolean,
    val startTime: LocalTime,
    val endTime: LocalTime
)
