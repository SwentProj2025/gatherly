package com.android.gatherly.ui.map

import android.content.Context
import android.location.Location as AndroidLocation
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.gatherly.model.event.Event
import com.android.gatherly.model.event.EventStatus
import com.android.gatherly.model.event.EventsRepository
import com.android.gatherly.model.event.EventsRepositoryFirestore
import com.android.gatherly.model.map.DisplayedMapElement
import com.android.gatherly.model.map.Location
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todo.ToDosRepository
import com.android.gatherly.model.todo.ToDosRepositoryFirestore
import com.android.gatherly.utils.GenericViewModelFactory
import com.android.gatherly.utils.MapCoordinator
import com.android.gatherly.utils.locationFlow
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/** Default location coordinates for EPFL campus. */
val EPFL_LATLNG = LatLng(46.5197, 6.5663)

/** Timeout duration for fetching user location. */
const val LOCATION_FETCH_TIMEOUT = 5000L

/** UI state for the Map screen. */
data class UIState(
    val itemsList: List<DisplayedMapElement> = emptyList(),
    val selectedItemId: String? = null,
    val lastConsultedTodoId: String? = null,
    val lastConsultedEventId: String? = null,
    val cameraPos: LatLng? = null,
    val errorMsg: String? = null,
    val onSignedOut: Boolean = false,
    val displayEventsPage: Boolean = false,
    val currentUserLocation: LatLng? = null
)

private fun getDrawableTodos(todos: List<ToDo>): List<ToDo> {
  return todos.filter { it.status != ToDoStatus.ENDED && it.location != null }
}

private fun getDrawableEvents(events: List<Event>): List<Event> {
  return events.filter { it.status != EventStatus.PAST && it.location != null }
}

class MapViewModel(
    private val todosRepository: ToDosRepository = ToDosRepositoryFirestore(Firebase.firestore),
    private val eventsRepository: EventsRepository = EventsRepositoryFirestore(Firebase.firestore),
    private val fusedLocationClient: FusedLocationProviderClient? = null,
    private val coordinator: MapCoordinator
) : ViewModel() {

  private val _uiState: MutableStateFlow<UIState> = MutableStateFlow(UIState())
  val uiState: StateFlow<UIState> = _uiState.asStateFlow()

  private var todoList: List<ToDo> = emptyList()
  private var eventsList: List<Event> = emptyList()

  private var locationJob: Job? = null

  private var loadingDataJob: Job? = null

  fun startLocationUpdates(context: Context) {
    locationJob?.cancel()
    locationJob =
        viewModelScope.launch {
          fusedLocationClient?.locationFlow(context)?.collect { location: AndroidLocation ->
            val latLng = LatLng(location.latitude, location.longitude)
            _uiState.update { it.copy(currentUserLocation = latLng) }
          }
        }
  }

  fun stopLocationUpdates() {
    locationJob?.cancel()
    locationJob = null
  }

  private fun toLatLng(location: Location): LatLng {
    return LatLng(location.latitude, location.longitude)
  }

  /**
   * Initializes the camera position based on last consulted item or user location. Suspends until
   * data is loaded to ensure proper centering.
   */
  @Suppress("SuspendFunctionOnCoroutineScope")
  suspend fun initialiseCameraPosition(context: Context) {
    loadingDataJob?.join()

    val pos = fetchLocationToCenterOn(context)
    _uiState.update { it.copy(cameraPos = pos) }
  }

  init {
    loadingDataJob =
        viewModelScope.launch {
          val todos = todosRepository.getAllTodos()
          todoList = getDrawableTodos(todos)

          val events = eventsRepository.getAllEvents()
          eventsList = getDrawableEvents(events)

          _uiState.update { it.copy(itemsList = todoList, displayEventsPage = false) }
        }
  }

  fun onSelectedItem(itemId: String) {
    _uiState.value = _uiState.value.copy(selectedItemId = itemId)
  }

  fun clearSelection() {
    _uiState.value = _uiState.value.copy(selectedItemId = null)
  }

  fun onItemConsulted(itemId: String) {
    if (_uiState.value.displayEventsPage) {
      _uiState.value =
          _uiState.value.copy(
              lastConsultedEventId = itemId, lastConsultedTodoId = null, cameraPos = null)
    } else {
      _uiState.value =
          _uiState.value.copy(
              lastConsultedTodoId = itemId, lastConsultedEventId = null, cameraPos = null)
    }
  }

  fun onNavigationToDifferentScreen() {
    _uiState.update { it.copy(cameraPos = null) }
  }

  fun changeView() {
    if (_uiState.value.displayEventsPage) {
      _uiState.value = _uiState.value.copy(itemsList = todoList, displayEventsPage = false)
    } else {
      _uiState.value = _uiState.value.copy(itemsList = eventsList, displayEventsPage = true)
    }
  }

  @Suppress("SuspendFunctionOnCoroutineScope")
  suspend fun fetchLocationToCenterOn(context: Context): LatLng {
    if (_uiState.value.lastConsultedTodoId != null) {
      val todo = todoList.find { it.uid == _uiState.value.lastConsultedTodoId }
      if (todo?.location != null) {
        return toLatLng(todo.location)
      }
    }

    coordinator.getUnconsumedEventId()?.let { eventId ->
      eventsList
          .find { it.id == eventId }
          ?.location
          ?.let { location ->
            coordinator.markConsumed()
            // Switch to events view and update itemsList
            _uiState.update { it.copy(displayEventsPage = true, itemsList = eventsList) }
            return toLatLng(location)
          }
    }

    if (_uiState.value.lastConsultedEventId != null) {
      val event = eventsList.find { it.id == _uiState.value.lastConsultedEventId }
      if (event?.location != null) {
        return toLatLng(event.location)
      }
    }

    try {
      val currentLocation =
          withTimeoutOrNull(LOCATION_FETCH_TIMEOUT) {
            fusedLocationClient?.locationFlow(context)?.first()
          }

      if (currentLocation != null) {
        return LatLng(currentLocation.latitude, currentLocation.longitude)
      }
    } catch (e: Exception) {
      // Permission denied or other error
      // Ignore this to fall back to EPFL
    }

    return EPFL_LATLNG
  }

  fun signOut(credentialManager: CredentialManager) {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(onSignedOut = true)
      Firebase.auth.signOut()
      credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
  }

  companion object {
    fun provideFactory(
        todosRepository: ToDosRepository = ToDosRepositoryFirestore(Firebase.firestore),
        eventsRepository: EventsRepository = EventsRepositoryFirestore(Firebase.firestore),
        fusedLocationClient: FusedLocationProviderClient? = null,
        coordinator: MapCoordinator
    ): ViewModelProvider.Factory {
      return GenericViewModelFactory {
        MapViewModel(todosRepository, eventsRepository, fusedLocationClient, coordinator)
      }
    }
  }
}
