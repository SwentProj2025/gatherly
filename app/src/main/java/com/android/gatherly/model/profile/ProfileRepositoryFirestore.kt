package com.android.gatherly.model.profile

import android.net.Uri
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.tasks.await
import java.io.File

/**
 * Firestore-backed implementation of [ProfileRepository].
 *
 * This class manages user [Profile]s and usernames stored in:
 * - /profiles/{uid} : main [Profile] documents
 * - /usernames/{username} : mapping from usernames to UIDs
 */
class ProfileRepositoryFirestore(
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ProfileRepository {

  private val profilesCollection = db.collection("profiles")
  private val usernamesCollection = db.collection("usernames")

  /**
   * Retrieves the [Profile] document associated with uid.
   *
   * @param uid The unique user identifier.
   * @return The corresponding [Profile], or null if none exists.
   */
  override suspend fun getProfileByUid(uid: String): Profile? {
    val doc = profilesCollection.document(uid).get().await()
    return if (doc.exists()) {
      snapshotToProfile(doc)
    } else {
      null
    }
  }

  /**
   * Updates an existing [Profile] document.
   *
   * @param profile The [Profile] object containing updated data.
   * @throws NoSuchElementException If no existing [Profile] is found for the UID.
   */
  override suspend fun updateProfile(profile: Profile) {
    val doc = profilesCollection.document(profile.uid)
    if (!doc.get().await().exists())
        throw NoSuchElementException("Profile not found for uid=${profile.uid}")
    doc.set(profileToMap(profile)).await()
  }

  /**
   * Deletes a user's [Profile] document.
   *
   * @param uid The user ID of the [Profile] to delete.
   */
  override suspend fun deleteProfile(uid: String) {
    profilesCollection.document(uid).delete().await()
  }

  /**
   * Checks whether a [Profile] document exists for the given uid.
   *
   * @param uid The UID to check.
   * @return `true` if the [Profile] exists, `false` otherwise.
   */
  override suspend fun isUidRegistered(uid: String): Boolean {
    val doc = profilesCollection.document(uid).get().await()
    return doc.exists()
  }

  /**
   * Performs a prefix-based search on [Profile] names. Non-scalable, temporary use before
   * implementing username.
   *
   * @param prefix The case-insensitive name prefix to search for.
   * @return A list of [Profile]s whose names start with [prefix].
   */
  override suspend fun searchProfilesByNamePrefix(prefix: String): List<Profile> {
    val trimmedPrefix = prefix.trim()
    if (trimmedPrefix.isEmpty()) {
      return emptyList()
    }
    val snap = profilesCollection.get().await()
    return snap.documents
        .mapNotNull { snapshotToProfile(it) }
        .filter { profile -> profile.name.startsWith(trimmedPrefix, ignoreCase = true) }
  }

  /**
   * Checks if a username is valid and available for registration.
   *
   * @param username The username to verify.
   * @return true if valid and available, false otherwise.
   */
  override suspend fun isUsernameAvailable(username: String): Boolean {
    val username = Username.normalize(username)
    if (!(Username.isValid(username))) {
      return false
    }
    return !usernamesCollection.document(username).get().await().exists()
  }

  /**
   * Registers a new username for the given uid.
   *
   * @param uid The user ID to associate.
   * @param username The username to register.
   * @return true if registration succeeded, false if invalid or taken.
   */
  override suspend fun registerUsername(uid: String, username: String): Boolean {
    val normalized = Username.normalize(username)
    if (!Username.isValid(normalized)) return false

    return db.runTransaction { tx ->
          val usernameDoc = usernamesCollection.document(normalized)
          val profileDoc = profilesCollection.document(uid)

          // Read both docs first
          val usernameSnap = tx.get(usernameDoc)
          val profileSnap = tx.get(profileDoc)

          // Validate both before writing
          if (usernameSnap.exists()) return@runTransaction false
          if (!profileSnap.exists())
              throw IllegalStateException("Cannot register username before profile creation")

          // Perform writes only after reads
          tx.set(usernameDoc, mapOf("uid" to uid))
          tx.update(profileDoc, "username", normalized)
          true
        }
        .await()
  }

  /**
   * Updates a user's username transactionally.
   *
   * @param uid The user ID.
   * @param oldUsername The previous username, if any.
   * @param newUsername The new username to assign.
   * @return true if update succeeded, false otherwise.
   */
  override suspend fun updateUsername(
      uid: String,
      oldUsername: String?,
      newUsername: String
  ): Boolean {
    val newUsername = Username.normalize(newUsername)
    if (!(Username.isValid(newUsername))) {
      return false
    }

    return db.runTransaction { tx ->
          val newUsernameDoc = usernamesCollection.document(newUsername)
          if (tx.get(newUsernameDoc).exists()) {
            return@runTransaction false
          }
          if (!oldUsername.isNullOrBlank()) {
            tx.delete(usernamesCollection.document(Username.normalize(oldUsername)))
          }

          tx.set(newUsernameDoc, mapOf("uid" to uid))
          tx.update(profilesCollection.document(uid), "username", newUsername)
          true
        }
        .await()
  }

  override suspend fun updateProfilePic(uid: String, uri: Uri): String {
      var tempFile: File? = null
      try{
          val storageRef = storage.reference.child("profile_pictures/$uid")
          val uploadUri =
              if (uri.scheme == "content") {
                  val context = Firebase.app.applicationContext
                  tempFile = kotlin.io.path.createTempFile("profile_$uid").toFile()
                  val inputStream = context.contentResolver.openInputStream(uri)
                  inputStream?.use { tempFile.outputStream().use { output -> inputStream.copyTo(output) } }
                  Uri.fromFile(tempFile)
              } else {
                  uri
              }
          storageRef.putFile(uploadUri).await()
          val downloadUrl = storageRef.downloadUrl.await().toString()
          val doc = profilesCollection.document(uid)
          doc.update("profilePicture", downloadUrl).await()
          return downloadUrl
      }finally{
          tempFile?.delete()
      }
  }

  /**
   * Retrieves a [Profile] by its username.
   *
   * @param username The username to search for.
   * @return The [Profile] if found, or null if not found.
   */
  override suspend fun getProfileByUsername(username: String): Profile? {
    val username = Username.normalize(username)
    val doc = usernamesCollection.document(username).get().await()
    val uid = doc.getString("uid") ?: return null
    return getProfileByUid(uid)
  }

  /**
   * Searches for [Profile]s whose usernames start with prefix.
   *
   * @param prefix The case-insensitive username prefix.
   * @param limit The maximum number of results to return.
   * @return A list of matching [Profile] objects.
   */
  override suspend fun searchProfilesByUsernamePrefix(prefix: String, limit: Int): List<Profile> {
    val prefix = Username.normalize(prefix)
    val snap =
        profilesCollection
            .orderBy("username")
            .startAt(prefix)
            .endAt(prefix + '\uf8ff')
            .get()
            .await()
    return snap.documents.mapNotNull { snapshotToProfile(it) }
  }

  /**
   * Ensures a [Profile] document exists for the given [uid]. Creates one with a defaultPhotoUrl and
   * an empty username if missing. This expects that if it was missing the user is then prompted to
   * update the mandatory fields. This is the function that will init a [Profile] if it is the first
   * time the user signs in.
   *
   * @param uid The user ID.
   * @param defaultPhotoUrl The default photo URL to assign if a [Profile] is created.
   * @return true if a new [Profile] was created, false if it already existed.
   */
  override suspend fun initProfileIfMissing(uid: String, defaultPhotoUrl: String): Boolean {
    val doc = profilesCollection.document(uid)
    val snap = doc.get().await()
    if (snap.exists()) {
      return false
    }
    val defaultProfile =
        Profile(uid = uid, name = "", username = "", profilePicture = defaultPhotoUrl)
    doc.set(profileToMap(defaultProfile)).await()
    return true
  }

  /** Creates a profile. This is to be used only for testing purpose. */
  override suspend fun addProfile(profile: Profile) {
    // Empty because this function is never used, it is here for test purposes only.
  }

  /**
   * Converts a Firestore [DocumentSnapshot] into a [Profile].
   *
   * @param doc The snapshot to convert.
   * @return The [Profile], or null if required fields are missing.
   */
  private fun snapshotToProfile(doc: DocumentSnapshot): Profile? {
    val uid = doc.getString("uid") ?: return null
    val name = doc.getString("name") ?: ""
    val username = doc.getString("username") ?: ""
    val focusSessionIds = doc.get("focusSessions") as? List<String> ?: emptyList()
    val eventIds = doc.get("events") as? List<String> ?: emptyList()
    val groupIds = doc.get("groups") as? List<String> ?: emptyList()
    val friendUids = doc.get("friendUids") as? List<String> ?: emptyList()
    val school = doc.getString("school") ?: ""
    val schoolYear = doc.getString("schoolYear") ?: ""
    val birthday = doc.getTimestamp("birthday")
    val profilePicture = doc.getString("profilePicture") ?: return null

    return Profile(
        uid = uid,
        name = name,
        username = username,
        focusSessionIds = focusSessionIds,
        eventIds = eventIds,
        groupIds = groupIds,
        friendUids = friendUids,
        school = school,
        schoolYear = schoolYear,
        birthday = birthday,
        profilePicture = profilePicture)
  }

  /**
   * Converts a [Profile] to a Firestore-compatible map.
   *
   * @param profile The [Profile] to convert.
   * @return A [Map] representation of the [Profile] suitable for Firestore writes.
   */
  private fun profileToMap(profile: Profile): Map<String, Any?> {
    return mapOf(
        "uid" to profile.uid,
        "name" to profile.name,
        "username" to profile.username,
        "focusSessionIds" to profile.focusSessionIds,
        "eventIds" to profile.eventIds,
        "groupIds" to profile.groupIds,
        "friendUids" to profile.friendUids,
        "school" to profile.school,
        "schoolYear" to profile.schoolYear,
        "birthday" to profile.birthday,
        "profilePicture" to profile.profilePicture)
  }

  override suspend fun getListNoFriends(currentUserId: String): List<String> {
    val currentProfile = getProfileByUid(currentUserId) ?: return emptyList()
    val friendUids = currentProfile.friendUids

    val snap = profilesCollection.get().await()
    val allProfiles = snap.documents.mapNotNull { snapshotToProfile(it) }

    return allProfiles
        .filter { it.uid != currentUserId && it.uid !in friendUids }
        .mapNotNull { it.username }
        .filter { it.isNotBlank() }
  }

  override suspend fun addFriend(friend: String, currentUserId: String) {
    val docRef = profilesCollection.document(currentUserId)
    val friendId = getProfileByUsername(friend)?.uid
    docRef.update("friendUids", FieldValue.arrayUnion(friendId)).await()
  }

  override suspend fun deleteFriend(friend: String, currentUserId: String) {
    val docRef = profilesCollection.document(currentUserId)
    val friendId = getProfileByUsername(friend)?.uid
    docRef.update("friendUids", FieldValue.arrayRemove(friendId)).await()
  }
}
