package com.android.gatherly.ui.settings

import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.profile.ProfileRepositoryProvider
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

data class SettingsUiState(
    val signedOut: Boolean = false,
    val name: String = "",
    val school: String = "",
    val schoolYear: String = "",
    val profilePictureUrl: String = "",
    val birthday: String = "",
    val errorMsg: String? = null,
    val invalidNameMsg: String? = null,
    val invalidSchoolMsg: String? = null,
    val invalidSchoolYearMsg: String? = null,
    val invalidPhotoUrlMsg: String? = null,
    val invalidBirthdayMsg: String? = null,
) {
  val isValid: Boolean
    get() =
        invalidNameMsg == null &&
            invalidSchoolMsg == null &&
            invalidBirthdayMsg == null &&
            invalidSchoolYearMsg == null &&
            invalidPhotoUrlMsg == null &&
            name.isNotEmpty() // todo quick something else ?
}
/**
 * ViewModel for the Settings screen. This ViewModel manages the state of input fields for the
 * Settings screen.
 */
class SettingsViewModel(
    private val repository: ProfileRepository = ProfileRepositoryProvider.repository,
) : ViewModel() {
  private val _uiState = MutableStateFlow(SettingsUiState())
  val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

  /** Initiates sign-out */
  fun signOut(credentialManager: CredentialManager): Unit {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(signedOut = true)
      Firebase.auth.signOut()
      credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
  }
  /** Clears the error message in the UI state. */
  // todo check errorMsg
  fun clearErrorMsg() {
    _uiState.value = _uiState.value.copy(errorMsg = null)
  }

  /** Sets an error message in the UI state. */
  private fun setErrorMsg(errorMsg: String) {
    _uiState.value = _uiState.value.copy(errorMsg = errorMsg)
  }

  /**
   * Loads a Profile by its ID and updates the UI state.
   *
   * @param profileUID The ID of the profile to be loaded.
   */
  fun loadProfile(profileUID: String) {
    viewModelScope.launch {
      try {
        val profile = repository.getProfileByUid(profileUID)
        if (profile == null) { // todo verify this
          setErrorMsg("Profile not found")
          return@launch
        } else {
          _uiState.value =
              SettingsUiState(
                  name = profile.name,
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
              )
        }
      } catch (e: Exception) {
        Log.e("SettingsViewModel", "Error loading ToDo by ID: $profileUID", e)
        setErrorMsg("Failed to load Profile: ${e.message}")
      }
    }
  }

  /**
   * Updates a Profile document.
   *
   * @param id The id of the Profile to be updated.
   */
  fun updateProfile(id: String): Boolean {
    val state = _uiState.value
    if (!state.isValid) {
      setErrorMsg("At least one field is not valid")
      return false
    }
    val birthdayDate = DateParser.parse(state.birthday)

    val uid =
        Firebase.auth.currentUser?.uid
            ?: "No user is currently logged in." // TOdo handle anonymous user + how are profile and
    // ID linked ?

    updateProfileInRepository(
        Profile(
            uid = id,
            name = state.name,
            school = state.school,
            schoolYear = state.schoolYear,
            profilePicture = state.profilePictureUrl,
            birthday = if (birthdayDate == null) null else Timestamp(birthdayDate)))
    clearErrorMsg()
    return true
  }

  /**
   * Updates a Profile document in the repository.
   *
   * @param profile The Profile object containing the new values.
   */
  private fun updateProfileInRepository(profile: Profile) {
    viewModelScope.launch {
      try {
        repository.updateProfile(profile = profile)
      } catch (e: Exception) {
        Log.e("SettingsViewModel", "Error updating profile", e)
        setErrorMsg("Failed to update profile: ${e.message}")
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
    _uiState.value =
        _uiState.value.copy(
            school = newSchool,
            invalidSchoolMsg = if (newSchool.isBlank()) "Name cannot be empty" else null)
  }

  fun editSchoolYear(newSchoolYear: String) {
    _uiState.value =
        _uiState.value.copy(
            schoolYear = newSchoolYear,
            invalidSchoolYearMsg =
                if (newSchoolYear.isBlank()) "School year cannot be empty" else null)
  }

  fun editPhoto(newPhotoUrl: String) {} // TOdo get Photo url viewModel or UI ?

  fun editBirthday(newBirthday: String) {
    _uiState.value =
        _uiState.value.copy(
            birthday = newBirthday,
            invalidBirthdayMsg =
                if (DateParser.parse(newBirthday) == null) "Date is not valid (format: dd/mm/yyyy)"
                else null)
  }
}
