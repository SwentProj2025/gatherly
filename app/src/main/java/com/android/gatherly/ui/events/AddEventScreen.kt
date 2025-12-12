package com.android.gatherly.ui.events

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.R
import com.android.gatherly.model.event.EventState
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu_Goback
import com.android.gatherly.ui.theme.GatherlyTheme
import com.android.gatherly.utils.DatePickerInputField
import com.android.gatherly.utils.GatherlyDatePicker
import com.android.gatherly.utils.GroupsActions
import com.android.gatherly.utils.GroupsFieldItem
import com.android.gatherly.utils.GroupsUiState
import com.android.gatherly.utils.ParticipantsActions
import com.android.gatherly.utils.ParticipantsFieldItem
import com.android.gatherly.utils.ParticipantsUiState
import com.android.gatherly.utils.TimeInputField
import kotlinx.coroutines.delay

object AddEventScreenTestTags {
  const val LAZY_LIST = "LAZY_LIST"
  const val INPUT_NAME = "EVENT_NAME"
  const val INPUT_DESCRIPTION = "EVENT_DESCRIPTION"
  const val INPUT_LOCATION = "EVENT_LOCATION"
  const val LOCATION_SUGGESTION = "EVENT_LOCATION"
  const val INPUT_DATE = "EVENT_DATE"
  const val INPUT_START = "EVENT_START_TIME"
  const val INPUT_END = "EVENT_END_TIME"
  const val BTN_SAVE = "EVENT_SAVE"
  const val ERROR_MESSAGE = "EVENT_ERROR_MESSAGE"
  const val INPUT_PARTICIPANT = "EVENT_PARTICIPANT_SEARCH"
  const val PARTICIPANT_MENU = "PARTICIPANT_MENU"
  const val LOCATION_MENU = "LOCATION_MENU"

  const val SWITCH_PUBLIC_PRIVATE_EVENT = "EVENT_SWITCH_PUBLIC_PRIVATE"

  const val BUTTON_PRIVATE_FRIENDS_EVENT = "EVENT_BUTTON_PRIVATE_FRIENDS"

  const val BUTTON_PRIVATE_GROUP_EVENT = "EVENT_BUTTON_PRIVATE_GROUP"
}

/**
 * Screen for adding an existing Event.
 *
 * @param onSave called after a successful save or deletion and navigation intent.
 * @param goBack called when back arrow is pressed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(
    addEventViewModel: AddEventViewModel = viewModel(factory = AddEventViewModel.provideFactory()),
    onSave: () -> Unit = {},
    goBack: () -> Unit = {},
) {

  val ui = addEventViewModel.uiState
  val context = LocalContext.current

  val screenPadding = dimensionResource(id = R.dimen.padding_screen)
  val fieldSpacing = dimensionResource(id = R.dimen.spacing_between_fields)
  val buttonSpacing = dimensionResource(id = R.dimen.spacing_between_buttons)

  val textFieldColors =
      TextFieldDefaults.colors(
          focusedContainerColor = MaterialTheme.colorScheme.background,
          unfocusedContainerColor = MaterialTheme.colorScheme.background,
          unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
          focusedTextColor = MaterialTheme.colorScheme.onBackground,
          errorTextColor = MaterialTheme.colorScheme.onBackground)

  // Local state for the dropdown visibility
  var showLocationDropdown by remember { mutableStateOf(false) }

  // Profile state for the dropdown visibility
  val showProfilesDropdown = remember { mutableStateOf(false) }

  // Group state for the dropdown visibility
  val showGroupDropDown = remember { mutableStateOf(false) }

  // Date state for the alert dialog visibility
  var showDatePicker by remember { mutableStateOf(false) }

  // Boolean values indicating the currently selected event type
  val isPrivateFriendsEvent = (ui.state == EventState.PRIVATE_FRIENDS)
  val isPrivateGroupEvent = (ui.state == EventState.PRIVATE_GROUP)
  val isPublicEvent = (ui.state == EventState.PUBLIC)

  // Toasts
  LaunchedEffect(ui.displayToast, ui.toastString) {
    if (ui.displayToast && ui.toastString != null) {
      Toast.makeText(context, ui.toastString, Toast.LENGTH_SHORT).show()
      addEventViewModel.clearErrorMsg()
    }
  }

  // Navigate back after save/delete
  LaunchedEffect(ui.backToOverview) {
    if (ui.backToOverview) {
      onSave()
    }
  }

  // Search location when input changes
  LaunchedEffect(ui.location) {
    if (ui.location.isNotBlank()) {
      delay(1000)
      addEventViewModel.searchLocationByString(ui.location)
    }
  }

  // Search participant when input changes
  LaunchedEffect(ui.participant, ui.state) {
    if (ui.participant.isNotBlank()) {
      if (isPrivateFriendsEvent) {
        addEventViewModel.searchFriendsProfileByString(ui.participant)
      } else if (isPublicEvent) {
        addEventViewModel.searchProfileByString(ui.participant)
      }
    }
  }

  // Search groups when input changes
  LaunchedEffect(ui.group) {
    if (ui.group.isNotBlank() && (isPrivateGroupEvent)) {
      addEventViewModel.searchGroupsNameByString(ui.group)
    }
  }

  val participantsUiState =
      ParticipantsUiState(
          participant = ui.participant,
          participants = ui.participants,
          suggestedProfiles = ui.suggestedProfiles,
          suggestedFriendsProfile = ui.suggestedFriendsProfile,
          state = ui.state)
  val groupsUiState =
      GroupsUiState(group = ui.group, groups = ui.groups, suggestedGroups = ui.suggestedGroups)

  val actions =
      ParticipantsActions(
          addParticipant = { profile -> addEventViewModel.addParticipant(profile) },
          deleteParticipant = { profileId -> addEventViewModel.deleteParticipant(profileId) },
          updateParticipant = { query -> addEventViewModel.updateParticipant(query) })
  val groupAction =
      GroupsActions(
          inviteGroup = { groupName -> addEventViewModel.inviteGroup(groupName) },
          removeGroup = { groupId -> addEventViewModel.removeGroup(groupId) },
          updateGroup = { query -> addEventViewModel.updateGroup(query) })

  Scaffold(
      topBar = {
        TopNavigationMenu_Goback(
            selectedTab = Tab.Events,
            modifier = Modifier.testTag(NavigationTestTags.TOP_NAVIGATION_MENU),
            goBack = goBack)
      }) { paddingVal ->
        LazyColumn(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingVal)
                    .padding(screenPadding)
                    .testTag(AddEventScreenTestTags.LAZY_LIST),
            verticalArrangement = Arrangement.spacedBy(fieldSpacing)) {

              // Switch button to choose between private and public
              item {
                Row {
                  EventStateSwitch(
                      checked = isPublicEvent,
                      onCheckedChange = { isPublic ->
                        if (isPublic) {
                          addEventViewModel.updateEventToPublic()
                        } else {
                          addEventViewModel.updateEventToPrivateFriends()
                        }
                      })

                  Text(
                      text =
                          if (isPublicEvent) stringResource(R.string.events_public_state_label)
                          else stringResource(R.string.events_private_state_label),
                      style = MaterialTheme.typography.bodyMedium)
                }
              }
              item {
                // Name
                OutlinedTextField(
                    value = ui.name,
                    onValueChange = { addEventViewModel.updateName(it) },
                    label = { Text(stringResource(R.string.events_title_field_label)) },
                    placeholder = { Text(stringResource(R.string.events_title_field_placeholder)) },
                    isError = ui.nameError,
                    supportingText = {
                      if (ui.nameError) {
                        Text(
                            "Name is required",
                            modifier = Modifier.testTag(AddEventScreenTestTags.ERROR_MESSAGE))
                      }
                    },
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth().testTag(AddEventScreenTestTags.INPUT_NAME))
              }

              item {
                // Description
                OutlinedTextField(
                    value = ui.description,
                    onValueChange = { addEventViewModel.updateDescription(it) },
                    label = { Text(stringResource(R.string.events_description_field_label)) },
                    placeholder = { Text(stringResource(R.string.events_description_placeholder)) },
                    isError = ui.descriptionError,
                    supportingText = {
                      if (ui.descriptionError) {
                        Text(
                            "Description is required",
                            modifier = Modifier.testTag(AddEventScreenTestTags.ERROR_MESSAGE))
                      }
                    },
                    colors = textFieldColors,
                    modifier =
                        Modifier.fillMaxWidth().testTag(AddEventScreenTestTags.INPUT_DESCRIPTION),
                    minLines = 3)
              }

              if (isPublicEvent) {

                item {
                  // Participants search with dropdown and + / - actions
                  ParticipantsFieldItem(
                      uiState = participantsUiState,
                      currentUserId = ui.currentUserId,
                      actions = actions,
                      textFieldColors = textFieldColors,
                      showProfilesDropdown = showProfilesDropdown)
                }
              } else {

                item {
                  // Buttons to choose the type of the event
                  PrivateEventButtons(
                      isFriendsOnly = isPrivateFriendsEvent,
                      onFriendsOnlySelected = { addEventViewModel.updateEventToPrivateFriends() },
                      onGroupSelected = { addEventViewModel.updateEventToPrivateGroup() })
                }

                if (isPrivateFriendsEvent) {
                  item {
                    // Participants friends search with dropdown and + / - actions
                    ParticipantsFieldItem(
                        uiState = participantsUiState,
                        currentUserId = ui.currentUserId,
                        actions = actions,
                        textFieldColors = textFieldColors,
                        showProfilesDropdown = showProfilesDropdown)
                  }
                } else {
                  item {
                    // Group search with dropdown and + / - actions
                    GroupsFieldItem(
                        uiState = groupsUiState,
                        actions = groupAction,
                        textFieldColors = textFieldColors,
                        showGroupsDropdown = showGroupDropDown)
                  }
                }
              }

              item {
                // Location with suggestions dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                  OutlinedTextField(
                      value = ui.location,
                      onValueChange = {
                        addEventViewModel.updateLocation(it)
                        showLocationDropdown = it.isNotBlank()
                      },
                      label = { Text(stringResource(R.string.events_location_field_label)) },
                      placeholder = {
                        Text(stringResource(R.string.events_location_field_placeholder))
                      },
                      colors = textFieldColors,
                      modifier =
                          Modifier.fillMaxWidth().testTag(AddEventScreenTestTags.INPUT_LOCATION))

                  DropdownMenu(
                      expanded = showLocationDropdown && ui.suggestedLocations.isNotEmpty(),
                      onDismissRequest = { showLocationDropdown = false },
                      properties = PopupProperties(focusable = false),
                      containerColor = MaterialTheme.colorScheme.surfaceVariant,
                      modifier =
                          Modifier.testTag(AddEventScreenTestTags.LOCATION_MENU)
                              .fillMaxWidth()
                              .height(200.dp)) {
                        ui.suggestedLocations.take(3).forEach { loc ->
                          DropdownMenuItem(
                              text = {
                                Text(
                                    text =
                                        loc.name.take(40) + if (loc.name.length > 40) "..." else "",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                              },
                              onClick = {
                                addEventViewModel.selectLocation(loc)
                                showLocationDropdown = false
                              },
                              modifier =
                                  Modifier.testTag(AddEventScreenTestTags.LOCATION_SUGGESTION))
                        }
                        if (ui.suggestedLocations.size > 3) {
                          DropdownMenuItem(
                              text = {
                                Text("More...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                              },
                              onClick = {})
                        }
                      }
                }
              }

              item {
                // Date
                DatePickerInputField(
                    value = ui.date,
                    label = stringResource(R.string.events_date_field_label),
                    isErrorMessage = if (!ui.dateError) null else "Invalid format or past date",
                    onClick = { showDatePicker = true },
                    colors = textFieldColors,
                    testTag =
                        Pair(
                            AddEventScreenTestTags.INPUT_DATE,
                            AddEventScreenTestTags.ERROR_MESSAGE),
                )
              }

              item {
                // Start time
                TimeInputField(
                    initialTime = ui.startTime,
                    onTimeChanged = { addEventViewModel.updateStartTime(it) },
                    label = stringResource(R.string.events_start_time_field_label),
                    dueTimeError = ui.startTimeError,
                    textFieldColors = textFieldColors,
                    testTagInput = AddEventScreenTestTags.INPUT_START,
                    testTagErrorMessage = AddEventScreenTestTags.ERROR_MESSAGE,
                    isStarting = true)
              }

              item {
                // End time
                TimeInputField(
                    initialTime = ui.endTime,
                    onTimeChanged = { addEventViewModel.updateEndTime(it) },
                    dueTimeError = ui.endTimeError,
                    label = stringResource(R.string.events_end_time_field_label),
                    textFieldColors = textFieldColors,
                    testTagInput = AddEventScreenTestTags.INPUT_END,
                    testTagErrorMessage = AddEventScreenTestTags.ERROR_MESSAGE,
                    isStarting = false)
              }

              item { Spacer(modifier = Modifier.height(buttonSpacing)) }

              item {
                // Save
                Button(
                    onClick = { addEventViewModel.saveEvent() },
                    modifier = Modifier.fillMaxWidth().testTag(AddEventScreenTestTags.BTN_SAVE),
                    colors = buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    enabled =
                        !ui.nameError &&
                            !ui.descriptionError &&
                            !ui.dateError &&
                            !ui.startTimeError &&
                            !ui.endTimeError &&
                            !ui.isSaving) {
                      Text(
                          text =
                              if (ui.isSaving) {
                                stringResource(R.string.saving)
                              } else {
                                stringResource(R.string.settings_save)
                              },
                          color = MaterialTheme.colorScheme.onSecondary)
                    }
              }
            }

        GatherlyDatePicker(
            show = showDatePicker,
            initialDate = ui.date,
            onDateSelected = { selectedDate -> addEventViewModel.updateDate(selectedDate) },
            onDismiss = { showDatePicker = false })
      }
}

@Preview(showBackground = true)
@Composable
fun AddEventScreenPreview() {
  GatherlyTheme(darkTheme = true) { AddEventScreen() }
}

/**
 * Helper composable function: Create a switch to choose between private or public event
 *
 * @param checked boolean to know if the user wants to create a public event
 * @param onCheckedChange function to apply depending on the chosen event
 */
@Composable
private fun EventStateSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
  Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      thumbContent =
          if (checked) {
            {
              Icon(
                  imageVector = Icons.Filled.LockOpen,
                  contentDescription = "Public Event",
                  modifier = Modifier.size(19.dp))
            }
          } else {
            {
              Icon(
                  imageVector = Icons.Filled.Lock,
                  contentDescription = "Private Event",
                  modifier = Modifier.size(19.dp))
            }
          },
      modifier =
          Modifier.size(60.dp, 40.dp).testTag(AddEventScreenTestTags.SWITCH_PUBLIC_PRIVATE_EVENT),
  )
}

/**
 * Helper composable function: create 2 buttons to let the user choose between private friends or
 * private group event.
 *
 * @param isFriendsOnly boolean to know if the user choose or not private friends only event
 * @param onFriendsOnlySelected function to apply when the user choose private friends only event
 * @param onGroupSelected function to apply when the user choose private group event
 */
@Composable
private fun PrivateEventButtons(
    isFriendsOnly: Boolean,
    onFriendsOnlySelected: () -> Unit,
    onGroupSelected: () -> Unit
) {
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .padding(vertical = dimensionResource(R.dimen.events_filter_bar_vertical_size)),
      horizontalArrangement = Arrangement.SpaceEvenly) {
        // Button private friends only event
        Button(
            onClick = onFriendsOnlySelected,
            colors =
                buttonColors(
                    containerColor =
                        if (isFriendsOnly) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor =
                        if (isFriendsOnly) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.background),
            shape = RoundedCornerShape(dimensionResource(R.dimen.rounded_corner_shape_large)),
            modifier =
                Modifier.testTag(AddEventScreenTestTags.BUTTON_PRIVATE_FRIENDS_EVENT)
                    .height(dimensionResource(R.dimen.events_filter_button_height))) {
              Text(text = stringResource(R.string.events_private_friends_label))
            }

        // Button private group event
        Button(
            onClick = onGroupSelected,
            colors =
                buttonColors(
                    containerColor =
                        if (!isFriendsOnly) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor =
                        if (!isFriendsOnly) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.background),
            shape = RoundedCornerShape(dimensionResource(R.dimen.rounded_corner_shape_large)),
            modifier =
                Modifier.testTag(AddEventScreenTestTags.BUTTON_PRIVATE_GROUP_EVENT)
                    .height(dimensionResource(R.dimen.events_filter_button_height))) {
              Text(text = stringResource(R.string.events_private_group_label))
            }
      }
}
