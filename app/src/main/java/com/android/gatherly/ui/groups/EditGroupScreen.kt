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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.R
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu_Goback
import com.android.gatherly.ui.todo.toDoTextFieldColors
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

  var searchQuery by remember { mutableStateOf("") }

  val screenPadding = dimensionResource(R.dimen.padding_screen)
  val smallPadding = dimensionResource(R.dimen.padding_small)
  val groupNameLabel = stringResource(R.string.group_name_bar_label)
  val groupNamePlaceholder = stringResource(R.string.group_name_bar_placeholder)
  val groupDescriptionLabel = stringResource(R.string.todos_description_field_label)
  val groupDescriptionPlaceholder = stringResource(R.string.group_description_bar_placeholder)
  val buttonHeight = dimensionResource(R.dimen.add_group_button_height)
  val buttonVerticalPadding = dimensionResource(R.dimen.add_group_button_vertical)
  val buttonCornerRadius = dimensionResource(R.dimen.friends_item_rounded_corner_shape)
  val buttonLabel = stringResource(R.string.edit_group_button)
  val buttonFontSize = dimensionResource(R.dimen.font_size_medium)
  val searchBarLabel = stringResource(R.string.friends_search_bar_label)
  val searchCornerShape = dimensionResource(R.dimen.rounded_corner_shape_large)
  val emptyFriendsMsg = stringResource(R.string.friends_empty_list_msg)
  val smallSpacing = dimensionResource(R.dimen.spacing_between_fields)
  val picDescription = stringResource(R.string.profile_picture_description)
  val picSize = dimensionResource(R.dimen.profile_pic_size_medium)
  val picBorder = dimensionResource(R.dimen.profile_pic_border)
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

  // Load the group when the screen receives a groupId
  LaunchedEffect(groupId) {
    if (groupId.isNotBlank()) {
      editGroupViewModel.loadGroup(groupId)
    }
  }

  // Observe save success
  LaunchedEffect(uiState.saveSuccess) {
    if (uiState.saveSuccess) {
      onSaved()
      editGroupViewModel.clearSaveSuccess()
    }
  }

  val isOwner = uiState.isOwner
  val isAdmin = uiState.isAdmin
  val currentUserId = uiState.currentUserId
  val creatorId = uiState.creatorId

  // Search query filtering for available friends
  val filteredAvailableFriends =
      remember(searchQuery, uiState.availableFriendsToAdd) {
        if (searchQuery.isBlank()) {
          uiState.availableFriendsToAdd
        } else {
          uiState.availableFriendsToAdd.filter {
            it.username.contains(searchQuery, ignoreCase = true)
          }
        }
      }

  // List of all current participants
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

  // Members of the group (if owner they don't see themselves, if admin they see everyone but can't
  // edit owner)
  val membersForList =
      remember(uiState.currentMemberProfiles, isOwner, currentUserId) {
        if (isOwner && currentUserId.isNotBlank()) {
          uiState.currentMemberProfiles.filter { it.uid != currentUserId }
        } else {
          uiState.currentMemberProfiles
        }
      }

  Scaffold(
      topBar = {
        TopNavigationMenu_Goback(
            selectedTab = Tab.EditGroup,
            modifier = Modifier.testTag(NavigationTestTags.TOP_NAVIGATION_MENU),
            goBack = goBack)
      },
      content = { padding ->
        LazyColumn(
            contentPadding = PaddingValues(vertical = smallPadding),
            modifier =
                Modifier.fillMaxWidth().padding(horizontal = screenPadding).padding(padding)) {

              // Group Name
              item {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { editGroupViewModel.onNameChanged(it) },
                    label = { Text(groupNameLabel) },
                    placeholder = { Text(groupNamePlaceholder) },
                    isError = uiState.nameError != null,
                    supportingText = {
                      uiState.nameError?.let {
                        Text(
                            it,
                            modifier = Modifier.testTag(EditGroupScreenTestTags.NAME_ERROR_MESSAGE))
                      }
                    },
                    colors = inputFieldColors,
                    modifier =
                        Modifier.fillMaxWidth().testTag(EditGroupScreenTestTags.GROUP_NAME_FIELD))
              }

              // Group Description
              item {
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { editGroupViewModel.onDescriptionChanged(it) },
                    label = { Text(groupDescriptionLabel) },
                    placeholder = { Text(groupDescriptionPlaceholder) },
                    colors = toDoTextFieldColors,
                    modifier =
                        Modifier.fillMaxWidth()
                            .testTag(EditGroupScreenTestTags.GROUP_DESCRIPTION_FIELD))
                Spacer(modifier = Modifier.height(smallSpacing))
              }

              // All current participants' profile pictures
              if (currentParticipants.isNotEmpty()) {
                item {
                  Spacer(modifier = Modifier.height(smallSpacing))
                  LazyRow {
                    items(currentParticipants) { participant ->
                      Image(
                          painter = profilePicturePainter(participant.profilePicture),
                          contentDescription = picDescription,
                          modifier =
                              Modifier.size(picSize)
                                  .clip(CircleShape)
                                  .border(
                                      picBorder,
                                      MaterialTheme.colorScheme.onBackground,
                                      CircleShape)
                                  .testTag(
                                      EditGroupScreenTestTags
                                          .getTestTagForSelectedFriendProfilePicture(
                                              participant.username)),
                          contentScale = ContentScale.Crop)
                      Spacer(modifier = Modifier.width(smallSpacing))
                    }
                  }
                  Spacer(modifier = Modifier.height(smallSpacing))
                  HorizontalDivider(
                      thickness = dividerThickness, color = MaterialTheme.colorScheme.primary)
                  Spacer(modifier = Modifier.height(smallSpacing))
                }
              }

              // ------------------------ Current members section ---------------------
              if (membersForList.isNotEmpty()) {
                item {
                  Spacer(modifier = Modifier.height(smallSpacing))
                  Text(
                      text = stringResource(R.string.group_members),
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.SemiBold)
                  Spacer(modifier = Modifier.height(smallSpacing))
                }

                items(membersForList) { member ->
                  val isCurrentUser = member.uid == currentUserId
                  val isOwnerMember = member.uid == creatorId

                  // Only owner can toggle admins
                  val showAdminToggle = isOwner

                  // Owner and admins can remove anyone except the owner
                  val canRemoveForAdmin = isAdmin && !isOwnerMember
                  val showRemoveToggle =
                      if (isOwner) {
                        true
                      } else {
                        canRemoveForAdmin
                      }

                  MemberItem(
                      member = member,
                      isAdmin = uiState.adminIds.contains(member.uid),
                      markedForRemoval = uiState.membersToRemove.contains(member.uid),
                      showAdminToggle = showAdminToggle,
                      showRemoveToggle = showRemoveToggle,
                      onToggleAdmin = { editGroupViewModel.onToggleAdmin(member.uid) },
                      onToggleRemove = { editGroupViewModel.onToggleRemoveMember(member.uid) })
                }

                item {
                  Spacer(modifier = Modifier.height(smallSpacing))
                  HorizontalDivider(
                      thickness = dividerThickness, color = MaterialTheme.colorScheme.primary)
                  Spacer(modifier = Modifier.height(smallSpacing))
                }

                item {
                  Spacer(modifier = Modifier.height(smallSpacing))
                  Text(
                      text = stringResource(R.string.group_available_friends),
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.SemiBold)
                  Spacer(modifier = Modifier.height(smallSpacing))
                }
              }

              // -------------------- Available friends section ----------------------------
              if (uiState.availableFriendsToAdd.isEmpty()) {
                item {
                  Text(
                      text = emptyFriendsMsg,
                      modifier =
                          Modifier.padding(screenPadding)
                              .testTag(EditGroupScreenTestTags.EMPTY_FRIENDS_MSG))
                }
              } else {
                // Search bar
                item {
                  OutlinedTextField(
                      value = searchQuery,
                      onValueChange = { searchQuery = it },
                      modifier =
                          Modifier.fillMaxWidth()
                              .padding(vertical = screenPadding)
                              .testTag(EditGroupScreenTestTags.SEARCH_FRIENDS_BAR),
                      shape = RoundedCornerShape(searchCornerShape),
                      placeholder = { Text(text = searchBarLabel) },
                      colors = inputFieldColors)
                  Spacer(modifier = Modifier.height(smallSpacing))
                }

                // Available friends list
                item {
                  Box(modifier = Modifier.fillMaxWidth().height(friendSectionHeight)) {
                    LazyColumn {
                      items(filteredAvailableFriends) { friend ->
                        AvailableFriendItem(
                            friend = friend,
                            isSelected = uiState.selectedNewFriendIds.contains(friend.uid),
                            onToggle = { editGroupViewModel.onNewFriendToggled(friend.uid) })
                      }
                    }
                  }
                }
              }

              // Save button
              item {
                Button(
                    onClick = { editGroupViewModel.saveGroup() },
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(buttonHeight)
                            .padding(vertical = buttonVerticalPadding)
                            .testTag(EditGroupScreenTestTags.BUTTON_SAVE_GROUP),
                    shape = RoundedCornerShape(buttonCornerRadius),
                    enabled = uiState.nameError == null && !uiState.isSaving,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary)) {
                      Text(
                          text = buttonLabel,
                          fontSize = buttonFontSize.value.sp,
                          fontWeight = FontWeight.Medium,
                          color = MaterialTheme.colorScheme.onSecondary)
                    }
              }

              // Delete group button (only seen by Owner)
              if (isOwner) {
                item {
                  Spacer(modifier = Modifier.height(smallSpacing))
                  Button(
                      onClick = { onDelete() },
                      modifier =
                          Modifier.fillMaxWidth()
                              .height(buttonHeight)
                              .padding(vertical = buttonVerticalPadding)
                              .testTag(EditGroupScreenTestTags.BUTTON_DELETE_GROUP),
                      shape = RoundedCornerShape(buttonCornerRadius),
                      colors =
                          ButtonDefaults.buttonColors(
                              containerColor = MaterialTheme.colorScheme.error)) {
                        Text(
                            text = stringResource(R.string.group_delete_button),
                            fontSize = buttonFontSize.value.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onError)
                      }
                }
              }
            }
      })
}

/** Row for a current group member with admin and removal toggles. */
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
            verticalAlignment = Alignment.CenterVertically,
        ) {
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

            // Admin title below the member name
            if (isAdmin) {
              Spacer(modifier = Modifier.height(smallSpacing / 2))
              Text(
                  text = stringResource(R.string.group_member_admin),
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.primary)
            }
          }

          Spacer(modifier = Modifier.width(regularSpacing))

          // ---------------- Toggles section ----------------
          Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.wrapContentWidth()) {
                // Admin star icon toggle (only for owner)
                if (showAdminToggle) {
                  Icon(
                      imageVector = Icons.Filled.Star,
                      contentDescription =
                          if (isAdmin) {
                            stringResource(R.string.group_member_admin)
                          } else {
                            stringResource(R.string.group_member_set_admin)
                          },
                      modifier =
                          Modifier.size(32.dp)
                              .testTag(
                                  EditGroupScreenTestTags.getTestTagForMemberAdminCheckbox(
                                      member.username))
                              .clickable { onToggleAdmin() },
                      tint =
                          if (isAdmin) {
                            MaterialTheme.colorScheme.primary
                          } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                          })
                }

                if (showAdminToggle && showRemoveToggle) {
                  Spacer(modifier = Modifier.width(smallSpacing))
                }
                if (showRemoveToggle) {
                  Checkbox(
                      checked = !markedForRemoval,
                      onCheckedChange = { _ -> onToggleRemove() },
                      modifier =
                          Modifier.wrapContentWidth()
                              .testTag(
                                  EditGroupScreenTestTags.getTestTagForMemberRemoveCheckbox(
                                      member.username)))
                }
              }
        }
      }
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
