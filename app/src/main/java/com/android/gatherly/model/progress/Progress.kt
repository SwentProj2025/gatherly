package com.android.gatherly.model.progress

/** Represents the Progress counts of the user. */
data class Progress(
    val createdTodoCount: Int = 0,
    val completedTodoCount: Int = 0,
    val createdEventCount: Int = 0,
    val participatedEventCount: Int = 0,
    val completedFocusSessionCount: Int = 0,
    val addedFriendsCount: Int = 0
)
