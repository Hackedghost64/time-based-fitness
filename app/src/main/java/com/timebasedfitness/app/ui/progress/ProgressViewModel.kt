package com.timebasedfitness.app.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timebasedfitness.app.data.model.Category
import com.timebasedfitness.app.data.model.CompletionLog
import com.timebasedfitness.app.data.repository.CompletionRepository
import com.timebasedfitness.app.domain.StreakCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class ProgressUiState(
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalCompletions: Int = 0,
    val currentMonth: YearMonth = YearMonth.now(),
    val completedDates: Set<LocalDate> = emptySet(),
    val completionsByCategory: Map<Category, Int> = emptyMap(),
    val recentLogs: List<CompletionLog> = emptyList()
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val completionRepository: CompletionRepository
) : ViewModel() {

    private val currentMonthFlow = MutableStateFlow(YearMonth.now())

    val uiState: StateFlow<ProgressUiState> = combine(
        completionRepository.completionLogs,
        currentMonthFlow
    ) { logs, month ->
        val completedDates = logs.map { it.date }.toSet()
        val currentStreak = StreakCalculator.calculateStreak(logs)
        val bestStreak = StreakCalculator.calculateBestStreak(logs)
        val byCat = logs.groupBy { it.category }.mapValues { it.value.size }

        ProgressUiState(
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            totalCompletions = logs.size,
            currentMonth = month,
            completedDates = completedDates,
            completionsByCategory = byCat,
            recentLogs = logs.take(20)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProgressUiState()
    )

    fun previousMonth() {
        currentMonthFlow.value = currentMonthFlow.value.minusMonths(1)
    }

    fun nextMonth() {
        currentMonthFlow.value = currentMonthFlow.value.plusMonths(1)
    }

    fun jumpToCurrentMonth() {
        currentMonthFlow.value = YearMonth.now()
    }
}
