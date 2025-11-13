package com.android.gatherly.ui.settings

import android.net.Uri
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.profile.ProfileRepositoryProvider
import com.android.gatherly.model.profile.Username
import com.android.gatherly.utils.DateParser
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Portions of the code in this file are adapted from the bootcamp solution provided by Swent staff

/**
 * UI state for the Settings screen. This state holds the data needed to display and edit a Profile
 *
 * @property isValid Returns true if all mandatory fields are valid.
 *     - Currently, only [name] is mandatory, birthday needs proper format if not empty.
 *     - Optional fields ([school], [schoolYear], [profilePictureUrl], [birthday]) do not block
 *       saving even if they are empty.
 */
data class SettingsUiState(
    val signedOut: Boolean = false,
    val name: String = "",
    val username: String = "",
    val school: String = "",
    val schoolYear: String = "",
    val profilePictureUrl: String = "",
    val birthday: String = "",
    val errorMsg: String? = null,
    val invalidNameMsg: String? = null,
    val invalidUsernameMsg: String? = null,
    val invalidBirthdayMsg: String? = null,
    val isUsernameAvailable: Boolean? = null,
    val isLoadingProfile: Boolean = false,
    val saveSuccess: Boolean = false
) {
  val isValid: Boolean
    get() =
        invalidNameMsg == null &&
            invalidBirthdayMsg == null &&
            invalidUsernameMsg == null &&
            name.isNotEmpty() &&
            username.isNotEmpty() &&
            (isUsernameAvailable != false)
}
/**
 * ViewModel for the Settings screen. This ViewModel manages the state of input fields for the
 * Settings screen.
 */
class SettingsViewModel(
    private val repository: ProfileRepository = ProfileRepositoryProvider.repository,
    private val currentUser: String = Firebase.auth.currentUser?.uid ?: ""
) : ViewModel() {
  private val _uiState = MutableStateFlow(SettingsUiState())
  val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

  private var originalProfile: Profile? = null

  /** Initiates sign-out */
  fun signOut(credentialManager: CredentialManager): Unit {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(signedOut = true)
      Firebase.auth.signOut()
      credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
  }
  /** Clears the error message in the UI state. */
  fun clearErrorMsg() {
    _uiState.value = _uiState.value.copy(errorMsg = null)
  }

  /** Clears the save success flag in the UI state. */
  fun clearSaveSuccess() {
    _uiState.value = _uiState.value.copy(saveSuccess = false)
  }

  /** Sets an error message in the UI state. */
  private fun setErrorMsg(errorMsg: String) {
    _uiState.value = _uiState.value.copy(errorMsg = errorMsg)
  }

  init {
    loadProfile(currentUser)
  }
  /**
   * Loads a Profile by its ID and updates the UI state.
   *
   * @param profileUID The ID of the profile to be loaded.
   */
  fun loadProfile(profileUID: String) {
    viewModelScope.launch {
      try {
        _uiState.value = _uiState.value.copy(isLoadingProfile = true)
        val profile = repository.getProfileByUid(profileUID) ?: Profile(uid = profileUID, name = "")
        originalProfile = profile
        _uiState.value =
            SettingsUiState(
                name = profile.name,
                username = profile.username,
                school = profile.school,
                schoolYear = profile.schoolYear,
                profilePictureUrl = profile.profilePicture,
                birthday =
                    profile.birthday.let {
                      val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                      return@let if (profile.birthday != null)
                          dateFormat.format(profile.birthday.toDate())
                      else ""
                    },
                isLoadingProfile = false)
      } catch (e: Exception) {
        Log.e("SettingsViewModel", "Error loading Profile by uid: $profileUID", e)
        setErrorMsg("Failed to load Profile: ${e.message}")
      }
    }
  }

  /**
   * Updates a Profile document.
   *
   * @param id The id of the Profile to be updated.
   */
  fun updateProfile(id: String = currentUser, isFirstTime: Boolean) {
    val state = _uiState.value
    if (!state.isValid) {
      setErrorMsg("At least one field is not valid.")
      return
    }

    val birthdayDate = DateParser.parse(state.birthday)
    val originalP =
        originalProfile
            ?: run {
              setErrorMsg("Original profile not loaded.")
              return
            }

    viewModelScope.launch {
      try {
        val usernameChanged = state.username != originalProfile?.username
        val usernameSuccess =
            if (isFirstTime) {
              repository.registerUsername(id, state.username)
            } else if (!usernameChanged) {
              true
            } else {
              repository.updateUsername(id, originalProfile?.username, state.username)
            }

        if (!usernameSuccess) {
          setErrorMsg("Username is invalid or already taken.")
          return@launch
        }

        val newProfilePictureUrl =
            if (state.profilePictureUrl.isNotBlank() &&
                state.profilePictureUrl != originalProfile?.profilePicture) {
              repository.updateProfilePic(id, Uri.parse(state.profilePictureUrl))
            } else {
              originalProfile?.profilePicture.orEmpty()
            }

        val updatedProfile =
            originalP.copy(
                uid = id,
                name = state.name,
                username = state.username,
                school = state.school,
                schoolYear = state.schoolYear,
                profilePicture = newProfilePictureUrl,
                birthday = birthdayDate?.let { Timestamp(it) })

        repository.updateProfile(updatedProfile)
        clearErrorMsg()
        _uiState.value = _uiState.value.copy(saveSuccess = true)
      } catch (e: Exception) {
        Log.e("SettingsViewModel", "Error saving profile", e)
        setErrorMsg("Failed to save profile: ${e.message}")
      }
    }
  }

  fun editName(newName: String) {
    _uiState.value =
        _uiState.value.copy(
            name = newName,
            invalidNameMsg = if (newName.isBlank()) "Name cannot be empty" else null)
  }

  fun editSchool(newSchool: String) {
    _uiState.value = _uiState.value.copy(school = newSchool)
  }

  fun editSchoolYear(newSchoolYear: String) {
    _uiState.value = _uiState.value.copy(schoolYear = newSchoolYear)
  }

  fun editPhoto(newPhotoUrl: String) {
    _uiState.value = _uiState.value.copy(profilePictureUrl = newPhotoUrl)
  }

  fun editBirthday(newBirthday: String) {
    _uiState.value =
        _uiState.value.copy(
            birthday = newBirthday,
            invalidBirthdayMsg =
                if (newBirthday.isNotBlank() && DateParser.parse(newBirthday) == null)
                    "Date is not valid (format: dd/mm/yyyy)"
                else null)
  }

  fun editUsername(newUsername: String) {
    val normalized = Username.normalize(newUsername)
    val validFormat = Username.isValid(normalized)
    _uiState.value =
        _uiState.value.copy(
            username = newUsername,
            invalidUsernameMsg =
                when {
                  newUsername.isBlank() -> "Username cannot be empty"
                  !validFormat -> "Invalid username format (3–20 chars, lowercase, ., -, _ allowed)"
                  else -> null
                },
            isUsernameAvailable = null)
    if (validFormat) {
      checkUsernameAvailability(normalized)
    }
  }

  fun editProfilePictureUrl(newPhotoUrl: String) {
    _uiState.value = _uiState.value.copy(profilePictureUrl = newPhotoUrl)
  }

  private fun checkUsernameAvailability(username: String) {
    viewModelScope.launch {
      try {
        if (username == originalProfile?.username) {
          _uiState.value.copy(isUsernameAvailable = true, invalidUsernameMsg = null)
          return@launch
        }
        val available = repository.isUsernameAvailable(username)
        _uiState.value =
            _uiState.value.copy(
                isUsernameAvailable = available,
                invalidUsernameMsg = if (!available) "This username is already taken" else null)
      } catch (e: Exception) {
        Log.e("SettingsViewModel", "Failed to check username", e)
        _uiState.value =
            _uiState.value.copy(invalidUsernameMsg = "Unable to verify username availability")
      }
    }
  }
}
