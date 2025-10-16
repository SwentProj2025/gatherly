package com.android.gatherly.ui.focusTimer

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.gatherly.utils.FirestoreGatherlyTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class TimerScreenTest : FirestoreGatherlyTest() {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  override fun setUp() {

    super.setUp()
    runBlocking {
      println("start run test")
      repository.addTodo(todo1)
    }

    composeTestRule.setContent { TimerScreen(timerViewModel = TimerViewModel(repository)) }
  }

  @Test
  fun canWriteHours() {
    composeTestRule.assertScreenDisplaysTimerNotStarted()
    println("display asserted")
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.HOURS_TEXT).performTextClearance()
    println("text cleared")
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.HOURS_TEXT).performTextInput("10")
    println("10 written")
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.HOURS_TEXT).assertTextContains("10")
    println("assert 10 in text")
  }

  @Test
  fun canWriteMinutes() {
    composeTestRule.assertScreenDisplaysTimerNotStarted()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.MINUTES_TEXT).performTextClearance()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.MINUTES_TEXT).performTextInput("10")
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.MINUTES_TEXT).assertTextContains("10")
  }

  @Test
  fun canWriteSeconds() {
    composeTestRule.assertScreenDisplaysTimerNotStarted()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.SECONDS_TEXT).performTextClearance()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.SECONDS_TEXT).performTextInput("10")
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.SECONDS_TEXT).assertTextContains("10")
  }

  @Test
  fun canStartTimer() {
    composeTestRule.assertScreenDisplaysTimerNotStarted()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.HOURS_TEXT).performTextClearance()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.HOURS_TEXT).performTextInput("10")
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.START_BUTTON).performClick()
    composeTestRule.assertScreenDisplaysTimerStartedRunning()
  }

  @Test
  fun canResetTimer() {
    composeTestRule.assertScreenDisplaysTimerNotStarted()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.HOURS_TEXT).performTextClearance()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.HOURS_TEXT).performTextInput("10")
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.MINUTES_TEXT).performTextClearance()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.MINUTES_TEXT).performTextInput("10")
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.SECONDS_TEXT).performTextClearance()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.SECONDS_TEXT).performTextInput("10")
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.RESET_BUTTON).performClick()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.HOURS_TEXT).assertTextContains("00")
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.MINUTES_TEXT).assertTextContains("00")
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.SECONDS_TEXT).assertTextContains("00")
  }

  @Test
  fun canPauseTimer() {
    composeTestRule.assertScreenDisplaysTimerNotStarted()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.HOURS_TEXT).performTextClearance()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.HOURS_TEXT).performTextInput("10")
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.START_BUTTON).performClick()
    composeTestRule.assertScreenDisplaysTimerStartedRunning()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.PAUSE_BUTTON).performClick()
    composeTestRule.assertScreenDisplaysTimerStartedPaused()
  }

  @Test
  fun canResumeTimer() {
    composeTestRule.assertScreenDisplaysTimerNotStarted()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.HOURS_TEXT).performTextClearance()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.HOURS_TEXT).performTextInput("10")
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.START_BUTTON).performClick()
    composeTestRule.assertScreenDisplaysTimerStartedRunning()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.PAUSE_BUTTON).performClick()
    composeTestRule.assertScreenDisplaysTimerStartedPaused()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.RESUME_BUTTON).performClick()
    composeTestRule.assertScreenDisplaysTimerStartedRunning()
  }

  @Test
  fun canStopTimer() {
    composeTestRule.assertScreenDisplaysTimerNotStarted()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.HOURS_TEXT).performTextClearance()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.HOURS_TEXT).performTextInput("10")
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.START_BUTTON).performClick()
    composeTestRule.assertScreenDisplaysTimerStartedRunning()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.STOP_BUTTON).performClick()
    composeTestRule.assertScreenDisplaysTimerNotStarted()
  }

  @Test
  fun canLinkToDo() {
    composeTestRule.assertScreenDisplaysTimerNotStarted()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.TODO_TO_CHOOSE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.TODO_TO_CHOOSE).performClick()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.HOURS_TEXT).performTextClearance()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.HOURS_TEXT).performTextInput("10")
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.START_BUTTON).performClick()
    composeTestRule.assertScreenDisplaysTimerStartedRunning()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.LINKED_TODO).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(FocusTimerScreenTestTags.LINKED_TODO)
        .assertTextContains(todo1.name, substring = true)
  }

  fun ComposeTestRule.assertScreenDisplaysTimerNotStarted() {
    onNodeWithTag(FocusTimerScreenTestTags.HOURS_TEXT).assertIsDisplayed()
    onNodeWithTag(FocusTimerScreenTestTags.MINUTES_TEXT).assertIsDisplayed()
    onNodeWithTag(FocusTimerScreenTestTags.SECONDS_TEXT).assertIsDisplayed()
    onNodeWithTag(FocusTimerScreenTestTags.START_BUTTON).assertIsDisplayed()
    onNodeWithTag(FocusTimerScreenTestTags.RESET_BUTTON).assertIsDisplayed()
  }

  fun ComposeTestRule.assertScreenDisplaysTimerStartedRunning() {
    onNodeWithTag(FocusTimerScreenTestTags.TIMER_CIRCLE).assertIsDisplayed()
    onNodeWithTag(FocusTimerScreenTestTags.TIMER_TIME).assertIsDisplayed()
    onNodeWithTag(FocusTimerScreenTestTags.PAUSE_BUTTON).assertIsDisplayed()
    onNodeWithTag(FocusTimerScreenTestTags.STOP_BUTTON).assertIsDisplayed()
  }

  fun ComposeTestRule.assertScreenDisplaysTimerStartedPaused() {
    onNodeWithTag(FocusTimerScreenTestTags.TIMER_CIRCLE).assertIsDisplayed()
    onNodeWithTag(FocusTimerScreenTestTags.TIMER_TIME).assertIsDisplayed()
    onNodeWithTag(FocusTimerScreenTestTags.RESUME_BUTTON).assertIsDisplayed()
    onNodeWithTag(FocusTimerScreenTestTags.STOP_BUTTON).assertIsDisplayed()
  }
}
