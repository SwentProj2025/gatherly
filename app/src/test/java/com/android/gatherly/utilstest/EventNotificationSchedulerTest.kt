package com.android.gatherly.utilstest

import com.android.gatherly.model.notification.NotificationType
import com.android.gatherly.model.notification.NotificationsLocalRepository
import com.android.gatherly.model.notification.NotificationsRepository
import com.android.gatherly.utils.EventNotificationScheduler
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/** Unit tests for EventNotificationScheduler. */
class EventNotificationSchedulerTest {

  private lateinit var notifRepo: NotificationsRepository
  private lateinit var scheduler: EventNotificationScheduler

  @Before
  fun setup() {
    notifRepo = NotificationsLocalRepository()
    scheduler = EventNotificationScheduler(notifRepo)
  }

  @Test
  fun `creates a new event reminder notification when none exists`() = runBlocking {
    val userId = "user1"
    val eventId = "eventA"

    scheduler.sendEventReminderNotification(userId, eventId)

    val notifications = notifRepo.getUserNotifications(userId)
    assertEquals(1, notifications.size)

    val notif = notifications.first()
    assertEquals(NotificationType.EVENT_REMINDER, notif.type)
    assertEquals(eventId, notif.relatedEntityId)
    assertEquals(userId, notif.recipientId)
    assertFalse(notif.wasRead)
    assertNotNull(notif.emissionTime)
  }

  @Test
  fun `does NOT create duplicate event reminders for the same user and event`() = runBlocking {
    val userId = "user1"
    val eventId = "eventA"

    // First reminder
    scheduler.sendEventReminderNotification(userId, eventId)
    // Attempt duplicate
    scheduler.sendEventReminderNotification(userId, eventId)

    val notifications = notifRepo.getUserNotifications(userId)
    assertEquals("Second call must not create a duplicate notification", 1, notifications.size)
  }

  @Test
  fun `notifications for different users do not interfere`() = runBlocking {
    val eventId = "eventA"

    scheduler.sendEventReminderNotification("u1", eventId)
    scheduler.sendEventReminderNotification("u2", eventId)

    val user1Notifs = notifRepo.getUserNotifications("u1")
    val user2Notifs = notifRepo.getUserNotifications("u2")

    assertEquals(1, user1Notifs.size)
    assertEquals(1, user2Notifs.size)

    assertEquals("u1", user1Notifs.first().recipientId)
    assertEquals("u2", user2Notifs.first().recipientId)
  }

  @Test
  fun `notifications for different events do not interfere`() = runBlocking {
    val userId = "user1"

    scheduler.sendEventReminderNotification(userId, "eventA")
    scheduler.sendEventReminderNotification(userId, "eventB")

    val notifications = notifRepo.getUserNotifications(userId)
    assertEquals(2, notifications.size)

    val eventIds = notifications.map { it.relatedEntityId }.toSet()
    assertTrue(eventIds.contains("eventA"))
    assertTrue(eventIds.contains("eventB"))
  }

  @Test
  fun `repo initially empty still creates notification`() = runBlocking {
    assertTrue(notifRepo.getUserNotifications("userX").isEmpty())

    scheduler.sendEventReminderNotification("userX", "event99")

    val notifications = notifRepo.getUserNotifications("userX")
    assertEquals(1, notifications.size)
  }
}
