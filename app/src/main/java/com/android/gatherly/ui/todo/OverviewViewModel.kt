package com.android.gatherly.ui.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.profile.ProfileRepositoryProvider
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todo.ToDosRepository
import com.android.gatherly.model.todo.ToDosRepositoryProvider
import com.android.gatherly.utils.editTodo
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
    val sortOrder: TodoSortOrder = TodoSortOrder.ALPHABETICAL,
    val errorMsg: String? = null,
    val isLoading: Boolean = false
)

/**
 * Specifies the available sorting criteria for the ToDo overview list.
 * - [DATE_ASC]: Sort tasks by increasing due date (earliest first).
 * - [DATE_DESC]: Sort tasks by decreasing due date (latest first).
 * - [ALPHABETICAL]: Sort tasks by their name in alphabetical order (A -> Z).
 */
enum class TodoSortOrder {
  DATE_ASC,
  DATE_DESC,
  ALPHABETICAL
}

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
    private val profileRepository: ProfileRepository = ProfileRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(OverviewUIState())
  val uiState: StateFlow<OverviewUIState> = _uiState.asStateFlow()

  private var allTodosCache: List<ToDo> = emptyList()

  init {
    getAllTodos()
  }

  /** Refreshes the UI state by fetching all [TODO] items from the repository. */
  fun refreshUIState() {
    getAllTodos()
  }

  /** Fetches all [TODO]s from the repository and updates the UI state. */
  private fun getAllTodos() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, errorMsg = null)
      try {
        val todos = todoRepository.getAllTodos()
        allTodosCache = todos
        val orderedTodos = applySortOrder(todos)
        _uiState.value =
            OverviewUIState(
                todos = orderedTodos, isLoading = false, sortOrder = _uiState.value.sortOrder)
      } catch (e: Exception) {
        _uiState.value =
            _uiState.value.copy(todos = emptyList(), errorMsg = e.message, isLoading = false)
      }
    }
  }

  /** Invoked when the user clicks on the checkbox, to mark the [TODO] as completed or ongoing. */
  fun onCheckboxChanged(uid: String, newStatus: ToDoStatus) {
    viewModelScope.launch {
      val ownerId = todoRepository.getTodo(uid).ownerId
      editTodo(todoRepository, profileRepository, uid, newStatus, ownerId)
      refreshUIState()
    }
  }

  /** Invoked when users type in the search bar to filter [TODO]s according to the typed query. */
  fun searchTodos(query: String) {
    val normalized = query.trim().lowercase()
    if (normalized.isEmpty()) {
      // When query is empty in the search bar, we display the full sorted list:
      val sorted = applySortOrder(allTodosCache)
      _uiState.value = _uiState.value.copy(todos = sorted)
      return
    }
    val filtered =
        allTodosCache.filter {
          it.name.lowercase().contains(normalized) ||
              it.description.lowercase().contains(normalized)
        }
    val sortedFiltered = applySortOrder(filtered)
    _uiState.value = _uiState.value.copy(todos = sortedFiltered)
  }

  /**
   * Updates the current sorting order of the UI and applies it immediately to the currently
   * displayed list of [TODO]s.
   *
   * @param order The new [TodoSortOrder] selected by the user.
   */
  fun setSortOrder(order: TodoSortOrder) {
    _uiState.value = _uiState.value.copy(sortOrder = order)
    val sorted = applySortOrder(_uiState.value.todos)
    _uiState.value = _uiState.value.copy(todos = sorted)
  }

  /**
   * Applies the current sorting rule to the given list of [TODO]s and return it.
   *
   * @param list The list of [TODO]s to sort.
   * @return A new list sorted according to the active sort order.
   */
  private fun applySortOrder(list: List<ToDo>): List<ToDo> {
    return when (_uiState.value.sortOrder) {
      TodoSortOrder.ALPHABETICAL -> list.sortedBy { it.name.lowercase() }
      TodoSortOrder.DATE_ASC -> list.sortedBy { it.dueDate?.toDate() }
      TodoSortOrder.DATE_DESC -> list.sortedByDescending { it.dueDate?.toDate() }
    }
  }
}
