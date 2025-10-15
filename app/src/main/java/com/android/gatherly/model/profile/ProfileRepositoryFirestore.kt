package com.android.gatherly.model.profile

import com.google.firebase.firestore.FirebaseFirestore

class ProfileRepositoryFirestore(private val db: FirebaseFirestore) : ProfileRepository {

  override suspend fun getProfileByUid(uid: String): Profile? {
    TODO("Not yet implemented")
  }

  override suspend fun addProfile(profile: Profile) {
    TODO("Not yet implemented")
  }

  override suspend fun updateProfile(profile: Profile) {
    TODO("Not yet implemented")
  }

  override suspend fun deleteProfile(uid: String) {
    TODO("Not yet implemented")
  }

  override suspend fun isUidRegistered(uid: String): Boolean {
    TODO("Not yet implemented")
  }

  override suspend fun findProfilesByUidSubstring(uidSubstring: String): List<Profile> {
    TODO("Not yet implemented")
  }
}
