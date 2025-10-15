package com.android.gatherly.model.profile

import com.google.firebase.Timestamp

/** Represents a single [Profile] item within the app. */
data class Profile(
    val uid: String = "",
    val name: String = "",
    val focusSessionIds: List<String> = emptyList(),
    val eventIds: List<String> = emptyList(),
    val groupIds: List<String> = emptyList(),
    val friendUids: List<String> = emptyList(),
    val school: String = "",
    val schoolYear: String = "",
    val birthday: Timestamp? = null,
    val profilePicture: String = ""
)
