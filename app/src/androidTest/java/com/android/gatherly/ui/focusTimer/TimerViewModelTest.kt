package com.android.gatherly.ui.focusTimer

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.utils.FirestoreGatherlyTest
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private const val TIMEOUT = 5000L
private const val DELAY = 200L

/** Test class to check that [TimerViewModel] functions correctly. */
@RunWith(AndroidJUnit4::class)
class TimerViewModelTest : FirestoreGatherlyTest() {

  private lateinit var viewModel: TimerViewModel

  private fun makeTodo(
      name: String,
      assignee: String = "user",
      description: String = "lorem ipsum",
      ownerId: String = "user123"
  ): ToDo {
    val now = Timestamp.now()
    return ToDo(
        uid = repository.getNewUid(),
        name = name,
        description = description,
        assigneeName = assignee,
        dueDate = now,
        dueTime = null,
        location = null,
        status = ToDoStatus.ONGOING,
        ownerId = ownerId)
  }

  @Before
  override fun setUp() {
    super.setUp()
    viewModel = TimerViewModel(repository)
  }

  /** Check that the timer counts down correctly from 5 seconds to 0. */
  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun timer_runs_out() = runTest {
    val hours = "00"
    val minutes = "00"
    val seconds = "05"

    viewModel.setHours(hours)
    viewModel.setMinutes(minutes)
    viewModel.setSeconds(seconds)

    viewModel.startTimer()

    withContext(Dispatchers.Default.limitedParallelism(1)) { delay(5000L) }

    val finalState = viewModel.uiState.value

    assertEquals("00", finalState.hours)
    assertEquals("00", finalState.minutes)
    assertEquals("00", finalState.seconds)
    assertFalse(finalState.isPaused)
    assertFalse(finalState.isStarted)
  }

  /** Check that the timer cannot be started with a duration of 0. */
  @Test
  fun cannot_be_0() = runTest {
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

  /** Check that the timer cannot be started with invalid hour input. */
  @Test
  fun cannot_parse_h() = runTest {
    val hours = "tn"
    val minutes = "00"
    val seconds = "00"

    viewModel.setHours(hours)
    viewModel.setMinutes(minutes)
    viewModel.setSeconds(seconds)

    val finalState = viewModel.uiState.value

    assertEquals("Invalid hour : Use numbers like 0–23 hours", finalState.errorMsg)
  }

  /** Check that the timer cannot be started with invalid minute input. */
  @Test
  fun cannot_parse_m() = runTest {
    val hours = "00"
    val minutes = "hdhhd"
    val seconds = "00"

    viewModel.setHours(hours)
    viewModel.setMinutes(minutes)
    viewModel.setSeconds(seconds)

    val finalState = viewModel.uiState.value

    assertEquals("Invalid minutes : Use numbers like 0–59 minutes", finalState.errorMsg)
  }

  /** Check that the timer cannot be started with invalid second input. */
  @Test
  fun cannot_parse_s() = runTest {
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
   * Check that getAllTodos() successfully retrieves data from the repository and updates the UI
   * state.
   */
  @Test
  fun getAllTodos_success_updatesUiStateWithData() = runTest {
    // Pre-populate repository
    val todo1 = makeTodo("Sample Todo 1")
    val todo2 = makeTodo("Sample Todo 2")
    repository.addTodo(todo1)
    repository.addTodo(todo2)

    // Wait for repository load
    withContext(Dispatchers.Default.limitedParallelism(1)) {
      withTimeout(TIMEOUT) {
        while (viewModel.uiState.value.allTodos.size < 2) {
          delay(DELAY)
          viewModel.getAllTodos()
        }
      }
    }

    // Trigger reload
    viewModel.getAllTodos()

    val state = viewModel.uiState.value
    assertNull("errorMsg should be null on success", state.errorMsg)
    assertEquals(2, state.allTodos.size)

    val names = state.allTodos.map { it.name }
    assertTrue("Sample Todo 1" in names)
    assertTrue("Sample Todo 2" in names)
  }

  /** Check that the timer cannot be started with invalid hours digit input. */
  @Test
  fun impossible_hours() = runTest {
    val hours = "26"
    val minutes = "00"
    val seconds = "00"

    viewModel.setHours(hours)
    viewModel.setMinutes(minutes)
    viewModel.setSeconds(seconds)

    val finalState = viewModel.uiState.value

    assertEquals("Invalid hour : Use numbers like 0–23 hours", finalState.errorMsg)
  }

  /** Check that the timer cannot be started with invalid minutes digit input. */
  @Test
  fun impossible_minutes() = runTest {
    val hours = "00"
    val minutes = "78"
    val seconds = "00"

    viewModel.setHours(hours)
    viewModel.setMinutes(minutes)
    viewModel.setSeconds(seconds)

    val finalState = viewModel.uiState.value

    assertEquals("Invalid minutes : Use numbers like 0–59 minutes", finalState.errorMsg)
  }

  /** Check that the timer cannot be started with invalid seconds digit input. */
  @Test
  fun impossible_seconds() = runTest {
    val hours = "00"
    val minutes = "00"
    val seconds = "98"

    viewModel.setHours(hours)
    viewModel.setMinutes(minutes)
    viewModel.setSeconds(seconds)

    val finalState = viewModel.uiState.value

    assertEquals("Invalid seconds : Use numbers like 0–59 seconds", finalState.errorMsg)
  }

  /** Check that a ToDo can be linked to the timer session correctly. */
  @Test
  fun can_link_todo() = runTest {
    val todo1 = makeTodo("Sample Todo 1")

    viewModel.linkToDo(todo1)

    val finalUiState = viewModel.uiState.value

    assertEquals(todo1, finalUiState.linkedTodo)
  }

  /** Check that a ToDo can be unlinked to the timer session correctly. */
  @Test
  fun can_unlink_todo() = runTest {
    val todo1 = makeTodo("Sample Todo 1")
    viewModel.linkToDo(todo1)
    viewModel.linkToDo(todo1)
    val finalUiState = viewModel.uiState.value

    assertEquals(null, finalUiState.linkedTodo)
  }
}
