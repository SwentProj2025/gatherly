package com.android.gatherly.utilstest

import com.android.gatherly.model.notification.Notification
import com.android.gatherly.model.notification.NotificationType
import com.android.gatherly.model.notification.NotificationsLocalRepository
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todo.ToDosLocalRepository
import com.android.gatherly.utils.NotificationScheduler
import com.google.firebase.Timestamp
import java.time.ZoneId
import java.util.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationSchedulerTest {

  private val userId = "user42"
  private val today = Date()

  @Test
  fun generatesNotification_whenTodoDeadlineIsToday() = runTest {
    // Arrange
    val todoRepo = ToDosLocalRepository()
    val notifRepo = NotificationsLocalRepository()
    val scheduler = NotificationScheduler(todoRepo, notifRepo)

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

  @Test
  fun doesNotDuplicateNotifications() = runTest {
    // Arrange
    val todoRepo = ToDosLocalRepository()
    val notifRepo = NotificationsLocalRepository()
    val scheduler = NotificationScheduler(todoRepo, notifRepo)

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

  @Test
  fun doesNotNotifyForTodosOwnedByAnotherUser() = runTest {
    val todoRepo = ToDosLocalRepository(limitToUser = "user1")
    val notifRepo = NotificationsLocalRepository()
    val scheduler = NotificationScheduler(todoRepo, notifRepo)

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

  @Test
  fun doesNotNotifyWhenDeadlineIsNotToday() = runTest {
    val todoRepo = ToDosLocalRepository()
    val notifRepo = NotificationsLocalRepository()
    val scheduler = NotificationScheduler(todoRepo, notifRepo)

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

  @Test
  fun ignoresTodosWithNullDeadline() = runTest {
    val todoRepo = ToDosLocalRepository()
    val notifRepo = NotificationsLocalRepository()
    val scheduler = NotificationScheduler(todoRepo, notifRepo)

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

  @Test
  fun generatesMultipleNotificationsForMultipleTodos() = runTest {
    val todoRepo = ToDosLocalRepository()
    val notifRepo = NotificationsLocalRepository()
    val scheduler = NotificationScheduler(todoRepo, notifRepo)

    todoRepo.addTodo(ToDo("t1", "A", "d", Timestamp(today), null, null, ToDoStatus.ONGOING, userId))
    todoRepo.addTodo(ToDo("t2", "B", "d", Timestamp(today), null, null, ToDoStatus.ONGOING, userId))

    scheduler.generateDailyTodoNotifications(userId)

    assertEquals(2, notifRepo.getUserNotifications(userId).size)
  }

  @Test
  fun onlyCreatesMissingNotifications_whenSomeAlreadyExist() = runTest {
    val todoRepo = ToDosLocalRepository()
    val notifRepo = NotificationsLocalRepository()
    val scheduler = NotificationScheduler(todoRepo, notifRepo)

    todoRepo.addTodo(ToDo("t1", "A", "d", Timestamp(today), null, null, ToDoStatus.ONGOING, userId))
    todoRepo.addTodo(ToDo("t2", "B", "d", Timestamp(today), null, null, ToDoStatus.ONGOING, userId))

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

  @Test
  fun timestamp_toLocalDate_convertsCorrectly() {
    val timestamp = Timestamp(today)
    val scheduler = NotificationScheduler(ToDosLocalRepository(), NotificationsLocalRepository())

    val converted = scheduler.run { timestamp.toLocalDate() }
    val expected = today.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

    assertEquals(expected, converted)
  }
}
