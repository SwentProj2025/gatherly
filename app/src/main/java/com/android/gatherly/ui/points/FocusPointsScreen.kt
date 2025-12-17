package com.android.gatherly.ui.points

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.R
import com.android.gatherly.model.points.Points
import com.android.gatherly.model.points.PointsSource
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu_Goback
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Test tags used in [FocusPointsScreen] for Compose UI testing.
 *
 * These tags allow tests to reliably find important UI elements such as the history cards and the
 * info overlay.
 */
object FocusPointsTestTags {
  const val NO_HISTORY = "noHistory"
  const val HISTORY_CARD = "historyCard"
  const val INFO_CARD = "infoCard"
  const val INFO_BUTTON = "infoButton"
  const val DISMISS_BUTTON = "dismissButton"
}

private const val INFO_CARD_SCREEN_FIT = 0.9f

/**
 * Screen showing how the user earned focus points and a history of point acquisitions.
 *
 * The screen displays:
 * - A header with an info button opening an overlay explaining how to obtain points
 * - Either an empty state message if no history exists, or a list of history cards otherwise
 *
 * @param focusPointsViewModel ViewModel providing the points history state.
 * @param goBack Callback invoked when the user presses the back button in the top bar.
 */
@Composable
fun FocusPointsScreen(
    focusPointsViewModel: FocusPointsViewModel = viewModel(),
    goBack: () -> Unit = {}
) {

  val ui by focusPointsViewModel.uiState.collectAsState()

  val showInfo = rememberSaveable { mutableStateOf(false) }

  Scaffold(
      topBar = {
        TopNavigationMenu_Goback(
            selectedTab = Tab.FocusPoints,
            modifier = Modifier.testTag(NavigationTestTags.TOP_NAVIGATION_MENU),
            goBack = goBack)
      }) { paddingVal ->
        Column(modifier = Modifier.padding(paddingVal)) {
          Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(dimensionResource(R.dimen.padding_screen))) {
                Text(
                    text = stringResource(R.string.focus_points_acquisition),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f))

                IconButton(
                    onClick = { showInfo.value = true },
                    modifier = Modifier.testTag(FocusPointsTestTags.INFO_BUTTON)) {
                      Icon(
                          imageVector = Icons.Default.Info,
                          contentDescription = "Info button",
                          tint = MaterialTheme.colorScheme.onBackground)
                    }
              }

          if (ui.focusHistory.isEmpty()) {
            Text(
                text = stringResource(R.string.focus_points_no_history),
                color = MaterialTheme.colorScheme.onBackground,
                modifier =
                    Modifier.testTag(FocusPointsTestTags.NO_HISTORY)
                        .padding(dimensionResource(R.dimen.padding_screen)))
          } else {
            LazyColumn(modifier = Modifier.padding(dimensionResource(R.dimen.padding_screen))) {
              for (points in ui.focusHistory) {
                item {
                  Card(
                      colors =
                          CardDefaults.cardColors(
                              containerColor = MaterialTheme.colorScheme.surfaceVariant,
                              contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                      modifier =
                          Modifier.testTag(FocusPointsTestTags.HISTORY_CARD)
                              .padding(vertical = dimensionResource(R.dimen.padding_small))
                              .fillMaxWidth()
                              .height(dimensionResource(R.dimen.focus_item_height))) {
                        Column(
                            verticalArrangement = Arrangement.SpaceEvenly,
                            modifier =
                                Modifier.padding(dimensionResource(R.dimen.padding_screen))) {
                              Text(
                                  text =
                                      stringResource(R.string.focus_points_gained, points.obtained),
                                  fontWeight = FontWeight.Bold)

                              val reasonText = reasonText(points = points)

                              Text(text = reasonText)

                              val datePattern = stringResource(R.string.focus_date_format)
                              val sdf =
                                  remember(datePattern) {
                                    SimpleDateFormat(datePattern, Locale.getDefault())
                                  }

                              Text(
                                  text =
                                      stringResource(
                                          R.string.focus_points_date,
                                          sdf.format(points.dateObtained.toDate())))
                            }
                      }
                }
              }
            }
          }
        }

        val screenFit = INFO_CARD_SCREEN_FIT

        if (showInfo.value) {
          Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier.padding(paddingVal).fillMaxSize()) {
                Card(
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                    modifier =
                        Modifier.testTag(FocusPointsTestTags.INFO_CARD).fillMaxSize(screenFit)) {
                      LazyColumn(
                          modifier = Modifier.padding(dimensionResource(R.dimen.padding_screen))) {
                            item {
                              Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.focus_points_how_to_obtain),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f))

                                IconButton(
                                    onClick = { showInfo.value = false },
                                    modifier =
                                        Modifier.testTag(FocusPointsTestTags.DISMISS_BUTTON)) {
                                      Icon(
                                          imageVector = Icons.Default.Cancel,
                                          contentDescription = "Dismiss info",
                                          tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                              }
                            }

                            item {
                              Text(
                                  text = stringResource(R.string.focus_points_focus_sessions),
                                  fontWeight = FontWeight.Bold)
                            }

                            item {
                              Text(text = stringResource(R.string.focus_points_focus_sessions_text))
                            }

                            item {
                              Text(
                                  text = stringResource(R.string.focus_points_badges),
                                  fontWeight = FontWeight.Bold)
                            }

                            item { Text(text = stringResource(R.string.focus_points_badges_text)) }

                            item {
                              Text(
                                  text = stringResource(R.string.focus_points_leaderboard),
                                  fontWeight = FontWeight.Bold)
                            }

                            item {
                              Text(text = stringResource(R.string.focus_points_leaderboard_text))
                            }
                          }
                    }
              }
        }
      }
}

/**
 * Builds the localized "reason" line shown in each focus points history card.
 *
 * Each [Points] entry has a [PointsSource] describing why the points were awarded (timer session,
 * badge unlock, or leaderboard rank). This function converts that source into a user-facing,
 * localized string using string resources (and plurals for minutes).
 *
 * @param points The focus points entry for which we want to display the awarding reason.
 * @return A localized string describing why the points were obtained.
 */
@Composable
private fun reasonText(points: Points): String {
  return when (points.reason) {
    is PointsSource.Timer ->
        pluralStringResource(
            R.plurals.focus_history_timer, points.reason.minutes, points.reason.minutes)
    is PointsSource.Badge -> stringResource(R.string.focus_history_badge, points.reason.badgeName)
    is PointsSource.Leaderboard ->
        stringResource(R.string.focus_history_leaderboard, points.reason.rank)
  }
}
