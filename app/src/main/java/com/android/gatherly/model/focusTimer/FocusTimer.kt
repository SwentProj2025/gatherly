package com.android.gatherly.model.focusTimer

import com.google.firebase.Timestamp
import kotlin.time.Duration

/** Represents a single [FocusTimer] item within the app. */
data class FocusTimer(
    /** ID of the ToDo this timer session is linked to, or `null` if standalone */
    val linkedTodoId: String? = null,
    /** Planned duration of the session in seconds */
    val plannedDuration: Duration = Duration.ZERO,
    /** Actual elapsed time in seconds when the session ends or pauses */
    val elapsedTime: Duration = Duration.ZERO,
    /** Timestamp indicating when the timer session started */
    val startedAt: Timestamp? = null
)
