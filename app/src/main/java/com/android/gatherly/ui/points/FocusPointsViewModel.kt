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

data class FocusPointsUIState(val focusHistory: List<Points> = emptyList())

class FocusPointsViewModel(
    private val pointsRepository: PointsRepository = PointsRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(FocusPointsUIState())
  val uiState: StateFlow<FocusPointsUIState> = _uiState.asStateFlow()

  init {
    loadPointsHistory()
  }

  fun loadPointsHistory() {
    viewModelScope.launch {
      val userHistory = pointsRepository.getAllPoints()
      _uiState.value = _uiState.value.copy(focusHistory = userHistory)
    }
  }
}
