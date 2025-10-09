package com.android.gatherly.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.gatherly.GatherlyApp
import com.android.gatherly.ui.overview.OverviewScreenTestTags
import com.android.gatherly.ui.profile.ProfileScreenTestTags
import com.github.se.bootcamp.utils.BootcampMilestone
import com.google.firebase.auth.FirebaseAuth
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// ***************************************************************************** //
// ***                                                                       *** //
// *** THIS FILE WILL BE OVERWRITTEN DURING GRADING. IT SHOULD BE LOCATED IN *** //
// *** `app/src/androidTest/java/com/github/se/bootcamp/ui/navigation`.        *** //
// *** DO **NOT** IMPLEMENT YOUR OWN TESTS IN THIS FILE                      *** //
// ***                                                                       *** //
// ***************************************************************************** //

class NavigationTest : InMemoryBootcampTest(BootcampMilestone.B2) {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  override fun setUp() {
    super.setUp()
    runBlocking {
      FirebaseAuth.getInstance().signInAnonymously().await()
    }
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
    composeTestRule.onNodeWithTag(NavigationTestTags.TOP_NAVIGATION_MENU)
      .assertIsDisplayed().assertTextContains(value = "Home")
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

  @Test
  fun bottomNavigationIsDisplayedForOverview() {
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
  }

  @Test
  fun bottomNavigationIsDisplayedForMap() {
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
  }

  @Test
  fun tabsAreClickable() {
    composeTestRule
        .onNodeWithTag(NavigationTestTags.OVERVIEW_TAB)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).assertIsDisplayed().performClick()
  }

  @Test
  fun topBarTitleIsCorrectForHome() {
    composeTestRule
        .onNodeWithTag(NavigationTestTags.TOP_NAVIGATION_MENU)
        .assertIsDisplayed()
        .assertTextContains(value = "HOME")
  }

  @Test
  fun topBarTitleIsCorrectForMap() {
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).performClick()
    composeTestRule
        .onNodeWithTag(NavigationTestTags.TOP_NAVIGATION_MENU)
        .assertIsDisplayed()
        .assertTextContains(value = "Map")
  }

  @Test
  fun topBarTitleIsCorrectForOverview() {
    composeTestRule.onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).performClick()
    composeTestRule
      .onNodeWithTag(NavigationTestTags.TOP_NAVIGATION_MENU)
      .assertIsDisplayed()
      .assertTextContains(value = "Overview")
  }


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
  fun topBarTitleIsCorrectForAddToDo() {
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.CREATE_TODO_BUTTON).performClick()
    composeTestRule
        .onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
        .assertIsDisplayed()
        .assertTextContains(value = "Create a new task", substring = false, ignoreCase = true)
  }

  @Test
  fun bottomNavigationNotDisplayedForAddToDo() {
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.CREATE_TODO_BUTTON).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertDoesNotExist()
  }

  @Test
  fun navigationStartsOnOverviewTab() {
    composeTestRule.checkOverviewScreenIsDisplayed()
  }

  @Test
  fun canNavigateToMap() {
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).performClick()
    composeTestRule.checkMapScreenIsDisplayed()
    composeTestRule.checkOverviewScreenIsNotDisplayed()
  }

  @Test
  fun canNavigateToMapAndBackToOverview() {
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).performClick()
    composeTestRule.checkOverviewScreenIsDisplayed()
  }

  //  @Test
  fun canNavigateBackToMapAndBackToOverviewUsingSystemBack() {
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).performClick()
    composeTestRule.checkOverviewScreenIsNotDisplayed()
    composeTestRule.checkMapScreenIsDisplayed()
    pressBack(shouldFinish = false)
    composeTestRule.checkOverviewScreenIsDisplayed()
  }

  @Test
  fun canNavigateToAddToDo() {
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.CREATE_TODO_BUTTON).performClick()
    composeTestRule.checkAddToDoScreenIsDisplayed()
    composeTestRule.checkOverviewScreenIsNotDisplayed()
  }

  @Test
  fun canNavigateBackToOverviewFromAddToDo() {
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.CREATE_TODO_BUTTON).performClick()
    composeTestRule.checkAddToDoScreenIsDisplayed()
    composeTestRule.checkOverviewScreenIsNotDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.GO_BACK_BUTTON).performClick()
    composeTestRule.checkOverviewScreenIsDisplayed()
  }

  //  @Test
  fun canNavigateBackToOverviewFromAddToDoUsingSystemBack() {
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.CREATE_TODO_BUTTON).performClick()
    composeTestRule.checkAddToDoScreenIsDisplayed()
    composeTestRule.checkOverviewScreenIsNotDisplayed()
    pressBack(shouldFinish = false)
    composeTestRule.checkOverviewScreenIsDisplayed()
  }
  */

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


  private fun pressBack(shouldFinish: Boolean) {
    composeTestRule.activityRule.scenario.onActivity { activity ->
      activity.onBackPressedDispatcher.onBackPressed()
    }
    composeTestRule.waitUntil { composeTestRule.activity.isFinishing == shouldFinish }
    assertEquals(shouldFinish, composeTestRule.activity.isFinishing)
  }
}


/*

  @Test
  fun canNavigateToEditToDoScreenFromOverview() {
    composeTestRule.navigateToEditToDoScreen(firstTodo)
    composeTestRule.checkEditToDoScreenIsDisplayed()
    composeTestRule.checkOverviewScreenIsNotDisplayed()
  }

  @Test
  fun addTodo_saveButtonNavigatesToOverviewToDoIfInputIsValid() {
    composeTestRule.navigateToAddToDoScreen()
    composeTestRule.enterAddTodoDetails(todo = validTodo)
    composeTestRule.clickOnSaveForAddTodo(waitForRedirection = true)
    composeTestRule.checkOverviewScreenIsDisplayed()
  }

  @Test
  fun editTodo_saveButtonNavigatesToOverviewToDoIfInputIsValid() {
    composeTestRule.navigateToEditToDoScreen(firstTodo)
    composeTestRule.clickOnSaveForEditTodo(waitForRedirection = true)
    composeTestRule.checkOverviewScreenIsDisplayed()
  }

  @Test
  fun topAppTitleIsCorrectOnEditToDoScreen() {
    composeTestRule.checkOverviewScreenIsDisplayed()
    composeTestRule.navigateToEditToDoScreen(firstTodo)
    composeTestRule.checkEditToDoScreenIsDisplayed()
  }

  @Test
  fun bottomBarIsNotDisplayedOnEditToDoScreen() {
    composeTestRule.checkOverviewScreenIsDisplayed()
    composeTestRule.clickOnTodoItem(firstTodo)
    composeTestRule.checkEditToDoScreenIsDisplayed()
    composeTestRule.checkBottomBarIsNotDisplayed()
  }
 */
