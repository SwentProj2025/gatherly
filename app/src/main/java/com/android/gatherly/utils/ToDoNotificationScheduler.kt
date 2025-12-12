package com.android.gatherly.utils

import com.android.gatherly.model.notification.Notification
import com.android.gatherly.model.notification.NotificationType
import com.android.gatherly.model.notification.NotificationsRepository
import com.android.gatherly.model.todo.ToDosRepository
import com.google.firebase.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Helper class responsible for generating in-app [NotificationType.TODO_REMINDER] notifications.
 *
 * The idea:
 * - Once per day, we call [generateDailyTodoNotifications] for the current user.
 * - It looks at all the user's todos and, for those whose deadline is today, it creates a
 *   [NotificationType.TODO_REMINDER] if one does not already exist.
 */
class ToDoNotificationScheduler(
    private val todoRepository: ToDosRepository,
    private val notificationsRepository: NotificationsRepository
) {

  /**
   * Generates missing [NotificationType.TODO_REMINDER] notifications for a given [userId] for the
   * specified [today].
   *
   * @param userId the ID of the user for whom we generate notifications.
   * @param today the logical "current date" (defaults to system date, overridable for tests).
   */
  suspend fun generateDailyTodoNotifications(userId: String, today: LocalDate = LocalDate.now()) {
    val todos = todoRepository.getAllTodos()
    val notifications = notificationsRepository.getUserNotifications(userId)

    // Examine each of the user's todo and decide to send or not a reminder:
    for (todo in todos) {
      val deadline = todo.dueDate?.toLocalDate()
      if (deadline != null) {
        val exists =
            notifications.any {
              it.type == NotificationType.TODO_REMINDER && it.relatedEntityId == todo.uid
            }
        if (deadline == today && !exists) {
          val notification =
              Notification(
                  id = notificationsRepository.getNewId(),
                  type = NotificationType.TODO_REMINDER,
                  emissionTime = Timestamp.now(),
                  senderId = null,
                  relatedEntityId = todo.uid,
                  recipientId = userId,
                  wasRead = false)
          notificationsRepository.addNotification(notification)
        }
      }
    }
  }

  /** Converts a Firestore Timestamp to a LocalDate using the device's default timezone. */
  fun Timestamp.toLocalDate(): LocalDate {
    return Instant.ofEpochSecond(seconds, nanoseconds.toLong())
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
  }
}
