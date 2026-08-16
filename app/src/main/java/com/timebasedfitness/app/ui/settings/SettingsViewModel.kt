package com.timebasedfitness.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timebasedfitness.app.data.model.Category
import com.timebasedfitness.app.data.model.CategorySelection
import com.timebasedfitness.app.data.repository.CategoryRepository
import com.timebasedfitness.app.data.repository.RoutineRepository
import com.timebasedfitness.app.data.content.PlanJsonCodec
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject
import com.timebasedfitness.app.notifications.NotificationScheduler
import com.timebasedfitness.app.widget.WidgetSnapshot
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context

data class SettingsUiState(
    val selections: List<CategorySelection> = emptyList(),
    val isSaving: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val routineRepository: RoutineRepository,
    private val notificationScheduler: NotificationScheduler,
    @ApplicationContext private val context: Context
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
            notificationScheduler.reschedule(_uiState.value.selections)
            WidgetSnapshot.update(context, _uiState.value.selections)
            _uiState.update { it.copy(isSaving = false) }
            onSuccess()
        }
    }

    fun importPlan(rawJson: String, onResult: (String?) -> Unit) {
        val decoded = PlanJsonCodec.decode(rawJson)
        decoded.fold(
            onSuccess = { plan ->
                viewModelScope.launch {
                    routineRepository.importPlan(plan)
                    categoryRepository.applyPlanSchedules(plan)
                    notificationScheduler.reschedule(plan.categories.map { item ->
                        com.timebasedfitness.app.data.model.CategorySelection(
                            category = Category.valueOf(item.category),
                            isEnabled = true,
                            startTime = item.startTime?.let(LocalTime::parse) ?: LocalTime.of(9, 0),
                            endTime = item.endTime?.let(LocalTime::parse) ?: LocalTime.of(17, 0)
                        )
                    })
                    WidgetSnapshot.update(context, categoryRepository.categorySelections.first())
                    onResult(null)
                }
            },
            onFailure = { error -> onResult(error.message ?: "Invalid plan JSON") }
        )
    }

    fun exportPlan(onResult: (String) -> Unit) {
        viewModelScope.launch { onResult(routineRepository.exportPlan()) }
    }
}
