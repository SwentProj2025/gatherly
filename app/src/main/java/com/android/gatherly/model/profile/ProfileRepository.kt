package com.android.gatherly.model.profile

interface ProfileRepository {
  suspend fun getProfileByUid(uid: String): Profile?

  suspend fun addProfile(profile: Profile)

  suspend fun updateProfile(profile: Profile)

  suspend fun deleteProfile(uid: String)

  suspend fun isUidRegistered(uid: String): Boolean
}
