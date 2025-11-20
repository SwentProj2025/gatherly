package com.android.gatherly.model.profile

import android.net.Uri
import com.android.gatherly.model.badge.ProfileBadges
import com.android.gatherly.model.badge.Rank
import com.android.gatherly.model.friends.Friends
import com.android.gatherly.model.notification.Notification

/**
 * Simplified in-memory local implementation of [ProfileRepository].
 *
 * Used for local testing or offline mode. Only implements the methods actually needed in the
 * current app logic.
 */
class ProfileLocalRepository : ProfileRepository {

  private val profiles: MutableList<Profile> = mutableListOf()

  private data class FriendRequest(val senderId: String, val recipientId: String)

  private val pendingFriendRequests = mutableListOf<FriendRequest>()

  var shouldFailRegisterUsername = false

  override suspend fun addProfile(profile: Profile) {
    if (!profiles.any { it.uid == profile.uid }) {
      profiles += profile
    }
  }

  override suspend fun getProfileByUid(uid: String): Profile? {
    return profiles.find { it.uid == uid }
  }

  override suspend fun updateProfile(profile: Profile) {
    val index = profiles.indexOfFirst { it.uid == profile.uid }
    if (index == -1) {
      throw NoSuchElementException("Profile not found for uid=${profile.uid}")
    } else {
      profiles[index] = profile
    }
  }

  override suspend fun deleteProfile(uid: String) {
    profiles.removeAll { it.uid == uid }
  }

  override suspend fun isUidRegistered(uid: String): Boolean {
    return profiles.any { it.uid == uid }
  }

  override suspend fun searchProfilesByNamePrefix(prefix: String): List<Profile> {
    return profiles.filter { it.name.contains(prefix, ignoreCase = true) }
  }

  // --- No-op or unused methods below ---
  override suspend fun isUsernameAvailable(username: String): Boolean {
    return profiles.none { it.username == username }
  }

  override suspend fun registerUsername(uid: String, username: String): Boolean {
    if (shouldFailRegisterUsername) return false
    if (!isUsernameAvailable(username)) return false
    val existing = getProfileByUid(uid)
    val updated = (existing ?: Profile(uid = uid)).copy(username = username)
    if (existing == null) addProfile(updated) else updateProfile(updated)
    return true
  }

  override suspend fun updateUsername(
      uid: String,
      oldUsername: String?,
      newUsername: String
  ): Boolean {
    // Allow same username (no change)
    if (oldUsername == newUsername) return true
    if (!isUsernameAvailable(newUsername)) return false

    val existing = getProfileByUid(uid)
    val updated = (existing ?: Profile(uid = uid)).copy(username = newUsername)
    if (existing == null) addProfile(updated) else updateProfile(updated)
    return true
  }

  override suspend fun updateProfilePic(uid: String, uri: Uri): String {
    val fakeUrl = "https://local.test.storage/$uid.jpg"
    val index = profiles.indexOfFirst { it.uid == uid }
    if (index == -1) {
      throw NoSuchElementException("Profile not found for uid=$uid")
    } else {
      val existing = profiles[index]
      val updated = existing.copy(profilePicture = fakeUrl)
      profiles[index] = updated
    }
    return fakeUrl
  }

  override suspend fun getProfileByUsername(username: String): Profile? =
      profiles.find { it.username == username }

  override suspend fun searchProfilesByUsernamePrefix(prefix: String, limit: Int): List<Profile> =
      profiles.filter { it.username.startsWith(prefix, ignoreCase = true) }.take(limit)

  override suspend fun initProfileIfMissing(uid: String, defaultPhotoUrl: String): Boolean {
    if (profiles.none { it.uid == uid }) {
      addProfile(Profile(uid = uid, profilePicture = defaultPhotoUrl))
      return true
    }
    return false
  }

  // ---- FRIENDS GESTION PART ----
  override suspend fun getFriendsAndNonFriendsUsernames(currentUserId: String): Friends {
    val currentProfile =
        getProfileByUid(currentUserId)
            ?: throw NoSuchElementException("Profile not found for uid=$currentUserId")

    val friendUsernames =
        currentProfile.friendUids.mapNotNull { friendUid -> getProfileByUid(friendUid)?.username }
    val nonFriendUsernames = getListNoFriends(currentUserId)

    return Friends(friendUsernames = friendUsernames, nonFriendUsernames = nonFriendUsernames)
  }

  override suspend fun deleteUserProfile(uid: String) {
    profiles.removeIf { it.uid == uid }
  }

  // ---- FRIENDS GESTION PART ----

  override suspend fun getListNoFriends(currentUserId: String): List<String> {
    val currentProfile = getProfileByUid(currentUserId) ?: return emptyList()
    val friendUids = currentProfile.friendUids.toSet()

    return profiles
        .filter { it.uid != currentUserId && it.uid !in friendUids }
        .mapNotNull { it.username.takeIf { username -> username.isNotBlank() } }
  }

  override suspend fun deleteFriend(friend: String, currentUserId: String) {
    val current = getProfileByUid(currentUserId) ?: return
    val friendProfile = getProfileByUsername(friend) ?: return

    // Remove friend from current user
    val updatedCurrentFriends = current.friendUids.filter { it != friendProfile.uid }
    updateProfile(current.copy(friendUids = updatedCurrentFriends))

    // Remove current user from the friend’s profile (symmetry)
    val updatedFriendFriends = friendProfile.friendUids.filter { it != currentUserId }
    updateProfile(friendProfile.copy(friendUids = updatedFriendFriends))
  }

  override suspend fun addFriend(friend: String, currentUserId: String) {
    val currentProfile = getProfileByUid(currentUserId) ?: return
    val friendId = getProfileByUsername(friend)?.uid
    if (!currentProfile.friendUids.contains(friendId)) {
      val updatedFriends = currentProfile.friendUids + friendId
      val updatedProfile = currentProfile.copy(friendUids = updatedFriends as List<String>)
      updateProfile(updatedProfile)
    }
  }

  // ---- STATUS GESTION PART ----

  override suspend fun updateStatus(uid: String, status: ProfileStatus) {
    val index = profiles.indexOfFirst { it.uid == uid }
    if (index != -1) {
      val existing = profiles[index]
      profiles[index] = existing.copy(status = status)
    }
  }

  override suspend fun createEvent(eventId: String, currentUserId: String) {
    val currentProfile = getProfileByUid(currentUserId) ?: return
    if (!currentProfile.ownedEventIds.contains(eventId)) {
      val updateEventOwnerIds = currentProfile.ownedEventIds + eventId
      val updatedProfile = currentProfile.copy(ownedEventIds = updateEventOwnerIds)
      updateProfile(updatedProfile)
    }
  }

  override suspend fun deleteEvent(eventId: String, currentUserId: String) {
    val currentProfile = getProfileByUid(currentUserId) ?: return
    if (currentProfile.ownedEventIds.contains(eventId)) {
      val updateEventOwnerIds = currentProfile.ownedEventIds.filter { it != eventId }
      val updatedProfile = currentProfile.copy(ownedEventIds = updateEventOwnerIds)
      updateProfile(updatedProfile)
    }
  }

  override suspend fun participateEvent(eventId: String, currentUserId: String) {
    val currentProfile = getProfileByUid(currentUserId) ?: return
    if (!currentProfile.participatingEventIds.contains(eventId)) {
      val updateEventIds = currentProfile.participatingEventIds + eventId
      val updatedProfile = currentProfile.copy(participatingEventIds = updateEventIds)
      updateProfile(updatedProfile)
    }
  }

  override suspend fun allParticipateEvent(eventId: String, participants: List<String>) {
    participants.forEach { participant -> participateEvent(eventId, participant) }
  }

  override suspend fun unregisterEvent(eventId: String, currentUserId: String) {
    val currentProfile = getProfileByUid(currentUserId) ?: return
    if (currentProfile.participatingEventIds.contains(eventId)) {
      val updatedEventIds = currentProfile.participatingEventIds.filter { it != eventId }
      val updatedProfile = currentProfile.copy(participatingEventIds = updatedEventIds)
      updateProfile(updatedProfile)
    }
  }

  override suspend fun allUnregisterEvent(eventId: String, participants: List<String>) {
    participants.forEach { participant -> unregisterEvent(eventId, participant) }
  }

  override suspend fun sendFriendRequest(senderId: String, recipientId: String) {
    // Prevent duplicates
    val alreadyPending =
        pendingFriendRequests.any { it.senderId == senderId && it.recipientId == recipientId }
    if (alreadyPending) return

    // Prevent sending request to yourself
    if (senderId == recipientId) return

    pendingFriendRequests += FriendRequest(senderId, recipientId)
  }

  override suspend fun acceptFriendRequest(notification: Notification) {
    val senderId = notification.senderId ?: return
    val recipientId = notification.recipientId

    // Remove pending request
    pendingFriendRequests.removeAll { it.senderId == senderId && it.recipientId == recipientId }

    // Add each other as friends
    val senderProfile = getProfileByUid(senderId)
    val recipientProfile = getProfileByUid(recipientId)

    if (senderProfile != null && !senderProfile.friendUids.contains(recipientId)) {
      updateProfile(senderProfile.copy(friendUids = senderProfile.friendUids + recipientId))
    }

    if (recipientProfile != null && !recipientProfile.friendUids.contains(senderId)) {
      updateProfile(recipientProfile.copy(friendUids = recipientProfile.friendUids + senderId))
    }
  }

  override suspend fun rejectFriendRequest(notification: Notification) {
    val senderId = notification.senderId ?: return
    val recipientId = notification.recipientId

    // Remove pending request — no friendship changes
    pendingFriendRequests.removeAll { it.senderId == senderId && it.recipientId == recipientId }
  }

  // ---- BADGE GESTION PART ----

  override suspend fun updateBadges(
      userProfile: Profile,
      createdTodosCount: Int?,
      completedTodosCount: Int?
  ) {

    if (createdTodosCount == null || completedTodosCount == null) {
      return
    }
    val updatedBadges =
        ProfileBadges(
            addFriends = rank(userProfile.friendUids.size),
            createdTodos = rank(createdTodosCount),
            completedTodos = rank(completedTodosCount),
            createEvent = rank(userProfile.ownedEventIds.size),
            participateEvent = rank(userProfile.participatingEventIds.size),
            focusSessionPoint = rank(userProfile.focusSessionIds.size))
    val updatedProfile = userProfile.copy(badges = updatedBadges)
    updateProfile(updatedProfile)
  }

  private fun rank(count: Int): Rank =
      when {
        count >= 20 -> Rank.LEGEND
        count >= 10 -> Rank.DIAMOND
        count >= 5 -> Rank.GOLD
        count >= 3 -> Rank.BRONZE
        count >= 1 -> Rank.STARTING
        else -> Rank.BLANK
      }
}
