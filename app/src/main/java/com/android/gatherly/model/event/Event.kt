package com.android.gatherly.model.event

import com.android.gatherly.model.map.Location
import com.google.firebase.Timestamp

/** Represents a single [Event] item within the app. */
data class Event(
    val id: String,
    val title: String,
    val description: String,
    val creatorName: String,
    val location: Location?,
    val date: Timestamp,
    val startTime: Timestamp,
    val endTime: Timestamp, // TODO: Is timestamp adequate for Start/End time?
    val creatorId: String,
    val participants: List<String>, // contains userIds
    val status: EventStatus
)

/** Represents the state of an [Event] item. */
enum class EventStatus {
  UPCOMING,
  ONGOING,
  PAST
}

/**
 * Converts the EventStatus enum to a more readable display string (camel case).
 *
 * @return A string representation of the EventStatus, formatted for display.
 */
fun EventStatus.displayString(): String =
    name.replace("_", " ").lowercase().replaceFirstChar {
      if (it.isLowerCase()) it.titlecase() else it.toString()
    }
