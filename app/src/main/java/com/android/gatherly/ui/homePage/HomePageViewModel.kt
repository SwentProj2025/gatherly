package com.android.gatherly.ui.homePage

import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.gatherly.model.event.Event
import com.android.gatherly.model.event.EventStatus
import com.android.gatherly.model.event.EventsRepository
import com.android.gatherly.model.event.EventsRepositoryFirestore
import com.android.gatherly.model.notification.NotificationsRepository
import com.android.gatherly.model.notification.NotificationsRepositoryProvider
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.profile.ProfileRepositoryProvider
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todo.ToDosRepository
import com.android.gatherly.model.todo.ToDosRepositoryProvider
import com.android.gatherly.utils.getProfileWithSyncedFriendNotifications
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomePageUIState(
    val displayableTodos: List<ToDo> = emptyList(),
    val displayableEvents: List<Event> = emptyList(),
    val friends: List<Profile> = emptyList(),
    val todos: List<ToDo> = emptyList(),
    val timerString: String = "Are you ready to focus?",
    val errorMsg: String? = null,
    val signedOut: Boolean = false,
    val isAnon: Boolean = true
)

class HomePageViewModel(
    private val eventsRepository: EventsRepository = EventsRepositoryFirestore(Firebase.firestore),
    private val toDosRepository: ToDosRepository = ToDosRepositoryProvider.repository,
    private val profileRepository: ProfileRepository = ProfileRepositoryProvider.repository,
    private val notificationsRepository: NotificationsRepository =
        NotificationsRepositoryProvider.repository,
    private val authProvider: () -> FirebaseAuth = { Firebase.auth }
) : ViewModel() {

  private val _uiState = MutableStateFlow(HomePageUIState())
  val uiState: StateFlow<HomePageUIState> = _uiState.asStateFlow()

  init {
    updateUI()
  }

  /** Updates the [uiState] with possibly new values from repositories */
  fun updateUI() {
    viewModelScope.launch {
      try {
        val todos = toDosRepository.getAllTodos()
        val events = eventsRepository.getAllEvents()
        val profile =
            getProfileWithSyncedFriendNotifications(
                profileRepository, notificationsRepository, authProvider().currentUser?.uid!!)!!
        val friends = profile.friendUids.take(3).map { profileRepository.getProfileByUid(it)!! }
        val isAnon = authProvider().currentUser?.isAnonymous ?: true

        _uiState.value =
            _uiState.value.copy(
                displayableTodos = getDrawableTodos(todos),
                displayableEvents = getDrawableEvents(events),
                friends = friends,
                todos = todos.take(3),
                isAnon = isAnon)
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(errorMsg = "There was an error loading your home page")
        Log.e("Homepage loading", "Exception when loading lists for Homepage displaying: $e")
      }
    }
  }

  /**
   * Filters todos to return only those that should be displayed on the map. A todo is drawable if
   * it is not complete and has a valid location.
   *
   * @param todos The list of todos to filter.
   * @return List of todos that can be drawn on the map.
   */
  private fun getDrawableTodos(todos: List<ToDo>): List<ToDo> {
    return todos.filter { it.status != ToDoStatus.ENDED && it.location != null }
  }

  /**
   * Filters events to return only those that should be displayed on the map. An event is drawable
   * if it is not past and has a valid location.
   *
   * @param events The list of events to filter.
   * @return List of events that can be drawn on the map.
   */
  private fun getDrawableEvents(events: List<Event>): List<Event> {
    return events.filter { it.status != EventStatus.PAST && it.location != null }
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
