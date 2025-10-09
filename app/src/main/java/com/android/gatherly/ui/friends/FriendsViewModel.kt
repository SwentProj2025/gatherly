package com.android.gatherly.ui.friends

import androidx.lifecycle.ViewModel
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FriendsUIState(
    val errorMsg: String? = null
)

class FriendsViewModel() : ViewModel() {

    private val _uiState = MutableStateFlow(FriendsUIState())
    val uiState: StateFlow<FriendsUIState> = _uiState.asStateFlow()


}

