package com.android.gatherly.ui.events

import android.annotation.SuppressLint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.gatherly.model.event.Event
import com.android.gatherly.model.event.EventStatus
import com.android.gatherly.model.event.EventsRepository
import com.android.gatherly.model.event.EventsRepositoryFirestore
import com.android.gatherly.model.map.Location
import com.android.gatherly.model.map.NominatimLocationRepository
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.profile.ProfileRepositoryFirestore
import com.android.gatherly.utils.GenericViewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import java.text.ParseException
import java.text.SimpleDateFormat
import kotlin.collections.plus
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

data class AddEventUiState(
    // the event title
    val name: String = "",
    // the event description
    val description: String = "",
    // the event creators name
    val creatorName: String = "",
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
    val participants: List<Profile> = listOf(),
    // list of suggested profiles given the search string
    val suggestedProfiles: List<Profile> = emptyList(),
    // list of suggested locations given the search string
    val suggestedLocations: List<Location> = emptyList(),
    // if there is an error in the name
    val nameError: Boolean = false,
    // if there is an error in the description
    val descriptionError: Boolean = false,
    // if there is an error in the creators name
    val creatorNameError: Boolean = false,
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
    val backToOverview: Boolean = false
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
 * @param eventsRepository The repository responsible for persisting Event items.
 */
@SuppressLint("SimpleDateFormat")
class AddEventViewModel(
    private val profileRepository: ProfileRepository,
    private val eventsRepository: EventsRepository,
    private val nominatimClient: NominatimLocationRepository = NominatimLocationRepository(client),
    private val currentUser: String = Firebase.auth.currentUser?.uid ?: ""
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
      currentUser.let { userUid ->
        val profile =
            profileRepository.getProfileByUid(userUid)
                ?: Profile(uid = userUid, name = "", username = "", profilePicture = "")

        currentProfile = profile
        uiState = uiState.copy(participants = listOf(currentProfile))
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
   * Updates the event creator name
   *
   * @param updatedCreatorName the string with which to update
   */
  fun updateCreatorName(updatedCreatorName: String) {
    uiState =
        uiState.copy(
            creatorName = updatedCreatorName, creatorNameError = updatedCreatorName.isBlank())
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
          dateFormat.parse(updatedDate)
          true
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
          timeFormat.parse(updatedEndTime)
          true
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
      println("profiles list" + profilesList.size)
      uiState = uiState.copy(suggestedProfiles = profilesList)
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
    updateCreatorName(uiState.creatorName)
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
        !uiState.creatorNameError &&
        !uiState.dateError &&
        !uiState.startTimeError &&
        !uiState.endTimeError) {

      // Parse date
      val date =
          dateFormat.parse(uiState.date)
              ?: run {
                uiState = uiState.copy(displayToast = true, toastString = "Cannot parse event date")
                return
              }
      val timestampDate = Timestamp(date)

      // Parse start time
      val startTime =
          timeFormat.parse(uiState.startTime)
              ?: run {
                uiState =
                    uiState.copy(displayToast = true, toastString = "Cannot parse event start time")
                return
              }
      val timestampStartTime = Timestamp(startTime)

      // Parse end time
      val endTime =
          timeFormat.parse(uiState.endTime)
              ?: run {
                uiState =
                    uiState.copy(displayToast = true, toastString = "Cannot parse event end time")
                return
              }
      val timestampEndTime = Timestamp(endTime)

      // Create ID of the new event
      val eventId = eventsRepository.getNewId()

      // Create new event
      val event =
          Event(
              id = eventId,
              title = uiState.name,
              description = uiState.description,
              creatorName = uiState.creatorName,
              location = chosenLocation,
              date = timestampDate,
              startTime = timestampStartTime,
              endTime = timestampEndTime,
              creatorId = currentProfile.uid,
              participants = uiState.participants.map { it.uid },
              status = EventStatus.UPCOMING)

      uiState = uiState.copy(displayToast = true, toastString = "Saving...")

      // Save in event repository
      viewModelScope.launch {
        eventsRepository.addEvent(event)
        profileRepository.createEvent(eventId, currentProfile.uid)
        uiState = uiState.copy(displayToast = true, toastString = "Saved")
      }

      uiState = uiState.copy(backToOverview = true)
    } else {
      uiState = uiState.copy(displayToast = true, toastString = "Failed to save :(")
    }
  }

  /**
   * Companion Object used to encapsulate a static method to retrieve a ViewModelProvider.Factory
   * and its default dependencies.
   */
  companion object {
    fun provideFactory(
        profileRepository: ProfileRepository = ProfileRepositoryFirestore(Firebase.firestore),
        eventsRepository: EventsRepository = EventsRepositoryFirestore(Firebase.firestore)
    ): ViewModelProvider.Factory {
      return GenericViewModelFactory { AddEventViewModel(profileRepository, eventsRepository) }
    }
  }
}
