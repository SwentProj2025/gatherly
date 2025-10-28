package com.android.gatherly.ui.homePage

import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.gatherly.model.event.Event
import com.android.gatherly.model.event.EventsRepository
import com.android.gatherly.model.event.EventsRepositoryFirestore
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.profile.ProfileRepositoryFirestore
import com.android.gatherly.model.todo.ToDo
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
    val friends: List<Profile> = emptyList(),
    val todos: List<ToDo> = emptyList(),
    val timerString: String = "",
    val errorMsg: String? = null,
    val signedOut: Boolean = false
)

class HomePageViewModel(
    private val eventsRepository: EventsRepository = EventsRepositoryFirestore(Firebase.firestore),
    private val toDosRepository: ToDosRepository = ToDosRepositoryFirestore(Firebase.firestore),
    private val profileRepository: ProfileRepository =
        ProfileRepositoryFirestore(Firebase.firestore)
) : ViewModel() {

  private val _uiState = MutableStateFlow(HomePageUIState())
  val uiState: StateFlow<HomePageUIState> = _uiState.asStateFlow()

  /** Initiates sign-out */
  fun signOut(credentialManager: CredentialManager): Unit {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(signedOut = true)
      Firebase.auth.signOut()
      credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
  }
}
