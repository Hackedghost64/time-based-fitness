package com.timebasedfitness.app.data.model

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutineStepTest {

    @Test
    fun extractDurationSeconds_detectsVariousFormats() {
        assertEquals(60, RoutineStep.extractDurationSeconds("Plank (60s)"))
        assertEquals(30, RoutineStep.extractDurationSeconds("Pushups 30 sec"))
        assertEquals(120, RoutineStep.extractDurationSeconds("Rest 2 min"))
        assertEquals(300, RoutineStep.extractDurationSeconds("5 mins meditation"))
        assertEquals(0, RoutineStep.extractDurationSeconds("Just regular pushups"))
    }

    @Test
    fun serializer_roundTripsPlainString() {
        val step = RoutineStep(text = "Drink water", durationSeconds = 0, group = "Tasks")
        val json = Json.encodeToString(step)
        assertEquals("\"Drink water\"", json)

        val decoded = Json.decodeFromString<RoutineStep>(json)
        assertEquals("Drink water", decoded.text)
        assertEquals(0, decoded.durationSeconds)
        assertFalse(decoded.isTimer)
    }

    @Test
    fun serializer_roundTripsRichObject() {
        val step = RoutineStep(text = "Plank", durationSeconds = 60, group = "Core")
        val json = Json.encodeToString(step)
        val decoded = Json.decodeFromString<RoutineStep>(json)

        assertEquals("Plank", decoded.text)
        assertEquals(60, decoded.durationSeconds)
        assertEquals("Core", decoded.group)
        assertTrue(decoded.isTimer)
    }
}
