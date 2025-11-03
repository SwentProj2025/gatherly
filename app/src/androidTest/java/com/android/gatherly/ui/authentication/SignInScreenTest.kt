package com.android.gatherly.ui.authentication

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.gatherly.utils.FakeCredentialManager
import com.android.gatherly.utils.FakeJwtGenerator
import com.android.gatherly.utils.FirebaseEmulator
import com.android.gatherly.utils.FirestoreGatherlyTest
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val WAIT_TIMEOUT = 5000L

@RunWith(AndroidJUnit4::class)
class SignInScreenTest : FirestoreGatherlyTest() {

  private lateinit var signInViewModel: SignInViewModel

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  override fun setUp() {
    super.setUp()
    FirebaseEmulator.auth.signOut()
  }

  @Test
  fun google_sign_in_is_configured() {
    val context = ApplicationProvider.getApplicationContext<Context>()

    val resourceId = context.resources.getIdentifier("web_client_id", "string", context.packageName)

    // Skip test if resource doesn't exist (useful for CI environments)
    assumeTrue("Google Sign-In not configured - skipping test", resourceId != 0)

    val clientId = context.getString(resourceId)
    assertTrue(
        "Invalid Google client ID format: $clientId", clientId.endsWith(".googleusercontent.com"))
  }

  @Test
  fun signInScreen_componentsAreDisplayed() {
    signInViewModel = SignInViewModel()
    composeTestRule.setContent { SignInScreen(authViewModel = signInViewModel) }

    composeTestRule.onNodeWithTag(SignInScreenTestTags.WELCOME_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SignInScreenTestTags.WELCOME_SUBTITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SignInScreenTestTags.GOOGLE_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SignInScreenTestTags.ANONYMOUS_BUTTON).assertIsDisplayed()
  }

  @Test
  fun canSignInWithGoogle() {
    val fakeToken =
        FakeJwtGenerator.createFakeGoogleIdToken(
            "signInName", email = "signinscreentest@signinscreen.com")
    val fakeCredentialManager = FakeCredentialManager.create(fakeToken)

    signInViewModel = SignInViewModel()

    composeTestRule.setContent {
      SignInScreen(authViewModel = signInViewModel, credentialManager = fakeCredentialManager)
    }

    // Click the Google sign-in button
    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.GOOGLE_BUTTON)
        .assertIsDisplayed()
        .performClick()

    // Wait until uiState becomes true or timeout is reached
    composeTestRule.waitUntil(timeoutMillis = WAIT_TIMEOUT) { signInViewModel.uiState.value }

    // Assert that the state is updated and user is signed in
    assert(signInViewModel.uiState.value) { "ViewModel did not report signed in" }
    val currentUser = FirebaseEmulator.auth.currentUser
    assert(currentUser != null) { "FirebaseEmulator has no signed-in user" }
    assert(currentUser!!.email == "signinscreentest@signinscreen.com") {
      "Signed-in user's email does not match expected"
    }
  }

  @Test
  fun canSignInAnonymously() {
    signInViewModel = SignInViewModel()
    composeTestRule.setContent { SignInScreen(authViewModel = signInViewModel) }

    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.ANONYMOUS_BUTTON)
        .assertIsDisplayed()
        .performClick()

    // Wait until uiState becomes true or timeout is reached
    composeTestRule.waitUntil(timeoutMillis = WAIT_TIMEOUT) { signInViewModel.uiState.value }

    assert(signInViewModel.uiState.value)
    assert(FirebaseEmulator.auth.currentUser != null)
  }
}
