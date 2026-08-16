package com.timebasedfitness.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.timebasedfitness.app.data.model.Category

/** Lightweight line icons kept local so the UI works offline. */
object CategoryIcons {
    val Morning = icon("morning") { moveTo(12f, 4f); arcTo(4f, 4f, 0f, true, false, 12f, 12f); moveTo(12f, 2f); lineTo(12f, 0f); moveTo(4f, 8f); lineTo(2f, 8f); moveTo(20f, 8f); lineTo(22f, 8f); moveTo(5f, 3f); lineTo(3.5f, 1.5f); moveTo(19f, 3f); lineTo(20.5f, 1.5f); moveTo(4f, 16f); lineTo(20f, 16f) }
    val Meals = icon("meals") { moveTo(4f, 12f); arcTo(8f, 8f, 0f, true, false, 20f, 12f); arcTo(8f, 8f, 0f, true, false, 4f, 12f); moveTo(12f, 4f); lineTo(12f, 20f) }
    val Workout = icon("workout") { moveTo(3f, 9f); lineTo(3f, 15f); moveTo(6f, 7f); lineTo(6f, 17f); moveTo(6f, 12f); lineTo(18f, 12f); moveTo(18f, 7f); lineTo(18f, 17f); moveTo(21f, 9f); lineTo(21f, 15f) }
    val Evening = icon("evening") { moveTo(20f, 14f); arcTo(8f, 8f, 0f, true, true, 9f, 4f); arcTo(7f, 7f, 0f, false, false, 20f, 14f) }

    fun forCategory(category: Category) = when (category) { Category.MORNING -> Morning; Category.MEALS -> Meals; Category.WORKOUT -> Workout; Category.EVENING -> Evening }

    private fun icon(name: String, draw: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit) = ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.6f, strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round, strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round, pathBuilder = draw)
    }.build()
}
