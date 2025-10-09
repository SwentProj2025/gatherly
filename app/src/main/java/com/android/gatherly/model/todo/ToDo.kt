package com.android.gatherly.model.todo

import com.android.gatherly.model.map.Location
import com.google.firebase.Timestamp
import java.util.Locale

/** Represents a single [ToDo] item within the app. */
data class ToDo(
    val uid: String,
    val name: String,
    val description: String,
    val assigneeName: String,
    val dueDate: Timestamp,
    val dueTime: Timestamp?,
    val location: Location?,
    val status: ToDoStatus,
    val ownerId: String
)

/** Represents the state of a [ToDo] item. */
enum class ToDoStatus {
  ONGOING,
  ENDED
}

/**
 * Converts the ToDoStatus enum to a more readable display string (camel case).
 *
 * @return A string representation of the ToDoStatus, formatted for display.
 */
fun ToDoStatus.displayString(): String =
    name.replace("_", " ").lowercase(Locale.ROOT).replaceFirstChar {
      if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
