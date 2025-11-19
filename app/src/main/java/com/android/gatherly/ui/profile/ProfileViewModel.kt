package com.android.gatherly.ui.profile

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.gatherly.R
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.profile.ProfileRepositoryProvider
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/** Represents the UI state of the Profile screen. */
data class ProfileState(
    val isLoading: Boolean = false,
    val profile: Profile? = null,
    val focusPoints: Int = 0,
    val errorMessage: String? = null,
    val signedOut: Boolean = false,
    val navigateToInit: Boolean = false,
    val isAnon: Boolean = true
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
    private val repository: ProfileRepository = ProfileRepositoryProvider.repository,
    private val authProvider: () -> FirebaseAuth = { Firebase.auth }
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

      val user = authProvider().currentUser
      if (user == null) {
        _uiState.value =
            _uiState.value.copy(isLoading = false, errorMessage = "User not authenticated")
        return@launch
      }

      _uiState.value = _uiState.value.copy(isAnon = authProvider().currentUser?.isAnonymous ?: true)

      try {
        val profile = repository.getProfileByUid(authProvider().currentUser?.uid!!)
        if (profile == null) {
          _uiState.value =
              _uiState.value.copy(isLoading = false, errorMessage = "Profile not found")
        } else {
          _uiState.value = _uiState.value.copy(isLoading = false, profile = profile)
        }
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
      }
    }
  }

  /**
   * Upgrades the user's current account to a Google account. The user keeps all of their current
   * data
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
            repository.initProfileIfMissing(uid, "")

            // Navigate to init profile
            _uiState.value = _uiState.value.copy(navigateToInit = true)
          } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(errorMessage = "Google sign-in failed")
            Log.e("SignInViewModel", "Google sign-in failed", e)
          }
        } else {
          _uiState.value =
              _uiState.value.copy(errorMessage = "Failed to recognize Google credentials")
          Log.e("Google credentials", "Failed to recognize Google credentials")
        }
      } catch (e: NoCredentialException) {
        _uiState.value = _uiState.value.copy(errorMessage = "No Google credentials")
        Log.e("Google authentication", e.message.orEmpty())
      } catch (e: GetCredentialException) {
        _uiState.value = _uiState.value.copy(errorMessage = "Failed to get Google credentials")
        Log.e("Google authentication", e.message.orEmpty())
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
