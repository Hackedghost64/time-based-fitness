package com.timebasedfitness.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timebasedfitness.app.data.model.Category
import com.timebasedfitness.app.data.model.CategorySelection
import com.timebasedfitness.app.data.prefs.OnboardingPrefsRepository
import com.timebasedfitness.app.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

data class OnboardingUiState(
    val selections: List<CategorySelection> = listOf(
        CategorySelection(Category.MORNING, isEnabled = true, startTime = LocalTime.of(6, 0), endTime = LocalTime.of(9, 0)),
        CategorySelection(Category.MEALS, isEnabled = true, startTime = LocalTime.of(12, 0), endTime = LocalTime.of(14, 0)),
        CategorySelection(Category.WORKOUT, isEnabled = true, startTime = LocalTime.of(17, 0), endTime = LocalTime.of(19, 0)),
        CategorySelection(Category.EVENING, isEnabled = true, startTime = LocalTime.of(21, 0), endTime = LocalTime.of(23, 0))
    ),
    val isSaving: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val prefsRepository: OnboardingPrefsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

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
            current.copy(selections = updated)
        }
    }

    fun completeOnboarding(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            categoryRepository.saveSelections(_uiState.value.selections)
            prefsRepository.setHasOnboarded(true)
            _uiState.update { it.copy(isSaving = false) }
            onSuccess()
        }
    }
}
