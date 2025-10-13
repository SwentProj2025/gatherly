package com.android.gatherly.ui.focusTimer

import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FocusTimerUIState(val errorMsg: String? = null, val signedOut: Boolean = false)

class FocusTimerViewModel() : ViewModel() {
  private val _uiState = MutableStateFlow(FocusTimerUIState())
  val uiState: StateFlow<FocusTimerUIState> = _uiState.asStateFlow()

  /** Initiates sign-out */
  fun signOut(credentialManager: CredentialManager): Unit {
    viewModelScope.launch {
      Firebase.auth.signOut()
      credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
  }
}
