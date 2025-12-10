package com.android.gatherly.ui.groups

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.R
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.ui.navigation.NavigationActions
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Screen
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu_Goback
import com.android.gatherly.utils.profilePicturePainter

@Composable
fun GroupInformationScreen(
    navigationActions: NavigationActions,
    groupInformationViewModel: GroupInformationViewModel = viewModel(),
    groupId: String
) {

  val uiState = groupInformationViewModel.uiState.collectAsState()

  LaunchedEffect(Unit) { groupInformationViewModel.loadUIState(groupId) }

  Scaffold(
      topBar = {
        TopNavigationMenu_Goback(
            selectedTab = Tab.GroupInfo,
            modifier = Modifier.testTag(NavigationTestTags.TOP_NAVIGATION_MENU),
            goBack = { navigationActions.goBack() })
      },
      content = { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally) {
              if (uiState.value.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                  Text(
                      text = stringResource(R.string.groups_info_loading),
                      style = MaterialTheme.typography.bodyLarge,
                      textAlign = TextAlign.Center)
                }
              } else {
                Text(
                    text = uiState.value.group.name,
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold)

                uiState.value.group.description?.let {
                  Text(
                      text = it,
                      style = MaterialTheme.typography.headlineMedium,
                      fontWeight = FontWeight.Bold)
                }

                Text(
                    text = stringResource(R.string.groups_info_members),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.align(Alignment.Start))

                HorizontalDivider(
                    modifier =
                        Modifier.padding(vertical = dimensionResource(R.dimen.padding_regular)))

                MembersList(
                    membersProfiles = uiState.value.memberProfiles,
                    adminIds = uiState.value.group.adminIds,
                    modifier = Modifier.weight(1f))

                if (uiState.value.isAdmin) {
                  Button(
                      onClick = {
                        navigationActions.navigateTo(Screen.EditGroup(uiState.value.group.gid))
                      },
                      modifier =
                          Modifier.padding(
                                  all = dimensionResource(R.dimen.add_group_button_vertical))
                              .fillMaxWidth()
                              .height(dimensionResource(R.dimen.homepage_focus_button_height)),
                      shape =
                          RoundedCornerShape(
                              dimensionResource(R.dimen.friends_item_rounded_corner_shape)),
                      colors = buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                        Text(
                            text = stringResource(R.string.groups_info_edit),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimary)
                      }
                }
              }
            }
      })
}

@Composable
fun MembersList(membersProfiles: List<Profile>, adminIds: List<String>, modifier: Modifier) {
  LazyColumn(modifier = modifier.fillMaxWidth()) {
    for (profile in membersProfiles) {
      item {
        Row(
            modifier = Modifier.height(dimensionResource(R.dimen.add_group_button_height)),
            verticalAlignment = Alignment.CenterVertically) {
              // Profile picture
              Image(
                  painter = profilePicturePainter(profile.profilePicture),
                  contentDescription = "Profile picture",
                  contentScale = ContentScale.Crop,
                  modifier =
                      Modifier.padding(
                              horizontal =
                                  dimensionResource(id = R.dimen.group_overview_avatar_spacing))
                          .size(dimensionResource(R.dimen.find_friends_item_profile_picture_size))
                          .clip(CircleShape))

              // Member name
              Text(
                  text = profile.name,
                  fontWeight = FontWeight.Bold,
                  style = MaterialTheme.typography.headlineSmall,
                  color = MaterialTheme.colorScheme.onBackground,
                  modifier =
                      Modifier.padding(horizontal = dimensionResource(R.dimen.padding_regular))
                          .weight(1f))

              if (adminIds.contains(profile.uid)) {
                // Admin mention
                Text(
                    text = stringResource(R.string.groups_info_admin),
                    fontStyle = FontStyle.Italic,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    modifier =
                        Modifier.padding(horizontal = dimensionResource(R.dimen.padding_regular)))
              }
            }
      }
    }
  }
}
