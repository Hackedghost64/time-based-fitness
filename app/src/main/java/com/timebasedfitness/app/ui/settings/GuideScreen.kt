package com.timebasedfitness.app.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GuideScreen(onBack: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "← Back",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onBack() }
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = "How Onset works",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "A quick reference for using the app.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(28.dp))

            GuideSection(
                emoji = "⏰",
                title = "Time Windows",
                color = MaterialTheme.colorScheme.primary,
                items = listOf(
                    "Your day is split into four windows: Morning, Meals, Workout, and Evening.",
                    "Open Onset during a window and the matching routine appears automatically — no searching needed.",
                    "Set start and end times for each window in Settings."
                )
            )

            GuideSection(
                emoji = "▶️",
                title = "Following a Routine",
                color = MaterialTheme.colorScheme.secondary,
                items = listOf(
                    "Tap the routine card on the home screen to open it.",
                    "Each step shows a recommended duration with an auto-running countdown timer.",
                    "Tap 'Done' to advance — or let the timer auto-skip to the next step.",
                    "Completing all steps marks your routine done and builds your streak."
                )
            )

            GuideSection(
                emoji = "⏱",
                title = "Rest Timer",
                color = MaterialTheme.colorScheme.tertiary,
                items = listOf(
                    "Between steps, tap 30s, 60s, or 90s for a quick inter-set rest countdown.",
                    "The rest banner shows remaining time with +15s and Skip options.",
                    "The rest timer clears automatically when you move on."
                )
            )

            GuideSection(
                emoji = "🔥",
                title = "Streaks",
                color = Color(0xFFE85D04),
                items = listOf(
                    "Complete at least one routine per day to keep your streak alive.",
                    "The streak resets if you miss a full day entirely.",
                    "Tap the streak tile or 📊 to see your best streak, monthly calendar, and category breakdown."
                )
            )

            GuideSection(
                emoji = "🔔",
                title = "Reminders",
                color = MaterialTheme.colorScheme.primary,
                items = listOf(
                    "Onset sends a nudge during your window if you haven't completed your routine yet.",
                    "Reminders stop automatically once the routine is marked done.",
                    "Adjust nudge interval (5–30 min) and daily max in Settings."
                )
            )

            GuideSection(
                emoji = "📦",
                title = "Plans & AI",
                color = MaterialTheme.colorScheme.secondary,
                items = listOf(
                    "Settings → 'Import or share JSON plan' to load a custom routine.",
                    "Use 'Create plan with AI' to generate a prompt for any AI assistant, then paste the response back.",
                    "Onset auto-repairs minor formatting issues on import."
                )
            )

            GuideSection(
                emoji = "🔊",
                title = "Sound & Haptics",
                color = MaterialTheme.colorScheme.tertiary,
                items = listOf(
                    "Audio beeps count down the final 3 seconds of each timer.",
                    "A completion tone plays when a step or rest timer finishes.",
                    "Toggle sound with the 🔊/🔇 button in the routine top bar."
                )
            )

            Spacer(Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("💡  One rule", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Open the app when your window arrives. Follow the routine. That's it. Consistency is the only feature that matters.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun GuideSection(
    emoji: String,
    title: String,
    color: Color,
    items: List<String>
) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(emoji, fontSize = 20.sp)
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(Modifier.height(8.dp))
        items.forEach { item ->
            Row(
                modifier = Modifier.padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodyMedium,
                    color = color.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = 4.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        )
    }
}
