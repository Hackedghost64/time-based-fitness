package com.timebasedfitness.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.timebasedfitness.app.data.model.Category
import com.timebasedfitness.app.data.model.RoutineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines")
    suspend fun getAll(): List<RoutineEntity>

    @Query("SELECT * FROM routines WHERE category = :category")
    fun observe(category: Category): Flow<RoutineEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(routine: RoutineEntity)

    @Query("DELETE FROM routines WHERE category = :category")
    suspend fun delete(category: Category)
}
