package com.timebasedfitness.app.ui.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timebasedfitness.app.data.content.RoutineContent
import com.timebasedfitness.app.data.model.Category
import com.timebasedfitness.app.data.model.CategorySelection
import com.timebasedfitness.app.data.model.RoutineStep
import com.timebasedfitness.app.data.prefs.OnboardingPrefsRepository
import com.timebasedfitness.app.data.repository.CategoryRepository
import com.timebasedfitness.app.data.repository.RoutineRepository
import com.timebasedfitness.app.notifications.NotificationScheduler
import com.timebasedfitness.app.widget.WidgetSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

enum class OnboardingStep {
    WELCOME,
    GOAL,
    ARCHETYPE,
    CATEGORIES,
    NOTIFICATIONS,
    READY
}

enum class FitnessGoal(
    val title: String,
    val subtitle: String,
    val emoji: String
) {
    STRENGTH(
        title = "Build Strength & Muscle",
        subtitle = "Progressive bodyweight & resistance exercises",
        emoji = "🏋️‍♂️"
    ),
    CARDIO(
        title = "Cardio & Fat Loss",
        subtitle = "Intervals, conditioning, and daily calorie burn",
        emoji = "🏃"
    ),
    MOBILITY(
        title = "Mobility & Recovery",
        subtitle = "Full body flexibility, posture, and joint relief",
        emoji = "🧘"
    ),
    DAILY_ENERGY(
        title = "Daily Energy & Habit",
        subtitle = "Quick movements, hydration, and consistency",
        emoji = "⚡"
    )
}

enum class ScheduleArchetype(
    val title: String,
    val subtitle: String,
    val morning: Pair<LocalTime, LocalTime>,
    val meals: Pair<LocalTime, LocalTime>,
    val workout: Pair<LocalTime, LocalTime>,
    val evening: Pair<LocalTime, LocalTime>
) {
    STANDARD(
        title = "Standard Day",
        subtitle = "Classic day rhythm with late afternoon training",
        morning = LocalTime.of(6, 0) to LocalTime.of(9, 0),
        meals = LocalTime.of(12, 0) to LocalTime.of(14, 0),
        workout = LocalTime.of(17, 30) to LocalTime.of(19, 30),
        evening = LocalTime.of(21, 30) to LocalTime.of(23, 0)
    ),
    EARLY_BIRD(
        title = "Early Bird",
        subtitle = "Dawn workout and early evening wind-down",
        morning = LocalTime.of(5, 0) to LocalTime.of(7, 30),
        meals = LocalTime.of(11, 30) to LocalTime.of(13, 30),
        workout = LocalTime.of(6, 0) to LocalTime.of(7, 30),
        evening = LocalTime.of(20, 30) to LocalTime.of(22, 0)
    ),
    NIGHT_OWL(
        title = "Late Rhythm",
        subtitle = "Shifted schedule with evening workout session",
        morning = LocalTime.of(8, 30) to LocalTime.of(11, 0),
        meals = LocalTime.of(13, 0) to LocalTime.of(15, 0),
        workout = LocalTime.of(19, 30) to LocalTime.of(21, 30),
        evening = LocalTime.of(23, 0) to LocalTime.of(1, 0)
    ),
    CUSTOM(
        title = "Custom Schedule",
        subtitle = "Fine-tune each individual time window",
        morning = LocalTime.of(6, 0) to LocalTime.of(9, 0),
        meals = LocalTime.of(12, 0) to LocalTime.of(14, 0),
        workout = LocalTime.of(17, 0) to LocalTime.of(19, 0),
        evening = LocalTime.of(21, 0) to LocalTime.of(23, 0)
    )
}

data class OnboardingUiState(
    val currentStep: OnboardingStep = OnboardingStep.WELCOME,
    val selectedGoal: FitnessGoal = FitnessGoal.STRENGTH,
    val selectedArchetype: ScheduleArchetype = ScheduleArchetype.STANDARD,
    val selections: List<CategorySelection> = listOf(
        CategorySelection(Category.MORNING, isEnabled = true, startTime = LocalTime.of(6, 0), endTime = LocalTime.of(9, 0)),
        CategorySelection(Category.MEALS, isEnabled = true, startTime = LocalTime.of(12, 0), endTime = LocalTime.of(14, 0)),
        CategorySelection(Category.WORKOUT, isEnabled = true, startTime = LocalTime.of(17, 30), endTime = LocalTime.of(19, 30)),
        CategorySelection(Category.EVENING, isEnabled = true, startTime = LocalTime.of(21, 30), endTime = LocalTime.of(23, 0))
    ),
    val soundEnabled: Boolean = true,
    val isSaving: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val routineRepository: RoutineRepository,
    private val prefsRepository: OnboardingPrefsRepository,
    private val notificationScheduler: NotificationScheduler,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun nextStep() {
        val next = when (_uiState.value.currentStep) {
            OnboardingStep.WELCOME -> OnboardingStep.GOAL
            OnboardingStep.GOAL -> OnboardingStep.ARCHETYPE
            OnboardingStep.ARCHETYPE -> OnboardingStep.CATEGORIES
            OnboardingStep.CATEGORIES -> OnboardingStep.NOTIFICATIONS
            OnboardingStep.NOTIFICATIONS -> OnboardingStep.READY
            OnboardingStep.READY -> OnboardingStep.READY
        }
        _uiState.update { it.copy(currentStep = next) }
    }

    fun previousStep() {
        val prev = when (_uiState.value.currentStep) {
            OnboardingStep.WELCOME -> OnboardingStep.WELCOME
            OnboardingStep.GOAL -> OnboardingStep.WELCOME
            OnboardingStep.ARCHETYPE -> OnboardingStep.GOAL
            OnboardingStep.CATEGORIES -> OnboardingStep.ARCHETYPE
            OnboardingStep.NOTIFICATIONS -> OnboardingStep.CATEGORIES
            OnboardingStep.READY -> OnboardingStep.NOTIFICATIONS
        }
        _uiState.update { it.copy(currentStep = prev) }
    }

    fun setGoal(goal: FitnessGoal) {
        _uiState.update { it.copy(selectedGoal = goal) }
    }

    fun setArchetype(archetype: ScheduleArchetype) {
        _uiState.update { current ->
            val updatedSelections = current.selections.map { sel ->
                val times = when (sel.category) {
                    Category.MORNING -> archetype.morning
                    Category.MEALS -> archetype.meals
                    Category.WORKOUT -> archetype.workout
                    Category.EVENING -> archetype.evening
                }
                sel.copy(startTime = times.first, endTime = times.second)
            }
            current.copy(
                selectedArchetype = archetype,
                selections = updatedSelections
            )
        }
    }

    fun toggleCategory(category: Category) {
        _uiState.update { current ->
            val updated = current.selections.map { sel ->
                if (sel.category == category) sel.copy(isEnabled = !sel.isEnabled) else sel
            }
            current.copy(selections = updated)
        }
    }

    fun updateTimes(category: Category, startTime: LocalTime, endTime: LocalTime) {
        _uiState.update { current ->
            val updated = current.selections.map { sel ->
                if (sel.category == category) sel.copy(startTime = startTime, endTime = endTime) else sel
            }
            current.copy(
                selectedArchetype = ScheduleArchetype.CUSTOM,
                selections = updated
            )
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        _uiState.update { it.copy(soundEnabled = enabled) }
    }

    fun completeOnboarding(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            // 1. Save time window category selections
            categoryRepository.saveSelections(_uiState.value.selections)

            // 2. Personalize workout routine based on selected goal
            val goal = _uiState.value.selectedGoal
            val routine = buildGoalRoutine(goal)
            routineRepository.save(Category.WORKOUT, routine)

            // 3. Save preferences
            prefsRepository.setTimerSoundEnabled(_uiState.value.soundEnabled)
            prefsRepository.setHasOnboarded(true)

            // 4. Schedule reminders and update widget snapshot
            notificationScheduler.reschedule(_uiState.value.selections)
            WidgetSnapshot.update(context, _uiState.value.selections)

            _uiState.update { it.copy(isSaving = false) }
            onSuccess()
        }
    }

    private fun buildGoalRoutine(goal: FitnessGoal): RoutineContent = when (goal) {
        FitnessGoal.STRENGTH -> RoutineContent(
            title = "Strength & Power Session",
            goal = "Upper & lower body resistance foundation",
            steps = listOf(
                RoutineStep("Dynamic Warmup & Hip Openers (120s)", 120, "Warm-up"),
                RoutineStep("Bodyweight Squats or Goblet Squats (3x15)", 0, "Lower"),
                RoutineStep("Pushups (3 sets of 10-15 reps)", 0, "Upper"),
                RoutineStep("Inter-set Rest (60s)", 60, "Rest"),
                RoutineStep("Glute Bridges (3x12)", 0, "Lower"),
                RoutineStep("Plank Hold (60s)", 60, "Core"),
                RoutineStep("Full Body Cool-down (120s)", 120, "Cool-down")
            )
        )
        FitnessGoal.CARDIO -> RoutineContent(
            title = "Cardio Conditioning",
            goal = "High-energy endurance & fat burn",
            steps = listOf(
                RoutineStep("Jumping Jacks & High Knees (120s)", 120, "Warm-up"),
                RoutineStep("Mountain Climbers (45s)", 45, "Interval"),
                RoutineStep("Rest (30s)", 30, "Rest"),
                RoutineStep("Bodyweight Squat Jumps (45s)", 45, "Interval"),
                RoutineStep("Rest (30s)", 30, "Rest"),
                RoutineStep("Burpees or Step-backs (45s)", 45, "Interval"),
                RoutineStep("Shadow Boxing / Light Jog (180s)", 180, "Conditioning"),
                RoutineStep("Deep breathing recovery (120s)", 120, "Cool-down")
            )
        )
        FitnessGoal.MOBILITY -> RoutineContent(
            title = "Mobility & Joint Flow",
            goal = "Spine decompression & deep flexibility",
            steps = listOf(
                RoutineStep("Cat-Cow Spine Awakening (60s)", 60, "Spine"),
                RoutineStep("World's Greatest Stretch (90s)", 90, "Hips"),
                RoutineStep("Deep Squat Hold (60s)", 60, "Ankles & Hips"),
                RoutineStep("Thoracic Rotations (60s)", 60, "Upper Back"),
                RoutineStep("Hamstring & Hip Flexor Flow (90s)", 90, "Lower"),
                RoutineStep("Child's Pose & Breath (120s)", 120, "Decompression")
            )
        )
        FitnessGoal.DAILY_ENERGY -> RoutineContent(
            title = "Daily Vitality Circuit",
            goal = "Quick full-body activation & consistency",
            steps = listOf(
                RoutineStep("500ml Water Hydration Check", 0, "Hydration"),
                RoutineStep("Arm Circles & Shoulder Rolls (60s)", 60, "Upper"),
                RoutineStep("Bodyweight Squats (20 reps)", 0, "Lower"),
                RoutineStep("Doorway Chest Stretch (60s)", 60, "Posture"),
                RoutineStep("Side Lunges (10 each side)", 0, "Mobility"),
                RoutineStep("Wall Sit (45s)", 45, "Core"),
                RoutineStep("Mindful Breathing (60s)", 60, "Mindset")
            )
        )
    }
}
