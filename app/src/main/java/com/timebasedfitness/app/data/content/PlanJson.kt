package com.timebasedfitness.app.data.content

import com.timebasedfitness.app.data.model.Category
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class FitnessPlanJson(
    val schemaVersion: Int = 1,
    val title: String = "Imported Fitness Plan",
    val categories: List<PlanCategoryJson>
)

@Serializable
data class PlanCategoryJson(
    val category: String,
    val title: String,
    val startTime: String? = null,
    val endTime: String? = null,
    val steps: List<String>
)

object PlanJsonCodec {
    private val json = Json { ignoreUnknownKeys = false; prettyPrint = true }

    fun encode(plan: FitnessPlanJson): String = json.encodeToString(plan)

    fun decode(raw: String): Result<FitnessPlanJson> = runCatching {
        json.decodeFromString<FitnessPlanJson>(raw).also(::validate)
    }

    private fun validate(plan: FitnessPlanJson) {
        require(plan.schemaVersion == 1) { "Unsupported schema version: ${plan.schemaVersion}" }
        require(plan.categories.isNotEmpty()) { "The plan must contain at least one category." }
        require(plan.categories.size <= Category.entries.size) { "The plan contains too many categories." }
        require(plan.title.isNotBlank() && plan.title.length <= 120) { "The plan title is invalid." }

        plan.categories.forEach { item ->
            require(item.category in Category.entries.map { it.name }) { "Unknown category: ${item.category}" }
            require(item.title.isNotBlank() && item.title.length <= 120) { "A routine title is invalid." }
            require(item.steps.isNotEmpty() && item.steps.size <= 100) { "Each routine needs 1–100 steps." }
            require(item.steps.all { it.isNotBlank() && it.length <= 500 }) { "A routine step is invalid." }
            item.startTime?.let { require(it.matches(Regex("^([01]\\d|2[0-3]):[0-5]\\d$")) { "Invalid start time: $it" } }
            item.endTime?.let { require(it.matches(Regex("^([01]\\d|2[0-3]):[0-5]\\d$")) { "Invalid end time: $it" } }
        }
        require(plan.categories.map { it.category }.toSet().size == plan.categories.size) { "Categories must be unique." }
    }
}
