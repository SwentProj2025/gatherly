package com.android.gatherly.viewmodel.points

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.gatherly.model.points.Points
import com.android.gatherly.model.points.PointsLocalRepository
import com.android.gatherly.model.points.PointsRepository
import com.android.gatherly.model.points.PointsSource
import com.android.gatherly.ui.points.FocusPointsViewModel
import com.google.firebase.Timestamp
import java.util.Calendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class FocusPointsViewModelTest {
  private lateinit var pointsRepository: PointsRepository
  private lateinit var pointsViewModel: FocusPointsViewModel

  // initialize this so that tests control all coroutines and can wait on them
  private val testDispatcher = StandardTestDispatcher()

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
  private val points1 =
      Points(
          userId = "test-user",
          obtained = 23.9,
          reason = PointsSource.Timer(22),
          dateObtained = Timestamp(nov21.time))

  private val points2 =
      Points(
          userId = "test-user",
          obtained = 30.0,
          reason = PointsSource.Badge("bronze friends"),
          dateObtained = Timestamp(nov23.time))

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

  @Test
  fun noFocusPointsHistoryGivesEmptyList() = runTest {
    val focusHistory = pointsViewModel.uiState.value.focusHistory

    assertEquals(emptyList<Points>(), focusHistory)
  }

  @Test
  fun focusPointsHistoryCorrectlyReturned() = runTest {
    pointsRepository.addPoints(points1)
    pointsRepository.addPoints(points2)

    advanceUntilIdle()

    pointsViewModel.loadPointsHistory()

    advanceUntilIdle()

    val focusHistory = pointsViewModel.uiState.value.focusHistory

    assertEquals(2, focusHistory.size)
    assertEquals(listOf(points2, points1), focusHistory)
  }
}
