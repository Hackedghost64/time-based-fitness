package com.timebasedfitness.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timebasedfitness.app.data.model.CategorySelection
import com.timebasedfitness.app.data.repository.CategoryRepository
import com.timebasedfitness.app.data.repository.CompletionRepository
import com.timebasedfitness.app.domain.WindowMatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalTime
import javax.inject.Inject

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Content(
        val activeCategories: List<CategorySelection>,
        val nextUpcoming: CategorySelection?,
        val streakCount: Int
    ) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    categoryRepository: CategoryRepository,
    completionRepository: CompletionRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        categoryRepository.categorySelections,
        completionRepository.currentStreak
    ) { selections, streak ->
        val now = LocalTime.now()
        val active = WindowMatcher.getMatchingCategories(now, selections)
        val next = if (active.isEmpty()) WindowMatcher.getNextUpcoming(now, selections) else null

        HomeUiState.Content(
            activeCategories = active,
            nextUpcoming = next,
            streakCount = streak
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState.Loading
    )
}
