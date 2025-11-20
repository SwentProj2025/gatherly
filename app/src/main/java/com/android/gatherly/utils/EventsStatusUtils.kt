package com.android.gatherly.utils

import com.android.gatherly.model.event.Event
import com.android.gatherly.model.event.EventStatus
import com.google.firebase.Timestamp
import java.util.Calendar

fun updateEventStatus(event: Event): Event {
  val date = event.date
  val startTime = event.startTime
  val endTime = event.endTime

  val now = Timestamp.now()

  val startCal = Calendar.getInstance().apply { time = startTime.toDate() }

  val endCal = Calendar.getInstance().apply { time = endTime.toDate() }
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
