package com.android.gatherly.model.profile

class ProfileRepositoryFirestore : ProfileRepository {

  override suspend fun getProfileByUid(username: String): Profile? {
    TODO("Not yet implemented")
  }

  override suspend fun addProfile(profile: Profile) {
    TODO("Not yet implemented")
  }

  override suspend fun updateProfile(profile: Profile) {
    TODO("Not yet implemented")
  }

  override suspend fun deleteProfile(username: String) {
    TODO("Not yet implemented")
  }

  override suspend fun isUidRegistered(username: String): Boolean {
    TODO("Not yet implemented")
  }
}
