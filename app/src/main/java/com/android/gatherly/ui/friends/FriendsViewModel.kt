package com.android.gatherly.ui.friends

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class FriendsUIState(val errorMsg: String? = null)

class FriendsViewModel() : ViewModel() {

  private val _uiState = MutableStateFlow(FriendsUIState())
  val uiState: StateFlow<FriendsUIState> = _uiState.asStateFlow()
}
