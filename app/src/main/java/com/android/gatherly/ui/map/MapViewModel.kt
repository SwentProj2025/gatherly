package com.android.gatherly.ui.map

import androidx.lifecycle.ViewModel
import androidx.credentials.CredentialManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MapUIState(
    val errorMsg: String? = null,
    val signedOut: Boolean = false
)

class MapViewModel(
    //private val authRepository: AuthRepository = AuthRepositoryFirebase(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(MapUIState())
    val uiState: StateFlow<MapUIState> = _uiState.asStateFlow()


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

