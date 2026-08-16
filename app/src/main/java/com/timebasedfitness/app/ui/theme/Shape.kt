package com.timebasedfitness.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Quiet Ritual Rounded Scale: sm=4dp, DEFAULT=8dp, md=12dp, lg=16dp, xl=24dp, full=9999dp
val AppShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

object AppSpacing {
    val marginPage = 24.dp
    val stackGap = 16.dp
    val cardPadding = 24.dp
    val listItemHeight = 48.dp
    val spaceSm = 8.dp
    val spaceMd = 16.dp
    val spaceLg = 24.dp
    val spaceXl = 32.dp
}
