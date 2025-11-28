package com.android.gatherly.model.points

import com.android.gatherly.utils.FirebaseEmulator
import com.android.gatherly.utils.FirestorePointsGatherlyTest
import java.lang.IllegalArgumentException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest

class PointsRepositoryFirestoreTest : FirestorePointsGatherlyTest() {

  /** Tests that adding a points instance stores it correctly in the Firebase */
  @Test
  fun addPointsStoresInFirebase() = runTest {
    val pointsCountBefore = getPointsCount()
    assertEquals(0, pointsCountBefore)

    repository.addPoints(points1.copy(userId = FirebaseEmulator.auth.currentUser?.uid ?: ""))

    val pointsCountAfter = getPointsCount()
    assertEquals(1, pointsCountAfter)
  }

  /**
   * Tests that adding then retrieving points returns the correct list, from most to least recent
   */
  @Test
  fun getPointsReturnsCorrectList() = runTest {
    val currentUserPoints1 = points1.copy(userId = FirebaseEmulator.auth.currentUser?.uid ?: "")
    val currentUserPoints2 = points2.copy(userId = FirebaseEmulator.auth.currentUser?.uid ?: "")
    repository.addPoints(currentUserPoints1)
    repository.addPoints(currentUserPoints2)

    val pointsList = repository.getAllPoints()

    assertEquals(2, pointsList.size)
    assertEquals(listOf(currentUserPoints2, currentUserPoints1), pointsList)
  }

  /**
   * Tests that trying to add a points instance with the incorrect user id throws an
   * [IllegalArgumentException]
   */
  @Test
  fun cannotAddPointsWithIncorrectId() = runTest {
    assertFailsWith<IllegalArgumentException> { repository.addPoints(points1) }
  }
}
