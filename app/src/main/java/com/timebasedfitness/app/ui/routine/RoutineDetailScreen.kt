package com.timebasedfitness.app.ui.routine

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Back",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onBackToHome() }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (uiState.isEditing) {
                        OutlinedTextField(
                            value = uiState.editTitle,
                            onValueChange = viewModel::updateTitle,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Routine title") },
                            singleLine = true
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = routine.title,
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = viewModel::startEditing) { Text("Edit") }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (uiState.isEditing) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(AppSpacing.spaceMd)) {
                            itemsIndexed(uiState.editSteps) { index, step ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(
                                        value = step,
                                        onValueChange = { viewModel.updateStep(index, it) },
                                        modifier = Modifier.weight(1f),
                                        label = { Text("Step ${index + 1}") }
                                    )
                                    TextButton(onClick = { viewModel.removeStep(index) }) { Text("Remove") }
                                }
                            }
                            item {
                                OutlinedButton(onClick = viewModel::addStep, modifier = Modifier.fillMaxWidth()) {
                                    Text("Add step")
                                }
                            }
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(AppSpacing.spaceMd)) {
                            itemsIndexed(routine.steps) { index, step ->
                                val isChecked = uiState.checkedSteps.contains(index)

                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { viewModel.toggleStep(index) }.padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(checked = isChecked, onCheckedChange = { viewModel.toggleStep(index) }, colors = CheckboxDefaults.colors(checkedColor = accentColor, uncheckedColor = MaterialTheme.colorScheme.outline))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(text = step, style = MaterialTheme.typography.bodyLarge, color = if (isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
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
