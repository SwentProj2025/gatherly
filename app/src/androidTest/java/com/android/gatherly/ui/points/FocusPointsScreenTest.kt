package com.android.gatherly.ui.points

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.gatherly.model.points.Points
import com.android.gatherly.model.points.PointsLocalRepository
import com.android.gatherly.model.points.PointsRepository
import com.android.gatherly.model.points.PointsSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Tests for the Focus points history screen */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class FocusPointsScreenTest {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private lateinit var focusPointsViewModel: FocusPointsViewModel
  private lateinit var pointsRepository: PointsRepository

  private val points = Points(obtained = 23.9, reason = PointsSource.Timer(22))

  @Before
  fun setUp() {
    pointsRepository = PointsLocalRepository()
    focusPointsViewModel = FocusPointsViewModel(pointsRepository = pointsRepository)

    composeTestRule.setContent { FocusPointsScreen(focusPointsViewModel = focusPointsViewModel) }
  }

  /** Checks that if there is no user history, the empty message is displayed */
  @Test
  fun emptyHistoryShowsEmptyHistoryMessage() {
    composeTestRule.onNodeWithTag(FocusPointsTestTags.NO_HISTORY).assertIsDisplayed()
  }

  /** Checks that if there is a user history, the card is displayed */
  @Test
  fun focusHistoryIsDisplayed() {
    fill_repository()
    focusPointsViewModel.loadUI()
    composeTestRule.onNodeWithTag(FocusPointsTestTags.HISTORY_CARD).assertIsDisplayed()
  }

  /** Checks that one can show and dismiss the information sheet */
  @Test
  fun canShowAndDismissInfoCard() {
    composeTestRule
        .onNodeWithTag(FocusPointsTestTags.INFO_BUTTON)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.onNodeWithTag(FocusPointsTestTags.INFO_CARD).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(FocusPointsTestTags.DISMISS_BUTTON)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.onNodeWithTag(FocusPointsTestTags.INFO_CARD).assertIsNotDisplayed()
  }

  fun fill_repository() = runTest { pointsRepository.addPoints(points) }
}
