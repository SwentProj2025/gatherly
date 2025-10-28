package com.android.gatherly.model.profile

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

  /** Creates a profile. This is to be used only for testing purpose. */
  suspend fun addProfile(profile: Profile)
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

}
