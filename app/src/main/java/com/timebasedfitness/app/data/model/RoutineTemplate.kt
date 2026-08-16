package com.timebasedfitness.app.data.model

import com.timebasedfitness.app.data.content.RoutineContent
import javax.inject.Inject
import javax.inject.Singleton

data class RoutineTemplate(
    val id: String,
    val name: String,
    val description: String,
    val targetCategory: Category,
    val content: RoutineContent
)

@Singleton
class TemplateRepository @Inject constructor() {

    fun getDefaultTemplates(): List<RoutineTemplate> = listOf(
        RoutineTemplate(
            id = "push_day",
            name = "Push Day (Chest, Shoulders, Triceps)",
            description = "Upper body pushing power and strength",
            targetCategory = Category.WORKOUT,
            content = RoutineContent(
                title = "Push Day",
                goal = "Chest, Shoulders & Triceps Strength",
                steps = listOf(
                    RoutineStep("Dynamic Warm-up & Arm Circles (120s)", 120, "Warm-up"),
                    RoutineStep("Pushups (3 sets to failure)", 0, "Chest"),
                    RoutineStep("Rest between sets (60s)", 60, "Rest"),
                    RoutineStep("Pike Pushups or Shoulder Press (3x10)", 0, "Shoulders"),
                    RoutineStep("Tricep Dips (3x12)", 0, "Arms"),
                    RoutineStep("Cool-down stretch (180s)", 180, "Cool-down")
                )
            )
        ),
        RoutineTemplate(
            id = "pull_day",
            name = "Pull Day (Back, Biceps, Core)",
            description = "Posterior chain pulling and core stability",
            targetCategory = Category.WORKOUT,
            content = RoutineContent(
                title = "Pull Day",
                goal = "Back, Biceps & Core Strength",
                steps = listOf(
                    RoutineStep("Dynamic Warm-up (120s)", 120, "Warm-up"),
                    RoutineStep("Pull-ups or Inverted Rows (3x8)", 0, "Back"),
                    RoutineStep("Rest between sets (60s)", 60, "Rest"),
                    RoutineStep("Bicep Curls or Towel Curls (3x12)", 0, "Arms"),
                    RoutineStep("Hollow Body Hold (45s)", 45, "Core"),
                    RoutineStep("Cool-down stretch (180s)", 180, "Cool-down")
                )
            )
        ),
        RoutineTemplate(
            id = "mobility_reset",
            name = "Full Body Mobility & Stretch",
            description = "Deep hip openers, spine decompression, and joint health",
            targetCategory = Category.MORNING,
            content = RoutineContent(
                title = "Morning Mobility Flow",
                goal = "Joint mobility and full body awakening",
                steps = listOf(
                    RoutineStep("Hydrate with 500ml water", 0, "Hydration"),
                    RoutineStep("Cat-Cow Spine Flow (60s)", 60, "Spine"),
                    RoutineStep("World's Greatest Stretch (90s)", 90, "Hips"),
                    RoutineStep("Deep Squat Hold (60s)", 60, "Mobility"),
                    RoutineStep("Diaphragmatic Breathing (120s)", 120, "Mindfulness")
                )
            )
        ),
        RoutineTemplate(
            id = "bedtime_wind_down",
            name = "Bedtime Reset & Deep Sleep",
            description = "Gentle parasympathetic nervous system down-regulation",
            targetCategory = Category.EVENING,
            content = RoutineContent(
                title = "Evening Wind Down",
                goal = "Parasympathetic activation and deep sleep prep",
                steps = listOf(
                    RoutineStep("Turn off screens / blue light", 0, "Environment"),
                    RoutineStep("Legs Up the Wall Pose (300s)", 300, "Relaxation"),
                    RoutineStep("Hamstring & Glute Stretch (120s)", 120, "Mobility"),
                    RoutineStep("Box Breathing 4-4-4-4 (180s)", 180, "Breathing"),
                    RoutineStep("Review 3 wins of the day", 0, "Mindset")
                )
            )
        )
    )

    fun getTemplatesForCategory(category: Category): List<RoutineTemplate> =
        getDefaultTemplates().filter { it.targetCategory == category }
}
