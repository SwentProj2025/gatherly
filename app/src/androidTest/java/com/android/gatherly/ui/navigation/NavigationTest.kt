package com.android.gatherly.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.gatherly.GatherlyApp
import com.android.gatherly.ui.authentication.SignInScreenTestTags
import com.android.gatherly.utils.FirestoreGatherlyTest
import org.junit.Before
import org.junit.Test

class NavigationTest : FirestoreGatherlyTest() {
  // @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  override fun setUp() {
    super.setUp()
    composeTestRule.setContent { GatherlyApp() }
  }

  // LOGOUT PART :

  @Test
  fun canLogOutFromHomePage() {
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.LOGOUT_TAB).performClick()
    composeTestRule.checkSignInScreenIsDisplayed()
  }

  @Test
  fun canLogOutFromProfile() {
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.checkProfileScreenIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.LOGOUT_TAB).performClick()
    composeTestRule.checkSignInScreenIsDisplayed()
  }

  @Test
  fun canLogOutFromTimer() {
    composeTestRule.navigateFromHomeToTimer()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.LOGOUT_TAB).performClick()
    composeTestRule.checkSignInScreenIsDisplayed()
  }

  @Test
  fun canLogOutFromEvents() {
    composeTestRule.navigateFromHomeToEvents()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.LOGOUT_TAB).performClick()
    composeTestRule.checkSignInScreenIsDisplayed()
  }

  @Test
  fun canLogOutFromMap() {
    composeTestRule.navigateFromHomeToMap()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.LOGOUT_TAB).performClick()
    composeTestRule.checkSignInScreenIsDisplayed()
  }

  @Test
  fun canLogOutFromOverview() {
    composeTestRule.navigateFromHomeToOverview()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.LOGOUT_TAB).performClick()
    composeTestRule.checkSignInScreenIsDisplayed()
  }

  // HOME PAGE TESTS PART

  @Test
  fun NavigationBarAreCorrectlySetOnHome() {
    composeTestRule.onNodeWithTag(NavigationTestTags.TOP_NAVIGATION_MENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).assertIsDisplayed()
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
    composeTestRule.onNodeWithTag(NavigationTestTags.TOP_NAVIGATION_MENU).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
        .assertTextContains(value = "Home")
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.HOMEPAGE_TAB).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsNotDisplayed()
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
  }

  @Test
  fun canNavigateToSettingsFromHomePage() {
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_TAB).performClick()
    composeTestRule.checkSettingsScreenIsDisplayed()
  }

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

  @Test
  fun NavigateFromProfileToSettings() {
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.checkProfileScreenIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_TAB).performClick()
    composeTestRule.checkSettingsScreenIsDisplayed()
  }

  @Test
  fun NavigateFromProfileToTimer() {
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.checkProfileScreenIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.TIMER_TAB).performClick()
    composeTestRule.checkTimerScreenIsDisplayed()
  }

  @Test
  fun NavigateFromProfileToOverview() {
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.checkProfileScreenIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).performClick()
    composeTestRule.checkOverviewScreenIsDisplayed()
  }

  @Test
  fun NavigateFromProfileToEvents() {
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.checkProfileScreenIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.EVENTS_TAB).performClick()
    composeTestRule.checkEventsScreenIsDisplayed()
  }

  @Test
  fun NavigateFromProfileToMap() {
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.checkProfileScreenIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).performClick()
    composeTestRule.checkMapScreenIsDisplayed()
  }

  /*
    TODO : Enable this test after implementing Profile screen
  @Test
  fun NavigateFromProfileToFriends(){
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.checkProfileScreenIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileTestTags.FRIENDS_TAB).performClick()
    composeTestRule.checkFriendsInScreenIsDisplayed()
  }
   */

  /*
   TODO : Enable this test after implementing Profile screen

  @Test
  fun NavigateFromProfileToFriends(){
   composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
   composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
   composeTestRule.checkProfileScreenIsDisplayed()
   composeTestRule.onNodeWithTag(ProfileTestTags.FRIENDS_TAB).performClick()
   composeTestRule.checkFriendsScreenIsDisplayed()
  }

   */

  /*
    TODO : Enable this test after implementing Profile screen
    @Test
  fun canNavigateBackToProfileFromFriendsUsingSystemBack() {
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.friendsbutton).performClick()
    composeTestRule.checkFriendsScreenIsDisplayed()
    composeTestRule.checkProfileScreenIsNotDisplayed()
    pressBack(shouldFinish = false)
    composeTestRule.checkProfileScreenIsDisplayed()
  }*/

  // TIMER PART

  fun ComposeTestRule.navigateFromHomeToTimer() {
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.TIMER_TAB).performClick()
    composeTestRule.checkTimerScreenIsDisplayed()
  }

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

  @Test
  fun NavigateFromTimerToHomePage() {
    composeTestRule.navigateFromHomeToTimer()
    composeTestRule.onNodeWithTag(NavigationTestTags.HOMEPAGE_TAB).performClick()
    composeTestRule.checkHomeScreenIsDisplayed()
  }

  @Test
  fun NavigateFromTimerToSettings() {
    composeTestRule.navigateFromHomeToTimer()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_TAB).performClick()
    composeTestRule.checkSettingsScreenIsDisplayed()
  }

  @Test
  fun NavigateFromTimerToProfile() {
    composeTestRule.navigateFromHomeToTimer()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.checkProfileScreenIsDisplayed()
  }

  @Test
  fun NavigateFromTimerToOverview() {
    composeTestRule.navigateFromHomeToTimer()
    composeTestRule.onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).performClick()
    composeTestRule.checkOverviewScreenIsDisplayed()
  }

  @Test
  fun NavigateFromTimerToEvents() {
    composeTestRule.navigateFromHomeToTimer()
    composeTestRule.onNodeWithTag(NavigationTestTags.EVENTS_TAB).performClick()
    composeTestRule.checkEventsScreenIsDisplayed()
  }

  @Test
  fun NavigateFromTimerToMap() {
    composeTestRule.navigateFromHomeToTimer()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).performClick()
    composeTestRule.checkMapScreenIsDisplayed()
  }

  // EVENTS PART ( without add event or edit event )

  fun ComposeTestRule.navigateFromHomeToEvents() {
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.EVENTS_TAB).performClick()
    composeTestRule.checkEventsScreenIsDisplayed()
  }

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

  @Test
  fun NavigateFromEventsToHomePage() {
    composeTestRule.navigateFromHomeToEvents()
    composeTestRule.onNodeWithTag(NavigationTestTags.HOMEPAGE_TAB).performClick()
    composeTestRule.checkHomeScreenIsDisplayed()
  }

  @Test
  fun NavigateFromEventsToSettings() {
    composeTestRule.navigateFromHomeToEvents()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_TAB).performClick()
    composeTestRule.checkSettingsScreenIsDisplayed()
  }

  @Test
  fun NavigateFromEventsToProfile() {
    composeTestRule.navigateFromHomeToEvents()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.checkProfileScreenIsDisplayed()
  }

  @Test
  fun NavigateFromEventsToOverview() {
    composeTestRule.navigateFromHomeToEvents()
    composeTestRule.onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).performClick()
    composeTestRule.checkOverviewScreenIsDisplayed()
  }

  @Test
  fun NavigateFromEventsToTimer() {
    composeTestRule.navigateFromHomeToEvents()
    composeTestRule.onNodeWithTag(NavigationTestTags.TIMER_TAB).performClick()
    composeTestRule.checkTimerScreenIsDisplayed()
  }

  @Test
  fun NavigateFromEventsToMap() {
    composeTestRule.navigateFromHomeToEvents()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).performClick()
    composeTestRule.checkMapScreenIsDisplayed()
  }

  // MAP PART

  fun ComposeTestRule.navigateFromHomeToMap() {
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).performClick()
    composeTestRule.checkMapScreenIsDisplayed()
  }

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

  @Test
  fun NavigateFromMapToHomePage() {
    composeTestRule.navigateFromHomeToMap()
    composeTestRule.onNodeWithTag(NavigationTestTags.HOMEPAGE_TAB).performClick()
    composeTestRule.checkHomeScreenIsDisplayed()
  }

  @Test
  fun NavigateFromMapToSettings() {
    composeTestRule.navigateFromHomeToMap()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_TAB).performClick()
    composeTestRule.checkSettingsScreenIsDisplayed()
  }

  @Test
  fun NavigateFromMapToProfile() {
    composeTestRule.navigateFromHomeToMap()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.checkProfileScreenIsDisplayed()
  }

  @Test
  fun NavigateFromMapToOverview() {
    composeTestRule.navigateFromHomeToMap()
    composeTestRule.onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).performClick()
    composeTestRule.checkOverviewScreenIsDisplayed()
  }

  @Test
  fun NavigateFromMapToTimer() {
    composeTestRule.navigateFromHomeToMap()
    composeTestRule.onNodeWithTag(NavigationTestTags.TIMER_TAB).performClick()
    composeTestRule.checkTimerScreenIsDisplayed()
  }

  @Test
  fun NavigateFromMapToEvents() {
    composeTestRule.navigateFromHomeToMap()
    composeTestRule.onNodeWithTag(NavigationTestTags.EVENTS_TAB).performClick()
    composeTestRule.checkEventsScreenIsDisplayed()
  }

  // OVERVIEW PART with AddToDo (without EditToDo)
  fun ComposeTestRule.navigateFromHomeToOverview() {
    onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).performClick()
    checkOverviewScreenIsDisplayed()
  }

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

  @Test
  fun NavigateFromOverviewToSettings() {
    composeTestRule.navigateFromHomeToOverview()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_TAB).performClick()
    composeTestRule.checkSettingsScreenIsDisplayed()
  }

  @Test
  fun NavigateFromOverviewToProfile() {
    composeTestRule.navigateFromHomeToOverview()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.checkProfileScreenIsDisplayed()
  }

  @Test
  fun NavigateFromOverviewToTimer() {
    composeTestRule.navigateFromHomeToOverview()
    composeTestRule.onNodeWithTag(NavigationTestTags.TIMER_TAB).performClick()
    composeTestRule.checkTimerScreenIsDisplayed()
  }

  @Test
  fun NavigateFromOverviewToEvents() {
    composeTestRule.navigateFromHomeToOverview()
    composeTestRule.onNodeWithTag(NavigationTestTags.EVENTS_TAB).performClick()
    composeTestRule.checkEventsScreenIsDisplayed()
  }

  @Test
  fun NavigateFromOverviewToMap() {
    composeTestRule.navigateFromHomeToOverview()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).performClick()
    composeTestRule.checkMapScreenIsDisplayed()
  }

  /*

    TODO : Enable this tests when the AddToDo screen will be implemented :)
    @Test
    fun canNavigateFromOverviewToAddToDo() {
      composeTestRule.navigateFromHomeToOverview()
      composeTestRule.onNodeWithTag(OverviewScreenTestTags.CREATE_TODO_BUTTON).performClick()
      composeTestRule.checkAddTodoScreenIsDisplayed()
    }


    @Test
    fun NavigationBarIsNotDisplayedOnAddToDoScreen() {
      composeTestRule.navigateFromHomeToOverview()
      composeTestRule.onNodeWithTag(OverviewScreenTestTags.CREATE_TODO_BUTTON).performClick()
      composeTestRule.checkAddTodoScreenIsDisplayed()
      composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsNotDisplayed()
      composeTestRule.onNodeWithTag(NavigationTestTags.HOMEPAGE_TAB).assertIsNotDisplayed()
      composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).assertIsNotDisplayed()
    }
  */
  /*
    @Test
    fun canNavigateFromOverviewToEditToDo() {
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
      composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsNotDisplayed()

    }
  }
     */

  /*
  fun ComposeTestRule.navigateToEditToDoScreen(editedToDo: ToDo) {
    clickOnTodoItem(editedToDo)
  }
   */

  ///////////////   utils  ///////////////////////////

  fun ComposeTestRule.checkOverviewScreenIsDisplayed() {
    onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
        .assertTextContains("To-Do", substring = true, ignoreCase = true)
  }

  fun ComposeTestRule.checkProfileScreenIsDisplayed() {
    onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
        .assertTextContains("Your profile", substring = true, ignoreCase = true)
  }

  fun ComposeTestRule.checkSettingsScreenIsDisplayed() {
    onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
        .assertTextContains("Settings", substring = true, ignoreCase = true)
  }

  fun ComposeTestRule.checkTimerScreenIsDisplayed() {
    onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
        .assertTextContains("Timer", substring = true, ignoreCase = true)
  }

  fun ComposeTestRule.checkEventsScreenIsDisplayed() {
    onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
        .assertTextContains("Events", substring = true, ignoreCase = true)
  }

  fun ComposeTestRule.checkMapScreenIsDisplayed() {
    onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
        .assertTextContains("Map", substring = true, ignoreCase = true)
  }

  fun ComposeTestRule.checkHomeScreenIsDisplayed() {
    onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
        .assertTextContains("Home", substring = true, ignoreCase = true)
  }

  fun ComposeTestRule.checkSignInScreenIsDisplayed() {
    onNodeWithTag(SignInScreenTestTags.WELCOME_TITLE)
        .assertIsDisplayed()
        .assertTextContains("Welcome to Gatherly,", substring = true, ignoreCase = true)
  }

  fun ComposeTestRule.checkFriendsScreenIsDisplayed() {
    onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
        .assertTextContains("Friends", substring = true, ignoreCase = true)
  }

  fun ComposeTestRule.checkAddTodoScreenIsDisplayed() {
    onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
        .assertTextContains("Add To-Do", substring = true, ignoreCase = true)
  }

  fun ComposeTestRule.checkEditTodoScreenIsDisplayed() {
    onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
        .assertTextContains("Edit To-Do", substring = true, ignoreCase = true)
  }
}

/*
private fun pressBack(shouldFinish: Boolean) {
 composeTestRule.activityRule.scenario.onActivity { activity ->
   activity.onBackPressedDispatcher.onBackPressed()
 }
 composeTestRule.waitUntil { composeTestRule.activity.isFinishing == shouldFinish }
 assertEquals(shouldFinish, composeTestRule.activity.isFinishing)
}
 */

///////////// not for now  ////////////////////////
