package com.timebasedfitness.app.data.content

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlanJsonCodecTest {
    @Test
    fun roundTrip_preservesPlan() {
        val plan = FitnessPlanJson(
            title = "Home plan",
            categories = listOf(
                PlanCategoryJson("WORKOUT", "Training", "17:00", "19:00", listOf("Warm up", "Squats"))
            )
        )

        val decoded = PlanJsonCodec.decode(PlanJsonCodec.encode(plan))

        assertTrue(decoded.isSuccess)
        assertEquals(plan, decoded.getOrThrow())
    }

    @Test
    fun decode_rejectsUnknownCategory() {
        val result = PlanJsonCodec.decode(
            """{"schemaVersion":1,"title":"Bad","categories":[{"category":"UNKNOWN","title":"Routine","steps":["Step"]}]}"""
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun decode_rejectsInvalidTimeFormat() {
        val badStart = PlanJsonCodec.decode(
            """{"schemaVersion":1,"title":"Bad","categories":[{"category":"WORKOUT","title":"Routine","startTime":"25:00","endTime":"18:00","steps":["Step"]}]}"""
        )
        assertTrue(badStart.isFailure)

        val badEnd = PlanJsonCodec.decode(
            """{"schemaVersion":1,"title":"Bad","categories":[{"category":"WORKOUT","title":"Routine","startTime":"07:00","endTime":"7:00","steps":["Step"]}]}"""
        )
        assertTrue(badEnd.isFailure)
    }

    @Test
    fun decode_allowsNullTimes() {
        val planWithoutTimes = """{"schemaVersion":1,"title":"Flexible","categories":[{"category":"MORNING","title":"Morning Flow","steps":["Stretch"]}]}"""
        val result = PlanJsonCodec.decode(planWithoutTimes)

        assertTrue(result.isSuccess)
        val decoded = result.getOrThrow()
        assertEquals(null, decoded.categories[0].startTime)
        assertEquals(null, decoded.categories[0].endTime)
    }
}
