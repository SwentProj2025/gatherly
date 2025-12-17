package com.android.gatherly.ui.todo

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.gatherly.model.map.Location
import com.android.gatherly.model.map.NominatimLocationRepository
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoPriority
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todo.ToDosRepository
import com.android.gatherly.model.todo.ToDosRepositoryProvider
import com.android.gatherly.model.todoCategory.ToDoCategory
import com.android.gatherly.model.todoCategory.ToDoCategoryRepository
import com.android.gatherly.model.todoCategory.ToDoCategoryRepositoryProvider
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

// Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the
// SwEnt staff.

private const val DATE_FORMAT = "dd/MM/yyyy"
private const val TIME_FORMAT = "HH:mm"

/**
 * UI state for the EditToDo screen. This state holds the data needed to edit an existing [ToDo]
 * item.
 */
data class EditTodoUIState(
    val title: String = "",
    val description: String = "",
    val dueDate: String = "",
    val dueTime: String = "",
    val location: String = "",
    val status: ToDoStatus = ToDoStatus.ONGOING,
    val errorMsg: String? = null,
    val titleError: String? = null,
    val dueDateError: String? = null,
    val dueTimeError: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val deleteSuccess: Boolean = false,
    val suggestions: List<Location> = emptyList(),
    val pastTime: Boolean = false,
    val priorityLevel: ToDoPriority = ToDoPriority.NONE,
    val tag: ToDoCategory? = null
) {
  val isValid: Boolean
    get() = titleError == null && dueDateError == null && dueTimeError == null && !isSaving
}

// create a HTTP Client for Nominatim
private var client: OkHttpClient =
    OkHttpClient.Builder()
        .addInterceptor { chain ->
          val request =
              chain
                  .request()
                  .newBuilder()
                  .header("User-Agent", "GatherlyApp (kerjangersende@gmail.com)")
                  .build()
          chain.proceed(request)
        }
        .build()

/**
 * ViewModel responsible for managing the [EditTodoScreen].
 *
 * Handles user input updates, field validation, and updating [ToDo] items on the Firestore
 * repository through [ToDosRepository].
 *
 * Currently, location handling is limited to plain string input until the Location repository is
 * implemented.
 *
 * @param todoRepository The repository responsible for persisting [ToDo] items.
 */
class EditTodoViewModel(
    private val todoRepository: ToDosRepository = ToDosRepositoryProvider.repository,
    private val nominatimClient: NominatimLocationRepository = NominatimLocationRepository(client),
    private val todoCategoryRepository: ToDoCategoryRepository =
        ToDoCategoryRepositoryProvider.repository
) : ViewModel() {
  private val _uiState = MutableStateFlow(EditTodoUIState())
  /** Public immutable access to the [EditTodoUIState]. */
  val uiState: StateFlow<EditTodoUIState> = _uiState.asStateFlow()

  private val _categories = MutableStateFlow<List<ToDoCategory>>(emptyList())
  val categories: StateFlow<List<ToDoCategory>> = _categories.asStateFlow()

  private var chosenLocation: Location? = null

  private lateinit var editTodoId: String

  /** Clears the error message in the UI state. */
  fun clearErrorMsg() {
    _uiState.value = _uiState.value.copy(errorMsg = null)
  }

  /** Clears the save success flag in the UI state. */
  fun clearSaveSuccess() {
    _uiState.value = _uiState.value.copy(saveSuccess = false)
  }

  /** Clears the delete success flag in the UI state. */
  fun clearDeleteSuccess() {
    _uiState.value = _uiState.value.copy(deleteSuccess = false)
  }

  /** Sets an error message in the UI state. */
  private fun setErrorMsg(errorMsg: String) {
    _uiState.value = _uiState.value.copy(errorMsg = errorMsg)
  }

  /** Clear the past time error */
  fun clearPastTime() {
    _uiState.value = _uiState.value.copy(pastTime = false)
  }

  init {
    viewModelScope.launch { loadAllCategories() }
  }

  /** Loads all [ToDo] categories from the repository and updates the UI state. */
  private suspend fun loadAllCategories() {
    try {
      todoCategoryRepository.initializeDefaultCategories()

      val list = todoCategoryRepository.getAllCategories()
      _categories.value = list
    } catch (e: Exception) {
      setErrorMsg("Failed to load categories: ${e.message}")
    }
  }

  /**
   * Loads a [ToDo] by its ID and updates the UI state.
   *
   * @param todoID The ID of the [ToDo] to be loaded.
   */
  fun loadTodo(todoID: String) {
    viewModelScope.launch {
      try {
        editTodoId = todoID
        val todo = todoRepository.getTodo(todoID)
        chosenLocation = todo.location
        _uiState.value =
            EditTodoUIState(
                title = todo.name,
                description = todo.description,
                dueDate =
                    todo.dueDate?.let {
                      val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
                      return@let dateFormat.format(todo.dueDate.toDate())
                    } ?: "",
                dueTime =
                    todo.dueTime?.let {
                      val dateFormat = SimpleDateFormat(TIME_FORMAT, Locale.getDefault())
                      return@let dateFormat.format(it.toDate())
                    } ?: "",
                location = todo.location?.name ?: "",
                priorityLevel = todo.priorityLevel,
                tag = todo.tag)
      } catch (e: Exception) {
        setErrorMsg("Failed to load ToDo: ${e.message}")
      }
    }
  }

  /** Checks if the due date and time are in the past before editing the [ToDo]. */
  fun checkPastTime() {
    val validated =
        _uiState.value.copy(
            titleError = if (_uiState.value.title.isBlank()) "Title cannot be empty" else null,
            dueDateError =
                if (!isValidDate(_uiState.value.dueDate)) "Invalid format ($DATE_FORMAT)" else null,
            dueTimeError =
                if (!isValidTime(_uiState.value.dueTime)) "Invalid time ($TIME_FORMAT)" else null)
    _uiState.value = validated

    // Abort if validation failed
    if (!uiState.value.isValid) {
      setErrorMsg("At least one field is not valid")
      return
    }
    lateinit var dateAndTime: Date
    if (validated.dueDate.isBlank()) {
      editTodo(editTodoId)
      return
    }
    if (validated.dueTime.isBlank()) {
      val sdfDate = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
      dateAndTime =
          sdfDate.parse(validated.dueDate) ?: throw IllegalArgumentException("Invalid date")
    } else {
      val sdfDateAndTime = SimpleDateFormat("$DATE_FORMAT $TIME_FORMAT", Locale.getDefault())
      dateAndTime =
          sdfDateAndTime.parse(validated.dueDate + " " + validated.dueTime)
              ?: throw IllegalArgumentException("Invalid date or time")
    }
    val dueDateAndTime = Timestamp(dateAndTime)

    val currentTimestamp = Timestamp.now()

    if (dueDateAndTime < currentTimestamp) {
      _uiState.value = _uiState.value.copy(pastTime = true)
    } else {
      editTodo(editTodoId)
    }
  }

  /**
   * Edits a [ToDo] document.
   *
   * @param id id of The [ToDo] document to be edited.
   */
  fun editTodo(id: String): Boolean {
    val state = uiState.value
    // Abort if validation failed
    if (!state.isValid) {
      return false
    }
    val date =
        if (state.dueDate.isNotBlank()) {
          val sdfTime = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
          Timestamp(sdfTime.parse(state.dueDate)!!)
        } else null

    val time =
        if (state.dueTime.isNotBlank()) {
          val sdfTime = SimpleDateFormat(TIME_FORMAT, Locale.getDefault())
          Timestamp(sdfTime.parse(state.dueTime)!!)
        } else null

    viewModelScope.launch {
      val ownerId = todoRepository.getTodo(id).ownerId

      editTodoToRepository(
          todoID = id,
          todo =
              ToDo(
                  name = state.title,
                  description = state.description.ifBlank { state.title },
                  dueDate = date,
                  dueTime = time,
                  location = chosenLocation,
                  status = state.status,
                  uid = id,
                  ownerId = ownerId,
                  priorityLevel = state.priorityLevel,
                  tag = state.tag))
    }
    clearErrorMsg()
    return true
  }

  /**
   * Edits a [ToDo] document in the repository.
   *
   * @param todoID The ID of the [ToDo] document to be edited.
   * @param todo The [ToDo] object containing the new values.
   */
  private fun editTodoToRepository(todoID: String, todo: ToDo) {

    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isSaving = true, errorMsg = null)
      try {
        todoRepository.editTodo(todoID = todoID, newValue = todo)
        _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
      } catch (e: Exception) {
        _uiState.value =
            _uiState.value.copy(isSaving = false, errorMsg = "Failed to edit ToDo: ${e.message}")
      }
    }
  }

  /**
   * Deletes a [ToDo] document by its ID.
   *
   * @param todoID The ID of the [ToDo] document to be deleted.
   */
  fun deleteToDo(todoID: String) {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isSaving = true, errorMsg = null)
      try {
        todoRepository.deleteTodo(todoID = todoID)
        _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
      } catch (e: Exception) {
        _uiState.value =
            _uiState.value.copy(isSaving = false, errorMsg = "Failed to delete ToDo: ${e.message}")
      }
    }
  }

  /**
   * Updates the title field and validates that it is not blank.
   *
   * @param newTitle The new title entered by the user. If blank, a validation error is set.
   */
  fun onTitleChanged(newTitle: String) {
    val errMsg = if (newTitle.isBlank()) "Title cannot be empty" else null
    _uiState.value = _uiState.value.copy(titleError = errMsg, title = newTitle)
  }

  /**
   * Updates the description field and validates that it is not blank.
   *
   * @param newDescription The new description entered by the user. If blank, a validation error is
   *   set.
   */
  fun onDescriptionChanged(newDescription: String) {
    _uiState.value = _uiState.value.copy(description = newDescription)
  }

  /**
   * Updates the location field.
   *
   * @param newLocation The name or description of the location.
   */
  fun onLocationChanged(newLocation: String) {
    _uiState.value = _uiState.value.copy(location = newLocation)
  }

  /**
   * Updates the due date field and validates the format.
   *
   * @param newDate The new due date as a string (expected format: dd/MM/yyyy).
   */
  fun onDateChanged(newDate: String) {
    val errMsg = if (!isValidDate(newDate)) "Invalid format ($DATE_FORMAT)" else null
    _uiState.value = _uiState.value.copy(dueDateError = errMsg, dueDate = newDate)
  }

  /**
   * Updates the due time field and validates the format.
   *
   * @param newTime The new due time as a string (expected format: HH:mm).
   */
  fun onTimeChanged(newTime: String) {
    val errMsg = if (!isValidTime(newTime)) "Invalid time ($TIME_FORMAT)" else null
    _uiState.value = _uiState.value.copy(dueTimeError = errMsg, dueTime = newTime)
  }

  /**
   * Checks whether a given date string is valid.
   *
   * @param date The date string to validate (expected format: dd/MM/yyyy).
   * @return `true` if the format and date are valid, `false` otherwise.
   */
  private fun isValidDate(date: String): Boolean {
    if (date.isBlank()) return true
    val regex = Regex("""\d{2}/\d{2}/\d{4}""")
    if (!regex.matches(date)) return false
    try {
      val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
      sdf.isLenient = false
      sdf.parse(date)
      return true
    } catch (_: Exception) {
      return false
    }
  }

  /**
   * Checks whether a given time string is valid.
   *
   * @param time The time string to validate (expected format: HH:mm). Blank values are allowed.
   * @return `true` if the format and time are valid, `false` otherwise.
   */
  private fun isValidTime(time: String): Boolean {
    if (time.isBlank()) return true
    val regex = Regex("""\d{2}:\d{2}""")
    if (!regex.matches(time)) return false
    try {
      val sdf = SimpleDateFormat(TIME_FORMAT, Locale.getDefault())
      sdf.isLenient = false
      sdf.parse(time)
      return true
    } catch (_: Exception) {
      return false
    }
  }

  /*----------------------------------Location--------------------------------------------------*/

  fun selectLocation(location: Location) {
    _uiState.value = _uiState.value.copy(location = location.name, suggestions = emptyList())
    chosenLocation = location
  }

  /*----------------------------------Priority Level--------------------------------------------*/

  /**
   * Updates the priority level of the item
   *
   * @param priorityLevel the selected priority level
   */
  fun selectPriorityLevel(priorityLevel: ToDoPriority) {
    _uiState.value = _uiState.value.copy(priorityLevel = priorityLevel)
  }

  /*------------------------------------Category------------------------------------------------*/

  /**
   * Updates the tag of the item
   *
   * @param category the selected category
   */
  fun selectTodoTag(category: ToDoCategory?) {
    _uiState.value = _uiState.value.copy(tag = category)
  }

  fun addCategory(name: String, color: Color) {

    viewModelScope.launch {
      try {
        val newCategory = ToDoCategory(name = name, color = color, isDefault = false)
        todoCategoryRepository.addToDoCategory(newCategory)
        _categories.value = todoCategoryRepository.getAllCategories()
      } catch (_: Exception) {
        setErrorMsg("Failed to create a new category of todo: $name")
      }
    }
  }

  fun deleteCategory(category: ToDoCategory) {
    viewModelScope.launch {
      try {
        val ownerId = category.ownerId

        todoRepository.updateTodosTagToNull(category.id, ownerId)
        todoCategoryRepository.deleteToDoCategory(category.id)
        _categories.value = todoCategoryRepository.getAllCategories()
        if (_uiState.value.tag == category) {
          _uiState.value = _uiState.value.copy(tag = null)
        }
      } catch (_: Exception) {
        setErrorMsg("Failed to delete the category of todo: ${category.name}")
      }
    }
  }

  /*----------------------------------Helpers--------------------------------------------------*/

  /**
   * Given a string, search locations with Nominatim
   *
   * @param location the substring with which to search
   */
  fun searchLocationByString(location: String) {
    viewModelScope.launch {
      val list = nominatimClient.search(location)
      _uiState.value = _uiState.value.copy(suggestions = list)
    }
  }
}
