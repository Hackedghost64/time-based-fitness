package com.timebasedfitness.app.ui.routine

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timebasedfitness.app.data.model.RoutineStep
import com.timebasedfitness.app.ui.theme.AppSpacing
import com.timebasedfitness.app.ui.theme.CategoryTheme

@Composable
fun RoutineDetailScreen(
    viewModel: RoutineDetailViewModel,
    onBackToHome: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val category = uiState.category
    val routine = uiState.routineContent

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (category == null || routine == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Routine not found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        } else {
            val accentColor = CategoryTheme.getAccentColor(category)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AppSpacing.marginPage),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Spacer(modifier = Modifier.height(AppSpacing.spaceMd))
                    Text(
                        text = "← Back",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onBackToHome() }
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.spaceMd))

                    if (uiState.isEditing) {
                        OutlinedTextField(
                            value = uiState.editTitle,
                            onValueChange = viewModel::updateTitle,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Routine title") },
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = uiState.editGoal,
                            onValueChange = viewModel::updateGoal,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Main goal / focus (optional)") },
                            singleLine = true
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = routine.title,
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                routine.goal?.let { goal ->
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Goal: $goal",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = accentColor,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            TextButton(onClick = viewModel::startEditing) { Text("Edit") }
                        }
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.spaceLg))

                    if (uiState.isEditing) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(AppSpacing.spaceMd)) {
                            if (uiState.editSteps.isEmpty()) {
                                item {
                                    Text(
                                        text = "No steps added yet. Tap \"Add step\" below to create your routine.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = AppSpacing.spaceMd)
                                    )
                                }
                            }
                            itemsIndexed(uiState.editSteps) { index, step ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            OutlinedTextField(
                                                value = step.text,
                                                onValueChange = { viewModel.updateStepText(index, it) },
                                                modifier = Modifier.weight(1f),
                                                label = { Text("Step ${index + 1}") }
                                            )
                                            IconButton(onClick = { viewModel.removeStep(index) }) {
                                                Text("✕", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Timer:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            listOf(0 to "None", 30 to "30s", 60 to "1m", 120 to "2m", 300 to "5m").forEach { (secs, label) ->
                                                val isSelected = step.durationSeconds == secs
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (isSelected) accentColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                                    border = BorderStroke(1.dp, if (isSelected) accentColor else MaterialTheme.colorScheme.outlineVariant),
                                                    modifier = Modifier.clickable { viewModel.updateStepDuration(index, secs) }
                                                ) {
                                                    Text(
                                                        text = label,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            item {
                                OutlinedButton(onClick = viewModel::addStep, modifier = Modifier.fillMaxWidth()) {
                                    Text("+ Add step")
                                }
                            }
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(AppSpacing.spaceSm)) {
                            itemsIndexed(routine.steps) { index, step ->
                                val isChecked = uiState.checkedSteps.contains(index)
                                val activeTimer = uiState.activeTimer
                                val isTimerActive = activeTimer?.stepIndex == index

                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isChecked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isTimerActive) accentColor else MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().clickable { viewModel.toggleStep(index) },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = isChecked,
                                                onCheckedChange = { viewModel.toggleStep(index) },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = accentColor,
                                                    uncheckedColor = MaterialTheme.colorScheme.outline
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = step.text,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    color = if (isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                                    fontWeight = if (isChecked) FontWeight.Normal else FontWeight.Medium
                                                )
                                                if (step.group.isNotEmpty() && step.group != "Tasks") {
                                                    Text(
                                                        text = step.group,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = accentColor.copy(alpha = 0.8f)
                                                    )
                                                }
                                            }
                                        }

                                        // Step Timer Controls
                                        if (step.isTimer && !isChecked) {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            val remaining = if (isTimerActive && activeTimer != null) activeTimer.remainingSeconds else step.durationSeconds
                                            val minutes = remaining / 60
                                            val seconds = remaining % 60
                                            val timeStr = "%02d:%02d".format(minutes, seconds)

                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = accentColor.copy(alpha = 0.08f),
                                                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f))
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text("⏱ ", fontSize = 16.sp)
                                                        Text(
                                                            text = timeStr,
                                                            style = MaterialTheme.typography.titleMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = accentColor
                                                        )
                                                    }

                                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        if (isTimerActive && activeTimer?.isRunning == true) {
                                                            FilledTonalButton(
                                                                onClick = viewModel::pauseTimer,
                                                                shape = CircleShape,
                                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                                            ) {
                                                                Text("Pause", style = MaterialTheme.typography.labelSmall)
                                                            }
                                                        } else {
                                                            Button(
                                                                onClick = { viewModel.startTimer(index, step.durationSeconds) },
                                                                shape = CircleShape,
                                                                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                                            ) {
                                                                Icon(Icons.Filled.PlayArrow, contentDescription = "Start", modifier = Modifier.size(16.dp))
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text(if (isTimerActive) "Resume" else "Start", style = MaterialTheme.typography.labelSmall)
                                                            }
                                                        }

                                                        if (isTimerActive) {
                                                            IconButton(
                                                                onClick = { viewModel.resetTimer(index, step.durationSeconds) },
                                                                modifier = Modifier.size(32.dp)
                                                            ) {
                                                                Icon(Icons.Filled.Refresh, contentDescription = "Reset", tint = accentColor, modifier = Modifier.size(16.dp))
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (uiState.isEditing) {
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.spaceMd)) {
                        OutlinedButton(onClick = viewModel::cancelEditing, modifier = Modifier.weight(1f).height(56.dp)) { Text("Cancel") }
                        Button(onClick = viewModel::saveEditing, enabled = !uiState.isSaving, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(16.dp)) { Text(if (uiState.isSaving) "Saving..." else "Save") }
                    }
                } else Button(
                    onClick = { viewModel.markDone(onBackToHome) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Done",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (!uiState.isEditing) {
                    TextButton(onClick = viewModel::resetToDefault, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text("Reset to default")
                    }
                }
            }
        }
    }
}
