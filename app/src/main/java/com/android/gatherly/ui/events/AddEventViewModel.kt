package com.android.gatherly.ui.events

import android.annotation.SuppressLint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.gatherly.model.event.Event
import com.android.gatherly.model.event.EventStatus
import com.android.gatherly.model.event.EventsRepository
import com.android.gatherly.model.map.Location
import com.android.gatherly.model.map.NominatimLocationRepository
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileRepository
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
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
 * through [EventRepository].
 *
 * @param eventRepository The repository responsible for persisting Event items.
 */
@SuppressLint("SimpleDateFormat")
class AddEventViewModel(
    private val profileRepository: ProfileRepository,
    private val eventsRepository: EventsRepository,
    private val nominatimClient: NominatimLocationRepository = NominatimLocationRepository(client)
    // private val eventRepository: EventsRepository = EventsRepositoryProvider.repository,
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
    // The string formatter should use strictly the format wanted
    dateFormat.isLenient = false
    timeFormat.isLenient = false
    viewModelScope.launch {
      val profile = profileRepository.getProfileByUid(Firebase.auth.currentUser?.uid!!)!!
      currentProfile = profile
      uiState = uiState.copy(participants = listOf(profile))
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
      val profilesList = profileRepository.findProfilesByUidSubstring(string)
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

      // Create new event
      val event =
          Event(
              id = eventsRepository.getNewId(),
              title = uiState.name,
              description = uiState.description,
              creatorName = uiState.creatorName,
              location = null,
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
        uiState = uiState.copy(displayToast = true, toastString = "Saved")
      }

      uiState = uiState.copy(backToOverview = true)
    } else {
      uiState = uiState.copy(displayToast = true, toastString = "Failed to save :(")
    }
  }
}
    /*

        private val _uiState = MutableStateFlow(AddEventUiState())

        /** Public immutable access to the Add Event UI state. */
        val uiState: StateFlow<AddEventUiState> = _uiState.asStateFlow()

        /** Clears the error message in the UI state. */
        fun clearErrorMsg() {
            _uiState.value = _uiState.value.copy(saveError = null)
        }

        /** Clears the save success flag in the UI state. */
        fun clearSaveSuccess() {
            _uiState.value = _uiState.value.copy(saveSuccess = false)
        }


        /**
         * Updates the title field and validates that it is not blank.
         *
         * @param newValue The new title entered by the user. If blank, a validation error is set.
         */
        fun onTitleChanged(newValue: String) {
            _uiState.value =
                _uiState.value.copy(
                    title = newValue,
                    titleError = if (newValue.isBlank()) "Title cannot be empty" else null)
        }

        /**
         * Updates the description field and validates that it is not blank.
         *
         * @param newValue The new description entered by the user. If blank, a validation error is set.
         */
        fun onDescriptionChanged(newValue: String) {
            _uiState.value =
                _uiState.value.copy(
                    description = newValue,
                    descriptionError = if (newValue.isBlank()) "Description cannot be empty" else null)
        }

        /**
         * Temporarily updates the location field.
         *
         * Currently stores it as a raw string until the Location class and repository are implemented.
         *
         * @param newValue The name or description of the location.
         */
        fun onLocationChanged(newValue: String) {
            _uiState.value =
                _uiState.value.copy(
                    location = newValue,
                    locationError = if (newValue.isBlank()) "Location cannot be empty" else null)
        }

        /**
         * Updates the due date field and validates the format.
         *
         * @param newValue The new due date as a string (expected format: dd/MM/yyyy).
         */
        fun onDateChanged(newValue: String) {
            _uiState.value =
                _uiState.value.copy(
                    dueDate = newValue,
                    dueDateError = if (!isValidDate(newValue)) "Invalid format (dd/MM/yyyy)" else null)
        }



        /**
         * Checks whether a given date string is valid.
         *
         * @param date The date string to validate (expected format: dd/MM/yyyy).
         * @return `true` if the format and date are valid, `false` otherwise.
         */
        private fun isValidDate(date: String): Boolean {
            val regex = Regex("""\d{2}/\d{2}/\d{4}""")
            if (!regex.matches(date)) return false
            return try {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                sdf.isLenient = false
                sdf.parse(date)
                true
            } catch (e: Exception) {
                false
            }
        }

        /**
         * Checks whether a given time string is valid.
         *
         * @param time The time string to validate (expected format: HH:mm). Blank values are allowed.
         * @return `true` if the format and time are valid, `false` otherwise.
         */
        private fun isValidTime(time: String): Boolean {
            if (time.isBlank()) return true // optional
            val regex = Regex("""\d{2}:\d{2}""")
            if (!regex.matches(time)) return false
            return try {
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                sdf.isLenient = false
                sdf.parse(time)
                true
            } catch (e: Exception) {
                false
            }
        }


        /**
         * Attempts to create and save a new [Event] entry to the repository.
         *
         * Performs field validation before saving, and updates the UI state to reflect loading, success,
         * and error states.
         *
         * @throws IllegalArgumentException If the provided date or time format is invalid.
         */
        fun saveEvent() {
            val validated =
                _uiState.value.copy(
                    titleError = if (_uiState.value.title.isBlank()) "Title cannot be empty" else null,
                    descriptionError =
                        if (_uiState.value.description.isBlank()) "Description cannot be empty" else null,
                    assigneeError =
                        if (_uiState.value.assignee.isBlank()) "Assignee cannot be empty" else null,
                    locationError =
                        if (_uiState.value.location.isBlank()) "Location cannot be empty" else null,
                    dueDateError =
                        if (!isValidDate(_uiState.value.dueDate)) "Invalid format (dd/MM/yyyy)" else null,
                    startTimeError =
                        if (!isValidTime(_uiState.value.dueTime)) "Invalid time (HH:mm)" else null)
            _uiState.value = validated

            // Abort if validation failed
            if (_uiState.value.titleError != null ||
                _uiState.value.descriptionError != null ||
                _uiState.value.assigneeError != null ||
                _uiState.value.dueDateError != null ||
                _uiState.value.dueTimeError != null) {
                return
            }

            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isSaving = true, saveError = null)
                try {
                    val uid = todoRepository.getNewUid()
                    val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val date =
                        sdfDate.parse(validated.dueDate) ?: throw IllegalArgumentException("Invalid date")

                    val dueDateTimestamp = Timestamp(date)
                    val dueTimeTimestamp =
                        if (validated.dueTime.isNotBlank()) {
                            val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
                            Timestamp(sdfTime.parse(validated.dueTime)!!)
                        } else null

                    // val loc = selectedLocation?.let { l ->
                    //  Location(latitude = l.latitude, longitude = l.longitude, name = l.name)
                    // }



                    eventRepository.addEvent(event)
                    _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(isSaving = false, saveError = e.message)
                }
            }
        }

    }

     */
