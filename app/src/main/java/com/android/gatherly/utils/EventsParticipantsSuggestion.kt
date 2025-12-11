package com.android.gatherly.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.android.gatherly.R
import com.android.gatherly.model.event.EventState
import com.android.gatherly.model.group.Group
import com.android.gatherly.model.profile.Profile
import kotlin.collections.forEach

object EventsParticipantsSuggestionTestTag {
  const val INPUT_PARTICIPANT = "EVENT_PARTICIPANT_SEARCH"

  const val INPUT_GROUP = "EVENT_GROUP_SEARCH"
  const val BUTTON_SEE_ADDED_PARTICIPANT = " EVENT_BUTTON_ADDED_PARTICIPANT"

  const val BUTTON_SEE_ADDED_GROUP = " EVENT_BUTTON_ADDED_GROUP"

  const val PARTICIPANT_MENU = "PARTICIPANT_MENU"

  const val GROUP_MENU = "GROUP_MENU"

  fun getTestTagProfileSuggestionItem(profileId: String): String =
      "profileSuggestionItem ${profileId}"

  fun getTestTagProfileAddItem(profileId: String): String = "profileAddedItem ${profileId}"

  fun getTestTagProfileRemoveItem(profileId: String): String = "profileRemoveItem ${profileId}"

  fun getTestTagAddedProfileItem(profileId: String): String = "addedProfileItem ${profileId}"

  fun getTestTagAddedProfileRemoveItem(profileId: String): String =
      "addedProfileRemoveItem ${profileId}"

  fun getTestTagGroupSuggestionItem(groupId: String): String = "groupSuggestionItem ${groupId}"

  fun getTestTagGroupSuggestionAdd(groupId: String): String = "groupSuggestionAdd ${groupId}"

  fun getTestTagGroupSuggestionRemove(groupId: String): String = "groupSuggestionRemove ${groupId}"
}

data class ParticipantsUiState(
    val participant: String,
    val participants: List<Profile>,
    val suggestedProfiles: List<Profile>,
    val suggestedFriendsProfile: List<Profile>,
    val state: EventState
)

data class ParticipantsActions(
    val addParticipant: (profile: Profile) -> Unit,
    val deleteParticipant: (participantUid: String) -> Unit,
    val updateParticipant: (updatedParticipant: String) -> Unit,
)

/**
 * Helper composable function: Item where the user will write the participants he wants to invite to
 * his event.
 *
 * @param uiState the UiState of the AddEvent and EditEvent
 * @param textFieldColors the colors theme to use for our implementation
 * @param showProfilesDropdown the boolean who will be updated whether the user clicked/wrote
 */
@Composable
fun ParticipantsFieldItem(
    uiState: ParticipantsUiState,
    currentUserId: String,
    actions: ParticipantsActions,
    textFieldColors: TextFieldColors,
    showProfilesDropdown: MutableState<Boolean>
) {
  var showAddedParticipantsDropDown by remember { mutableStateOf(false) }

  Column(modifier = Modifier.fillMaxWidth()) {
    Box(modifier = Modifier.fillMaxWidth()) {

      // Text field where to writ the participant name
      OutlinedTextField(
          value = uiState.participant,
          onValueChange = {
            actions.updateParticipant(it)
            showProfilesDropdown.value = it.isNotBlank()
          },
          label = { Text(stringResource(R.string.events_participant_field_label)) },
          placeholder = { Text(stringResource(R.string.events_participant_placeholder)) },
          colors = textFieldColors,
          modifier =
              Modifier.fillMaxWidth()
                  .testTag(EventsParticipantsSuggestionTestTag.INPUT_PARTICIPANT),

          // TrailingIcon to visualise the added participants
          trailingIcon = {
            AddedParticipantsIconAdd(
                participants = uiState.participants,
                showAddedParticipantsDropDown =
                    remember { mutableStateOf(showAddedParticipantsDropDown) },
                showProfilesDropdown = showProfilesDropdown,
                onIconClicked = { isExpanded -> showAddedParticipantsDropDown = isExpanded })
          })

      // -- DROPDOWN Participants Suggestions --
      ParticipantsSuggestionsDropdown(
          showProfilesDropdown = showProfilesDropdown,
          suggestedProfiles = uiState.suggestedProfiles,
          participants = uiState.participants,
          addParticipant = { profile -> actions.addParticipant(profile) },
          deleteParticipant = { profile -> actions.deleteParticipant(profile) },
          state = uiState.state,
          suggestedFriendsProfile = uiState.suggestedFriendsProfile)
    }

    if (showAddedParticipantsDropDown && uiState.participants.isNotEmpty()) {
      AddedParticipantsDisplay(
          participants = uiState.participants,
          onRemoveParticipant = { uid ->
            if (uid != currentUserId) {
              actions.deleteParticipant(uid)
              if (uiState.participants.size == 1) showAddedParticipantsDropDown = false
            }
          },
          currentUserId = currentUserId)
    }
  }
}

/**
 * Helper composable function : Handle the Icon visibility that will show the participants profiles
 *
 * @param participants list of profile already register as participant for the event
 * @param showAddedParticipantsDropDown boolean to handle the visibility of the dropdown
 * @param showProfilesDropdown boolean to handle the visibility of the suggestion dropdown
 */
@Composable
private fun AddedParticipantsIconAdd(
    participants: List<Profile>,
    showAddedParticipantsDropDown: MutableState<Boolean>,
    showProfilesDropdown: MutableState<Boolean>,
    onIconClicked: (Boolean) -> Unit = {}
) {
  if (participants.isEmpty()) return

  IconButton(
      onClick = {
        val newState = !showAddedParticipantsDropDown.value
        showAddedParticipantsDropDown.value = newState
        onIconClicked(newState)
        showProfilesDropdown.value = false
      },
      modifier =
          Modifier.testTag(EventsParticipantsSuggestionTestTag.BUTTON_SEE_ADDED_PARTICIPANT)) {
        Icon(
            imageVector = Icons.Filled.ContactPage,
            contentDescription = "See the participant profiles",
        )
      }
}

/**
 * Helper composable function: Handles the dropdown menu
 *
 * @param state Type of Event
 * @param suggestedFriendsProfile list of suggested friends
 * @param suggestedProfiles list of suggested profiles
 * @param participants list of the actual participants of the event
 * @param addParticipant function to add participant to the event
 * @param deleteParticipant function to unregister a participant from the event
 * @param showProfilesDropdown the boolean to handles the visibility of this dropdown
 */
@Composable
private fun ParticipantsSuggestionsDropdown(
    state: EventState,
    suggestedFriendsProfile: List<Profile>,
    suggestedProfiles: List<Profile>,
    participants: List<Profile>,
    addParticipant: (Profile) -> Unit,
    deleteParticipant: (String) -> Unit,
    showProfilesDropdown: MutableState<Boolean>
) {
  val listToShow =
      if (state == EventState.PRIVATE_FRIENDS) suggestedFriendsProfile else suggestedProfiles

  if (listToShow.isEmpty()) return

  DropdownMenu(
      expanded = showProfilesDropdown.value,
      onDismissRequest = { showProfilesDropdown.value = false },
      properties = PopupProperties(focusable = false),
      containerColor = MaterialTheme.colorScheme.surfaceVariant,
      modifier =
          Modifier.testTag(EventsParticipantsSuggestionTestTag.PARTICIPANT_MENU)
              .fillMaxWidth()
              .height(200.dp)) {
        ParticipantsDropdown(
            participants,
            listToShow,
            { profile: Profile -> addParticipant(profile) },
            { profileId: String -> deleteParticipant(profileId) })
      }
}

/**
 * Helper composable function : the drop down used for public event or private friends only event
 *
 * @param participants list of user's profile that are already participant to the event
 * @param listSuggestedProfiles list of profiles suggested depending on what the user wrote in the
 *   text field
 * @param addParticipant function to add a new profile as participant
 * @param deleteParticipant function to delete a profile from participating to this event
 */
@Composable
fun ParticipantsDropdown(
    participants: List<Profile>,
    listSuggestedProfiles: List<Profile>,
    addParticipant: (Profile) -> Unit,
    deleteParticipant: (String) -> Unit
) {
  listSuggestedProfiles.forEach { profile ->
    val isAlreadyParticipant = participants.any { it.uid == profile.uid }

    // Item of the dropdown : suggested profile
    DropdownMenuItem(
        text = {
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween) {

                // Name of the suggested profile
                Text(
                    profile.name,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 19.sp),
                    modifier =
                        Modifier.testTag(
                            EventsParticipantsSuggestionTestTag.getTestTagProfileSuggestionItem(
                                profile.uid)))
                // If already participant, possibility to unregister him
                if (isAlreadyParticipant) {
                  IconButton(
                      onClick = { deleteParticipant(profile.uid) },
                  ) {
                    Icon(
                        Icons.Filled.Remove,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error)
                  }
                  // Possibility to register the suggested profile to the event
                } else {
                  IconButton(
                      onClick = { addParticipant(profile) },
                      modifier =
                          Modifier.testTag(
                              EventsParticipantsSuggestionTestTag.getTestTagProfileAddItem(
                                  profile.uid))) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Add",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                      }
                }
              }
        },
        onClick = {},
        modifier =
            Modifier.testTag(
                EventsParticipantsSuggestionTestTag.getTestTagProfileRemoveItem(profile.uid)))
  }
}

/**
 * Helper composable function : Dropdown specific to show the added participants
 *
 * @param participants list of the profile added as participate to the event
 * @param onRemoveParticipant function to apply when the user choose to unregister a profile
 * @param currentUserId the id of the currentUserId
 */
@Composable
private fun AddedParticipantsDisplay(
    participants: List<Profile>,
    onRemoveParticipant: (String) -> Unit,
    currentUserId: String
) {
  Card(
      modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
      shape = RoundedCornerShape(CornerSize(8.dp)),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        LazyColumn(
            modifier = Modifier.heightIn(max = 200.dp).padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
              items(participants) { profile ->
                if (profile.uid != currentUserId) {
                  Row(
                      modifier =
                          Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                      horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = profile.name,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier =
                                Modifier.weight(1f)
                                    .padding(end = 8.dp)
                                    .testTag(
                                        EventsParticipantsSuggestionTestTag
                                            .getTestTagAddedProfileItem(profile.uid)))
                        IconButton(
                            onClick = { onRemoveParticipant(profile.uid) },
                            modifier =
                                Modifier.size(24.dp)
                                    .testTag(
                                        EventsParticipantsSuggestionTestTag
                                            .getTestTagAddedProfileRemoveItem(profile.uid))) {
                              Icon(
                                  Icons.Filled.Remove,
                                  contentDescription = "Delete ${profile.name}",
                                  tint = MaterialTheme.colorScheme.error)
                            }
                      }
                }
              }
            }
      }
}

// ---------------------------------------- GROUPS EVENTS

data class GroupsUiState(
    val group: String,
    val groups: List<Group>,
    val suggestedGroups: List<Group>,
)

data class GroupsActions(
    val inviteGroup: (groupName: String) -> Unit,
    val removeGroup: (groupId: String) -> Unit,
    val updateGroup: (updatedGroup: String) -> Unit
)

/**
 * Helper composable function: Item where the user will write the groups he wants to invite to his
 * event.
 *
 * @param uiState GroupsUiState with necessary data
 * @param actions viewmodel functions
 * @param textFieldColors
 * @param showGroupsDropdown
 */
@Composable
fun GroupsFieldItem(
    uiState: GroupsUiState,
    actions: GroupsActions,
    textFieldColors: TextFieldColors,
    showGroupsDropdown: MutableState<Boolean>
) {
  var showAddedGroupsDropDown by remember { mutableStateOf(false) }

  Column(modifier = Modifier.fillMaxWidth()) {
    Box(modifier = Modifier.fillMaxWidth()) {

      // Text field where the user types the group name
      OutlinedTextField(
          value = uiState.group,
          onValueChange = {
            actions.updateGroup(it)
            showGroupsDropdown.value = true
          },
          label = { Text(stringResource(R.string.events_group_field_label)) },
          placeholder = { Text(stringResource(R.string.events_group_placeholder)) },
          colors = textFieldColors,
          modifier =
              Modifier.fillMaxWidth().testTag(EventsParticipantsSuggestionTestTag.INPUT_GROUP),
          trailingIcon = {
            AddedGroupsIcon(
                groups = uiState.groups,
                showAddedGroupsDropDown = remember { mutableStateOf(showAddedGroupsDropDown) },
                showGroupsDropdown = showGroupsDropdown,
                onIconClicked = { isExpanded -> showAddedGroupsDropDown = isExpanded })
          })

      // -- DROPDOWN Group Suggestions --
      GroupsSuggestionsDropdown(
          showGroupsDropdown = showGroupsDropdown,
          suggestedGroups = uiState.suggestedGroups,
          invitedGroups = uiState.groups,
          inviteGroup = { group -> actions.inviteGroup(group.name) },
          removeGroup = { group -> actions.removeGroup(group.gid) },
      )
    }

    if (showAddedGroupsDropDown && uiState.groups.isNotEmpty()) {
      AddedGroupsDisplay(
          groups = uiState.groups,
          onRemoveGroup = { groupId ->
            actions.removeGroup(groupId)
            if (uiState.groups.size == 1) showAddedGroupsDropDown = false
          })
    }
  }
}

/** Helper composable function : Handle the Icon visibility that will show the invited groups */
@Composable
private fun AddedGroupsIcon(
    groups: List<Group>,
    showAddedGroupsDropDown: MutableState<Boolean>,
    showGroupsDropdown: MutableState<Boolean>,
    onIconClicked: (Boolean) -> Unit = {}
) {
  if (groups.isEmpty()) return

  IconButton(
      onClick = {
        val newState = !showAddedGroupsDropDown.value
        showAddedGroupsDropDown.value = newState
        onIconClicked(newState)
        showGroupsDropdown.value = false
      },
      modifier = Modifier.testTag(EventsParticipantsSuggestionTestTag.BUTTON_SEE_ADDED_GROUP)) {
        Icon(
            imageVector = Icons.Filled.Groups,
            contentDescription = "See the invited groups",
        )
      }
}

/** Helper composable function: Handles the dropdown menu of group suggestions */
@Composable
private fun GroupsSuggestionsDropdown(
    showGroupsDropdown: MutableState<Boolean>,
    suggestedGroups: List<Group>,
    invitedGroups: List<Group>,
    inviteGroup: (Group) -> Unit,
    removeGroup: (Group) -> Unit,
) {
  if (suggestedGroups.isEmpty()) return

  DropdownMenu(
      expanded = showGroupsDropdown.value,
      onDismissRequest = { showGroupsDropdown.value = false },
      properties = PopupProperties(focusable = false),
      containerColor = MaterialTheme.colorScheme.surfaceVariant,
      modifier =
          Modifier.testTag(EventsParticipantsSuggestionTestTag.GROUP_MENU)
              .fillMaxWidth()
              .height(200.dp)) {
        GroupsDropdown(
            invitedGroups = invitedGroups,
            listSuggestedGroups = suggestedGroups,
            inviteGroup = inviteGroup,
            removeGroup = removeGroup)
      }
}

/** Helper composable function : the drop down used for group suggestions */
@Composable
fun GroupsDropdown(
    invitedGroups: List<Group>,
    listSuggestedGroups: List<Group>,
    inviteGroup: (Group) -> Unit,
    removeGroup: (Group) -> Unit
) {
  listSuggestedGroups.forEach { group ->
    val isAlreadyInvited = invitedGroups.any { it.gid == group.gid }

    // Item of the dropdown : suggested group
    DropdownMenuItem(
        text = {
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween) {

                // Name of the suggested group
                Text(
                    group.name,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 19.sp),
                    modifier =
                        Modifier.testTag(
                            EventsParticipantsSuggestionTestTag.getTestTagGroupSuggestionItem(
                                group.gid)))

                // If already invited, possibility to remove it
                if (isAlreadyInvited) {
                  IconButton(
                      onClick = { removeGroup(group) },
                  ) {
                    Icon(
                        Icons.Filled.Remove,
                        contentDescription = "Remove group",
                        tint = MaterialTheme.colorScheme.error)
                  }
                  // Possibility to invite the suggested group to the event
                } else {
                  IconButton(
                      onClick = { inviteGroup(group) },
                      modifier =
                          Modifier.testTag(
                              EventsParticipantsSuggestionTestTag.getTestTagGroupSuggestionAdd(
                                  group.gid))) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Invite group",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                      }
                }
              }
        },
        onClick = {},
        modifier =
            Modifier.testTag(
                EventsParticipantsSuggestionTestTag.getTestTagGroupSuggestionRemove(group.gid)))
  }
}

/** Helper composable function : Dropdown specific to show the invited groups */
@Composable
private fun AddedGroupsDisplay(
    groups: List<Group>,
    onRemoveGroup: (String) -> Unit,
) {
  Card(
      modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
      shape = RoundedCornerShape(CornerSize(8.dp)),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        LazyColumn(
            modifier = Modifier.heightIn(max = 200.dp).padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
              items(groups) { group ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                      Text(
                          text = group.name,
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
                          style = MaterialTheme.typography.bodyLarge,
                          modifier = Modifier.weight(1f).padding(end = 8.dp))
                      IconButton(
                          onClick = { onRemoveGroup(group.gid) }, modifier = Modifier.size(24.dp)) {
                            Icon(
                                Icons.Filled.Remove,
                                contentDescription = "Remove ${group.name}",
                                tint = MaterialTheme.colorScheme.error)
                          }
                    }
              }
            }
      }
}
