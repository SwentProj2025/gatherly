package com.android.gatherly.ui.events

import android.annotation.SuppressLint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.gatherly.model.event.Event
import com.android.gatherly.model.event.EventState
import com.android.gatherly.model.event.EventStatus
import com.android.gatherly.model.event.EventsRepository
import com.android.gatherly.model.event.EventsRepositoryFirestore
import com.android.gatherly.model.group.Group
import com.android.gatherly.model.group.GroupsRepository
import com.android.gatherly.model.group.GroupsRepositoryFirestore
import com.android.gatherly.model.map.Location
import com.android.gatherly.model.map.LocationRepository
import com.android.gatherly.model.map.NominatimLocationRepository
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.profile.ProfileRepositoryFirestore
import com.android.gatherly.utils.GenericViewModelFactory
import com.android.gatherly.utils.cancelEvent
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.text.ParseException
import java.text.SimpleDateFormat
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

data class EditEventsUIState(
    // the event title
    val name: String = "",
    // the event description
    val description: String = "",
    // the event location
    val location: String = "",
    // the event date
    val date: String = "",
    // the event start time
    val startTime: String = "",
    // the event end time
    val endTime: String = "",
    // the event participant search string
    val participant: String = "",
    // list of event participants
    val participants: List<Profile> = emptyList(),
    // list of suggested profiles given the search string
    val suggestedProfiles: List<Profile> = emptyList(),
    // list of suggested locations given the search string
    val suggestedLocations: List<Location> = emptyList(),
    // if there is an error in the name
    val nameError: Boolean = false,
    // if there is an error in the description
    val descriptionError: Boolean = false,
    // if there is an error in the date
    val dateError: Boolean = false,
    // if there is an error in the start time
    val startTimeError: Boolean = false,
    // if there is an error in the end time
    val endTimeError: Boolean = false,
    // if the UI should display a toast
    val displayToast: Boolean = false,
    // the string the toast should display
    val toastString: String? = null,
    // when the event is edited or deleted, return to event overview
    val backToOverview: Boolean = false,
    // when the event is being
    val isLoading: Boolean = false,
    // the state of the event
    val state: EventState = EventState.PUBLIC,
    // the friend participant search string
    val friend: String = "",
    // list of suggested friends given the search string
    val suggestedFriendsProfile: List<Profile> = emptyList(),
    // the event group search string
    val group: String = "",
    // when it's a private group event
    val groups: List<Group> = emptyList(),
    // list of suggested groups given the search string
    val suggestedGroups: List<Group> = emptyList(),
    // the id of the current user
    val currentUserId: String = ""
)

// create a HTTP Client for Nominatim
private var client: OkHttpClient =
    OkHttpClient.Builder()
        .addInterceptor { chain ->
          val request =
              chain
                  .request()
                  .newBuilder()
                  .header("User-Agent", "BootcampApp (croissant.kerjan@gmail.com)")
                  .build()
          chain.proceed(request)
        }
        .build()

@SuppressLint("SimpleDateFormat")
class EditEventsViewModel(
    private val profileRepository: ProfileRepository,
    private val groupsRepository: GroupsRepository,
    private val eventsRepository: EventsRepository,
    private val nominatimClient: LocationRepository = NominatimLocationRepository(client)
) : ViewModel() {

  // State with a private set
  var uiState by mutableStateOf<EditEventsUIState>(EditEventsUIState())
    private set

  // Formats used for date and time parsing
  private val dateFormat = SimpleDateFormat("dd/MM/yyyy")
  private val timeFormat = SimpleDateFormat("HH:mm")

  // Event id and Creator id needed for saving the edited event
  private lateinit var eventId: String
  private lateinit var creatorId: String
  private lateinit var creatorName: String
  private lateinit var currentProfile: Profile

  // The list of participants ID is needed in case
  // if the event is canceled we have to unregister everybody
  private lateinit var participants: List<String>

  // Selected Location
  private var chosenLocation: Location? = null

  /*----------------------------------Initialize------------------------------------------------*/
  init {
    // The string formatter should use strictly the format wanted
    dateFormat.isLenient = false
    timeFormat.isLenient = false
  }

  // Sets the values of the event to edit given an event id
  fun setEventValues(givenEventId: String) {
    viewModelScope.launch {
      val event = eventsRepository.getEvent(givenEventId)
      uiState =
          uiState.copy(
              name = event.title,
              description = event.description,
              location = event.location?.name ?: "",
              date = dateFormat.format(event.date.toDate()),
              startTime = timeFormat.format(event.startTime.toDate()),
              endTime = timeFormat.format(event.endTime.toDate()),
              participants = event.participants.map { profileRepository.getProfileByUid(it)!! },
              state = event.state,
              currentUserId = event.creatorId,
              groups = event.groups)
      eventId = event.id
      creatorId = event.creatorId
      creatorName = event.creatorName
      participants = event.participants
      currentProfile = profileRepository.getProfileByUid(creatorId)!!
    }
  }

  // Clears the error message once the toast is done displaying
  fun clearErrorMsg() {
    uiState = uiState.copy(displayToast = false, toastString = null)
  }

  /*----------------------------------Update strings--------------------------------------------*/

  /**
   * Updates the event name
   *
   * @param updatedName the string with which to update
   */
  fun updateName(updatedName: String) {
    uiState = uiState.copy(name = updatedName, nameError = updatedName.isBlank())
  }

  /**
   * Updates the event description
   *
   * @param updatedDescription the string with which to update
   */
  fun updateDescription(updatedDescription: String) {
    uiState =
        uiState.copy(
            description = updatedDescription, descriptionError = updatedDescription.isBlank())
  }

  /**
   * Updates the event location
   *
   * @param updatedLocation the string with which to update
   */
  fun updateLocation(updatedLocation: String) {
    uiState = uiState.copy(location = updatedLocation)
  }

  /**
   * Updates the event date
   *
   * @param updatedDate the string with which to update
   */
  fun updateDate(updatedDate: String) {
    val dateError =
        try {
          val date = dateFormat.parse(updatedDate) ?: throw IllegalArgumentException()
          val dateTimestamp = Timestamp(date)

          val currentTimestamp = Timestamp.now()

          dateTimestamp >= currentTimestamp
        } catch (_: ParseException) {
          false
        }
    uiState = uiState.copy(date = updatedDate, dateError = !dateError)
  }

  /**
   * Updates the event start time
   *
   * @param updatedStartTime the string with which to update
   */
  fun updateStartTime(updatedStartTime: String) {
    val startTimeError =
        try {
          timeFormat.parse(updatedStartTime)
          true
        } catch (_: ParseException) {
          false
        }
    uiState = uiState.copy(startTime = updatedStartTime, startTimeError = !startTimeError)
  }

  /**
   * Updates the event end time
   *
   * @param updatedEndTime the string with which to update
   */
  fun updateEndTime(updatedEndTime: String) {
    val endTimeError =
        try {
          val endTime = timeFormat.parse(updatedEndTime) ?: throw IllegalArgumentException()

          val dateCheck =
              if (!uiState.dateError && uiState.date.isNotBlank()) {
                val currentTimestamp = Timestamp.now()
                val sdfDateAndTime = SimpleDateFormat("dd/MM/yyyy HH:mm")
                val dateAndTime =
                    sdfDateAndTime.parse(uiState.date + " " + updatedEndTime)
                        ?: throw IllegalArgumentException()
                val dateAndTimeTimestamp = Timestamp(dateAndTime)

                dateAndTimeTimestamp < currentTimestamp
              } else {
                false
              }

          val startTimeCheck =
              if (!uiState.startTimeError && uiState.startTime.isNotBlank()) {
                val endTimeTimestamp = Timestamp(endTime)

                val startTime =
                    timeFormat.parse(uiState.startTime) ?: throw IllegalArgumentException()
                val startTimeTimestamp = Timestamp(startTime)

                endTimeTimestamp <= startTimeTimestamp
              } else {
                false
              }

          !(startTimeCheck || dateCheck)
        } catch (_: ParseException) {
          false
        }
    uiState = uiState.copy(endTime = updatedEndTime, endTimeError = !endTimeError)
  }

  /**
   * Updates the event participant string
   *
   * @param updatedParticipant the string with which to update
   */
  fun updateParticipant(updatedParticipant: String) {
    uiState = uiState.copy(participant = updatedParticipant)
  }

  /**
   * Updates the event group string
   *
   * @param updatedGroup the string with which to update
   */
  fun updateGroup(updatedGroup: String) {
    uiState = uiState.copy(group = updatedGroup)
  }

  /** Update the event to make it public */
  fun updatePrivateEventToPublicEvent() {
    if (uiState.state != EventState.PUBLIC) {
      uiState = uiState.copy(state = EventState.PUBLIC)
    }
  }

  /*----------------------------------Participants----------------------------------------------*/
  /**
   * Deletes a participant
   *
   * @param participant the id of the participant to remove
   */
  fun deleteParticipant(participant: String) {
    val index = uiState.participants.find { it.uid == participant }
    uiState =
        if (index == null) {
          uiState.copy(
              displayToast = true,
              toastString = "Cannot delete this participant, as they are not participating")
        } else if (participant == creatorId) {
          uiState.copy(displayToast = true, toastString = "Cannot delete the owner")
        } else {
          viewModelScope.launch { profileRepository.unregisterEvent(eventId, participant) }
          uiState.copy(
              participants = uiState.participants.filter { it.uid != participant },
              suggestedProfiles = emptyList())
        }
  }

  /**
   * Adds a participant
   *
   * @param participant the profile of the participant to add
   */
  fun addParticipant(participant: Profile) {
    if (uiState.participants.any { it == participant }) {
      uiState =
          uiState.copy(
              displayToast = true, toastString = "Cannot add a participant that is already added")
      return
    }
    viewModelScope.launch { profileRepository.participateEvent(eventId, participant.uid) }
    uiState =
        uiState.copy(
            participants = uiState.participants + participant, suggestedProfiles = emptyList())
  }
  /**
   * The user choose the group to invite to this event
   *
   * @param groupName the group the user wants to invite for the event
   */
  fun inviteGroup(groupName: String) {
    viewModelScope.launch {
      val newGroup = groupsRepository.getGroupByName(groupName)

      if (uiState.groups.any { it.gid == newGroup.gid }) {
        uiState =
            uiState.copy(
                displayToast = true, toastString = "You already invited this group to this event")
        return@launch
      }

      val updatedGroups = uiState.groups + newGroup

      val allMemberUids = updatedGroups.flatMap { it.memberIds }.distinct()

      val membersProfile =
          allMemberUids.mapNotNull { uid -> profileRepository.getProfileByUid(uid) }

      uiState = uiState.copy(groups = updatedGroups, participants = membersProfile)
    }
  }

  /**
   * The user changes his mind, he wants to remove a chosen group
   *
   * @param groupId the id of the group the user wants to remove for the event
   */
  fun removeGroup(groupId: String) {
    if (uiState.groups.isEmpty()) return

    val updatedGroups = uiState.groups.filter { it.gid != groupId }

    viewModelScope.launch {
      val allMemberUids = updatedGroups.flatMap { it.memberIds }.distinct()

      val membersProfile =
          allMemberUids.mapNotNull { uid -> profileRepository.getProfileByUid(uid) }

      uiState = uiState.copy(groups = updatedGroups, participants = membersProfile)
    }
  }

  /*----------------------------------Location--------------------------------------------------*/

  fun selectLocation(location: Location) {
    uiState = uiState.copy(location = location.name, suggestedLocations = emptyList())
    chosenLocation = location
  }

  /*----------------------------------Helpers---------------------------------------------------*/
  /**
   * Given a string, search profiles that have it as a substring in their name
   *
   * @param string the substring with which to search
   */
  fun searchProfileByString(string: String) {
    viewModelScope.launch {
      val profilesList = profileRepository.searchProfilesByNamePrefix(string)
      println("profiles list" + profilesList.size)
      uiState = uiState.copy(suggestedProfiles = profilesList)
    }
  }

  /**
   * Given a string, search profiles that have it as a substring in their name
   *
   * @param string the substring with which to search
   */
  fun searchFriendsProfileByString(string: String) {
    viewModelScope.launch {
      val friendsIds = currentProfile.friendUids
      val list = friendsIds.mapNotNull { friendId -> profileRepository.getProfileByUid(friendId) }
      val profilesList = searchGivenListByNamePrefix(string, list)
      uiState = uiState.copy(suggestedFriendsProfile = profilesList)
    }
  }

  /**
   * Given a string, search groups belonging to the current user that have it as a substring in
   * their name
   *
   * @param string the substring with which to search
   */
  fun searchGroupsNameByString(string: String) {
    viewModelScope.launch {
      val trimmedString = string.trim()

      val allGroups = groupsRepository.getUserGroups()

      if (trimmedString.isBlank()) {
        uiState = uiState.copy(suggestedGroups = allGroups)
        return@launch
      }

      val suggestedGroups =
          allGroups.filter { group -> group.name.startsWith(trimmedString, ignoreCase = true) }

      uiState = uiState.copy(suggestedGroups = suggestedGroups)
    }
  }

  /**
   * Given a string, search locations with Nominatim
   *
   * @param location the substring with which to search
   */
  fun searchLocationByString(location: String) {
    viewModelScope.launch {
      val list = nominatimClient.search(location)
      uiState = uiState.copy(suggestedLocations = list)
    }
  }

  /** Checks that all entries are correctly set */
  private fun checkAllEntries() {
    updateName(uiState.name)
    updateDescription(uiState.description)
    updateDate(uiState.date)
    updateStartTime(uiState.startTime)
    updateEndTime(uiState.endTime)
  }

  /*----------------------------------Repository calls------------------------------------------*/

  /** Save the event as modified in the events repository */
  fun saveEvent() {
    checkAllEntries()
    if (!uiState.nameError &&
        !uiState.descriptionError &&
        !uiState.dateError &&
        !uiState.startTimeError &&
        !uiState.endTimeError) {
      uiState = uiState.copy(isLoading = true)

      // Parse date
      val date =
          dateFormat.parse(uiState.date)
              ?: run {
                uiState =
                    uiState.copy(
                        displayToast = true,
                        toastString = "Cannot parse event date",
                        isLoading = false)
                return
              }
      val timestampDate = Timestamp(date)

      // Parse start time
      val startTime =
          timeFormat.parse(uiState.startTime)
              ?: run {
                uiState =
                    uiState.copy(
                        displayToast = true,
                        toastString = "Cannot parse event start time",
                        isLoading = false)
                return
              }
      val timestampStartTime = Timestamp(startTime)

      // Parse end time
      val endTime =
          timeFormat.parse(uiState.endTime)
              ?: run {
                uiState =
                    uiState.copy(
                        displayToast = true,
                        toastString = "Cannot parse event end time",
                        isLoading = false)
                return
              }
      val timestampEndTime = Timestamp(endTime)

      // Create new event
      val event =
          Event(
              id = eventId,
              title = uiState.name,
              description = uiState.description,
              creatorName = creatorName,
              location = chosenLocation,
              date = timestampDate,
              startTime = timestampStartTime,
              endTime = timestampEndTime,
              creatorId = creatorId,
              participants = uiState.participants.map { it.uid },
              status = EventStatus.UPCOMING,
              state = uiState.state,
              groups = uiState.groups)

      // Save in event repository
      viewModelScope.launch {
        eventsRepository.editEvent(eventId, event)
        uiState =
            uiState.copy(
                displayToast = true,
                toastString = "Saved",
                isLoading = false,
                backToOverview = true)
      }
    } else {
      uiState =
          uiState.copy(displayToast = true, toastString = "Failed to save :(", isLoading = false)
    }
  }

  /** Deletes the event from the events repository */
  fun deleteEvent() {
    // Call event repository
    viewModelScope.launch {
      cancelEvent(eventsRepository, profileRepository, eventId, creatorId, participants)
      uiState = uiState.copy(backToOverview = true)
    }
  }

  /**
   * Companion Object used to encapsulate a static method to retrieve a ViewModelProvider.Factory
   * and its default dependencies.
   */
  companion object {
    fun provideFactory(
        profileRepository: ProfileRepository =
            ProfileRepositoryFirestore(Firebase.firestore, Firebase.storage),
        eventsRepository: EventsRepository = EventsRepositoryFirestore(Firebase.firestore),
        nominatimClient: LocationRepository = NominatimLocationRepository(client),
        groupsRepository: GroupsRepository = GroupsRepositoryFirestore(Firebase.firestore)
    ): ViewModelProvider.Factory {
      return GenericViewModelFactory {
        EditEventsViewModel(
            profileRepository = profileRepository,
            eventsRepository = eventsRepository,
            nominatimClient = nominatimClient,
            groupsRepository = groupsRepository)
      }
    }
  }

  /**
   * Helper function : to search a prefix string in the name of profiles given by the list
   *
   * @param prefix substring to search
   * @param list list of profile where we want to find the substring in their name
   */
  private fun searchGivenListByNamePrefix(prefix: String, list: List<Profile>): List<Profile> {
    val trimmedPrefix = prefix.trim()
    if (trimmedPrefix.isEmpty()) {
      return emptyList()
    }

    return list.filter { profile -> profile.name.startsWith(trimmedPrefix, ignoreCase = true) }
  }
}
