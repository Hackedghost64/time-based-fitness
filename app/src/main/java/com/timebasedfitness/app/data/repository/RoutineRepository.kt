package com.timebasedfitness.app.data.repository

import com.timebasedfitness.app.data.content.ContentRepository
import com.timebasedfitness.app.data.content.FitnessPlanJson
import com.timebasedfitness.app.data.content.PlanCategoryJson
import com.timebasedfitness.app.data.content.PlanJsonCodec
import com.timebasedfitness.app.data.content.RoutineContent
import com.timebasedfitness.app.data.local.RoutineDao
import com.timebasedfitness.app.data.model.Category
import com.timebasedfitness.app.data.model.RoutineEntity
import org.json.JSONArray
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineRepository @Inject constructor(
    private val dao: RoutineDao,
    private val contentRepository: ContentRepository
) {
    fun observe(category: Category): Flow<RoutineContent?> = dao.observe(category).map { entity ->
        entity?.let { RoutineContent(it.title, decodeSteps(it.stepsJson)) }
            ?: contentRepository.getRoutine(category)
    }

    suspend fun save(category: Category, content: RoutineContent) {
        val stepsJson = JSONArray(content.steps).toString()
        dao.upsert(
            RoutineEntity(
                category = category,
                title = content.title.trim(),
                stepsJson = stepsJson
            )
        )
    }

    suspend fun reset(category: Category) {
        dao.delete(category)
    }

    suspend fun importPlan(plan: FitnessPlanJson) {
        // Per-item isolation: a single bad row should not abort the entire import.
        // PlanJsonCodec already enforces enum membership for `category`, but this is
        // defense-in-depth for any future caller that constructs a FitnessPlanJson
        // without going through the codec.
        plan.categories.forEach { item ->
            runCatching {
                val category = Category.valueOf(item.category)
                save(category, RoutineContent(item.title, item.steps))
            }
        }
    }

    suspend fun exportPlan(): String {
        val overrides = dao.getAll().associateBy { it.category }
        val categories = Category.entries.mapNotNull { category ->
            overrides[category]?.let { entity ->
                PlanCategoryJson(category.name, entity.title, steps = decodeSteps(entity.stepsJson))
            } ?: contentRepository.getRoutine(category)?.let { content ->
                PlanCategoryJson(category.name, content.title, steps = content.steps)
            }
        }
        return PlanJsonCodec.encode(FitnessPlanJson(categories = categories))
    }

    private fun decodeSteps(json: String): List<String> = runCatching {
        Json.decodeFromString<List<String>>(json)
    }.getOrDefault(emptyList())
}
