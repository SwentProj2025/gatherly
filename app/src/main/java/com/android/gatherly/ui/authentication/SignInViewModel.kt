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
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Represents the UI state for authentication.
 *
 * @property isLoading Whether an authentication operation is in progress.
 * @property user The currently signed-in [FirebaseUser], or null if not signed in.
 * @property errorMsg An error message to display, or null if there is no error.
 * @property signedOut True if a sign-out operation has completed.
 */

/*
data class AuthUIState(
    val isLoading: Boolean = false,
    val user: FirebaseUser? = null,
    val errorMsg: String? = null,
    val signedOut: Boolean = false
)

data class AuthUIState(val isAnonym: Boolean = false, val isGoogle: Boolean = false)

 */

class SignInViewModel : ViewModel() {
  // UI State containing the user sign in status
  private val _uiState = MutableStateFlow<Boolean>(false)

  // Read-only UI State presented to the UI
  val uiState: StateFlow<Boolean>
    get() = _uiState
  //private val _uiState = MutableStateFlow(AuthUIState())
  //val uiState: StateFlow<AuthUIState> = _uiState

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
        Firebase.auth
            .signInWithCredential(firebaseCredential)
            .addOnSuccessListener {
                //_uiState.update { it.copy(isGoogle = true) }
              Log.d("Firebase authentication with Google", "Successful authentication")
              /*_uiState.update {
                it.copy(
                    isLoading = false,
                    errorMsg = null,
                    signedOut = false,
                    user = Firebase.auth.currentUser)
              }*/

            }
            .addOnFailureListener {
              Log.e(
                  "Firebase authentication with Google",
                  "Failed to authenticate Firebase credentials")
              /*_uiState.update {
                it.copy(
                    isLoading = false, errorMsg = "Failed to SignIn", signedOut = true, user = null)
              }*/
            }
      } else {
        Log.e("Google credentials", "Failed to recognize Google credentials")
        /*_uiState.update {
          it.copy(
              isLoading = false,
              errorMsg = "Failed to recognize Google credentials",
              signedOut = true,
              user = null)
        }*/

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
      //viewModelScope.launch {
      // _uiState.update { it.copy(isLoading = true, errorMsg = null) }

      //Log.e("Function signInAnonymously", "IS IN ")
      Firebase.auth
          .signInAnonymously()
          .addOnSuccessListener {
           // _uiState.update { it.copy(isAnonym = true) }

            //Log.e("Firebase anonymous authentication", "Successful authentication")
            /*_uiState.update {
              it.copy(
                  isLoading = false,
                  errorMsg = null,
                  signedOut = false,
                  user = Firebase.auth.currentUser)
            }*/
          }
          .addOnFailureListener {
            Log.e(
                "Firebase anonymous authentication", "Failed to authenticate Firebase credentials")
            /*_uiState.update {
              it.copy(
                  isLoading = false, errorMsg = "Failed to SignIn", signedOut = true, user = null)
            }*/
          }
    //}
  }
  /*
   fun clearErrorMsg() {
     _uiState.update { it.copy(errorMsg = null) }
   }
  */
}
