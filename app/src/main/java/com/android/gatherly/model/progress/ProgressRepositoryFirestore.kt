package com.android.gatherly.model.progress

import com.android.gatherly.model.badge.BadgeRank
import com.android.gatherly.model.badge.BadgeType
import com.android.gatherly.model.profile.ProfileRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class ProgressRepositoryFirestore(
    private val db: FirebaseFirestore,
    private val profileRepository: ProfileRepository
) : ProgressRepository {

  private val progressCollection = db.collection("progress")
  private val badgesCollection = db.collection("badges")

  override suspend fun getProgress(uid: String): Progress {
    val docRef = progressCollection.document(uid)
    val snap = docRef.get().await()

    if (!snap.exists()) {
      val default = Progress()
      docRef.set(default).await()
      return default
    }

    return Progress(
        createdTodoCount = (snap.getLong("createdTodoCount") ?: 0L).toInt(),
        completedTodoCount = (snap.getLong("completedTodoCount") ?: 0L).toInt(),
        createdEventCount = (snap.getLong("createdEventCount") ?: 0L).toInt(),
        participatedEventCount = (snap.getLong("participatedEventCount") ?: 0L).toInt(),
        completedFocusSessionCount = (snap.getLong("completedFocusSessionCount") ?: 0L).toInt(),
        addedFriendsCount = (snap.getLong("addedFriendsCount") ?: 0L).toInt())
  }

  override suspend fun incrementCreatedTodo(uid: String): Int {
    val count = incrementField(uid, "createdTodoCount")
    awardBadgeFor(uid, BadgeType.TODOS_CREATED, count)
    return count
  }

  override suspend fun incrementCompletedTodo(uid: String): Int {
    val count = incrementField(uid, "completedTodoCount")
    awardBadgeFor(uid, BadgeType.TODOS_COMPLETED, count)
    return count
  }

  override suspend fun incrementCreatedEvent(uid: String): Int {
    val count = incrementField(uid, "createdEventCount")
    awardBadgeFor(uid, BadgeType.EVENTS_CREATED, count)
    return count
  }

  override suspend fun incrementParticipatedEvent(uid: String): Int {
    val count = incrementField(uid, "participatedEventCount")
    awardBadgeFor(uid, BadgeType.EVENTS_PARTICIPATED, count)
    return count
  }

  override suspend fun incrementCompletedFocusSession(uid: String): Int {
    val count = incrementField(uid, "completedFocusSessionCount")
    awardBadgeFor(uid, BadgeType.FOCUS_SESSIONS_COMPLETED, count)
    return count
  }

  override suspend fun incrementAddedFriend(uid: String): Int {
    val count = incrementField(uid, "addedFriendsCount")
    awardBadgeFor(uid, BadgeType.FRIENDS_ADDED, count)
    return count
  }

  /** Atomically increments the given numeric field and returns the new value. */
  private suspend fun incrementField(uid: String, field: String): Int {
    val docRef = progressCollection.document(uid)

    return db.runTransaction { tx ->
          val snap = tx.get(docRef)

          val current = (snap.getLong(field) ?: 0L).toInt()
          val updated = current + 1

          tx.set(docRef, mapOf(field to updated), SetOptions.merge())

          updated
        }
        .await()
  }

  /**
   * Converts a count to a rank, then finds the matching badge in Firestore and adds it to profile.
   */
  private suspend fun awardBadgeFor(uid: String, type: BadgeType, count: Int) {
    val rank = countToRank(count)
    if (rank == BadgeRank.BLANK) return

    // Find the global badge doc for this type + rank
    val snap =
        badgesCollection
            .whereEqualTo("type", type.name)
            .whereEqualTo("rank", rank.name)
            .limit(1)
            .get()
            .await()

    val badgeDoc = snap.documents.firstOrNull() ?: return
    val badgeId = badgeDoc.id

    val profile = profileRepository.getProfileByUid(uid) ?: return
    profileRepository.addBadge(profile, badgeId)
  }

  /** Shared thresholds from count to BadgeRank. */
  private fun countToRank(count: Int): BadgeRank =
      when {
        count >= 30 -> BadgeRank.LEGEND
        count >= 20 -> BadgeRank.DIAMOND
        count >= 10 -> BadgeRank.GOLD
        count >= 5 -> BadgeRank.SILVER
        count >= 3 -> BadgeRank.BRONZE
        count >= 1 -> BadgeRank.STARTING
        else -> BadgeRank.BLANK
      }
}
