package com.android.gatherly.ui.settings

import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SettingsUIState(
    val errorMsg: String? = null,
    val signedOut: Boolean = false
)

class SettingsViewModel(
    //private val authRepository: AuthRepository = AuthRepositoryFirebase(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUIState())
    val uiState: StateFlow<SettingsUIState> = _uiState.asStateFlow()


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

