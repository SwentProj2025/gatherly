package com.android.gatherly.viewmodel.todo

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todo.ToDosLocalRepository
import com.android.gatherly.model.todo.ToDosRepository
import com.android.gatherly.ui.todo.AddTodoViewModel
import com.android.gatherly.utilstest.MockitoUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private const val TIMEOUT = 100_000L
private const val DELAY = 500L

/**
 * Integration tests for [AddTodoViewModel]
 *
 * These tests verify that valid inputs lead to successful writes, and invalid inputs are rejected
 * without repository side effects.
 */
@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class AddTodoViewModelTest {

  private lateinit var addToDoViewModel: AddTodoViewModel
  private lateinit var toDosRepository: ToDosRepository
  private lateinit var profileRepository: ProfileRepository
  private lateinit var mockitoUtils: MockitoUtils

  // initialize this so that tests control all coroutines and can wait on them
  private val testDispatcher = StandardTestDispatcher()

  private var ownerProfile: Profile =
      Profile(
          uid = "0",
          name = "Owner",
          focusSessionIds = emptyList(),
          groupIds = emptyList(),
          friendUids = emptyList())

  @Before
  fun setUp() {
    // so that tests can wait on coroutines
    Dispatchers.setMain(testDispatcher)

    toDosRepository = ToDosLocalRepository()
    profileRepository = ProfileLocalRepository()

    // Add owner profile to repo
    runTest { profileRepository.addProfile(ownerProfile) }

    // Mock Firebase Auth
    mockitoUtils = MockitoUtils()
    mockitoUtils.chooseCurrentUser("0")

    addToDoViewModel =
        AddTodoViewModel(
            todoRepository = toDosRepository,
            profileRepository = profileRepository,
            authProvider = { mockitoUtils.mockAuth })
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun saveTodo_withValidFields_savesToFirestore() = runTest {
    addToDoViewModel.onTitleChanged("Study session")
    addToDoViewModel.onDescriptionChanged("Revise for algorithms exam")
    addToDoViewModel.onDateChanged("10/10/2025")
    addToDoViewModel.onTimeChanged("13:30")

    addToDoViewModel.saveTodo()

    advanceUntilIdle()

    // Check UI state
    val state = addToDoViewModel.uiState.value
    assertTrue(state.saveSuccess)
    assertNull(state.saveError)

    // Check repository contents
    val todos = toDosRepository.getAllTodos()
    assertEquals(1, todos.size)
    val saved = todos.first()

    assertEquals("Study session", saved.name)
    assertEquals("Revise for algorithms exam", saved.description)
    assertEquals(ToDoStatus.ONGOING, saved.status)
    assertNotNull(saved.dueDate)
  }

  @Test
  fun saveTodo_withInvalidDate_doesNotSave() = runTest {
    addToDoViewModel.onTitleChanged("Invalid date task")
    addToDoViewModel.onDescriptionChanged("Desc")
    addToDoViewModel.onDateChanged("10-10-2025") // Wrong format

    addToDoViewModel.saveTodo()
    delay(DELAY)

    val state = addToDoViewModel.uiState.value
    assertNotNull(state.dueDateError)
    assertFalse(state.saveSuccess)

    // Repo should still be empty
    val count = toDosRepository.getAllTodos().size
    assertEquals(0, count)
  }

  @Test
  fun saveTodo_withMissingFields_doesNotSave() = runTest {
    addToDoViewModel.onTitleChanged("") // Missing title
    addToDoViewModel.onDescriptionChanged("Some description")
    addToDoViewModel.onDateChanged("10/10/2025")

    addToDoViewModel.checkTodoTime()
    delay(DELAY)

    val state = addToDoViewModel.uiState.value
    assertNotNull(state.titleError)
    assertFalse(state.saveSuccess)

    val count = toDosRepository.getAllTodos().size
    assertEquals(0, count)
  }

  @Test
  fun multipleValidTodos_areSavedIndependently() = runTest {
    // First ToDo
    addToDoViewModel.onTitleChanged("Walk dog")
    addToDoViewModel.onDescriptionChanged("30 mins around block")
    addToDoViewModel.onDateChanged("10/10/2025")
    addToDoViewModel.onTimeChanged("18:00")
    addToDoViewModel.saveTodo()

    // Wait until the todo appears in repository
    waitForTodosCount(toDosRepository, expectedCount = 1)

    // Reset VM for next add
    addToDoViewModel =
        AddTodoViewModel(
            toDosRepository, profileRepository, authProvider = { mockitoUtils.mockAuth })

    // Second ToDo
    addToDoViewModel.onTitleChanged("Do groceries")
    addToDoViewModel.onDescriptionChanged("Buy milk and eggs")
    addToDoViewModel.onDateChanged("11/10/2025")
    addToDoViewModel.saveTodo()

    // Wait until both todos appear in repository
    waitForTodosCount(toDosRepository, expectedCount = 2)

    val todos = toDosRepository.getAllTodos()
    assertEquals(2, todos.size)

    val names = todos.map { it.name }
    assertTrue("Walk dog" in names)
    assertTrue("Do groceries" in names)
  }

  @Test
  fun dueTime_optional_isHandledCorrectly() = runTest {
    addToDoViewModel.onTitleChanged("No time field")
    addToDoViewModel.onDescriptionChanged("Task without time")
    addToDoViewModel.onDateChanged("15/10/2025")
    addToDoViewModel.onTimeChanged("")

    addToDoViewModel.saveTodo()

    val todos = toDosRepository.getAllTodos()
    advanceUntilIdle()
    assertEquals(1, todos.size)
    val saved = todos.first()
    assertNull(saved.dueTime)
  }

  @Test
  fun invalidTimeFormat_triggersErrorAndPreventsSave1() = runTest {
    addToDoViewModel.onTitleChanged("Bad time")
    addToDoViewModel.onDescriptionChanged("Should not save")
    addToDoViewModel.onDateChanged("10/10/2025")
    addToDoViewModel.onTimeChanged("25:99") // invalid time

    addToDoViewModel.saveTodo()

    val state = addToDoViewModel.uiState.value
    assertEquals("Invalid time (HH:mm)", state.dueTimeError)
    assertFalse("Expected saveSuccess=false but got true", state.saveSuccess)
    val count = toDosRepository.getAllTodos().size
    assertEquals(0, count)
  }

  @Test
  fun invalidTimeFormat_triggersErrorAndPreventsSave2() = runTest {
    addToDoViewModel.onTitleChanged("Bad time")
    addToDoViewModel.onDescriptionChanged("Should not save")
    addToDoViewModel.onDateChanged("10/10/2025")
    addToDoViewModel.onTimeChanged("25:04") // invalid time

    addToDoViewModel.saveTodo()

    val state = addToDoViewModel.uiState.value
    assertEquals("Invalid time (HH:mm)", state.dueTimeError)
    assertFalse("Expected saveSuccess=false but got true", state.saveSuccess)
    val count = toDosRepository.getAllTodos().size
    assertEquals(0, count)
  }

  @Test
  fun invalidTimeFormat_triggersErrorAndPreventsSave3() = runTest {
    addToDoViewModel.onTitleChanged("Bad time")
    addToDoViewModel.onDescriptionChanged("Should not save")
    addToDoViewModel.onDateChanged("10/10/2025")
    addToDoViewModel.onTimeChanged("14:001") // invalid time

    addToDoViewModel.saveTodo()

    val state = addToDoViewModel.uiState.value
    assertEquals("Invalid time (HH:mm)", state.dueTimeError)
    assertFalse("Expected saveSuccess=false but got true", state.saveSuccess)
    val count = toDosRepository.getAllTodos().size
    assertEquals(0, count)
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

    val viewModel = AddTodoViewModel(failingRepo, profileRepository, { mockitoUtils.mockAuth })

    viewModel.onTitleChanged("Some task")
    viewModel.onDescriptionChanged("Should fail to save")
    viewModel.onDateChanged("10/10/2025")

    viewModel.saveTodo()

    // Snippet of code to wait until saving is done:
    advanceUntilIdle()

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
