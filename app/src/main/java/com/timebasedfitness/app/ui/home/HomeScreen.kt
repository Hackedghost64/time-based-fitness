package com.timebasedfitness.app.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timebasedfitness.app.data.content.RoutineContent
import com.timebasedfitness.app.data.model.Category
import com.timebasedfitness.app.data.model.CategorySelection
import com.timebasedfitness.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(viewModel: HomeViewModel, onRoutineClick: (Category) -> Unit, onSettingsClick: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val timeFormat = DateTimeFormatter.ofPattern("h:mm a")
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 32.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column {
                    Text("Today", style = MaterialTheme.typography.headlineLarge)
                    Text(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d")), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onSettingsClick) { Icon(Icons.Outlined.Settings, "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            Spacer(Modifier.height(32.dp))
            AnimatedContent(state, transitionSpec = { (fadeIn(tween(Motion.CardChangeDuration)) + slideInVertically(tween(Motion.CardChangeDuration)) { it / 8 }).togetherWith(fadeOut(tween(Motion.FadeDuration)) + slideOutVertically(tween(Motion.FadeDuration)) { -it / 8 }) }, label = "home-content") { current ->
                when (current) {
                    HomeUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = MaterialTheme.colorScheme.outline) }
                    is HomeUiState.Content -> if (current.activeCategories.isNotEmpty()) {
                        val selection = current.activeCategories.first()
                        val routine = current.routineContentMap[selection.category]
                        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            HomeCard(selection, routine, timeFormat) { onRoutineClick(selection.category) }
                            if (current.activeCategories.size > 1) Text("+${current.activeCategories.size - 1} more ready when you are", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            StreakRow(current.streakCount)
                        }
                    } else EmptyState(current, timeFormat)
                }
            }
        }
    }
}

@Composable
private fun HomeCard(
    selection: CategorySelection,
    routine: RoutineContent?,
    format: DateTimeFormatter,
    onClick: () -> Unit
) {
    val accent = CategoryTheme.getAccentColor(selection.category)
    val timePeriodText = "${selection.category.displayName} • ${selection.startTime.format(format).lowercase()} – ${selection.endTime.format(format).lowercase()}"

    Surface(
        modifier = Modifier.fillMaxWidth().semantics(mergeDescendants = true) {}.shadow(2.dp, RoundedCornerShape(28.dp), ambientColor = accent.copy(alpha = .06f)).clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = accent.copy(alpha = .1f),
                    border = BorderStroke(1.dp, accent.copy(alpha = .25f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(rememberVectorPainter(CategoryIcons.forCategory(selection.category)), null, tint = accent, modifier = Modifier.size(24.dp))
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = accent.copy(alpha = .12f)
                ) {
                    Text(
                        text = "ACTIVE NOW",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = accent,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(text = timePeriodText, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(routine?.title ?: selection.category.displayName, style = MaterialTheme.typography.headlineLarge)

            routine?.goal?.let { goal ->
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Goal: $goal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = accent,
                    fontWeight = FontWeight.Medium
                )
            }

            // Grouped Task Preview
            if (routine != null && routine.steps.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(Modifier.height(12.dp))

                val previewSteps = routine.steps.take(3)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    previewSteps.forEach { step ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(6.dp).clip(CircleShape).background(accent)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = step.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            if (step.isTimer) {
                                Text(
                                    text = "⏱ ${step.durationSeconds}s",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = accent
                                )
                            }
                        }
                    }
                    if (routine.steps.size > 3) {
                        Text(
                            text = "+ ${routine.steps.size - 3} more tasks",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Begin Routine  →", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = accent)
        }
    }
}

@Composable private fun StreakRow(streak: Int) { if (streak > 0) Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outlineVariant)); Spacer(Modifier.width(8.dp)); Text("$streak day streak", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }

@Composable private fun EmptyState(content: HomeUiState.Content, format: DateTimeFormatter) {
    Box(Modifier.fillMaxSize(), Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(56.dp).clip(CircleShape).border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape), Alignment.Center) { Box(Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outlineVariant)) }
        Spacer(Modifier.height(24.dp)); Text("Nothing right now", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp)); Text(content.nextUpcoming?.let { "Next: ${it.category.displayName} at ${it.startTime.format(format).lowercase()}" } ?: "Set up category windows in Settings", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    } }
}

