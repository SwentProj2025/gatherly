package com.android.gatherly.ui.SignIn

import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SignInUIState(val errorMsg: String? = null, val signedIn: Boolean = false)

class SignInViewModel(
    // private val authRepository: AuthRepository = AuthRepositoryFirebase(),
) : ViewModel() {
  private val _uiState = MutableStateFlow(SignInUIState())
  val uiState: StateFlow<SignInUIState> = _uiState.asStateFlow()

  fun signIn(credentialManager: CredentialManager): Unit {}
}
