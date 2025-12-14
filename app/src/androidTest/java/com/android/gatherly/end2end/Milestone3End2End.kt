package com.android.gatherly.end2end

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.credentials.CredentialManager
import androidx.test.rule.GrantPermissionRule
import com.android.gatherly.GatherlyApp
import com.android.gatherly.ui.authentication.InitProfileScreenTestTags
import com.android.gatherly.ui.authentication.SignInScreenTestTags
import com.android.gatherly.ui.homePage.HomePageScreenTestTags
import com.android.gatherly.utils.FakeCredentialManager
import com.android.gatherly.utils.FakeJwtGenerator
import com.android.gatherly.utils.FirebaseEmulator
import com.android.gatherly.utils.FirestoreGatherlyTest
import org.junit.Before
import org.junit.Rule
import kotlin.test.Test



class Milestone3End2End : FirestoreGatherlyTest() {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

    val TIMEOUT = 15000L
    private var currentCredentialManager: CredentialManager? = null

    private fun setAppContent() {
        currentCredentialManager?.let { credentialManager ->
            composeTestRule.setContent {
                GatherlyApp(credentialManager = credentialManager)
            }
        }
    }

    @Before
    override fun setUp() {
        super.setUp()
        FirebaseEmulator.auth.signOut()
        val fakeGoogleIdToken = FakeJwtGenerator.createFakeGoogleIdToken("user", email = "user@gmail.com")
        currentCredentialManager = FakeCredentialManager.create(fakeGoogleIdToken)
        setAppContent()
    }

    private fun switchToUser(user: String, email: String) {
        composeTestRule.waitForIdle()

        val fakeGoogleIdToken = FakeJwtGenerator.createFakeGoogleIdToken(user, email = email)
        currentCredentialManager = FakeCredentialManager.create(fakeGoogleIdToken)

        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.recreate()
        }
        composeTestRule.waitUntil(TIMEOUT) {
            composeTestRule.onNodeWithTag(SignInScreenTestTags.GOOGLE_BUTTON).isDisplayed()
        }
    }

    @Test
    fun testFriendRequestFlow() {
        signInWithGoogle()
        createProfile("friend1", "friend1_name")

        switchToUser("friend2", "friend2@gmail.com")

        signInWithGoogle()
        createProfile("friend2", "friend2_name")

        sendFriendRequest()

        switchToUser("friend1", "friend1@gmail.com")

        acceptFriendRequest()
    }

    private fun signInWithGoogle() {
        composeTestRule
            .onNodeWithTag(SignInScreenTestTags.GOOGLE_BUTTON)
            .performClick()
        composeTestRule.waitUntil(TIMEOUT) {
            composeTestRule.onNodeWithTag(InitProfileScreenTestTags.SAVE_BUTTON).isDisplayed()
        }
    }

    private fun createProfile(username: String, name: String) {
        composeTestRule
            .onNodeWithTag(InitProfileScreenTestTags.USERNAME)
            .performTextInput(username)
        composeTestRule
            .onNodeWithTag(InitProfileScreenTestTags.NAME_FIELD)
            .performTextInput(name)
        composeTestRule
            .onNodeWithTag(InitProfileScreenTestTags.SAVE_BUTTON)
            .performClick()
        composeTestRule.waitUntil(TIMEOUT) {
            composeTestRule.onNodeWithTag(HomePageScreenTestTags.FOCUS_BUTTON).isDisplayed()
        }
    }

    private fun sendFriendRequest() {
        // TODO
    }

    private fun acceptFriendRequest() {
        // TODO
    }
}