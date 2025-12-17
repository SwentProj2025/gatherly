package com.android.gatherly.viewmodel.points

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.gatherly.model.points.Points
import com.android.gatherly.model.points.PointsLocalRepository
import com.android.gatherly.model.points.PointsRepository
import com.android.gatherly.ui.points.FocusPointsViewModel
import com.android.gatherly.viewmodel.points.FocusPointsViewModelTestData.points1
import com.android.gatherly.viewmodel.points.FocusPointsViewModelTestData.points2
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Tests for [FocusPointsViewModel] */
@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class FocusPointsViewModelTest {
  private lateinit var pointsRepository: PointsRepository
  private lateinit var pointsViewModel: FocusPointsViewModel

  // initialize this so that tests control all coroutines and can wait on them
  private val testDispatcher = UnconfinedTestDispatcher()
  val testTimeout = 120.seconds

  @Before
  fun setUp() {
    // so that tests can wait on coroutines
    Dispatchers.setMain(testDispatcher)

    pointsRepository = PointsLocalRepository()
    pointsViewModel = FocusPointsViewModel(pointsRepository = pointsRepository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  /** Verifies that focus points history returns an empty list when no points exist */
  @Test
  fun noFocusPointsHistoryGivesEmptyList() =
      runTest(testDispatcher, testTimeout) {
        val focusHistory = pointsViewModel.uiState.value.focusHistory

        assertEquals(emptyList<Points>(), focusHistory)
      }

  /**
   * Verifies that focus points history correctly returns all timer-based points in reverse
   * chronological order
   */
  @Test
  fun focusPointsHistoryCorrectlyReturned() =
      runTest(testDispatcher, testTimeout) {
        pointsRepository.addPoints(points1)
        pointsRepository.addPoints(points2)

        pointsViewModel.loadPointsHistory()

        val focusHistory = pointsViewModel.uiState.value.focusHistory

        assertEquals(2, focusHistory.size)
        assertEquals(listOf(points2, points1), focusHistory)
      }
}
