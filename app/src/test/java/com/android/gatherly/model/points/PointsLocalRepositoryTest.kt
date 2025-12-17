package com.android.gatherly.model.points

import com.android.gatherly.runUnconfinedTest
import com.google.firebase.Timestamp
import kotlin.time.Duration.Companion.seconds
import org.junit.Before
import org.junit.Test

/** Unit tests for [PointsLocalRepository]. */
class PointsLocalRepositoryTest {

  private lateinit var pointsRepository: PointsRepository
  private val testTimeout = 120.seconds

  /** Sample Points instances for testing. */
  val points1 =
      Points(
          obtained = 23.9,
          reason = PointsSource.Timer(22),
          dateObtained = Timestamp(Timestamp.now().seconds - 3600, 0))

  val points2 =
      Points(
          obtained = 30.0,
          reason = PointsSource.Badge("bronze friends"),
          dateObtained = Timestamp(Timestamp.now().seconds - 1800, 0))

  val points3 =
      Points(
          obtained = 75.0,
          reason = PointsSource.Leaderboard("2nd"),
          dateObtained = Timestamp(Timestamp.now().seconds - 7200, 0))

  @Before
  fun setUp() {
    pointsRepository = PointsLocalRepository()
  }

  /**
   * Checks that adding a few [Points] instances and retrieving them, retrieves them correctly and
   * in the correct order
   */
  @Test
  fun addAndFetchWorks() =
      runUnconfinedTest(testTimeout) {
        pointsRepository.addPoints(points1)
        pointsRepository.addPoints(points2)
        pointsRepository.addPoints(points3)

        val pointsList = pointsRepository.getAllPoints()

        assert(pointsList.size == 3)
        assert(pointsList == listOf(points2, points1, points3))
      }
}
