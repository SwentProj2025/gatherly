package com.android.gatherly.ui.profile

import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.profile.ProfileRepositoryProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Represents the UI state of the Profile screen. */
data class ProfileState(
    val isLoading: Boolean = false,
    val profile: Profile? = null,
    val errorMessage: String? = null,
    val signedOut: Boolean = false
)

/**
 * ViewModel responsible for fetching the authenticated user's [Profile].
 *
 * This ViewModel interacts with [ProfileRepository] to:
 * - Load the authenticated user's profile from Firestore.
 *
 * Handle user sign-out action.
 *
 * The UI observes [uiState] to react to updates in [Profile] data, loading, or errors.
 *
 * @param repository The [ProfileRepository] used to interact with Firestore.
 */
class ProfileViewModel(
    private val repository: ProfileRepository = ProfileRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(ProfileState())

  /** Exposes the immutable UI state of the Profile screen. */
  val uiState: StateFlow<ProfileState> = _uiState

  /**
   * Loads the currently authenticated user's [Profile] from Firestore.
   *
   * Assumes that a [Profile] already exists (it should be created at sign-in). If the [Profile] is
   * missing, sets an appropriate error state.
   */
  fun loadUserProfile() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

      val user = Firebase.auth.currentUser
      if (user == null) {
        _uiState.value =
            _uiState.value.copy(isLoading = false, errorMessage = "User not authenticated")
        return@launch
      }

      try {
        val profile = repository.getProfileByUid(user.uid)
        if (profile == null) {
          _uiState.value = ProfileState(isLoading = false, errorMessage = "Profile not found")
        } else {
          _uiState.value = ProfileState(isLoading = false, profile = profile)
        }
      } catch (e: Exception) {
        _uiState.value = ProfileState(isLoading = false, errorMessage = e.message)
      }
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
