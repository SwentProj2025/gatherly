package com.android.gatherly.end2end

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.rule.GrantPermissionRule
import com.android.gatherly.GatherlyApp
import com.android.gatherly.ui.authentication.InitProfileScreenTestTags
import com.android.gatherly.ui.authentication.SignInScreenTestTags
import com.android.gatherly.ui.events.AddEventScreenTestTags
import com.android.gatherly.ui.events.EventsScreenTestTags
import com.android.gatherly.ui.homePage.HomePageScreenTestTags
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.profile.ProfileScreenTestTags
import com.android.gatherly.ui.settings.SettingsScreenTestTags
import com.android.gatherly.utils.FakeCredentialManager
import com.android.gatherly.utils.FakeJwtGenerator
import com.android.gatherly.utils.FirebaseEmulator
import com.android.gatherly.utils.FirestoreGatherlyTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class Milestone2End2End : FirestoreGatherlyTest() {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  // Grant location permissions for the tests (required!)
  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

  val TIMEOUT = 5000L

  // set content
  @Before
  override fun setUp() {
    super.setUp()
    FirebaseEmulator.auth.signOut()
    // Create a fake Google ID token for a new user
    val fakeGoogleIdToken =
        FakeJwtGenerator.createFakeGoogleIdToken("user", email = "user@gmail.com")
    val fakeCredentialManager = FakeCredentialManager.create(fakeGoogleIdToken)
    composeTestRule.setContent { GatherlyApp(credentialManager = fakeCredentialManager) }
  }

  // this end to end test verifies that a user can sign in with google, create a profile and create
  // an event successfully
  @Test
  fun createEvent() {
    // Sign in using google
    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.GOOGLE_BUTTON)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.waitUntil(TIMEOUT) {
      composeTestRule.onNodeWithTag(InitProfileScreenTestTags.SAVE_BUTTON).isDisplayed()
    }

    // Create a username, name then save
    composeTestRule
        .onNodeWithTag(InitProfileScreenTestTags.USERNAME)
        .assertIsDisplayed()
        .performTextInput("username")
    composeTestRule
        .onNodeWithTag(InitProfileScreenTestTags.NAME_FIELD)
        .assertIsDisplayed()
        .performTextInput("User Name")
    composeTestRule
        .onNodeWithTag(InitProfileScreenTestTags.SAVE_BUTTON)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.waitUntil(TIMEOUT) {
      composeTestRule.onNodeWithTag(HomePageScreenTestTags.FOCUS_BUTTON).isDisplayed()
    }

    // Go to events page
    composeTestRule.onNodeWithTag(NavigationTestTags.EVENTS_TAB).assertIsDisplayed().performClick()
    composeTestRule.waitUntil(TIMEOUT) {
      composeTestRule.onNodeWithTag(EventsScreenTestTags.BROWSE_TITLE).isDisplayed()
    }

    // Scroll to bottom
    composeTestRule
        .onNodeWithTag(EventsScreenTestTags.ALL_LISTS)
        .performScrollToNode(hasTestTag(EventsScreenTestTags.CREATE_EVENT_BUTTON))

    // Create a new event
    composeTestRule
        .onNodeWithTag(EventsScreenTestTags.CREATE_EVENT_BUTTON)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.waitUntil(TIMEOUT) {
      composeTestRule.onNodeWithTag(AddEventScreenTestTags.INPUT_NAME).isDisplayed()
    }

    // Input event details
    composeTestRule
        .onNodeWithTag(AddEventScreenTestTags.INPUT_NAME)
        .assertIsDisplayed()
        .performTextInput("My event")
    composeTestRule
        .onNodeWithTag(AddEventScreenTestTags.INPUT_DESCRIPTION)
        .assertIsDisplayed()
        .performTextInput("Description for my great event")
    composeTestRule
        .onNodeWithTag(AddEventScreenTestTags.INPUT_CREATOR)
        .assertIsDisplayed()
        .performTextInput("User1")
    composeTestRule
        .onNodeWithTag(AddEventScreenTestTags.INPUT_DATE)
        .assertIsDisplayed()
        .performTextInput("12/12/2025")
    composeTestRule
        .onNodeWithTag(AddEventScreenTestTags.INPUT_START)
        .assertIsDisplayed()
        .performTextInput("10:00")
    composeTestRule
        .onNodeWithTag(AddEventScreenTestTags.INPUT_END)
        .assertIsDisplayed()
        .performTextInput("12:00")

    // Input location
    composeTestRule
        .onNodeWithTag(AddEventScreenTestTags.INPUT_LOCATION)
        .assertIsDisplayed()
        .performTextInput("3 rue mehl")
    composeTestRule.waitUntil(TIMEOUT) {
      composeTestRule.onNodeWithTag(AddEventScreenTestTags.LOCATION_SUGGESTION).isDisplayed()
    }
    composeTestRule
        .onNodeWithTag(AddEventScreenTestTags.LOCATION_SUGGESTION)
        .assertIsDisplayed()
        .performClick()

    // Save
    composeTestRule
        .onNodeWithTag(AddEventScreenTestTags.LAZY_LIST)
        .performScrollToNode(hasTestTag(AddEventScreenTestTags.BTN_SAVE))
    composeTestRule
        .onNodeWithTag(AddEventScreenTestTags.BTN_SAVE)
        .assertIsDisplayed()
        .performClick()

    // Check that event has been created
    composeTestRule.waitUntil(TIMEOUT) {
      composeTestRule
          .onNodeWithTag(EventsScreenTestTags.EVENT_TITLE, useUnmergedTree = true)
          .isDisplayed()
    }
    composeTestRule
        .onNodeWithTag(EventsScreenTestTags.EVENT_TITLE, useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextContains("My event")
  }

  // this end to end test verifies that a user can sign in with google, create a profile and modify
  // their profile successfully
  @Test
  fun changeProfileInformation() {
    // Sign in using google
    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.GOOGLE_BUTTON)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.waitUntil(TIMEOUT) {
      composeTestRule.onNodeWithTag(InitProfileScreenTestTags.USERNAME).isDisplayed()
    }

    // Create a username, name then save
    composeTestRule
        .onNodeWithTag(InitProfileScreenTestTags.USERNAME)
        .assertIsDisplayed()
        .performTextInput("username")
    composeTestRule
        .onNodeWithTag(InitProfileScreenTestTags.NAME_FIELD)
        .assertIsDisplayed()
        .performTextInput("User Name")
    composeTestRule
        .onNodeWithTag(InitProfileScreenTestTags.SAVE_BUTTON)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.waitUntil(TIMEOUT) {
      composeTestRule.onNodeWithTag(HomePageScreenTestTags.FOCUS_BUTTON).isDisplayed()
    }

    // Go to settings
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).assertIsDisplayed().performClick()
    composeTestRule
        .onNodeWithTag(NavigationTestTags.SETTINGS_TAB)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.waitUntil(TIMEOUT) {
      composeTestRule.onNodeWithTag(SettingsScreenTestTags.USERNAME).isDisplayed()
    }

    // Change username
    composeTestRule
        .onNodeWithTag(SettingsScreenTestTags.USERNAME)
        .assertIsDisplayed()
        .performTextClearance()
    composeTestRule
        .onNodeWithTag(SettingsScreenTestTags.USERNAME)
        .assertIsDisplayed()
        .performTextInput("different_username")
    composeTestRule
        .onNodeWithTag(SettingsScreenTestTags.SAVE_BUTTON)
        .assertIsDisplayed()
        .performClick()

    // Navigate to profile
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).assertIsDisplayed().performClick()
    composeTestRule.waitUntil(TIMEOUT) {
      composeTestRule.onNodeWithTag(ProfileScreenTestTags.PROFILE_USERNAME).isDisplayed()
    }

    // Check that the username changed
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.PROFILE_USERNAME)
        .assertIsDisplayed()
        .assertTextContains("different_username", substring = true)
  }
}
