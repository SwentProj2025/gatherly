package com.android.gatherly.ui.events

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.gatherly.R
import com.android.gatherly.model.event.Event
import com.android.gatherly.model.event.EventState
import com.android.gatherly.model.event.EventStatus
import com.android.gatherly.model.event.EventsRepository
import com.android.gatherly.model.event.EventsRepositoryFirestore
import com.android.gatherly.model.map.Location
import com.android.gatherly.model.notification.NotificationsRepository
import com.android.gatherly.model.notification.NotificationsRepositoryProvider
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.profile.ProfileRepositoryFirestore
import com.android.gatherly.utils.GenericViewModelFactory
import com.android.gatherly.utils.distance
import com.android.gatherly.utils.locationFlow
import com.android.gatherly.utils.userParticipate
import com.android.gatherly.utils.userUnregister
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.storage
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state for the Events ViewModel
 *
 * @property fullEventList list of events
 * @property participatedEventList list of events the current user is participating in
 * @property createdEventList list of events the current user has created
 * @property globalEventList list of events neither created by nor participated in by current user
 * @property errorMsg optional error message to display
 * @property currentUserId the id of the current user
 * @property isAnon whether the current user is anonymous
 * @property isLoading whether the data is currently loading
 * @property sortOrder the current sorting order for the events list
 */
data class EventsOverviewUIState(
    val fullEventList: List<Event> = emptyList(),
    val participatedEventList: List<Event> = emptyList(),
    val createdEventList: List<Event> = emptyList(),
    val globalEventList: List<Event> = emptyList(),
    val errorMsg: String? = null,
    val currentUserId: String = "",
    val isAnon: Boolean = true,
    val isLoading: Boolean = true,
    val sortOrder: EventSortOrder = EventSortOrder.DATE_ASC,
)

/**
 * Data class with all the needed events list
 *
 * @property fullEventList list of all events
 * @property participatedEventList list of events the current user is participating in
 * @property createdEventList list of events the current user has created
 * @property globalEventList list of events neither created by nor participated in by current user
 */
private data class ProcessedEvents(
    val fullEventList: List<Event>,
    val participatedEventList: List<Event>,
    val createdEventList: List<Event>,
    val globalEventList: List<Event>
)

/**
 * Specifies the available sorting criteria for the Events list.
 *
 * @property DATE_ASC : Sort events by increasing date (earliest first)
 * @property ALPHABETICAL : Sort events by their name in alphabetical order (A -> Z).
 * @property PROXIMITY : Sort events by their proximity (nearest first)
 */
enum class EventSortOrder {
  DATE_ASC,
  ALPHABETICAL,
  PROXIMITY
}

/**
 * ViewModel for the Events screen.
 *
 * @param profileRepository the repository to fetch user profiles from
 * @param eventsRepository the repository to fetch events from
 * @param authProvider function that provides the FirebaseAuth instance
 * @param fusedLocationClient the FusedLocationProviderClient for location updates
 * @param fakeCurrentUserLocation optional fake location for testing purposes
 */
class EventsOverviewViewModel(
    private val profileRepository: ProfileRepository,
    private val eventsRepository: EventsRepository,
    private val authProvider: () -> FirebaseAuth = { Firebase.auth },
    private val fusedLocationClient: FusedLocationProviderClient? = null,
    private val fakeCurrentUserLocation: Location? = null,
    val notificationsRepository: NotificationsRepository =
        NotificationsRepositoryProvider.repository,
) : ViewModel() {

  // MutableStateFlow holding the current UI state
  private val _uiState: MutableStateFlow<EventsOverviewUIState> =
      MutableStateFlow(EventsOverviewUIState())

  /**
   * StateFlow exposing the current UI state, including all event lists categorized by user
   * relationship.
   */
  val uiState: StateFlow<EventsOverviewUIState> = _uiState.asStateFlow()

  private val _editEventRequest = MutableStateFlow<Event?>(null)

  /** StateFlow exposing the event currently being edited, or null if no edit is in progress. */
  val editEventRequest: StateFlow<Event?> = _editEventRequest.asStateFlow()

  // MutableStateFlow holding the current search query
  private val _searchQuery = MutableStateFlow("")

  // Cache of all events fetched from the repository
  private var allEventsCache: List<Event> = emptyList()

  // MutableStateFlow holding the current user location
  private val _currentUserLocation = MutableStateFlow<Location?>(null)

  /**
   * Initializes the ViewModel by loading all events for the current user. Events are automatically
   * categorized into created, participated, and global lists.
   */
  init {
    viewModelScope.launch { refreshEvents(authProvider().currentUser?.uid ?: "") }
  }

  /**
   * Refreshes the event lists based on the current user ID.
   *
   * @param currentUserId the ID of the current user
   */
  suspend fun refreshEvents(currentUserId: String) {
    _uiState.value = _uiState.value.copy(isLoading = true)
    val events = eventsRepository.getAllEvents()

    allEventsCache = events
    updateUIStateWithProcessedEvents(currentUserId)

    _uiState.value =
        _uiState.value.copy(
            isLoading = false,
            currentUserId = currentUserId,
            isAnon = authProvider().currentUser?.isAnonymous ?: true)
  }

  /**
   * Applies the current search filter and sort order to the cached events, and updates the UI State
   * with the categorized lists.
   *
   * @param currentUserId the ID of the current user
   */
  private fun updateUIStateWithProcessedEvents(currentUserId: String) {
    val processedEvents = processEvents(allEventsCache, currentUserId)

    _uiState.value =
        _uiState.value.copy(
            fullEventList = processedEvents.fullEventList,
            participatedEventList = processedEvents.participatedEventList,
            createdEventList = processedEvents.createdEventList,
            globalEventList = processedEvents.globalEventList)
  }

  /**
   * Handles user participation in an event.
   *
   * @param eventId the ID of the event to participate in
   * @param currentUserId the ID of the current user
   */
  fun onParticipate(eventId: String, currentUserId: String) {
    viewModelScope.launch {
      userParticipate(
          eventsRepository, profileRepository, notificationsRepository, eventId, currentUserId)
      refreshEvents(currentUserId)
    }
  }

  /**
   * Handles user unregistration from an event.
   *
   * @param eventId the ID of the event to unregister from
   * @param currentUserId the ID of the current user
   */
  fun onUnregister(eventId: String, currentUserId: String) {
    viewModelScope.launch {
      userUnregister(eventsRepository, profileRepository, eventId, currentUserId)
      refreshEvents(currentUserId)
    }
  }
  /**
   * Requests to edit an event.
   *
   * @param event the event to be edited
   */
  fun requestEditEvent(event: Event) {
    _editEventRequest.value = event
  }

  /**
   * Handles the editing of an event.
   *
   * @param eventId the ID of the event to be edited
   * @param newEvent the new event data
   * @param currentUserId the ID of the current user
   */
  fun onEditEvent(eventId: String, newEvent: Event, currentUserId: String) {
    viewModelScope.launch {
      eventsRepository.editEvent(eventId, newEvent)
      refreshEvents(currentUserId)
    }
    _editEventRequest.value = null
  }

  /**
   * Companion Object used to encapsulate a static method to retrieve a ViewModelProvider.Factory
   * and its default dependencies.
   *
   * @param eventsRepository The EventsRepository to use (default is EventsRepositoryFirestore).
   * @param profileRepository The ProfileRepository to use (default is ProfileRepositoryFirestore).
   * @param fusedLocationClient The FusedLocationProviderClient for location updates (optional).
   * @return A ViewModelProvider.Factory for creating EventsOverviewViewModel instances.
   */
  companion object {
    fun provideFactory(
        eventsRepository: EventsRepository = EventsRepositoryFirestore(Firebase.firestore),
        profileRepository: ProfileRepository =
            ProfileRepositoryFirestore(
                com.google.firebase.Firebase.firestore, com.google.firebase.Firebase.storage),
        fusedLocationClient: FusedLocationProviderClient? = null,
    ): ViewModelProvider.Factory {

      return GenericViewModelFactory {
        EventsOverviewViewModel(
            profileRepository = profileRepository,
            eventsRepository = eventsRepository,
            fusedLocationClient = fusedLocationClient)
      }
    }
  }

  /**
   * Helper function : return the list of events filtered according to the selected filter status
   *
   * @param selectedFilter the filter selected by the user
   * @param listEvents the list of events to filter
   * @return the filtered list of events
   */
  fun getFilteredEvents(
      selectedFilter: MutableState<EventFilter>,
      listEvents: List<Event>
  ): List<Event> {
    return when (selectedFilter.value) {
      EventFilter.ALL -> listEvents
      EventFilter.UPCOMING -> listEvents.filter { it.status == EventStatus.UPCOMING }
      EventFilter.ONGOING -> listEvents.filter { it.status == EventStatus.ONGOING }
      EventFilter.PAST -> listEvents.filter { it.status == EventStatus.PAST }
    }
  }

  /**
   * Invoked when users type in the search bar to filter [Event]s according to the typed query.
   *
   * @param query The current search query entered by the user.
   * @param currentUserId the id of the current user
   */
  fun searchEvents(query: String, currentUserId: String) {
    _searchQuery.value = query
    updateUIStateWithProcessedEvents(currentUserId)
  }

  /**
   * Updates the current sorting order of the UI and applies it immediately to the currently
   * displayed list of [Event]s.
   *
   * @param order The new [EventSortOrder] selected by the user.
   */
  fun setSortOrder(order: EventSortOrder) {
    _uiState.value = _uiState.value.copy(sortOrder = order)
    viewModelScope.launch {
      val currentUserId = _uiState.value.currentUserId
      if (currentUserId.isNotBlank()) {
        updateUIStateWithProcessedEvents(currentUserId)
      }
    }
  }

  /**
   * Applies the current sorting rule to the given list of [Event]s and return it.
   *
   * @param list The list of [Event]s to sort.
   * @return A new list sorted according to the active sort order.
   */
  private fun applySortOrder(list: List<Event>): List<Event> {
    return when (_uiState.value.sortOrder) {
      EventSortOrder.ALPHABETICAL -> list.sortedBy { it.title.lowercase() }
      EventSortOrder.DATE_ASC -> list.sortedBy { it.date.toDate() }
      EventSortOrder.PROXIMITY -> {
        val userLocation = fakeCurrentUserLocation ?: _currentUserLocation.value
        if (userLocation == null) {
          list.sortedBy { it.title.lowercase() }
        } else {
          list.sortedWith(
              compareBy { event ->
                if (event.location == null) {
                  Double.MAX_VALUE
                } else {
                  distance(userLocation, event.location)
                }
              })
        }
      }
    }
  }

  /**
   * Handle the evolution of the list, depending on the searching, sorting needed
   *
   * @param allEvents list of all the events
   * @param currentUserId the id of the current user
   * @return ProcessedEvents containing all categorized event lists
   */
  private fun processEvents(allEvents: List<Event>, currentUserId: String): ProcessedEvents {
    val searchFiltered =
        if (_searchQuery.value.isNotBlank()) {
          val normalized = _searchQuery.value.trim().lowercase()
          allEvents.filter { event -> event.title.lowercase().contains(normalized) }
        } else {
          allEvents
        }

    val sorted = applySortOrder(searchFiltered)

    val participatedEventList =
        sorted.filter { it.participants.contains(currentUserId) && it.creatorId != currentUserId }

    val createdEventList = sorted.filter { it.creatorId == currentUserId }

    val globalEventList =
        sorted.filter {
          it.creatorId != currentUserId &&
              !it.participants.contains(currentUserId) &&
              conditionToParticipate(it, currentUserId)
        }

    return ProcessedEvents(
        fullEventList = sorted,
        participatedEventList = participatedEventList,
        createdEventList = createdEventList,
        globalEventList = globalEventList)
  }

  /** Function to trigger all the name from the list of participants in order to display them */
  private val _participantsNames = MutableStateFlow<List<String>>(emptyList())
  val participantsNames: StateFlow<List<String>> = _participantsNames

  /**
   * Loads the names of participants given their IDs, replacing the current user ID with "YOU".
   *
   * @param listIds List of participant IDs.
   * @param currentUserId The ID of the current user.
   */
  fun loadParticipantsNames(listIds: List<String>, currentUserId: String) {
    viewModelScope.launch {
      val names =
          listIds.map { id ->
            if (id != currentUserId) {
              val profile = profileRepository.getProfileByUid(id)
              profile?.name ?: R.string.events_anonymous_attendees.toString()
            } else {
              R.string.events_current_user_attendees.toString()
            }
          }
      _participantsNames.value = names
    }
  }

  /**
   * Handles the creation of the string who represents the distance between the user and the event
   * location
   *
   * @param event the event that the user wants to know his proximity with
   * @return the string representing the distance (in km or m) or null if location is not available
   */
  fun getDistanceUserEvent(event: Event): String? {
    val userLocation = fakeCurrentUserLocation ?: _currentUserLocation.value
    if (userLocation != null && event.location != null) {
      val distanceInKilometers = distance(userLocation, event.location)
      return if (distanceInKilometers >= 1.0) {
        String.format(Locale.getDefault(), "%.1f km", distanceInKilometers)
      } else {
        val distanceInMeters = distanceInKilometers * 1000
        String.format(Locale.getDefault(), "%.0f m", distanceInMeters)
      }
    }
    return null
  }

  /**
   * Initialize the first position of the current user
   *
   * @param context The context used to access location services.
   */
  fun startLocationUpdates(context: Context) {
    fusedLocationClient ?: return

    viewModelScope.launch {
      try {
        fusedLocationClient.locationFlow(context).collect { loc ->
          updateCurrentUserLocation(
              Location(
                  latitude = loc.latitude, longitude = loc.longitude, name = "currentUserLocation"))
        }
      } catch (e: Exception) {
        Log.e("EventsOverviewViewModel", "Error getting location: ${e.message}")
      }
    }
  }

  /**
   * Update the location of the current user by the launch trigger from the UI
   *
   * @param newLocation the new location of the current user to register
   */
  fun updateCurrentUserLocation(newLocation: Location) {
    _currentUserLocation.value = newLocation
    if (_uiState.value.sortOrder == EventSortOrder.PROXIMITY) {
      viewModelScope.launch { updateUIStateWithProcessedEvents(_uiState.value.currentUserId) }
    }
  }

  /**
   * Helper private function : return true if the user can participate to the event
   *
   * @param event the event to check
   * @param currentUserId the id of the current user
   * @return Boolean indicating if the user can participate to the event depending on its state
   */
  private fun conditionToParticipate(event: Event, currentUserId: String): Boolean {
    return when (event.state) {
      EventState.PUBLIC -> true
      EventState.PRIVATE_GROUP -> false
      EventState.PRIVATE_FRIENDS -> {
        val final = false
        viewModelScope.launch {
          val creatorProfile = profileRepository.getProfileByUid(event.creatorId)
          if (creatorProfile == null) {
            false
          } else {
            val friendsIdsList = creatorProfile.friendUids
            friendsIdsList.contains(currentUserId)
          }
        }
        return final
      }
    }
  }
}
