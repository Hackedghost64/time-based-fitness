package com.timebasedfitness.app.data.repository

import com.timebasedfitness.app.data.local.CategorySelectionDao
import com.timebasedfitness.app.data.model.Category
import com.timebasedfitness.app.data.model.CategorySelection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val dao: CategorySelectionDao
) {
    val categorySelections: Flow<List<CategorySelection>> = dao.getAllCategorySelections()
        .onStart {
            val existing = dao.getAllCategorySelectionsSync()
            if (existing.isEmpty()) {
                dao.insertAll(getDefaultSelections())
            }
        }

    suspend fun saveSelections(selections: List<CategorySelection>) {
        dao.insertAll(selections)
    }

    companion object {
        fun getDefaultSelections(): List<CategorySelection> {
            return listOf(
                CategorySelection(Category.MORNING, isEnabled = true, startTime = LocalTime.of(6, 0), endTime = LocalTime.of(9, 0)),
                CategorySelection(Category.MEALS, isEnabled = true, startTime = LocalTime.of(12, 0), endTime = LocalTime.of(14, 0)),
                CategorySelection(Category.WORKOUT, isEnabled = true, startTime = LocalTime.of(17, 0), endTime = LocalTime.of(19, 0)),
                CategorySelection(Category.EVENING, isEnabled = true, startTime = LocalTime.of(21, 0), endTime = LocalTime.of(23, 0))
            )
        }
    }
}
