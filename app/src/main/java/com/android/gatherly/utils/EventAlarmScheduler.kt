package com.android.gatherly.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import com.google.firebase.Timestamp

/**
 * Schedules a one-shot exact alarm to create an in-app [NotificationType.EVENT_REMINDER]
 * notification at the start time of a given event.
 */
class EventAlarmScheduler(
    private val context: Context,
    /**
     * Provides the "current time" in milliseconds. Default = real system clock, but tests can
     * override it with a fake clock.
     */
    private val nowProvider: () -> Long = { System.currentTimeMillis() }
) {
  companion object {
    /**
     * Key used in the alarm Intent to pass the signed-in user's ID. When the alarm triggers,
     * Android launches our BroadcastReceiver and passes this extra inside the Intent so we know
     * *which user* we must generate reminders for.
     *
     * Example: Intent -> extras["extra_user_id"] == "uid_123"
     */
    const val EXTRA_USER_ID = "extra_event_user_id"
    /**
     * Key used in the alarm Intent to pass the signed-in user's ID. When the alarm triggers,
     * Android launches our BroadcastReceiver and passes this extra inside the Intent so we know
     * which event we must generate reminders for.
     *
     * Example: Intent -> extras["extra_event_id"] == "uid_123"
     */
    const val EXTRA_EVENT_ID = "extra_event_id"
  }

  /** Schedule an exact alarm at [eventDate] + [eventStartTime] for the given user/event. */
  @SuppressLint("ScheduleExactAlarm", "MissingPermission")
  fun scheduleEventReminder(
      userId: String,
      eventId: String,
      eventDate: Timestamp,
      eventStartTime: Timestamp
  ) {
    val triggerAtMillis = computeEventStartMillis(eventDate, eventStartTime)
    val now = nowProvider()
    // Do not schedule alarms for past events
    if (triggerAtMillis <= now) return
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    // Create the Intent describing what should happen when the alarm fires. A normal Intent is just
    // a description of an action.
    // It works like : when the alarm fires, deliver this Intent to EventReminderReceiver.
    val intent =
        Intent(context, EventReminderReceiver::class.java).apply {
          putExtra(EXTRA_USER_ID, userId)
          putExtra(EXTRA_EVENT_ID, eventId)
        }
    // Use a per-(user,event) request code so each alarm is distinct. This allows multiple events
    // per user:
    val requestCode = ("$userId:$eventId").hashCode()
    // We wrap into a pendingIntent to allow Android system to execute our Intent later.
    // FLAG_IMMUTABLE : Means the OS cannot modify the content of this PendingIntent
    // FLAG_UPDATE_CURRENT : If a PendingIntent with the same request code already exists, replace
    // it with this one.
    val pendingIntent =
        PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    // Cancel existing scheduled future alarms with this PendingIntent to prevent duplicates:
    alarmManager.cancel(pendingIntent)
    // Ask AlarmManager to trigger at the start of the event:
    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
  }

  /**
   * Compute the event start time in by combining the calendar date from [eventDate] and the
   * hour/minute from [eventStartTime]
   */
  fun computeEventStartMillis(eventDate: Timestamp, eventStartTime: Timestamp): Long {
    val dateCal = Calendar.getInstance().apply { time = eventDate.toDate() }
    val timeCal = Calendar.getInstance().apply { time = eventStartTime.toDate() }
    return Calendar.getInstance()
        .apply {
          this[Calendar.YEAR] = dateCal[Calendar.YEAR]
          this[Calendar.MONTH] = dateCal[Calendar.MONTH]
          this[Calendar.DAY_OF_MONTH] = dateCal[Calendar.DAY_OF_MONTH]
          this[Calendar.HOUR_OF_DAY] = timeCal[Calendar.HOUR_OF_DAY]
          this[Calendar.MINUTE] = timeCal[Calendar.MINUTE]
          this[Calendar.SECOND] = 0
          this[Calendar.MILLISECOND] = 0
        }
        .timeInMillis
  }

  /** Cancel a reminder that was already created for an event. */
  fun cancelEventReminder(eventId: String, userId: String) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val intent =
        Intent(context, EventReminderReceiver::class.java).apply {
          putExtra(EXTRA_EVENT_ID, eventId)
          putExtra(EXTRA_USER_ID, userId)
        }

    val pendingIntent =
        PendingIntent.getBroadcast(
            context,
            ("$userId:$eventId").hashCode(), // must match scheduleEventReminder
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

    alarmManager.cancel(pendingIntent)
  }
}
