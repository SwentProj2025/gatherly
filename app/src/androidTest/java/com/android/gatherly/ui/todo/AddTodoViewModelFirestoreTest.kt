package com.android.gatherly.ui.todo

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todo.ToDosRepository
import com.android.gatherly.utils.FirestoreGatherlyTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private const val TIMEOUT = 100_000L
private const val DELAY = 500L

/**
 * Integration tests for [AddTodoViewModel] using the real Firestore repository (via emulator).
 *
 * These tests verify that valid inputs lead to successful writes in Firestore, and invalid inputs
 * are rejected without repository side effects.
 */
@RunWith(AndroidJUnit4::class)
class AddTodoViewModelFirestoreTest : FirestoreGatherlyTest() {

  private lateinit var viewModel: AddTodoViewModel

  @Before
  override fun setUp() {
    super.setUp()
    viewModel = AddTodoViewModel(repository)
  }

  @Test
  fun saveTodo_withValidFields_savesToFirestore() = runTest {
    viewModel.onTitleChanged("Study session")
    viewModel.onDescriptionChanged("Revise for algorithms exam")
    viewModel.onAssigneeChanged("Claud")
    viewModel.onDateChanged("10/10/2025")
    viewModel.onTimeChanged("13:30")

    viewModel.saveTodo()

    // Snippet of code to wait until saving is done:
    withContext(Dispatchers.Default.limitedParallelism(1)) {
      withTimeout(TIMEOUT) {
        while (!viewModel.uiState.value.saveSuccess && viewModel.uiState.value.saveError == null) {
          delay(DELAY)
        }
      }
    }

    // Check UI state
    val state = viewModel.uiState.value
    assertTrue(state.saveSuccess)
    assertNull(state.saveError)

    // Check repository contents
    val todos = repository.getAllTodos()
    assertEquals(1, todos.size)
    val saved = todos.first()

    assertEquals("Study session", saved.name)
    assertEquals("Revise for algorithms exam", saved.description)
    assertEquals("Claud", saved.assigneeName)
    assertEquals(ToDoStatus.ONGOING, saved.status)
    assertNotNull(saved.dueDate)
  }

  @Test
  fun saveTodo_withInvalidDate_doesNotSave() = runTest {
    viewModel.onTitleChanged("Invalid date task")
    viewModel.onDescriptionChanged("Desc")
    viewModel.onAssigneeChanged("Someone")
    viewModel.onDateChanged("10-10-2025") // Wrong format

    viewModel.saveTodo()
    delay(DELAY)

    val state = viewModel.uiState.value
    assertNotNull(state.dueDateError)
    assertFalse(state.saveSuccess)

    // Repo should still be empty
    assertEquals(0, getTodosCount())
  }

  @Test
  fun saveTodo_withMissingFields_doesNotSave() = runTest {
    viewModel.onTitleChanged("") // Missing title
    viewModel.onDescriptionChanged("Some description")
    viewModel.onAssigneeChanged("User")
    viewModel.onDateChanged("10/10/2025")

    viewModel.saveTodo()
    delay(DELAY)

    val state = viewModel.uiState.value
    assertNotNull(state.titleError)
    assertFalse(state.saveSuccess)

    assertEquals(0, getTodosCount())
  }

  @Test
  fun multipleValidTodos_areSavedIndependently() = runTest {
    // First ToDo
    viewModel.onTitleChanged("Walk dog")
    viewModel.onDescriptionChanged("30 mins around block")
    viewModel.onAssigneeChanged("Alice")
    viewModel.onDateChanged("10/10/2025")
    viewModel.onTimeChanged("18:00")
    viewModel.saveTodo()

    // Wait until the todo appears in repository
    waitForTodosCount(repository, expectedCount = 1)

    // Reset VM for next add
    viewModel = AddTodoViewModel(repository)

    // Second ToDo
    viewModel.onTitleChanged("Do groceries")
    viewModel.onDescriptionChanged("Buy milk and eggs")
    viewModel.onAssigneeChanged("Bob")
    viewModel.onDateChanged("11/10/2025")
    viewModel.saveTodo()

    // Wait until both todos appear in repository
    waitForTodosCount(repository, expectedCount = 2)

    val todos = repository.getAllTodos()
    assertEquals(2, todos.size)

    val names = todos.map { it.name }
    assertTrue("Walk dog" in names)
    assertTrue("Do groceries" in names)
  }

  @Test
  fun dueTime_optional_isHandledCorrectly() = runTest {
    viewModel.onTitleChanged("No time field")
    viewModel.onDescriptionChanged("Task without time")
    viewModel.onAssigneeChanged("User")
    viewModel.onDateChanged("15/10/2025")
    viewModel.onTimeChanged("")

    viewModel.saveTodo()

    val todos = repository.getAllTodos()
    assertEquals(1, todos.size)
    val saved = todos.first()
    assertNull(saved.dueTime)
  }

  @Test
  fun invalidTimeFormat_triggersErrorAndPreventsSave1() = runTest {
    viewModel.onTitleChanged("Bad time")
    viewModel.onDescriptionChanged("Should not save")
    viewModel.onAssigneeChanged("User")
    viewModel.onDateChanged("10/10/2025")
    viewModel.onTimeChanged("25:99") // invalid time

    viewModel.saveTodo()

    val state = viewModel.uiState.value
    assertEquals("Invalid time (HH:mm)", state.dueTimeError)
    assertFalse("Expected saveSuccess=false but got true", state.saveSuccess)
    assertEquals(0, getTodosCount())
  }

  @Test
  fun invalidTimeFormat_triggersErrorAndPreventsSave2() = runTest {
    viewModel.onTitleChanged("Bad time")
    viewModel.onDescriptionChanged("Should not save")
    viewModel.onAssigneeChanged("User")
    viewModel.onDateChanged("10/10/2025")
    viewModel.onTimeChanged("25:04") // invalid time

    viewModel.saveTodo()

    val state = viewModel.uiState.value
    assertEquals("Invalid time (HH:mm)", state.dueTimeError)
    assertFalse("Expected saveSuccess=false but got true", state.saveSuccess)
    assertEquals(0, getTodosCount())
  }

  @Test
  fun invalidTimeFormat_triggersErrorAndPreventsSave3() = runTest {
    viewModel.onTitleChanged("Bad time")
    viewModel.onDescriptionChanged("Should not save")
    viewModel.onAssigneeChanged("User")
    viewModel.onDateChanged("10/10/2025")
    viewModel.onTimeChanged("14:001") // invalid time

    viewModel.saveTodo()

    val state = viewModel.uiState.value
    assertEquals("Invalid time (HH:mm)", state.dueTimeError)
    assertFalse("Expected saveSuccess=false but got true", state.saveSuccess)
    assertEquals(0, getTodosCount())
  }

  @Test
  fun repositoryError_isReflectedInUiState() = runTest {
    // Simulate a Firestore repository error on adding a todo.
    val failingRepo =
        object : ToDosRepository {
          override suspend fun addTodo(toDo: ToDo) {
            throw RuntimeException("Simulated Firestore failure")
          }

          override suspend fun getAllTodos() = emptyList<ToDo>()

          override suspend fun getTodo(todoID: String) = throw NoSuchElementException()

          override suspend fun deleteTodo(todoID: String) {}

          override suspend fun editTodo(todoID: String, newValue: ToDo) {}

          override suspend fun getAllEndedTodos() = emptyList<ToDo>()

          override fun getNewUid() = "fake-id"

          override suspend fun toggleStatus(todoID: String) {}
        }

    val viewModel = AddTodoViewModel(failingRepo)

    viewModel.onTitleChanged("Some task")
    viewModel.onDescriptionChanged("Should fail to save")
    viewModel.onAssigneeChanged("A")
    viewModel.onDateChanged("10/10/2025")

    viewModel.saveTodo()

    // Snippet of code to wait until saving is done:
    withContext(Dispatchers.Default.limitedParallelism(1)) {
      withTimeout(TIMEOUT) {
        while (viewModel.uiState.value.saveError == null && viewModel.uiState.value.isSaving) {
          delay(DELAY)
        }
      }
    }

    val state = viewModel.uiState.value

    assertNotNull("Expected a saveError but got none", state.saveError)
    assertFalse("Expected saveSuccess=false but got true", state.saveSuccess)
  }

  private suspend fun waitForTodosCount(repository: ToDosRepository, expectedCount: Int) {
    withContext(Dispatchers.Default.limitedParallelism(1)) {
      withTimeout(TIMEOUT) {
        while (repository.getAllTodos().size < expectedCount) {
          delay(DELAY)
        }
      }
    }
  }
}
