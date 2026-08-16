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
    val isSaving: Boolean = false,
    val canUndoImport: Boolean = false,
    val pendingPlanPreview: com.timebasedfitness.app.data.content.FitnessPlanJson? = null
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
    private var isLocallyModified = false
    private var previousPlanBackup: String? = null

    init {
        viewModelScope.launch {
            categoryRepository.categorySelections.collect { list ->
                if (!isLocallyModified || _uiState.value.selections.isEmpty()) {
                    _uiState.update { it.copy(selections = list) }
                }
            }
        }
    }

    fun toggleCategory(category: Category) {
        isLocallyModified = true
        _uiState.update { current ->
            val updated = current.selections.map { sel ->
                if (sel.category == category) sel.copy(isEnabled = !sel.isEnabled) else sel
            }
            current.copy(selections = updated)
        }
    }

    fun updateTimes(category: Category, startTime: LocalTime, endTime: LocalTime) {
        isLocallyModified = true
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
            val currentSelections = _uiState.value.selections
            categoryRepository.saveSelections(currentSelections)
            notificationScheduler.reschedule(currentSelections)
            WidgetSnapshot.update(context, currentSelections)
            isLocallyModified = false
            _uiState.update { it.copy(isSaving = false) }
            onSuccess()
        }
    }

    fun previewPlan(rawJson: String, onResult: (String?) -> Unit) {
        val decoded = PlanJsonCodec.decode(rawJson)
        decoded.fold(
            onSuccess = { plan ->
                _uiState.update { it.copy(pendingPlanPreview = plan) }
                onResult(null)
            },
            onFailure = { error ->
                onResult(error.message ?: "Invalid plan JSON")
            }
        )
    }

    fun clearPreview() {
        _uiState.update { it.copy(pendingPlanPreview = null) }
    }

    fun confirmImportPendingPlan(onResult: (String?) -> Unit) {
        val plan = _uiState.value.pendingPlanPreview ?: return
        viewModelScope.launch {
            try {
                previousPlanBackup = routineRepository.exportPlan()
                val updatedSelections = routineRepository.importPlan(plan)
                notificationScheduler.reschedule(updatedSelections)
                WidgetSnapshot.update(context, updatedSelections)
                isLocallyModified = false
                _uiState.update {
                    it.copy(
                        selections = updatedSelections,
                        pendingPlanPreview = null,
                        canUndoImport = true
                    )
                }
                onResult(null)
            } catch (e: Exception) {
                onResult(e.message ?: "Failed to import plan")
            }
        }
    }

    fun undoLastImport(onResult: (String?) -> Unit) {
        val backup = previousPlanBackup ?: return
        importPlan(backup) { err ->
            if (err == null) {
                previousPlanBackup = null
                _uiState.update { it.copy(canUndoImport = false) }
            }
            onResult(err)
        }
    }

    fun importPlan(rawJson: String, onResult: (String?) -> Unit) {
        val decoded = PlanJsonCodec.decode(rawJson)
        decoded.fold(
            onSuccess = { plan ->
                viewModelScope.launch {
                    try {
                        previousPlanBackup = routineRepository.exportPlan()
                        val updatedSelections = routineRepository.importPlan(plan)
                        notificationScheduler.reschedule(updatedSelections)
                        WidgetSnapshot.update(context, updatedSelections)
                        isLocallyModified = false
                        _uiState.update { it.copy(selections = updatedSelections, canUndoImport = true) }
                        onResult(null)
                    } catch (e: Exception) {
                        onResult(e.message ?: "Failed to import plan")
                    }
                }
            },
            onFailure = { error -> onResult(error.message ?: "Invalid plan JSON") }
        )
    }

    fun exportPlan(onResult: (String) -> Unit) {
        viewModelScope.launch { onResult(routineRepository.exportPlan()) }
    }
}
