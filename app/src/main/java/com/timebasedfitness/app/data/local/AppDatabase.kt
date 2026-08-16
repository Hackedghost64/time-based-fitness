package com.timebasedfitness.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.timebasedfitness.app.data.model.CategorySelection
import com.timebasedfitness.app.data.model.CompletionLog
import com.timebasedfitness.app.data.model.RoutineEntity

@Database(
    entities = [CategorySelection::class, CompletionLog::class, RoutineEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categorySelectionDao(): CategorySelectionDao
    abstract fun completionLogDao(): CompletionLogDao
    abstract fun routineDao(): RoutineDao
}
