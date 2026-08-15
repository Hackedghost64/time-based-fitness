package com.timebasedfitness.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.timebasedfitness.app.data.model.Category

object CategoryTheme {
    fun getAccentColor(category: Category): Color {
        return when (category) {
            Category.MORNING -> AccentMorningAmber
            Category.MEALS -> AccentMealsTerracotta
            Category.WORKOUT -> AccentWorkoutTeal
            Category.EVENING -> AccentEveningIndigo
        }
    }
}
