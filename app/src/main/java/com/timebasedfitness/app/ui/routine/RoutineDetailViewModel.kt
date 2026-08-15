package com.timebasedfitness.app.ui.routine

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timebasedfitness.app.data.content.ContentRepository
import com.timebasedfitness.app.data.content.RoutineContent
import com.timebasedfitness.app.data.model.Category
import com.timebasedfitness.app.data.repository.CompletionRepository
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
    val isCompleted: Boolean = false
)

@HiltViewModel
class RoutineDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val contentRepository: ContentRepository,
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
            val content = contentRepository.getRoutine(category)
            _uiState.update {
                it.copy(
                    category = category,
                    routineContent = content
                )
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
        val category = _uiState.value.category ?: return
        viewModelScope.launch {
            completionRepository.logCompletion(category)
            _uiState.update { it.copy(isCompleted = true) }
            onDone()
        }
    }
}
