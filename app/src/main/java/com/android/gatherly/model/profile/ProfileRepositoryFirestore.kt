package com.android.gatherly.model.profile

import androidx.compose.animation.core.infiniteRepeatable
import com.firebase.ui.auth.data.model.User
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProfileRepositoryFirestore(private val db: FirebaseFirestore) : ProfileRepository {
//DONT FORGET TO CHANGE FIRESTORE RULES
  private val profilesCollection = db.collection("profiles")
  private val usernamesCollection = db.collection("usernames")

  override suspend fun getProfileByUid(uid: String): Profile? {
    val doc = profilesCollection.document(uid).get().await()
    return if (doc.exists()){
      snapshotToProfile(doc)
    }else{
      null
    }
  }

  override suspend fun addProfile(profile: Profile) {
    profilesCollection.document(profile.uid).set(profileToMap(profile)).await()
  }

  override suspend fun updateProfile(profile: Profile) {
    val doc = profilesCollection.document(profile.uid)
    if(!doc.get().await().exists()) throw NoSuchElementException("Profile not found for uid=${profile.uid}")
    doc.set(profileToMap(profile)).await()
  }

  override suspend fun deleteProfile(uid: String) {
    profilesCollection.document(uid).delete().await()
  }

  override suspend fun isUidRegistered(uid: String): Boolean {
    val doc = profilesCollection.document(uid).get().await()
    return doc.exists()
  }

  //This function is not scalable!!! it is just for M1
  override suspend fun searchProfilesByNamePrefix(prefix: String): List<Profile> {
    val trimmedPrefix = prefix.trim()
    if(trimmedPrefix.isEmpty()){
      return emptyList()
    }
    val snap = profilesCollection.get().await()
    return snap.documents
      .mapNotNull { snapshotToProfile(it) }
      .filter { profile ->
        profile.name.startsWith(trimmedPrefix, ignoreCase = true)
      }
  }


  override suspend fun isUsernameAvailable(username: String): Boolean {
    val username = Username.normalize(username)
    if (!(Username.isValid(username))){
      return false
    }
    return !usernamesCollection.document(username).get().await().exists()
  }

  //Used ai to convert initial code to the transaction one
  override suspend fun registerUsername(
    uid: String,
    username: String
  ): Boolean {
    val username = Username.normalize(username)
    if(!(Username.isValid(username))){
      return false
    }
    return db.runTransaction { tx ->
      val usernameDoc = usernamesCollection.document(username)
      if(tx.get(usernameDoc).exists()){
        return@runTransaction false
      }

      tx.set(usernameDoc, mapOf("uid" to uid))

      val profileDoc = profilesCollection.document(uid)
      if(tx.get(profileDoc).exists()){
        tx.update(profileDoc, "username", username)
      }else{
        tx.set(profileDoc, profileToMap(Profile(uid = uid, username = username)))
      }
      true
    }.await()
  }

  override suspend fun updateUsername(
    uid: String,
    oldUsername: String?,
    newUsername: String
  ): Boolean {
    val newUsername = Username.normalize(newUsername)
    if(!(Username.isValid(newUsername))){
      return false
    }

    return db.runTransaction { tx ->

      val newUsernameDoc = usernamesCollection.document(newUsername)
      if(tx.get(newUsernameDoc).exists()){
        return@runTransaction false
      }
      if(!oldUsername.isNullOrBlank()){
        tx.delete(usernamesCollection.document(Username.normalize(oldUsername)))
      }

      tx.set(newUsernameDoc, mapOf("uid" to uid))
      tx.update(profilesCollection.document(uid), "username", newUsername)
      true
    }.await()


  }

  override suspend fun getProfileByUsername(username: String): Profile? {
    val username = Username.normalize(username)
    val doc = usernamesCollection.document(username).get().await()
    val uid = doc.getString("uid") ?: return null
    return getProfileByUid(uid)
  }

  override suspend fun searchProfilesByUsernamePrefix(
    prefix: String,
    limit: Int
  ): List<Profile> {
    val prefix = Username.normalize(prefix)
    val snap = profilesCollection
      .orderBy("username")
      .startAt(prefix)
      .endAt(prefix + '\uf8ff')//Found this on internet, have no idea if it is good.
      .get()
      .await()
    return snap.documents.mapNotNull { snapshotToProfile(it) }
  }

  override suspend fun ensureProfileExists(uid: String, defaultPhotoUrl: String): Boolean {
    val doc = profilesCollection.document(uid)
    val snap = doc.get().await()
    if(snap.exists()){
      return false
    }
    val defaultProfile = Profile(
      uid = uid,
      name = "",
      username = "",
      profilePicture = defaultPhotoUrl
    )
    doc.set(profileToMap(defaultProfile)).await()
    return true
  }

  private fun snapshotToProfile(doc: DocumentSnapshot): Profile? {
    val uid = doc.getString("uid") ?: return null
    val name = doc.getString("name") ?: ""
    val focusSessionIds = doc.get("focusSessions") as? List<String> ?: emptyList()
    val eventIds = doc.get("events") as? List<String> ?: emptyList()
    val groupIds = doc.get("groups") as? List<String> ?: emptyList()
    val friendUids = doc.get("friends") as? List<String> ?: emptyList()
    val school = doc.getString("school") ?: ""
    val schoolYear = doc.getString("schoolYear") ?: ""
    val birthday = doc.getTimestamp("birthday")
    val profilePicture = doc.getString("profilePicture") ?: return null

    return Profile(
      uid = uid,
      name = name,
      focusSessionIds = focusSessionIds,
      eventIds = eventIds,
      groupIds = groupIds,
      friendUids = friendUids,
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
