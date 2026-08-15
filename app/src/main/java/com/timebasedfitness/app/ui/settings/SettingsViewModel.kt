package com.timebasedfitness.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timebasedfitness.app.data.model.Category
import com.timebasedfitness.app.data.model.CategorySelection
import com.timebasedfitness.app.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

data class SettingsUiState(
    val selections: List<CategorySelection> = emptyList(),
    val isSaving: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            categoryRepository.categorySelections.collect { list ->
                if (_uiState.value.selections.isEmpty()) {
                    _uiState.update { it.copy(selections = list) }
                }
            }
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
            current.copy(selections = updated)
        }
    }

    fun saveSettings(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            categoryRepository.saveSelections(_uiState.value.selections)
            _uiState.update { it.copy(isSaving = false) }
            onSuccess()
        }
    }
}
