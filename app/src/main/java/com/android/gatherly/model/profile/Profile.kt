package com.android.gatherly.model.profile

import com.google.firebase.Timestamp

/** Represents a single [Profile] item within the app. */
data class Profile(
    val uid: String = "",
    val username: String = "",
    val name: String = "",
    val focusSessionIds: List<String> = emptyList(),
    val participatingEventIds: List<String> =
        emptyList(), // Represents the list of events that the user is participating
    val ownedEventIds: List<String> =
        emptyList(), // Represents the list of events that the user create
    val groupIds: List<String> = emptyList(),
    val friendUids: List<String> = emptyList(),
    val pendingSentFriendsUids: List<String> =
        emptyList(), // List of uids of user we sent a friend request to that is still pending
    val school: String = "",
    val schoolYear: String = "",
    val birthday: Timestamp? = null,
    val profilePicture: String = "",
    val status: ProfileStatus = ProfileStatus.OFFLINE,
    val userStatusSource: UserStatusSource = UserStatusSource.AUTOMATIC,
    val badgeIds: List<String> = emptyList(),
    val badgeCount: Map<String, Long> = emptyMap(),
    val focusPoints: Double = 0.0,
    val weeklyPoints: Double = 0.0
)
