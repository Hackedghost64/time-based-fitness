package com.timebasedfitness.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.timebasedfitness.app.data.model.CategorySelection
import kotlinx.coroutines.flow.Flow

@Dao
interface CategorySelectionDao {

    @Query("SELECT * FROM category_selections")
    fun getAllCategorySelections(): Flow<List<CategorySelection>>

    @Query("SELECT * FROM category_selections")
    suspend fun getAllCategorySelectionsSync(): List<CategorySelection>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(selections: List<CategorySelection>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(selection: CategorySelection)
}
