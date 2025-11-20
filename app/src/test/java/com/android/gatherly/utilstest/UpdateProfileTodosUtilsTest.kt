package com.android.gatherly.utilstest

import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todo.ToDosRepository
import com.android.gatherly.utils.addTodo_updateBadges
import com.android.gatherly.utils.editTodo_updateBadges
import com.google.firebase.Timestamp
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UpdateProfileTodosUtilsTest {

  private val todoRepository = mockk<ToDosRepository>()
  private val profileRepository = mockk<ProfileRepository>()
  private val currentUserId = "user123"
  private val mockTodo =
      ToDo(
          uid = "todo456",
          ownerId = currentUserId,
          name = "Test Task",
          status = ToDoStatus.ONGOING,
          description = "Test Description",
          assigneeName = "Test User",
          dueDate = Timestamp.now(),
          dueTime = null,
          location = null)

  @Before
  fun setup() {
    coEvery { todoRepository.addTodo(any()) } returns Unit
    coEvery { todoRepository.deleteTodo(any()) } returns Unit
    coEvery { todoRepository.getTodo(any()) } returns mockTodo
    coEvery { todoRepository.editTodo(any(), any()) } returns Unit
    coEvery { profileRepository.incrementCreatedTodo(any()) } returns 1
    coEvery { profileRepository.incrementCompletedTodo(any()) } returns 1
  }

  /** Test adding a ToDo calls addTodo and increments "created todos" exactly once. */
  @Test
  fun testAddTodoUpdateBadges_callsIncrementCreatedTodo() = runTest {
    addTodo_updateBadges(todoRepository, profileRepository, mockTodo, currentUserId)

    coVerify(exactly = 1) {
      todoRepository.addTodo(mockTodo)
      profileRepository.incrementCreatedTodo(currentUserId)
    }

    // make sure we do NOT touch completed counter here
    coVerify(exactly = 0) { profileRepository.incrementCompletedTodo(any()) }
  }

  /** When status goes from ONGOING -> ENDED, we increment "completed todos" once. */
  @Test
  fun testEditTodoUpdateBadges_ongoingToEnded_incrementsCompletedTodo() = runTest {
    val todoId = "task123"
    val existing = mockTodo.copy(uid = todoId, status = ToDoStatus.ONGOING)
    val newStatus = ToDoStatus.ENDED
    val expectedUpdated = existing.copy(status = newStatus)

    coEvery { todoRepository.getTodo(todoId) } returns existing

    editTodo_updateBadges(
      todoRepository = todoRepository,
      profileRepository = profileRepository,
      todoID = todoId,
      newStatus = newStatus,
      currentUserId = currentUserId
    )

    coVerify {
      todoRepository.getTodo(todoId)
      todoRepository.editTodo(todoId, expectedUpdated)
      profileRepository.incrementCompletedTodo(currentUserId)
    }
  }

  /** When status goes from ENDED -> ONGOING, we do NOT increment "completed todos". */
  @Test
  fun testEditTodoUpdateBadges_endedToOngoing_doesNotIncrementCompletedTodo() = runTest {
    val todoId = "task789"
    val existing = mockTodo.copy(uid = todoId, status = ToDoStatus.ENDED)
    val newStatus = ToDoStatus.ONGOING
    val expectedUpdated = existing.copy(status = newStatus)

    coEvery { todoRepository.getTodo(todoId) } returns existing

    editTodo_updateBadges(
      todoRepository = todoRepository,
      profileRepository = profileRepository,
      todoID = todoId,
      newStatus = newStatus,
      currentUserId = currentUserId
    )

    coVerify {
      todoRepository.getTodo(todoId)
      todoRepository.editTodo(todoId, expectedUpdated)
    }

    // No new completion should be counted in this direction
    coVerify(exactly = 0) { profileRepository.incrementCompletedTodo(any()) }
  }
}
