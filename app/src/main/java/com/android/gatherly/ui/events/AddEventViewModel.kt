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
import com.android.gatherly.model.points.PointsRepository
import com.android.gatherly.model.points.PointsRepositoryProvider
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.profile.ProfileRepositoryFirestore
import com.android.gatherly.utils.GenericViewModelFactory
import com.android.gatherly.utils.createEvent
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import java.text.ParseException
import java.text.SimpleDateFormat
import kotlin.collections.plus
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

/**
 * Data class representing the UI state for the "Add Event" screen.
 *
 * @property name The event title.
 * @property description The event description.
 * @property location The event location.
 * @property date The event date.
 * @property startTime The event start time.
 * @property endTime The event end time.
 * @property participant The event participant search string.
 * @property participants List of event participants.
 * @property suggestedLocations List of suggested locations given the search string.
 * @property nameError If there is an error in the name.
 * @property descriptionError If there is an error in the description.
 * @property dateError If there is an error in the date.
 * @property startTimeError If there is an error in the start time.
 * @property endTimeError If there is an error in the end time.
 * @property displayToast If the UI should display a toast.
 * @property toastString The string the toast should display.
 * @property backToOverview When the event is edited or deleted, return to event overview.
 * @property isSaving When the event is being saved.
 * @property currentUserId Current user profile Id.
 * @property suggestedProfiles List of suggested profiles given the search string.
 * @property friend The event friend search string.
 * @property suggestedFriendsProfile List of suggested friends' profiles given the search string.
 * @property group The event group search string.
 * @property groups List of groups invited to the event.
 * @property suggestedGroups List of suggested groups given the search string.
 * @property state The event state (public, private friends, private group).
 */
data class AddEventUiState(
    val name: String = "",
    val description: String = "",
    val location: String = "",
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val participant: String = "",
    val participants: List<Profile> = listOf(),
    val suggestedLocations: List<Location> = emptyList(),
    val nameError: Boolean = false,
    val descriptionError: Boolean = false,
    val dateError: Boolean = false,
    val startTimeError: Boolean = false,
    val endTimeError: Boolean = false,
    val displayToast: Boolean = false,
    val toastString: String? = null,
    val backToOverview: Boolean = false,
    val isSaving: Boolean = false,
    val currentUserId: String = "",
    val suggestedProfiles: List<Profile> = emptyList(),
    val friend: String = "",
    val suggestedFriendsProfile: List<Profile> = emptyList(),
    val group: String = "",
    val groups: List<Group> = emptyList(),
    val suggestedGroups: List<Group> = emptyList(),
    val state: EventState = EventState.PUBLIC
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

/**
 * ViewModel responsible for managing the "Add Event" screen.
 *
 * Handles user input updates, field validation, and saving Event items to the Firestore repository
 * through [EventsRepository].
 *
 * @param profileRepository The repository responsible for retrieving Profile items.
 * @param groupsRepository The repository responsible for retrieving Group items.
 * @param eventsRepository The repository responsible for persisting Event items.
 * @param pointsRepository The repository responsible for managing points.
 * @param nominatimClient The repository responsible for retrieving Location items.
 * @param authProvider A function that provides the FirebaseAuth instance.
 */
@SuppressLint("SimpleDateFormat")
class AddEventViewModel(
    private val profileRepository: ProfileRepository,
    private val groupsRepository: GroupsRepository,
    private val eventsRepository: EventsRepository,
    private val pointsRepository: PointsRepository,
    private val nominatimClient: LocationRepository = NominatimLocationRepository(client),
    private val authProvider: () -> FirebaseAuth = { Firebase.auth }
) : ViewModel() {
  // State with a private set
  var uiState by mutableStateOf(AddEventUiState())
    private set

  // Formats used for date and time parsing
  private val dateFormat = SimpleDateFormat("dd/MM/yyyy")
  private val timeFormat = SimpleDateFormat("HH:mm")

  // Current user profile
  private lateinit var currentProfile: Profile
  // Chosen location
  private var chosenLocation: Location? = null

  /*----------------------------------Initialize------------------------------------------------*/
  init {
    dateFormat.isLenient = false
    timeFormat.isLenient = false

    viewModelScope.launch {
      authProvider().currentUser?.uid?.let { userUid ->
        val profile =
            profileRepository.getProfileByUid(userUid)
                ?: Profile(uid = userUid, name = "", username = "", profilePicture = "")

        currentProfile = profile
        uiState =
            uiState.copy(participants = listOf(currentProfile), currentUserId = currentProfile.uid)
      }
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

  /*------------------------------Public/Private Event------------------------------------------*/

  /** Updates the event status to a private friends only event */
  fun updateEventToPrivateFriends() {
    val friendsOnly =
        uiState.participants.filter { participant ->
          currentProfile.friendUids.contains(participant.uid)
        }
    uiState =
        uiState.copy(
            state = EventState.PRIVATE_FRIENDS, groups = emptyList(), participants = friendsOnly)
  }

  /** Updates the event status to a public Event */
  fun updateEventToPublic() {
    uiState = uiState.copy(state = EventState.PUBLIC, groups = emptyList())
  }

  /**
   * Updates the event status to a private Event where the user have to choose a group to invite to
   */
  fun updateEventToPrivateGroup() {
    uiState =
        uiState.copy(
            state = EventState.PRIVATE_GROUP, groups = emptyList(), participants = emptyList())
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
        } else if (participant == currentProfile.uid) {
          uiState.copy(displayToast = true, toastString = "Cannot delete yourself from your event")
        } else {
          uiState.copy(
              participant = "",
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
    uiState =
        uiState.copy(
            participant = "",
            participants = uiState.participants + participant,
            suggestedProfiles = emptyList())
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

  /**
   * Updates the location to the selected location
   *
   * @param location the selected location
   */
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

      uiState = uiState.copy(isSaving = true)

      // Parse date
      val date =
          dateFormat.parse(uiState.date)
              ?: run {
                uiState =
                    uiState.copy(
                        displayToast = true,
                        toastString = "Cannot parse event date",
                        isSaving = false)
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
                        isSaving = false)
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
                        isSaving = false)
                return
              }
      val timestampEndTime = Timestamp(endTime)

      // Create ID of the new event
      val eventId = eventsRepository.getNewId()

      // List of the ID of every participants
      val participants: List<String> =
          if (uiState.state != EventState.PRIVATE_GROUP) {
            uiState.participants.map { it.uid }
          } else {
            uiState.groups.flatMap { it.memberIds }.distinct()
          }

      // Create new event
      val event =
          Event(
              id = eventId,
              title = uiState.name,
              description = uiState.description,
              creatorName = currentProfile.name,
              location = chosenLocation,
              date = timestampDate,
              startTime = timestampStartTime,
              endTime = timestampEndTime,
              creatorId = currentProfile.uid,
              participants = participants,
              status = EventStatus.UPCOMING,
              state = uiState.state,
              groups = uiState.groups)

      // Save in event repository
      viewModelScope.launch {
        createEvent(
            eventsRepository,
            profileRepository,
            pointsRepository,
            event,
            currentProfile.uid,
            participants)
        uiState =
            uiState.copy(
                displayToast = true, toastString = "Saved", isSaving = false, backToOverview = true)
      }
    } else {
      uiState =
          uiState.copy(displayToast = true, toastString = "Failed to save :(", isSaving = false)
    }
  }

  /**
   * Companion Object used to encapsulate a static method to retrieve a ViewModelProvider.Factory
   * and its default dependencies.
   *
   * @param profileRepository The repository responsible for retrieving Profile items.
   * @param eventsRepository The repository responsible for persisting Event items.
   * @param nominatimClient The repository responsible for retrieving Location items.
   * @param groupsRepository The repository responsible for retrieving Group items.
   * @param pointsRepository The repository responsible for managing points.
   * @return A ViewModelProvider.Factory that creates AddEventViewModel instances with default
   *   dependencies.
   */
  companion object {
    fun provideFactory(
        profileRepository: ProfileRepository =
            ProfileRepositoryFirestore(Firebase.firestore, Firebase.storage),
        eventsRepository: EventsRepository = EventsRepositoryFirestore(Firebase.firestore),
        nominatimClient: NominatimLocationRepository = NominatimLocationRepository(client),
        groupsRepository: GroupsRepository = GroupsRepositoryFirestore(Firebase.firestore),
        pointsRepository: PointsRepository = PointsRepositoryProvider.repository
    ): ViewModelProvider.Factory {
      return GenericViewModelFactory {
        AddEventViewModel(
            profileRepository = profileRepository,
            eventsRepository = eventsRepository,
            pointsRepository = pointsRepository,
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
