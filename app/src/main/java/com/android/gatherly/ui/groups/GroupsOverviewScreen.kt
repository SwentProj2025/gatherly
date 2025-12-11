package com.android.gatherly.ui.groups

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.R
import com.android.gatherly.ui.navigation.BottomNavigationMenu
import com.android.gatherly.ui.navigation.NavigationActions
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Screen
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu
import com.android.gatherly.utils.profilePicturePainter

object GroupsOverviewScreenTestTags {
  const val LIST = "list"
  const val CREATE_BUTTON = "createButton"
  const val NO_GROUPS = "noGroups"

  fun getTestTagForGroupItem(gid: String): String = "group${gid}"
}

@Composable
fun GroupsOverviewScreen(
    navigationActions: NavigationActions? = null,
    groupsOverviewViewModel: GroupsOverviewViewModel = viewModel()
) {

  val uiState = groupsOverviewViewModel.uiState.collectAsState()
  val snackBarHostState = remember { SnackbarHostState() }
  val errorMessage = uiState.value.errorMsg

  LaunchedEffect(errorMessage) {
    if (errorMessage != null) {
      snackBarHostState.showSnackbar(message = errorMessage, withDismissAction = true)
      groupsOverviewViewModel.resetErrorMessage()
    }
  }

  Scaffold(
      topBar = {
        TopNavigationMenu(
            selectedTab = Tab.GroupsOverview,
            onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
            modifier = Modifier.testTag(NavigationTestTags.TOP_NAVIGATION_MENU))
      },
      bottomBar = {
        BottomNavigationMenu(
            selectedTab = Tab.GroupsOverview,
            onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
            modifier = Modifier.testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU))
      },
      content = { padding ->
        OverviewContent(
            groupsOverviewViewModel = groupsOverviewViewModel,
            padding = padding,
            navigationActions = navigationActions)
      })
}

@Composable
fun OverviewContent(
    groupsOverviewViewModel: GroupsOverviewViewModel,
    padding: PaddingValues,
    navigationActions: NavigationActions?
) {
  val uiState = groupsOverviewViewModel.uiState.collectAsState()

  Column(
      modifier = Modifier.padding(padding).fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceEvenly) {
        if (uiState.value.isLoading) {
          Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.groups_overview_loading),
                modifier =
                    Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.padding_small)),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground)
          }
        } else if (uiState.value.groups.isEmpty()) {
          Box(
              modifier =
                  Modifier.fillMaxWidth()
                      .weight(1f)
                      .testTag(GroupsOverviewScreenTestTags.NO_GROUPS),
              contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.groups_overview_no_groups),
                    modifier =
                        Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.padding_small)),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
              }
        } else {

          // Groups
          LazyColumn(
              modifier =
                  Modifier.fillMaxWidth().weight(1f).testTag(GroupsOverviewScreenTestTags.LIST)) {
                for ((index, group) in uiState.value.groups.withIndex()) {
                  item {
                    Card(
                        modifier =
                            Modifier.fillMaxWidth()
                                .height(dimensionResource(R.dimen.add_group_button_height))
                                .testTag(
                                    GroupsOverviewScreenTestTags.getTestTagForGroupItem(group.gid)),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                contentColor = MaterialTheme.colorScheme.onBackground)) {
                          Row(
                              modifier =
                                  Modifier.fillMaxSize()
                                      .padding(dimensionResource(R.dimen.padding_small)),
                              verticalAlignment = Alignment.CenterVertically) {

                                // Profile pictures of the first 3 users
                                val picsList = uiState.value.profilePics[index]
                                GroupPictures(picsList = picsList)

                                // name of the group
                                Text(
                                    text = group.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier =
                                        Modifier.padding(
                                                horizontal =
                                                    dimensionResource(R.dimen.padding_regular))
                                            .weight(1f))

                                // Icon to go to group
                                IconButton(
                                    onClick = {
                                      navigationActions?.navigateTo(Screen.GroupInfo(group.gid))
                                    }) {
                                      Icon(
                                          imageVector = Icons.Outlined.ChevronRight,
                                          contentDescription = "Go to group info",
                                          Modifier.size(dimensionResource(R.dimen.padding_regular)))
                                    }
                              }
                        }
                  }

                  item {
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant)
                  }
                }
              }
        }
        // Create a group button
        Button(
            onClick = { navigationActions?.navigateTo(Screen.AddGroupScreen) },
            modifier =
                Modifier.padding(all = dimensionResource(R.dimen.add_group_button_vertical))
                    .fillMaxWidth()
                    .height(dimensionResource(R.dimen.homepage_focus_button_height))
                    .testTag(GroupsOverviewScreenTestTags.CREATE_BUTTON),
            shape =
                RoundedCornerShape(dimensionResource(R.dimen.friends_item_rounded_corner_shape)),
            colors = buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
              Text(
                  text = stringResource(R.string.add_group_button_label),
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.Medium,
                  color = MaterialTheme.colorScheme.onPrimary)
            }
      }
}

@Composable
fun GroupPictures(picsList: List<String>) {
  for (i in 0 until picsList.size) {
    // Profile pic
    Image(
        painter = profilePicturePainter(picsList[i]),
        contentDescription = "Profile picture",
        contentScale = ContentScale.Crop,
        modifier =
            Modifier.padding(
                    horizontal = dimensionResource(id = R.dimen.group_overview_avatar_spacing))
                .size(dimensionResource(R.dimen.find_friends_item_profile_picture_size))
                .clip(CircleShape))
  }
}
