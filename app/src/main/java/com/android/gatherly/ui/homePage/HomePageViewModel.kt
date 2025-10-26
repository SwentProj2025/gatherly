package com.android.gatherly.ui.homePage

import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.gatherly.model.event.Event
import com.android.gatherly.model.event.EventStatus
import com.android.gatherly.model.event.EventsRepository
import com.android.gatherly.model.event.EventsRepositoryFirestore
import com.android.gatherly.model.map.DisplayedMapElement
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.profile.ProfileRepositoryFirestore
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todo.ToDosRepository
import com.android.gatherly.model.todo.ToDosRepositoryFirestore
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomePageUIState(
    val displayableTodos: List<ToDo> = emptyList(),
    val displayableEvents: List<Event> = emptyList(),
    val expandedElement: DisplayedMapElement? = null,
    val friends: List<Profile> = emptyList(),
    val todos: List<ToDo> = emptyList(),
    val timerString: String = "Are you ready to focus?",
    val errorMsg: String? = null,
    val signedOut: Boolean = false
)

/**
 * Filters todos to return only those that should be displayed on the map. A todo is drawable if it
 * is not complete and has a valid location.
 *
 * @param todos The list of todos to filter.
 * @return List of todos that can be drawn on the map.
 */
private fun getDrawableTodos(todos: List<ToDo>): List<ToDo> {
  return todos.filter { it.status != ToDoStatus.ENDED && it.location != null }
}

/**
 * Filters events to return only those that should be displayed on the map. An event is drawable if
 * it is not past and has a valid location.
 *
 * @param events The list of events to filter.
 * @return List of events that can be drawn on the map.
 */
private fun getDrawableEvents(events: List<Event>): List<Event> {
  return events.filter { it.status != EventStatus.PAST && it.location != null }
}

class HomePageViewModel(
    private val eventsRepository: EventsRepository = EventsRepositoryFirestore(Firebase.firestore),
    private val toDosRepository: ToDosRepository = ToDosRepositoryFirestore(Firebase.firestore),
    private val profileRepository: ProfileRepository =
        ProfileRepositoryFirestore(Firebase.firestore)
) : ViewModel() {

  private val _uiState = MutableStateFlow(HomePageUIState())
  val uiState: StateFlow<HomePageUIState> = _uiState.asStateFlow()

  init {
    viewModelScope.launch {
      val todos = toDosRepository.getAllTodos()
      val events = eventsRepository.getAllEvents()
      val profile = profileRepository.getProfileByUid(Firebase.auth.currentUser?.uid!!)!!
      val friends = profile.friendUids.take(3).map { profileRepository.getProfileByUid(it)!! }

      _uiState.value =
          _uiState.value.copy(
              displayableTodos = getDrawableTodos(todos),
              displayableEvents = getDrawableEvents(events),
              friends = friends,
              todos = todos.take(3))
    }
  }

  /**
   * Handles a tap on a displayable element so that it can be expanded. Clicking on an already
   * expanded element collapses it.
   *
   * @param displayable the item we want to expand
   */
  fun selectItem(displayable: DisplayedMapElement) {
    if (_uiState.value.expandedElement == displayable) {
      _uiState.value = _uiState.value.copy(expandedElement = null)
    } else {
      _uiState.value = _uiState.value.copy(expandedElement = displayable)
    }
  }

  /** Handles the dismissal of an expanded element */
  fun dismissItem() {
    _uiState.value = _uiState.value.copy(expandedElement = null)
  }

  /** Initiates sign-out */
  fun signOut(credentialManager: CredentialManager): Unit {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(signedOut = true)
      Firebase.auth.signOut()
      credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
  }
}
