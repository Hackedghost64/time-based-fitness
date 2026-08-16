package com.timebasedfitness.app.ui.routine

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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    val completedTimerIndex: Int? = null,
    val isAutoChainingEnabled: Boolean = false,
    val isCompleted: Boolean = false,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val editTitle: String = "",
    val editGoal: String = "",
    val editSteps: List<RoutineStep> = emptyList()
)

@HiltViewModel
class RoutineDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val routineRepository: RoutineRepository,
    private val completionRepository: CompletionRepository,
    private val timerNotificationHelper: TimerNotificationHelper
) : ViewModel() {

    private val categoryParam: String? = savedStateHandle["category"]

    private val _uiState = MutableStateFlow(RoutineDetailUiState())
    val uiState: StateFlow<RoutineDetailUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        val category = categoryParam?.let {
            try { Category.valueOf(it) } catch (e: Exception) { null }
        }
        if (category != null) {
            _uiState.update { it.copy(category = category) }
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
                    }
                }
            }
        }
    }

    fun toggleAutoChaining() {
        _uiState.update { it.copy(isAutoChainingEnabled = !it.isAutoChainingEnabled) }
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

    fun startTimer(stepIndex: Int, totalSeconds: Int) {
        val currentTimer = _uiState.value.activeTimer
        val remaining = if (currentTimer != null && currentTimer.stepIndex == stepIndex && currentTimer.remainingSeconds > 0) {
            currentTimer.remainingSeconds
        } else {
            totalSeconds
        }

        val targetEnd = System.currentTimeMillis() + (remaining * 1000L)

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

        val stepName = _uiState.value.routineContent?.steps?.getOrNull(stepIndex)?.text ?: "Step ${stepIndex + 1}"
        val categoryName = _uiState.value.category?.name.orEmpty()
        timerNotificationHelper.showTimerNotification(stepName, remaining, categoryName)

        timerJob = viewModelScope.launch {
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

                if (rem > 0) {
                    timerNotificationHelper.showTimerNotification(stepName, rem, categoryName)
                } else {
                    break
                }
            }

            // Auto-complete step when timer reaches zero!
            timerNotificationHelper.dismiss()
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
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        timerNotificationHelper.dismiss()
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

    fun startEditing() {
        val content = _uiState.value.routineContent ?: return
        _uiState.update {
            it.copy(
                isEditing = true,
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

        val existingDays = _uiState.value.routineContent?.stepsByDay.orEmpty()
        val todayKey = java.time.LocalDate.now().dayOfWeek.name
        val updatedDays = if (existingDays.isNotEmpty()) {
            existingDays.toMutableMap().apply { put(todayKey, steps) }
        } else {
            emptyMap()
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            routineRepository.save(category, RoutineContent(title, steps, updatedDays, goal))
            _uiState.update { it.copy(isEditing = false, isSaving = false) }
        }
    }

    fun resetToDefault() {
        val category = _uiState.value.category ?: return
        viewModelScope.launch { routineRepository.reset(category) }
    }
}
