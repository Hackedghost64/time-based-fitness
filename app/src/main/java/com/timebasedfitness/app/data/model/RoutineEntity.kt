package com.timebasedfitness.app.data.model

import androidx.room.Entity

@Entity(tableName = "routines")
data class RoutineEntity(
    @androidx.room.PrimaryKey val category: Category,
    val title: String,
    val stepsJson: String
)
