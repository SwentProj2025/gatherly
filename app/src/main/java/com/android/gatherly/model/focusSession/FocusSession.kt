package com.android.gatherly.model.focusSession

import com.google.firebase.Timestamp
import kotlin.time.Duration

/** Represents a single [FocusSession] item within the app. */
data class FocusSession(
    /** ID of focus session */
    val focusSessionId: String,
    /** ID of user who created the focus session */
    val creatorId: String,
    /** ID of the ToDo this focus session was linked to, or `null` if standalone */
    val linkedTodoId: String? = null,
    /** duration of the session in seconds */
    val duration: Duration = Duration.ZERO,
    /** Timestamp indicating when the focus session started */
    val startedAt: Timestamp? = null,
    /** Timestamp indicating when the focus session started */
    val endedAt: Timestamp? = null
)
