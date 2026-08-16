package com.timebasedfitness.app.data.content

import org.junit.Assert.assertTrue
import org.junit.Test

class AiPromptBuilderTest {
    @Test
    fun promptContainsUserDetailsAndSchemaInstructions() {
        val prompt = AiPromptBuilder.build(
            AiPlanRequest("Claude", "Build strength", "Beginner", "Dumbbells", "3 days", "None", "Short sessions")
        )

        assertTrue(prompt.contains("Build strength"))
        assertTrue(prompt.contains("Claude"))
        assertTrue(prompt.contains("schemaVersion"))
        assertTrue(prompt.contains("ONLY valid JSON"))
    }
}
