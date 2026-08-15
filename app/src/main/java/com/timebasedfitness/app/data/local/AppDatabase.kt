package com.timebasedfitness.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.timebasedfitness.app.data.model.CategorySelection
import com.timebasedfitness.app.data.model.CompletionLog

@Database(
    entities = [CategorySelection::class, CompletionLog::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categorySelectionDao(): CategorySelectionDao
    abstract fun completionLogDao(): CompletionLogDao
}
