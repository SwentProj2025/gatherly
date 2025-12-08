package com.android.gatherly.model.profile

import android.net.Uri
import com.android.gatherly.model.badge.BadgeType
import com.android.gatherly.model.friends.Friends

/**
 * Interface for repositories managing user profiles.
 *
 * A [Profile] contains basic information such as the user's name, school, and social data (friends,
 * groups, and focus sessions). Implementations may store this data in Firestore or in-memory for
 * testing.
 */
interface ProfileRepository {

  /**
   * Retrieves the [Profile] corresponding to a given uid.
   *
   * @param uid The unique identifier of the user.
   * @return The corresponding [Profile], or null if none exists.
   */
  suspend fun getProfileByUid(uid: String): Profile?

  /**
   * Updates an existing [Profile].
   *
   * @param profile The [Profile] object with updated fields.
   */
  suspend fun updateProfile(profile: Profile)

  /**
   * Deletes the [Profile] with the specified uid.
   *
   * @param uid The unique identifier of the user.
   */
  suspend fun deleteProfile(uid: String)

  /**
   * Checks if a [Profile] exists for the specified uid.
   *
   * @param uid The user identifier to check.
   * @return `true` if a [Profile] exists, `false` otherwise.
   */
  suspend fun isUidRegistered(uid: String): Boolean

  /**
   * Searches for [Profile]s whose names start with the given [prefix].
   *
   * @param prefix The name prefix to search for (case-insensitive).
   * @return A list of matching [Profile] objects.
   */
  suspend fun searchProfilesByNamePrefix(prefix: String): List<Profile>

  /**
   * Checks if a username is valid and available.
   *
   * @param username The username to verify.
   * @return true if the username is valid and not taken, false otherwise.
   */
  suspend fun isUsernameAvailable(username: String): Boolean

  /**
   * Registers a unique username for a user.
   *
   * @param uid The UID of the user.
   * @param username The username to register.
   * @return true if registration succeeded, false if invalid or already taken.
   */
  suspend fun registerUsername(uid: String, username: String): Boolean

  /**
   * Updates a user's username.
   *
   * @param uid The UID of the user.
   * @param oldUsername The previous username, if any.
   * @param newUsername The new desired username.
   * @return true if successfully updated, false otherwise.
   */
  suspend fun updateUsername(uid: String, oldUsername: String?, newUsername: String): Boolean

  /**
   * Updates a user's profile picture.
   *
   * @param uid The UID of the user.
   * @param url The new profile picture link.
   */
  suspend fun updateProfilePic(uid: String, uri: Uri): String
  /**
   * Retrieves a [Profile] by its username.
   *
   * @param username The username to search for.
   * @return The corresponding [Profile], or null if not found.
   */
  suspend fun getProfileByUsername(username: String): Profile?

  /**
   * Searches [Profile]s by username prefix.
   *
   * @param prefix The username prefix to search for.
   * @param limit The maximum number of results to return.
   * @return A list of matching [Profile] objects.
   */
  suspend fun searchProfilesByUsernamePrefix(prefix: String, limit: Int = 10): List<Profile>

  /**
   * Ensures a [Profile] document exists for the given [uid]. Creates one with a defaultPhotoUrl and
   * an empty username if missing. This expects that if it was missing the user is then prompted to
   * update his username.
   *
   * @param uid The user ID.
   * @param defaultPhotoUrl The default photo URL to assign if a [Profile] is created.
   * @return true if a new [Profile] was created, false if it already existed.
   */
  suspend fun initProfileIfMissing(uid: String, defaultPhotoUrl: String): Boolean

  /**
   * Deletes a user's entire profile, including:
   * - Their document in /profiles/{uid}
   * - Their username in /usernames/{username}
   * - Their profile picture in Firebase Storage
   *
   * After this call, the username becomes available again for reuse.
   */
  suspend fun deleteUserProfile(uid: String)

  /** Creates a profile. This is to be used only for testing purpose. */
  suspend fun addProfile(profile: Profile)

  /**
   * Retrieves the list of current user's friends (as usernames) and the list of other users who are
   * not friends (as usernames).
   *
   * @param currentUserId The ID of the current user.
   * @return A Friends object containing the two processed lists of usernames.
   */
  suspend fun getFriendsAndNonFriendsUsernames(currentUserId: String): Friends

  /**
   * Retrieves all the profiles username that the user is not friends with.
   *
   * @param currentUserId the ID of the current user
   */
  suspend fun getListNoFriends(currentUserId: String): List<String>
  /**
   * Deletes a Friend from the friend list from the repository.
   *
   * @param friend the username of the friend to unfollow
   * @param currentUserId the ID of the current user
   */
  suspend fun deleteFriend(friend: String, currentUserId: String)

  /**
   * Adds a Friend to the friend list from the repository.
   *
   * @param friend the username of the friend to follow
   * @param currentUserId the ID of the current user
   */
  suspend fun addFriend(friend: String, currentUserId: String)

  /**
   * Adds a UID to the list of pending friend requests sent by the current user. Called when a user
   * initiates a friend request to user with said uid.
   *
   * @param currentUserId UID of the user who is sending the request.
   * @param targetUid UID of the user to whom the request is being sent.
   */
  suspend fun addPendingSentFriendUid(currentUserId: String, targetUid: String)

  /**
   * Removes a UID from the list of pending friend requests sent by the current user. Called when
   * current user got an answer to his request by the recipient or cancelled his request.
   *
   * @param currentUserId UID of the user who originally sent the request.
   * @param targetUid UID being removed from the pending list.
   */
  suspend fun removePendingSentFriendUid(currentUserId: String, targetUid: String)

  /**
   * Updates the online/offline status of a user and the source of the update
   *
   * @param uid The user ID whose status to update.
   * @param status The new [ProfileStatus] to set.
   * @param source Indicates if the status update is automatic or manual. Defaults to AUTOMATIC.
   */
  suspend fun updateStatus(
      uid: String,
      status: ProfileStatus,
      source: UserStatusSource = UserStatusSource.AUTOMATIC
  )

  /**
   * The user creates a new event
   *
   * @param eventId the ID of the event created
   * @param currentUserId the ID of the current user
   */
  suspend fun createEvent(eventId: String, currentUserId: String)

  /**
   * The user deletes an event he created
   *
   * @param eventId the ID of the event he wants to delete
   * @param currentUserId the ID of the current user who is the owner of this event
   */
  suspend fun deleteEvent(eventId: String, currentUserId: String)

  /**
   * The current user is added as a participant to an event
   *
   * @param eventId The ID of the event he wants to participate
   * @param currentUserId the ID of the current user
   */
  suspend fun participateEvent(eventId: String, currentUserId: String)

  /**
   * When the event is created, we want chosen participants to be register for this event
   *
   * @param eventId the ID of the event concerned
   * @param participants list of all the ID of profiles wanted to be registered for this event
   */
  suspend fun allParticipateEvent(eventId: String, participants: List<String>)

  /**
   * The current user does not want anymore to participate to an event
   *
   * @param eventId The ID of the event he wants to unregister
   * @param currentUserId the ID of the current user
   */
  suspend fun unregisterEvent(eventId: String, currentUserId: String)

  /**
   * When the event is delete, we want every participant to be unregister from this event
   *
   * @param eventId the ID of the event concerned
   * @param participants list of all the ID of profiles register for this event
   */
  suspend fun allUnregisterEvent(eventId: String, participants: List<String>)

  // -- BADGES GESTION PART --

  /**
   * The user obtains a badge in his profile via the badgeId
   *
   * @param uid the user's profile id
   * @param badgeId the badge that the user just gained
   */
  suspend fun addBadge(uid: String, badgeId: String)

  /**
   * The user's count is updated accordingly to the action that has been done is his profile
   *
   * @param uid the user's profile id
   * @param type the type of action that needs it's count incremented
   */
  suspend fun incrementBadge(uid: String, type: BadgeType)

  /**
   * Adds the given number of points to the user's total number of points.
   *
   * @param uid The profile to update
   * @param points The number of points to add
   * @param addToLeaderboard True if the points should also be added to the users weekly point
   *   count. False if these points should not count towards the leaderboard.
   */
  suspend fun updateFocusPoints(uid: String, points: Double, addToLeaderboard: Boolean = true)
}
