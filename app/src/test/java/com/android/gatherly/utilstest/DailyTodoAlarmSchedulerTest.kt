package com.android.gatherly.utilstest

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.gatherly.utils.DailyTodoAlarmScheduler
import com.android.gatherly.utils.DailyTodoReminderReceiver
import java.util.Calendar
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class DailyTodoAlarmSchedulerTest {

  private val context = ApplicationProvider.getApplicationContext<Context>()

  @Test
  fun computeNextMidnight_returnsTomorrowMidnight() {
    val now = System.currentTimeMillis()
    val midnight = DailyTodoAlarmScheduler(context).computeNextMidnight()

    // should be > now and less than 24h ahead
    assertTrue(midnight > now)
    assertTrue(midnight - now <= 24 * 60 * 60 * 1000)
  }

  @Test
  fun scheduleNextTodoCheck_setsExactAlarmWithCorrectExtras() {
    val scheduler = DailyTodoAlarmScheduler(context)
    val userId = "user123"

    scheduler.scheduleNextTodoCheck(userId)

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val shadowAlarm = Shadows.shadowOf(alarmManager)
    val scheduled = shadowAlarm.nextScheduledAlarm

    assertNotNull(scheduled)
    assertEquals(AlarmManager.RTC_WAKEUP, scheduled!!.type)

    val pendingIntent = scheduled.operation as PendingIntent
    val intent = Shadows.shadowOf(pendingIntent).savedIntent

    assertEquals(DailyTodoAlarmScheduler.Companion.EXTRA_USER_ID, intent.extras?.keySet()?.first())
    assertEquals(userId, intent.getStringExtra(DailyTodoAlarmScheduler.Companion.EXTRA_USER_ID))
  }

  @Test
  fun scheduleNextTodoCheck_usesCorrectPendingIntentFlags() {
    val scheduler = DailyTodoAlarmScheduler(context)
    val userId = "abc123"

    scheduler.scheduleNextTodoCheck(userId)

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val shadowAlarm = Shadows.shadowOf(alarmManager)
    val scheduled = shadowAlarm.scheduledAlarms.first()

    // Use the deprecated but correct field
    val pi = scheduled.operation
    val flags = Shadows.shadowOf(pi).flags

    assertTrue(flags and PendingIntent.FLAG_IMMUTABLE != 0)
    assertTrue(flags and PendingIntent.FLAG_UPDATE_CURRENT != 0)
  }

  @Test
  fun scheduleNextTodoCheck_overridesPreviousAlarm_whenCalledTwice() {
    val scheduler = DailyTodoAlarmScheduler(context)
    val userId = "user123"

    scheduler.scheduleNextTodoCheck(userId)
    val firstAlarm =
        Shadows.shadowOf(context.getSystemService(Context.ALARM_SERVICE) as AlarmManager)
            .nextScheduledAlarm

    // Call again
    scheduler.scheduleNextTodoCheck(userId)

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val shadow = Shadows.shadowOf(alarmManager)
    val secondAlarm = shadow.nextScheduledAlarm

    assertNotNull(firstAlarm)
    assertNotNull(secondAlarm)

    // Should be different objects (overridden)
    assertNotEquals(firstAlarm, secondAlarm)
  }

  @Test
  fun computeNextMidnight_handlesMonthYearChange() {
    val fakeNow =
        Calendar.getInstance().apply { set(2023, Calendar.DECEMBER, 31, 23, 59, 59) }.timeInMillis

    val scheduler = DailyTodoAlarmScheduler(context, nowProvider = { fakeNow })

    val result = scheduler.computeNextMidnight()

    val expected =
        Calendar.getInstance()
            .apply {
              set(2024, Calendar.JANUARY, 1, 0, 0, 0)
              set(Calendar.MILLISECOND, 0)
            }
            .timeInMillis

    assertEquals(expected, result)
  }

  @Test
  fun scheduleNextTodoCheck_setsIntentTargetToReceiver() {
    val scheduler = DailyTodoAlarmScheduler(context)
    val userId = "user123"

    scheduler.scheduleNextTodoCheck(userId)

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val scheduled = Shadows.shadowOf(alarmManager).nextScheduledAlarm!!
    val pi = scheduled.operation

    val intent = Shadows.shadowOf(pi).savedIntent

    assertEquals(DailyTodoReminderReceiver::class.java.name, intent.component?.className)
  }

  @Test
  fun scheduleNextTodoCheck_schedulesAtComputedMidnight() {
    val scheduler = DailyTodoAlarmScheduler(context)
    val userId = "u1"

    val expected = scheduler.computeNextMidnight()

    scheduler.scheduleNextTodoCheck(userId)

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val shadowAlarm = Shadows.shadowOf(alarmManager)
    val scheduled = shadowAlarm.nextScheduledAlarm!!

    assertEquals(expected, scheduled.triggerAtTime)
  }
}
