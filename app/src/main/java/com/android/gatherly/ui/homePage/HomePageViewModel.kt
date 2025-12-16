package com.android.gatherly.ui.homePage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.gatherly.model.event.Event
import com.android.gatherly.model.event.EventStatus
import com.android.gatherly.model.event.EventsRepository
import com.android.gatherly.model.event.EventsRepositoryProvider
import com.android.gatherly.model.notification.NotificationsRepository
import com.android.gatherly.model.notification.NotificationsRepositoryProvider
import com.android.gatherly.model.points.PointsRepository
import com.android.gatherly.model.points.PointsRepositoryProvider
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state holder for the Home Page screen.
 *
 * Contains all data required to render the Home Page, including tasks, events, friends, and user
 * status.
 *
 * @param displayableTodos Todos that can be displayed on the map.
 * @param displayableEvents Events that can be displayed on the map.
 * @param friends List of the user's friends.
 * @param todos Full list of the user's todos.
 * @param errorMsg Optional error message displayed when loading fails.
 * @param isAnon Whether the current user is anonymous.
 */
data class HomePageUIState(
    val displayableTodos: List<ToDo> = emptyList(),
    val displayableEvents: List<Event> = emptyList(),
    val friends: List<Profile> = emptyList(),
    val todos: List<ToDo> = emptyList(),
    val errorMsg: String? = null,
    val isAnon: Boolean = true
)

/**
 * ViewModel for the Home Page screen.
 *
 * Responsible for:
 * - Fetching todos, events, and profile data
 * - Resolving friends and notification state
 * - Exposing a single immutable UI state via [StateFlow]
 *
 * @param eventsRepository Repository used to fetch events.
 * @param toDosRepository Repository used to fetch todos.
 * @param profileRepository Repository used to fetch profiles.
 * @param pointsRepository Repository used to fetch points data.
 * @param notificationsRepository Repository used to fetch notifications.
 * @param authProvider Provider for the current [FirebaseAuth] instance.
 */
class HomePageViewModel(
    private val eventsRepository: EventsRepository = EventsRepositoryProvider.repository,
    private val toDosRepository: ToDosRepository = ToDosRepositoryProvider.repository,
    private val profileRepository: ProfileRepository = ProfileRepositoryProvider.repository,
    private val pointsRepository: PointsRepository = PointsRepositoryProvider.repository,
    private val notificationsRepository: NotificationsRepository =
        NotificationsRepositoryProvider.repository,
    private val authProvider: () -> FirebaseAuth = { Firebase.auth }
) : ViewModel() {

  private val _uiState = MutableStateFlow(HomePageUIState())
  /**
   * Public UI state exposed to the Home Page screen.
   *
   * This state should be observed by the UI layer and treated as immutable.
   */
  val uiState: StateFlow<HomePageUIState> = _uiState.asStateFlow()

  init {
    updateUI()
  }

  /**
   * Refreshes the Home Page UI state by fetching data from repositories.
   *
   * Loads todos, events, profile information, and friends, then updates the exposed [uiState]. In
   * case of failure, an error message is set.
   */
  fun updateUI() {
    viewModelScope.launch {
      try {
        val todos = toDosRepository.getAllTodos()
        val events = eventsRepository.getAllEvents()
        val profile =
            getProfileWithSyncedFriendNotifications(
                profileRepository,
                notificationsRepository,
                pointsRepository,
                authProvider().currentUser?.uid!!)!!
        val friends = profile.friendUids.map { profileRepository.getProfileByUid(it)!! }
        val isAnon = authProvider().currentUser?.isAnonymous ?: true

        _uiState.value =
            _uiState.value.copy(
                displayableTodos = getDrawableTodos(todos),
                displayableEvents = getDrawableEvents(events),
                friends = friends,
                todos = todos,
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
}
