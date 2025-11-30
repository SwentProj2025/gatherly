package com.android.gatherly.ui.points

import android.annotation.SuppressLint
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.R
import com.android.gatherly.model.points.PointsSource
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu_Goback
import java.text.SimpleDateFormat

object FocusPointsTestTags {
  const val NO_HISTORY = "noHistory"
  const val HISTORY_CARD = "historyCard"
  const val INFO_CARD = "infoCard"
  const val INFO_BUTTON = "infoButton"
  const val DISMISS_BUTTON = "dismissButton"
}

@SuppressLint("SimpleDateFormat")
@Composable
fun FocusPointsScreen(
    focusPointsViewModel: FocusPointsViewModel = viewModel(),
    goBack: () -> Unit = {}
) {

  val ui = focusPointsViewModel.uiState.collectAsState()
  val showInfo = remember { mutableStateOf(false) }

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

          if (ui.value.focusHistory.isEmpty()) {
            Text(
                text = stringResource(R.string.focus_points_no_history),
                color = MaterialTheme.colorScheme.onBackground,
                modifier =
                    Modifier.testTag(FocusPointsTestTags.NO_HISTORY)
                        .padding(dimensionResource(R.dimen.padding_screen)))
          } else {
            LazyColumn(modifier = Modifier.padding(dimensionResource(R.dimen.padding_screen))) {
              for (points in ui.value.focusHistory) {
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
                                  text = "+ ${points.obtained} points",
                                  fontWeight = FontWeight.Bold)

                              val reasonText =
                                  when (points.reason) {
                                    is PointsSource.Timer ->
                                        "Focused for ${points.reason.minutes} minutes"
                                    is PointsSource.Badge ->
                                        "Obtained the ${points.reason.badgeName} badge"
                                    is PointsSource.Leaderboard ->
                                        "Reached ${points.reason.rank} on your friends leaderboard"
                                  }

                              Text(text = reasonText)

                              val sdf = SimpleDateFormat("dd/MM/yyyy 'at' HH:mm")

                              Text(
                                  text = "Obtained on: ${sdf.format(points.dateObtained.toDate())}")
                            }
                      }
                }
              }
            }
          }
        }

        val screenFit = 0.9f

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
                                          contentDescription = "Dismiss info")
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
