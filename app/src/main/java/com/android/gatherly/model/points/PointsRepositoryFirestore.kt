package com.android.gatherly.model.points

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class PointsRepositoryFirestore(private val db: FirebaseFirestore) : PointsRepository {

  private val collection
    get() = db.collection("points")

  /**
   * Returns the UID of the currently authenticated Firebase user.
   *
   * @throws IllegalStateException if no user is signed in.
   */
  private fun currentUserId(): String {
    return Firebase.auth.currentUser?.uid ?: throw IllegalStateException("No signed in user")
  }

  /** Creates and returns a new Firestore document ID. */
  private fun getNewUid(): String {
    return collection.document().id
  }

  override suspend fun getAllPoints(): List<Points> {
    val snap = collection.whereEqualTo("userId", currentUserId()).get().await()
    return snap.mapNotNull { snapshotToPoints(it) }.sortedByDescending { it.dateObtained }
  }

  override suspend fun addPoints(points: Points) {
    if (points.userId != currentUserId()) {
      throw IllegalArgumentException("Trying to add points for another user!")
    }

    collection.document(getNewUid()).set(pointsToMap(points)).await()
  }

  /**
   * Converts a Firestore [DocumentSnapshot] into a [Points] object.
   *
   * @param doc The Firestore document representing a [Points] instance.
   * @return The constructed [Points], or `null` if required fields are missing or malformatted.
   */
  private fun snapshotToPoints(doc: DocumentSnapshot): Points? {
    val userId = doc.getString("userId") ?: return null
    val obtained = doc.getDouble("obtained") ?: return null
    val pointsType = doc.getString("reason") ?: return null
    val dateObtained = doc.getTimestamp("dateObtained") ?: return null

    when (pointsType) {
      "Timer" -> {
        val minutes = doc.getLong("minutes")?.toInt() ?: return null
        return Points(userId, obtained, PointsType.Timer(minutes), dateObtained)
      }
      "Badge" -> {
        val badgeName = doc.getString("badgeName") ?: return null
        return Points(userId, obtained, PointsType.Badge(badgeName), dateObtained)
      }
      "Leaderboard" -> {
        val rank = doc.getString("rank") ?: return null
        return Points(userId, obtained, PointsType.Leaderboard(rank), dateObtained)
      }
      else -> return null
    }
  }

  /**
   * Converts a [Points] instance into a Firestore-compatible map.
   *
   * @param points The [Points] instance to serialize.
   * @return A map of field names to values that can be stored in Firestore
   */
  private fun pointsToMap(points: Points): Map<String, Any?> {
    return mapOf(
        "userId" to points.userId,
        "obtained" to points.obtained,
        "reason" to points.reason,
        "dateObtained" to points.dateObtained,
        when (points.reason) {
          is PointsType.Timer -> "minutes" to points.reason.minutes
          is PointsType.Badge -> "badgeName" to points.reason.badgeName
          is PointsType.Leaderboard -> "rank" to points.reason.rank
        })
  }
}
