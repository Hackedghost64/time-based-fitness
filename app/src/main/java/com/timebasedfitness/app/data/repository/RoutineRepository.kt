package com.timebasedfitness.app.data.repository

import androidx.room.withTransaction
import com.timebasedfitness.app.data.content.ContentRepository
import com.timebasedfitness.app.data.content.FitnessPlanJson
import com.timebasedfitness.app.data.content.PlanCategoryJson
import com.timebasedfitness.app.data.content.PlanJsonCodec
import com.timebasedfitness.app.data.content.RoutineContent
import com.timebasedfitness.app.data.local.AppDatabase
import com.timebasedfitness.app.data.local.CategorySelectionDao
import com.timebasedfitness.app.data.local.RoutineDao
import com.timebasedfitness.app.data.model.Category
import com.timebasedfitness.app.data.model.CategorySelection
import com.timebasedfitness.app.data.model.RoutineEntity
import org.json.JSONArray
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.time.LocalTime
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

import com.timebasedfitness.app.data.model.RoutineStep

@Singleton
class RoutineRepository @Inject constructor(
    private val dao: RoutineDao,
    private val categorySelectionDao: CategorySelectionDao,
    private val database: AppDatabase,
    private val contentRepository: ContentRepository
) {
    fun observe(category: Category): Flow<RoutineContent?> = dao.observe(category).map { entity ->
        entity?.let { contentForToday(it, ZoneId.systemDefault()) }
            ?: contentRepository.getRoutine(category)
    }

    suspend fun save(category: Category, content: RoutineContent) {
        val stepsJson = encodeSteps(content)
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

    suspend fun importPlan(plan: FitnessPlanJson): List<CategorySelection> {
        // Pre-validate all category entries and parse times before starting the transaction.
        val parsedItems = plan.categories.map { item ->
            val category = runCatching { Category.valueOf(item.category) }
                .getOrElse { throw IllegalArgumentException("Unknown category: ${item.category}") }
            val start = item.startTime?.let { raw ->
                runCatching { LocalTime.parse(raw) }.getOrElse { throw IllegalArgumentException("Invalid start time: $raw") }
            }
            val end = item.endTime?.let { raw ->
                runCatching { LocalTime.parse(raw) }.getOrElse { throw IllegalArgumentException("Invalid end time: $raw") }
            }
            Triple(category, item, start to end)
        }

        return database.withTransaction {
            parsedItems.forEach { (category, item, _) ->
                val stepsJson = encodeSteps(RoutineContent(item.title, item.steps, item.days, item.goal))
                dao.upsert(
                    RoutineEntity(
                        category = category,
                        title = item.title.trim(),
                        stepsJson = stepsJson
                    )
                )
            }

            val existing = categorySelectionDao.getAllCategorySelectionsSync().ifEmpty {
                CategoryRepository.getDefaultSelections()
            }
            val byCategory = parsedItems.associateBy { it.first }
            val updatedSelections = existing.map { current ->
                val imported = byCategory[current.category]
                if (imported != null) {
                    val (start, end) = imported.third
                    current.copy(
                        isEnabled = true,
                        startTime = start ?: current.startTime,
                        endTime = end ?: current.endTime
                    )
                } else {
                    current.copy(isEnabled = false)
                }
            }
            categorySelectionDao.insertAll(updatedSelections)
            updatedSelections
        }
    }

    suspend fun exportPlan(): String {
        val overrides = dao.getAll().associateBy { it.category }
        val schedules = categorySelectionDao.getAllCategorySelectionsSync().associateBy { it.category }
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val categories = Category.entries.mapNotNull { category ->
            val schedule = schedules[category]
            val startTime = schedule?.startTime?.format(formatter)
            val endTime = schedule?.endTime?.format(formatter)
            overrides[category]?.let { entity ->
                val envelope = decodeEnvelope(entity.stepsJson)
                PlanCategoryJson(
                    category = category.name,
                    title = entity.title,
                    goal = envelope.goal,
                    startTime = startTime,
                    endTime = endTime,
                    steps = envelope.steps,
                    days = envelope.days
                )
            } ?: contentRepository.getRoutine(category)?.let { content ->
                PlanCategoryJson(
                    category = category.name,
                    title = content.title,
                    goal = content.goal,
                    startTime = startTime,
                    endTime = endTime,
                    steps = content.steps,
                    days = content.stepsByDay
                )
            }
        }
        return PlanJsonCodec.encode(FitnessPlanJson(categories = categories))
    }

    internal fun contentForToday(entity: RoutineEntity, zone: ZoneId = ZoneId.systemDefault()): RoutineContent {
        val envelope = decodeEnvelope(entity.stepsJson)
        val todayKey = java.time.LocalDate.now(zone).dayOfWeek.name
        // Case-insensitive match guards against user-editable weekday labels
        // (e.g. "Monday" vs "MONDAY") that came from older import formats.
        val steps = envelope.days[todayKey]
            ?: envelope.days.entries.firstOrNull { it.key.equals(todayKey, ignoreCase = true) }?.value
            ?: envelope.steps
        return RoutineContent(entity.title, steps, envelope.days, envelope.goal)
    }

    private fun encodeSteps(content: RoutineContent): String {
        val envelope = StepsEnvelope(content.steps, content.stepsByDay, content.goal)
        return json.encodeToString(envelope)
    }

    private fun decodeEnvelope(jsonStr: String): StepsEnvelope = runCatching {
        json.decodeFromString<StepsEnvelope>(jsonStr)
    }.getOrElse {
        // Fallback for legacy string lists: ["Step 1", "Step 2"]
        val legacySteps = runCatching {
            json.decodeFromString<List<String>>(jsonStr).map { RoutineStep.fromText(it) }
        }.getOrDefault(emptyList())
        StepsEnvelope(legacySteps, emptyMap(), null)
    }

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    @kotlinx.serialization.Serializable
    private data class StepsEnvelope(
        val steps: List<RoutineStep> = emptyList(),
        val days: Map<String, List<RoutineStep>> = emptyMap(),
        val goal: String? = null
    )
}
