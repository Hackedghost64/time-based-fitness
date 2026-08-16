package com.timebasedfitness.app.di

import android.content.Context
import androidx.room.Room
import com.timebasedfitness.app.data.local.AppDatabase
import com.timebasedfitness.app.data.local.CategorySelectionDao
import com.timebasedfitness.app.data.local.CompletionLogDao
import com.timebasedfitness.app.data.local.RoutineDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "time_based_fitness.db"
        ).addMigrations(
            androidx.room.migration.Migration(1, 2) { database ->
                database.execSQL("CREATE TABLE IF NOT EXISTS routines (category TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, stepsJson TEXT NOT NULL)")
            }
        ).build()
    }

    @Provides
    fun provideCategorySelectionDao(db: AppDatabase): CategorySelectionDao {
        return db.categorySelectionDao()
    }

    @Provides
    fun provideCompletionLogDao(db: AppDatabase): CompletionLogDao {
        return db.completionLogDao()
    }

    @Provides
    fun provideRoutineDao(db: AppDatabase): RoutineDao = db.routineDao()
}
