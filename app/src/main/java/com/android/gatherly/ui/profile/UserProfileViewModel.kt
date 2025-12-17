package com.android.gatherly.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.profile.ProfileRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** UI state for [UserProfileViewModel] */
data class UserProfileState(
    val isLoading: Boolean = true,
    val profile: Profile? = null,
    val errorMessage: String? = null
)

/**
 * ViewModel for displaying another user's profile in the app. Fetches the profile from
 * [ProfileRepository] by uid and exposes loading/error state.
 *
 * @param repository The profile repository to fetch profiles from. Defaults to the provided
 *   [ProfileRepositoryProvider.repository].
 */
class UserProfileViewModel(
    private val repository: ProfileRepository = ProfileRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(UserProfileState())
  val uiState: StateFlow<UserProfileState> = _uiState

  /**
   * Sets an error message in the UI state and stops loading.
   *
   * @param msg The error message to set.
   */
  private fun setErrorMsg(msg: String?) {
    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = msg)
  }

  /** Clears the error message in the UI state. */
  fun clearErrorMsg() {
    _uiState.value = _uiState.value.copy(errorMessage = null)
  }

  /**
   * Loads the profile with the given [uid]. Updates [uiState] with the loaded profile or an error
   * message.
   *
   * @param uid The UID of the user whose profile to load.
   */
  fun loadUserProfile(uid: String) {
    viewModelScope.launch {
      _uiState.value = UserProfileState(isLoading = true)

      try {
        val profile = repository.getProfileByUid(uid)
        if (profile == null) {
          setErrorMsg("Error : Profile not found. Try quitting and coming back to the screen.")
        } else {
          _uiState.value = UserProfileState(isLoading = false, profile = profile)
        }
      } catch (e: Exception) {
        Log.e("UserProfileViewModel", "Error loading profile", e)
        setErrorMsg("Error while loading Profile. Try quitting and coming back to the screen.")
      }
    }
  }
}
