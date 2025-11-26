package com.android.gatherly.model.profile

import android.net.Uri
import com.android.gatherly.model.badge.BadgeRank
import com.android.gatherly.model.badge.BadgeType
import com.android.gatherly.model.friends.Friends

/**
 * Simplified in-memory local implementation of [ProfileRepository].
 *
 * Used for local testing or offline mode. Only implements the methods actually needed in the
 * current app logic.
 */
class ProfileLocalRepository : ProfileRepository {

  private val profiles: MutableList<Profile> = mutableListOf()

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
    val currentProfile = getProfileByUid(currentUserId) ?: return
    val friendId = getProfileByUsername(friend)?.uid
    val updatedFriends = currentProfile.friendUids.filter { it != friendId }
    val updatedProfile = currentProfile.copy(friendUids = updatedFriends)
    updateProfile(updatedProfile)
  }

  override suspend fun addFriend(friend: String, currentUserId: String) {
    val currentProfile = getProfileByUid(currentUserId) ?: return
    val friendId = getProfileByUsername(friend)?.uid ?: return

    if (!currentProfile.friendUids.contains(friendId)) {
      val updatedFriends = currentProfile.friendUids + friendId
      val updatedProfile = currentProfile.copy(friendUids = updatedFriends)
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

  // ---- BADGE GESTION PART ----

  override suspend fun addBadge(profile: Profile, badgeId: String) {
    val index = profiles.indexOfFirst { it.uid == profile.uid }
    if (index == -1) return

    val existing = profiles[index]
    if (badgeId in existing.badgeIds) return

    profiles[index] = existing.copy(badgeIds = existing.badgeIds + badgeId)
  }

  // ---- COUNTERS + BADGES (LOCAL) ----

  override suspend fun incrementCreatedTodo(uid: String): Int {
    val profile =
        getProfileByUid(uid) ?: throw NoSuchElementException("Profile not found for uid=$uid")
    val newCount = profile.createdTodoCount + 1
    val updated = profile.copy(createdTodoCount = newCount)
    updateProfile(updated)
    awardBadge(uid, BadgeType.TODOS_CREATED, newCount)
    return newCount
  }

  override suspend fun incrementCompletedTodo(uid: String): Int {
    val profile =
        getProfileByUid(uid) ?: throw NoSuchElementException("Profile not found for uid=$uid")
    val newCount = profile.completedTodoCount + 1
    val updated = profile.copy(completedTodoCount = newCount)
    updateProfile(updated)
    awardBadge(uid, BadgeType.TODOS_COMPLETED, newCount)
    return newCount
  }

  override suspend fun incrementCreatedEvent(uid: String): Int {
    val profile =
        getProfileByUid(uid) ?: throw NoSuchElementException("Profile not found for uid=$uid")
    val newCount = profile.createdEventCount + 1
    val updated = profile.copy(createdEventCount = newCount)
    updateProfile(updated)
    awardBadge(uid, BadgeType.EVENTS_CREATED, newCount)
    return newCount
  }

  override suspend fun incrementParticipatedEvent(uid: String): Int {
    val profile =
        getProfileByUid(uid) ?: throw NoSuchElementException("Profile not found for uid=$uid")
    val newCount = profile.participatedEventCount + 1
    val updated = profile.copy(participatedEventCount = newCount)
    updateProfile(updated)
    awardBadge(uid, BadgeType.EVENTS_PARTICIPATED, newCount)
    return newCount
  }

  override suspend fun incrementCompletedFocusSession(uid: String): Int {
    val profile =
        getProfileByUid(uid) ?: throw NoSuchElementException("Profile not found for uid=$uid")
    val newCount = profile.completedFocusSessionCount + 1
    val updated = profile.copy(completedFocusSessionCount = newCount)
    updateProfile(updated)
    awardBadge(uid, BadgeType.FOCUS_SESSIONS_COMPLETED, newCount)
    return newCount
  }

  override suspend fun incrementAddedFriend(uid: String): Int {
    val profile =
        getProfileByUid(uid) ?: throw NoSuchElementException("Profile not found for uid=$uid")
    val newCount = profile.addedFriendsCount + 1
    val updated = profile.copy(addedFriendsCount = newCount)
    updateProfile(updated)
    awardBadge(uid, BadgeType.FRIENDS_ADDED, newCount)
    return newCount
  }

  // ---------- helpers ----------

  private suspend fun awardBadge(uid: String, type: BadgeType, count: Int) {
    val rank = countToRank(count)
    if (rank == BadgeRank.BLANK) return

    // Fake badge id for tests/local usage: e.g. "TODOS_CREATED_GOLD"
    val badgeId = "${type.name}_${rank.name}"

    val profile = getProfileByUid(uid) ?: return
    addBadge(profile, badgeId)
  }

  private fun countToRank(count: Int): BadgeRank =
      when {
        count >= 30 -> BadgeRank.LEGEND
        count >= 20 -> BadgeRank.DIAMOND
        count >= 10 -> BadgeRank.GOLD
        count >= 5 -> BadgeRank.SILVER
        count >= 3 -> BadgeRank.BRONZE
        count >= 1 -> BadgeRank.STARTING
        else -> BadgeRank.BLANK
      }
}
