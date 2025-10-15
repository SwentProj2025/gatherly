package com.android.gatherly.ui.events

import android.annotation.SuppressLint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.dimensionResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.gatherly.model.event.Event
import com.android.gatherly.model.event.EventStatus
import com.android.gatherly.model.event.EventsRepository
import com.android.gatherly.model.map.Location
import com.android.gatherly.model.map.NominatimLocationRepository
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.profile.ProfileRepositoryFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat

data class EditEventsUIState(
    val name : String = "",
    val description : String = "",
    val creatorName : String = "",
    val location : String = "",
    val date : String = "",
    val startTime : String = "",
    val endTime : String = "",
    val participant : String = "",
    val participants : List<Profile> = emptyList(),
    val suggestedProfiles : List<Profile> = emptyList(),
    val suggestedLocations : List<Location> = emptyList(),
    val nameError : Boolean = false,
    val descriptionError : Boolean = false,
    val creatorNameError : Boolean = false,
    val dateError : Boolean = false,
    val startTimeError : Boolean = false,
    val endTimeError : Boolean = false,
    val displayToast : Boolean = false,
    val toastString : String? = null
)

//TODO change everything name to title

@SuppressLint("SimpleDateFormat")
class EditEventsViewModel(
    private val profileRepository: ProfileRepository = ProfileRepositoryFirestore(),
    private val eventsRepository: EventsRepository,
    private val client: NominatimLocationRepository// = NominatimLocationRepository(HttpClientProvider.client)
) : ViewModel() {

    var uiState by mutableStateOf<EditEventsUIState>(EditEventsUIState())
        private set

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy")
    private val timeFormat = SimpleDateFormat("HH:mm")

    private var searchedProfiles : List<Profile> = emptyList()
    private lateinit var eventId : String
    private lateinit var creatorId : String

    init {
        // The string formatter should use strictly the format wanted
        dateFormat.isLenient = false
        timeFormat.isLenient = false
    }

    fun setEventValues(givenEventId : String) {
        viewModelScope.launch {
            val event = eventsRepository.getEvent(givenEventId)
            uiState = uiState.copy(
                name = event.title,
                description = event.description,
                creatorName = event.creatorName,
                location = event.location?.name ?: "",
                date = dateFormat.format(event.date.toDate()),
                startTime = timeFormat.format(event.startTime.toDate()),
                endTime = timeFormat.format(event.endTime.toDate()),
                participants = event.participants.map { profileRepository.getProfileByUid(it)!! }
            )
            eventId = event.id
            creatorId = event.creatorId
        }
    }

    fun clearErrorMsg() {
        uiState = uiState.copy(displayToast = false, toastString = null)
    }

    fun updateName(updatedName : String) {
        uiState = uiState.copy(name = updatedName, nameError = updatedName.isBlank())
    }

    fun updateDescription(updatedDescription : String) {
        uiState = uiState.copy(description = updatedDescription, descriptionError = updatedDescription.isBlank())
    }

    fun updateCreatorName(updatedCreatorName : String) {
        uiState = uiState.copy(creatorName = updatedCreatorName, creatorNameError = updatedCreatorName.isBlank())
    }

    fun updateLocation(updatedLocation : String) {
        uiState = uiState.copy(location = updatedLocation)
    }

    fun updateDate(updatedDate : String) {
        val dateError = try {
            dateFormat.parse(updatedDate)
            true
        } catch (_: ParseException) {
            false
        }
        uiState = uiState.copy(date = updatedDate, dateError = dateError)
    }

    fun updateStartTime(updatedStartTime : String) {
        val startTimeError = try {
            timeFormat.parse(updatedStartTime)
            true
        } catch (_: ParseException) {
            false
        }
        uiState = uiState.copy(startTime = updatedStartTime, startTimeError = startTimeError)
    }

    fun updateEndTime(updatedEndTime : String) {
        val endTimeError = try {
            timeFormat.parse(updatedEndTime)
            true
        } catch (_: ParseException) {
            false
        }
        uiState = uiState.copy(endTime = updatedEndTime, endTimeError = endTimeError)
    }

    fun updateParticipant(updatedParticipant : String) {
        uiState = uiState.copy(participant = updatedParticipant)
    }

    fun deleteParticipant(participant : String) {
        val index = uiState.participants.find {it.uid == participant}
        uiState = if (index == null) {
            uiState.copy(displayToast = true, toastString = "Cannot delete this participant, as they are not participating")
        } else if (participant == creatorId) {
            uiState.copy(displayToast = true, toastString = "Cannot delete the owner")
        } else {
            uiState.copy(participants = uiState.participants.filter {it.uid == participant})
        }
    }

    fun addParticipant(participant : String) {
        searchProfileByString(participant)
        uiState.copy(participant = uiState.participant + searchedProfiles)
    }

    fun searchProfileByString(participant : String) {
        viewModelScope.launch {
            val profilesList = profileRepository.findProfilesByUidSubstring(participant)
            searchedProfiles = profilesList
        }
    }

    fun searchLocationByString(location : String) {
        viewModelScope.launch {
            val list = client.search(location)
            uiState = uiState.copy(suggestedLocations = list)
        }
    }

    fun saveEvent() {
        checkAllEntries()
        if (!uiState.nameError && !uiState.descriptionError && !uiState.creatorNameError
            && !uiState.dateError && !uiState.startTimeError && !uiState.endTimeError) {
            val date = dateFormat.parse(uiState.date) ?: run {
                uiState.copy(displayToast = true, toastString = "Cannot parse event date")
                return
            }
            val timestampDate = Timestamp(date)
            val startTime = timeFormat.parse(uiState.startTime) ?: run {
                uiState.copy(displayToast = true, toastString = "Cannot parse event start time")
                return
            }
            val timestampStartTime = Timestamp(startTime)
            val endTime = timeFormat.parse(uiState.endTime) ?: run {
                uiState.copy(displayToast = true, toastString = "Cannot parse event end time")
                return
            }
            val timestampEndTime = Timestamp(endTime)

            val event = Event(
                id = eventId,
                title = uiState.name,
                description = uiState.description,
                creatorName = uiState.creatorName,
                location = null,
                date = timestampDate,
                startTime = timestampStartTime,
                endTime = timestampEndTime,
                creatorId = creatorId,
                participants = uiState.participants.map { it.uid },
                status = EventStatus.UPCOMING
            )

            uiState = uiState.copy(displayToast = true, toastString = "Saving...")


            viewModelScope.launch {
                eventsRepository.editEvent(eventId, event)
                uiState = uiState.copy(displayToast = true, toastString = "Saved")
            }
        } else {
            uiState = uiState.copy(displayToast = true, toastString = "Failed to save :(")
        }
    }

    fun deleteEvent() {
        viewModelScope.launch {
            eventsRepository.deleteEvent(eventId)
        }
    }

    private fun checkAllEntries() {
        updateName(uiState.name)
        updateDescription(uiState.description)
        updateCreatorName(uiState.creatorName)
        updateDate(uiState.date)
        updateStartTime(uiState.startTime)
        updateEndTime(uiState.endTime)
    }
}