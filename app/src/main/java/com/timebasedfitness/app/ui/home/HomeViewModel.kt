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

    val uiState: StateFlow<HomeUiState> = combine(
        categoryRepository.categorySelections,
        completionRepository.currentStreak,
        completionRepository.recentCompletedDates,
        allRoutinesFlow,
        minuteTicker()
    ) { selections, streak, completedDates, routineMap, _ ->
        val now = LocalTime.now()
        val active = WindowMatcher.getMatchingCategories(now, selections)
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

