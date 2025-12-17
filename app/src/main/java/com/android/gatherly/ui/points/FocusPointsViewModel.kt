package com.android.gatherly.ui.points

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.gatherly.model.points.Points
import com.android.gatherly.model.points.PointsRepository
import com.android.gatherly.model.points.PointsRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state for the Focus Points screen.
 *
 * @param focusHistory List of all focus points entries for the current user, ordered as returned by
 *   the repository.
 */
data class FocusPointsUIState(val focusHistory: List<Points> = emptyList())

/**
 * ViewModel for the Focus Points screen.
 *
 * Loads the current user's points history from [PointsRepository] and exposes it via [uiState].
 *
 * @param pointsRepository Repository used to retrieve the points history. Defaults to the app
 *   repository provider.
 */
class FocusPointsViewModel(
    private val pointsRepository: PointsRepository = PointsRepositoryProvider.repository
) : ViewModel() {

  /** StateFlow exposing the current [FocusPointsUIState] for the Focus Points screen. */
  private val _uiState = MutableStateFlow(FocusPointsUIState())
  val uiState: StateFlow<FocusPointsUIState> = _uiState.asStateFlow()

  /** Loads the user's points history when the ViewModel is created. */
  init {
    loadPointsHistory()
  }

  /** Fetches the user's full points history and updates [uiState]. */
  fun loadPointsHistory() {
    viewModelScope.launch {
      val userHistory = pointsRepository.getAllPoints()
      _uiState.value = _uiState.value.copy(focusHistory = userHistory)
    }
  }
}
