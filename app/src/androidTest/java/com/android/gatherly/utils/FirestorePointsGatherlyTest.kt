package com.android.gatherly.utils

import android.util.Log
import com.android.gatherly.model.points.Points
import com.android.gatherly.model.points.PointsRepository
import com.android.gatherly.model.points.PointsRepositoryFirestore
import com.android.gatherly.model.points.PointsSource
import com.google.firebase.Timestamp
import java.util.Calendar
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before

/**
 * Base class for Firestore-based Android tests using the Firebase Emulator Suite and a
 * [PointsRepository].
 *
 * Before running, start the emulators: firebase emulators:start
 */
open class FirestorePointsGatherlyTest {

  protected lateinit var repository: PointsRepository

  private val nov23 =
      Calendar.getInstance().apply {
        set(Calendar.YEAR, 2025)
        set(Calendar.MONTH, Calendar.NOVEMBER)
        set(Calendar.DAY_OF_MONTH, 23)
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 39)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
      }

  private val nov21 =
      Calendar.getInstance().apply {
        set(Calendar.YEAR, 2025)
        set(Calendar.MONTH, Calendar.NOVEMBER)
        set(Calendar.DAY_OF_MONTH, 21)
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 39)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
      }

  // Example points used in tests
  protected val points1 =
      Points(
          userId = "test-user",
          obtained = 23.9,
          reason = PointsSource.Timer(22),
          dateObtained = Timestamp(nov21.time))

  protected val points2 =
      Points(
          userId = "test-user",
          obtained = 30.0,
          reason = PointsSource.Badge("bronze friends"),
          dateObtained = Timestamp(nov23.time))

  @Before
  open fun setUp() = runBlocking {
    if (!FirebaseEmulator.isRunning) {
      error("Firebase emulator must be running! Use: firebase emulators:start")
    }
    FirebaseEmulator.auth.signInAnonymously().await()
    repository = PointsRepositoryFirestore(FirebaseEmulator.firestore)
    clearPoints()
  }

  @After
  open fun tearDown() {
    runTest { clearPoints() }
    FirebaseEmulator.clearAuthEmulator()
    FirebaseEmulator.clearFirestoreEmulator()
  }

  /** Deletes all [Points] in the emulator Firestore */
  protected suspend fun clearPoints() {
    val points = FirebaseEmulator.firestore.collection("points").get().await()

    val batch = FirebaseEmulator.firestore.batch()
    points.documents.forEach { batch.delete(it.reference) }
    batch.commit().await()

    Log.d("FirestoreGatherlyTest", "Cleared ${points.size()} points")
  }

  /** Returns the current count of [Points] in the emulator Firestore */
  protected suspend fun getPointsCount(): Int {
    val snap = FirebaseEmulator.firestore.collection("points").get().await()
    return snap.size()
  }
}
