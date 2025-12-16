package com.android.gatherly.model.profile

import android.net.Uri
import com.android.gatherly.model.badge.Badge
import com.android.gatherly.model.badge.BadgeRank
import com.android.gatherly.model.badge.BadgeType
import com.android.gatherly.model.friends.Friends

/**
 * Simplified in-memory local implementation of [ProfileRepository].
 *
 * Used for local testing or offline mode. Only implements the methods actually needed in the
 * current app logic.
 *
 * @property profiles Mutable list that holds all profiles in memory.
 * @property shouldFailRegisterUsername boolean that represent failure when registering a username.
 * @throws NoSuchElementException if attempting to update or delete a profile that does not exist.
 */
class ProfileLocalRepository : ProfileRepository {

  private val profiles: MutableList<Profile> = mutableListOf()

  var shouldFailRegisterUsername = false

  // ---- ADD / UPDATE / DELETE PART ----

  override suspend fun addProfile(profile: Profile) {
    if (!profiles.any { it.uid == profile.uid }) {
      profiles += profile
    }
  }

  override suspend fun initProfileIfMissing(uid: String, defaultPhotoUrl: String): Boolean {
    if (profiles.none { it.uid == uid }) {
      addProfile(Profile(uid = uid, profilePicture = defaultPhotoUrl))
      return true
    }
    return false
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

  override suspend fun deleteUserProfile(uid: String) {
    profiles.removeIf { it.uid == uid }
  }

  // ---- CHECK PART ----

  override suspend fun isUidRegistered(uid: String): Boolean {
    return profiles.any { it.uid == uid }
  }

  override suspend fun isUsernameAvailable(username: String): Boolean {
    return profiles.none { it.username == username }
  }

  // ---- RETRIEVE PART ----

  override suspend fun getProfileByUid(uid: String): Profile? {
    return profiles.find { it.uid == uid }
  }

  override suspend fun getProfileByUsername(username: String): Profile? =
      profiles.find { it.username == username }

  // ---- SEARCH PART ----

  override suspend fun searchProfilesByNamePrefix(prefix: String): List<Profile> {
    return profiles.filter { it.name.contains(prefix, ignoreCase = true) }
  }

  override suspend fun searchProfilesByUsernamePrefix(prefix: String, limit: Int): List<Profile> =
      profiles.filter { it.username.startsWith(prefix, ignoreCase = true) }.take(limit)

  // ---- USERNAME GESTION PART ----

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

  // ---- PROFILE PICTURE GESTION PART ----

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

  override suspend fun addPendingSentFriendUid(currentUserId: String, targetUid: String) {
    val index = profiles.indexOfFirst { it.uid == currentUserId }
    if (index == -1) return

    val p = profiles[index]
    profiles[index] = p.copy(pendingSentFriendsUids = p.pendingSentFriendsUids + targetUid)
  }

  override suspend fun removePendingSentFriendUid(currentUserId: String, targetUid: String) {
    val index = profiles.indexOfFirst { it.uid == currentUserId }
    if (index == -1) return

    val p = profiles[index]
    profiles[index] = p.copy(pendingSentFriendsUids = p.pendingSentFriendsUids - targetUid)
  }

  // ---- STATUS GESTION PART ----

  override suspend fun updateStatus(uid: String, status: ProfileStatus, source: UserStatusSource) {
    val index = profiles.indexOfFirst { it.uid == uid }
    if (index != -1) {
      val existing = profiles[index]
      profiles[index] = existing.copy(status = status, userStatusSource = source)
    }
  }

  // ---- EVENT GESTION PART ----

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
    incrementBadge(currentUserId, BadgeType.EVENTS_PARTICIPATED)
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

  override suspend fun addBadge(uid: String, badgeId: String) {
    val profile =
        getProfileByUid(uid) ?: throw NoSuchElementException("Profile not found for uid=$uid")

    val currentBadgesSet = profile.badgeIds.toMutableSet()
    currentBadgesSet.add(badgeId)

    updateProfile(profile.copy(badgeIds = currentBadgesSet.toList()))
  }

  override suspend fun incrementBadge(uid: String, type: BadgeType): String? {
    val profile =
        getProfileByUid(uid) ?: throw NoSuchElementException("Profile not found for uid=$uid")

    val badgeCount = profile.badgeCount
    val key = type.name
    val currentValue: Long

    val updated =
        badgeCount.toMutableMap().apply {
          currentValue = this[key] ?: 0L
          this[key] = currentValue + 1
        }
    updateProfile(profile.copy(badgeCount = updated))
    val addBadge = awardBadge(type, currentValue + 1)

    return addBadge
  }

  // ---- FOCUS POINTS GESTION PART ----

  override suspend fun updateFocusPoints(uid: String, points: Double, addToLeaderboard: Boolean) {
    var profile = getProfileByUid(uid) ?: throw IllegalArgumentException("Profile doesn't exist")
    val leaderboard = if (addToLeaderboard) points else 0.0
    profile =
        profile.copy(
            focusPoints = profile.focusPoints + points,
            weeklyPoints = profile.weeklyPoints + leaderboard)
    updateProfile(profile)
  }

  // ---------- helpers ----------

  private fun awardBadge(type: BadgeType, count: Long): String? {
    val rank = countToRank(count)
    if (rank == BadgeRank.BLANK) return null

    val badge = Badge.entries.firstOrNull { it.type == type && it.rank == rank } ?: return null

    return badge.id
  }

  private fun countToRank(count: Long): BadgeRank =
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
