package com.timebasedfitness.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val QuietRitualLightColorScheme = lightColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimary,
    secondary = SecondaryGray,
    onSecondary = OnSecondary,
    background = BackgroundWarm,
    onBackground = TextOnSurface,
    surface = SurfaceContainerLowest,
    onSurface = TextOnSurface,
    surfaceVariant = SurfaceContainerHigh,
    onSurfaceVariant = TextOnSurfaceVariant,
    outline = TextOutline,
    outlineVariant = TextOutlineVariant
)

private val QuietRitualDarkColorScheme = darkColorScheme(
    primary = OnPrimary,
    onPrimary = PrimaryDark,
    secondary = SurfaceContainerHighest,
    onSecondary = TextOnSurface,
    background = Color(0xFF1F1B16),
    onBackground = BackgroundBase,
    surface = Color(0xFF1F1B16),
    onSurface = BackgroundBase,
    surfaceVariant = Color(0xFF313030),
    onSurfaceVariant = SurfaceContainerHighest,
    outline = TextOutlineVariant,
    outlineVariant = TextOutline
)

@Composable
fun TimeBasedFitnessTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) QuietRitualDarkColorScheme else QuietRitualLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
