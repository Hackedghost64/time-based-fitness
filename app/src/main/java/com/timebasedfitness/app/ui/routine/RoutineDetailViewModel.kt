package com.timebasedfitness.app.ui.routine

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timebasedfitness.app.data.content.RoutineContent
import com.timebasedfitness.app.data.model.Category
import com.timebasedfitness.app.data.repository.CompletionRepository
import com.timebasedfitness.app.data.repository.RoutineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoutineDetailUiState(
    val category: Category? = null,
    val routineContent: RoutineContent? = null,
    val checkedSteps: Set<Int> = emptySet(),
    val isCompleted: Boolean = false,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val editTitle: String = "",
    val editSteps: List<String> = emptyList()
)

@HiltViewModel
class RoutineDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val routineRepository: RoutineRepository,
    private val completionRepository: CompletionRepository
) : ViewModel() {

    private val categoryParam: String? = savedStateHandle["category"]

    private val _uiState = MutableStateFlow(RoutineDetailUiState())
    val uiState: StateFlow<RoutineDetailUiState> = _uiState.asStateFlow()

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
                                editSteps = content?.steps.orEmpty()
                            )
                        }
                    }
                }
            }
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
        _uiState.update { it.copy(isEditing = true, editTitle = content.title, editSteps = content.steps) }
    }

    fun cancelEditing() {
        _uiState.update { it.copy(isEditing = false) }
    }

    fun updateTitle(title: String) = _uiState.update { it.copy(editTitle = title.take(120)) }

    fun updateStep(index: Int, value: String) = _uiState.update { state ->
        state.copy(editSteps = state.editSteps.mapIndexed { i, step -> if (i == index) value.take(500) else step })
    }

    fun addStep() = _uiState.update {
        if (it.editSteps.size >= 100) it else it.copy(editSteps = it.editSteps + "")
    }

    fun removeStep(index: Int) = _uiState.update { state ->
        state.copy(editSteps = state.editSteps.filterIndexed { i, _ -> i != index })
    }

    fun saveEditing() {
        val category = _uiState.value.category ?: return
        val title = _uiState.value.editTitle.trim().take(120)
        val steps = _uiState.value.editSteps
            .map(String::trim)
            .filter(String::isNotEmpty)
            .take(100)
            .map { it.take(500) }
        if (title.isEmpty() || steps.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            routineRepository.save(category, RoutineContent(title, steps))
            _uiState.update { it.copy(isEditing = false, isSaving = false) }
        }
    }

    fun resetToDefault() {
        val category = _uiState.value.category ?: return
        viewModelScope.launch { routineRepository.reset(category) }
    }
}
