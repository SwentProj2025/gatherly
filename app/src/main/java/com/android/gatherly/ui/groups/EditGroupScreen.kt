package com.android.gatherly.ui.groups

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.R
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu_Goback
import com.android.gatherly.utils.profilePicturePainter

// This file was inspired by AddGroupScreen.kt in this project.

/** Object containing test tags for the EditGroupScreen and its components. */
object EditGroupScreenTestTags {
  const val BUTTON_SAVE_GROUP = "buttonSaveGroup"

  const val BUTTON_DELETE_GROUP = "buttonDeleteGroup"
  const val GROUP_NAME_FIELD = "editGroupNameField"
  const val GROUP_DESCRIPTION_FIELD = "editGroupDescriptionField"
  const val SEARCH_FRIENDS_BAR = "editSearchFriendsBar"
  const val EMPTY_FRIENDS_MSG = "editEmptyFriendsMessage"
  const val NAME_ERROR_MESSAGE = "editNameErrorMessage"

  const val GENERAL_ERROR_MESSAGE = "editGroupGeneralErrorMessage"

  /**
   * Returns a unique test tag for the card representing a given friend.
   *
   * @param friend The friend whose test tag will be generated.
   * @return A string uniquely identifying the friend's card in the UI.
   */
  fun getTestTagForAvailableFriendItem(friend: String): String = "availableFriendItem$friend"

  /**
   * Returns a unique test tag for the checkbox representing a given friend item.
   *
   * @param friend The friend whose test tag will be generated.
   * @return A string uniquely identifying the friend's checkbox in the UI.
   */
  fun getTestTagForAvailableFriendCheckbox(friend: String): String =
      "availableFriendCheckbox$friend"

  /**
   * Returns a unique test tag for the profile picture representing an available friend item.
   *
   * @param friend The friend whose test tag will be generated.
   * @return A string uniquely identifying the friend's profile picture in the UI.
   */
  fun getTestTagForAvailableFriendProfilePicture(friend: String): String =
      "availableFriendProfilePicture$friend"

  /**
   * Returns a unique test tag for the profile picture representing a given selected friend item.
   *
   * @param friend The friend whose test tag will be generated.
   * @return A string uniquely identifying the friend's profile picture in the UI.
   */
  fun getTestTagForSelectedFriendProfilePicture(friend: String): String =
      "editSelectedFriendProfilePicture$friend"

  /**
   * Returns a unique test tag for the member item.
   *
   * @param member The member whose test tag will be generated.
   * @return A string uniquely identifying the member's checkbox in the UI.
   */
  fun getTestTagForMemberItem(member: String): String = "groupMemberItem$member"

  /**
   * Returns a unique test tag for the admin member checkbox.
   *
   * @param member The member whose test tag will be generated.
   * @return A string uniquely identifying the member's checkbox in the UI.
   */
  fun getTestTagForMemberAdminCheckbox(member: String): String = "groupMemberAdminCheckbox$member"

  /**
   * Returns a unique test tag for the remove member checkbox.
   *
   * @param member The member whose test tag will be generated.
   * @return A string uniquely identifying the member's checkbox in the UI.
   */
  fun getTestTagForMemberRemoveCheckbox(member: String): String = "groupMemberRemoveCheckbox$member"
}

/**
 * Composable function representing the Edit Group screen.
 *
 * @param groupId The ID of the group to edit.
 * @param editGroupViewModel ViewModel managing the Edit Group screen state.
 * @param goBack Callback to navigate back.
 * @param onSaved Callback invoked after a successful save.
 */
@Composable
fun EditGroupScreen(
    groupId: String,
    editGroupViewModel: EditGroupViewModel =
        viewModel(factory = EditGroupViewModel.provideFactory()),
    goBack: () -> Unit = {},
    onSaved: () -> Unit = {},
    onDelete: () -> Unit = {},
) {
  val uiState by editGroupViewModel.uiState.collectAsState()

  val screenPadding = dimensionResource(R.dimen.padding_screen)
  val smallPadding = dimensionResource(R.dimen.padding_small)
  val smallSpacing = dimensionResource(R.dimen.spacing_between_fields)

  val buttonHeight = dimensionResource(R.dimen.add_group_button_height)
  val buttonVerticalPadding = dimensionResource(R.dimen.add_group_button_vertical)
  val buttonCornerRadius = dimensionResource(R.dimen.friends_item_rounded_corner_shape)
  val buttonFontSize = dimensionResource(R.dimen.font_size_medium)

  val dividerThickness = dimensionResource(R.dimen.add_group_horizontal_divider_thickness)
  val friendSectionHeight = dimensionResource(R.dimen.add_group_friend_section_height)

  val inputFieldColors =
      OutlinedTextFieldDefaults.colors(
          focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
          unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
          errorContainerColor = MaterialTheme.colorScheme.surfaceVariant,
          unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
          focusedTextColor = MaterialTheme.colorScheme.onBackground,
          errorTextColor = MaterialTheme.colorScheme.onBackground,
          focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
          unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
          focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
          unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
          unfocusedBorderColor = Color.Transparent,
          focusedBorderColor = Color.Transparent,
          disabledBorderColor = Color.Transparent,
          errorBorderColor = Color.Transparent)

  LaunchedEffect(groupId) { if (groupId.isNotBlank()) editGroupViewModel.loadGroup(groupId) }

  LaunchedEffect(uiState.saveSuccess) {
    if (uiState.saveSuccess) {
      onSaved()
      editGroupViewModel.clearSaveSuccess()
    }
  }

  val isOwner = uiState.currentUserId.isNotBlank() && uiState.currentUserId == uiState.creatorId
  val isAdmin =
      uiState.currentUserId.isNotBlank() && uiState.adminIds.contains(uiState.currentUserId)
  val generalError = uiState.loadError ?: uiState.saveError

  val currentParticipants =
      remember(
          uiState.currentMemberProfiles,
          uiState.membersToRemove,
          uiState.availableFriendsToAdd,
          uiState.selectedNewFriendIds) {
            val removedIds = uiState.membersToRemove.toSet()
            val selectedNewIds = uiState.selectedNewFriendIds.toSet()

            val remainingMembers = uiState.currentMemberProfiles.filter { it.uid !in removedIds }
            val newFriends = uiState.availableFriendsToAdd.filter { it.uid in selectedNewIds }

            (remainingMembers + newFriends).distinctBy { it.uid }
          }

  Scaffold(
      topBar = {
        TopNavigationMenu_Goback(
            selectedTab = Tab.EditGroup,
            modifier = Modifier.testTag(NavigationTestTags.TOP_NAVIGATION_MENU),
            goBack = goBack)
      }) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(vertical = smallPadding),
            modifier =
                Modifier.fillMaxWidth().padding(horizontal = screenPadding).padding(padding)) {
              groupFieldsSection(
                  uiState = uiState,
                  inputFieldColors = inputFieldColors,
                  onNameChanged = editGroupViewModel::onNameChanged,
                  onDescriptionChanged = editGroupViewModel::onDescriptionChanged,
                  smallSpacing = smallSpacing)

              participantsStripSection(
                  participants = currentParticipants,
                  dividerThickness = dividerThickness,
                  smallSpacing = smallSpacing)

              membersSection(
                  uiState = uiState,
                  isOwner = isOwner,
                  isAdmin = isAdmin,
                  dividerThickness = dividerThickness,
                  smallSpacing = smallSpacing,
                  onToggleAdmin = editGroupViewModel::onToggleAdmin,
                  onToggleRemove = editGroupViewModel::onToggleRemoveMember)

              availableFriendsSection(
                  uiState = uiState,
                  inputFieldColors = inputFieldColors,
                  screenPadding = screenPadding,
                  smallSpacing = smallSpacing,
                  friendSectionHeight = friendSectionHeight,
                  onSearchChanged = editGroupViewModel::onFriendsSearchQueryChanged,
                  onToggleFriend = editGroupViewModel::onNewFriendToggled)

              generalErrorSection(generalError, smallSpacing)

              saveButtonSection(
                  enabled = uiState.nameError == null && !uiState.isSaving,
                  buttonHeight = buttonHeight,
                  buttonVerticalPadding = buttonVerticalPadding,
                  buttonCornerRadius = buttonCornerRadius,
                  buttonFontSize = buttonFontSize,
                  onClick = editGroupViewModel::saveGroup)

              deleteButtonSection(
                  isOwner = isOwner,
                  buttonHeight = buttonHeight,
                  buttonVerticalPadding = buttonVerticalPadding,
                  buttonCornerRadius = buttonCornerRadius,
                  buttonFontSize = buttonFontSize,
                  smallSpacing = smallSpacing,
                  onClick = {
                    editGroupViewModel.deleteGroup()
                    onDelete()
                  })
            }
      }
}

/* ----------------------- EditGroupScreen Helpers ----------------------- */

private fun LazyListScope.groupFieldsSection(
    uiState: EditGroupUiState,
    inputFieldColors: TextFieldColors,
    onNameChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    smallSpacing: Dp
) {
  item {
    OutlinedTextField(
        value = uiState.name,
        onValueChange = onNameChanged,
        label = { Text(stringResource(R.string.group_name_bar_label)) },
        placeholder = { Text(stringResource(R.string.group_name_bar_placeholder)) },
        isError = uiState.nameError != null,
        supportingText = {
          uiState.nameError?.let {
            Text(it, modifier = Modifier.testTag(EditGroupScreenTestTags.NAME_ERROR_MESSAGE))
          }
        },
        colors = inputFieldColors,
        modifier = Modifier.fillMaxWidth().testTag(EditGroupScreenTestTags.GROUP_NAME_FIELD))
  }

  item {
    OutlinedTextField(
        value = uiState.description,
        onValueChange = onDescriptionChanged,
        label = { Text(stringResource(R.string.todos_description_field_label)) },
        placeholder = { Text(stringResource(R.string.group_description_bar_placeholder)) },
        colors = inputFieldColors,
        modifier = Modifier.fillMaxWidth().testTag(EditGroupScreenTestTags.GROUP_DESCRIPTION_FIELD))
    Spacer(modifier = Modifier.height(smallSpacing))
  }
}

private fun LazyListScope.participantsStripSection(
    participants: List<Profile>,
    dividerThickness: Dp,
    smallSpacing: Dp
) {
  if (participants.isEmpty()) return

  item {
    val picDescription = stringResource(R.string.profile_picture_description)
    val picSize = dimensionResource(R.dimen.profile_pic_size_medium)
    val picBorder = dimensionResource(R.dimen.profile_pic_border)

    Spacer(modifier = Modifier.height(smallSpacing))
    LazyRow {
      items(participants) { participant ->
        Image(
            painter = profilePicturePainter(participant.profilePicture),
            contentDescription = picDescription,
            modifier =
                Modifier.size(picSize)
                    .clip(CircleShape)
                    .border(picBorder, MaterialTheme.colorScheme.onBackground, CircleShape)
                    .testTag(
                        EditGroupScreenTestTags.getTestTagForSelectedFriendProfilePicture(
                            participant.username)),
            contentScale = ContentScale.Crop)
        Spacer(modifier = Modifier.width(smallSpacing))
      }
    }
    Spacer(modifier = Modifier.height(smallSpacing))
    HorizontalDivider(thickness = dividerThickness, color = MaterialTheme.colorScheme.primary)
    Spacer(modifier = Modifier.height(smallSpacing))
  }
}

private fun LazyListScope.membersSection(
    uiState: EditGroupUiState,
    isOwner: Boolean,
    isAdmin: Boolean,
    dividerThickness: Dp,
    smallSpacing: Dp,
    onToggleAdmin: (String) -> Unit,
    onToggleRemove: (String) -> Unit
) {
  if (uiState.membersForList.isEmpty()) return

  item {
    Spacer(modifier = Modifier.height(smallSpacing))
    Text(
        text = stringResource(R.string.group_members),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold)
    Spacer(modifier = Modifier.height(smallSpacing))
  }

  items(uiState.membersForList) { member ->
    val showAdminToggle = isOwner
    val showRemoveToggle = isOwner || (isAdmin && member.uid != uiState.creatorId)

    MemberItem(
        member = member,
        isAdmin = uiState.adminIds.contains(member.uid),
        markedForRemoval = uiState.membersToRemove.contains(member.uid),
        showAdminToggle = showAdminToggle,
        showRemoveToggle = showRemoveToggle,
        onToggleAdmin = { onToggleAdmin(member.uid) },
        onToggleRemove = { onToggleRemove(member.uid) })
  }

  item {
    Spacer(modifier = Modifier.height(smallSpacing))
    HorizontalDivider(thickness = dividerThickness, color = MaterialTheme.colorScheme.primary)
    Spacer(modifier = Modifier.height(smallSpacing))
    Text(
        text = stringResource(R.string.group_available_friends),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold)
    Spacer(modifier = Modifier.height(smallSpacing))
  }
}

private fun LazyListScope.availableFriendsSection(
    uiState: EditGroupUiState,
    inputFieldColors: TextFieldColors,
    screenPadding: Dp,
    smallSpacing: Dp,
    friendSectionHeight: Dp,
    onSearchChanged: (String) -> Unit,
    onToggleFriend: (String) -> Unit
) {
  if (uiState.availableFriendsToAdd.isEmpty()) {
    item {
      Text(
          text = stringResource(R.string.friends_empty_list_msg),
          modifier =
              Modifier.padding(screenPadding).testTag(EditGroupScreenTestTags.EMPTY_FRIENDS_MSG))
    }
    return
  }

  item {
    val searchCornerShape = dimensionResource(R.dimen.rounded_corner_shape_large)
    OutlinedTextField(
        value = uiState.friendsSearchQuery,
        onValueChange = onSearchChanged,
        modifier =
            Modifier.fillMaxWidth()
                .padding(vertical = screenPadding)
                .testTag(EditGroupScreenTestTags.SEARCH_FRIENDS_BAR),
        shape = RoundedCornerShape(searchCornerShape),
        placeholder = { Text(stringResource(R.string.friends_search_bar_label)) },
        colors = inputFieldColors)
    Spacer(modifier = Modifier.height(smallSpacing))
  }

  item {
    Box(modifier = Modifier.fillMaxWidth().height(friendSectionHeight)) {
      LazyColumn {
        items(uiState.filteredAvailableFriends) { friend ->
          AvailableFriendItem(
              friend = friend,
              isSelected = uiState.selectedNewFriendIds.contains(friend.uid),
              onToggle = { onToggleFriend(friend.uid) })
        }
      }
    }
  }
}

private fun LazyListScope.generalErrorSection(generalError: String?, smallSpacing: Dp) {
  if (generalError == null) return
  item {
    Spacer(modifier = Modifier.height(smallSpacing))
    Text(
        text = generalError,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.testTag(EditGroupScreenTestTags.GENERAL_ERROR_MESSAGE))
    Spacer(modifier = Modifier.height(smallSpacing))
  }
}

private fun LazyListScope.saveButtonSection(
    enabled: Boolean,
    buttonHeight: Dp,
    buttonVerticalPadding: Dp,
    buttonCornerRadius: Dp,
    buttonFontSize: Dp,
    onClick: () -> Unit
) {
  item {
    Button(
        onClick = onClick,
        modifier =
            Modifier.fillMaxWidth()
                .height(buttonHeight)
                .padding(vertical = buttonVerticalPadding)
                .testTag(EditGroupScreenTestTags.BUTTON_SAVE_GROUP),
        shape = RoundedCornerShape(buttonCornerRadius),
        enabled = enabled,
        colors =
            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
          Text(
              text = stringResource(R.string.edit_group_button),
              fontSize = buttonFontSize.value.sp,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSecondary)
        }
  }
}

private fun LazyListScope.deleteButtonSection(
    isOwner: Boolean,
    buttonHeight: Dp,
    buttonVerticalPadding: Dp,
    buttonCornerRadius: Dp,
    buttonFontSize: Dp,
    smallSpacing: Dp,
    onClick: () -> Unit
) {
  if (!isOwner) return
  item {
    Spacer(modifier = Modifier.height(smallSpacing))
    Button(
        onClick = onClick,
        modifier =
            Modifier.fillMaxWidth()
                .height(buttonHeight)
                .padding(vertical = buttonVerticalPadding)
                .testTag(EditGroupScreenTestTags.BUTTON_DELETE_GROUP),
        shape = RoundedCornerShape(buttonCornerRadius),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
          Text(
              text = stringResource(R.string.group_delete_button),
              fontSize = buttonFontSize.value.sp,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onError)
        }
  }
}

/** Row for a current group member with admin and remove toggles. */
@Composable
private fun MemberItem(
    member: Profile,
    isAdmin: Boolean,
    markedForRemoval: Boolean,
    showAdminToggle: Boolean,
    showRemoveToggle: Boolean,
    onToggleAdmin: () -> Unit,
    onToggleRemove: () -> Unit
) {
  val roundedCornerShape = dimensionResource(R.dimen.friends_item_card_rounded_corner_shape)
  val verticalPadding = dimensionResource(R.dimen.friends_item_card_padding_vertical)
  val cardPadding = dimensionResource(R.dimen.friends_item_card_padding)
  val picDescription = stringResource(R.string.profile_picture_description)
  val picSize = dimensionResource(R.dimen.profile_pic_size_regular)
  val smallSpacing = dimensionResource(R.dimen.spacing_between_fields_smaller_regular)
  val regularSpacing = dimensionResource(R.dimen.spacing_between_fields_regular)

  Card(
      shape = RoundedCornerShape(roundedCornerShape),
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant,
              contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
      modifier =
          Modifier.testTag(EditGroupScreenTestTags.getTestTagForMemberItem(member.username))
              .fillMaxWidth()
              .padding(vertical = verticalPadding)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(cardPadding),
            verticalAlignment = Alignment.CenterVertically) {
              Image(
                  painter = profilePicturePainter(member.profilePicture),
                  contentDescription = picDescription,
                  modifier = Modifier.size(picSize).clip(CircleShape),
                  contentScale = ContentScale.Crop)

              Spacer(modifier = Modifier.width(smallSpacing))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.username,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium)

                if (isAdmin) {
                  Spacer(modifier = Modifier.height(smallSpacing / 2))
                  AdminLabel()
                }
              }

              Spacer(modifier = Modifier.width(regularSpacing))

              MemberToggles(
                  username = member.username,
                  isAdmin = isAdmin,
                  markedForRemoval = markedForRemoval,
                  showAdminToggle = showAdminToggle,
                  showRemoveToggle = showRemoveToggle,
                  spacing = smallSpacing,
                  onToggleAdmin = onToggleAdmin,
                  onToggleRemove = onToggleRemove)
            }
      }
}

@Composable
private fun AdminLabel() {
  Text(
      text = stringResource(R.string.group_member_admin),
      style = MaterialTheme.typography.bodySmall,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun MemberToggles(
    username: String,
    isAdmin: Boolean,
    markedForRemoval: Boolean,
    showAdminToggle: Boolean,
    showRemoveToggle: Boolean,
    spacing: Dp,
    onToggleAdmin: () -> Unit,
    onToggleRemove: () -> Unit
) {
  if (!showAdminToggle && !showRemoveToggle) return

  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.wrapContentWidth()) {
    if (showAdminToggle) {
      MemberAdminToggle(username = username, isAdmin = isAdmin, onToggleAdmin = onToggleAdmin)
    }

    if (showAdminToggle && showRemoveToggle) {
      Spacer(modifier = Modifier.width(spacing))
    }

    if (showRemoveToggle) {
      MemberRemoveToggle(
          username = username, markedForRemoval = markedForRemoval, onToggleRemove = onToggleRemove)
    }
  }
}

@Composable
private fun MemberAdminToggle(username: String, isAdmin: Boolean, onToggleAdmin: () -> Unit) {
  val contentDesc =
      if (isAdmin) {
        stringResource(R.string.group_member_admin)
      } else {
        stringResource(R.string.group_member_set_admin)
      }

  Icon(
      imageVector = Icons.Filled.Star,
      contentDescription = contentDesc,
      modifier =
          Modifier.size(32.dp)
              .testTag(EditGroupScreenTestTags.getTestTagForMemberAdminCheckbox(username))
              .clickable { onToggleAdmin() },
      tint =
          if (isAdmin) {
            MaterialTheme.colorScheme.onBackground
          } else {
            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
          })
}

@Composable
private fun MemberRemoveToggle(
    username: String,
    markedForRemoval: Boolean,
    onToggleRemove: () -> Unit
) {
  Checkbox(
      checked = !markedForRemoval,
      onCheckedChange = { onToggleRemove() },
      modifier =
          Modifier.wrapContentWidth()
              .testTag(EditGroupScreenTestTags.getTestTagForMemberRemoveCheckbox(username)))
}

/** Row for a friend that can be added to the group. */
@Composable
private fun AvailableFriendItem(friend: Profile, isSelected: Boolean, onToggle: () -> Unit) {
  val roundedCornerShape = dimensionResource(R.dimen.friends_item_card_rounded_corner_shape)
  val verticalPadding = dimensionResource(R.dimen.friends_item_card_padding_vertical)
  val cardPadding = dimensionResource(R.dimen.friends_item_card_padding)
  val picDescription = stringResource(R.string.profile_picture_description)
  val picSize = dimensionResource(R.dimen.profile_pic_size_regular)
  val smallSpacing = dimensionResource(R.dimen.spacing_between_fields_smaller_regular)
  val regularSpacing = dimensionResource(R.dimen.spacing_between_fields_regular)

  Card(
      shape = RoundedCornerShape(roundedCornerShape),
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant,
              contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
      modifier =
          Modifier.testTag(
                  EditGroupScreenTestTags.getTestTagForAvailableFriendItem(friend.username))
              .fillMaxWidth()
              .padding(vertical = verticalPadding)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(cardPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Image(
              painter = profilePicturePainter(friend.profilePicture),
              contentDescription = picDescription,
              modifier =
                  Modifier.size(picSize)
                      .clip(CircleShape)
                      .testTag(
                          EditGroupScreenTestTags.getTestTagForAvailableFriendProfilePicture(
                              friend.username)),
              contentScale = ContentScale.Crop)

          Spacer(modifier = Modifier.width(smallSpacing))

          Column(modifier = Modifier.weight(1f)) {
            Text(
                text = friend.username,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium)
          }

          Spacer(modifier = Modifier.width(regularSpacing))

          Checkbox(
              checked = isSelected,
              onCheckedChange = { onToggle() },
              modifier =
                  Modifier.wrapContentWidth()
                      .testTag(
                          EditGroupScreenTestTags.getTestTagForAvailableFriendCheckbox(
                              friend.username)))
        }
      }
}
