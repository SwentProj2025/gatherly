package com.android.gatherly.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.android.gatherly.model.notification.NotificationsRepository
import com.android.gatherly.model.notification.NotificationsRepositoryFirestore
import com.android.gatherly.model.todo.ToDosRepository
import com.android.gatherly.model.todo.ToDosRepositoryFirestore
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * A BroadcastReceiver triggered once per day (at midnight) by AlarmManager. This receiver runs ONLY
 * when the alarm fires.
 */
class DailyTodoReminderReceiver : BroadcastReceiver() {

  companion object {

    /** Provides a ToDosRepository (Firestore by default). */
    var todoRepoProvider: (Context) -> ToDosRepository = {
      ToDosRepositoryFirestore(Firebase.firestore)
    }
    /** Provides a NotificationsRepository (Firestore by default). */
    var notificationsRepoProvider: (Context) -> NotificationsRepository = {
      NotificationsRepositoryFirestore(Firebase.firestore)
    }
    /** Provides a scheduler that can reschedule the alarm. */
    var alarmSchedulerProvider: (Context) -> DailyTodoAlarmScheduler = {
      DailyTodoAlarmScheduler(it)
    }

    /**
     * Allows tests to inject fake repositories and fake schedulers. This avoids Firestore and
     * AlarmManager usage in unit tests.
     */
    fun setTestProviders(
        todoProvider: (Context) -> ToDosRepository,
        notifProvider: (Context) -> NotificationsRepository,
        alarmProvider: (Context) -> DailyTodoAlarmScheduler
    ) {
      todoRepoProvider = todoProvider
      notificationsRepoProvider = notifProvider
      alarmSchedulerProvider = alarmProvider
    }

    /** Restores default Firestore providers after tests. */
    fun resetProviders() {
      todoRepoProvider = { ToDosRepositoryFirestore(Firebase.firestore) }
      notificationsRepoProvider = { NotificationsRepositoryFirestore(Firebase.firestore) }
      alarmSchedulerProvider = { DailyTodoAlarmScheduler(it) }
    }
  }

  /**
   * Called automatically by Android when AlarmManager triggers this receiver. It launches a
   * coroutine to generate notifications and reschedule the alarm.
   */
  override fun onReceive(context: Context, intent: Intent) {
    // Retrieve the user ID that was saved inside the alarm's Intent:
    val userId = intent.getStringExtra(DailyTodoAlarmScheduler.EXTRA_USER_ID) ?: return
    val todoRepository = todoRepoProvider(context)
    val notificationsRepository = notificationsRepoProvider(context)
    // Scheduler responsible for generating todo reminder notifications.
    val scheduler = ToDoNotificationScheduler(todoRepository, notificationsRepository)
    CoroutineScope(Dispatchers.IO).launch {
      scheduler.generateDailyTodoNotifications(userId)
      alarmSchedulerProvider(context).scheduleNextTodoCheck(userId)
    }
  }
}
