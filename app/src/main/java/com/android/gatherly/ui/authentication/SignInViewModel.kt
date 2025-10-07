package com.android.gatherly.ui.authentication

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SignInViewModel : ViewModel() {
  // UI State containing the user sign in status
  private val _uiState = MutableStateFlow<Boolean>(false)

  // Read-only UI State presented to the UI
  val uiState: StateFlow<Boolean>
    get() = _uiState

  /** Sign in with Google */
  fun signInWithGoogle(context: Context, credentialManager: CredentialManager) {}

  /** Sign in anonymously */
  fun signInAnonymously() {}
}
