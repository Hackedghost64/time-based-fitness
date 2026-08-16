package com.timebasedfitness.app.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.timebasedfitness.app.data.model.Category
import com.timebasedfitness.app.data.model.CategorySelection
import com.timebasedfitness.app.ui.theme.AppSpacing
import com.timebasedfitness.app.ui.theme.CategoryTheme
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(viewModel: HomeViewModel, onRoutineClick: (Category) -> Unit, onSettingsClick: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(AppSpacing.marginPage)) {
            Row(Modifier.fillMaxWidth().padding(vertical = AppSpacing.spaceMd), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Today", style = MaterialTheme.typography.headlineLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.spaceMd), verticalAlignment = Alignment.CenterVertically) {
                    if (state is HomeUiState.Content) Text("${(state as HomeUiState.Content).streakCount} day streak", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Settings", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable(onClick = onSettingsClick))
                }
            }
            Spacer(Modifier.height(AppSpacing.spaceLg))
            AnimatedContent(state, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "home-content") { current ->
                when (current) {
                    HomeUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
                    is HomeUiState.Content -> if (current.activeCategories.isNotEmpty()) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(AppSpacing.stackGap)) { items(current.activeCategories) { item -> HomeCard(item) { onRoutineClick(item.category) } } }
                    } else {
                        Box(Modifier.fillMaxSize(), Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("○", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(AppSpacing.spaceMd))
                                Text(current.nextUpcoming?.let { "Next up: ${it.category.displayName}" } ?: "Nothing scheduled right now.", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(AppSpacing.spaceSm))
                                Text(current.nextUpcoming?.let { "Scheduled for ${it.startTime.format(DateTimeFormatter.ofPattern("hh:mm a"))}" } ?: "Set up category windows in Settings.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeCard(selection: CategorySelection, onClick: () -> Unit) {
    val accent = CategoryTheme.getAccentColor(selection.category)
    val formatter = DateTimeFormatter.ofPattern("hh:mm a")
    Surface(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, accent.copy(alpha = 0.3f))) {
        Column(Modifier.padding(AppSpacing.cardPadding)) {
            Text(categoryGlyph(selection.category), color = accent, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(AppSpacing.spaceSm))
            Text(selection.category.displayName, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(AppSpacing.spaceSm))
            Text("Window: ${selection.startTime.format(formatter)} – ${selection.endTime.format(formatter)}", style = MaterialTheme.typography.bodySmall, color = accent, fontWeight = FontWeight.Medium)
        }
    }
}

private fun categoryGlyph(category: Category) = when (category) {
    Category.MORNING -> "☼"
    Category.MEALS -> "◦"
    Category.WORKOUT -> "＋"
    Category.EVENING -> "☾"
}
