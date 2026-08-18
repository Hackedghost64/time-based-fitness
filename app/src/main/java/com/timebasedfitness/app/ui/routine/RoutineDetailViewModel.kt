package com.timebasedfitness.app.ui.routine

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibrationEffect.Composition
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timebasedfitness.app.data.content.RoutineContent
import com.timebasedfitness.app.data.model.Category
import com.timebasedfitness.app.data.model.RoutineStep
import com.timebasedfitness.app.data.repository.CompletionRepository
import com.timebasedfitness.app.data.repository.RoutineRepository
import com.timebasedfitness.app.notifications.TimerNotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.timebasedfitness.app.data.model.RoutineTemplate
import com.timebasedfitness.app.data.model.TemplateRepository

/** Haptic feedback patterns for timer completion and milestones */
object HapticPatterns {
    /** Gentle double-pulse for timer completion */
    fun timerCompletePattern(): VibrationEffect {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
        } else {
            VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 100), -1)
        }
    }

    /** Stronger celebration pattern for streak milestones */
    fun milestoneCelebrationPattern(): VibrationEffect {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
        } else {
            VibrationEffect.createWaveform(longArrayOf(0, 150, 50, 150, 50, 150), -1)
        }
    }
}

/** Helper class for haptic feedback */
class HapticFeedback @Inject constructor(@ApplicationContext private val context: Context) {
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    fun vibrate(effect: VibrationEffect) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 100), -1)
        }
    }

    fun vibrateTimerComplete() {
        vibrate(HapticPatterns.timerCompletePattern())
    }

    fun vibrateMilestone() {
        vibrate(HapticPatterns.milestoneCelebrationPattern())
    }
}

data class ActiveTimer(
    val stepIndex: Int,
    val remainingSeconds: Int,
    val totalSeconds: Int,
    val isRunning: Boolean,
    val targetEndMillis: Long = 0L
)

data class RoutineDetailUiState(
    val category: Category? = null,
    val routineContent: RoutineContent? = null,
    val checkedSteps: Set<Int> = emptySet(),
    val activeTimer: ActiveTimer? = null,
    val pendingTimerStepIndex: Int? = null,
    val pendingTimerRemainingSeconds: Int = 0,
    val completedTimerIndex: Int? = null,
    val isAutoChainingEnabled: Boolean = false,
    val isCompleted: Boolean = false,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val selectedEditingDay: String? = null,
    val availableTemplates: List<RoutineTemplate> = emptyList(),
    val editTitle: String = "",
    val editGoal: String = "",
    val editSteps: List<RoutineStep> = emptyList()
)

internal fun RoutineDetailUiState.requestTimerStepToggle(index: Int): RoutineDetailUiState {
    val step = routineContent?.steps?.getOrNull(index) ?: return this
    val toggled = copy(
        checkedSteps = if (checkedSteps.contains(index)) checkedSteps - index else checkedSteps + index,
        pendingTimerStepIndex = null,
        pendingTimerRemainingSeconds = 0
    )
    if (checkedSteps.contains(index) || !step.isTimer) return toggled

    val timerFinished = activeTimer?.stepIndex == index && activeTimer.remainingSeconds == 0
    return if (timerFinished) {
        toggled
    } else {
        copy(
            pendingTimerStepIndex = index,
            pendingTimerRemainingSeconds = activeTimer
                ?.takeIf { it.stepIndex == index }
                ?.remainingSeconds
                ?: step.durationSeconds
        )
    }
}

internal fun RoutineDetailUiState.confirmTimerStepOverride(): RoutineDetailUiState {
    val index = pendingTimerStepIndex ?: return this
    return copy(
        checkedSteps = if (checkedSteps.contains(index)) checkedSteps - index else checkedSteps + index,
        pendingTimerStepIndex = null,
        pendingTimerRemainingSeconds = 0
    )
}

internal fun RoutineDetailUiState.cancelTimerStepOverride(): RoutineDetailUiState =
    copy(pendingTimerStepIndex = null, pendingTimerRemainingSeconds = 0)

@HiltViewModel
class RoutineDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val routineRepository: RoutineRepository,
    private val completionRepository: CompletionRepository,
    private val timerNotificationHelper: TimerNotificationHelper,
    private val templateRepository: TemplateRepository,
    private val hapticFeedback: HapticFeedback
) : ViewModel() {

    private val categoryParam: String? = savedStateHandle["category"]

    private val _uiState = MutableStateFlow(RoutineDetailUiState())
    val uiState: StateFlow<RoutineDetailUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var restoredTimer = false

    init {
        val category = categoryParam?.let {
            try { Category.valueOf(it) } catch (e: Exception) { null }
        }
        if (category != null) {
            val templates = templateRepository.getTemplatesForCategory(category)
            _uiState.update { it.copy(category = category, availableTemplates = templates) }
            viewModelScope.launch {
                routineRepository.observe(category).collect { content ->
                    if (!_uiState.value.isEditing) {
                        _uiState.update { state ->
                            state.copy(
                                routineContent = content,
                                editTitle = content?.title.orEmpty(),
                                editGoal = content?.goal.orEmpty(),
                                editSteps = content?.steps.orEmpty()
                            )
                        }
                        if (!restoredTimer) {
                            restoredTimer = true
                            restorePersistedTimer()
                        }
                    }
                }
            }
        }
    }

    fun toggleAutoChaining() {
        _uiState.update { it.copy(isAutoChainingEnabled = !it.isAutoChainingEnabled) }
    }

    fun selectEditingDay(day: String?) {
        val content = _uiState.value.routineContent ?: return
        val daySteps = if (day != null) {
            content.stepsByDay[day] ?: content.steps
        } else {
            content.steps
        }
        _uiState.update {
            it.copy(
                selectedEditingDay = day,
                editSteps = daySteps
            )
        }
    }

    fun applyTemplate(template: RoutineTemplate) {
        _uiState.update {
            it.copy(
                editTitle = template.content.title,
                editGoal = template.content.goal.orEmpty(),
                editSteps = template.content.steps
            )
        }
    }

    fun toggleStep(index: Int) {
        _uiState.update { current ->
            val updated = if (current.checkedSteps.contains(index)) {
                current.checkedSteps - index
            } else {
                current.checkedSteps + index
            }
            current.copy(checkedSteps = updated)
        }
    }

    fun requestToggleStep(index: Int) {
        _uiState.update { it.requestTimerStepToggle(index) }
    }

    fun confirmTimerOverride() {
        _uiState.update { it.confirmTimerStepOverride() }
    }

    fun cancelTimerOverride() {
        _uiState.update { it.cancelTimerStepOverride() }
    }

    fun startTimer(stepIndex: Int, totalSeconds: Int) {
        val currentTimer = _uiState.value.activeTimer
        val remaining = if (currentTimer != null && currentTimer.stepIndex == stepIndex && currentTimer.remainingSeconds > 0) {
            currentTimer.remainingSeconds
        } else {
            totalSeconds
        }

        startTimerAt(stepIndex, totalSeconds, remaining, System.currentTimeMillis() + (remaining * 1000L))
    }

    private fun startTimerAt(stepIndex: Int, totalSeconds: Int, remaining: Int, targetEnd: Long) {

        timerJob?.cancel()
        _uiState.update {
            it.copy(
                activeTimer = ActiveTimer(
                    stepIndex = stepIndex,
                    remainingSeconds = remaining,
                    totalSeconds = totalSeconds,
                    isRunning = true,
                    targetEndMillis = targetEnd
                ),
                completedTimerIndex = null
            )
        }
        persistActiveTimer(_uiState.value.activeTimer)

        val stepName = _uiState.value.routineContent?.steps?.getOrNull(stepIndex)?.text ?: "Step ${stepIndex + 1}"
        val categoryName = _uiState.value.category?.name.orEmpty()
        timerNotificationHelper.showTimerNotification(stepName, remaining, categoryName)

        timerJob = viewModelScope.launch {
            var lastNotifiedRemaining = remaining
            while (true) {
                delay(500L)
                val now = System.currentTimeMillis()
                val rem = kotlin.math.max(0, ((targetEnd - now + 999) / 1000).toInt())

                _uiState.update { state ->
                    state.activeTimer?.let { timer ->
                        if (timer.stepIndex == stepIndex && timer.isRunning) {
                            state.copy(activeTimer = timer.copy(remainingSeconds = rem))
                        } else state
                    } ?: state
                }

                if (rem == 0) {
                    break
                }
                if (rem != lastNotifiedRemaining) {
                    timerNotificationHelper.showTimerNotification(stepName, rem, categoryName)
                    lastNotifiedRemaining = rem
                }
            }

            // Auto-complete step when timer reaches zero!
            timerNotificationHelper.dismiss()
            hapticFeedback.vibrateTimerComplete()  // Haptic feedback on timer completion
            clearPersistedTimer()
            val newChecked = _uiState.value.checkedSteps + stepIndex
            _uiState.update { state ->
                state.copy(
                    checkedSteps = newChecked,
                    activeTimer = null,
                    completedTimerIndex = stepIndex
                )
            }

            // Rest timer chaining: if enabled, auto-start next timed step!
            if (_uiState.value.isAutoChainingEnabled) {
                val steps = _uiState.value.routineContent?.steps.orEmpty()
                val nextTimedIndex = (stepIndex + 1 until steps.size).firstOrNull { idx ->
                    steps[idx].isTimer && !newChecked.contains(idx)
                }
                if (nextTimedIndex != null) {
                    delay(1500L) // Brief transition pause
                    startTimer(nextTimedIndex, steps[nextTimedIndex].durationSeconds)
                }
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        timerNotificationHelper.dismiss()
        _uiState.update { state ->
            state.activeTimer?.let { timer ->
                state.copy(activeTimer = timer.copy(isRunning = false))
            } ?: state
        }
        persistActiveTimer(_uiState.value.activeTimer)
    }

    fun resetTimer(stepIndex: Int, totalSeconds: Int) {
        timerJob?.cancel()
        timerNotificationHelper.dismiss()
        _uiState.update { state ->
            state.copy(
                activeTimer = ActiveTimer(
                    stepIndex = stepIndex,
                    remainingSeconds = totalSeconds,
                    totalSeconds = totalSeconds,
                    isRunning = false
                ),
                completedTimerIndex = null
            )
        }
        persistActiveTimer(_uiState.value.activeTimer)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        // Keep a running timer's saved timestamp and notification available when the
        // routine screen is recreated. A restored screen derives remaining time from it.
    }

    fun markDone(onDone: () -> Unit) {
        if (_uiState.value.isCompleted) return
        val category = _uiState.value.category ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isCompleted = true) }
            completionRepository.logCompletion(category)
            onDone()
        }
    }

    private fun restorePersistedTimer() {
        val stepIndex = savedStateHandle.get<Int>(KEY_TIMER_STEP) ?: return
        val totalSeconds = savedStateHandle.get<Int>(KEY_TIMER_TOTAL) ?: return clearPersistedTimer()
        val remainingSeconds = savedStateHandle.get<Int>(KEY_TIMER_REMAINING) ?: return clearPersistedTimer()
        val isRunning = savedStateHandle.get<Boolean>(KEY_TIMER_RUNNING) ?: return clearPersistedTimer()
        val targetEnd = savedStateHandle.get<Long>(KEY_TIMER_TARGET) ?: 0L
        if (stepIndex < 0 || totalSeconds <= 0 || remainingSeconds <= 0) return clearPersistedTimer()

        if (isRunning) {
            val remaining = kotlin.math.max(0, ((targetEnd - System.currentTimeMillis() + 999) / 1000).toInt())
            if (remaining == 0) return clearPersistedTimer()
            startTimerAt(stepIndex, totalSeconds, remaining, targetEnd)
        } else {
            _uiState.update { it.copy(activeTimer = ActiveTimer(stepIndex, remainingSeconds, totalSeconds, isRunning = false)) }
        }
    }

    private fun persistActiveTimer(timer: ActiveTimer?) {
        if (timer == null) return clearPersistedTimer()
        savedStateHandle[KEY_TIMER_STEP] = timer.stepIndex
        savedStateHandle[KEY_TIMER_TOTAL] = timer.totalSeconds
        savedStateHandle[KEY_TIMER_REMAINING] = timer.remainingSeconds
        savedStateHandle[KEY_TIMER_RUNNING] = timer.isRunning
        savedStateHandle[KEY_TIMER_TARGET] = timer.targetEndMillis
    }

    private fun clearPersistedTimer() {
        savedStateHandle.remove<Int>(KEY_TIMER_STEP)
        savedStateHandle.remove<Int>(KEY_TIMER_TOTAL)
        savedStateHandle.remove<Int>(KEY_TIMER_REMAINING)
        savedStateHandle.remove<Boolean>(KEY_TIMER_RUNNING)
        savedStateHandle.remove<Long>(KEY_TIMER_TARGET)
    }

    private companion object {
        const val KEY_TIMER_STEP = "active_timer_step"
        const val KEY_TIMER_TOTAL = "active_timer_total"
        const val KEY_TIMER_REMAINING = "active_timer_remaining"
        const val KEY_TIMER_RUNNING = "active_timer_running"
        const val KEY_TIMER_TARGET = "active_timer_target"
    }

    fun startEditing() {
        val content = _uiState.value.routineContent ?: return
        _uiState.update {
            it.copy(
                isEditing = true,
                selectedEditingDay = null,
                editTitle = content.title,
                editGoal = content.goal.orEmpty(),
                editSteps = content.steps
            )
        }
    }

    fun cancelEditing() {
        _uiState.update { it.copy(isEditing = false) }
    }

    fun updateTitle(title: String) = _uiState.update { it.copy(editTitle = title.take(120)) }
    fun updateGoal(goal: String) = _uiState.update { it.copy(editGoal = goal.take(120)) }

    fun updateStepText(index: Int, value: String) = _uiState.update { state ->
        state.copy(editSteps = state.editSteps.mapIndexed { i, step ->
            if (i == index) RoutineStep.fromText(value.take(500), step.group) else step
        })
    }

    fun updateStepDuration(index: Int, durationSeconds: Int) = _uiState.update { state ->
        state.copy(editSteps = state.editSteps.mapIndexed { i, step ->
            if (i == index) step.copy(durationSeconds = durationSeconds.coerceIn(0, 7200)) else step
        })
    }

    fun updateStepGroup(index: Int, group: String) = _uiState.update { state ->
        state.copy(editSteps = state.editSteps.mapIndexed { i, step ->
            if (i == index) step.copy(group = group.take(40)) else step
        })
    }

    fun addStep() = _uiState.update {
        if (it.editSteps.size >= 100) it else it.copy(editSteps = it.editSteps + RoutineStep("", 0, "Tasks"))
    }

    fun removeStep(index: Int) = _uiState.update { state ->
        state.copy(editSteps = state.editSteps.filterIndexed { i, _ -> i != index })
    }

    fun saveEditing() {
        val category = _uiState.value.category ?: return
        val title = _uiState.value.editTitle.trim().take(120)
        val goal = _uiState.value.editGoal.trim().takeIf(String::isNotEmpty)
        val steps = _uiState.value.editSteps
            .filter { it.text.isNotBlank() }
            .take(100)
            .map { it.copy(text = it.text.trim().take(500)) }
        if (title.isEmpty() || steps.isEmpty()) return

        val selectedDay = _uiState.value.selectedEditingDay
        val existingContent = _uiState.value.routineContent
        val existingDays = existingContent?.stepsByDay.orEmpty().toMutableMap()

        val updatedMainSteps: List<RoutineStep>
        if (selectedDay == null) {
            updatedMainSteps = steps
        } else {
            existingDays[selectedDay] = steps
            updatedMainSteps = existingContent?.steps ?: steps
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            routineRepository.save(category, RoutineContent(title, updatedMainSteps, existingDays, goal))
            _uiState.update { it.copy(isEditing = false, isSaving = false) }
        }
    }

    fun resetToDefault() {
        val category = _uiState.value.category ?: return
        viewModelScope.launch { routineRepository.reset(category) }
    }
}
