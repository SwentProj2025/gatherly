package com.android.gatherly.model.profile

import android.net.Uri

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
    val friendId = getProfileByUsername(friend)?.uid
    if (!currentProfile.friendUids.contains(friendId)) {
      val updatedFriends = currentProfile.friendUids + friendId
      val updatedProfile = currentProfile.copy(friendUids = updatedFriends as List<String>)
      updateProfile(updatedProfile)
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
}
