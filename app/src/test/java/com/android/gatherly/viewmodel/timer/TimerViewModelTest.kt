package com.android.gatherly.viewmodel.timer

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.gatherly.model.focusSession.FocusSessionsLocalRepository
import com.android.gatherly.model.focusSession.FocusSessionsRepository
import com.android.gatherly.model.notification.NotificationsLocalRepository
import com.android.gatherly.model.notification.NotificationsRepository
import com.android.gatherly.model.points.PointsLocalRepository
import com.android.gatherly.model.points.PointsRepository
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.profile.ProfileStatus
import com.android.gatherly.model.profile.UserStatusManager
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todo.ToDosLocalRepository
import com.android.gatherly.model.todo.ToDosRepository
import com.android.gatherly.ui.focusTimer.TimerViewModel
import com.android.gatherly.utilstest.MockitoUtils
import com.google.firebase.Timestamp
import java.util.SortedMap
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.kotlin.verify

private const val TIMEOUT = 5000L
private const val DELAY = 200L
private const val SESSION_DELAY = 1000L
private val testTimeout = 120.seconds

/** Test class to check that [TimerViewModel] functions correctly. */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class TimerViewModelTest {

  private lateinit var toDosRepository: ToDosRepository
  private lateinit var viewModel: TimerViewModel
  private lateinit var statusManagerMock: UserStatusManager
  private lateinit var mockitoUtils: MockitoUtils
  private lateinit var profileRepository: ProfileRepository
  private lateinit var pointsRepository: PointsRepository
  private lateinit var notificationsRepository: NotificationsRepository
  private lateinit var focusSessionsRepository: FocusSessionsRepository

  private fun makeTodo(
      name: String,
      description: String = "lorem ipsum",
      ownerId: String = "user123"
  ): ToDo {
    val now = Timestamp.now()
    return ToDo(
        uid = toDosRepository.getNewUid(),
        name = name,
        description = description,
        dueDate = now,
        dueTime = null,
        location = null,
        status = ToDoStatus.ONGOING,
        ownerId = ownerId)
  }

  private val testDispatcher = UnconfinedTestDispatcher()

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)

    toDosRepository = ToDosLocalRepository()
    profileRepository = ProfileLocalRepository()
    pointsRepository = PointsLocalRepository()
    notificationsRepository = NotificationsLocalRepository()

    populateProfile()

    statusManagerMock = mock()
    mockitoUtils = MockitoUtils()
    mockitoUtils.chooseCurrentUser("fakeUid")
    focusSessionsRepository = FocusSessionsLocalRepository()
    viewModel =
        TimerViewModel(
            todoRepository = toDosRepository,
            pointsRepository = pointsRepository,
            profileRepository = profileRepository,
            notificationsRepository = notificationsRepository,
            userStatusManager = statusManagerMock,
            focusSessionsRepository = focusSessionsRepository,
            authProvider = { mockitoUtils.mockAuth })
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  val currentProfile =
      Profile(
          uid = "fakeUid",
          weeklyPoints = 75.0,
          friendUids = listOf("friend1", "friend2", "friend3", "friend4"))
  val friend1 = Profile(uid = "friend1", weeklyPoints = 200.0, friendUids = listOf("fakeUid"))
  val friend2 = Profile(uid = "friend2", weeklyPoints = 30.0, friendUids = listOf("fakeUid"))
  val friend3 = Profile(uid = "friend3", weeklyPoints = 80.0, friendUids = listOf("fakeUid"))
  val friend4 = Profile(uid = "friend4", weeklyPoints = 309.0, friendUids = listOf("fakeUid"))

  fun populateProfile() =
      runTest(testDispatcher, testTimeout) {
        profileRepository.addProfile(currentProfile)
        profileRepository.addProfile(friend1)
        profileRepository.addProfile(friend2)
        profileRepository.addProfile(friend3)
        profileRepository.addProfile(friend4)
      }

  /** Tests that the timer counts down correctly from 3 seconds to 0. */
  @Test
  fun timer_runs_out() =
      runTest(testDispatcher, testTimeout) {
        val hours = "00"
        val minutes = "00"
        val seconds = "03"

        viewModel.setHours(hours)
        viewModel.setMinutes(minutes)
        viewModel.setSeconds(seconds)

        viewModel.startTimer()

        withContext(Dispatchers.Default.limitedParallelism(1)) {
          withTimeout(7000L) {
            while (viewModel.uiState.value.let {
              it.hours != "00" || it.minutes != "00" || it.seconds != "00"
            }) {
              delay(100)
            }
          }
        }

        val finalState = viewModel.uiState.value

        assertEquals("00", finalState.hours)
        assertEquals("00", finalState.minutes)
        assertEquals("00", finalState.seconds)
        assertFalse(finalState.isPaused)
        assertFalse(finalState.isStarted)
      }

  /** Tests that the timer cannot be started with a duration of 0. */
  @Test
  fun cannot_be_0() =
      runTest(testDispatcher, testTimeout) {
        val hours = "00"
        val minutes = "00"
        val seconds = "00"

        viewModel.setHours(hours)
        viewModel.setMinutes(minutes)
        viewModel.setSeconds(seconds)

        viewModel.startTimer()

        val finalState = viewModel.uiState.value

        assertEquals("Planned duration must be greater than 0.", finalState.errorMsg)
      }

  /** Tests that the timer cannot be started with invalid hour input. */
  @Test
  fun cannot_parse_h() =
      runTest(testDispatcher, testTimeout) {
        val hours = "tn"
        val minutes = "00"
        val seconds = "00"

        viewModel.setHours(hours)
        viewModel.setMinutes(minutes)
        viewModel.setSeconds(seconds)

        val finalState = viewModel.uiState.value

        assertEquals("Invalid hour : Use numbers like 0–23 hours", finalState.errorMsg)
      }

  /** Tests that the timer cannot be started with invalid minute input. */
  @Test
  fun cannot_parse_m() =
      runTest(testDispatcher, testTimeout) {
        val hours = "00"
        val minutes = "hdhhd"
        val seconds = "00"

        viewModel.setHours(hours)
        viewModel.setMinutes(minutes)
        viewModel.setSeconds(seconds)

        val finalState = viewModel.uiState.value

        assertEquals("Invalid minutes : Use numbers like 0–59 minutes", finalState.errorMsg)
      }

  /** Tests that the timer cannot be started with invalid second input. */
  @Test
  fun cannot_parse_s() =
      runTest(testDispatcher, testTimeout) {
        val hours = "00"
        val minutes = "00"
        val seconds = "dgfyg"

        viewModel.setHours(hours)
        viewModel.setMinutes(minutes)
        viewModel.setSeconds(seconds)

        val finalState = viewModel.uiState.value

        assertEquals("Invalid seconds : Use numbers like 0–59 seconds", finalState.errorMsg)
      }

  /**
   * Tests that loadUI() successfully retrieves data from the repository and updates the UI state.
   */
  @Test
  fun loadUI_success_updatesUiStateWithData() =
      runTest(testDispatcher, testTimeout) {
        val todo1 = makeTodo("Sample Todo 1")
        val todo2 = makeTodo("Sample Todo 2")
        toDosRepository.addTodo(todo1)
        toDosRepository.addTodo(todo2)

        advanceUntilIdle()

        viewModel.loadUI()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull("errorMsg should be null on success", state.errorMsg)
        assertEquals(2, state.allTodos.size)

        val names = state.allTodos.map { it.name }
        assertTrue("Sample Todo 1" in names)
        assertTrue("Sample Todo 2" in names)

        val expectedLeaderboard: SortedMap<Double, List<Profile>> =
            sortedMapOf<Double, List<Profile>>(compareByDescending { it }).apply {
              this[friend4.weeklyPoints] = listOf(friend4)
              this[friend1.weeklyPoints] = listOf(friend1)
              this[friend3.weeklyPoints] = listOf(friend3)
              this[currentProfile.weeklyPoints] = listOf(currentProfile)
              this[friend2.weeklyPoints] = listOf(friend2)
            }

        assertEquals(5, state.leaderboard.keys.size)
        assertEquals(expectedLeaderboard, state.leaderboard)
      }

  /** Tests that the timer cannot be started with invalid hours digit input. */
  @Test
  fun impossible_hours() =
      runTest(testDispatcher, testTimeout) {
        val hours = "26"
        val minutes = "00"
        val seconds = "00"

        viewModel.setHours(hours)
        viewModel.setMinutes(minutes)
        viewModel.setSeconds(seconds)

        val finalState = viewModel.uiState.value

        assertEquals("Invalid hour : Use numbers like 0–23 hours", finalState.errorMsg)
      }

  /** Tests that the timer cannot be started with invalid minutes digit input. */
  @Test
  fun impossible_minutes() =
      runTest(testDispatcher, testTimeout) {
        val hours = "00"
        val minutes = "78"
        val seconds = "00"

        viewModel.setHours(hours)
        viewModel.setMinutes(minutes)
        viewModel.setSeconds(seconds)

        val finalState = viewModel.uiState.value

        assertEquals("Invalid minutes : Use numbers like 0–59 minutes", finalState.errorMsg)
      }

  /** Tests that the timer cannot be started with invalid seconds digit input. */
  @Test
  fun impossible_seconds() =
      runTest(testDispatcher, testTimeout) {
        val hours = "00"
        val minutes = "00"
        val seconds = "98"

        viewModel.setHours(hours)
        viewModel.setMinutes(minutes)
        viewModel.setSeconds(seconds)

        val finalState = viewModel.uiState.value

        assertEquals("Invalid seconds : Use numbers like 0–59 seconds", finalState.errorMsg)
      }

  /** Tests that a ToDo can be linked to the timer session correctly. */
  @Test
  fun can_link_todo() =
      runTest(testDispatcher, testTimeout) {
        val todo1 = makeTodo("Sample Todo 1")

        viewModel.linkToDo(todo1)

        val finalUiState = viewModel.uiState.value

        assertEquals(todo1, finalUiState.linkedTodo)
      }

  /** Tests that a ToDo can be unlinked from the timer session correctly. */
  @Test
  fun can_unlink_todo() =
      runTest(testDispatcher, testTimeout) {
        val todo1 = makeTodo("Sample Todo 1")
        viewModel.linkToDo(todo1)
        viewModel.linkToDo(todo1)
        val finalUiState = viewModel.uiState.value

        assertEquals(null, finalUiState.linkedTodo)
      }

  /** Tests that an error message can be cleared correctly. */
  @Test
  fun can_clear_message() =
      runTest(testDispatcher, testTimeout) {
        viewModel.setError("Test error message")
        viewModel.clearError()
        val finalUiState = viewModel.uiState.value

        assertEquals(null, finalUiState.errorMsg)
      }

  /** Tests that the timer can be ended correctly. */
  @Test
  fun can_end_timer() =
      runTest(testDispatcher, testTimeout) {
        val hours = "00"
        val minutes = "00"
        val seconds = "05"

        viewModel.setHours(hours)
        viewModel.setMinutes(minutes)
        viewModel.setSeconds(seconds)

        viewModel.startTimer()
        advanceUntilIdle()
        viewModel.endTimer()
        advanceUntilIdle()

        withContext(Dispatchers.Default.limitedParallelism(1)) { delay(6000L) }

        val finalState = viewModel.uiState.value

        assertEquals("00", finalState.hours)
        assertEquals("00", finalState.minutes)
        assertEquals("00", finalState.seconds)
        assertFalse(finalState.isPaused)
        assertFalse(finalState.isStarted)
      }

  /** Tests that the timer can be paused correctly. */
  @Test
  fun can_pause_timer() =
      runTest(testDispatcher, testTimeout) {
        val hours = "00"
        val minutes = "00"
        val seconds = "05"

        viewModel.setHours(hours)
        viewModel.setMinutes(minutes)
        viewModel.setSeconds(seconds)

        viewModel.startTimer()

        withContext(Dispatchers.Default.limitedParallelism(1)) { delay(1000L) }

        viewModel.pauseTimer()

        val finalState = viewModel.uiState.value

        assertEquals("00", finalState.hours)
        assertEquals("00", finalState.minutes)
        assertEquals("04", finalState.seconds)
        assertTrue(finalState.isPaused)
        assertTrue(finalState.isStarted)
      }

  /** Tests that the hours set function works correctly when given valid input. */
  @Test
  fun right_value_set_hours() =
      runTest(testDispatcher, testTimeout) {
        val hours = "05"
        val minutes = "00"
        val seconds = "00"

        viewModel.setHours(hours)
        viewModel.setMinutes(minutes)
        viewModel.setSeconds(seconds)

        val finalState = viewModel.uiState.value

        assertEquals("05", finalState.hours)
      }

  /** Tests that the minutes set function works correctly when given valid input. */
  @Test
  fun right_value_set_minutes() =
      runTest(testDispatcher, testTimeout) {
        val hours = "00"
        val minutes = "05"
        val seconds = "00"

        viewModel.setHours(hours)
        viewModel.setMinutes(minutes)
        viewModel.setSeconds(seconds)

        val finalState = viewModel.uiState.value

        assertEquals("05", finalState.minutes)
      }

  /** Tests that the seconds set function works correctly when given valid input. */
  @Test
  fun right_value_set_seconds() =
      runTest(testDispatcher, testTimeout) {
        val hours = "00"
        val minutes = "00"
        val seconds = "05"

        viewModel.setHours(hours)
        viewModel.setMinutes(minutes)
        viewModel.setSeconds(seconds)

        val finalState = viewModel.uiState.value

        assertEquals("05", finalState.seconds)
      }

  /** Tests that starting the timer sets the user status to FOCUSED. */
  @Test
  fun timer_start_sets_status_focused() =
      runTest(testDispatcher, testTimeout) {
        viewModel.setHours("00")
        viewModel.setMinutes("00")
        viewModel.setSeconds("10")

        viewModel.startTimer()

        advanceUntilIdle()

        verify(statusManagerMock, times(1)).setStatus(ProfileStatus.FOCUSED)
      }

  /** Tests that pausing the timer sets the user status to ONLINE. */
  @Test
  fun timer_pause_sets_status_online() =
      runTest(testDispatcher, testTimeout) {
        viewModel.setHours("00")
        viewModel.setMinutes("00")
        viewModel.setSeconds("10")

        viewModel.startTimer()
        advanceUntilIdle()

        viewModel.pauseTimer()
        advanceUntilIdle()

        verify(statusManagerMock, times(1)).setStatus(ProfileStatus.ONLINE)
      }

  /** Tests that ending the timer sets the user status to ONLINE. */
  @Test
  fun timer_end_sets_status_online() =
      runTest(testDispatcher, testTimeout) {
        viewModel.setHours("00")
        viewModel.setMinutes("00")
        viewModel.setSeconds("10")

        viewModel.startTimer()
        advanceUntilIdle()

        viewModel.endTimer()
        advanceUntilIdle()

        verify(statusManagerMock, times(1)).setStatus(ProfileStatus.ONLINE)
      }

  /**
   * Tests that when the timer naturally runs out (reaches 00:00:00), the user status is set back to
   * ONLINE.
   */
  @Test
  fun timer_runs_out_sets_status_online() =
      runTest(testDispatcher, testTimeout) {
        viewModel.setHours("00")
        viewModel.setMinutes("00")
        viewModel.setSeconds("03")

        viewModel.startTimer()
        advanceUntilIdle()

        withContext(Dispatchers.Default.limitedParallelism(1)) {
          withTimeout(7000L) {
            while (viewModel.uiState.value.let {
              it.hours != "00" || it.minutes != "00" || it.seconds != "00"
            }) {
              delay(100)
            }
          }
        }

        advanceUntilIdle()

        verify(statusManagerMock, times(1)).setStatus(ProfileStatus.FOCUSED)
        verify(statusManagerMock, times(1)).setStatus(ProfileStatus.ONLINE)
      }

  /** Tests that a focus session is created on start and finalized on end. */
  @Test
  fun creates_and_finalizes_focus_session() =
      runTest(testDispatcher, testTimeout) {
        val hours = "00"
        val minutes = "00"
        val seconds = "02"

        viewModel.setHours(hours)
        viewModel.setMinutes(minutes)
        viewModel.setSeconds(seconds)

        assertTrue(focusSessionsRepository.getUserFocusSessions().isEmpty())

        viewModel.startTimer()

        withContext(Dispatchers.Default.limitedParallelism(1)) {
          withTimeout(TIMEOUT) {
            while (focusSessionsRepository.getUserFocusSessions().isEmpty()) {
              delay(DELAY)
            }
          }
        }

        val sessions = focusSessionsRepository.getUserFocusSessions()
        assertEquals(1, sessions.size)

        val startedSession = sessions[0]

        assertNotNull("Session must have non-null startedAt", startedSession.startedAt)
        assertNull("Session should not yet have endedAt", startedSession.endedAt)
        assertEquals(0, startedSession.duration.inWholeSeconds)

        withContext(Dispatchers.Default.limitedParallelism(1)) { delay(SESSION_DELAY) }
        viewModel.endTimer()

        withContext(Dispatchers.Default.limitedParallelism(1)) { delay(SESSION_DELAY) }

        val finalSession = sessions[0]

        assertNotNull("endedAt must be set after ending timer", finalSession.endedAt)
        assertTrue(
            "duration should be > 0 after running timer", finalSession.duration.inWholeSeconds > 0)
      }
}
