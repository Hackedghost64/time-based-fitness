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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.timebasedfitness.app.data.model.Category
import com.timebasedfitness.app.ui.theme.AppSpacing
import com.timebasedfitness.app.ui.theme.CategoryTheme
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackToHome: () -> Unit,
    onPlanTransfer: () -> Unit,
    onAiPlan: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var activePicker by remember { mutableStateOf<PickerTarget?>(null) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(), onResult = {}
    )

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

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(onClick = onPlanTransfer, modifier = Modifier.fillMaxWidth()) {
                    Text("Import or share JSON plan")
                }
                OutlinedButton(onClick = onAiPlan, modifier = Modifier.fillMaxWidth()) {
                    Text("Create plan with AI")
                }
                if (android.os.Build.VERSION.SDK_INT >= 33) {
                    OutlinedButton(
                        onClick = { notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Allow routine reminders") }
                }

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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = selection.startTime.format(formatter),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = accentColor,
                                    modifier = Modifier.clickable {
                                        activePicker = PickerTarget(selection.category, TimeEnd.START)
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = " - ${selection.endTime.format(formatter)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = accentColor,
                                    modifier = Modifier.clickable {
                                        activePicker = PickerTarget(selection.category, TimeEnd.END)
                                    }
                                )
                            }
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

        activePicker?.let { target ->
            val sel = state.selections.firstOrNull { it.category == target.category } ?: return@let
            val current = if (target.end == TimeEnd.START) sel.startTime else sel.endTime
            val title = "${sel.category.displayName} ${if (target.end == TimeEnd.START) "start" else "end"}"
            TimeWindowPickerDialog(
                initialTime = current,
                title = title,
                onConfirm = { newTime ->
                    val newStart = if (target.end == TimeEnd.START) newTime else sel.startTime
                    val newEnd = if (target.end == TimeEnd.END) newTime else sel.endTime
                    viewModel.updateTimes(target.category, newStart, newEnd)
                    activePicker = null
                },
                onDismiss = { activePicker = null }
            )
        }
    }
}

private enum class TimeEnd { START, END }

private data class PickerTarget(
    val category: Category,
    val end: TimeEnd
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeWindowPickerDialog(
    initialTime: LocalTime,
    title: String,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val pickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = false
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            TimePicker(state = pickerState)
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime.of(pickerState.hour, pickerState.minute)) }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
