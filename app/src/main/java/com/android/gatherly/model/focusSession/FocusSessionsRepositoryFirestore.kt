package com.android.gatherly.model.focusSession

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.tasks.await

// This class contains code adapted from the CS-311 bootcamp.

/**
 * Firestore-based implementation of [FocusSessionsRepository].
 *
 * This repository stores each user's ToDo items under: /focusSessions/{focusSessionsId}
 *
 * All methods are asynchronous and must be called from a coroutine scope.
 *
 * @param db A [FirebaseFirestore] instance used for database operations.
 */
class FocusSessionsRepositoryFirestore(private val db: FirebaseFirestore) :
    FocusSessionsRepository {

  /** Firestore collection for focus sessions. */
  private val collection = db.collection("focusSessions")

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

  override suspend fun getAllFocusSessions(): List<FocusSession> {
    val snap = collection.get().await()
    return snap.documents.mapNotNull { doc -> snapshotToFocusSession(doc) }
  }

  override suspend fun getUserFocusSessions(): List<FocusSession> {
    val userId = currentUserId()
    val snap = collection.whereEqualTo("creatorId", userId).get().await()
    return snap.documents.mapNotNull { doc -> snapshotToFocusSession(doc) }
  }

  override suspend fun getFocusSession(focusSessionId: String): FocusSession {
    val doc = collection.document(focusSessionId).get().await()
    return snapshotToFocusSession(doc)
        ?: throw NoSuchElementException("Focus session with id=$focusSessionId not found")
  }

  override suspend fun addFocusSession(focusSession: FocusSession) {
    val userId = currentUserId()
    val ownedFocusSession = focusSession.copy(creatorId = userId)
    collection
        .document(ownedFocusSession.focusSessionId)
        .set(focusSessionToMap(ownedFocusSession))
        .await()
  }

  override suspend fun updateFocusSession(
      focusSessionId: String,
      updatedFocusSession: FocusSession
  ) {
    val existing = getFocusSession(focusSessionId)
    if (currentUserId() != existing.creatorId) {
      throw SecurityException("Only the creator of this focus session can edit it")
    }
    // Preserve the original creatorId
    val updated = updatedFocusSession.copy(creatorId = existing.creatorId)
    collection.document(focusSessionId).set(focusSessionToMap(updated)).await()
  }

  override suspend fun deleteFocusSession(focusSessionId: String) {
    val existing = getFocusSession(focusSessionId)
    if (currentUserId() != existing.creatorId) {
      throw SecurityException("Only the creator of this focus session can delete it")
    }
    collection.document(focusSessionId).delete().await()
  }

  /**
   * Converts a Firestore [DocumentSnapshot] into a [FocusSession] object.
   *
   * @param doc The Firestore document representing a [FocusSession].
   * @return The constructed [FocusSession], or `null` if required fields are missing.
   */
  private fun snapshotToFocusSession(doc: DocumentSnapshot): FocusSession? {
    val focusSessionId = doc.getString("focusSessionId") ?: return null
    val creatorId = doc.getString("creatorId") ?: return null
    val linkedTodoId = doc.getString("linkedTodoId")
    val durationSeconds = doc.getLong("duration")
    val startedAt = doc.getTimestamp("startedAt")
    val endedAt = doc.getTimestamp("endedAt")

    return FocusSession(
        focusSessionId = focusSessionId,
        creatorId = creatorId,
        linkedTodoId = linkedTodoId,
        duration = durationSeconds?.seconds ?: Duration.ZERO,
        startedAt = startedAt,
        endedAt = endedAt)
  }

  /**
   * Converts a [FocusSession] into a Firestore-compatible map.
   *
   * Used when adding or editing a focus session in Firestore.
   *
   * @param focusSession The [FocusSession] to serialize.
   * @return A map of field names to values compatible with Firestore.
   */
  private fun focusSessionToMap(focusSession: FocusSession): Map<String, Any?> {
    return mapOf(
        "focusSessionId" to focusSession.focusSessionId,
        "creatorId" to focusSession.creatorId,
        "linkedTodoId" to focusSession.linkedTodoId,
        "duration" to focusSession.duration.inWholeSeconds,
        "startedAt" to focusSession.startedAt,
        "endedAt" to focusSession.endedAt)
  }
}
