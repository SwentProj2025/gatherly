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
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todo.ToDosLocalRepository
import com.android.gatherly.model.todo.ToDosRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class TimerScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private val todo1 =
      ToDo(
          uid = "1",
          name = "Buy groceries",
          description = "Milk, eggs, bread",
          assigneeName = "Alice",
          dueDate = Timestamp.now(),
          dueTime = null,
          location = null,
          status = ToDoStatus.ONGOING,
          ownerId = "test-user")

  private lateinit var toDosRepository: ToDosRepository
  private lateinit var timerViewModel: TimerViewModel

  @Before
  fun setUp() {
    toDosRepository = ToDosLocalRepository()
    // Add a todo in the repository to test linking
    fill_repository()
    timerViewModel = TimerViewModel(toDosRepository)

    composeTestRule.setContent { TimerScreen(timerViewModel) }
  }

  fun fill_repository() = runTest {
    toDosRepository.addTodo(todo1)
    advanceUntilIdle()
  }

  // Can enter a valid number of hours in the corresponding field
  @Test
  fun canWriteHours() {
    composeTestRule.assertScreenDisplaysTimerNotStarted()

    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.HOURS_TEXT).performTextClearance()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.HOURS_TEXT).performTextInput("10")

    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.HOURS_TEXT).assertTextContains("10")
  }

  // Can enter a valid number of minutes in the corresponding field
  @Test
  fun canWriteMinutes() {
    composeTestRule.assertScreenDisplaysTimerNotStarted()

    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.MINUTES_TEXT).performTextClearance()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.MINUTES_TEXT).performTextInput("10")

    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.MINUTES_TEXT).assertTextContains("10")
  }

  // Can enter a valid number of seconds in the corresponding field
  @Test
  fun canWriteSeconds() {
    composeTestRule.assertScreenDisplaysTimerNotStarted()

    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.SECONDS_TEXT).performTextClearance()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.SECONDS_TEXT).performTextInput("10")

    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.SECONDS_TEXT).assertTextContains("10")
  }

  // Starting the timer changes the screen
  @Test
  fun canStartTimer() {
    composeTestRule.assertScreenDisplaysTimerNotStarted()

    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.HOURS_TEXT).performTextClearance()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.HOURS_TEXT).performTextInput("10")

    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.START_BUTTON).performClick()

    composeTestRule.assertScreenDisplaysTimerStartedRunning()
  }

  // Resetting the timer puts all fields to 00
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

  // Pausing the timer updates correctly the pause/resume button
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

  // Resuming the timer updates correctly the pause/resume button
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

  // Stopping the timer displays the editing time screen
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

  // Linking a todo displys it when the timer starts
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

  // Helper function to check all test tags on the editing timer time screen
  fun ComposeTestRule.assertScreenDisplaysTimerNotStarted() {
    onNodeWithTag(FocusTimerScreenTestTags.HOURS_TEXT).assertIsDisplayed()
    onNodeWithTag(FocusTimerScreenTestTags.MINUTES_TEXT).assertIsDisplayed()
    onNodeWithTag(FocusTimerScreenTestTags.SECONDS_TEXT).assertIsDisplayed()
    onNodeWithTag(FocusTimerScreenTestTags.START_BUTTON).assertIsDisplayed()
    onNodeWithTag(FocusTimerScreenTestTags.RESET_BUTTON).assertIsDisplayed()
  }

  // Helper function to check all test tags on the running timer screen
  fun ComposeTestRule.assertScreenDisplaysTimerStartedRunning() {
    onNodeWithTag(FocusTimerScreenTestTags.TIMER_CIRCLE).assertIsDisplayed()
    onNodeWithTag(FocusTimerScreenTestTags.TIMER_TIME).assertIsDisplayed()
    onNodeWithTag(FocusTimerScreenTestTags.PAUSE_BUTTON).assertIsDisplayed()
    onNodeWithTag(FocusTimerScreenTestTags.STOP_BUTTON).assertIsDisplayed()
  }

  // Helper function to check all test tags on the paused timer screen
  fun ComposeTestRule.assertScreenDisplaysTimerStartedPaused() {
    onNodeWithTag(FocusTimerScreenTestTags.TIMER_CIRCLE).assertIsDisplayed()
    onNodeWithTag(FocusTimerScreenTestTags.TIMER_TIME).assertIsDisplayed()
    onNodeWithTag(FocusTimerScreenTestTags.RESUME_BUTTON).assertIsDisplayed()
    onNodeWithTag(FocusTimerScreenTestTags.STOP_BUTTON).assertIsDisplayed()
  }
}
