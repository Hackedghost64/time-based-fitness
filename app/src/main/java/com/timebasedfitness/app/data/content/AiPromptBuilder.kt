package com.timebasedfitness.app.data.content

data class AiPlanRequest(
    val provider: String,
    val goal: String,
    val experience: String,
    val equipment: String,
    val availability: String,
    val limitations: String,
    val preferences: String
)

object AiPromptBuilder {
    fun build(request: AiPlanRequest): String = """
        You are creating a safe, practical fitness plan for a mobile app.

        Ask any essential follow-up questions before generating the plan. Do not diagnose injuries or medical conditions. Recommend professional advice when the user's limitations require it.

        User details:
        - Goal: ${request.goal}
        - Experience: ${request.experience}
        - Equipment: ${request.equipment}
        - Availability: ${request.availability}
        - Limitations or injuries: ${request.limitations}
        - Preferences: ${request.preferences}

        After the questions are answered, return ONLY valid JSON matching this exact schema. Do not wrap it in Markdown fences. Use categories only from MORNING, MEALS, WORKOUT, EVENING. Times must use 24-hour HH:mm format. Each category needs at least one step.

        {
          "schemaVersion": 1,
          "title": "Plan title",
          "categories": [
            {
              "category": "WORKOUT",
              "title": "Routine title",
              "startTime": "17:00",
              "endTime": "19:00",
              "steps": ["Step one", "Step two"]
            }
          ]
        }

        Keep the plan realistic, concise, editable, and appropriate for the user's stated experience and equipment. The user will paste your final JSON into the Time-Based Fitness app.
    """.trimIndent()
}
