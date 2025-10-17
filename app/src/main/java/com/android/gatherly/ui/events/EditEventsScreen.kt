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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.android.gatherly.model.event.EventsLocalRepository
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu_Goback
import com.android.gatherly.ui.theme.GatherlyTheme
import com.android.gatherly.utils.GenericViewModelFactory
import kotlinx.coroutines.delay

object EditEventsScreenTestTags {
  const val INPUT_NAME = "EVENT_NAME"
  const val INPUT_DESCRIPTION = "EVENT_DESCRIPTION"
  const val INPUT_CREATOR = "EVENT_CREATOR"
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

  const val PROFILE_SUGGESTION_ITEM = "EVENT_PROFILE_SUGGESTION_ITEM"
  const val PROFILE_SUGGESTION_ADD = "EVENT_PROFILE_SUGGESTION_ADD"
  const val PROFILE_SUGGESTION_REMOVE = "EVENT_PROFILE_SUGGESTION_REMOVE"
}

/**
 * Screen for editing an existing Event.
 *
 * @param eventId id of the event to load and edit.
 * @param onSave called after a successful save or deletion and navigation intent.
 * @param goBack called when back arrow is pressed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventsScreen(
    eventId: String = "",
    editEventsViewModel: EditEventsViewModel =
        viewModel(
            factory =
                GenericViewModelFactory {
                  EditEventsViewModel(
                      profileRepository = ProfileLocalRepository(),
                      eventsRepository = EventsLocalRepository())
                }),
    onSave: () -> Unit = {},
    goBack: () -> Unit = {},
) {
  // Load event once
  LaunchedEffect(eventId) { editEventsViewModel.setEventValues(eventId) }

  val ui = editEventsViewModel.uiState
  val context = LocalContext.current

  val screenPadding = dimensionResource(id = R.dimen.padding_screen)
  val fieldSpacing = dimensionResource(id = R.dimen.spacing_between_fields)
  val buttonSpacing = dimensionResource(id = R.dimen.spacing_between_buttons)

  val textFieldColors =
      TextFieldDefaults.colors(
          focusedContainerColor = MaterialTheme.colorScheme.background,
          unfocusedContainerColor = MaterialTheme.colorScheme.background,
          unfocusedTextColor = MaterialTheme.colorScheme.primary,
          focusedTextColor = MaterialTheme.colorScheme.primary,
      )

  // Local state for the dropdown visibility
  var showLocationDropdown by remember { mutableStateOf(false) }

  // Profile state for the dropdown visibility
  var showProfilesDropdown by remember { mutableStateOf(false) }

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
  LaunchedEffect(ui.participant) {
    if (ui.participant.isNotBlank()) {
      delay(1000)
      editEventsViewModel.searchProfileByString(ui.participant)
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
            modifier = Modifier.fillMaxSize().padding(paddingVal).padding(screenPadding),
            verticalArrangement = Arrangement.spacedBy(fieldSpacing)) {
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

              item {
                // Creator name
                OutlinedTextField(
                    value = ui.creatorName,
                    onValueChange = { editEventsViewModel.updateCreatorName(it) },
                    label = { Text(stringResource(R.string.events_creator_field_label)) },
                    placeholder = { Text(stringResource(R.string.events_creator_placeholder)) },
                    isError = ui.creatorNameError,
                    supportingText = {
                      if (ui.creatorNameError) {
                        Text(
                            "Creator name is required",
                            modifier = Modifier.testTag(EditEventsScreenTestTags.ERROR_MESSAGE))
                      }
                    },
                    colors = textFieldColors,
                    modifier =
                        Modifier.fillMaxWidth().testTag(EditEventsScreenTestTags.INPUT_CREATOR))
              }

              item {
                // Participants search with dropdown and + / - actions
                Box(modifier = Modifier.fillMaxWidth()) {
                  OutlinedTextField(
                      value = ui.participant,
                      onValueChange = {
                        editEventsViewModel.updateParticipant(it)
                        showProfilesDropdown = it.isNotBlank()
                      },
                      label = { Text(stringResource(R.string.events_participant_field_label)) },
                      placeholder = {
                        Text(stringResource(R.string.events_participant_placeholder))
                      },
                      colors = textFieldColors,
                      modifier =
                          Modifier.fillMaxWidth()
                              .testTag(EditEventsScreenTestTags.INPUT_PARTICIPANT))

                  DropdownMenu(
                      expanded = showProfilesDropdown && ui.suggestedProfiles.isNotEmpty(),
                      onDismissRequest = { showProfilesDropdown = false },
                      properties = PopupProperties(focusable = false),
                      modifier =
                          Modifier.testTag(EditEventsScreenTestTags.PARTICIPANT_MENU)
                              .fillMaxWidth()
                              .height(200.dp)) {
                        ui.suggestedProfiles.forEach { profile ->
                          val isAlreadyParticipant = ui.participants.any { it.uid == profile.uid }
                          DropdownMenuItem(
                              text = {
                                Row(
                                    modifier =
                                        Modifier.fillMaxWidth()
                                            .testTag(
                                                EditEventsScreenTestTags.PROFILE_SUGGESTION_ITEM),
                                    horizontalArrangement = Arrangement.SpaceBetween) {
                                      Text(profile.name)
                                      if (isAlreadyParticipant) {
                                        IconButton(
                                            onClick = {
                                              editEventsViewModel.deleteParticipant(profile.uid)
                                            },
                                            modifier =
                                                Modifier.testTag(
                                                    EditEventsScreenTestTags
                                                        .PROFILE_SUGGESTION_REMOVE)) {
                                              Icon(
                                                  Icons.Filled.Remove,
                                                  contentDescription = "Remove")
                                            }
                                      } else {
                                        IconButton(
                                            onClick = {
                                              editEventsViewModel.addParticipant(profile)
                                            },
                                            modifier =
                                                Modifier.testTag(
                                                    EditEventsScreenTestTags
                                                        .PROFILE_SUGGESTION_ADD)) {
                                              Icon(Icons.Filled.Add, contentDescription = "Add")
                                            }
                                      }
                                    }
                              },
                              onClick = {})
                        }
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
                      modifier =
                          Modifier.testTag(EditEventsScreenTestTags.LOCATION_MENU)
                              .fillMaxWidth()
                              .height(200.dp)) {
                        ui.suggestedLocations.take(3).forEach { loc ->
                          DropdownMenuItem(
                              text = {
                                Text(
                                    text =
                                        loc.name.take(40) + if (loc.name.length > 40) "..." else "")
                              },
                              onClick = {
                                editEventsViewModel.selectLocation(loc)
                                showLocationDropdown = false
                              },
                              modifier = Modifier.testTag(EditEventsScreenTestTags.INPUT_LOCATION))
                        }
                        if (ui.suggestedLocations.size > 3) {
                          DropdownMenuItem(text = { Text("More...") }, onClick = {})
                        }
                      }
                }
              }

              item {
                // Date
                OutlinedTextField(
                    value = ui.date,
                    onValueChange = { editEventsViewModel.updateDate(it) },
                    label = { Text(stringResource(R.string.events_date_field_label)) },
                    placeholder = { Text("dd/MM/yyyy") },
                    isError = ui.dateError,
                    supportingText = {
                      if (ui.dateError) {
                        Text(
                            "Use format dd/MM/yyyy",
                            modifier = Modifier.testTag(EditEventsScreenTestTags.ERROR_MESSAGE))
                      }
                    },
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth().testTag(EditEventsScreenTestTags.INPUT_DATE))
              }

              item {
                // Start time
                OutlinedTextField(
                    value = ui.startTime,
                    onValueChange = { editEventsViewModel.updateStartTime(it) },
                    label = { Text(stringResource(R.string.events_start_time_field_label)) },
                    placeholder = { Text("HH:mm") },
                    isError = ui.startTimeError,
                    supportingText = {
                      if (ui.startTimeError) {
                        Text(
                            "Use format HH:mm",
                            modifier = Modifier.testTag(EditEventsScreenTestTags.ERROR_MESSAGE))
                      }
                    },
                    colors = textFieldColors,
                    modifier =
                        Modifier.fillMaxWidth().testTag(EditEventsScreenTestTags.INPUT_START))
              }

              item {
                // End time
                OutlinedTextField(
                    value = ui.endTime,
                    onValueChange = { editEventsViewModel.updateEndTime(it) },
                    label = { Text(stringResource(R.string.events_end_time_field_label)) },
                    placeholder = { Text("HH:mm") },
                    isError = ui.endTimeError,
                    supportingText = {
                      if (ui.endTimeError) {
                        Text(
                            "Use format HH:mm",
                            modifier = Modifier.testTag(EditEventsScreenTestTags.ERROR_MESSAGE))
                      }
                    },
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth().testTag(EditEventsScreenTestTags.INPUT_END))
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
                            !ui.creatorNameError &&
                            !ui.dateError &&
                            !ui.startTimeError &&
                            !ui.endTimeError) {
                      Text("Save", color = MaterialTheme.colorScheme.primary)
                    }
              }

              item {
                // Delete
                TextButton(
                    onClick = {
                      editEventsViewModel.deleteEvent()
                      onSave()
                    },
                    modifier = Modifier.fillMaxWidth().testTag(EditEventsScreenTestTags.BTN_DELETE),
                    colors =
                        ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.tertiary)) {
                      Icon(
                          imageVector = Icons.Filled.DeleteForever,
                          contentDescription = "Delete event",
                          tint = MaterialTheme.colorScheme.tertiary)
                      Text(
                          "Delete",
                          modifier = Modifier.padding(start = buttonSpacing),
                          color = MaterialTheme.colorScheme.tertiary)
                    }
              }
            }
      }
}

@Preview
@Composable
fun EditEventsScreenPreview() {
  GatherlyTheme { EditEventsScreen() }
}
