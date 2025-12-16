package com.android.gatherly.model.friends

/**
 * Represents friends and non friends of the user. To be used in the Profile
 *
 * @param friendUsernames Usernames of the user's friends
 * @param nonFriendUsernames Usernames of users that are not friends with the current user
 */
data class Friends(
    val friendUsernames: List<String> = emptyList(),
    val nonFriendUsernames: List<String> = emptyList()
)
