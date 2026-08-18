package com.timebasedfitness.app.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.graphics.vector.path
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
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            HomeCard(selection, routine, timeFormat) { onRoutineClick(selection.category) }
                            if (current.activeCategories.size > 1) Text("+${current.activeCategories.size - 1} more ready when you are", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            StreakTile(current.streakCount, bestStreak(current.completedDates), current.completedDates)
                            QuietWeeklyHistoryRow(current.completedDates)
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

@Composable
private fun StreakTile(streak: Int, bestStreak: Int, completedDates: Set<LocalDate>) {
    if (streak == 0) return

    val accentColor = CategoryTheme.getAccentColor(Category.WORKOUT)
    val isTodayCompleted = completedDates.contains(LocalDate.now())
    val shape = RoundedCornerShape(if (streak >= 7) 20.dp else 16.dp)
    val modifier = if (streak >= 7) {
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.horizontalGradient(listOf(accentColor, accentColor.copy(alpha = 0.6f))))
    } else {
        Modifier.fillMaxWidth()
    }
    val contentColor = if (streak >= 7) MaterialTheme.colorScheme.onPrimary else accentColor
    
    // Milestone detection for animation trigger
    val isMilestone = streak in listOf(7, 30, 60, 90, 100, 365)
    val scale = remember { Animatable(1f) }
    
    LaunchedEffect(streak) {
        if (isMilestone) {
            // Subtle pop animation on milestone
            scale.animateTo(1.15f, tween(150))
            scale.animateTo(1f, tween(150))
        }
    }

    Surface(
        modifier = modifier.then(Modifier.scale(scale.value).animateContentSize()),
        shape = shape,
        color = if (streak >= 7) MaterialTheme.colorScheme.surface.copy(alpha = 0f) else accentColor.copy(alpha = 0.12f),
        border = if (streak >= 7) null else BorderStroke(1.dp, accentColor.copy(alpha = 0.28f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = if (streak >= 7) 16.dp else 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(StreakFlame, contentDescription = null, tint = contentColor, modifier = Modifier.size(if (streak >= 7) 30.dp else 22.dp))
            if (streak >= 7) {
                Column {
                    Text("$streak", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = contentColor)
                    Text("Best: $bestStreak${if (isTodayCompleted) " · Today ✓" else ""}", style = MaterialTheme.typography.labelSmall, color = contentColor)
                }
            } else {
                Text("$streak", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = contentColor)
                Text("day streak", style = MaterialTheme.typography.labelSmall, color = contentColor)
            }
        }
    }
}

@Composable
private fun QuietWeeklyHistoryRow(completedDates: Set<LocalDate>) {
    val today = LocalDate.now()
    val accentColor = CategoryTheme.getAccentColor(Category.WORKOUT)
    val daysOfWeek = (6 downTo 0).map { offset -> today.minusDays(offset.toLong()) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            daysOfWeek.forEach { date ->
                val isCompleted = completedDates.contains(date)
                val isToday = date == today
                val dayLabel = date.dayOfWeek.name.take(2)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = dayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (isCompleted && isToday) accentColor
                                else if (isCompleted) MaterialTheme.colorScheme.primary
                                else if (isToday) MaterialTheme.colorScheme.outline
                                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                    )
                }
            }
        }
    }
}

@Composable private fun EmptyState(content: HomeUiState.Content, format: DateTimeFormatter) {
    Box(Modifier.fillMaxSize(), Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(56.dp).clip(CircleShape).border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape), Alignment.Center) { Box(Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outlineVariant)) }
        Spacer(Modifier.height(24.dp)); Text("Nothing right now", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp)); Text(content.nextUpcoming?.let { "Next: ${it.category.displayName} at ${it.startTime.format(format).lowercase()}" } ?: "Set up category windows in Settings", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        StreakTile(content.streakCount, bestStreak(content.completedDates), content.completedDates)
        if (content.streakCount > 0) Spacer(Modifier.height(8.dp))
        QuietWeeklyHistoryRow(content.completedDates)
    } }
}

private fun bestStreak(completedDates: Set<LocalDate>): Int {
    val dates = completedDates.sorted()
    if (dates.isEmpty()) return 0
    var best = 1
    var current = 1
    dates.zipWithNext().forEach { (previous, next) ->
        if (next == previous.plusDays(1)) current++ else current = 1
        best = maxOf(best, current)
    }
    return best
}

private val StreakFlame: ImageVector = ImageVector.Builder(
    name = "streak_flame",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(fill = SolidColor(Color.Black)) {
        moveTo(13f, 2f)
        curveTo(13f, 6f, 8f, 7f, 8f, 13f)
        curveTo(8f, 17f, 10f, 20f, 12f, 22f)
        curveTo(7f, 20f, 4f, 17f, 4f, 12f)
        curveTo(4f, 7f, 8f, 4f, 11f, 2f)
        curveTo(10f, 6f, 14f, 7f, 14f, 11f)
        curveTo(14f, 13f, 13f, 15f, 12f, 16f)
        curveTo(16f, 14f, 18f, 11f, 16f, 7f)
        curveTo(20f, 10f, 20f, 14f, 18f, 18f)
        curveTo(16f, 21f, 14f, 22f, 12f, 22f)
    }
}.build()
