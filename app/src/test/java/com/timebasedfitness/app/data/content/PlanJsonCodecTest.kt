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
}
