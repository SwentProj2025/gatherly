package com.android.gatherly.ui.settings

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.gatherly.R
import com.android.gatherly.model.event.EventsRepository
import com.android.gatherly.model.event.EventsRepositoryProvider
import com.android.gatherly.model.focusSession.FocusSessionsRepository
import com.android.gatherly.model.focusSession.FocusSessionsRepositoryProvider
import com.android.gatherly.model.group.GroupsRepository
import com.android.gatherly.model.group.GroupsRepositoryProvider
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.profile.ProfileRepositoryProvider
import com.android.gatherly.model.profile.ProfileStatus
import com.android.gatherly.model.profile.UserStatusManager
import com.android.gatherly.model.profile.UserStatusSource
import com.android.gatherly.model.profile.Username
import com.android.gatherly.model.todo.ToDosRepository
import com.android.gatherly.model.todo.ToDosRepositoryProvider
import com.android.gatherly.utils.DateParser
import com.android.gatherly.utils.DeleteUserAccountUseCase
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Portions of the code in this file are adapted from the bootcamp solution provided by Swent staff

/**
 * UI state backing the Settings screen. This state holds the data needed to display and edit a
 * Profile
 *
 * @property signedOut True when the user has signed out.
 * @property name Display name of the user.
 * @property username Unique username chosen by the user.
 * @property school Optional school name.
 * @property schoolYear Optional school year.
 * @property profilePictureUrl URL of the user's profile picture.
 * @property birthday User birthday formatted as dd/MM/yyyy.
 * @property errorMsg Generic error message to display as a toast.
 * @property invalidNameMsg Validation error for the name field.
 * @property invalidUsernameMsg Validation error for the username field.
 * @property invalidBirthdayMsg Validation error for the birthday field.
 * @property isUsernameAvailable Result of username availability check.
 * @property isLoadingProfile True while the profile is being loaded.
 * @property saveSuccess True when profile save completes successfully.
 * @property navigateToInit Triggers navigation to initial profile setup.
 * @property isAnon True if the current user is anonymous.
 * @property isSaving True while a save operation is in progress.
 * @property currentUserStatus Current presence/status of the user.
 * @property bio Optional user biography text.
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
    val saveSuccess: Boolean = false,
    val navigateToInit: Boolean = false,
    val isAnon: Boolean = true,
    val isSaving: Boolean = false,
    val currentUserStatus: ProfileStatus = ProfileStatus.OFFLINE,
    val bio: String = ""
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
 * ViewModel responsible for managing Settings screen state and profile updates.
 *
 * @param profileRepository Repository used for reading and writing profile data.
 * @param authProvider Provider for FirebaseAuth instance.
 * @param userStatusManager Manages user presence/status updates.
 * @param groupsRepository Repository for managing groups the user belongs to.
 * @param eventsRepository Repository for managing user events.
 * @param focusSessionsRepository Repository for managing user focus sessions.
 * @param todosRepository Repository for managing user to-dos.
 */
class SettingsViewModel(
    private val profileRepository: ProfileRepository = ProfileRepositoryProvider.repository,
    private val authProvider: () -> FirebaseAuth = { Firebase.auth },
    private val userStatusManager: UserStatusManager =
        UserStatusManager(authProvider(), profileRepository),
    private val groupsRepository: GroupsRepository = GroupsRepositoryProvider.repository,
    private val eventsRepository: EventsRepository = EventsRepositoryProvider.repository,
    private val focusSessionsRepository: FocusSessionsRepository =
        FocusSessionsRepositoryProvider.repository,
    private val todosRepository: ToDosRepository = ToDosRepositoryProvider.repository,
) : ViewModel() {
  private val _uiState = MutableStateFlow(SettingsUiState())
  /** Observable UI state exposed to the Settings screen. */
  val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

  /**
   * Use case responsible for deleting all user data associated with a given account, including
   * profile, groups, events, focus sessions, and to-dos.
   */
  private val deleteUserAccountUseCase =
      DeleteUserAccountUseCase(
          profileRepository = profileRepository,
          groupsRepository = groupsRepository,
          eventsRepository = eventsRepository,
          focusSessionsRepository = focusSessionsRepository,
          todosRepository = todosRepository)

  private var originalProfile: Profile? = null

  /**
   * Signs the user out of Firebase and clears credential state.
   *
   * Also sets the user's status to OFFLINE and updates UI state.
   *
   * @param credentialManager CredentialManager used to clear stored credentials.
   */
  fun signOut(credentialManager: CredentialManager) {
    viewModelScope.launch {
      userStatusManager.setStatus(ProfileStatus.OFFLINE)
      _uiState.value = _uiState.value.copy(signedOut = true)
      authProvider().signOut()
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
    loadProfile(authProvider().currentUser?.uid ?: "")
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
        val profile =
            profileRepository.getProfileByUid(profileUID) ?: Profile(uid = profileUID, name = "")
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
                isLoadingProfile = false,
                isAnon = authProvider().currentUser?.isAnonymous ?: true,
                bio = profile.bio,
                currentUserStatus = profile.status)
      } catch (e: Exception) {
        Log.e("SettingsViewModel", "Error loading Profile by uid: $profileUID", e)
        setErrorMsg("Failed to load Profile: ${e.message}")
      }
    }
  }

  /**
   * Persists changes made to the user's profile.
   *
   * Validates input, checks username availability, uploads profile picture if changed, and updates
   * the profile in the repository.
   *
   * @param id UID of the profile to update.
   * @param isFirstTime True if this is the user's initial profile setup.
   */
  fun updateProfile(id: String = authProvider().currentUser?.uid!!, isFirstTime: Boolean) {
    _uiState.value = _uiState.value.copy(isSaving = true)
    val state = _uiState.value
    if (!state.isValid) {
      setErrorMsg("At least one field is not valid.")
      _uiState.value = _uiState.value.copy(isSaving = false)
      return
    }

    val birthdayDate = DateParser.parse(state.birthday)
    val originalP =
        originalProfile
            ?: run {
              setErrorMsg("Original profile not loaded.")
              _uiState.value = _uiState.value.copy(isSaving = false)
              return
            }

    viewModelScope.launch {
      try {
        if (!checkUsernameSuccess(state, id, isFirstTime)) {
          setErrorMsg("Username is invalid or already taken.")
          _uiState.value = _uiState.value.copy(isSaving = false)
          return@launch
        }

        val newProfilePictureUrl =
            if (state.profilePictureUrl.isNotBlank() &&
                state.profilePictureUrl != originalProfile?.profilePicture) {
              profileRepository.updateProfilePic(id, state.profilePictureUrl.toUri())
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
                birthday = birthdayDate?.let { Timestamp(it) },
                bio = state.bio,
            )

        profileRepository.updateProfile(updatedProfile)
        clearErrorMsg()
        _uiState.value = _uiState.value.copy(saveSuccess = true, isSaving = false)
      } catch (e: Exception) {
        Log.e("SettingsViewModel", "Error saving profile", e)
        setErrorMsg("Failed to save profile: ${e.message}")
        _uiState.value = _uiState.value.copy(isSaving = false)
      }
    }
  }

  /**
   * Updates the name field and verifies the name is not left blank. (If so an error message is set)
   */
  fun editName(newName: String) {
    _uiState.value =
        _uiState.value.copy(
            name = newName,
            invalidNameMsg = if (newName.isBlank()) "Name cannot be empty" else null)
  }

  /** Updates the school field. */
  fun editSchool(newSchool: String) {
    _uiState.value = _uiState.value.copy(school = newSchool)
  }

  /** Updates the school year field. */
  fun editSchoolYear(newSchoolYear: String) {
    _uiState.value = _uiState.value.copy(schoolYear = newSchoolYear)
  }

  /** Updates the birthday field and validates date format. */
  fun editBirthday(newBirthday: String) {
    _uiState.value =
        _uiState.value.copy(
            birthday = newBirthday,
            invalidBirthdayMsg =
                if (newBirthday.isNotBlank() && DateParser.parse(newBirthday) == null)
                    "Date is not valid (format: dd/mm/yyyy)"
                else null)
  }

  /** Updates the username and triggers availability check if valid. */
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

  /** Updates the profile picture URL. */
  fun editProfilePictureUrl(newPhotoUrl: String) {
    _uiState.value = _uiState.value.copy(profilePictureUrl = newPhotoUrl)
  }

  /**
   * Links the current anonymous account with a Google account.
   *
   * Preserves existing user data and initializes a profile if missing.
   *
   * @param context Android context used to resolve resources.
   * @param credentialManager CredentialManager used for Google sign-in.
   */
  fun upgradeWithGoogle(context: Context, credentialManager: CredentialManager) {
    viewModelScope.launch {
      try {
        val signInWithGoogleOption =
            GetSignInWithGoogleOption.Builder(
                    serverClientId = context.getString(R.string.web_client_id))
                .build()

        val request =
            GetCredentialRequest.Builder().addCredentialOption(signInWithGoogleOption).build()

        val result = credentialManager.getCredential(request = request, context = context)

        if (result.credential is CustomCredential &&
            result.credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
          try {
            // Create idToken
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
            val idToken = googleIdTokenCredential.idToken

            // Authenticate to Firebase
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            Firebase.auth.currentUser!!.linkWithCredential(firebaseCredential).await()
            val uid = Firebase.auth.currentUser?.uid ?: return@launch

            // Initialize profile in profileRepository
            profileRepository.initProfileIfMissing(uid, "")

            // Navigate to init profile
            _uiState.value = _uiState.value.copy(navigateToInit = true)
          } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(errorMsg = "Google sign-in failed")
            Log.e("SignInViewModel", "Google sign-in failed", e)
          }
        } else {
          _uiState.value = _uiState.value.copy(errorMsg = "Failed to recognize Google credentials")
          Log.e("Google credentials", "Failed to recognize Google credentials")
        }
      } catch (e: NoCredentialException) {
        _uiState.value = _uiState.value.copy(errorMsg = "No Google credentials")
        Log.e("Google authentication", e.message.orEmpty())
      } catch (e: GetCredentialException) {
        _uiState.value = _uiState.value.copy(errorMsg = "Failed to get Google credentials")
        Log.e("Google authentication", e.message.orEmpty())
      }
    }
  }

  /**
   * Checks whether a username is available and updates validation state.
   *
   * @param username Normalized username to verify.
   */
  private fun checkUsernameAvailability(username: String) {
    viewModelScope.launch {
      try {
        if (username == originalProfile?.username) {
          _uiState.value.copy(isUsernameAvailable = true, invalidUsernameMsg = null)
          return@launch
        }
        val available = profileRepository.isUsernameAvailable(username)
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

  /**
   * Checks the validity of a user's chosen username, true if the user can change to the new
   * username, false otherwise
   *
   * @param state the state of the UI when the user pressed save
   * @param id the users uid
   * @param isFirstTime true if the user is new
   */
  private suspend fun checkUsernameSuccess(
      state: SettingsUiState,
      id: String,
      isFirstTime: Boolean
  ): Boolean {
    val usernameChanged = state.username != originalProfile?.username
    return if (isFirstTime) {
      profileRepository.registerUsername(id, state.username)
    } else if (!usernameChanged) {
      true
    } else {
      profileRepository.updateUsername(id, originalProfile?.username, state.username)
    }
  }

  /**
   * Updates the user's status both locally and in the backend.
   * - Immediately updates the UI state with the new status.
   * - Asynchronously notifies the UserStatusManager to persist the change.
   * - Clicking on ONLINE reset status behaviour to auto
   *
   * @param status The new ProfileStatus selected by the user.
   */
  fun updateUserStatus(status: ProfileStatus) {
    viewModelScope.launch {
      val resetToAuto = status == ProfileStatus.ONLINE // reset to auto
      userStatusManager.setStatus(
          status = status, source = UserStatusSource.MANUAL, resetToAuto = resetToAuto)
    }
    _uiState.value = _uiState.value.copy(currentUserStatus = status)
  }

  /** Updates the bio field. */
  fun editBio(newBio: String) {
    _uiState.value = _uiState.value.copy(bio = newBio)
  }

  /**
   * Deletes the current user's account and all associated data.
   *
   * On success, emits an account-deleted event. On failure, exposes an error message.
   *
   * @param credentialManager CredentialManager used to clear stored credentials.
   */
  fun deleteProfile(credentialManager: CredentialManager) {
    val uid = authProvider().currentUser?.uid ?: return
    viewModelScope.launch {
      try {
        deleteUserAccountUseCase.deleteUserAccount(uid)
        signOutAfterDeletion(credentialManager)
      } catch (e: Exception) {
        Log.e("SettingsViewModel", "Failed to delete profile", e)
        _uiState.value =
            _uiState.value.copy(errorMsg = "Failed to delete profile. Please try again.")
      }
    }
  }

  /**
   * Signs out the user after their account was deleted.
   *
   * @param credentialManager CredentialManager used to clear stored credentials.
   */
  private fun signOutAfterDeletion(credentialManager: CredentialManager) {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(signedOut = true)
      authProvider().signOut()
      credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
  }
}
