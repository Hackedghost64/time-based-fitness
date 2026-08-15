package com.timebasedfitness.app.ui.home

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
fun HomeScreen(
    viewModel: HomeViewModel,
    onRoutineClick: (Category) -> Unit,
    onSettingsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.marginPage)
        ) {
            // Top Bar: Header & Streak Count & Settings
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (uiState is HomeUiState.Content) {
                        val streak = (uiState as HomeUiState.Content).streakCount
                        Text(
                            text = "$streak day streak",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onSettingsClick() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is HomeUiState.Content -> {
                    if (state.activeCategories.isNotEmpty()) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.stackGap)
                        ) {
                            items(state.activeCategories) { selection ->
                                HomeCard(
                                    selection = selection,
                                    onClick = { onRoutineClick(selection.category) }
                                )
                            }
                        }
                    } else {
                        // Quiet Empty State
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val next = state.nextUpcoming
                                if (next != null) {
                                    val formatter = DateTimeFormatter.ofPattern("hh:mm a")
                                    Text(
                                        text = "Next up: ${next.category.displayName}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Scheduled for ${next.startTime.format(formatter)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Text(
                                        text = "Nothing scheduled right now.",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Set up category windows in Settings.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeCard(
    selection: CategorySelection,
    onClick: () -> Unit
) {
    val accentColor = CategoryTheme.getAccentColor(selection.category)
    val formatter = DateTimeFormatter.ofPattern("hh:mm a")

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.cardPadding)
        ) {
            Text(
                text = selection.category.displayName,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Window: ${selection.startTime.format(formatter)} - ${selection.endTime.format(formatter)}",
                style = MaterialTheme.typography.bodyMedium,
                color = accentColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
