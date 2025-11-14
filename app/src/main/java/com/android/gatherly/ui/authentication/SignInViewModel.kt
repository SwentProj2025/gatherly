package com.android.gatherly.ui.authentication

import android.content.Context
import android.util.Log
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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Represents the UI state for authentication.
 *
 * @property isLoading Whether an authentication operation is in progress.
 * @property user The currently signed-in [FirebaseUser], or null if not signed in.
 * @property errorMsg An error message to display, or null if there is no error.
 * @property signedOut True if a sign-out operation has completed.
 */
class SignInViewModel(
    private val profileRepository: ProfileRepository =
        ProfileRepositoryFirestore(Firebase.firestore, Firebase.storage)
) : ViewModel() {
  // UI State containing the user sign in status
  private val _uiState = MutableStateFlow<Boolean>(false)

  // Read-only UI State presented to the UI
  val uiState: StateFlow<Boolean>
    get() = _uiState

  private val _destination = MutableStateFlow<String?>(null)
  val destination: StateFlow<String?>
    get() = _destination

  /** Decide where to navigate after a successful sign in */
  private suspend fun handlePostSignInNav() {
    val uid = Firebase.auth.currentUser?.uid ?: return
    val profile = profileRepository.getProfileByUid(uid)

    _destination.value =
        if (profile?.username.isNullOrEmpty()) {
          "init_profile"
        } else {
          "home"
        }
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
          _uiState.value = true
        } catch (e: Exception) {
          Log.e("SignInViewModel", "Google sign-in failed", e)
        }
      } else {
        Log.e("Google credentials", "Failed to recognize Google credentials")
      }
    }
  }

  /** Sign in with Google */
  fun signInWithGoogle(context: Context, credentialManager: CredentialManager) {
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
        Log.e("Google authentication", e.message.orEmpty())
      } catch (e: GetCredentialException) {
        Log.e("Google authentication", e.message.orEmpty())
      }
    }
  }

  /** Sign in anonymously */
  fun signInAnonymously() {
    viewModelScope.launch {
      try {
        Firebase.auth.signInAnonymously().await()
        val uid = Firebase.auth.currentUser?.uid ?: return@launch
        profileRepository.initProfileIfMissing(uid, "")
        profileRepository.updateStatus(uid, ProfileStatus.ONLINE)
        handlePostSignInNav()

        _uiState.value = true
      } catch (e: Exception) {
        Log.e("SignInViewModel", "Anonymous sign-in failed", e)
      }
    }
  }
}
