package com.android.gatherly.model.profile

interface ProfileRepository {
  suspend fun getProfileByUid(uid: String): Profile?

  suspend fun addProfile(profile: Profile)

  suspend fun updateProfile(profile: Profile)

  suspend fun deleteProfile(uid: String)

  suspend fun isUidRegistered(uid: String): Boolean

  suspend fun searchProfilesByNamePrefix(prefix: String): List<Profile>

  suspend fun isUsernameAvailable(username: String): Boolean

  suspend fun registerUsername(uid: String, username: String):Boolean

  suspend fun updateUsername(uid: String, oldUsername: String?, newUsername: String): Boolean

  suspend fun getProfileByUsername(username: String): Profile?

  suspend fun searchProfilesByUsernamePrefix(prefix: String, limit: Int = 10): List<Profile>

  suspend fun ensureProfileExists(uid: String, defaultPhotoUrl: String): Boolean
}
