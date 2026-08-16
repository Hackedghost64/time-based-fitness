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
                PlanCategoryJson(
                    category = "WORKOUT",
                    title = "Training",
                    goal = "Get strong",
                    startTime = "17:00",
                    endTime = "19:00",
                    steps = listOf(
                        com.timebasedfitness.app.data.model.RoutineStep("Warm up", 0, "Tasks"),
                        com.timebasedfitness.app.data.model.RoutineStep("Squats", 60, "Tasks")
                    )
                )
            )
        )

        val decoded = PlanJsonCodec.decode(PlanJsonCodec.encode(plan))

        assertTrue(decoded.isSuccess)
        assertEquals(plan, decoded.getOrThrow())
    }

    @Test
    fun decode_acceptsFlexibleTimeFormats() {
        val json = """{"schemaVersion":1,"title":"Flexible Times","categories":[{"category":"WORKOUT","title":"Gym","startTime":"6:00 AM","endTime":"7:30 pm","steps":["Lift"]}]}"""
        val result = PlanJsonCodec.decode(json)

        assertTrue(result.isSuccess)
        val decoded = result.getOrThrow()
        assertEquals("06:00", decoded.categories[0].startTime)
        assertEquals("19:30", decoded.categories[0].endTime)
    }

    @Test
    fun decode_mapsCategoryAliases() {
        val json = """{"schemaVersion":1,"title":"AI Plan","categories":[{"category":"Breakfast Routine","title":"Healthy Meal","steps":["Oatmeal"]},{"category":"Gym Training","title":"Strength","steps":["Bench"]}]}"""
        val result = PlanJsonCodec.decode(json)

        assertTrue(result.isSuccess)
        val decoded = result.getOrThrow()
        assertEquals("MEALS", decoded.categories[0].category)
        assertEquals("WORKOUT", decoded.categories[1].category)
    }

    @Test
    fun decode_normalizesWeekdayAbbreviations() {
        val json = """{"schemaVersion":1,"title":"Split","categories":[{"category":"WORKOUT","title":"Gym","steps":["Default"],"days":{"Mon":["Chest"],"Wed":["Back"],"Fri":["Legs"]}}]}"""
        val result = PlanJsonCodec.decode(json)

        assertTrue(result.isSuccess)
        val decoded = result.getOrThrow()
        val days = decoded.categories[0].days
        assertTrue(days.containsKey("MONDAY"))
        assertTrue(days.containsKey("WEDNESDAY"))
        assertTrue(days.containsKey("FRIDAY"))
    }

    @Test
    fun decode_rejectsPlanWithNoValidCategories() {
        val result = PlanJsonCodec.decode(
            """{"schemaVersion":1,"title":"Bad","categories":[{"category":"XYZZY_RANDOM","title":"Routine","steps":["Step"]}]}"""
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun decode_parsesStructuredStepsWithTimersAndGoals() {
        val json = """
        {
          "schemaVersion": 1,
          "title": "Rich Plan",
          "categories": [
            {
              "category": "WORKOUT",
              "title": "Strength & Core",
              "goal": "Build core stability",
              "steps": [
                {"text": "Plank", "durationSeconds": 60, "group": "Core"},
                "Pushups 3x10",
                "Deep Breathing (120s)"
              ]
            }
          ]
        }
        """.trimIndent()

        val result = PlanJsonCodec.decode(json)
        assertTrue(result.isSuccess)
        val decoded = result.getOrThrow()
        val cat = decoded.categories[0]
        assertEquals("Build core stability", cat.goal)
        assertEquals(3, cat.steps.size)
        assertEquals("Plank", cat.steps[0].text)
        assertEquals(60, cat.steps[0].durationSeconds)
        assertEquals("Core", cat.steps[0].group)

        assertEquals("Pushups 3x10", cat.steps[1].text)
        assertEquals(0, cat.steps[1].durationSeconds)

        assertEquals("Deep Breathing (120s)", cat.steps[2].text)
        assertEquals(120, cat.steps[2].durationSeconds)
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
