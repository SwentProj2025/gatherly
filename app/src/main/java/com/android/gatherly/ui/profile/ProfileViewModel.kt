package com.android.gatherly.ui.profile

import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.profile.ProfileRepositoryFirestore
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileState(
    val isLoading: Boolean = false,
    val profile: Profile? = null,
    val focusHoursPerDay: Map<LocalDate, Double> = emptyMap(),
    val focusPoints: Int = 0,
    val error: String? = null,
    val signedOut: Boolean = false
)

class ProfileViewModel(
    private val repository: ProfileRepository = ProfileRepositoryFirestore()
    // Will also need the focus sessions repository when it is available
) : ViewModel() {
  private val _uiState = MutableStateFlow(ProfileState())
  val uiState: StateFlow<ProfileState> = _uiState

  fun loadProfile(username: String) {
    viewModelScope.launch {
      val profile = repository.getProfileByUid(username)
      val focusHoursPerDay: Map<LocalDate, Double> = emptyMap()
      val focusPoints = 0
      _uiState.value =
          ProfileState(
              isLoading = false,
              profile = profile,
              focusHoursPerDay = focusHoursPerDay,
              focusPoints = focusPoints)
    }
  }

  /** Initiates sign-out */
  fun signOut(credentialManager: CredentialManager): Unit {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(signedOut = true)
      Firebase.auth.signOut()
      credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
  }
}
