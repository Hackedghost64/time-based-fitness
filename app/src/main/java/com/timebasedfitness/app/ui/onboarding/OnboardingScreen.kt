package com.timebasedfitness.app.ui.onboarding

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timebasedfitness.app.data.model.Category
import com.timebasedfitness.app.data.model.CategorySelection
import com.timebasedfitness.app.ui.theme.AccentWorkoutTeal
import com.timebasedfitness.app.ui.theme.AppSpacing
import com.timebasedfitness.app.ui.theme.CategoryTheme
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onOnboardingComplete: () -> Unit
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
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // Top Navigation & Step Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.currentStep != OnboardingStep.WELCOME && state.currentStep != OnboardingStep.READY) {
                    TextButton(
                        onClick = viewModel::previousStep,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("← Back", style = MaterialTheme.typography.labelMedium)
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }

                // Step progress bar (1 to 6)
                val stepNumber = state.currentStep.ordinal + 1
                val totalSteps = OnboardingStep.entries.size
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..totalSteps) {
                        Box(
                            modifier = Modifier
                                .height(4.dp)
                                .width(if (i == stepNumber) 24.dp else 12.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (i <= stepNumber) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                )
                        )
                    }
                }

                if (state.currentStep != OnboardingStep.READY) {
                    TextButton(
                        onClick = { viewModel.completeOnboarding(onOnboardingComplete) },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Skip", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animated Step Content
            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = state.currentStep,
                    transitionSpec = {
                        if (targetState.ordinal > initialState.ordinal) {
                            (slideInHorizontally(tween(300)) { it / 4 } + fadeIn(tween(300)))
                                .togetherWith(slideOutHorizontally(tween(300)) { -it / 4 } + fadeOut(tween(200)))
                        } else {
                            (slideInHorizontally(tween(300)) { -it / 4 } + fadeIn(tween(300)))
                                .togetherWith(slideOutHorizontally(tween(300)) { it / 4 } + fadeOut(tween(200)))
                        }
                    },
                    label = "onboarding_step"
                ) { step ->
                    when (step) {
                        OnboardingStep.WELCOME -> WelcomeStep(
                            onNext = viewModel::nextStep
                        )
                        OnboardingStep.GOAL -> GoalSelectionStep(
                            selectedGoal = state.selectedGoal,
                            onGoalSelected = viewModel::setGoal,
                            onNext = viewModel::nextStep
                        )
                        OnboardingStep.ARCHETYPE -> ScheduleArchetypeStep(
                            selectedArchetype = state.selectedArchetype,
                            onArchetypeSelected = viewModel::setArchetype,
                            onNext = viewModel::nextStep
                        )
                        OnboardingStep.CATEGORIES -> CategoryCustomizationStep(
                            selections = state.selections,
                            onToggleCategory = viewModel::toggleCategory,
                            onPickTime = { category, end -> activePicker = PickerTarget(category, end) },
                            onNext = viewModel::nextStep
                        )
                        OnboardingStep.NOTIFICATIONS -> NotificationSetupStep(
                            soundEnabled = state.soundEnabled,
                            onToggleSound = viewModel::setSoundEnabled,
                            onRequestNotifications = {
                                if (Build.VERSION.SDK_INT >= 33) {
                                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                }
                            },
                            onNext = viewModel::nextStep
                        )
                        OnboardingStep.READY -> CompletionSummaryStep(
                            goal = state.selectedGoal,
                            selections = state.selections,
                            isSaving = state.isSaving,
                            onFinish = { viewModel.completeOnboarding(onOnboardingComplete) }
                        )
                    }
                }
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

// --- Step 1: Welcome & Philosophy ---------------------------------------------

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Fitness by rhythm,\nnot rigidity.",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Most workout apps ask you to plan every set. Onset simply delivers the right routine the moment its time window arrives.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Day Cycle Feature Visuals
            val periods = listOf(
                Triple("☀️ Morning", "Awaken, mobility & hydration", CategoryTheme.getAccentColor(Category.MORNING)),
                Triple("🥗 Meals", "Nutrition timing & mindful habits", CategoryTheme.getAccentColor(Category.MEALS)),
                Triple("⚡ Workout", "Targeted training with countdown timers", CategoryTheme.getAccentColor(Category.WORKOUT)),
                Triple("🌙 Evening", "Recovery flow & deep wind-down", CategoryTheme.getAccentColor(Category.EVENING))
            )

            periods.forEach { (title, subtitle, color) ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = color.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
                            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(top = 16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Personalize My Rhythm  →", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

// --- Step 2: Goal Selection ---------------------------------------------------

@Composable
private fun GoalSelectionStep(
    selectedGoal: FitnessGoal,
    onGoalSelected: (FitnessGoal) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "What is your main\nfitness focus?",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "We will calibrate your default workout steps and duration.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            FitnessGoal.entries.forEach { goal ->
                val isSelected = goal == selectedGoal
                val accent = AccentWorkoutTeal

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (isSelected) accent.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) accent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onGoalSelected(goal) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(goal.emoji, fontSize = 28.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = goal.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) accent else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = goal.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isSelected) {
                            Text("✓", color = accent, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            }
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(top = 16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Continue  →", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

// --- Step 3: Schedule Archetype ----------------------------------------------

@Composable
private fun ScheduleArchetypeStep(
    selectedArchetype: ScheduleArchetype,
    onArchetypeSelected: (ScheduleArchetype) -> Unit,
    onNext: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("h:mm a")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Choose your daily\nrhythm preset",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Pick the archetype that best matches when you're awake and active.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            ScheduleArchetype.entries.forEach { archetype ->
                val isSelected = archetype == selectedArchetype
                val accent = MaterialTheme.colorScheme.primary

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (isSelected) accent.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) accent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onArchetypeSelected(archetype) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = archetype.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) accent else MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Text("✓", color = accent, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                        Text(
                            text = archetype.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (archetype != ScheduleArchetype.CUSTOM) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                TimePill("☀️ ${archetype.morning.first.format(formatter).lowercase()}")
                                TimePill("🥗 ${archetype.meals.first.format(formatter).lowercase()}")
                                TimePill("⚡ ${archetype.workout.first.format(formatter).lowercase()}")
                                TimePill("🌙 ${archetype.evening.first.format(formatter).lowercase()}")
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(top = 16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Continue  →", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun TimePill(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// --- Step 4: Category Customization ------------------------------------------

@Composable
private fun CategoryCustomizationStep(
    selections: List<CategorySelection>,
    onToggleCategory: (Category) -> Unit,
    onPickTime: (Category, TimeEnd) -> Unit,
    onNext: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("h:mm a")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Your daily time\nwindows",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap any time chip to adjust start or end times. Toggle categories on or off.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            selections.forEach { selection ->
                val accentColor = CategoryTheme.getAccentColor(selection.category)
                val isEnabled = selection.isEnabled

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (isEnabled) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, if (isEnabled) accentColor.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isEnabled,
                                onCheckedChange = { onToggleCategory(selection.category) },
                                colors = CheckboxDefaults.colors(checkedColor = accentColor)
                            )
                            Text(
                                text = selection.category.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isEnabled) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = accentColor.copy(alpha = 0.15f),
                                    modifier = Modifier.clickable { onPickTime(selection.category, TimeEnd.START) }
                                ) {
                                    Text(
                                        text = selection.startTime.format(formatter).lowercase(),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor
                                    )
                                }
                                Text("–", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = accentColor.copy(alpha = 0.15f),
                                    modifier = Modifier.clickable { onPickTime(selection.category, TimeEnd.END) }
                                ) {
                                    Text(
                                        text = selection.endTime.format(formatter).lowercase(),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(top = 16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Confirm Schedule  →", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

// --- Step 5: Sound & Notifications -------------------------------------------

@Composable
private fun NotificationSetupStep(
    soundEnabled: Boolean,
    onToggleSound: (Boolean) -> Unit,
    onRequestNotifications: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Smart reminders &\naudio countdowns",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Onset nudges you during your active window. Once you complete your routine, reminders instantly stop.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Notification Opt-In Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔔", fontSize = 24.sp)
                            Column {
                                Text("Routine Reminders", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Gentle in-window nudge alarms", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onRequestNotifications,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Text("Enable Notifications")
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Timer Audio Beep Toggle Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔊", fontSize = 24.sp)
                        Column {
                            Text("Timer Sound & Beeps", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("3-2-1 countdown ticks and finish tone", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = onToggleSound
                    )
                }
            }
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(top = 16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Almost Done  →", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

// --- Step 6: Completion Summary -----------------------------------------------

@Composable
private fun CompletionSummaryStep(
    goal: FitnessGoal,
    selections: List<CategorySelection>,
    isSaving: Boolean,
    onFinish: () -> Unit
) {
    val enabledCount = selections.count { it.isEnabled }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Your daily rhythm\nis ready.",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Everything is tuned to your natural day. Open the app at routine time and follow along.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(28.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(goal.emoji, fontSize = 24.sp)
                        Column {
                            Text(goal.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Calibrated for $enabledCount daily time windows", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Philosophy reminder:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• No complicated planning.\n• One active card at a time.\n• Finish today's routine to build your streak.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(top = 16.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !isSaving
        ) {
            Text(
                text = if (isSaving) "Setting up..." else "Enter Onset  🚀",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// --- Time Picker Dialog ------------------------------------------------------

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
