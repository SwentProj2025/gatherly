package com.android.gatherly.screen.authentication

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.ui.authentication.SignInViewModel
import com.android.gatherly.utils.FakeCredentialManager
import com.android.gatherly.utils.FakeJwtGenerator
import com.android.gatherly.utils.FirebaseEmulator
import com.android.gatherly.utils.FirestoreGatherlyTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

const val WAIT_TIMEOUT = 5_000L

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
  }

  @Test
  fun canSignInWithGoogle() = runTest {
    val fakeGoogleIdToken =
        FakeJwtGenerator.createFakeGoogleIdToken("12345", email = "test@example.com")

    val fakeCredentialManager = FakeCredentialManager.create(fakeGoogleIdToken)

    val context = ApplicationProvider.getApplicationContext<Context>()

    signInViewModel = SignInViewModel(profileRepository = profileRepository)
    val userSignedIn = signInViewModel.uiState

    signInViewModel.signInWithGoogle(context, fakeCredentialManager)

    withContext(Dispatchers.Default.limitedParallelism(1)) {
      withTimeout(5000L) {
        while (!signInViewModel.uiState.value) {
          delay(50)
        }
      }
    }

    assert(userSignedIn.value)
    assert(FirebaseEmulator.auth.currentUser != null)
    assert(!profileRepository.initProfileIfMissing(FirebaseEmulator.auth.currentUser?.uid!!, ""))
  }

  @Test
  fun canSignInAnonymously() = runTest {
    signInViewModel = SignInViewModel(profileRepository = profileRepository)
    val userSignedIn = signInViewModel.uiState

    signInViewModel.signInAnonymously()

    withContext(Dispatchers.Default.limitedParallelism(1)) {
      withTimeout(5000L) {
        while (!signInViewModel.uiState.value) {
          delay(50)
        }
      }
    }

    assert(userSignedIn.value)
    assert(FirebaseEmulator.auth.currentUser != null)
    assert(!profileRepository.initProfileIfMissing(FirebaseEmulator.auth.currentUser?.uid!!, ""))
  }
}
