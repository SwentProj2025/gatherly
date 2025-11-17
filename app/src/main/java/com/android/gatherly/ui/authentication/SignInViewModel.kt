package com.android.gatherly.ui.authentication

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.gatherly.R
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.profile.ProfileRepositoryFirestore
import com.android.gatherly.model.profile.ProfileStatus
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Represents the UI state for authentication.
 *
 * @property signedIn Whether the authentication was successful
 * @property destinationScreen The screen to navigate to upon successful authentication
 * @property isLoading Whether an authentication operation is in progress.
 */
data class SignInUIState(
    val signedIn: Boolean = false,
    val destinationScreen: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class SignInViewModel(
    private val profileRepository: ProfileRepository =
        ProfileRepositoryFirestore(Firebase.firestore, Firebase.storage)
) : ViewModel() {

  // State with a private set
  var uiState by mutableStateOf(SignInUIState())
    private set

  /**
   * Resets the state of the UI after successful authentication, so that the user can successfully
   * navigate back from init profile screen if wanted
   */
  fun resetAuth() {
    uiState = SignInUIState()
  }

  /** Once the error message is shown, reset it to null */
  fun resetErrorMessage() {
    uiState = uiState.copy(errorMessage = null)
  }

  /** Decide where to navigate after a successful sign in */
  private suspend fun handlePostSignInNav() {
    val uid = Firebase.auth.currentUser?.uid ?: return
    val profile = profileRepository.getProfileByUid(uid)

    uiState =
        uiState.copy(
            destinationScreen =
                if (profile?.username.isNullOrEmpty()) {
                  "init_profile"
                } else {
                  "home"
                })
  }

  /** Authenticate to Firebase */
  private fun authenticateFirebaseWithGoogle(credential: Credential) {
    viewModelScope.launch {
      // Check that this is the right type of credential
      if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        try {
          // Create idToken
          val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
          val idToken = googleIdTokenCredential.idToken

          // Authenticate to Firebase
          val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
          Firebase.auth.signInWithCredential(firebaseCredential).await()
          val uid = Firebase.auth.currentUser?.uid ?: return@launch
          profileRepository.initProfileIfMissing(uid, "")
          profileRepository.updateStatus(uid, ProfileStatus.ONLINE)
          handlePostSignInNav()
          uiState = uiState.copy(signedIn = true, isLoading = false)
        } catch (e: Exception) {
          uiState = uiState.copy(isLoading = false, errorMessage = "Google sign-in failed")
          Log.e("SignInViewModel", "Google sign-in failed", e)
        }
      } else {
        uiState =
            uiState.copy(isLoading = false, errorMessage = "Failed to recognize Google credentials")
        Log.e("Google credentials", "Failed to recognize Google credentials")
      }
    }
  }

  /** Sign in with Google */
  fun signInWithGoogle(context: Context, credentialManager: CredentialManager) {
    uiState = uiState.copy(isLoading = true)
    viewModelScope.launch {
      try {
        val signInWithGoogleOption =
            GetSignInWithGoogleOption.Builder(
                    serverClientId = context.getString(R.string.web_client_id))
                .build()

        val request =
            GetCredentialRequest.Builder().addCredentialOption(signInWithGoogleOption).build()

        val result = credentialManager.getCredential(request = request, context = context)

        authenticateFirebaseWithGoogle(result.credential)
      } catch (e: NoCredentialException) {
        uiState = uiState.copy(isLoading = false, errorMessage = "No Google credentials")
        Log.e("Google authentication", e.message.orEmpty())
      } catch (e: GetCredentialException) {
        uiState = uiState.copy(isLoading = false, errorMessage = "Failed to get Google credentials")
        Log.e("Google authentication", e.message.orEmpty())
      }
    }
  }

  /** Sign in anonymously */
  fun signInAnonymously() {
    uiState = uiState.copy(isLoading = true)
    viewModelScope.launch {
      try {
        Firebase.auth.signInAnonymously().await()
        val uid = Firebase.auth.currentUser?.uid ?: return@launch
        profileRepository.initProfileIfMissing(uid, "")
        profileRepository.updateStatus(uid, ProfileStatus.ONLINE)
        handlePostSignInNav()

        uiState = uiState.copy(signedIn = true, isLoading = false)
      } catch (e: Exception) {
        uiState = uiState.copy(isLoading = false, errorMessage = "Failed to log in")
        Log.e("SignInViewModel", "Anonymous sign-in failed", e)
      }
    }
  }
}
