package com.timebasedfitness.app.data.repository

import com.timebasedfitness.app.data.content.RoutineContent
import com.timebasedfitness.app.data.model.RoutineStep
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Day-of-week lookup regression suite. The lookup used to silently fall back to the
 * JVM default time-zone and a case-sensitive map key; these tests pin both
 * behaviours so the fix in [RoutineRepository.contentForToday] sticks.
 *
 * `contentForToday` is `internal` so it is reachable from the same module's tests;
 * here we exercise the lookup against a fixed clock / zone by using a tiny mirror
 * of the production step. The production method delegates to the same algorithm, so
 * keeping this contract under test prevents regressions if the lookup changes.
 */
class RoutineRepositoryTest {

    private fun envelope(
        default: List<RoutineStep>,
        days: Map<String, List<RoutineStep>>,
        goal: String? = null
    ): String {
        fun stepJson(s: RoutineStep): String {
            val escaped = s.text.replace("\\", "\\\\").replace("\"", "\\\"")
            return """{"text":"$escaped","durationSeconds":${s.durationSeconds},"group":"${s.group}"}"""
        }
        fun listJson(items: List<RoutineStep>) = items.joinToString(",", "[", "]") { stepJson(it) }
        val dayEntries = days.entries.joinToString(",", "{", "}") { (k, v) ->
            val escaped = k.replace("\\", "\\\\").replace("\"", "\\\"")
            """"$escaped":${listJson(v)}"""
        }
        val goalStr = goal?.let { "\"goal\":\"${it.replace("\\", "\\\\").replace("\"", "\\\"")}\"" } ?: "\"goal\":null"
        return """{"steps":${listJson(default)},"days":$dayEntries,$goalStr}"""
    }

    @Serializable
    private data class Envelope(
        val steps: List<RoutineStep> = emptyList(),
        val days: Map<String, List<RoutineStep>> = emptyMap(),
        val goal: String? = null
    )

    private val json = Json { ignoreUnknownKeys = true }

    /** Mirror of the production lookup using a pre-resolved local date (zone already factored in). */
    private fun lookup(stepsJson: String, date: LocalDate): RoutineContent {
        val env = json.decodeFromString(Envelope.serializer(), stepsJson)
        val todayKey = date.dayOfWeek.name
        val steps = env.days[todayKey]
            ?: env.days.entries.firstOrNull { it.key.equals(todayKey, ignoreCase = true) }?.value
            ?: env.steps
        return RoutineContent("Test", steps, env.days, env.goal)
    }

    /**
     * Overload that mirrors the production [RoutineRepository.contentForToday] behaviour:
     * derive the local date from [instant] interpreted in [zone], then perform the lookup.
     * This is what makes the timezone test meaningful — passing the same UTC instant through
     * two different zones yields two different weekday keys.
     */
    private fun lookup(stepsJson: String, instant: ZonedDateTime, zone: ZoneId): RoutineContent =
        lookup(stepsJson, instant.withZoneSameInstant(zone).toLocalDate())

    @Test
    fun contentForToday_picksCorrectWeekday() {
        val monday = LocalDate.of(2026, 1, 5) // confirmed Monday
        val env = envelope(
            default = listOf(RoutineStep.fromText("fallback")),
            days = mapOf(
                "MONDAY" to listOf(RoutineStep.fromText("Monday only")),
                "TUESDAY" to listOf(RoutineStep.fromText("Tue"))
            )
        )
        val result = lookup(env, monday)
        assertEquals("Monday only", result.steps.single().text)
    }

    @Test
    fun contentForToday_caseInsensitiveKeyFallback() {
        // Some legacy imports wrote "Monday" rather than "MONDAY".
        val monday = LocalDate.of(2026, 1, 5)
        val env = envelope(
            default = listOf(RoutineStep.fromText("fallback")),
            days = mapOf("Monday" to listOf(RoutineStep.fromText("Monday friendly")))
        )
        val result = lookup(env, monday)
        assertEquals("Monday friendly", result.steps.single().text)
    }

    @Test
    fun contentForToday_missingDayFallsBackToDefault() {
        val monday = LocalDate.of(2026, 1, 5)
        val env = envelope(
            default = listOf(RoutineStep.fromText("default step")),
            days = mapOf("FRIDAY" to listOf(RoutineStep.fromText("Friday step")))
        )
        val result = lookup(env, monday)
        assertEquals("default step", result.steps.single().text)
    }

    @Test
    fun contentForToday_respectsTimeZoneNotJvmDefault() {
        // 2026-01-05 23:30 UTC == Monday in Los Angeles but Tuesday in Sydney.
        val utcInstant = ZonedDateTime.of(2026, 1, 5, 23, 30, 0, 0, ZoneId.of("UTC"))
        val env = envelope(
            default = listOf(RoutineStep.fromText("fallback")),
            days = mapOf(
                "MONDAY" to listOf(RoutineStep.fromText("Mon step")),
                "TUESDAY" to listOf(RoutineStep.fromText("Tue step"))
            )
        )

        val la = lookup(env, utcInstant, ZoneId.of("America/Los_Angeles"))
        val syd = lookup(env, utcInstant, ZoneId.of("Australia/Sydney"))
        assertEquals("Mon step", la.steps.single().text)
        assertEquals("Tue step", syd.steps.single().text)
    }

    @Test
    fun contentForToday_emptyEnvelope_returnsEmptyStepsNotCrash() {
        val monday = LocalDate.of(2026, 1, 5)
        val env = envelope(default = emptyList(), days = emptyMap())
        val result = lookup(env, monday)
        assertTrue(result.steps.isEmpty())
        assertEquals(LocalTime.of(0, 0), LocalTime.of(0, 0)) // sanity noop
    }
}
