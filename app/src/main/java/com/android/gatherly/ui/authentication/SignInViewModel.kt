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
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SignInViewModel(
    private val profileRepository: ProfileRepository =
        ProfileRepositoryFirestore(Firebase.firestore)
) : ViewModel() {
  // UI State containing the user sign in status
  private val _uiState = MutableStateFlow(false)

  // Read-only UI State presented to the UI
  val uiState: StateFlow<Boolean>
    get() = _uiState

  /** Authenticate to Firebase */
  private fun authenticateFirebaseWithGoogle(credential: Credential) {
    viewModelScope.launch {
      // Check that this is the right type of credential
      if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        // Create idToken
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val idToken = googleIdTokenCredential.idToken

        // Authenticate to Firebase
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
        Firebase.auth.signInWithCredential(firebaseCredential).await()
        val bool = profileRepository.initProfileIfMissing(Firebase.auth.currentUser?.uid!!, "")
        _uiState.value = true
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
    Firebase.auth
        .signInAnonymously()
        .addOnSuccessListener {
          Log.d("Firebase anonymous authentication", "Successful authentication")
          viewModelScope.launch {
            val bool =
                profileRepository.initProfileIfMissing(Firebase.auth.currentUser?.uid!!, "default")
            _uiState.value = true
          }
        }
        .addOnFailureListener {
          Log.e("Firebase anonymous authentication", "Failed to authenticate Firebase credentials")
        }
  }
}
