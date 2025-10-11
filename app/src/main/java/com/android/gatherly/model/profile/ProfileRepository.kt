package com.android.gatherly.model.profile

interface ProfileRepository {
  suspend fun getProfileByUid(username: String): Profile?

  suspend fun addProfile(profile: Profile)

  suspend fun updateProfile(profile: Profile)

  suspend fun deleteProfile(username: String)

  suspend fun isUidRegistered(username: String): Boolean
}
