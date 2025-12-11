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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.android.gatherly.utils.GatherlyAlertDialog
import com.android.gatherly.utils.GatherlyDatePicker
import com.android.gatherly.utils.GroupsActions
import com.android.gatherly.utils.GroupsFieldItem
import com.android.gatherly.utils.GroupsUiState
import com.android.gatherly.utils.ParticipantsActions
import com.android.gatherly.utils.ParticipantsFieldItem
import com.android.gatherly.utils.ParticipantsUiState
import com.android.gatherly.utils.TimeInputField
import kotlin.collections.forEach
import kotlinx.coroutines.delay

object EditEventsScreenTestTags {
  const val INPUT_NAME = "EVENT_NAME"
  const val INPUT_DESCRIPTION = "EVENT_DESCRIPTION"
  const val INPUT_LOCATION = "EVENT_LOCATION"
  const val INPUT_DATE = "EVENT_DATE"
  const val INPUT_START = "EVENT_START_TIME"
  const val INPUT_END = "EVENT_END_TIME"
  const val BTN_SAVE = "EVENT_SAVE"
  const val BTN_DELETE = "EVENT_DELETE"
  const val ERROR_MESSAGE = "EVENT_ERROR_MESSAGE"
  const val INPUT_PARTICIPANT = "EVENT_PARTICIPANT_SEARCH"
  const val PARTICIPANT_MENU = "PARTICIPANT_MENU"
  const val LOCATION_MENU = "LOCATION_MENU"
  const val LIST = "LIST"
  const val SWITCH_PUBLIC_PRIVATE_EVENT = "EVENT_SWITCH_PUBLIC_PRIVATE"
}

/**
 * Screen for editing an existing Event.
 *
 * @param eventId id of the event to load and edit.
 * @param editEventsViewModel The ViewModel managing the state and logic for the Edit Event screen,
 *   instantiated with a factory provider defined in the ViewModel's companion object.
 * @param onSave called after a successful save or deletion and navigation intent.
 * @param goBack called when back arrow is pressed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventsScreen(
    eventId: String = "",
    editEventsViewModel: EditEventsViewModel =
        viewModel(factory = EditEventsViewModel.provideFactory()),
    onSave: () -> Unit = {},
    goBack: () -> Unit = {},
) {
  // Load event once
  LaunchedEffect(eventId) { editEventsViewModel.setEventValues(eventId) }

  val ui = editEventsViewModel.uiState
  val context = LocalContext.current
  val shouldShowDialog = remember { mutableStateOf(false) }

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

  // Warning popup to be sure that the user wants to make his event public
  var showWarningPublicEvent by remember { mutableStateOf(false) }

  // Boolean values indicating the currently selected event type
  val isPrivateFriendsEvent = (ui.state == EventState.PRIVATE_FRIENDS)
  val isPrivateGroupEvent = (ui.state == EventState.PRIVATE_GROUP)
  val isPublicEvent = (ui.state == EventState.PUBLIC)

  val participantsUiState =
      ParticipantsUiState(
          participant = ui.participant,
          participants = ui.participants,
          suggestedProfiles = ui.suggestedProfiles,
          suggestedFriendsProfile = ui.suggestedFriendsProfile,
          state = ui.state)
  val actions =
      ParticipantsActions(
          addParticipant = { profile -> editEventsViewModel.addParticipant(profile) },
          deleteParticipant = { participantId ->
            editEventsViewModel.deleteParticipant(participantId)
          },
          updateParticipant = { query -> editEventsViewModel.updateParticipant(query) })

  val groupsUiState =
      GroupsUiState(group = ui.group, groups = ui.groups, suggestedGroups = ui.suggestedGroups)
  val groupAction =
      GroupsActions(
          inviteGroup = { groupName -> editEventsViewModel.inviteGroup(groupName) },
          removeGroup = { groupId -> editEventsViewModel.removeGroup(groupId) },
          updateGroup = { query -> editEventsViewModel.updateGroup(query) })

  // Toasts
  LaunchedEffect(ui.displayToast, ui.toastString) {
    if (ui.displayToast && ui.toastString != null) {
      Toast.makeText(context, ui.toastString, Toast.LENGTH_SHORT).show()
      editEventsViewModel.clearErrorMsg()
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
      editEventsViewModel.searchLocationByString(ui.location)
    }
  }

  // Search participant when input changes
  LaunchedEffect(ui.participant, ui.state) {
    if (ui.participant.isNotBlank()) {
      delay(1000)
      if (isPrivateFriendsEvent) {
        editEventsViewModel.searchFriendsProfileByString(ui.participant)
      } else if (isPublicEvent) {
        editEventsViewModel.searchProfileByString(ui.participant)
      }
    }
  }

  // Search groups when input changes
  LaunchedEffect(ui.group) {
    if (ui.group.isNotBlank() && (isPrivateGroupEvent)) {
      delay(1000)
      editEventsViewModel.searchGroupsNameByString(ui.group)
    }
  }

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
                    .testTag(EditEventsScreenTestTags.LIST),
            verticalArrangement = Arrangement.spacedBy(fieldSpacing)) {
              if (!isPublicEvent) {
                item {
                  // Switch button to go to public event
                  Row {
                    ElevatedButton(
                        onClick = { showWarningPublicEvent = true },
                        modifier =
                            Modifier.testTag(
                                EditEventsScreenTestTags.SWITCH_PUBLIC_PRIVATE_EVENT)) {
                          Icon(
                              imageVector = Icons.Filled.LockOpen,
                              contentDescription = "Public Event",
                              modifier = Modifier.size(19.dp))
                        }

                    Text(text = "Make the event public", style = MaterialTheme.typography.bodySmall)
                  }
                }
              }

              item {
                // Name
                OutlinedTextField(
                    value = ui.name,
                    onValueChange = { editEventsViewModel.updateName(it) },
                    label = { Text(stringResource(R.string.events_title_field_label)) },
                    placeholder = { Text(stringResource(R.string.events_title_field_placeholder)) },
                    isError = ui.nameError,
                    supportingText = {
                      if (ui.nameError) {
                        Text(
                            "Name is required",
                            modifier = Modifier.testTag(EditEventsScreenTestTags.ERROR_MESSAGE))
                      }
                    },
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth().testTag(EditEventsScreenTestTags.INPUT_NAME))
              }

              item {
                // Description
                OutlinedTextField(
                    value = ui.description,
                    onValueChange = { editEventsViewModel.updateDescription(it) },
                    label = { Text(stringResource(R.string.events_description_field_label)) },
                    placeholder = { Text(stringResource(R.string.events_description_placeholder)) },
                    isError = ui.descriptionError,
                    supportingText = {
                      if (ui.descriptionError) {
                        Text(
                            "Description is required",
                            modifier = Modifier.testTag(EditEventsScreenTestTags.ERROR_MESSAGE))
                      }
                    },
                    colors = textFieldColors,
                    modifier =
                        Modifier.fillMaxWidth().testTag(EditEventsScreenTestTags.INPUT_DESCRIPTION),
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
                        editEventsViewModel.updateLocation(it)
                        showLocationDropdown = it.isNotBlank()
                      },
                      label = { Text(stringResource(R.string.events_location_field_label)) },
                      placeholder = {
                        Text(stringResource(R.string.events_location_field_placeholder))
                      },
                      colors = textFieldColors,
                      modifier =
                          Modifier.fillMaxWidth().testTag(EditEventsScreenTestTags.INPUT_LOCATION))

                  DropdownMenu(
                      expanded = showLocationDropdown && ui.suggestedLocations.isNotEmpty(),
                      onDismissRequest = { showLocationDropdown = false },
                      properties = PopupProperties(focusable = false),
                      containerColor = MaterialTheme.colorScheme.surfaceVariant,
                      modifier =
                          Modifier.testTag(EditEventsScreenTestTags.LOCATION_MENU)
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
                                editEventsViewModel.selectLocation(loc)
                                showLocationDropdown = false
                              },
                              modifier = Modifier.testTag(EditEventsScreenTestTags.INPUT_LOCATION))
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
                            EditEventsScreenTestTags.INPUT_DATE,
                            EditEventsScreenTestTags.ERROR_MESSAGE))
              }

              item {
                // Start time
                TimeInputField(
                    initialTime = ui.startTime,
                    onTimeChanged = { editEventsViewModel.updateStartTime(it) },
                    dueTimeError = ui.startTimeError,
                    label = stringResource(R.string.events_start_time_field_label),
                    textFieldColors = textFieldColors,
                    testTagInput = EditEventsScreenTestTags.INPUT_START,
                    testTagErrorMessage = EditEventsScreenTestTags.ERROR_MESSAGE,
                    isStarting = true)
              }

              item {
                // End time
                TimeInputField(
                    initialTime = ui.endTime,
                    onTimeChanged = { editEventsViewModel.updateEndTime(it) },
                    dueTimeError = ui.endTimeError,
                    label = stringResource(R.string.events_end_time_field_label),
                    textFieldColors = textFieldColors,
                    testTagInput = EditEventsScreenTestTags.INPUT_END,
                    testTagErrorMessage = EditEventsScreenTestTags.ERROR_MESSAGE,
                    isStarting = false)
              }

              item {
                Spacer(modifier = Modifier.height(buttonSpacing))

                // Save
                Button(
                    onClick = { editEventsViewModel.saveEvent() },
                    modifier = Modifier.fillMaxWidth().testTag(EditEventsScreenTestTags.BTN_SAVE),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary),
                    enabled =
                        !ui.nameError &&
                            !ui.descriptionError &&
                            !ui.dateError &&
                            !ui.startTimeError &&
                            !ui.endTimeError &&
                            !ui.isLoading) {
                      Text(
                          text =
                              if (ui.isLoading) {
                                stringResource(R.string.saving)
                              } else {
                                stringResource(R.string.settings_save)
                              },
                          color = MaterialTheme.colorScheme.onSecondary)
                    }
              }

              item {
                // Delete
                TextButton(
                    onClick = { shouldShowDialog.value = true },
                    modifier = Modifier.fillMaxWidth().testTag(EditEventsScreenTestTags.BTN_DELETE),
                    colors =
                        ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error)) {
                      Icon(
                          imageVector = Icons.Filled.DeleteForever,
                          contentDescription = "Delete event",
                          tint = MaterialTheme.colorScheme.error)
                      Text(
                          "Delete",
                          modifier = Modifier.padding(start = buttonSpacing),
                          color = MaterialTheme.colorScheme.error)
                    }
              }
            }
        if (shouldShowDialog.value) {
          GatherlyAlertDialog(
              titleText = stringResource(R.string.events_delete_warning),
              bodyText = stringResource(R.string.events_delete_warning_text),
              dismissText = stringResource(R.string.cancel),
              confirmText = stringResource(R.string.delete),
              onDismiss = { shouldShowDialog.value = false },
              onConfirm = {
                editEventsViewModel.deleteEvent()
                shouldShowDialog.value = false
              },
              isImportantWarning = true)
        }

        if (showWarningPublicEvent) {
          GatherlyAlertDialog(
              titleText = "Make the event public",
              bodyText = "This action is unreversible, the event will be open to everyone",
              dismissText = stringResource(R.string.cancel),
              confirmText = "Make it public",
              onDismiss = { showWarningPublicEvent = false },
              onConfirm = {
                editEventsViewModel.updatePrivateEventToPublicEvent()
                showWarningPublicEvent = false
              })
        }
        GatherlyDatePicker(
            show = showDatePicker,
            initialDate = ui.date,
            onDateSelected = { selectedDate -> editEventsViewModel.updateDate(selectedDate) },
            onDismiss = { showDatePicker = false })
      }
}

@Preview
@Composable
fun EditEventsScreenPreview() {
  GatherlyTheme(darkTheme = true) { EditEventsScreen() }
}
