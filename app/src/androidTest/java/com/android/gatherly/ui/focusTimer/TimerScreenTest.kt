package com.android.gatherly.ui.focusTimer

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.gatherly.model.focusSession.FocusSession
import com.android.gatherly.model.focusSession.FocusSessionsLocalRepository
import com.android.gatherly.model.focusSession.FocusSessionsRepository
import com.android.gatherly.model.notification.NotificationsLocalRepository
import com.android.gatherly.model.notification.NotificationsRepository
import com.android.gatherly.model.points.PointsLocalRepository
import com.android.gatherly.model.points.PointsRepository
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.profile.UserStatusManager
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todo.ToDosLocalRepository
import com.android.gatherly.model.todo.ToDosRepository
import com.android.gatherly.utils.MockitoUtils
import com.google.firebase.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TIMEOUT = 15000L

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
/** Tests the Timer display */
class TimerScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private val todo1 =
      ToDo(
          uid = "1",
          name = "Buy groceries",
          description = "Milk, eggs, bread",
          dueDate = Timestamp.now(),
          dueTime = null,
          location = null,
          status = ToDoStatus.ONGOING,
          ownerId = "test-user")

  private val focusSessionStartInstant1 =
      LocalDateTime.of(2025, 11, 11, 13, 35, 20).atZone(ZoneId.systemDefault()).toInstant()
  private val focusSessionEndInstant1 =
      LocalDateTime.of(2025, 11, 11, 14, 47, 45).atZone(ZoneId.systemDefault()).toInstant()
  private val focusSessionStartInstant2 =
      LocalDateTime.of(2025, 11, 12, 11, 25, 20).atZone(ZoneId.systemDefault()).toInstant()
  private val focusSessionEndInstant2 =
      LocalDateTime.of(2025, 11, 12, 14, 47, 45).atZone(ZoneId.systemDefault()).toInstant()
  private val focusSessionWithTodo =
      FocusSession(
          focusSessionId = "focusSession1",
          creatorId = "test-user",
          linkedTodoId = "1",
          duration = 1.hours + 12.minutes + 25.seconds,
          startedAt = Timestamp(Date.from(focusSessionStartInstant1)),
          endedAt = Timestamp(Date.from(focusSessionEndInstant1)),
      )
  private val focusSessionNoTodo =
      FocusSession(
          focusSessionId = "focusSession2",
          creatorId = "test-user",
          linkedTodoId = null,
          duration = 3.hours + 22.minutes + 25.seconds,
          startedAt = Timestamp(Date.from(focusSessionStartInstant2)),
          endedAt = Timestamp(Date.from(focusSessionEndInstant2)),
      )

  private lateinit var toDosRepository: ToDosRepository
  private lateinit var focusSessionsRepository: FocusSessionsRepository
  private lateinit var timerViewModel: TimerViewModel

  private lateinit var mockitoUtils: MockitoUtils
  private lateinit var profileRepository: ProfileRepository
  private lateinit var pointsRepository: PointsRepository
  private lateinit var notificationsRepository: NotificationsRepository
  private lateinit var userStatusManager: UserStatusManager
  private val fakeUid = "test-user"

  /**
   * Fills the repository and sets the viewModel and UI contents
   *
   * @param fillFocusSessions whether or not to put focus sessions in their repository
   */
  private fun setContent(fillFocusSessions: Boolean = true) {
    toDosRepository = ToDosLocalRepository()
    focusSessionsRepository = FocusSessionsLocalRepository()
    // Add a todo in the repository to test linking
    profileRepository = ProfileLocalRepository()
    pointsRepository = PointsLocalRepository()
    notificationsRepository = NotificationsLocalRepository()
    fill_repository(fillFocusSessions)

    mockitoUtils = MockitoUtils()
    mockitoUtils.chooseCurrentUser(fakeUid)
    userStatusManager = UserStatusManager(auth = mockitoUtils.mockAuth, repo = profileRepository)

    timerViewModel =
        TimerViewModel(
            todoRepository = toDosRepository,
            pointsRepository = pointsRepository,
            profileRepository = profileRepository,
            notificationsRepository = notificationsRepository,
            userStatusManager = userStatusManager,
            focusSessionsRepository = focusSessionsRepository,
            authProvider = { mockitoUtils.mockAuth })

    composeTestRule.setContent { TimerScreen(timerViewModel) }
  }

  /**
   * Fills the repositories
   *
   * @param fillFocusSessions true if focus sessions should be added
   */
  fun fill_repository(fillFocusSessions: Boolean = true) = runTest {
    toDosRepository.addTodo(todo1)
    profileRepository.addProfile(Profile(uid = fakeUid, name = "Test", profilePicture = ""))
    if (fillFocusSessions) {
      focusSessionsRepository.addFocusSession(focusSessionWithTodo)
      focusSessionsRepository.addFocusSession(focusSessionNoTodo)
    }
    advanceUntilIdle()
  }

  // Can enter a valid number of hours in the corresponding field
  @Test
  fun canWriteHours() {
    setContent()
    composeTestRule.assertScreenDisplaysTimerNotStarted()

    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.HOURS_TEXT).performTextClearance()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.HOURS_TEXT).performTextInput("10")

    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.HOURS_TEXT).assertTextContains("10")
  }

  // Can enter a valid number of minutes in the corresponding field
  @Test
  fun canWriteMinutes() {
    setContent()
    composeTestRule.assertScreenDisplaysTimerNotStarted()

    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.MINUTES_TEXT).performTextClearance()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.MINUTES_TEXT).performTextInput("10")

    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.MINUTES_TEXT).assertTextContains("10")
  }

  // Can enter a valid number of seconds in the corresponding field
  @Test
  fun canWriteSeconds() {
    setContent()
    composeTestRule.assertScreenDisplaysTimerNotStarted()

    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.SECONDS_TEXT).performTextClearance()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.SECONDS_TEXT).performTextInput("10")

    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.SECONDS_TEXT).assertTextContains("10")
  }

  // Starting the timer changes the screen
  @Test
  fun canStartTimer() {
    setContent()
    composeTestRule.assertScreenDisplaysTimerNotStarted()

    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.HOURS_TEXT).performTextClearance()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.HOURS_TEXT).performTextInput("10")

    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.START_BUTTON).performClick()

    composeTestRule.assertScreenDisplaysTimerStartedRunning()
  }

  // Resetting the timer puts all fields to 00
  @Test
  fun canResetTimer() {
    setContent()
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
    setContent()
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
    setContent()
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
    setContent()
    composeTestRule.assertScreenDisplaysTimerNotStarted()

    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.HOURS_TEXT).performTextClearance()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.HOURS_TEXT).performTextInput("10")

    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.START_BUTTON).performClick()

    composeTestRule.assertScreenDisplaysTimerStartedRunning()

    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.STOP_BUTTON).performClick()

    composeTestRule.assertScreenDisplaysTimerNotStarted()
  }

  // Linking a todo displays it when the timer starts
  @Test
  fun canLinkToDo() {
    setContent()
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

  // When going to the leaderboard section, we can view it
  @Test
  fun canSeeLeaderboard() {
    setContent()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.TIMER_SELECT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.HISTORY_SELECT).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(FocusTimerScreenTestTags.LEADERBOARD_SELECT)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.waitUntil(TIMEOUT) {
      composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.LEADERBOARD_LIST).isDisplayed()
    }
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.LEADERBOARD_LIST).assertIsDisplayed()
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

  /** Test: Verifies that the sessions history screen correctly displays focus sessions */
  @Test
  fun sessionsHistoryCorrectlyDisplaysFocusSessions() {
    setContent()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.TIMER_SELECT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.LEADERBOARD_SELECT).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(FocusTimerScreenTestTags.HISTORY_SELECT)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.waitUntil(TIMEOUT) {
      composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.HISTORY_LIST).isDisplayed()
    }
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.HISTORY_LIST).assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(FocusTimerScreenTestTags.getDurationTagForFocusSessionItem("focusSession1"))
        .assertTextContains("01:12:25")

    composeTestRule
        .onNodeWithTag(FocusTimerScreenTestTags.getTimestampTagForFocusSessionItem("focusSession1"))
        .assertTextContains("11/11/2025 - 13:35:20")

    composeTestRule
        .onNodeWithTag(FocusTimerScreenTestTags.getTodoTagForFocusSessionItem("focusSession1"))
        .assertTextContains("Buy groceries")

    composeTestRule
        .onNodeWithTag(FocusTimerScreenTestTags.getTodoTagForFocusSessionItem("focusSession2"))
        .assertTextContains("NO LINKED TODO")
  }

  /**
   * Test: Verifies that the sessions history screen correctly displays an empty list message when
   * there are no focus sessions
   */
  @Test
  fun sessionsHistoryCorrectlyDisplaysEmptyFocusSessions() {
    setContent(fillFocusSessions = false)
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.TIMER_SELECT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.LEADERBOARD_SELECT).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(FocusTimerScreenTestTags.HISTORY_SELECT)
        .assertIsDisplayed()
        .performClick()

    composeTestRule
        .onNodeWithTag(FocusTimerScreenTestTags.EMPTY_HISTORY_LIST_MSG)
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag(FocusTimerScreenTestTags.HISTORY_LIST).assertIsNotDisplayed()
  }
}
