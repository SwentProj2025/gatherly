package com.android.gatherly.ui.navigation

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.rule.GrantPermissionRule
import com.android.gatherly.GatherlyApp
import com.android.gatherly.ui.authentication.InitProfileScreenTestTags
import com.android.gatherly.ui.authentication.SignInScreenTestTags
import com.android.gatherly.ui.homePage.HomePageScreenTestTags
import com.android.gatherly.ui.profile.ProfileScreenTestTags
import com.android.gatherly.utils.FakeCredentialManager
import com.android.gatherly.utils.FakeJwtGenerator
import com.android.gatherly.utils.FirebaseEmulator
import com.android.gatherly.utils.FirestoreGatherlyTest
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class NavigationTest : FirestoreGatherlyTest() {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  // Grant location permissions for the tests (required!)
  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

  private fun setUpWithGoogle() {
    runTest {
      FirebaseEmulator.auth.signOut()

      // Create google user
      val fakeGoogleIdToken =
          FakeJwtGenerator.createFakeGoogleIdToken("12345", email = "test@example.com")
      val fakeCredentialManager = FakeCredentialManager.create(fakeGoogleIdToken)

      composeTestRule.setContent { GatherlyApp(credentialManager = fakeCredentialManager) }
      composeTestRule.waitUntil(10000L) {
        composeTestRule.onNodeWithTag(SignInScreenTestTags.WELCOME_TITLE).isDisplayed()
      }
      composeTestRule.onNodeWithTag(SignInScreenTestTags.GOOGLE_BUTTON).performClick()
      composeTestRule.waitUntil(10_000L) {
        composeTestRule.onNodeWithTag("initProfile_save_button").isDisplayed()
      }
      // Fill mandatory fields so navigation can continue
      composeTestRule.onNodeWithTag("initProfile_username").performTextInput("testuser")
      composeTestRule.onNodeWithTag("initProfile_name_field").performTextInput("Test User")
      // Save and wait for HomePage
      composeTestRule.onNodeWithTag("initProfile_save_button").performClick()
      composeTestRule.waitForIdle()

      composeTestRule.waitUntil(10000L) {
        try {
          composeTestRule
              .onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
              .assertTextContains(value = "Home")
          true
        } catch (_: AssertionError) {
          false
        }
      }
    }
  }

  /** Verifies that while signing in with google, the init profile screen appears. */
  @Test
  fun logInWithGoogleDisplaysInitProfile() {
    val timeout = 10_000L

    // sign out
    Firebase.auth.signOut()

    // Create google user
    val fakeGoogleIdToken =
        FakeJwtGenerator.createFakeGoogleIdToken("12345", email = "test@example.com")
    val fakeCredentialManager = FakeCredentialManager.create(fakeGoogleIdToken)

    composeTestRule.setContent { GatherlyApp(credentialManager = fakeCredentialManager) }

    // Sign in with google
    composeTestRule.checkSignInScreenIsDisplayed()
    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.GOOGLE_BUTTON)
        .assertIsDisplayed()
        .performClick()

    // Check that we are redirected to the init profile page
    composeTestRule.waitUntil(timeout) {
      composeTestRule.onNodeWithTag(InitProfileScreenTestTags.USERNAME).isDisplayed()
    }
    composeTestRule.onNodeWithTag(InitProfileScreenTestTags.USERNAME).assertIsDisplayed()
  }

  /** Verifies that while signing in anonymously, the homepage screen appears. */
  @Test
  fun logInAnonymouslyDisplaysHomePage() {
    val timeout = 10_000L

    // sign out
    Firebase.auth.signOut()

    composeTestRule.setContent { GatherlyApp() }

    // Sign in with google
    composeTestRule.checkSignInScreenIsDisplayed()
    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.ANONYMOUS_BUTTON)
        .assertIsDisplayed()
        .performClick()

    // Check that we are redirected to the init profile page
    composeTestRule.waitUntil(timeout) {
      composeTestRule.onNodeWithTag(HomePageScreenTestTags.FOCUS_TIMER_TEXT).isDisplayed()
    }
    composeTestRule.onNodeWithTag(HomePageScreenTestTags.FOCUS_TIMER_TEXT).assertIsDisplayed()
  }

  /** Verifies that upgrading an anonymous account to google shows the init profile screen */
  @Test
  fun upgradeAccountOnProfileWorks() {
    val timeout = 30_000L

    // sign out
    Firebase.auth.signOut()

    // Create google user
    val fakeGoogleIdToken =
        FakeJwtGenerator.createFakeGoogleIdToken("12345", email = "test@example.com")
    val fakeCredentialManager = FakeCredentialManager.create(fakeGoogleIdToken)

    composeTestRule.setContent { GatherlyApp(credentialManager = fakeCredentialManager) }

    // Sign in with google
    composeTestRule.checkSignInScreenIsDisplayed()
    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.ANONYMOUS_BUTTON)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.waitUntil(timeout) {
      composeTestRule.onNodeWithTag(HomePageScreenTestTags.FOCUS_BUTTON).isDisplayed()
    }

    // Go to profile screen
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).assertIsDisplayed().performClick()
    composeTestRule.waitUntil(timeout) {
      composeTestRule.onNodeWithTag(ProfileScreenTestTags.GOOGLE_BUTTON).isDisplayed()
    }

    // click to upgrade to google
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.GOOGLE_BUTTON)
        .assertIsDisplayed()
        .performClick()

    // Check that we are redirected to the init profile screen
    composeTestRule.waitUntil(timeout) {
      composeTestRule.onNodeWithTag(InitProfileScreenTestTags.USERNAME).isDisplayed()
    }
    composeTestRule.onNodeWithTag(InitProfileScreenTestTags.USERNAME).assertIsDisplayed()
  }

  // LOGOUT PART :

  /**
   * Test: Verifies that the user can sign out from the home page screen using the logout button in
   * the drop-down menu of the top app bar.
   */
  @Test
  fun canLogOutFromHomePage() {
    setUpWithGoogle()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.LOGOUT_TAB).performClick()
    composeTestRule.checkSignInScreenIsDisplayed()
  }

  /**
   * Test: Verifies that the user can sign out from the profile screen using the logout button in
   * the drop-down menu of the top app bar.
   */
  @Test
  fun canLogOutFromProfile() {
    setUpWithGoogle()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.checkProfileScreenIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.LOGOUT_TAB).performClick()
    composeTestRule.checkSignInScreenIsDisplayed()
  }

  /**
   * Test: Verifies that the user can sign out from the focus timer screen using the logout button
   * in the drop-down menu of the top app bar.
   */
  @Test
  fun canLogOutFromTimer() {
    setUpWithGoogle()
    composeTestRule.navigateFromHomeToTimer()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.LOGOUT_TAB).performClick()
    composeTestRule.checkSignInScreenIsDisplayed()
  }

  /**
   * Test: Verifies that the user can sign out from the events overview screen using the logout
   * button in the drop-down menu of the top app bar.
   */
  @Test
  fun canLogOutFromEvents() {
    setUpWithGoogle()
    composeTestRule.navigateFromHomeToEvents()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.LOGOUT_TAB).performClick()
    composeTestRule.checkSignInScreenIsDisplayed()
  }

  /**
   * Test: Verifies that the user can sign out from the map screen using the logout button in the
   * drop-down menu of the top app bar.
   */
  @Test
  fun canLogOutFromMap() {
    setUpWithGoogle()
    composeTestRule.navigateFromHomeToMap()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.LOGOUT_TAB).performClick()
    composeTestRule.checkSignInScreenIsDisplayed()
  }

  /**
   * Test: Verifies that the user can sign out from the Todo overview screen using the logout button
   * in the drop-down menu of the top app bar.
   */
  @Test
  fun canLogOutFromOverview() {
    setUpWithGoogle()
    composeTestRule.navigateFromHomeToOverview()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.LOGOUT_TAB).performClick()
    composeTestRule.checkSignInScreenIsDisplayed()
  }

  // HOME PAGE TESTS PART

  /** Test: Verifies that clicking the drop-down menu button correctly displays all tabs. */
  @Test
  fun testTagsDropMenuAreCorrectlySet() {
    setUpWithGoogle()
    composeTestRule.onNodeWithTag(NavigationTestTags.TOP_NAVIGATION_MENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.LOGOUT_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).assertIsDisplayed()
  }

  /** Test: Verifies if the Top Navigation Bar is correctly displayed in the home page screen */
  @Test
  fun topNavigationIsCorrectlySetForHomePage() {
    setUpWithGoogle()
    composeTestRule.onNodeWithTag(NavigationTestTags.TOP_NAVIGATION_MENU).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
        .assertTextContains(value = "Home")
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.HOMEPAGE_TAB).assertIsNotDisplayed()
  }

  /** Test: Verifies if the Bottom Navigation Bar is correctly on the home page screen */
  @Test
  fun bottomNavigationIsDisplayedForHomePage() {
    setUpWithGoogle()
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.TIMER_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.EVENTS_TAB).assertIsDisplayed()
  }

  /**
   * Navigation Test: Verifies that when the user is on the home page, they can navigate to the
   * profile screen.
   */
  @Test
  fun canNavigateToProfileFromHomePage() {
    setUpWithGoogle()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.checkProfileScreenIsDisplayed()
  }

  /**
   * Navigation Test: Verifies that when the user is on the home page, they can navigate to the
   * settings screen.
   */
  @Test
  fun canNavigateToSettingsFromHomePage() {
    setUpWithGoogle()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_TAB).performClick()
    composeTestRule.checkSettingsScreenIsDisplayed()
  }

  // PROFILE PART

  /**
   * Test: Verifies if the Top Navigation Bar and the Bottom Navigation Bar is correctly displayed
   * on the profile screen
   */
  @Test
  fun NavigationBarIsCorrectlySetForProfile() {
    setUpWithGoogle()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.checkProfileScreenIsDisplayed()

    composeTestRule.onNodeWithTag(NavigationTestTags.TOP_NAVIGATION_MENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.HOMEPAGE_TAB).assertIsDisplayed()

    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.TIMER_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.EVENTS_TAB).assertIsDisplayed()

    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.LOGOUT_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).assertIsNotDisplayed()
  }

  /**
   * Navigation Test: Verifies that when the user is on the profile screen, they can navigate to the
   * settings screen.
   */
  @Test
  fun NavigateFromProfileToSettings() {
    setUpWithGoogle()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.checkProfileScreenIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_TAB).performClick()
    composeTestRule.checkSettingsScreenIsDisplayed()
  }

  /**
   * Navigation Test: Verifies that when the user is on the profile screen, they can navigate to the
   * settings screen.
   */
  @Test
  fun NavigateFromProfileToTimer() {
    setUpWithGoogle()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.checkProfileScreenIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.TIMER_TAB).performClick()
    composeTestRule.checkTimerScreenIsDisplayed()
  }

  /**
   * Navigation Test: Verifies that when the user is on the profile screen, they can navigate to the
   * todo overview screen.
   */
  @Test
  fun NavigateFromProfileToOverview() {
    setUpWithGoogle()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.checkProfileScreenIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).performClick()
    composeTestRule.checkOverviewScreenIsDisplayed()
  }

  /**
   * Navigation Test: Verifies that when the user is on the profile screen, they can navigate to the
   * events screen.
   */
  @Test
  fun NavigateFromProfileToEvents() {
    setUpWithGoogle()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.checkProfileScreenIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.EVENTS_TAB).performClick()
    composeTestRule.checkEventsScreenIsDisplayed()
  }

  /**
   * Navigation Test: Verifies that when the user is on the profile screen, they can navigate to the
   * map screen.
   */
  @Test
  fun NavigateFromProfileToMap() {
    setUpWithGoogle()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.checkProfileScreenIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).performClick()
    composeTestRule.checkMapScreenIsDisplayed()
  }

  /**
   * Navigation Test: Verifies that when the user is on the profile screen, tapping the home icon
   * navigates back to the home page.
   */
  @Test
  fun NavigateFromProfileToHomePage() {
    setUpWithGoogle()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.checkProfileScreenIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.HOMEPAGE_TAB).performClick()
    composeTestRule.checkHomeScreenIsDisplayed()
  }

  // TIMER PART

  /**
   * Helper function that simplifies navigation from the home page to the timer screen, avoiding
   * repetitive performClick calls.
   */
  fun ComposeTestRule.navigateFromHomeToTimer() {
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.TIMER_TAB).performClick()
    composeTestRule.checkTimerScreenIsDisplayed()
  }
  /**
   * Test: Verifies if the Top Navigation Bar and the Bottom Navigation Bar is correctly displayed
   * on the timer screen
   */
  @Test
  fun NavigationBarIsCorrectlySetForTimer() {
    setUpWithGoogle()
    composeTestRule.navigateFromHomeToTimer()

    composeTestRule.onNodeWithTag(NavigationTestTags.TOP_NAVIGATION_MENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.HOMEPAGE_TAB).assertIsDisplayed()

    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.TIMER_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.EVENTS_TAB).assertIsDisplayed()

    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.LOGOUT_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).assertIsDisplayed()
  }

  /**
   * Navigation Test: Verifies that when the user is on the focus timer screen, they can navigate to
   * the home page screen.
   */
  @Test
  fun NavigateFromTimerToHomePage() {
    setUpWithGoogle()
    composeTestRule.navigateFromHomeToTimer()
    composeTestRule.onNodeWithTag(NavigationTestTags.HOMEPAGE_TAB).performClick()
    composeTestRule.checkHomeScreenIsDisplayed()
  }

  /**
   * Navigation Test: Verifies that when the user is on the focus timer screen, they can navigate to
   * the settings screen.
   */
  @Test
  fun NavigateFromTimerToSettings() {
    setUpWithGoogle()
    composeTestRule.navigateFromHomeToTimer()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_TAB).performClick()
    composeTestRule.checkSettingsScreenIsDisplayed()
  }

  /**
   * Navigation Test: Verifies that when the user is on the focus timer screen, they can navigate to
   * the profile screen.
   */
  @Test
  fun NavigateFromTimerToProfile() {
    setUpWithGoogle()
    composeTestRule.navigateFromHomeToTimer()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.checkProfileScreenIsDisplayed()
  }

  /**
   * Navigation Test: Verifies that when the user is on the focus timer screen, they can navigate to
   * the todo overview screen.
   */
  @Test
  fun NavigateFromTimerToOverview() {
    setUpWithGoogle()
    composeTestRule.navigateFromHomeToTimer()
    composeTestRule.onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).performClick()
    composeTestRule.checkOverviewScreenIsDisplayed()
  }

  /**
   * Navigation Test: Verifies that when the user is on the focus timer screen, they can navigate to
   * the events screen.
   */
  @Test
  fun NavigateFromTimerToEvents() {
    setUpWithGoogle()
    composeTestRule.navigateFromHomeToTimer()
    composeTestRule.onNodeWithTag(NavigationTestTags.EVENTS_TAB).performClick()
    composeTestRule.checkEventsScreenIsDisplayed()
  }

  /**
   * Navigation Test: Verifies that when the user is on the focus timer screen, they can navigate to
   * the map screen.
   */
  @Test
  fun NavigateFromTimerToMap() {
    setUpWithGoogle()
    composeTestRule.navigateFromHomeToTimer()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).performClick()
    composeTestRule.checkMapScreenIsDisplayed()
  }

  // EVENTS PART

  /**
   * Helper function that simplifies navigation from the home page to the events screen, avoiding
   * repetitive performClick calls.
   */
  fun ComposeTestRule.navigateFromHomeToEvents() {
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.EVENTS_TAB).performClick()
    composeTestRule.checkEventsScreenIsDisplayed()
  }

  /**
   * Test: Verifies if the Top Navigation Bar and the Bottom Navigation Bar is correctly displayed
   * on the events screen
   */
  @Test
  fun NavigationBarIsCorrectlySetForEvents() {
    setUpWithGoogle()
    composeTestRule.navigateFromHomeToEvents()

    composeTestRule.onNodeWithTag(NavigationTestTags.TOP_NAVIGATION_MENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.HOMEPAGE_TAB).assertIsDisplayed()

    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.TIMER_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.EVENTS_TAB).assertIsDisplayed()

    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.LOGOUT_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).assertIsDisplayed()
  }

  /**
   * Navigation Test: Verifies that when the user is on the events screen, they can navigate to the
   * home page screen.
   */
  @Test
  fun NavigateFromEventsToHomePage() {
    setUpWithGoogle()
    composeTestRule.navigateFromHomeToEvents()
    composeTestRule.onNodeWithTag(NavigationTestTags.HOMEPAGE_TAB).performClick()
    composeTestRule.checkHomeScreenIsDisplayed()
  }

  /**
   * Navigation Test: Verifies that when the user is on the events screen, they can navigate to the
   * settings screen.
   */
  @Test
  fun NavigateFromEventsToSettings() {
    setUpWithGoogle()
    composeTestRule.navigateFromHomeToEvents()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_TAB).performClick()
    composeTestRule.checkSettingsScreenIsDisplayed()
  }

  /**
   * Navigation Test: Verifies that when the user is on the events screen, they can navigate to the
   * profile screen.
   */
  @Test
  fun NavigateFromEventsToProfile() {
    setUpWithGoogle()
    composeTestRule.navigateFromHomeToEvents()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.checkProfileScreenIsDisplayed()
  }

  /**
   * Navigation Test: Verifies that when the user is on the events screen, they can navigate to the
   * todo overview screen.
   */
  @Test
  fun NavigateFromEventsToOverview() {
    setUpWithGoogle()
    composeTestRule.navigateFromHomeToEvents()
    composeTestRule.onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).performClick()
    composeTestRule.checkOverviewScreenIsDisplayed()
  }

  /**
   * Navigation Test: Verifies that when the user is on the events screen, they can navigate to the
   * focus timer screen.
   */
  @Test
  fun NavigateFromEventsToTimer() {
    setUpWithGoogle()
    composeTestRule.navigateFromHomeToEvents()
    composeTestRule.onNodeWithTag(NavigationTestTags.TIMER_TAB).performClick()
    composeTestRule.checkTimerScreenIsDisplayed()
  }

  /**
   * Navigation Test: Verifies that when the user is on the events screen, they can navigate to the
   * map screen.
   */
  @Test
  fun NavigateFromEventsToMap() {
    setUpWithGoogle()
    composeTestRule.navigateFromHomeToEvents()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).performClick()
    composeTestRule.checkMapScreenIsDisplayed()
  }

  // MAP PART

  /**
   * Helper function that simplifies navigation from the home page to the map screen, avoiding
   * repetitive performClick calls.
   */
  fun ComposeTestRule.navigateFromHomeToMap() {
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).performClick()
    composeTestRule.checkMapScreenIsDisplayed()
  }

  /**
   * Test: Verifies if the Top Navigation Bar and the Bottom Navigation Bar is correctly displayed
   * on the map screen
   */
  @Test
  fun NavigationBarIsCorrectlySetForMap() {
    setUpWithGoogle()
    composeTestRule.navigateFromHomeToMap()

    composeTestRule.onNodeWithTag(NavigationTestTags.TOP_NAVIGATION_MENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.HOMEPAGE_TAB).assertIsDisplayed()

    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.TIMER_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.EVENTS_TAB).assertIsDisplayed()

    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.LOGOUT_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).assertIsDisplayed()
  }

  /**
   * Navigation Test: Verifies that when the user is on the map screen, they can navigate to the
   * home page screen.
   */
  @Test
  fun NavigateFromMapToHomePage() {
    setUpWithGoogle()
    composeTestRule.navigateFromHomeToMap()
    composeTestRule.onNodeWithTag(NavigationTestTags.HOMEPAGE_TAB).performClick()
    composeTestRule.checkHomeScreenIsDisplayed()
  }

  /**
   * Navigation Test: Verifies that when the user is on the map screen, they can navigate to the
   * settings screen.
   */
  @Test
  fun NavigateFromMapToSettings() {
    setUpWithGoogle()
    composeTestRule.navigateFromHomeToMap()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_TAB).performClick()
    composeTestRule.checkSettingsScreenIsDisplayed()
  }

  /**
   * Navigation Test: Verifies that when the user is on the map screen, they can navigate to the
   * profile screen.
   */
  @Test
  fun NavigateFromMapToProfile() {
    setUpWithGoogle()
    composeTestRule.navigateFromHomeToMap()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.checkProfileScreenIsDisplayed()
  }

  /**
   * Navigation Test: Verifies that when the user is on the map screen, they can navigate to the
   * todo overview screen.
   */
  @Test
  fun NavigateFromMapToOverview() {
    setUpWithGoogle()
    composeTestRule.navigateFromHomeToMap()
    composeTestRule.onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).performClick()
    composeTestRule.checkOverviewScreenIsDisplayed()
  }

  /**
   * Navigation Test: Verifies that when the user is on the map screen, they can navigate to the
   * focus timer screen.
   */
  @Test
  fun NavigateFromMapToTimer() {
    setUpWithGoogle()
    composeTestRule.navigateFromHomeToMap()
    composeTestRule.onNodeWithTag(NavigationTestTags.TIMER_TAB).performClick()
    composeTestRule.checkTimerScreenIsDisplayed()
  }

  /**
   * Navigation Test: Verifies that when the user is on the map screen, they can navigate to the
   * events screen.
   */
  @Test
  fun NavigateFromMapToEvents() {
    setUpWithGoogle()
    composeTestRule.navigateFromHomeToMap()
    composeTestRule.onNodeWithTag(NavigationTestTags.EVENTS_TAB).performClick()
    composeTestRule.checkEventsScreenIsDisplayed()
  }

  // OVERVIEW PART with AddToDo

  /**
   * Helper function that simplifies navigation from the home page to the todo overview screen,
   * avoiding repetitive performClick calls.
   */
  fun ComposeTestRule.navigateFromHomeToOverview() {
    onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).performClick()
    checkOverviewScreenIsDisplayed()
  }

  /**
   * Test: Verifies if the Top Navigation Bar and the Bottom Navigation Bar is correctly displayed
   * on the map screen
   */
  @Test
  fun NavigationBarIsCorrectlySetForOverview() {
    setUpWithGoogle()
    composeTestRule.navigateFromHomeToOverview()

    composeTestRule.onNodeWithTag(NavigationTestTags.TOP_NAVIGATION_MENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.HOMEPAGE_TAB).assertIsDisplayed()

    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.TIMER_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.EVENTS_TAB).assertIsDisplayed()

    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.LOGOUT_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).assertIsDisplayed()
  }

  /**
   * Navigation Test: Verifies that when the user is on the todo overview screen, they can navigate
   * to the settings screen.
   */
  @Test
  fun NavigateFromOverviewToSettings() {
    setUpWithGoogle()
    composeTestRule.navigateFromHomeToOverview()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_TAB).performClick()
    composeTestRule.checkSettingsScreenIsDisplayed()
  }

  /**
   * Navigation Test: Verifies that when the user is on the todo overview screen, they can navigate
   * to the profile screen.
   */
  @Test
  fun NavigateFromOverviewToProfile() {
    setUpWithGoogle()
    composeTestRule.navigateFromHomeToOverview()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.checkProfileScreenIsDisplayed()
  }

  /**
   * Navigation Test: Verifies that when the user is on the todo overview screen, they can navigate
   * to the focus timer screen.
   */
  @Test
  fun NavigateFromOverviewToTimer() {
    setUpWithGoogle()
    composeTestRule.navigateFromHomeToOverview()
    composeTestRule.onNodeWithTag(NavigationTestTags.TIMER_TAB).performClick()
    composeTestRule.checkTimerScreenIsDisplayed()
  }

  /**
   * Navigation Test: Verifies that when the user is on the todo overview screen, they can navigate
   * to the events screen.
   */
  @Test
  fun NavigateFromOverviewToEvents() {
    setUpWithGoogle()
    composeTestRule.navigateFromHomeToOverview()
    composeTestRule.onNodeWithTag(NavigationTestTags.EVENTS_TAB).performClick()
    composeTestRule.checkEventsScreenIsDisplayed()
  }

  /**
   * Navigation Test: Verifies that when the user is on the todo overview screen, they can navigate
   * to the map screen.
   */
  @Test
  fun NavigateFromOverviewToMap() {
    setUpWithGoogle()
    composeTestRule.navigateFromHomeToOverview()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).performClick()
    composeTestRule.checkMapScreenIsDisplayed()
  }

  ///////////////   utils  ///////////////////////////

  /**
   * Helper function to use when we want to check if the current screen displaying is the todo
   * overview screen
   */
  fun ComposeTestRule.checkOverviewScreenIsDisplayed() {
    onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
        .assertTextContains("To-Do", substring = true, ignoreCase = true)
  }

  /**
   * Helper function to use when we want to check if the current screen displaying is the profile
   * screen
   */
  fun ComposeTestRule.checkProfileScreenIsDisplayed() {
    onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
        .assertTextContains("Your profile", substring = true, ignoreCase = true)
  }

  /**
   * Helper function to use when we want to check if the current screen displaying is the settings
   * screen
   */
  fun ComposeTestRule.checkSettingsScreenIsDisplayed() {
    onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
        .assertTextContains("Settings", substring = true, ignoreCase = true)
  }

  /**
   * Helper function to use when we want to check if the current screen displaying is the focus
   * timer screen
   */
  fun ComposeTestRule.checkTimerScreenIsDisplayed() {
    onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
        .assertTextContains("Timer", substring = true, ignoreCase = true)
  }

  /**
   * Helper function to use when we want to check if the current screen displaying is the events
   * screen
   */
  fun ComposeTestRule.checkEventsScreenIsDisplayed() {
    onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
        .assertTextContains("Events", substring = true, ignoreCase = true)
  }

  /**
   * Helper function to use when we want to check if the current screen displaying is the map screen
   */
  fun ComposeTestRule.checkMapScreenIsDisplayed() {
    onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
        .assertTextContains("Map", substring = true, ignoreCase = true)
  }

  /**
   * Helper function to use when we want to check if the current screen displaying is the home page
   * screen
   */
  fun ComposeTestRule.checkHomeScreenIsDisplayed() {
    onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
        .assertTextContains("Home", substring = true, ignoreCase = true)
  }

  /**
   * Helper function to use when we want to check if the current screen displaying is the signIn
   * screen
   */
  fun ComposeTestRule.checkSignInScreenIsDisplayed() {
    onNodeWithTag(SignInScreenTestTags.WELCOME_TITLE)
        .assertIsDisplayed()
        .assertTextContains("Welcome to Gatherly,", substring = true, ignoreCase = true)
  }
}
