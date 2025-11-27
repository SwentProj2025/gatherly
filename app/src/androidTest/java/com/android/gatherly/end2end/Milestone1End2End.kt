package com.android.gatherly.end2end

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.rule.GrantPermissionRule
import com.android.gatherly.GatherlyApp
import com.android.gatherly.ui.authentication.SignInScreenTestTags
import com.android.gatherly.ui.focusTimer.FocusTimerScreenTestTags
import com.android.gatherly.ui.homePage.HomePageScreenTestTags
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.todo.AddToDoScreenTestTags
import com.android.gatherly.ui.todo.OverviewScreenTestTags
import com.android.gatherly.utils.FirebaseEmulator
import com.android.gatherly.utils.FirestoreGatherlyTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class Milestone1End2End : FirestoreGatherlyTest() {
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
    composeTestRule.setContent { GatherlyApp() }
  }

  // an end to end test with what was implemented in the M1
  @Test
  fun testAddTodoThenTimer() {
    // sign in anonymously
    composeTestRule.onNodeWithTag(SignInScreenTestTags.ANONYMOUS_BUTTON).performClick()

    // wait for homescreen to load
    composeTestRule.waitUntil(TIMEOUT) {
      composeTestRule.onNodeWithTag(HomePageScreenTestTags.UPCOMING_EVENTS_TITLE).isDisplayed()
    }

    // go to todos tab
    composeTestRule.onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).performClick()
    composeTestRule.waitForIdle()

    // wait for it to appear
    composeTestRule.waitUntil(TIMEOUT) {
      composeTestRule.onNodeWithTag(OverviewScreenTestTags.CREATE_TODO_BUTTON).isDisplayed()
    }

    // click to create a todo
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.CREATE_TODO_BUTTON).performClick()
    composeTestRule.waitForIdle()

    // wait for add todo screen to appear
    composeTestRule.waitUntil(TIMEOUT) {
      composeTestRule.onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_TITLE).isDisplayed()
    }

    composeTestRule
        .onNodeWithTag(AddToDoScreenTestTags.MORE_OPTIONS)
        .assertIsDisplayed()
        .performClick()
    // input information and save
    composeTestRule.onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_TITLE).performTextInput("Title")
    composeTestRule
        .onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_DESCRIPTION)
        .performTextInput("Description")
    composeTestRule
        .onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_DATE)
        .performTextInput("20/12/2025")
    composeTestRule.onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_TIME).performTextInput("10:00")
    composeTestRule.onNodeWithTag(AddToDoScreenTestTags.TODO_SAVE).performClick()
    composeTestRule.waitForIdle()

    // wait for overview todos to appear again
    composeTestRule.waitUntil(TIMEOUT) {
      composeTestRule.onNodeWithTag(OverviewScreenTestTags.CREATE_TODO_BUTTON).isDisplayed()
    }

    // go to timer tab
    composeTestRule.onNodeWithTag(NavigationTestTags.TIMER_TAB).performClick()
    composeTestRule.waitForIdle()

    // wait for timer tab to appear
    composeTestRule.waitUntil(TIMEOUT) {
      composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.START_BUTTON).isDisplayed()
    }

    // choose my todo, start 1 minute timer
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.TODO_TO_CHOOSE).performClick()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.MINUTES_TEXT).performTextClearance()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.MINUTES_TEXT).performTextInput("01")
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.START_BUTTON).performClick()

    // wait for timer to start
    composeTestRule.waitUntil(TIMEOUT) {
      composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.TIMER_CIRCLE).isDisplayed()
    }

    // stop timer
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.STOP_BUTTON).performClick()

    // click on drop down menu
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.waitForIdle()

    // wait for drop down menu to appear
    composeTestRule.waitUntil(TIMEOUT) {
      composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_TAB).isDisplayed()
    }

    // go to settings screen
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_TAB).performClick()

    // click on drop down menu
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.waitForIdle()

    // wait for drop down menu to appear
    composeTestRule.waitUntil(TIMEOUT) {
      composeTestRule.onNodeWithTag(NavigationTestTags.LOGOUT_TAB).isDisplayed()
    }

    // log out
    composeTestRule.onNodeWithTag(NavigationTestTags.LOGOUT_TAB).performClick()
  }
}
