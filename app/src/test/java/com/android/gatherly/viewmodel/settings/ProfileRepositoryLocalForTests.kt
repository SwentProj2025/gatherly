package com.android.gatherly.viewmodel.settings

import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.ui.settings.SettingsViewModel

/**
 * An in-memory implementation of [ProfileRepository] for use in unit tests.
 *
 * This class stores profiles in a mutable map and allows adding, retrieving, updating, and removing
 * profiles without accessing any real database or network. Useful for testing [SettingsViewModel]
 * or other components that depend on [ProfileRepository].
 */
class ProfileRepositoryLocalForTests : ProfileRepository {
  private val profiles = mutableMapOf<String, Profile>()

  override suspend fun getProfileByUid(uid: String): Profile? {
    return profiles[uid]
  }

  override suspend fun addProfile(profile: Profile) {
    profiles[profile.uid] = profile
  }

  override suspend fun updateProfile(profile: Profile) {
    profiles[profile.uid] = profile
  }

  override suspend fun deleteProfile(uid: String) {
    profiles.remove(uid)
  }

  override suspend fun isUidRegistered(uid: String): Boolean {
    return profiles.containsKey(uid)
  }

  override suspend fun searchProfilesByNamePrefix(prefix: String): List<Profile> {
    return profiles.values.filter { it.uid.contains(prefix) }
  }

  override suspend fun isUsernameAvailable(username: String): Boolean = true

  override suspend fun registerUsername(uid: String, username: String): Boolean = true

  override suspend fun updateUsername(
      uid: String,
      oldUsername: String?,
      newUsername: String
  ): Boolean = true

  override suspend fun getProfileByUsername(username: String): Profile? = null

  override suspend fun searchProfilesByUsernamePrefix(prefix: String, limit: Int): List<Profile> =
      emptyList()

  override suspend fun initProfileIfMissing(uid: String, defaultPhotoUrl: String): Boolean = true
}
