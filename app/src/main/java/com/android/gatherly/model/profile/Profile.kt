package com.android.gatherly.model.profile

import com.google.firebase.Timestamp

/** Represents a single [Profile] item within the app. */
data class Profile(
    val uid: String = "",
    val username: String = "",
    val name: String = "",
    val focusSessionIds: List<String> = emptyList(),
    val eventIds: List<String> =
        emptyList(), // Represents the list of events that the user is participating
    val eventOwnerIds: List<String> =
        emptyList(), // Represents the list of events that the user create
    val groupIds: List<String> = emptyList(),
    val friendUids: List<String> = emptyList(),
    val school: String = "",
    val schoolYear: String = "",
    val birthday: Timestamp? = null,
    val profilePicture: String = ""
)
