package com.android.gatherly.viewmodel.settings

import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileRepository

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

  override suspend fun findProfilesByUidSubstring(uidSubstring: String): List<Profile> {
    return profiles.values.filter { it.uid.contains(uidSubstring) }
  }
}
