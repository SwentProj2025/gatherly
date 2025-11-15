package com.android.gatherly.ui.todo

import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todo.ToDosRepository
import com.android.gatherly.model.todo.ToDosRepositoryProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the
// SwEnt staff.

/**
 * Represents the UI state for the Overview screen.
 *
 * @property todos A list of `ToDo` items to be displayed in the Overview screen. Defaults to an
 *   empty list if no items are available.
 */
data class OverviewUIState(
    val todos: List<ToDo> = emptyList(),
    val errorMsg: String? = null,
    val isLoading: Boolean = false,
    val signedOut: Boolean = false
)

/**
 * ViewModel for the Overview screen.
 *
 * Responsible for managing the UI state, by fetching and providing ToDo items via the
 * [ToDosRepository].
 *
 * @property todoRepository The repository used to fetch and manage ToDo items.
 */
class OverviewViewModel(
    private val todoRepository: ToDosRepository = ToDosRepositoryProvider.repository,
) : ViewModel() {

  private val _uiState = MutableStateFlow(OverviewUIState())
  val uiState: StateFlow<OverviewUIState> = _uiState.asStateFlow()

  private var allTodosCache: List<ToDo> = emptyList()

  init {
    getAllTodos()
  }

  /** Refreshes the UI state by fetching all ToDo items from the repository. */
  fun refreshUIState() {
    getAllTodos()
  }

  /** Fetches all todos from the repository and updates the UI state. */
  private fun getAllTodos() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, errorMsg = null)
      try {
        val todos = todoRepository.getAllTodos()
        allTodosCache = todos
        _uiState.value = OverviewUIState(todos = todos, isLoading = false)
      } catch (e: Exception) {
        _uiState.value =
            _uiState.value.copy(todos = emptyList(), errorMsg = e.message, isLoading = false)
      }
    }
  }

  /** Invoked when the user clicks on the checkbox, to mark the ToDo as completed or ongoing. */
  fun onCheckboxChanged(uid: String, newStatus: ToDoStatus) {
    viewModelScope.launch {
      val updatedTodo = todoRepository.getTodo(uid).copy(status = newStatus)
      todoRepository.editTodo(uid, updatedTodo)
      refreshUIState()
    }
  }

  /** Invoked when users type in the search bar to filter todos according to the typed query. */
  fun searchTodos(query: String) {
    val normalized = query.trim().lowercase()
    if (normalized.isEmpty()) {
      refreshUIState()
      return
    }
    val filtered =
        allTodosCache.filter {
          it.name.lowercase().contains(normalized) ||
              it.description.lowercase().contains(normalized)
        }
    _uiState.value = _uiState.value.copy(todos = filtered)
  }

  /** Initiates sign-out */
  fun onSignedOut(credentialManager: CredentialManager): Unit {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(signedOut = true)
      Firebase.auth.signOut()
      credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
  }
}
