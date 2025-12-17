package com.android.gatherly.utilstest

import com.android.gatherly.model.badge.BadgeType
import com.android.gatherly.model.points.PointsLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todo.ToDosRepository
import com.android.gatherly.runUnconfinedTest
import com.android.gatherly.utils.addTodo
import com.android.gatherly.utils.editTodo
import com.google.firebase.Timestamp
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlin.time.Duration.Companion.seconds
import org.junit.Before
import org.junit.Test

/** Unit tests for updating profile badges when adding/editing ToDos. */
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
          dueDate = Timestamp.now(),
          dueTime = null,
          location = null)

  private val testTimeout = 120.seconds

  @Before
  fun setup() {
    coEvery { todoRepository.addTodo(any()) } just runs

    coEvery { todoRepository.deleteTodo(any()) } just runs
    coEvery { todoRepository.getTodo(any()) } returns mockTodo
    coEvery { todoRepository.editTodo(any(), any()) } just runs

    coEvery { profileRepository.incrementBadge(any(), any()) } returns null
  }

  /** Test adding a ToDo calls addTodo and increments "created todos" exactly once. */
  @Test
  fun testAddTodoUpdateBadges_callsIncrementCreatedTodo() =
      runUnconfinedTest(testTimeout) {
        val pointsRepository = PointsLocalRepository()

        addTodo(todoRepository, profileRepository, pointsRepository, mockTodo, currentUserId)

        coVerify(exactly = 1) {
          todoRepository.addTodo(mockTodo)
          profileRepository.incrementBadge(currentUserId, BadgeType.TODOS_CREATED)
        }
      }

  /** When status goes from ONGOING -> ENDED, we increment "completed todos" once. */
  @Test
  fun testEditTodoUpdateBadges_ongoingToEnded_incrementsCompletedTodo() =
      runUnconfinedTest(testTimeout) {
        val todoId = "task123"
        val existing = mockTodo.copy(uid = todoId, status = ToDoStatus.ONGOING)
        val newStatus = ToDoStatus.ENDED
        val expectedUpdated = existing.copy(status = newStatus)
        val pointsRepository = PointsLocalRepository()

        coEvery { todoRepository.getTodo(todoId) } returns existing

        editTodo(
            todoRepository = todoRepository,
            profileRepository = profileRepository,
            pointsRepository = pointsRepository,
            todoID = todoId,
            newStatus = newStatus,
            currentUserId = currentUserId)

        coVerify {
          todoRepository.getTodo(todoId)
          todoRepository.editTodo(todoId, expectedUpdated)
          profileRepository.incrementBadge(currentUserId, BadgeType.TODOS_COMPLETED)
        }
      }

  /** When status goes from ENDED -> ONGOING, we do NOT increment "completed todos". */
  @Test
  fun testEditTodoUpdateBadges_endedToOngoing_doesNotIncrementCompletedTodo() =
      runUnconfinedTest(testTimeout) {
        val todoId = "task789"
        val existing = mockTodo.copy(uid = todoId, status = ToDoStatus.ENDED)
        val newStatus = ToDoStatus.ONGOING
        val expectedUpdated = existing.copy(status = newStatus)
        val pointsRepository = PointsLocalRepository()

        coEvery { todoRepository.getTodo(todoId) } returns existing

        editTodo(
            todoRepository = todoRepository,
            profileRepository = profileRepository,
            pointsRepository = pointsRepository,
            todoID = todoId,
            newStatus = newStatus,
            currentUserId = currentUserId)

        coVerify {
          todoRepository.getTodo(todoId)
          todoRepository.editTodo(todoId, expectedUpdated)
        }
      }
}
