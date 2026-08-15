package com.timebasedfitness.app.data.model

enum class Category {
    MORNING,
    MEALS,
    WORKOUT,
    EVENING;

    val displayName: String
        get() = when (this) {
            MORNING -> "Morning"
            MEALS -> "Meals"
            WORKOUT -> "Workout"
            EVENING -> "Evening"
        }
}
