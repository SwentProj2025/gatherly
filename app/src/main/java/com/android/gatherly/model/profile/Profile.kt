package com.android.gatherly.model.profile

import com.google.firebase.Timestamp

/**
 * Represents a single [Profile] item within the app.
 *
 * @property uid The unique identifier for the user
 * @property username The username of the user
 * @property name The display name of the user
 * @property focusSessionIds List of focus session IDs associated with the user
 * @property participatingEventIds List of event IDs that the user is participating in
 * @property ownedEventIds List of event IDs that the user has created
 * @property groupIds List of group IDs that the user is a member of
 * @property friendUids List of user IDs who are friends with the user
 * @property pendingSentFriendsUids List of user IDs to whom the user has sent friend requests that
 *   are still pending
 * @property school The school the user is associated with
 * @property schoolYear The academic year of the user
 * @property birthday The birthday of the user
 * @property profilePicture The URL of the user's profile picture
 * @property status The current online status of the user
 * @property userStatusSource The source of the user's status (automatic or manual)
 * @property badgeIds List of badge IDs that the user has earned
 * @property badgeCount A map representing the count of each badge type the user has earned
 * @property focusPoints The total focus points accumulated by the user
 * @property weeklyPoints The focus points accumulated by the user in the current week
 * @property bio The biography or description provided by the user
 */
data class Profile(
    val uid: String = "",
    val username: String = "",
    val name: String = "",
    val focusSessionIds: List<String> = emptyList(),
    val participatingEventIds: List<String> = emptyList(),
    val ownedEventIds: List<String> = emptyList(),
    val groupIds: List<String> = emptyList(),
    val friendUids: List<String> = emptyList(),
    val pendingSentFriendsUids: List<String> = emptyList(),
    val school: String = "",
    val schoolYear: String = "",
    val birthday: Timestamp? = null,
    val profilePicture: String = "",
    val status: ProfileStatus = ProfileStatus.OFFLINE,
    val userStatusSource: UserStatusSource = UserStatusSource.AUTOMATIC,
    val badgeIds: List<String> = emptyList(),
    val badgeCount: Map<String, Long> = emptyMap(),
    val focusPoints: Double = 0.0,
    val weeklyPoints: Double = 0.0,
    val bio: String = ""
)
