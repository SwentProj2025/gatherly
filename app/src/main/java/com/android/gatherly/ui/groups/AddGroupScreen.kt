package com.android.gatherly.ui.groups

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.R
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu_Goback
import com.android.gatherly.ui.todo.toDoTextFieldColors
import com.android.gatherly.utils.profilePicturePainter

/** Object containing test tags for the AddGroupScreen and its components. */
object AddGroupScreenTestTags {
  const val BUTTON_CREATE_GROUP = "buttonCreateGroup"
  const val GROUP_NAME_FIELD = "groupNameField"
  const val GROUP_DESCRIPTION_FIELD = "groupDescriptionField"
  const val SEARCH_FRIENDS_BAR = "searchFriendsBar"
  const val EMPTY_LIST_MSG = "emptyListMessage"
  const val NAME_ERROR_MESSAGE = "nameErrorMessage"

  /**
   * Returns a unique test tag for the card representing a given friend.
   *
   * @param friend The friend whose test tag will be generated.
   * @return A string uniquely identifying the friend's card in the UI.
   */
  fun getTestTagForFriendItem(friend: String): String = "friendItem${friend}"

  /**
   * Returns a unique test tag for the username text representing a given friend.
   *
   * @param friend The friend whose test tag will be generated.
   * @return A string uniquely identifying the friend's username text in the UI.
   */
  fun getTestTagForFriendUsername(friend: String): String = "friendUsername${friend}"

  /**
   * Returns a unique test tag for the profile picture representing a given friend item.
   *
   * @param friend The friend whose test tag will be generated.
   * @return A string uniquely identifying the friend's profile picture in the UI.
   */
  fun getTestTagForFriendProfilePicture(friend: String): String = "friendProfilePicture${friend}"

  /**
   * Returns a unique test tag for the profile picture representing a given selected friend item.
   *
   * @param friend The friend whose test tag will be generated.
   * @return A string uniquely identifying the friend's profile picture in the UI.
   */
  fun getTestTagForSelectedFriendProfilePicture(friend: String): String =
      "selectedFriendProfilePicture${friend}"

  /**
   * Returns a unique test tag for the checkbox representing a given friend item.
   *
   * @param friend The friend whose test tag will be generated.
   * @return A string uniquely identifying the friend's checkbox in the UI.
   */
  fun getTestTagForFriendCheckbox(friend: String): String = "friendToggleCheckbox${friend}"
}

/**
 * Composable function representing the Add Group screen.
 *
 * @param addGroupViewModel The ViewModel managing the state of the AddGroupScreen.
 * @param goBack Callback function to navigate back to the previous screen.
 * @param onCreate Callback function to be invoked after a group is successfully created.
 */
@Composable
fun AddGroupScreen(
    addGroupViewModel: AddGroupViewModel = viewModel(factory = AddGroupViewModel.provideFactory()),
    goBack: () -> Unit = {},
    onCreate: () -> Unit = {},
) {

  val uiState by addGroupViewModel.uiState.collectAsState()

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
  val buttonLabel = stringResource(R.string.add_group_button_label)
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

  // Observe save success
  LaunchedEffect(uiState.saveSuccess) {
    if (uiState.saveSuccess) {
      onCreate()
      addGroupViewModel.clearSaveSuccess()
    }
  }

  Scaffold(
      topBar = {
        TopNavigationMenu_Goback(
            selectedTab = Tab.AddGroup,
            modifier = Modifier.testTag(NavigationTestTags.TOP_NAVIGATION_MENU),
            goBack = goBack)
      },
      content = { padding ->
        LazyColumn(
            contentPadding = PaddingValues(vertical = smallPadding),
            modifier =
                Modifier.fillMaxWidth().padding(horizontal = screenPadding).padding(padding)) {

              // Group Name Input Field
              item {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { addGroupViewModel.onNameChanged(it) },
                    label = { Text(groupNameLabel) },
                    placeholder = { Text(groupNamePlaceholder) },
                    isError = uiState.nameError != null,
                    supportingText = {
                      uiState.nameError?.let {
                        Text(
                            it,
                            modifier = Modifier.testTag(AddGroupScreenTestTags.NAME_ERROR_MESSAGE))
                      }
                    },
                    colors = inputFieldColors,
                    modifier =
                        Modifier.fillMaxWidth().testTag(AddGroupScreenTestTags.GROUP_NAME_FIELD))
              }

              // Group Description Input Field
              item {
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { addGroupViewModel.onDescriptionChanged(it) },
                    label = { Text(groupDescriptionLabel) },
                    placeholder = { Text(groupDescriptionPlaceholder) },
                    colors = toDoTextFieldColors,
                    modifier =
                        Modifier.fillMaxWidth()
                            .testTag(AddGroupScreenTestTags.GROUP_DESCRIPTION_FIELD))
              }

              if (uiState.friendsList.isEmpty()) {
                item {
                  // Message shown when there are no friends to display
                  Text(
                      text = emptyFriendsMsg,
                      modifier =
                          Modifier.padding(screenPadding)
                              .testTag(AddGroupScreenTestTags.EMPTY_LIST_MSG))
                }
              } else {
                item {
                  // Search Bar for filtering friends
                  OutlinedTextField(
                      value = searchQuery,
                      onValueChange = {
                        searchQuery = it
                        addGroupViewModel.filterFriends(searchQuery)
                      },
                      modifier =
                          Modifier.fillMaxWidth()
                              .padding(vertical = screenPadding)
                              .testTag(AddGroupScreenTestTags.SEARCH_FRIENDS_BAR),
                      shape = RoundedCornerShape(searchCornerShape),
                      placeholder = { Text(text = searchBarLabel) },
                      colors = inputFieldColors)
                  Spacer(modifier = Modifier.height(smallSpacing))
                }

                // Selected Friends Row
                item {
                  if (uiState.selectedFriends.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(smallSpacing))
                    LazyRow {
                      items(uiState.selectedFriends) { friend ->
                        Image(
                            painter = profilePicturePainter(friend.profilePicture),
                            contentDescription = picDescription,
                            modifier =
                                Modifier.size(picSize)
                                    .clip(CircleShape)
                                    .border(
                                        picBorder,
                                        MaterialTheme.colorScheme.onBackground,
                                        CircleShape)
                                    .testTag(
                                        AddGroupScreenTestTags
                                            .getTestTagForSelectedFriendProfilePicture(
                                                friend.username)),
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

                // Friends List Section
                item {
                  Box(modifier = Modifier.fillMaxWidth().height(friendSectionHeight)) {
                    LazyColumn {
                      items(uiState.friendsList) { friend ->
                        FriendItem(friend = friend, viewModel = addGroupViewModel)
                      }
                    }
                  }
                }
              }

              item {
                // Create Group Button
                Button(
                    onClick = { addGroupViewModel.saveGroup() },
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(buttonHeight)
                            .padding(vertical = buttonVerticalPadding)
                            .testTag(AddGroupScreenTestTags.BUTTON_CREATE_GROUP),
                    shape = RoundedCornerShape(buttonCornerRadius),
                    enabled = uiState.nameError == null,
                    colors = buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                      Text(
                          text = buttonLabel,
                          fontSize = buttonFontSize.value.sp,
                          fontWeight = FontWeight.Medium,
                          color = MaterialTheme.colorScheme.onSecondary)
                    }
              }
            }
      })
}

/**
 * Composable representing a single friend item with profile picture, username, and selection
 * checkbox.
 *
 * @param friend The profile of the friend to display.
 * @param viewModel The ViewModel managing the state of the AddGroupScreen.
 */
@Composable
private fun FriendItem(friend: Profile, viewModel: AddGroupViewModel) {

  val roundedCornerShape = dimensionResource(R.dimen.friends_item_card_rounded_corner_shape)
  val verticalPadding = dimensionResource(R.dimen.friends_item_card_padding_vertical)
  val cardPadding = dimensionResource(R.dimen.friends_item_card_padding)
  val picDescription = stringResource(R.string.profile_picture_description)
  val picSize = dimensionResource(R.dimen.profile_pic_size_regular)
  val smallSpacing = dimensionResource(R.dimen.spacing_between_fields_smaller_regular)
  val regularSpacing = dimensionResource(R.dimen.spacing_between_fields_regular)

  val uiState = viewModel.uiState.collectAsState()

  Card(
      shape = RoundedCornerShape(roundedCornerShape),
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant,
              contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
      modifier =
          Modifier.testTag(AddGroupScreenTestTags.getTestTagForFriendItem(friend.username))
              .fillMaxWidth()
              .padding(vertical = verticalPadding)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(cardPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          // Friend Profile Picture
          Image(
              painter = profilePicturePainter(friend.profilePicture),
              contentDescription = picDescription,
              modifier =
                  Modifier.size(picSize)
                      .clip(CircleShape)
                      .testTag(
                          AddGroupScreenTestTags.getTestTagForFriendProfilePicture(
                              friend.username)),
              contentScale = ContentScale.Crop)

          Spacer(modifier = Modifier.width(smallSpacing))

          // Friend Username
          Column(modifier = Modifier.weight(1f)) {
            Text(
                text = friend.username,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier =
                    Modifier.testTag(
                        AddGroupScreenTestTags.getTestTagForFriendUsername(friend.username)))
          }

          Spacer(modifier = Modifier.width(regularSpacing))

          // Selection Checkbox
          Checkbox(
              checked = uiState.value.selectedFriends.contains(friend),
              onCheckedChange = { viewModel.onFriendToggled(friend) },
              modifier =
                  Modifier.wrapContentWidth()
                      .testTag(AddGroupScreenTestTags.getTestTagForFriendCheckbox(friend.username)))
        }
      }
}
