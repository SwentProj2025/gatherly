package com.android.gatherly.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

/**
 * Schedules the daily background task that checks for todos with a deadline "today". It uses
 * AlarmManager since exact alarms are needed when a reminder must occur at a predictable moment.
 */
open class DailyTodoAlarmScheduler(
    private val context: Context,
    /**
     * Provides the "current time" in milliseconds. Default = real system clock, but tests can
     * override it with a fake clock.
     */
    private val nowProvider: () -> Long = { System.currentTimeMillis() }
) {

  companion object {
    /**
     * A unique integer used to identify the PendingIntent created for this alarm. AlarmManager does
     * NOT compare PendingIntents by their content. Instead, it considers two PendingIntents equal
     * if:
     * - they have the same Intent (same component + same extras)
     * - AND the same requestCode This matters for updating or cancelling alarms. If we schedule an
     *   alarm today and another one tomorrow, we want the second alarm to REPLACE the first one
     *   (not create duplicates). Using a fixed requestCode guarantees that both alarms refer to the
     *   same PendingIntent.
     */
    const val REQUEST_CODE = 1001
    /**
     * Key used in the alarm Intent to pass the signed-in user's ID. When the alarm triggers,
     * Android launches our BroadcastReceiver and passes this extra inside the Intent so we know
     * *which user* we must generate reminders for.
     *
     * Example: Intent -> extras["extra_user_id"] == "uid_123"
     */
    const val EXTRA_USER_ID = "extra_user_id"
  }

  /**
   * Schedules the next exact alarm that will fire the DailyTodoReminderReceiver. The alarm fires at
   * the next midnight. It uses setExactAndAllowWhileIdle, meaning the alarm will fire even if the
   * device is in Doze mode, and the timestamp will not be batched or delayed by the OS.
   * The @SuppressLint annotations are intentional because the app uses USE_EXACT_ALARM, which
   * allows exact alarms without needing user approval.
   */
  @SuppressLint("ScheduleExactAlarm", "MissingPermission")
  open fun scheduleNextTodoCheck(userId: String) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    // Create the Intent describing what should happen when the alarm fires. A normal Intent is just
    // a description of an action.
    // It works like : when the alarm fires, deliver this Intent to DailyTodoReminderReceiver.
    val intent =
        Intent(context, DailyTodoReminderReceiver::class.java).apply {
          putExtra(EXTRA_USER_ID, userId)
        }
    // We wrap into a pendingIntent to allow Android system to execute our Intent later.
    // FLAG_IMMUTABLE : Means the OS cannot modify the content of this PendingIntent
    // FLAG_UPDATE_CURRENT : If a PendingIntent with the same request code already exists, replace
    // it with this one.
    val pendingIntent =
        PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

    // Ask AlarmManager to trigger at the next midnight:
    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP, // Wake the device up if it is sleeping
        computeNextMidnight(),
        pendingIntent)
  }

  /** Computes the timestamp (in millis) of tomorrow at 00:00 based on [nowProvider]. */
  fun computeNextMidnight(): Long {
    val cal = Calendar.getInstance().apply { timeInMillis = nowProvider() }
    // Move to next day:
    cal.add(Calendar.DAY_OF_YEAR, 1)
    // Reset to midnight:
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
  }
}
