package com.timebasedfitness.app.data.content

import com.timebasedfitness.app.data.model.Category
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.format.DateTimeFormatter
import java.util.Locale

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
    val steps: List<String> = emptyList(),
    /** Optional ISO weekday routines, for example {"MONDAY": ["..." ]}. */
    val days: Map<String, List<String>> = emptyMap()
)

object PlanJsonCodec {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    fun encode(plan: FitnessPlanJson): String = json.encodeToString(plan)

    fun decode(raw: String): Result<FitnessPlanJson> = runCatching {
        val decoded = json.decodeFromString<FitnessPlanJson>(raw)
        normalizeAndValidate(decoded)
    }

    private fun normalizeAndValidate(plan: FitnessPlanJson): FitnessPlanJson {
        require(plan.schemaVersion >= 1) { "Unsupported schema version: ${plan.schemaVersion}" }
        require(plan.categories.isNotEmpty()) { "The plan must contain at least one category." }
        require(plan.title.isNotBlank() && plan.title.length <= 120) { "The plan title is invalid." }

        val normalizedCategories = mutableListOf<PlanCategoryJson>()
        val seenCategories = mutableSetOf<String>()

        for (item in plan.categories) {
            val mappedCategory = mapCategory(item.category) ?: continue
            if (seenCategories.contains(mappedCategory.name)) continue
            seenCategories.add(mappedCategory.name)

            require(item.title.isNotBlank() && item.title.length <= 120) { "A routine title is invalid." }
            require(item.steps.isNotEmpty() || item.days.values.any { it.isNotEmpty() }) { "Each routine needs 1–100 steps." }
            require(item.steps.size <= 100 && item.steps.all { it.isNotBlank() && it.length <= 500 }) { "A routine step is invalid." }

            val normalizedDays = mutableMapOf<String, List<String>>()
            for ((dayKey, daySteps) in item.days) {
                val mappedDay = mapWeekday(dayKey) ?: continue
                require(daySteps.isNotEmpty() && daySteps.size <= 100 && daySteps.all { step -> step.isNotBlank() && step.length <= 500 }) {
                    "A daily routine step for $mappedDay is invalid."
                }
                normalizedDays[mappedDay] = daySteps
            }

            val normalizedStart = item.startTime?.let { raw ->
                FlexibleTimeParser.parse(raw)?.format(DateTimeFormatter.ofPattern("HH:mm"))
                    ?: error("Invalid start time format: $raw")
            }
            val normalizedEnd = item.endTime?.let { raw ->
                FlexibleTimeParser.parse(raw)?.format(DateTimeFormatter.ofPattern("HH:mm"))
                    ?: error("Invalid end time format: $raw")
            }

            normalizedCategories.add(
                item.copy(
                    category = mappedCategory.name,
                    title = item.title.trim(),
                    startTime = normalizedStart,
                    endTime = normalizedEnd,
                    steps = item.steps.map(String::trim).filter(String::isNotEmpty),
                    days = normalizedDays
                )
            )
        }

        require(normalizedCategories.isNotEmpty()) {
            "No recognized categories found in the plan (must match Morning, Meals, Workout, or Evening)."
        }

        return plan.copy(
            title = plan.title.trim(),
            categories = normalizedCategories
        )
    }

    fun mapCategory(raw: String): Category? {
        val clean = raw.trim().uppercase(Locale.US).replace(" ", "_").replace("-", "_")
        return runCatching { Category.valueOf(clean) }.getOrNull() ?: when {
            clean.contains("BREAKFAST") || clean.contains("MEAL") || clean.contains("LUNCH") || clean.contains("DINNER") || clean.contains("FOOD") || clean.contains("NUTRITION") || clean.contains("DIET") || clean.contains("SNACK") -> Category.MEALS
            clean.contains("MORN") || clean.contains("WAKE") || clean.contains("SUNRISE") || clean.contains("DAWN") -> Category.MORNING
            clean.contains("WORKOUT") || clean.contains("EXERCISE") || clean.contains("GYM") || clean.contains("FITNESS") || clean.contains("TRAIN") || clean.contains("CARDIO") || clean.contains("LIFT") || clean.contains("STRETCH") || clean.contains("RUN") -> Category.WORKOUT
            clean.contains("EVEN") || clean.contains("NIGHT") || clean.contains("SLEEP") || clean.contains("BED") || clean.contains("WIND") || clean.contains("MEDITATION") -> Category.EVENING
            else -> null
        }
    }

    private fun mapWeekday(raw: String): String? {
        val clean = raw.trim().uppercase(Locale.US)
        return when {
            clean.startsWith("MON") -> "MONDAY"
            clean.startsWith("TUE") -> "TUESDAY"
            clean.startsWith("WED") -> "WEDNESDAY"
            clean.startsWith("THU") -> "THURSDAY"
            clean.startsWith("FRI") -> "FRIDAY"
            clean.startsWith("SAT") -> "SATURDAY"
            clean.startsWith("SUN") -> "SUNDAY"
            else -> null
        }
    }
}

object FlexibleTimeParser {
    private val patterns = listOf(
        DateTimeFormatter.ofPattern("H:mm"),
        DateTimeFormatter.ofPattern("HH:mm"),
        DateTimeFormatter.ofPattern("h:mm a", Locale.US),
        DateTimeFormatter.ofPattern("hh:mm a", Locale.US),
        DateTimeFormatter.ofPattern("h:mma", Locale.US),
        DateTimeFormatter.ofPattern("hh:mma", Locale.US),
        DateTimeFormatter.ofPattern("h a", Locale.US),
        DateTimeFormatter.ofPattern("ha", Locale.US),
        DateTimeFormatter.ofPattern("H", Locale.US),
        DateTimeFormatter.ofPattern("HH", Locale.US)
    )

    fun parse(raw: String): java.time.LocalTime? {
        val trimmed = raw.trim()
        for (formatter in patterns) {
            try {
                return java.time.LocalTime.parse(trimmed, formatter)
            } catch (_: Exception) {}
        }
        val upper = trimmed.uppercase(Locale.US)
        for (formatter in patterns) {
            try {
                return java.time.LocalTime.parse(upper, formatter)
            } catch (_: Exception) {}
        }
        return null
    }
}
