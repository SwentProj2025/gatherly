package com.android.gatherly.ui.groups

import androidx.compose.foundation.Image
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

  if (uiState.value.isLoading) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text(
          text = stringResource(R.string.groups_overview_no_groups),
          modifier = Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.padding_small)),
          textAlign = TextAlign.Center,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground)
    }
  } else {
    Column(modifier = Modifier.padding(padding)) {

      // Groups
      LazyColumn(modifier = Modifier.fillMaxSize()) {
        for ((index, group) in uiState.value.groups.withIndex()) {
          item {
            Card(
                modifier =
                    Modifier.fillMaxWidth()
                        .height(dimensionResource(R.dimen.add_group_button_height)),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.onBackground)) {
                  Row(
                      modifier =
                          Modifier.fillMaxSize().padding(dimensionResource(R.dimen.padding_medium)),
                      verticalAlignment = Alignment.CenterVertically) {

                        // Profile pictures of the first 3 users
                        val picsList = uiState.value.profilePics[index]
                        for (i in 0 until picsList.size) {
                          // Profile pic
                          Image(
                              painter = profilePicturePainter(picsList[i]),
                              contentDescription = "Profile picture",
                              contentScale = ContentScale.Crop,
                              modifier =
                                  Modifier.padding(
                                          horizontal =
                                              dimensionResource(R.dimen.friends_item_card_padding))
                                      .size(
                                          dimensionResource(
                                              R.dimen.find_friends_item_profile_picture_size))
                                      .clip(CircleShape))
                        }

                        // name of the group
                        Text(
                            text = group.name,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier =
                                Modifier.padding(dimensionResource(R.dimen.padding_medium))
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
        }
      }

      // Create a group button
      Button(
          onClick = { navigationActions?.navigateTo(Screen.AddEventScreen) },
          modifier =
              Modifier.fillMaxWidth()
                  .height(dimensionResource(R.dimen.add_group_button_height))
                  .padding(vertical = dimensionResource(R.dimen.add_group_button_vertical))
                  .testTag(AddGroupScreenTestTags.BUTTON_CREATE_GROUP),
          shape = RoundedCornerShape(dimensionResource(R.dimen.friends_item_rounded_corner_shape)),
          colors = buttonColors(containerColor = MaterialTheme.colorScheme.inversePrimary)) {
            Text(
                text = stringResource(R.string.add_group_button_label),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimary)
          }
    }
  }
}
