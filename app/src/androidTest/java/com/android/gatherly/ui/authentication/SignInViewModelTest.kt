package com.android.gatherly.ui.authentication

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.profile.ProfileStatus
import com.android.gatherly.utils.FakeCredentialManager
import com.android.gatherly.utils.FakeJwtGenerator
import com.android.gatherly.utils.FirebaseEmulator
import com.android.gatherly.utils.FirestoreGatherlyTest
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class SignInViewModelTest : FirestoreGatherlyTest() {

  private lateinit var signInViewModel: SignInViewModel
  private val profileRepository: ProfileRepository = ProfileLocalRepository()

  // set up then sign out of the firebase
  @Before
  override fun setUp() {
    super.setUp()
    FirebaseEmulator.auth.signOut()
    signInViewModel = SignInViewModel(profileRepository)
  }

  /** Sign in with google is done correctly, and then clearing the UI State works as well */
  @Test
  fun canSignInWithGoogle_destinationInitProfileIfFirst() = runTest {
    val fakeGoogleIdToken =
        FakeJwtGenerator.createFakeGoogleIdToken("12345", email = "test@example.com")

    val fakeCredentialManager = FakeCredentialManager.create(fakeGoogleIdToken)

    val context = ApplicationProvider.getApplicationContext<Context>()

    signInViewModel = SignInViewModel(profileRepository = profileRepository)

    signInViewModel.signInWithGoogle(context, fakeCredentialManager)

    // Check that the screen is loading
    assert(signInViewModel.uiState.isLoading)

    // Wait for the user to be signed in
    waitForUserSignIn()

    // Check the loading, firebase user, destination screen and signedIn
    assert(!signInViewModel.uiState.isLoading)
    assert(FirebaseEmulator.auth.currentUser != null)
    assert(!profileRepository.initProfileIfMissing(FirebaseEmulator.auth.currentUser?.uid!!, ""))
    assert(signInViewModel.uiState.destinationScreen == "init_profile")
    assert(signInViewModel.uiState.signedIn)

    // Reset the UI state
    signInViewModel.resetAuth()

    // Check that it is correctly cleared
    assert(!signInViewModel.uiState.isLoading)
    assert(!signInViewModel.uiState.signedIn)
    assert(signInViewModel.uiState.destinationScreen == null)
    assert(signInViewModel.uiState.errorMessage == null)
  }

  /**
   * Checks that signing into google with no credentials sets the error message and that clearing it
   * works
   */
  @Test
  fun signInWithNoGoogleErrorMessage() = runTest {
    // Sign in with no creds
    val context = ApplicationProvider.getApplicationContext<Context>()
    signInViewModel = SignInViewModel(profileRepository = profileRepository)
    signInViewModel.signInWithGoogle(context, CredentialManager.create(context))

    // Check that the screen is loading
    assert(signInViewModel.uiState.isLoading)

    // Wait for the error message to appear
    withContext(Dispatchers.Default.limitedParallelism(1)) {
      withTimeout(5000L) {
        while (signInViewModel.uiState.errorMessage == null) {
          delay(50)
        }
      }
    }

    // Check that there is an error message ans the screen is no longer loading
    assert(signInViewModel.uiState.errorMessage != null)
    assert(!signInViewModel.uiState.isLoading)

    // Clear error message and check that it is cleared
    signInViewModel.resetErrorMessage()

    assert(signInViewModel.uiState.errorMessage == null)
  }

  @Test
  fun canSignInAnonymously_destinationInitProfileIfFirst() = runTest {
    signInViewModel = SignInViewModel(profileRepository = profileRepository)

    signInViewModel.signInAnonymously()

    waitForUserSignIn()

    assert(signInViewModel.uiState.signedIn)
    assert(FirebaseEmulator.auth.currentUser != null)
    assert(!profileRepository.initProfileIfMissing(FirebaseEmulator.auth.currentUser?.uid!!, ""))
    assert(signInViewModel.uiState.destinationScreen == "home")
  }

  /** Test that after Google sign-in, the user's profile status is updated to ONLINE. */
  @Test
  fun googleSignIn_updatesStatusOnline() = runTest {
    // Use fake Google sign-in for emulator
    val fakeGoogleIdToken = FakeJwtGenerator.createFakeGoogleIdToken("12345", "test@example.com")
    val fakeCredentialManager = FakeCredentialManager.create(fakeGoogleIdToken)
    val context = ApplicationProvider.getApplicationContext<Context>()

    signInViewModel.signInWithGoogle(context, fakeCredentialManager)

    // Wait for coroutine to finish
    waitForUserSignIn()

    val uid = FirebaseEmulator.auth.currentUser!!.uid
    val status = profileRepository.getProfileByUid(uid)?.status
    assertEquals(ProfileStatus.ONLINE, status)
  }

  /** Test that after anonymous sign-in, the user's profile status is updated to ONLINE. */
  @Test
  fun anonymousSignIn_updatesStatusOnline() = runTest {
    signInViewModel.signInAnonymously()

    waitForUserSignIn()

    val uid = FirebaseEmulator.auth.currentUser!!.uid
    val status = profileRepository.getProfileByUid(uid)?.status
    assertEquals(ProfileStatus.ONLINE, status)
  }

  fun waitForUserSignIn() = runTest {
    withContext(Dispatchers.Default.limitedParallelism(1)) {
      while (!signInViewModel.uiState.signedIn) delay(50)
    }
  }
}
