package com.android.gatherly.model.todo

import com.android.gatherly.model.map.DisplayedMapElement
import com.android.gatherly.model.map.Location
import com.android.gatherly.model.todoCategory.ToDoCategory
import com.google.firebase.Timestamp

/** Represents a single [ToDo] item within the app. */
data class ToDo(
    /** Unique identifier of this todo item. */
    val uid: String,
    /** Title of the todo */
    val name: String,
    /** Long-form description shown on the todo detail screen. */
    val description: String,
    /** Optional due date (calendar day). */
    val dueDate: Timestamp?,
    /** Optional due time (clock time). */
    val dueTime: Timestamp?,
    /** Optional location associated with this todo. */
    override val location: Location?,
    /** Current lifecycle status (ongoing vs completed). */
    val status: ToDoStatus,
    /** UID of the user who owns this todo. */
    val ownerId: String,
    /** Priority level of the todo. Defaults to [ToDoPriority.NONE]. */
    val priorityLevel: ToDoPriority = ToDoPriority.NONE,
    /** Optional category/tag used for filtering/grouping. */
    val tag: ToDoCategory? = null,
) : DisplayedMapElement

/** Represents the state of a [ToDo] item. */
enum class ToDoStatus {
  /** The todo is still active and not completed. */
  ONGOING,
  /** The todo has been completed. */
  ENDED
}

/** Represents the priority level of a [ToDo] item */
enum class ToDoPriority(val displayName: String?) {
  NONE(null),
  LOW("Low Priority"),
  MEDIUM("Medium Priority"),
  HIGH("High Priority")
}
