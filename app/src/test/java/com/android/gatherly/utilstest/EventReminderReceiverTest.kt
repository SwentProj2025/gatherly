package com.android.gatherly.utilstest

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.gatherly.model.notification.Notification
import com.android.gatherly.model.notification.NotificationType
import com.android.gatherly.model.notification.NotificationsLocalRepository
import com.android.gatherly.model.notification.NotificationsRepository
import com.android.gatherly.utils.EventAlarmScheduler
import com.android.gatherly.utils.EventReminderReceiver
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventReminderReceiverTest {

    private lateinit var context: Context
    private lateinit var repo: NotificationsRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        repo = NotificationsLocalRepository()
        EventReminderReceiver.setTestProviders { repo }
    }

    @After
    fun tearDown() {
        EventReminderReceiver.resetProviders()
    }

    @Test
    fun receiver_ignores_missing_userId() = runBlocking {
        val intent = Intent(context, EventReminderReceiver::class.java).apply {
            putExtra(EventAlarmScheduler.EXTRA_EVENT_ID, "event1")
        }

        EventReminderReceiver().onReceive(context, intent)
        delay(10)

        assertTrue(repo.getUserNotifications("anything").isEmpty())
    }

    @Test
    fun receiver_ignores_missing_eventId() = runBlocking {
        val intent = Intent(context, EventReminderReceiver::class.java).apply {
            putExtra(EventAlarmScheduler.EXTRA_USER_ID, "alice")
        }

        EventReminderReceiver().onReceive(context, intent)
        delay(10)

        assertTrue(repo.getUserNotifications("alice").isEmpty())
    }

    @Test
    fun receiver_creates_notification() = runBlocking {
        val intent = Intent(context, EventReminderReceiver::class.java).apply {
            putExtra(EventAlarmScheduler.EXTRA_USER_ID, "alice")
            putExtra(EventAlarmScheduler.EXTRA_EVENT_ID, "event123")
        }

        EventReminderReceiver().onReceive(context, intent)
        delay(10)

        val notifs = repo.getUserNotifications("alice")
        assertEquals(1, notifs.size)

        val notif = notifs.first()
        assertEquals(NotificationType.EVENT_REMINDER, notif.type)
        assertEquals("event123", notif.relatedEntityId)
    }

    @Test
    fun receiver_does_not_duplicate_notifications() = runBlocking {
        val intent = Intent(context, EventReminderReceiver::class.java).apply {
            putExtra(EventAlarmScheduler.EXTRA_USER_ID, "bob")
            putExtra(EventAlarmScheduler.EXTRA_EVENT_ID, "ev42")
        }

        EventReminderReceiver().onReceive(context, intent)
        EventReminderReceiver().onReceive(context, intent)
        delay(20)

        val notifs = repo.getUserNotifications("bob")
        assertEquals(1, notifs.size)
    }
}
