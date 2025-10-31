package com.android.gatherly.end2end

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.android.gatherly.GatherlyApp
import com.android.gatherly.ui.authentication.InitProfileScreenTestTags
import com.android.gatherly.ui.authentication.SignInScreenTestTags
import com.android.gatherly.ui.focusTimer.FocusTimerScreenTestTags
import com.android.gatherly.ui.homePage.HomePageScreenTestTags
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.theme.GatherlyTheme
import com.android.gatherly.ui.todo.AddToDoScreenTestTags
import com.android.gatherly.ui.todo.OverviewScreenTestTags
import com.android.gatherly.utils.FirebaseEmulator
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class Milestone1End2End {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  val TIMEOUT = 5000L

  // set content
  @Before
  fun setUp() {
    if (!FirebaseEmulator.isRunning) {
      error("Firebase emulator must be running! Use: firebase emulators:start")
    }

    composeTestRule.setContent {
      GatherlyTheme(darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) { GatherlyApp() }
      }
    }
  }

  // make sure to clear Firebase emulators
  @After
  fun tearDown() {
    FirebaseEmulator.auth.signOut()
    FirebaseEmulator.clearAuthEmulator()
  }

  // an end to end test with what was implemented in the M1
  @Test
  fun testAddTodoThenTimer() {
    // sign in anonymously
    composeTestRule.onNodeWithTag(SignInScreenTestTags.ANONYMOUS_BUTTON).performClick()

    // Fill profile init screen
    composeTestRule.waitUntil(TIMEOUT) {
      composeTestRule.onNodeWithTag(InitProfileScreenTestTags.SAVE_BUTTON).isDisplayed()
    }

    // Fill username and name (mandatory fields)
    composeTestRule.onNodeWithTag(InitProfileScreenTestTags.USERNAME).performTextInput("testuser")
    composeTestRule
        .onNodeWithTag(InitProfileScreenTestTags.NAME_FIELD)
        .performTextInput("Test User")

    // Save so navigate automatically to HomePage
    composeTestRule.onNodeWithTag(InitProfileScreenTestTags.SAVE_BUTTON).performClick()

    // wait for homescreen to load
    composeTestRule.waitUntil(TIMEOUT) {
      composeTestRule.onNodeWithTag(HomePageScreenTestTags.HOMETEXT).isDisplayed()
    }

    // go to todos tab
    composeTestRule.onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).performClick()

    // wait for it to appear
    composeTestRule.waitUntil(TIMEOUT) {
      composeTestRule.onNodeWithTag(OverviewScreenTestTags.CREATE_TODO_BUTTON).isDisplayed()
    }

    // click to create a todo
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.CREATE_TODO_BUTTON).performClick()

    // wait for add todo screen to appear
    composeTestRule.waitUntil(TIMEOUT) {
      composeTestRule.onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_TITLE).isDisplayed()
    }

    // input information and save
    composeTestRule.onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_TITLE).performTextInput("Title")
    composeTestRule
        .onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_DESCRIPTION)
        .performTextInput("Description")
    composeTestRule
        .onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_ASSIGNEE)
        .performTextInput("Assignee")
    composeTestRule
        .onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_DATE)
        .performTextInput("20/12/2025")
    composeTestRule.onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_TIME).performTextInput("10:00")
    composeTestRule.onNodeWithTag(AddToDoScreenTestTags.TODO_SAVE).performClick()

    // wait for overview todos to appear again
    composeTestRule.waitUntil(TIMEOUT) {
      composeTestRule.onNodeWithTag(OverviewScreenTestTags.CREATE_TODO_BUTTON).isDisplayed()
    }

    // go to timer tab
    composeTestRule.onNodeWithTag(NavigationTestTags.TIMER_TAB).performClick()

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

    // wait for drop down menu to appear
    composeTestRule.waitUntil(TIMEOUT) {
      composeTestRule.onNodeWithTag(NavigationTestTags.LOGOUT_TAB).isDisplayed()
    }

    // log out
    composeTestRule.onNodeWithTag(NavigationTestTags.LOGOUT_TAB).performClick()
  }
}
