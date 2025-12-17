package com.android.gatherly.utilstest

import com.android.gatherly.model.notification.Notification
import com.android.gatherly.model.notification.NotificationType
import com.android.gatherly.model.notification.NotificationsLocalRepository
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todo.ToDosLocalRepository
import com.android.gatherly.runUnconfinedTest
import com.android.gatherly.utils.ToDoNotificationScheduler
import com.google.firebase.Timestamp
import java.time.ZoneId
import java.util.*
import kotlin.time.Duration.Companion.seconds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for [ToDoNotificationScheduler]. */
class ToDoNotificationSchedulerTest {

  private val userId = "user42"
  private val today = Date()

  private val testTimeout = 120.seconds

  /** Tests that a notification is generated for a to-do with a deadline set for today. */
  @Test
  fun generatesNotification_whenTodoDeadlineIsToday() =
      runUnconfinedTest(testTimeout) {
        // Arrange
        val todoRepo = ToDosLocalRepository()
        val notifRepo = NotificationsLocalRepository()
        val scheduler = ToDoNotificationScheduler(todoRepo, notifRepo)

        todoRepo.addTodo(
            ToDo(
                uid = "todo1",
                name = "Test Todo",
                description = "desc",
                dueDate = Timestamp(today),
                dueTime = null,
                location = null,
                status = ToDoStatus.ONGOING,
                ownerId = userId))

        // Act
        scheduler.generateDailyTodoNotifications(userId)

        // Assert
        val userNotifications = notifRepo.getUserNotifications(userId)

        assertEquals(1, userNotifications.size)
        val notif = userNotifications.first()

        assertEquals(NotificationType.TODO_REMINDER, notif.type)
        assertEquals("todo1", notif.relatedEntityId)
        assertEquals(userId, notif.recipientId)
      }

  /** Tests that duplicate notifications are not created for the same to-do. */
  @Test
  fun doesNotDuplicateNotifications() =
      runUnconfinedTest(testTimeout) {
        // Arrange
        val todoRepo = ToDosLocalRepository()
        val notifRepo = NotificationsLocalRepository()
        val scheduler = ToDoNotificationScheduler(todoRepo, notifRepo)

        todoRepo.addTodo(
            ToDo(
                uid = "todo1",
                name = "Test Todo",
                description = "desc",
                dueDate = Timestamp(today),
                dueTime = null,
                location = null,
                status = ToDoStatus.ONGOING,
                ownerId = userId))

        // Add an existing reminder
        notifRepo.addNotification(
            Notification(
                id = "n1",
                type = NotificationType.TODO_REMINDER,
                emissionTime = Timestamp.now(),
                senderId = null,
                relatedEntityId = "todo1",
                recipientId = userId,
                wasRead = false))

        // Act
        scheduler.generateDailyTodoNotifications(userId)

        // Assert → still only one notification
        val userNotifications = notifRepo.getUserNotifications(userId)

        assertEquals(1, userNotifications.size)
      }

  /** Tests that no notification is generated for to-dos owned by another user. */
  @Test
  fun doesNotNotifyForTodosOwnedByAnotherUser() =
      runUnconfinedTest(testTimeout) {
        val todoRepo = ToDosLocalRepository(limitToUser = "user1")
        val notifRepo = NotificationsLocalRepository()
        val scheduler = ToDoNotificationScheduler(todoRepo, notifRepo)

        todoRepo.addTodo(
            ToDo(
                uid = "todoX",
                name = "Other user todo",
                description = "desc",
                dueDate = Timestamp(today),
                dueTime = null,
                location = null,
                status = ToDoStatus.ONGOING,
                ownerId = "anotherUser"))

        scheduler.generateDailyTodoNotifications(userId)

        assertTrue(notifRepo.getUserNotifications(userId).isEmpty())
      }

  /** Tests that no notification is generated for to-dos with deadlines not set for today. */
  @Test
  fun doesNotNotifyWhenDeadlineIsNotToday() =
      runUnconfinedTest(testTimeout) {
        val todoRepo = ToDosLocalRepository()
        val notifRepo = NotificationsLocalRepository()
        val scheduler = ToDoNotificationScheduler(todoRepo, notifRepo)

        val tomorrow = Date(today.time + 24 * 60 * 60 * 1000)

        todoRepo.addTodo(
            ToDo(
                uid = "todo1",
                name = "Test",
                description = "desc",
                dueDate = Timestamp(tomorrow),
                dueTime = null,
                location = null,
                status = ToDoStatus.ONGOING,
                ownerId = userId))

        scheduler.generateDailyTodoNotifications(userId)

        assertTrue(notifRepo.getUserNotifications(userId).isEmpty())
      }

  /** Tests that no notification is generated for to-dos without a deadline. */
  @Test
  fun ignoresTodosWithNullDeadline() =
      runUnconfinedTest(testTimeout) {
        val todoRepo = ToDosLocalRepository()
        val notifRepo = NotificationsLocalRepository()
        val scheduler = ToDoNotificationScheduler(todoRepo, notifRepo)

        todoRepo.addTodo(
            ToDo(
                uid = "todo1",
                name = "Null date todo",
                description = "desc",
                dueDate = null,
                dueTime = null,
                location = null,
                status = ToDoStatus.ONGOING,
                ownerId = userId))

        scheduler.generateDailyTodoNotifications(userId)

        assertTrue(notifRepo.getUserNotifications(userId).isEmpty())
      }

  /**
   * Tests that multiple notifications are generated for multiple to-dos with deadlines set for
   * today.
   */
  @Test
  fun generatesMultipleNotificationsForMultipleTodos() =
      runUnconfinedTest(testTimeout) {
        val todoRepo = ToDosLocalRepository()
        val notifRepo = NotificationsLocalRepository()
        val scheduler = ToDoNotificationScheduler(todoRepo, notifRepo)

        todoRepo.addTodo(
            ToDo("t1", "A", "d", Timestamp(today), null, null, ToDoStatus.ONGOING, userId))
        todoRepo.addTodo(
            ToDo("t2", "B", "d", Timestamp(today), null, null, ToDoStatus.ONGOING, userId))

        scheduler.generateDailyTodoNotifications(userId)

        assertEquals(2, notifRepo.getUserNotifications(userId).size)
      }

  /** Tests that only missing notifications are created when some already exist. */
  @Test
  fun onlyCreatesMissingNotifications_whenSomeAlreadyExist() =
      runUnconfinedTest(testTimeout) {
        val todoRepo = ToDosLocalRepository()
        val notifRepo = NotificationsLocalRepository()
        val scheduler = ToDoNotificationScheduler(todoRepo, notifRepo)

        todoRepo.addTodo(
            ToDo("t1", "A", "d", Timestamp(today), null, null, ToDoStatus.ONGOING, userId))
        todoRepo.addTodo(
            ToDo("t2", "B", "d", Timestamp(today), null, null, ToDoStatus.ONGOING, userId))

        // Existing notification for t1
        notifRepo.addNotification(
            Notification(
                id = "n1",
                type = NotificationType.TODO_REMINDER,
                emissionTime = Timestamp.now(),
                senderId = null,
                relatedEntityId = "t1",
                recipientId = userId,
                wasRead = false))

        scheduler.generateDailyTodoNotifications(userId)

        val list = notifRepo.getUserNotifications(userId)

        assertEquals(2, list.size) // total
        assertTrue(list.any { it.relatedEntityId == "t2" }) // new one
      }

  /** Tests the conversion from [Timestamp] to [java.time.LocalDate]. */
  @Test
  fun timestamp_toLocalDate_convertsCorrectly() {
    val timestamp = Timestamp(today)
    val scheduler =
        ToDoNotificationScheduler(ToDosLocalRepository(), NotificationsLocalRepository())

    val converted = scheduler.run { timestamp.toLocalDate() }
    val expected = today.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

    assertEquals(expected, converted)
  }
}
