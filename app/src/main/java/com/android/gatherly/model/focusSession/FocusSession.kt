package com.android.gatherly.model.focusSession

import com.google.firebase.Timestamp
import kotlin.time.Duration

/**
 * Represents a single [FocusSession] item within the app.
 *
 * @property focusSessionId ID of the focus session.
 * @property creatorId ID of the user who created the focus session.
 * @property linkedTodoId ID of the ToDo this focus session was linked to, or `null` if standalone.
 * @property duration Duration of the session. Defaults to zero.
 * @property startedAt Timestamp indicating when the focus session started.
 * @property endedAt Timestamp indicating when the focus session ended.
 */
data class FocusSession(
    val focusSessionId: String,
    val creatorId: String,
    val linkedTodoId: String? = null,
    val duration: Duration = Duration.ZERO,
    val startedAt: Timestamp? = null,
    val endedAt: Timestamp? = null
)
