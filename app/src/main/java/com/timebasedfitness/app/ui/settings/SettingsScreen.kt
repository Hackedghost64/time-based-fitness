package com.timebasedfitness.app.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.timebasedfitness.app.data.model.Category
import com.timebasedfitness.app.ui.theme.AppSpacing
import com.timebasedfitness.app.ui.theme.CategoryTheme
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackToHome: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.marginPage)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Back",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onBackToHome() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Edit your routine categories and daily time windows.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(16.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Category.entries.forEach { category ->
                        val isSelected = state.selections.find { it.category == category }?.isEnabled == true
                        val accentColor = CategoryTheme.getAccentColor(category)

                        Surface(
                            modifier = Modifier.clickable { viewModel.toggleCategory(category) },
                            shape = CircleShape,
                            color = if (isSelected) accentColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, if (isSelected) accentColor else MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Text(
                                text = category.displayName,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Time Windows",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(16.dp))

                state.selections.filter { it.isEnabled }.forEach { selection ->
                    val accentColor = CategoryTheme.getAccentColor(selection.category)
                    val formatter = DateTimeFormatter.ofPattern("hh:mm a")

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(AppSpacing.spaceMd),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selection.category.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${selection.startTime.format(formatter)} - ${selection.endTime.format(formatter)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = accentColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.saveSettings(onBackToHome) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                enabled = !state.isSaving
            ) {
                Text(
                    text = if (state.isSaving) "Saving..." else "Save Settings",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
