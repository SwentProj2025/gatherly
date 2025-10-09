package com.android.gatherly.ui.focusTimer

import androidx.lifecycle.ViewModel
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class FocusTimerUIState(
    val errorMsg: String? = null,
    val signedOut: Boolean = false
)

class FocusTimerViewModel(
    //private val authRepository: AuthRepository = AuthRepositoryFirebase(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(FocusTimerUIState())
    val uiState: StateFlow<FocusTimerUIState> = _uiState.asStateFlow()


    /*fun signOut(credentialManager: CredentialManager): Unit {
        viewModelScope.launch {
            authRepository
                .signOut()
                .fold(
                    onSuccess = { _uiState.update { it.copy(signedOut = true) } },
                    onFailure = { throwable ->
                        _uiState.update { it.copy(errorMsg = throwable.localizedMessage) }
                    })
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        }
    }
     */

    fun signOut(credentialManager: CredentialManager): Unit {

    }
}

