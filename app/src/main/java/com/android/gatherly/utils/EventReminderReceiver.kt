package com.android.gatherly.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.android.gatherly.model.notification.NotificationsRepository
import com.android.gatherly.model.notification.NotificationsRepositoryFirestore
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Receives the alarm fired by [EventAlarmScheduler] at event start time and creates an in-app
 * EVENT_REMINDER notification for the given user using [EventNotificationScheduler].
 */
class EventReminderReceiver : BroadcastReceiver() {
  companion object {
    /** Provides a NotificationsRepository (Firestore by default). */
    var notificationsRepositoryProvider: (Context) -> NotificationsRepository = {
      NotificationsRepositoryFirestore(Firebase.firestore)
    }
    /**
     * Allows tests to inject fake repositories and fake schedulers. This avoids Firestore and
     * AlarmManager usage in unit tests.
     */
    fun setTestProviders(provider: (Context) -> NotificationsRepository) {
      notificationsRepositoryProvider = provider
    }
    /** Restores default Firestore providers after tests. */
    fun resetProviders() {
      notificationsRepositoryProvider = { NotificationsRepositoryFirestore(Firebase.firestore) }
    }
  }

  /**
   * Called automatically by Android when AlarmManager triggers this receiver. It launches a
   * coroutine to generate notifications.
   */
  override fun onReceive(context: Context, intent: Intent) {
    // Retrieve the user ID and event ID that were saved inside the alarm's Intent:
    val userId = intent.getStringExtra(EventAlarmScheduler.EXTRA_USER_ID) ?: return
    val eventId = intent.getStringExtra(EventAlarmScheduler.EXTRA_EVENT_ID) ?: return
    val notificationsRepository = notificationsRepositoryProvider(context)
    // Scheduler responsible for generating event reminder notifications:
    val scheduler = EventNotificationScheduler(notificationsRepository = notificationsRepository)
    CoroutineScope(Dispatchers.IO).launch {
      scheduler.sendEventReminderNotification(userId, eventId)
    }
  }
}
