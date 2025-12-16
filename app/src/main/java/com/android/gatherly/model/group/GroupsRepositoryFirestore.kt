package com.android.gatherly.model.group

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

// This class contains code adapted by an LLM (GitHub Copilot, Claude.ai) from the CS-311 bootcamp.

/**
 * Firebase Firestore implementation of [GroupsRepository].
 *
 * This implementation enforces security rules, requiring admin privileges for most modification
 * operations. Uses Firebase Authentication to identify the current user.
 *
 * @property db The Firestore database instance.
 */
class GroupsRepositoryFirestore(private val db: FirebaseFirestore) : GroupsRepository {

  /** Firestore collection for groups. */
  private val collection = db.collection("groups")

  /**
   * Returns the user ID of the currently signed-in user, or throws an exception if no user is
   * signed in.
   */
  private fun currentUserId(): String {
    return Firebase.auth.currentUser?.uid ?: throw IllegalStateException("No signed in user")
  }

  override fun getNewId(): String {
    return collection.document().id
  }

  override suspend fun getAllGroups(): List<Group> {
    val snap = collection.get().await() // No filtering - get ALL groups
    return snap.documents.mapNotNull { doc -> snapshotToGroup(doc) }
  }

  override suspend fun getUserGroups(): List<Group> {
    val userId = currentUserId()
    val snap = collection.whereArrayContains("memberIds", userId).get().await()
    return snap.documents.mapNotNull { doc -> snapshotToGroup(doc) }
  }

  override suspend fun getGroup(groupId: String): Group {
    val doc = collection.document(groupId).get().await()
    return snapshotToGroup(doc) ?: throw NoSuchElementException("Group with id=$groupId not found")
  }

  override suspend fun addGroup(group: Group) {
    val userId = currentUserId()
    // Ensure creator is in both memberIds and adminIds
    val ownedGroup =
        group.copy(
            creatorId = userId,
            memberIds =
                if (userId !in group.memberIds) group.memberIds + userId else group.memberIds,
            adminIds = if (userId !in group.adminIds) group.adminIds + userId else group.adminIds)
    collection.document(ownedGroup.gid).set(groupToMap(ownedGroup)).await()
  }

  override suspend fun editGroup(groupId: String, newValue: Group) {
    val existing = getGroup(groupId)
    if (currentUserId() !in existing.adminIds) {
      throw SecurityException("Only admins can edit this group")
    }
    // Preserve the original creatorId
    val updated = newValue.copy(creatorId = existing.creatorId)
    collection.document(groupId).set(groupToMap(updated)).await()
  }

  override suspend fun deleteGroup(groupId: String) {
    val existing = getGroup(groupId)
    if (currentUserId() != existing.creatorId) {
      throw SecurityException("Only admins can delete this group")
    }
    collection.document(groupId).delete().await()
  }

  override suspend fun addMember(groupId: String, userId: String) {
    val existing = getGroup(groupId)
    if (currentUserId() !in existing.adminIds) {
      throw SecurityException("Only admins can add members to this group")
    }
    val groupRef = collection.document(groupId)
    groupRef.update("memberIds", FieldValue.arrayUnion(userId)).await()
  }

  override suspend fun removeMember(groupId: String, userId: String) {
    val existing = getGroup(groupId)
    val currentUser = currentUserId()

    // Prevent creator from being removed from the group
    check(userId == existing.creatorId) { "User cannot be removed from a group he created" }

    // Allow self-removal (leaving the group) OR admin removing someone
    if (currentUser != userId && currentUser !in existing.adminIds) {
      throw SecurityException("Only admins can remove other members from this group")
    }

    // Prevent removing the last admin
    check(userId !in existing.adminIds || existing.adminIds.size > 1) {
      "Cannot remove the last admin from the group"
    }

    val groupRef = collection.document(groupId)
    // Single update with both fields
    groupRef
        .update(
            mapOf(
                "memberIds" to FieldValue.arrayRemove(userId),
                "adminIds" to FieldValue.arrayRemove(userId)))
        .await()
  }

  override suspend fun addAdmin(groupId: String, userId: String) {
    val existing = getGroup(groupId)
    if (currentUserId() !in existing.adminIds) {
      throw SecurityException("Only admins can promote members to admin")
    }
    // Verify user is a member before making them admin
    check(userId in existing.memberIds) { "User must be a member before becoming an admin" }

    collection.document(groupId).update("adminIds", FieldValue.arrayUnion(userId)).await()
  }

  override suspend fun removeAdmin(groupId: String, userId: String) {
    val existing = getGroup(groupId)
    if (currentUserId() !in existing.adminIds) {
      throw SecurityException("Only admins can demote other admins")
    }
    // Prevent removing the last admin
    check(existing.adminIds.size > 1) { "Cannot remove the last admin from the group" }

    collection.document(groupId).update("adminIds", FieldValue.arrayRemove(userId)).await()
  }

  override suspend fun getGroupByName(groupName: String): Group {
    val snap = collection.whereEqualTo("name", groupName).get().await()

    val doc = snap.documents.firstOrNull()

    return snapshotToGroup(
        doc ?: throw NoSuchElementException("Group with name=$groupName not found"))
        ?: throw NoSuchElementException("Group with name=$groupName found but invalid data")
  }

  /**
   * Converts a Firestore [DocumentSnapshot] into a [Group] object.
   *
   * @param doc The Firestore document representing a [Group].
   * @return The constructed [Group], or `null` if required fields are missing.
   */
  private fun snapshotToGroup(doc: DocumentSnapshot): Group? {
    val gid = doc.getString("gid") ?: return null
    val creatorId = doc.getString("creatorId") ?: return null
    val name = doc.getString("name") ?: return null
    val description = doc.getString("description")
    val memberIds = doc["memberIds"] as? List<*> ?: return null
    val adminIds = doc["adminIds"] as? List<*> ?: return null

    // Ensure all memberIds and adminIds are strings
    val memberIdsStr = memberIds.mapNotNull { it as? String }
    val adminIdsStr = adminIds.mapNotNull { it as? String }

    return Group(
        gid = gid,
        creatorId = creatorId,
        name = name,
        description = description,
        memberIds = memberIdsStr,
        adminIds = adminIdsStr)
  }

  /**
   * Converts a [Group] into a Firestore-compatible map.
   *
   * Used when adding or editing a group in Firestore.
   *
   * @param group The [Group] to serialize.
   * @return A map of field names to values compatible with Firestore.
   */
  private fun groupToMap(group: Group): Map<String, Any?> {
    return mapOf(
        "gid" to group.gid,
        "creatorId" to group.creatorId,
        "name" to group.name,
        "description" to group.description,
        "memberIds" to group.memberIds,
        "adminIds" to group.adminIds)
  }
}
