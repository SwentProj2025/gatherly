package com.android.gatherly.ui.groups

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.R
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.ui.navigation.NavigationActions
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Screen
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu_Goback
import com.android.gatherly.utils.GatherlyAlertDialog
import com.android.gatherly.utils.GatherlyAlertDialogActions
import com.android.gatherly.utils.LoadingAnimation
import com.android.gatherly.utils.profilePicturePainter

object GroupInformationScreenTestTags {
  const val MEMBERS_LIST = "membersList"
  const val EDIT_BUTTON = "editButton"
  const val LEAVE_BUTTON = "leaveButton"
  const val GROUP_NAME = "groupName"
  const val GROUP_DESCRIPTION = "groupDescription"

  fun getTestTagForMemberItem(uid: String): String = "member_${uid}"

  fun getTestTagForAdminItem(uid: String): String = "admin_${uid}"
}

/**
 * Displays information about a given group
 *
 * @param navigationActions Used to navigate from one screen to another
 * @param groupInformationViewModel The viewModel through which we access data
 * @param groupId The id of the group to display
 */
@Composable
fun GroupInformationScreen(
    navigationActions: NavigationActions? = null,
    groupInformationViewModel: GroupInformationViewModel = viewModel(),
    groupId: String
) {

  val uiState by groupInformationViewModel.uiState.collectAsState()
  val showDialog = remember { mutableStateOf(false) }

  // Loads the UI
  LaunchedEffect(Unit) { groupInformationViewModel.loadUIState(groupId) }

  // Checks whether to navigate back to overview
  LaunchedEffect(uiState.navigateToOverview) {
    if (uiState.navigateToOverview) {
      navigationActions?.navigateTo(Screen.OverviewGroupsScreen)
    }
  }

  Scaffold(
      topBar = {
        TopNavigationMenu_Goback(
            selectedTab = Tab.GroupInfo,
            modifier = Modifier.testTag(NavigationTestTags.TOP_NAVIGATION_MENU),
            goBack = { navigationActions?.navigateTo(Screen.OverviewGroupsScreen) })
      },
      content = { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally) {

              // While the screen is loading
              if (uiState.isLoading) {
                LoadingAnimation(stringResource(R.string.groups_info_loading), padding)
              } else {

                // Group name
                Text(
                    text = uiState.group.name,
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag(GroupInformationScreenTestTags.GROUP_NAME))

                // Group description
                uiState.group.description?.let {
                  Text(
                      text = it,
                      style = MaterialTheme.typography.headlineMedium,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.testTag(GroupInformationScreenTestTags.GROUP_DESCRIPTION))
                }

                // Members text
                Text(
                    text = stringResource(R.string.groups_info_members),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.align(Alignment.Start))

                HorizontalDivider(
                    modifier =
                        Modifier.padding(vertical = dimensionResource(R.dimen.padding_regular)))

                // The list of group members
                MembersList(
                    membersProfiles = uiState.memberProfiles,
                    adminIds = uiState.group.adminIds,
                    modifier = Modifier.weight(1f))

                // Edit group
                EditButton(uiState, navigationActions)

                // Leave group
                LeaveButton(uiState, showDialog)

                // Leave group warning pop up
                if (showDialog.value) {
                  GatherlyAlertDialog(
                      titleText = stringResource(R.string.groups_dialog_title),
                      bodyText = stringResource(R.string.groups_dialog_text),
                      dismissText = stringResource(R.string.cancel),
                      confirmText = stringResource(R.string.groups_dialog_confirm),
                      neutralEnabled = false,
                      actions =
                          GatherlyAlertDialogActions(
                              onConfirm = {
                                showDialog.value = false
                                groupInformationViewModel.onLeaveGroup()
                              },
                              onDismiss = { showDialog.value = false }),
                      isImportantWarning = true)
                }
              }
            }
      })
}

/**
 * Displays a "leave group" button if the user is not the owner
 *
 * @param uiState The state currently exposed to the UI
 * @param showDialog Whether to show the [GatherlyAlertDialog]
 */
@Composable
fun LeaveButton(uiState: GroupInformationUIState, showDialog: MutableState<Boolean>) {
  if (!uiState.isOwner) {
    // Leave group button
    Button(
        onClick = { showDialog.value = true },
        modifier =
            Modifier.padding(all = dimensionResource(R.dimen.add_group_button_vertical))
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.homepage_focus_button_height))
                .testTag(GroupInformationScreenTestTags.LEAVE_BUTTON),
        shape = RoundedCornerShape(dimensionResource(R.dimen.friends_item_rounded_corner_shape)),
        colors = buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
          Text(
              text = stringResource(R.string.groups_info_leave),
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onError)
        }
  }
}

/**
 * Displays an "edit group" button if the user is an admin
 *
 * @param uiState The state currently exposed to the UI
 * @param navigationActions Used to navigate to different screens
 */
@Composable
fun EditButton(uiState: GroupInformationUIState, navigationActions: NavigationActions?) {
  if (uiState.isAdmin) {
    // Edit group button
    Button(
        onClick = { navigationActions?.navigateTo(Screen.EditGroupScreen(uiState.group.gid)) },
        modifier =
            Modifier.padding(all = dimensionResource(R.dimen.add_group_button_vertical))
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.homepage_focus_button_height))
                .testTag(GroupInformationScreenTestTags.EDIT_BUTTON),
        shape = RoundedCornerShape(dimensionResource(R.dimen.friends_item_rounded_corner_shape)),
        colors = buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
          Text(
              text = stringResource(R.string.groups_info_edit),
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onPrimary)
        }
  }
}

/**
 * Displays list of current group members
 *
 * @param membersProfiles The list of profiles to display
 * @param adminIds The list of ids that are admins to determine if a certain user is admin or not
 * @param modifier The modifier passed down to the composable
 */
@Composable
fun MembersList(membersProfiles: List<Profile>, adminIds: List<String>, modifier: Modifier) {
  LazyColumn(
      modifier = modifier.fillMaxWidth().testTag(GroupInformationScreenTestTags.MEMBERS_LIST)) {
        for (profile in membersProfiles) {
          item {
            Row(
                modifier =
                    Modifier.height(dimensionResource(R.dimen.add_group_button_height))
                        .testTag(
                            GroupInformationScreenTestTags.getTestTagForMemberItem(profile.uid)),
                verticalAlignment = Alignment.CenterVertically) {
                  // Profile picture
                  Image(
                      painter = profilePicturePainter(profile.profilePicture),
                      contentDescription = stringResource(R.string.groups_profile_pic_tag),
                      contentScale = ContentScale.Crop,
                      modifier =
                          Modifier.padding(
                                  horizontal =
                                      dimensionResource(id = R.dimen.group_overview_avatar_spacing))
                              .size(
                                  dimensionResource(R.dimen.find_friends_item_profile_picture_size))
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
                            Modifier.padding(
                                    horizontal = dimensionResource(R.dimen.padding_regular))
                                .testTag(
                                    GroupInformationScreenTestTags.getTestTagForAdminItem(
                                        profile.uid)))
                  }
                }
          }
        }
      }
}
