package com.android.gatherly.utilstest

import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todo.ToDosRepository
import com.android.gatherly.utils.addTodo_updateBadges
import com.android.gatherly.utils.deleteTodo_updateBadges
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
  private val mockProfile = Profile(uid = currentUserId, name = "Test User")
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
    coEvery { profileRepository.getProfileByUid(currentUserId) } returns mockProfile
    coEvery { todoRepository.addTodo(any()) } returns Unit
    coEvery { todoRepository.deleteTodo(any()) } returns Unit
    coEvery { todoRepository.getTodo(any()) } returns mockTodo
    coEvery { todoRepository.editTodo(any(), any()) } returns Unit
    coEvery { profileRepository.updateBadges(any()) } returns Unit
  }

  /** Test adding a ToDo and ensuring badges are updated accordingly. */
  @Test
  fun testAddTodoUpdateBadgesSuccess() = runTest {
    addTodo_updateBadges(todoRepository, profileRepository, mockTodo, currentUserId)
    coVerify {
      todoRepository.addTodo(mockTodo)
      profileRepository.updateBadges(mockProfile)
    }
  }

  /** Test adding a ToDo when profile retrieval fails, ensuring badges are not updated. */
  @Test
  fun testAddTodoUpdateBadgesFail() = runTest {
    coEvery { profileRepository.getProfileByUid(currentUserId) } returns null
    addTodo_updateBadges(todoRepository, profileRepository, mockTodo, currentUserId)
    coVerify(exactly = 1) { todoRepository.addTodo(mockTodo) }
    coVerify(exactly = 0) { profileRepository.updateBadges(any()) }
  }

  /** Test deleting a ToDo and ensuring badges are updated accordingly. */
  @Test
  fun testDeleteTodoUpdateBadgesSuccess() = runTest {
    val todoId = "someId"

    deleteTodo_updateBadges(todoRepository, profileRepository, todoId, currentUserId)
    coVerify {
      todoRepository.deleteTodo(todoID = todoId)
      profileRepository.updateBadges(mockProfile)
    }
  }

  /** Test updating a ToDo's status and ensuring badges are updated accordingly. */
  @Test
  fun testEditTodoUpdateBadgesSuccess() = runTest {
    val todoId = "task123"
    val newStatus = ToDoStatus.ENDED
    val initialTodo = mockTodo.copy(uid = todoId, status = ToDoStatus.ONGOING)
    val expectedUpdatedTodo = initialTodo.copy(status = newStatus)

    coEvery { todoRepository.getTodo(todoId) } returns initialTodo

    editTodo_updateBadges(todoRepository, profileRepository, todoId, newStatus, currentUserId)

    coVerify() {
      todoRepository.getTodo(todoId)
      todoRepository.editTodo(todoId, expectedUpdatedTodo)
      profileRepository.updateBadges(mockProfile)
    }
  }
}
