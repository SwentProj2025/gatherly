package com.android.gatherly.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.gatherly.GatherlyApp
import com.android.gatherly.ui.SignIn.SignInScreenTestTags
import com.android.gatherly.ui.focusTimer.FocusTimerScreenTestTags
import com.android.gatherly.ui.homePage.HomePageScreenTestTags
import com.android.gatherly.ui.overview.OverviewScreenTestTags
import com.android.gatherly.ui.profile.ProfileScreenTestTags
import com.android.gatherly.utils.GatherlyTest
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NavigationTest : GatherlyTest() {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  override fun setUp() {
    super.setUp()
    composeTestRule.setContent { GatherlyApp() }
  }

  @Test
  fun testTagsAreCorrectlySet() {
    composeTestRule.onNodeWithTag(NavigationTestTags.TOP_NAVIGATION_MENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.EVENTS_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.TIMER_TAB).assertIsDisplayed()
  }

  @Test
  fun testTagsDropMenuAreCorrectlySet() {
    composeTestRule.onNodeWithTag(NavigationTestTags.TOP_NAVIGATION_MENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.LOGOUT_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).assertIsDisplayed()
  }

  @Test
  fun topNavigationIsCorrectlySetForHomePage() {
    composeTestRule
        .onNodeWithTag(NavigationTestTags.TOP_NAVIGATION_MENU)
        .assertIsDisplayed()
        .assertTextContains(value = "Home")
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.HOMEPAGE_TAB).assertIsNotDisplayed()
  }

  @Test
  fun bottomNavigationIsNotDisplayedForHomePage() {
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.TIMER_TAB).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.EVENTS_TAB).assertIsNotDisplayed()
  }

  @Test
  fun canNavigateToProfileFromHomePage() {
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.checkProfileScreenIsDisplayed()
    composeTestRule.checkOverviewScreenIsNotDisplayed()
  }

  @Test
  fun canNavigateToSettingsFromHomePage() {
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_TAB).performClick()
    composeTestRule.checkSettingsScreenIsDisplayed()
    composeTestRule.checkOverviewScreenIsNotDisplayed()
  }

  @Test
  fun canLogOutFromHomePage() {
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.LOGOUT_TAB).performClick()
    composeTestRule.checkSignInScreenIsDisplayed()
    composeTestRule.checkOverviewScreenIsNotDisplayed()
  }
  /*
   @Test
   fun bottomNavigationIsDisplayedForOverview() {
     composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
   }

   @Test
   fun bottomNavigationIsDisplayedForMap() {
     composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).performClick()
     composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
   }

  */
  /*
   @Test
   fun tabsAreClickable() {
     composeTestRule
         .onNodeWithTag(NavigationTestTags.OVERVIEW_TAB)
         .assertIsDisplayed()
         .performClick()
     composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).assertIsDisplayed().performClick()
   }

  */

  @Test
  fun topBarTitleIsCorrectForHome() {
    composeTestRule
        .onNodeWithTag(NavigationTestTags.TOP_NAVIGATION_MENU)
        .assertIsDisplayed()
        .assertTextContains(value = "HOME")
  }
  /*
   @Test
   fun topBarTitleIsCorrectForMap() {
     composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).performClick()
     composeTestRule
         .onNodeWithTag(NavigationTestTags.TOP_NAVIGATION_MENU)
         .assertIsDisplayed()
         .assertTextContains(value = "Map")
   }

  */

  /*
  @Test
  fun topBarTitleIsCorrectForOverview() {
    composeTestRule.onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).performClick()
    composeTestRule
        .onNodeWithTag(NavigationTestTags.TOP_NAVIGATION_MENU)
        .assertIsDisplayed()
        .assertTextContains(value = "Overview")
  }


   */
  /*
    TODO : Enable this test after implementing Profile screen
  fun canNavigateBackToProfileFromFriendsUsingSystemBack() {
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.friendsbutton).performClick()
    composeTestRule.checkFriendsScreenIsDisplayed()
    composeTestRule.checkProfileScreenIsNotDisplayed()
    pressBack(shouldFinish = false)
    composeTestRule.checkProfileScreenIsDisplayed()
  }*/
  /*
   @Test
   fun canNavigateBetweenTabs() {
     composeTestRule.onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).performClick()
     composeTestRule.checkOverviewScreenIsDisplayed()
     composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).performClick()
     composeTestRule.checkMapScreenIsDisplayed()
     composeTestRule.checkOverviewScreenIsNotDisplayed()
     composeTestRule.onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).performClick()
     composeTestRule.checkOverviewScreenIsDisplayed()
     composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).performClick()
     composeTestRule.checkMapScreenIsDisplayed()
     composeTestRule.checkOverviewScreenIsNotDisplayed()
   }

  */

  private fun pressBack(shouldFinish: Boolean) {
    composeTestRule.activityRule.scenario.onActivity { activity ->
      activity.onBackPressedDispatcher.onBackPressed()
    }
    composeTestRule.waitUntil { composeTestRule.activity.isFinishing == shouldFinish }
    assertEquals(shouldFinish, composeTestRule.activity.isFinishing)
  }

  fun ComposeTestRule.checkOverviewScreenIsNotDisplayed() {
    onNodeWithTag(OverviewScreenTestTags.OverviewText).assertDoesNotExist()
  }

  fun ComposeTestRule.checkOverviewScreenIsDisplayed() {
    onNodeWithTag(NavigationTestTags.TOP_NAVIGATION_MENU)
        .assertIsDisplayed()
        .assertTextContains("overview", substring = true, ignoreCase = true)
  }

  fun ComposeTestRule.checkHomeScreenIsNotDisplayed() {
    onNodeWithTag(HomePageScreenTestTags.HOMETEXT).assertDoesNotExist()
  }

  fun ComposeTestRule.checkHomeScreenIsDisplayed() {
    onNodeWithTag(NavigationTestTags.TOP_NAVIGATION_MENU)
        .assertIsDisplayed()
        .assertTextContains("home page", substring = true, ignoreCase = true)
  }

  fun ComposeTestRule.checkTimerScreenIsNotDisplayed() {
    onNodeWithTag(FocusTimerScreenTestTags.TIMERTEXT).assertDoesNotExist()
  }

  fun ComposeTestRule.checkTimerScreenIsDisplayed() {
    onNodeWithTag(NavigationTestTags.TOP_NAVIGATION_MENU)
        .assertIsDisplayed()
        .assertTextContains("focus timer", substring = true, ignoreCase = true)
  }

  fun ComposeTestRule.checkProfileScreenIsNotDisplayed() {
    onNodeWithTag(ProfileScreenTestTags.ProfileText).assertDoesNotExist()
  }

  fun ComposeTestRule.checkProfileScreenIsDisplayed() {
    onNodeWithTag(NavigationTestTags.TOP_NAVIGATION_MENU)
        .assertIsDisplayed()
        .assertTextContains("Your profile", substring = true, ignoreCase = true)
  }

  fun ComposeTestRule.checkSettingsScreenIsNotDisplayed() {
    onNodeWithTag(ProfileScreenTestTags.ProfileText).assertDoesNotExist()
  }

  fun ComposeTestRule.checkSettingsScreenIsDisplayed() {
    onNodeWithTag(NavigationTestTags.TOP_NAVIGATION_MENU)
        .assertIsDisplayed()
        .assertTextContains("Your profile", substring = true, ignoreCase = true)
  }

  fun ComposeTestRule.checkSignInScreenIsNotDisplayed() {
    onNodeWithTag(SignInScreenTestTags.SignInText).assertDoesNotExist()
  }

  fun ComposeTestRule.checkSignInScreenIsDisplayed() {
    onNodeWithTag(NavigationTestTags.TOP_NAVIGATION_MENU)
        .assertIsDisplayed()
        .assertTextContains("SignIN page", substring = true, ignoreCase = true)
  }

  fun ComposeTestRule.checkMapScreenIsDisplayed() {
    onNodeWithTag(NavigationTestTags.TOP_NAVIGATION_MENU)
        .assertIsDisplayed()
        .assertTextContains("map", substring = true, ignoreCase = true)
  }
}
