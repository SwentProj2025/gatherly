package com.android.gatherly.utilstest

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.gatherly.utils.EventAlarmScheduler
import com.android.gatherly.utils.EventAlarmScheduler.Companion.EXTRA_EVENT_ID
import com.android.gatherly.utils.EventAlarmScheduler.Companion.EXTRA_USER_ID
import com.android.gatherly.utils.EventReminderReceiver
import com.google.firebase.Timestamp
import java.util.Calendar
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class EventAlarmSchedulerTest {

  private val context: Context = ApplicationProvider.getApplicationContext()

  /** Helper to build a Timestamp for a specific local date/time. */
  private fun timestampOf(
      year: Int,
      month: Int, // Calendar.JANUARY etc.
      day: Int,
      hour: Int = 0,
      minute: Int = 0,
      second: Int = 0,
      millis: Int = 0
  ): Timestamp {
    val cal =
        Calendar.getInstance().apply {
          set(year, month, day, hour, minute, second)
          set(Calendar.MILLISECOND, millis)
        }
    return Timestamp(cal.time)
  }

  @Test
  fun computeEventStartMillis_combinesDateAndTimeCorrectly() {
    val scheduler = EventAlarmScheduler(context)

    // Event on 2025-01-10 15:30
    val eventDate = timestampOf(2025, Calendar.JANUARY, 10, 0, 0, 0, 0)
    val eventStartTime = timestampOf(1970, Calendar.JANUARY, 1, 15, 30, 0, 0)

    val resultMillis = scheduler.computeEventStartMillis(eventDate, eventStartTime)

    val cal = Calendar.getInstance().apply { timeInMillis = resultMillis }

    assertEquals(2025, cal.get(Calendar.YEAR))
    assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH))
    assertEquals(10, cal.get(Calendar.DAY_OF_MONTH))
    assertEquals(15, cal.get(Calendar.HOUR_OF_DAY))
    assertEquals(30, cal.get(Calendar.MINUTE))
    assertEquals(0, cal.get(Calendar.SECOND))
    assertEquals(0, cal.get(Calendar.MILLISECOND))
  }

  @Test
  fun scheduleEventReminder_schedulesExactAlarmInFuture_withCorrectIntentAndFlags() {
    // Arrange
    val now = timestampOf(2025, Calendar.JANUARY, 10, 10, 0).toDate().time
    val userId = "user42"
    val eventId = "event123"

    val eventDate = timestampOf(2025, Calendar.JANUARY, 10, 0, 0)
    val eventStartTime = timestampOf(1970, Calendar.JANUARY, 1, 12, 0) // 12:00

    val scheduler =
        EventAlarmScheduler(
            context = context, nowProvider = { now } // freeze "now" for test
            )

    val expectedTrigger = scheduler.computeEventStartMillis(eventDate, eventStartTime)

    // Act
    scheduler.scheduleEventReminder(
        userId = userId, eventId = eventId, eventDate = eventDate, eventStartTime = eventStartTime)

    // Assert
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val shadowAlarm = Shadows.shadowOf(alarmManager)

    val scheduled = shadowAlarm.scheduledAlarms.firstOrNull()
    assertNotNull("Expected one scheduled alarm", scheduled)

    // Type should be RTC_WAKEUP
    assertEquals(AlarmManager.RTC_WAKEUP, scheduled!!.type)

    // Trigger time should match our computed event start
    assertEquals(expectedTrigger, scheduled.triggerAtTime)

    // Inspect PendingIntent
    val pendingIntent = scheduled.operation
    val shadowPi = Shadows.shadowOf(pendingIntent)
    val intent = shadowPi.savedIntent

    // Intent target receiver should be EventReminderReceiver
    assertEquals(EventReminderReceiver::class.java.name, intent.component?.className)

    // Extras should contain correct user & event IDs
    assertEquals(userId, intent.getStringExtra(EXTRA_USER_ID))
    assertEquals(eventId, intent.getStringExtra(EXTRA_EVENT_ID))

    // Flags must include IMMUTABLE and UPDATE_CURRENT
    val flags = shadowPi.flags
    assertTrue(flags and PendingIntent.FLAG_IMMUTABLE != 0)
    assertTrue(flags and PendingIntent.FLAG_UPDATE_CURRENT != 0)
  }

  @Test
  fun scheduleEventReminder_doesNotScheduleForPastEvents() {
    // Arrange: event at 09:00, but "now" is 10:00 the same day.
    val eventDate = timestampOf(2025, Calendar.MARCH, 5, 0, 0)
    val eventStartTime = timestampOf(1970, Calendar.JANUARY, 1, 9, 0)
    val now = timestampOf(2025, Calendar.MARCH, 5, 10, 0).toDate().time

    val scheduler = EventAlarmScheduler(context = context, nowProvider = { now })

    // Act
    scheduler.scheduleEventReminder(
        userId = "userX",
        eventId = "eventPast",
        eventDate = eventDate,
        eventStartTime = eventStartTime)

    // Assert: no alarms scheduled
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val shadowAlarm = Shadows.shadowOf(alarmManager)
    assertTrue(
        "No alarm should be scheduled for past events", shadowAlarm.scheduledAlarms.isEmpty())
  }

  @Test
  fun scheduleEventReminder_replacesExistingAlarmForSameUserAndEvent() {
    // Arrange
    val userId = "user42"
    val eventId = "event123"

    // First schedule: event at 12:00
    val date = timestampOf(2025, Calendar.APRIL, 1, 0, 0)
    val startTime1 = timestampOf(1970, Calendar.JANUARY, 1, 12, 0)
    val startTime2 = timestampOf(1970, Calendar.JANUARY, 1, 14, 30)

    val now = timestampOf(2025, Calendar.APRIL, 1, 9, 0).toDate().time

    val scheduler = EventAlarmScheduler(context = context, nowProvider = { now })

    // First schedule
    scheduler.scheduleEventReminder(userId, eventId, date, startTime1)

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val shadowAlarm = Shadows.shadowOf(alarmManager)

    assertEquals(1, shadowAlarm.scheduledAlarms.size)
    val firstTrigger = shadowAlarm.scheduledAlarms.first().triggerAtTime

    // Second schedule with a later time should replace the first alarm
    scheduler.scheduleEventReminder(userId, eventId, date, startTime2)

    assertEquals(
        "There should still be exactly one alarm after rescheduling",
        1,
        shadowAlarm.scheduledAlarms.size)

    val secondTrigger = shadowAlarm.scheduledAlarms.first().triggerAtTime

    assertNotEquals("Trigger time should change after rescheduling", firstTrigger, secondTrigger)

    val expectedSecondTrigger = scheduler.computeEventStartMillis(date, startTime2)
    assertEquals(expectedSecondTrigger, secondTrigger)
  }

  @Test
  fun cancelEventReminder_removesPreviouslyScheduledAlarm() {
    // Arrange
    val userId = "user42"
    val eventId = "eventToCancel"

    val date = timestampOf(2025, Calendar.JUNE, 10, 0, 0)
    val startTime = timestampOf(1970, Calendar.JANUARY, 1, 16, 0)
    val now = timestampOf(2025, Calendar.JUNE, 10, 10, 0).toDate().time

    val scheduler = EventAlarmScheduler(context = context, nowProvider = { now })

    // Schedule first
    scheduler.scheduleEventReminder(userId, eventId, date, startTime)

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val shadowAlarm = Shadows.shadowOf(alarmManager)

    assertEquals(1, shadowAlarm.scheduledAlarms.size)

    // Act: cancel
    scheduler.cancelEventReminder(eventId = eventId, userId = userId)

    // Assert: all alarms for that PendingIntent should be gone
    assertTrue(
        "Expected no alarms after cancelEventReminder", shadowAlarm.scheduledAlarms.isEmpty())
  }
}
