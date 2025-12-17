package com.android.gatherly.ui.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.gatherly.model.points.PointsRepository
import com.android.gatherly.model.points.PointsRepositoryProvider
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.profile.ProfileRepositoryProvider
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todo.ToDosRepository
import com.android.gatherly.model.todo.ToDosRepositoryProvider
import com.android.gatherly.model.todoCategory.ToDoCategory
import com.android.gatherly.model.todoCategory.ToDoCategoryRepository
import com.android.gatherly.model.todoCategory.ToDoCategoryRepositoryProvider
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
 * @property todos A list of [ToDo] items to be displayed in the Overview screen. Defaults to an
 *   empty list if no items are available.
 */
data class OverviewUIState(
    val todos: List<ToDo> = emptyList(),
    val sortOrder: TodoSortOrder = TodoSortOrder.ALPHABETICAL,
    val errorMsg: String? = null,
    val isLoading: Boolean = true,
    val selectedCategory: ToDoCategory? = null
)

/**
 * Specifies the available sorting criteria for the [ToDo] overview list.
 * - [DATE_ASC]: Sort tasks by increasing due date (earliest first).
 * - [DATE_DESC]: Sort tasks by decreasing due date (latest first).
 * - [ALPHABETICAL]: Sort tasks by their name in alphabetical order (A -> Z).
 * - [PRIORITY_LEVEL]: Sort tasks by their priority level (highest priority first).
 */
enum class TodoSortOrder {
  DATE_ASC,
  DATE_DESC,
  ALPHABETICAL,
  PRIORITY_LEVEL
}

/**
 * ViewModel for the Overview screen.
 *
 * Responsible for managing the UI state, by fetching and providing [ToDo] items via the
 * [ToDosRepository].
 *
 * @param todoRepository The repository used to fetch and manage [ToDo] items.
 * @param profileRepository The repository used to fetch and manage user profiles.
 * @param pointsRepository The repository used to manage user points.
 * @param todoCategoryRepository The repository used to fetch and manage [ToDoCategory] items
 */
class OverviewViewModel(
    private val todoRepository: ToDosRepository = ToDosRepositoryProvider.repository,
    private val profileRepository: ProfileRepository = ProfileRepositoryProvider.repository,
    private val pointsRepository: PointsRepository = PointsRepositoryProvider.repository,
    private val todoCategoryRepository: ToDoCategoryRepository =
        ToDoCategoryRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(OverviewUIState())
  val uiState: StateFlow<OverviewUIState> = _uiState.asStateFlow()

  private val _categories = MutableStateFlow<List<ToDoCategory>>(emptyList())
  val categories: StateFlow<List<ToDoCategory>> = _categories.asStateFlow()

  private var allTodosCache: List<ToDo> = emptyList()

  private var currentSearchQuery: String = ""

  init {
    getAllTodos()
    viewModelScope.launch { loadAllCategories() }
  }

  /** Refreshes the UI state by fetching all [ToDo] items from the repository. */
  fun refreshUIState() {
    _uiState.value = _uiState.value.copy(selectedCategory = null)
    getAllTodos()
    viewModelScope.launch { loadAllCategories() }
  }

  /** Fetches all [ToDo]s from the repository and updates the UI state. */
  private fun getAllTodos() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, errorMsg = null)
      try {
        val todos = todoRepository.getAllTodos()
        allTodosCache = todos
        _uiState.value = _uiState.value.copy(isLoading = false)
        refreshVisibleTodos()
      } catch (e: Exception) {
        _uiState.value =
            _uiState.value.copy(todos = emptyList(), errorMsg = e.message, isLoading = false)
      }
    }
  }

  /** Loads all [ToDoCategory] items from the repository and updates the UI state. */
  private suspend fun loadAllCategories() {
    try {
      todoCategoryRepository.initializeDefaultCategories()

      val list = todoCategoryRepository.getAllCategories()
      _categories.value = list
    } catch (e: Exception) {
      _uiState.value = _uiState.value.copy(errorMsg = e.message)
    }
  }

  /** Invoked when the user clicks on the checkbox, to mark the [ToDo] as completed or ongoing. */
  fun onCheckboxChanged(uid: String, newStatus: ToDoStatus) {
    viewModelScope.launch {
      val ownerId = todoRepository.getTodo(uid).ownerId
      editTodo(todoRepository, profileRepository, pointsRepository, uid, newStatus, ownerId)
      refreshUIState()
    }
  }

  /**
   * Invoked when users type in the search bar to filter [ToDo]s according to the typed query.
   *
   * @param query The search query entered by the user.
   */
  fun searchTodos(query: String) {
    currentSearchQuery = query.trim()
    refreshVisibleTodos()
  }

  /**
   * Updates the current category filter of the UI and applies it immediately to the currently
   * displayed list of [ToDo]s.
   *
   * @param category The new [ToDoCategory] selected by the user, or null to clear the filter.
   */
  fun setCategoryFilter(category: ToDoCategory?) {
    _uiState.value = _uiState.value.copy(selectedCategory = category)
    refreshVisibleTodos()
  }

  /**
   * Updates the current sorting order of the UI and applies it immediately to the currently
   * displayed list of [ToDo]s.
   *
   * @param order The new [TodoSortOrder] selected by the user.
   */
  fun setSortOrder(order: TodoSortOrder) {
    _uiState.value = _uiState.value.copy(sortOrder = order)
    refreshVisibleTodos()
  }

  /**
   * Applies the current sorting rule to the given list of [ToDo]s and return it.
   *
   * @param list The list of [ToDo]s to sort.
   * @return A new list sorted according to the active sort order.
   */
  private fun applySortOrder(list: List<ToDo>): List<ToDo> {
    return when (_uiState.value.sortOrder) {
      TodoSortOrder.ALPHABETICAL -> list.sortedBy { it.name.lowercase() }
      TodoSortOrder.DATE_ASC -> list.sortedBy { it.dueDate?.toDate() }
      TodoSortOrder.DATE_DESC -> list.sortedByDescending { it.dueDate?.toDate() }
      TodoSortOrder.PRIORITY_LEVEL -> list.sortedByDescending { it.priorityLevel.ordinal }
    }
  }

  /** Refreshes the list of visible [ToDo]s based on the current filters and search query. */
  private fun refreshVisibleTodos() {
    val category = _uiState.value.selectedCategory
    val query = currentSearchQuery.trim().lowercase()

    var result = allTodosCache

    if (category != null) {
      result = result.filter { it.tag?.id == category.id }
    }

    if (query.isNotEmpty()) {
      result =
          result.filter {
            it.name.lowercase().contains(query) || it.description.lowercase().contains(query)
          }
    }

    result = applySortOrder(result)

    _uiState.value = _uiState.value.copy(todos = result)
  }
}
