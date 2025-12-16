package com.android.gatherly.model.profile

import android.net.Uri
import android.util.Log
import com.android.gatherly.model.badge.Badge
import com.android.gatherly.model.badge.BadgeRank
import com.android.gatherly.model.badge.BadgeType
import com.android.gatherly.model.friends.Friends
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.File
import kotlinx.coroutines.tasks.await

/**
 * Firestore-backed implementation of [ProfileRepository].
 *
 * This class manages user [Profile]s and usernames stored in:
 * - /profiles/{uid} : main [Profile] documents
 * - /usernames/{username} : mapping from usernames to UIDs
 *
 * @property db The Firestore database instance.
 * @property storage The Firebase Storage instance for profile pictures.
 * @property profilesCollection Reference to the "profiles" collection.
 * @property usernamesCollection Reference to the "usernames" collection.
 * @throws SecurityException for operations requiring creator privileges.
 * @throws NoSuchElementException when requested profiles are not found.
 * @throws IllegalStateException when no user is currently signed in.
 */
class ProfileRepositoryFirestore(
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ProfileRepository {

  /** Firestore collection for profiles. */
  private val profilesCollection = db.collection("profiles")

  /** Firestore collection for usernames. */
  private val usernamesCollection = db.collection("usernames")

  // --- ADD / UPDATE / DELETE PART ---

  override suspend fun addProfile(profile: Profile) {
    profilesCollection.add(profile)
  }

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

  override suspend fun updateProfile(profile: Profile) {
    val doc = profilesCollection.document(profile.uid)
    if (!doc.get().await().exists())
        throw NoSuchElementException("Profile not found for uid=${profile.uid}")
    doc.set(profileToMap(profile)).await()
  }

  override suspend fun deleteProfile(uid: String) {
    profilesCollection.document(uid).delete().await()
  }

  override suspend fun deleteUserProfile(uid: String) {
    val profile = getProfileByUid(uid) ?: return

    // Delete the user from all groups it is a part of:
    // All groups the user belongs to:
    val groupSnapshot = db.collection("groups").whereArrayContains("memberIds", uid).get().await()

    // Loop through those groups:
    for (doc in groupSnapshot.documents) {
      val data = doc.data ?: continue
      val members =
          when (val value = data["memberIds"]) {
            is List<*> -> {
              require(value.all { it is String }) { "memberIds must be a List<String>" }
              value.map { it as String }.toMutableList()
            }
            else -> mutableListOf()
          }
      val admins =
          when (val value = data["adminIds"]) {
            is List<*> -> {
              require(value.all { it is String }) { "adminIds must be a List<String>" }
              value.map { it as String }.toMutableList()
            }
            else -> mutableListOf()
          }
      val newMembers = members.filter { it != uid }
      val newAdmins = admins.filter { it != uid }

      db.collection("groups")
          .document(doc.id)
          .update(mapOf("memberIds" to newMembers, "adminIds" to newAdmins))
          .await()
    }

    // Delete todos of the user:
    val todosSnapshot = db.collection("users").document(uid).collection("todos").get().await()

    for (doc in todosSnapshot.documents) {
      doc.reference.delete().await()
    }

    // Delete events created by the user:
    val eventsSnapshot = db.collection("events").whereEqualTo("creatorId", uid).get().await()

    for (doc in eventsSnapshot.documents) {
      doc.reference.delete().await()
    }

    // Delete focus sessions of the user:
    val focusSessionsSnapshot =
        db.collection("focusSessions").whereEqualTo("creatorId", uid).get().await()

    for (doc in focusSessionsSnapshot.documents) {
      doc.reference.delete().await()
    }

    // Delete username and profile:
    db.runBatch { batch ->
          if (profile.username.isNotBlank()) {
            batch.delete(usernamesCollection.document(profile.username))
          }
          batch.delete(profilesCollection.document(uid))
        }
        .await()

    // Delete profile picture:
    try {
      val storageRef = Firebase.storage.reference.child("profile_pictures/$uid")
      storageRef.delete().await()
    } catch (e: Exception) {
      Log.d("ProfileRepository", "No profile picture to delete: ${e.message}")
    }
  }

  // --- CHECK PART ---

  override suspend fun isUsernameAvailable(username: String): Boolean {
    val username = Username.normalize(username)
    if (!(Username.isValid(username))) {
      return false
    }
    return !usernamesCollection.document(username).get().await().exists()
  }

  override suspend fun isUidRegistered(uid: String): Boolean {
    val doc = profilesCollection.document(uid).get().await()
    return doc.exists()
  }

  // --- RETRIEVE PART ---

  override suspend fun getProfileByUid(uid: String): Profile? {
    val doc = profilesCollection.document(uid).get().await()
    return if (doc.exists()) {
      snapshotToProfile(doc)
    } else {
      null
    }
  }

  override suspend fun getProfileByUsername(username: String): Profile? {
    val username = Username.normalize(username)
    val doc = usernamesCollection.document(username).get().await()
    val uid = doc.getString("uid") ?: return null
    return getProfileByUid(uid)
  }

  // --- SEARCH PART ---
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

  // --- USERNAME PART ---
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

  // --- PROFILE PICTURE PART ---
  override suspend fun updateProfilePic(uid: String, uri: Uri): String {
    var tempFile: File? = null
    try {
      val storageRef = storage.reference.child("profile_pictures/$uid")
      val uploadUri =
          if (uri.scheme == "content") {
            val context = Firebase.app.applicationContext
            tempFile = kotlin.io.path.createTempFile("profile_$uid").toFile()
            val inputStream = context.contentResolver.openInputStream(uri)
            inputStream?.use {
              tempFile.outputStream().use { output -> inputStream.copyTo(output) }
            }
            Uri.fromFile(tempFile)
          } else {
            uri
          }
      storageRef.putFile(uploadUri).await()
      val downloadUrl = storageRef.downloadUrl.await().toString()
      val doc = profilesCollection.document(uid)
      doc.update("profilePicture", downloadUrl).await()
      return downloadUrl
    } finally {
      if (tempFile != null && !tempFile.delete()) {
        Log.w("ProfileRepository", "Temporary file ${tempFile.path} could not be deleted.")
      }
    }
  }

  // -- FRIENDS GESTION PART --
  override suspend fun getFriendsAndNonFriendsUsernames(currentUserId: String): Friends {
    val currentProfile =
        getProfileByUid(currentUserId)
            ?: throw NoSuchElementException("Profile not found for uid=$currentUserId")

    val friendUsernames =
        currentProfile.friendUids.mapNotNull { friendUid -> getProfileByUid(friendUid)?.username }

    val nonFriendUsernames = getListNoFriends(currentUserId)

    return Friends(friendUsernames = friendUsernames, nonFriendUsernames = nonFriendUsernames)
  }

  override suspend fun getListNoFriends(currentUserId: String): List<String> {
    val currentProfile = getProfileByUid(currentUserId) ?: return emptyList()
    val friendUids = currentProfile.friendUids

    val snap = profilesCollection.get().await()
    val allProfiles = snap.documents.mapNotNull { snapshotToProfile(it) }

    return allProfiles
        .filter { it.uid != currentUserId && it.uid !in friendUids }
        .map { it.username }
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

  override suspend fun addPendingSentFriendUid(currentUserId: String, targetUid: String) {
    profilesCollection
        .document(currentUserId)
        .update("pendingSentFriendsUids", FieldValue.arrayUnion(targetUid))
        .await()
  }

  override suspend fun removePendingSentFriendUid(currentUserId: String, targetUid: String) {
    profilesCollection
        .document(currentUserId)
        .update("pendingSentFriendsUids", FieldValue.arrayRemove(targetUid))
        .await()
  }

  // -- STATUS GESTION PART --
  override suspend fun updateStatus(uid: String, status: ProfileStatus, source: UserStatusSource) {
    profilesCollection
        .document(uid)
        .update(mapOf("status" to status.value, "userStatusSource" to source.value))
        .await()
  }

  // -- EVENTS GESTION PART --

  override suspend fun createEvent(eventId: String, currentUserId: String) {
    val docRef = profilesCollection.document(currentUserId)
    docRef.update("ownedEventIds", FieldValue.arrayUnion(eventId)).await()
  }

  override suspend fun deleteEvent(eventId: String, currentUserId: String) {
    val docRef = profilesCollection.document(currentUserId)
    docRef.update("ownedEventIds", FieldValue.arrayRemove(eventId)).await()
  }

  override suspend fun participateEvent(eventId: String, currentUserId: String) {
    val docRef = profilesCollection.document(currentUserId)
    docRef.update("participatingEventIds", FieldValue.arrayUnion(eventId)).await()
  }

  override suspend fun allParticipateEvent(eventId: String, participants: List<String>) {
    participants.forEach { participant -> participateEvent(eventId, participant) }
  }

  override suspend fun unregisterEvent(eventId: String, currentUserId: String) {
    val docRef = profilesCollection.document(currentUserId)
    docRef.update("participatingEventIds", FieldValue.arrayRemove(eventId)).await()
  }

  override suspend fun allUnregisterEvent(eventId: String, participants: List<String>) {
    participants.forEach { participant -> unregisterEvent(eventId, participant) }
  }

  // -- BADGES GESTION PART --

  override suspend fun addBadge(uid: String, badgeId: String) {
    val docRef = profilesCollection.document(uid)
    docRef.update("badgeIds", FieldValue.arrayUnion(badgeId)).await()
  }

  override suspend fun incrementBadge(uid: String, type: BadgeType): String? {
    val docRef = profilesCollection.document(uid)
    val fieldName = "badgeCount.${type.name}"

    docRef.update(fieldName, FieldValue.increment(1)).await()

    val addBadge = awardBadgeFor(type, docRef.get().await().getLong(fieldName) ?: 0L)

    return addBadge
  }

  // -- FOCUS POINTS PART --
  override suspend fun updateFocusPoints(uid: String, points: Double, addToLeaderboard: Boolean) {
    var profile = getProfileByUid(uid) ?: throw IllegalArgumentException("Profile doesn't exist")
    val leaderboard = if (addToLeaderboard) points else 0.0
    profile =
        profile.copy(
            focusPoints = profile.focusPoints + points,
            weeklyPoints = profile.weeklyPoints + leaderboard)
    updateProfile(profile)
  }

  // -- PRIVATE UTILS PART --

  /** Shared thresholds from count to BadgeRank. */
  private fun countToRank(count: Long): BadgeRank =
      when {
        count == 30L -> BadgeRank.LEGEND
        count >= 20 -> BadgeRank.DIAMOND
        count >= 10 -> BadgeRank.GOLD
        count >= 5 -> BadgeRank.SILVER
        count >= 3 -> BadgeRank.BRONZE
        count >= 1 -> BadgeRank.STARTING
        else -> BadgeRank.BLANK
      }

  /**
   * Converts a count to a rank, then finds the matching badge in Badges enum
   *
   * It returns null if no badge should be awarded, or the id of the badge to award
   */
  private fun awardBadgeFor(type: BadgeType, count: Long): String? {
    val rank = countToRank(count)
    if (rank == BadgeRank.BLANK) return null

    // Find the matching Badge enum entry for this type + rank
    val badge = Badge.entries.firstOrNull { it.type == type && it.rank == rank } ?: return null

    return badge.id
  }

  // -- MAPPING PART --

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
    val focusSessionIds =
        (doc["focusSessionIds"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    val eventIds =
        (doc["participatingEventIds"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    val eventOwnerIds =
        (doc["ownedEventIds"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    val groupIds = (doc["groups"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    val friendUids = (doc["friendUids"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    val pendingSentFriendsUids =
        (doc["pendingSentFriendsUids"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    val school = doc.getString("school") ?: ""
    val schoolYear = doc.getString("schoolYear") ?: ""
    val birthday = doc.getTimestamp("birthday")
    val profilePicture = doc.getString("profilePicture") ?: return null
    val status = ProfileStatus.fromString(doc.getString("status"))
    val userStatusSource = UserStatusSource.fromString(doc.getString("userStatusSource"))
    val badgeIds = (doc["badgeIds"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    val badgeCount =
        (doc["badgeCount"] as? Map<*, *>)
            ?.mapNotNull { (key, value) ->
              (key as? String)?.let { k -> (value as? Long)?.let { v -> k to v } }
            }
            ?.toMap() ?: emptyMap()
    val focusPoints: Double = doc.getDouble("focusPoints") ?: 0.0
    val bio = doc.getString("bio") ?: ""
    val weeklyPoints: Double = doc.getDouble("weeklyPoints") ?: 0.0

    return Profile(
        uid = uid,
        name = name,
        username = username,
        focusSessionIds = focusSessionIds,
        participatingEventIds = eventIds,
        ownedEventIds = eventOwnerIds,
        groupIds = groupIds,
        friendUids = friendUids,
        pendingSentFriendsUids = pendingSentFriendsUids,
        school = school,
        schoolYear = schoolYear,
        birthday = birthday,
        profilePicture = profilePicture,
        status = status,
        userStatusSource = userStatusSource,
        badgeIds = badgeIds,
        badgeCount = badgeCount,
        focusPoints = focusPoints,
        weeklyPoints = weeklyPoints,
        bio = bio,
    )
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
        "participatingEventIds" to profile.participatingEventIds,
        "ownedEventIds" to profile.ownedEventIds,
        "groupIds" to profile.groupIds,
        "friendUids" to profile.friendUids,
        "pendingSentFriendsUids" to profile.pendingSentFriendsUids,
        "school" to profile.school,
        "schoolYear" to profile.schoolYear,
        "birthday" to profile.birthday,
        "profilePicture" to profile.profilePicture,
        "status" to profile.status.value,
        "userStatusSource" to profile.userStatusSource.value,
        "badgeIds" to profile.badgeIds,
        "badgeCount" to profile.badgeCount,
        "focusPoints" to profile.focusPoints,
        "weeklyPoints" to profile.weeklyPoints,
        "bio" to profile.bio,
    )
  }
}
