package com.android.gatherly.model.profile

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProfileRepositoryFirestore(private val db: FirebaseFirestore) : ProfileRepository {

  private val collection = db.collection("profiles")

  override suspend fun getProfileByUid(uid: String): Profile? {
    val doc = collection.document(uid).get().await()
    return if (doc.exists()){
      snapshotToProfile(doc)
    }else{
      null
    }
  }

  override suspend fun addProfile(profile: Profile) {
    collection.document(profile.uid).set(profileToMap(profile)).await()
  }

  override suspend fun updateProfile(profile: Profile) {
    val doc = collection.document(profile.uid).get().await()
    if(!doc.exists()) throw NoSuchElementException("Profile not found for uid=${profile.uid}")
    collection.document(profile.uid).set(profileToMap(profile)).await()
  }

  override suspend fun deleteProfile(uid: String) {
    collection.document(uid).delete().await()
  }

  override suspend fun isUidRegistered(uid: String): Boolean {
    val doc = collection.document(uid).get().await()
    return doc.exists()
  }

  override suspend fun findProfilesByUidSubstring(uidSubstring: String): List<Profile> {
    TODO("Not yet implemented")
  }

  private fun snapshotToProfile(doc: DocumentSnapshot): Profile? {
    val uid = doc.getString("uid") ?: return null
    val name = doc.getString("name") ?: ""
    val focusSessionIds = doc.get("focusSessions") as? List<String> ?: emptyList()
    val eventIds = doc.get("events") as? List<String> ?: emptyList()
    val groupIds = doc.get("groups") as? List<String> ?: emptyList()
    val friendUid = doc.get("friends") as? List<String> ?: emptyList()
    val school = doc.getString("school") ?: ""
    val schoolYear = doc.getString("schoolYear") ?: ""
    val birthday = doc.getTimestamp("birthday")
    val profilePicture = doc.getString("profilePicture")

    return Profile(
      uid = uid,
      name = name,
      focusSessionIds = focusSessionIds,
      eventIds = eventIds,
      groupIds = groupIds,
      friendUids = friendUid,
      school = school,
      schoolYear = schoolYear,
      birthday = birthday,
      profilePicture = profilePicture
    )
  }

  private fun profileToMap(profile: Profile): Map<String, Any?>{
    return mapOf(
      "uid" to profile.uid,
      "name" to profile.name,
      "focusSessionIds" to profile.focusSessionIds,
      "eventIds" to profile.eventIds,
      "groupIds" to profile.groupIds,
      "friendUids" to profile.friendUids,
      "school" to profile.school,
      "schoolYear" to profile.schoolYear,
      "birthday" to profile.birthday,
      "profilePicture" to profile.profilePicture
    )
  }
}
