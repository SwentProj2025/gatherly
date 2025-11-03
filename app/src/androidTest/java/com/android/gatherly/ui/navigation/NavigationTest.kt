package com.android.gatherly.ui.navigation

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
import com.android.gatherly.GatherlyApp
import com.android.gatherly.ui.authentication.SignInScreenTestTags
import com.android.gatherly.utils.FirebaseEmulator
import com.android.gatherly.utils.FirestoreGatherlyTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NavigationTest : FirestoreGatherlyTest() {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  override fun setUp() {
    super.setUp()
    runTest {
      FirebaseEmulator.auth.signOut()
      composeTestRule.setContent { GatherlyApp() }
      composeTestRule.waitUntil(10000L) {
        composeTestRule.onNodeWithTag(SignInScreenTestTags.WELCOME_TITLE).isDisplayed()
      }
      composeTestRule.onNodeWithTag(SignInScreenTestTags.ANONYMOUS_BUTTON).performClick()
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

  // LOGOUT PART :

  /**
   * Test: Verifies that the user can sign out from the home page screen using the logout button in
   * the drop-down menu of the top app bar.
   */
  @Test
  fun canLogOutFromHomePage() {
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
    composeTestRule.navigateFromHomeToOverview()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.LOGOUT_TAB).performClick()
    composeTestRule.checkSignInScreenIsDisplayed()
  }

  // HOME PAGE TESTS PART

  /** Test: Verifies that clicking the drop-down menu button correctly displays all tabs. */
  @Test
  fun testTagsDropMenuAreCorrectlySet() {
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
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.checkProfileScreenIsDisplayed()

    composeTestRule.onNodeWithTag(NavigationTestTags.TOP_NAVIGATION_MENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.HOMEPAGE_TAB).assertIsNotDisplayed()

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
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.checkProfileScreenIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).performClick()
    composeTestRule.checkMapScreenIsDisplayed()
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
