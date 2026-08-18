package com.timebasedfitness.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timebasedfitness.app.data.content.RoutineContent
import com.timebasedfitness.app.data.model.Category
import com.timebasedfitness.app.data.model.CategorySelection
import com.timebasedfitness.app.data.repository.CategoryRepository
import com.timebasedfitness.app.data.repository.CompletionRepository
import com.timebasedfitness.app.data.repository.RoutineRepository
import com.timebasedfitness.app.domain.WindowMatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged

import java.time.LocalDate

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Content(
        val activeCategories: List<CategorySelection>,
        val nextUpcoming: CategorySelection?,
        val streakCount: Int,
        val routineContentMap: Map<Category, RoutineContent?> = emptyMap(),
        val completedDates: Set<LocalDate> = emptySet()
    ) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    categoryRepository: CategoryRepository,
    completionRepository: CompletionRepository,
    routineRepository: RoutineRepository
) : ViewModel() {

    private val allRoutinesFlow = combine(
        Category.entries.map { cat -> routineRepository.observe(cat) }
    ) { contents ->
        Category.entries.zip(contents).toMap()
    }

    // Track previous streak for milestone detection
    private val previousStreakFlow = MutableStateFlow(0)

    val uiState: StateFlow<HomeUiState> = combine(
        categoryRepository.categorySelections,
        completionRepository.currentStreak,
        completionRepository.recentCompletedDates,
        allRoutinesFlow,
        minuteTicker()
    ) { selections, streak, completedDates, routineMap, _ ->
        val now = LocalTime.now()
        val active = WindowMatcher.getMatchingCategories(now, selections)
        
        // Detect streak milestones (7, 30, 60, 90, 100, 365)
        val prevStreak = previousStreakFlow.value
        val isMilestone = streak in listOf(7, 30, 60, 90, 100, 365) && streak > prevStreak
        if (isMilestone) {
            // Could trigger haptic/notification here in v2
            previousStreakFlow.value = streak
        }
        
        HomeUiState.Content(
            activeCategories = active,
            nextUpcoming = if (active.isEmpty()) WindowMatcher.getNextUpcoming(now, selections) else null,
            streakCount = streak,
            routineContentMap = routineMap,
            completedDates = completedDates
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState.Loading)

    private fun minuteTicker() = flow {
        emit(Unit)
        val initialDelay = 60_000L - (System.currentTimeMillis() % 60_000L)
        delay(initialDelay)
        while (true) {
            emit(Unit)
            delay(60_000L)
        }
    }
}

