package com.android.gatherly.ui.overview

import androidx.lifecycle.ViewModel
import androidx.credentials.CredentialManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class OverviewUIState(
    val errorMsg: String? = null,
    val signedOut: Boolean = false
)

class OverviewViewModel(
    //private val authRepository: AuthRepository = AuthRepositoryFirebase(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(OverviewUIState())
    val uiState: StateFlow<OverviewUIState> = _uiState.asStateFlow()


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

