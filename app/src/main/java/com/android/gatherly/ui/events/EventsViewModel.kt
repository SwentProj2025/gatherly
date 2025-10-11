package com.android.gatherly.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.gatherly.model.event.Event
import com.android.gatherly.model.event.EventStatus
import com.android.gatherly.model.event.EventsRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** UI state for the Events ViewModel.
 *
 * @param fullEventList list of events
 * @param participatedEventList list of events the current user is participating in
 * @param createdEventList list of events the current user has created
 * @param globalEventList list of events neither created by nor participated in by current user
 */
data class UIState(
    val fullEventList: List<Event> = emptyList(),
    val participatedEventList: List<Event> = emptyList(),
    val createdEventList: List<Event> = emptyList(),
    val globalEventList: List<Event> = emptyList() // Events neither created by nor participated in by current user
)
/**
 * Function that retrieves "drawable" events, i.e. those which are not past, and have a valid
 * location.
 *
 * @param events input list of events to filter from
 * @return list of drawable events
 */
private fun getDrawableEvents(events: List<Event>): List<Event> {
    return events.filter { it.status != EventStatus.PAST && it.location != null }
}

/**
 * ViewModel for the Events screen.
 *
 * @param repository the repository to fetch events from
 */

class EventsViewModel(private val repository: EventsRepository) : ViewModel() {
    private val _uiState: MutableStateFlow<UIState> = MutableStateFlow(UIState())
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()

    private val _editEventRequest = MutableStateFlow<Event?>(null)
    val editEventRequest: StateFlow<Event?> = _editEventRequest.asStateFlow()

    init {
        viewModelScope.launch {
            val currentUserId = Firebase.auth.currentUser?.uid ?: throw Exception("User not authenticated")
            refreshEvents(currentUserId)
        }
    }

    /** Refreshes the event lists based on the current user ID.
     *
     * @param currentUserId the ID of the current user
     */
    private suspend fun refreshEvents(currentUserId: String) {
        val events = repository.getAllEvents()
        _uiState.value = _uiState.value.copy(
            fullEventList = events,
            participatedEventList = events.filter { it.participants.contains(currentUserId) },
            createdEventList = events.filter { it.creatorId == currentUserId },
            globalEventList = events.filter {
                it.creatorId != currentUserId && !it.participants.contains(currentUserId)
            }
        )
    }

    /** Handles user participation in an event.
     *
     * @param eventId the ID of the event to participate in
     * @param currentUserId the ID of the current user
     */
    fun onParticipate(eventId: String, currentUserId: String) {
        viewModelScope.launch {
            repository.addParticipant(eventId, currentUserId)
            refreshEvents(currentUserId)
        }
    }
    /** Handles user unregistration from an event.
     *
     * @param eventId the ID of the event to unregister from
     * @param currentUserId the ID of the current user
     */
    fun onUnregister(eventId: String, currentUserId: String) {
        viewModelScope.launch {
            repository.removeParticipant(eventId, currentUserId)
            refreshEvents(currentUserId)
        }
    }
    /** Requests to edit an event.
     *
     * @param event the event to be edited
     */
    fun requestEditEvent(event: Event) {
        _editEventRequest.value = event
    }

    /** Handles the editing of an event.
     *
     * @param eventId the ID of the event to be edited
     * @param newEvent the new event data
     * @param currentUserId the ID of the current user
     */
    fun onEditEvent(eventId: String, newEvent: Event, currentUserId: String) {
        viewModelScope.launch {
            repository.editEvent(eventId, newEvent)
            refreshEvents(currentUserId)
        }
        _editEventRequest.value = null
    }
}