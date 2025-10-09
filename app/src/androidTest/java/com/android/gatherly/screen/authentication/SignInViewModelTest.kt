package com.android.gatherly.screen.authentication

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.gatherly.ui.authentication.SignInViewModel
import com.android.gatherly.utils.FakeCredentialManager
import com.android.gatherly.utils.FakeJwtGenerator
import com.android.gatherly.utils.FirebaseEmulator
import com.android.gatherly.utils.FirestoreGatherlyTest
import java.lang.Thread.sleep
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

const val WAIT_TIMEOUT = 5_000L

@RunWith(AndroidJUnit4::class)
class SignInViewModelTest : FirestoreGatherlyTest() {

  private lateinit var signInViewModel: SignInViewModel

  // set up then sign out of the firebase
  @Before
  override fun setUp() {
    super.setUp()
    FirebaseEmulator.auth.signOut()
  }

  @Test
  fun canSignInWithGoogle() {
    val fakeGoogleIdToken =
        FakeJwtGenerator.createFakeGoogleIdToken("12345", email = "test@example.com")

    val fakeCredentialManager = FakeCredentialManager.create(fakeGoogleIdToken)

    val context = ApplicationProvider.getApplicationContext<Context>()

    signInViewModel = SignInViewModel()
    val userSignedIn = signInViewModel.uiState

    signInViewModel.signInWithGoogle(context, fakeCredentialManager)

    sleep(WAIT_TIMEOUT)

    assert(userSignedIn.value)
    assert(FirebaseEmulator.auth.currentUser != null)
  }

  @Test
  fun canSignInAnonymously() {
    signInViewModel = SignInViewModel()
    val userSignedIn = signInViewModel.uiState

    signInViewModel.signInAnonymously()

    sleep(WAIT_TIMEOUT)

    assert(userSignedIn.value)
    assert(FirebaseEmulator.auth.currentUser != null)
  }
}
