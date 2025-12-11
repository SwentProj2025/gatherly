package com.android.gatherly.utils

import com.android.gatherly.model.notification.Notification
import com.android.gatherly.model.notification.NotificationType
import com.android.gatherly.model.notification.NotificationsRepository
import com.google.firebase.Timestamp

/**
 * Handles creation of event reminder notifications.
 */
class EventNotificationScheduler(
    private val notificationsRepository: NotificationsRepository
) {

    /**
     * Generates a notification reminding the user that an event is starting now.
     *
     * @param userId the user who should receive the reminder
     * @param eventId the event ID
     */
    suspend fun sendEventReminderNotification(userId: String, eventId: String) {
        val existing = notificationsRepository.getUserNotifications(userId)
            .any { it.type == NotificationType.EVENT_REMINDER && it.relatedEntityId == eventId }

        if (existing) return

        val notification = Notification(
            id = notificationsRepository.getNewId(),
            type = NotificationType.EVENT_REMINDER,
            emissionTime = Timestamp.now(),
            senderId = null,
            relatedEntityId = eventId,
            recipientId = userId,
            wasRead = false
        )

        notificationsRepository.addNotification(notification)
    }
}
