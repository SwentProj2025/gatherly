package com.android.gatherly.utils

import com.android.gatherly.model.event.Event
import com.android.gatherly.model.event.EventStatus
import com.google.firebase.Timestamp
import java.util.Calendar

/**
 * Event status utilities for the Gatherly app.
 *
 * Provides functionality to determine an event's current status (UPCOMING, ONGOING, or PAST) based
 * on the current time.
 */

/**
 * Updates an event's status based on the current time.
 *
 * Compares the current timestamp against the event's start and end times to determine whether the
 * event is upcoming, currently ongoing, or already past. The function combines the event's date
 * with its start and end times to create precise timestamps for comparison.
 *
 * @param event The event to update.
 * @return A copy of the event with its status field updated to reflect the current time.
 */
fun updateEventStatus(event: Event): Event {
  val date = event.date
  val startTime = event.startTime
  val endTime = event.endTime

  val now = Timestamp.now()

  val startCal = Calendar.getInstance().apply { time = startTime.toDate() }

  val endCal = Calendar.getInstance().apply { time = endTime.toDate() }

  // Combine event date with start time to get precise event start timestamp
  val eventStart =
      Calendar.getInstance()
          .apply {
            time = date.toDate()
            set(Calendar.HOUR_OF_DAY, startCal[Calendar.HOUR_OF_DAY])
            set(Calendar.MINUTE, startCal[Calendar.MINUTE])
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
          }
          .let { Timestamp(it.time) }

  // Combine event date with end time to get precise event end timestamp
  val eventEnd =
      Calendar.getInstance()
          .apply {
            time = date.toDate()
            set(Calendar.HOUR_OF_DAY, endCal[Calendar.HOUR_OF_DAY])
            set(Calendar.MINUTE, endCal[Calendar.MINUTE])
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
          }
          .let { Timestamp(it.time) }

  val updateStatus =
      when {
        now < eventStart -> EventStatus.UPCOMING
        now >= eventStart && now <= eventEnd -> EventStatus.ONGOING
        else -> EventStatus.PAST
      }
  return event.copy(status = updateStatus)
}
