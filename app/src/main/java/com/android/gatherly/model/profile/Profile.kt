package com.android.gatherly.model.profile

/** Represents a single [Profile] item within the app. */
data class Profile(
    val uid: String = "",
    val name: String = "",
    val focusSessionIds: List<String> = emptyList(),
    val eventIds: List<String> = emptyList(),
    val groupIds: List<String> = emptyList(),
    val friendUids: List<String> = emptyList()
)
