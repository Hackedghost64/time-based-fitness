package com.timebasedfitness.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timebasedfitness.app.data.model.CategorySelection
import com.timebasedfitness.app.data.repository.CategoryRepository
import com.timebasedfitness.app.data.repository.CompletionRepository
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

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Content(val activeCategories: List<CategorySelection>, val nextUpcoming: CategorySelection?, val streakCount: Int) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(categoryRepository: CategoryRepository, completionRepository: CompletionRepository) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = combine(categoryRepository.categorySelections, completionRepository.currentStreak, minuteTicker()) { selections, streak, _ ->
        val now = LocalTime.now()
        val active = WindowMatcher.getMatchingCategories(now, selections)
        HomeUiState.Content(active, if (active.isEmpty()) WindowMatcher.getNextUpcoming(now, selections) else null, streak)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState.Loading)

    private fun minuteTicker() = flow {
        while (true) { emit(Unit); delay(60_000) }
    }
}
