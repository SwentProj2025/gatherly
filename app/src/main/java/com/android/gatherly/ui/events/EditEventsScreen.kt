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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.R
import com.android.gatherly.model.event.EventState
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu_Goback
import com.android.gatherly.ui.theme.GatherlyTheme
import com.android.gatherly.utils.DatePickerInputField
import com.android.gatherly.utils.EventAlarmScheduler
import com.android.gatherly.utils.GatherlyAlertDialog
import com.android.gatherly.utils.GatherlyAlertDialogActions
import com.android.gatherly.utils.GatherlyDatePicker
import com.android.gatherly.utils.GroupsActions
import com.android.gatherly.utils.GroupsFieldItem
import com.android.gatherly.utils.GroupsUiState
import com.android.gatherly.utils.LoadingAnimation
import com.android.gatherly.utils.ParticipantsActions
import com.android.gatherly.utils.ParticipantsFieldItem
import com.android.gatherly.utils.ParticipantsUiState
import com.android.gatherly.utils.TimeInputField
import kotlin.collections.forEach
import kotlinx.coroutines.delay

/** Test tags for the EditEventScreen composable, used to identify UI elements during testing. */
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
 * @param editEventViewModel The ViewModel managing the state and logic for the Edit Event screen,
 *   instantiated with a factory provider defined in the ViewModel's companion object.
 * @param onSave called after a successful save or deletion and navigation intent.
 * @param goBack called when back arrow is pressed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventsScreen(
    eventId: String = "",
    editEventViewModel: EditEventViewModel =
        viewModel(factory = EditEventViewModel.provideFactory()),
    onSave: () -> Unit = {},
    goBack: () -> Unit = {},
) {
  // Load event once
  LaunchedEffect(eventId) { editEventViewModel.setEventValues(eventId) }

  // UI state
  val ui = editEventViewModel.uiState
  // Local context
  val context = LocalContext.current

  // Dimensions values from resources
  val screenPadding = dimensionResource(id = R.dimen.padding_screen)
  val fieldSpacing = dimensionResource(id = R.dimen.spacing_between_fields)
  val buttonSpacing = dimensionResource(id = R.dimen.spacing_between_buttons)
  val suggestionsLength = integerResource(id = R.integer.events_location_suggestion_length)

  // Text field colors
  val textFieldColors =
      TextFieldDefaults.colors(
          focusedContainerColor = MaterialTheme.colorScheme.background,
          unfocusedContainerColor = MaterialTheme.colorScheme.background,
          unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
          focusedTextColor = MaterialTheme.colorScheme.onBackground,
          errorTextColor = MaterialTheme.colorScheme.onBackground)

  // Warning popup to be sure that the user wants to delete the event
  val showDeleteEventDialog = remember { mutableStateOf(false) }

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

  // Participants UI state and actions
  val participantsUiState =
      ParticipantsUiState(
          participant = ui.participant,
          participants = ui.participants,
          suggestedProfiles = ui.suggestedProfiles,
          suggestedFriendsProfile = ui.suggestedFriendsProfile,
          state = ui.state)
  val actions =
      ParticipantsActions(
          addParticipant = { profile -> editEventViewModel.addParticipant(profile) },
          deleteParticipant = { participantId ->
            editEventViewModel.deleteParticipant(participantId)
          },
          updateParticipant = { query -> editEventViewModel.updateParticipant(query) })

  // Groups UI state and actions
  val groupsUiState =
      GroupsUiState(group = ui.group, groups = ui.groups, suggestedGroups = ui.suggestedGroups)
  val groupAction =
      GroupsActions(
          inviteGroup = { groupName -> editEventViewModel.inviteGroup(groupName) },
          removeGroup = { groupId -> editEventViewModel.removeGroup(groupId) },
          updateGroup = { query -> editEventViewModel.updateGroup(query) })

  // Toasts
  LaunchedEffect(ui.displayToast, ui.toastString) {
    if (ui.displayToast && ui.toastString != null) {
      Toast.makeText(context, ui.toastString, Toast.LENGTH_SHORT).show()
      editEventViewModel.clearErrorMsg()
    }
  }

  // Navigate back after save/delete
  LaunchedEffect(ui.backToOverview) {
    if (ui.backToOverview) {
      if (ui.editedEvent != null) {
        if (ui.eventDeleted) {
          EventAlarmScheduler(context)
              .cancelEventReminder(eventId = ui.editedEvent.id, userId = ui.editedEvent.creatorId)
        } else {
          EventAlarmScheduler(context)
              .scheduleEventReminder(
                  userId = ui.editedEvent.creatorId,
                  eventId = ui.editedEvent.id,
                  eventDate = ui.editedEvent.date,
                  eventStartTime = ui.editedEvent.startTime)
        }
      }
      onSave()
    }
  }

  // Search location when input changes
  LaunchedEffect(ui.location) {
    if (ui.location.isNotBlank()) {
      delay(1000)
      editEventViewModel.searchLocationByString(ui.location)
    }
  }

  // Search participant when input changes
  LaunchedEffect(ui.participant, ui.state) {
    if (ui.participant.isNotBlank()) {
      if (isPrivateFriendsEvent) {
        editEventViewModel.searchFriendsProfileByString(ui.participant)
      } else if (isPublicEvent) {
        editEventViewModel.searchProfileByString(ui.participant)
      }
    }
  }

  // Search groups when input changes
  LaunchedEffect(ui.group) {
    if (ui.group.isNotBlank() && (isPrivateGroupEvent)) {
      editEventViewModel.searchGroupsNameByString(ui.group)
    }
  }

  // --- Main scaffold for the Edit Event screen ---
  Scaffold(
      topBar = {
        TopNavigationMenu_Goback(
            selectedTab = Tab.EventsOverview,
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
              if (ui.isLoading) {
                item {
                  LoadingAnimation(stringResource(R.string.loading_event_message), paddingVal)
                }
              } else {
                if (!isPublicEvent) {
                  // -- Switch to public event button --
                  item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      ElevatedButton(
                          onClick = { showWarningPublicEvent = true },
                          modifier =
                              Modifier.testTag(
                                  EditEventsScreenTestTags.SWITCH_PUBLIC_PRIVATE_EVENT)) {
                            Icon(
                                imageVector = Icons.Filled.LockOpen,
                                contentDescription = "Public Event",
                                modifier =
                                    Modifier.size(dimensionResource(R.dimen.icons_size_small)))
                          }

                      Spacer(
                          modifier =
                              Modifier.width(
                                  dimensionResource(
                                      R.dimen.spacing_between_fields_smaller_regular)))

                      Text(
                          text = stringResource(R.string.events_edit_private_to_public_label),
                          style = MaterialTheme.typography.bodySmall)
                    }
                  }
                }

                // -- Input: Event's Name --
                item {
                  OutlinedTextField(
                      value = ui.name,
                      onValueChange = { editEventViewModel.updateName(it) },
                      label = { Text(stringResource(R.string.events_title_field_label)) },
                      placeholder = {
                        Text(stringResource(R.string.events_title_field_placeholder))
                      },
                      isError = ui.nameError,
                      supportingText = {
                        if (ui.nameError) {
                          Text(
                              stringResource(R.string.events_error_name_message),
                              modifier = Modifier.testTag(EditEventsScreenTestTags.ERROR_MESSAGE))
                        }
                      },
                      colors = textFieldColors,
                      modifier =
                          Modifier.fillMaxWidth().testTag(EditEventsScreenTestTags.INPUT_NAME))
                }

                // -- Input: Event's Description --
                item {
                  OutlinedTextField(
                      value = ui.description,
                      onValueChange = { editEventViewModel.updateDescription(it) },
                      label = { Text(stringResource(R.string.events_description_field_label)) },
                      placeholder = {
                        Text(stringResource(R.string.events_description_placeholder))
                      },
                      isError = ui.descriptionError,
                      supportingText = {
                        if (ui.descriptionError) {
                          Text(
                              stringResource(R.string.events_error_description_message),
                              modifier = Modifier.testTag(EditEventsScreenTestTags.ERROR_MESSAGE))
                        }
                      },
                      colors = textFieldColors,
                      modifier =
                          Modifier.fillMaxWidth()
                              .testTag(EditEventsScreenTestTags.INPUT_DESCRIPTION),
                      minLines = integerResource(R.integer.events_description_min_lines))
                }

                if (isPublicEvent) {
                  // -- Input: Participants search with dropdown and + / - actions --
                  item {
                    ParticipantsFieldItem(
                        uiState = participantsUiState,
                        currentUserId = ui.currentUserId,
                        actions = actions,
                        textFieldColors = textFieldColors,
                        showProfilesDropdown = showProfilesDropdown)
                  }
                } else {

                  if (isPrivateFriendsEvent) {
                    // -- Input: Participants friends search with dropdown and + / - actions --
                    item {
                      ParticipantsFieldItem(
                          uiState = participantsUiState,
                          currentUserId = ui.currentUserId,
                          actions = actions,
                          textFieldColors = textFieldColors,
                          showProfilesDropdown = showProfilesDropdown)
                    }
                  } else {
                    // -- Input: Groups search with dropdown and + / - actions --
                    item {
                      GroupsFieldItem(
                          uiState = groupsUiState,
                          actions = groupAction,
                          textFieldColors = textFieldColors,
                          showGroupsDropdown = showGroupDropDown)
                    }
                  }
                }

                // -- Input: Event's Location with dropdown --
                item {
                  Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = ui.location,
                        onValueChange = {
                          editEventViewModel.updateLocation(it)
                          showLocationDropdown = it.isNotBlank()
                        },
                        label = { Text(stringResource(R.string.events_location_field_label)) },
                        placeholder = {
                          Text(stringResource(R.string.events_location_field_placeholder))
                        },
                        colors = textFieldColors,
                        modifier =
                            Modifier.fillMaxWidth()
                                .testTag(EditEventsScreenTestTags.INPUT_LOCATION))

                    DropdownMenu(
                        expanded = showLocationDropdown && ui.suggestedLocations.isNotEmpty(),
                        onDismissRequest = { showLocationDropdown = false },
                        properties = PopupProperties(focusable = false),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier =
                            Modifier.testTag(EditEventsScreenTestTags.LOCATION_MENU)
                                .fillMaxWidth()
                                .height(dimensionResource(R.dimen.dropdown_height))) {
                          ui.suggestedLocations
                              .take(
                                  integerResource(R.integer.events_location_number_of_suggestions))
                              .forEach { loc ->
                                DropdownMenuItem(
                                    text = {
                                      Text(
                                          text =
                                              loc.name.take(suggestionsLength) +
                                                  if (loc.name.length > suggestionsLength)
                                                      stringResource(
                                                          R.string.location_suggestion_loading)
                                                  else stringResource(R.string.empty_string),
                                          color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    },
                                    onClick = {
                                      editEventViewModel.selectLocation(loc)
                                      showLocationDropdown = false
                                    },
                                    modifier =
                                        Modifier.testTag(EditEventsScreenTestTags.INPUT_LOCATION))
                              }
                          if (ui.suggestedLocations.size >
                              integerResource(R.integer.events_location_number_of_suggestions)) {
                            DropdownMenuItem(
                                text = {
                                  Text(
                                      stringResource(R.string.location_suggestion_more),
                                      color = MaterialTheme.colorScheme.onSurfaceVariant)
                                },
                                onClick = {})
                          }
                        }
                  }
                }

                // -- Input: Event's Date --
                item {
                  DatePickerInputField(
                      value = ui.date,
                      label = stringResource(R.string.events_date_field_label),
                      isErrorMessage =
                          if (!ui.dateError) null
                          else stringResource(R.string.error_date_picker_message),
                      onClick = { showDatePicker = true },
                      colors = textFieldColors,
                      testTag =
                          Pair(
                              EditEventsScreenTestTags.INPUT_DATE,
                              EditEventsScreenTestTags.ERROR_MESSAGE))
                }

                // -- Input: Event's Start Time --
                item {
                  TimeInputField(
                      initialTime = ui.startTime,
                      onTimeChanged = { editEventViewModel.updateStartTime(it) },
                      dueTimeError = ui.startTimeError,
                      label = stringResource(R.string.events_start_time_field_label),
                      textFieldColors = textFieldColors,
                      testTagInput = EditEventsScreenTestTags.INPUT_START,
                      testTagErrorMessage = EditEventsScreenTestTags.ERROR_MESSAGE,
                      isStarting = true)
                }

                // -- Input: Event's End Time --
                item {
                  TimeInputField(
                      initialTime = ui.endTime,
                      onTimeChanged = { editEventViewModel.updateEndTime(it) },
                      dueTimeError = ui.endTimeError,
                      label = stringResource(R.string.events_end_time_field_label),
                      textFieldColors = textFieldColors,
                      testTagInput = EditEventsScreenTestTags.INPUT_END,
                      testTagErrorMessage = EditEventsScreenTestTags.ERROR_MESSAGE,
                      isStarting = false)
                }

                item {
                  Spacer(modifier = Modifier.height(buttonSpacing))

                  // -- Save button --
                  Button(
                      onClick = { editEventViewModel.saveEvent() },
                      modifier = Modifier.fillMaxWidth().testTag(EditEventsScreenTestTags.BTN_SAVE),
                      colors =
                          ButtonDefaults.buttonColors(
                              containerColor = MaterialTheme.colorScheme.secondary),
                      enabled =
                          !ui.nameError &&
                              !ui.descriptionError &&
                              !ui.dateError &&
                              !ui.startTimeError &&
                              !ui.endTimeError) {
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

                // -- Delete Event button --
                item {
                  TextButton(
                      onClick = { showDeleteEventDialog.value = true },
                      modifier =
                          Modifier.fillMaxWidth().testTag(EditEventsScreenTestTags.BTN_DELETE),
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
            }

        // -- Delete Event Confirmation Dialog --
        if (showDeleteEventDialog.value) {
          GatherlyAlertDialog(
              titleText = stringResource(R.string.events_delete_warning),
              bodyText = stringResource(R.string.events_delete_warning_text),
              dismissText = stringResource(R.string.cancel),
              confirmText = stringResource(R.string.delete),
              actions =
                  GatherlyAlertDialogActions(
                      onDismiss = { showDeleteEventDialog.value = false },
                      onConfirm = {
                        editEventViewModel.deleteEvent()
                        showDeleteEventDialog.value = false
                      }),
              isImportantWarning = true)
        }

        // -- Warning Dialog for switching to Public Event --
        if (showWarningPublicEvent) {
          GatherlyAlertDialog(
              titleText = stringResource(R.string.events_edit_private_to_public_label),
              bodyText = stringResource(R.string.events_warning_to_public_event_body_text),
              dismissText = stringResource(R.string.cancel),
              confirmText = stringResource(R.string.events_confirm_text_private_to_public_text),
              actions =
                  GatherlyAlertDialogActions(
                      onDismiss = { showWarningPublicEvent = false },
                      onConfirm = {
                        editEventViewModel.updatePrivateEventToPublicEvent()
                        showWarningPublicEvent = false
                      }))
        }

        // -- Date Picker Dialog --
        GatherlyDatePicker(
            show = showDatePicker,
            initialDate = ui.date,
            onDateSelected = { selectedDate -> editEventViewModel.updateDate(selectedDate) },
            onDismiss = { showDatePicker = false })
      }
}

/** Preview of the EditEventScreen composable in dark theme. */
@Preview
@Composable
fun EditEventScreenPreview() {
  GatherlyTheme(darkTheme = true) { EditEventsScreen() }
}
