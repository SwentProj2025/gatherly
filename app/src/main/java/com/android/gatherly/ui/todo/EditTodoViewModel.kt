package com.android.gatherly.ui.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.gatherly.model.map.Location
import com.android.gatherly.model.map.NominatimLocationRepository
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todo.ToDosRepository
import com.android.gatherly.model.todo.ToDosRepositoryProvider
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

// Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the
// SwEnt staff.

/**
 * UI state for the EditToDo screen. This state holds the data needed to edit an existing ToDo item.
 */
data class EditTodoUIState(
    val title: String = "",
    val description: String = "",
    val assignee: String = "",
    val dueDate: String = "",
    val dueTime: String = "",
    val location: String = "",
    val status: ToDoStatus = ToDoStatus.ONGOING,
    val errorMsg: String? = null,
    val titleError: String? = null,
    val descriptionError: String? = null,
    val assigneeError: String? = null,
    val dueDateError: String? = null,
    val dueTimeError: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val deleteSuccess: Boolean = false,
    val suggestions: List<Location> = emptyList()
)

// create a HTTP Client for Nominatim
private var client: OkHttpClient =
    OkHttpClient.Builder()
        .addInterceptor { chain ->
          val request =
              chain
                  .request()
                  .newBuilder()
                  .header("User-Agent", "BootcampApp (croissant.kerjan@gmail.com)")
                  .build()
          chain.proceed(request)
        }
        .build()

/**
 * ViewModel responsible for managing the "Edit ToDo" screen.
 *
 * Handles user input updates, field validation, and updating ToDo items on the Firestore repository
 * through [ToDosRepository].
 *
 * Currently, location handling is limited to plain string input until the Location repository is
 * implemented.
 *
 * @param todoRepository The repository responsible for persisting ToDo items.
 */
class EditTodoViewModel(
    private val todoRepository: ToDosRepository = ToDosRepositoryProvider.repository,
    private val nominatimClient: NominatimLocationRepository = NominatimLocationRepository(client),
    private val authProvider: () -> FirebaseAuth = { Firebase.auth }
) : ViewModel() {
  private val _uiState = MutableStateFlow(EditTodoUIState())
  /** Public immutable access to the Edit ToDo UI state. */
  val uiState: StateFlow<EditTodoUIState> = _uiState.asStateFlow()

  // Selected Location
  private var chosenLocation: Location? = null

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

  /**
   * Loads a ToDo by its ID and updates the UI state.
   *
   * @param todoID The ID of the ToDo to be loaded.
   */
  fun loadTodo(todoID: String) {
    viewModelScope.launch {
      try {
        val todo = todoRepository.getTodo(todoID)
        chosenLocation = todo.location
        _uiState.value =
            EditTodoUIState(
                title = todo.name,
                description = todo.description,
                assignee = todo.assigneeName,
                dueDate =
                    todo.dueDate.let {
                      val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                      return@let dateFormat.format(todo.dueDate.toDate())
                    },
                dueTime =
                    todo.dueTime?.let {
                      val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                      return@let dateFormat.format(it.toDate())
                    } ?: "",
                location = todo.location?.name ?: "",
            )
      } catch (e: Exception) {
        setErrorMsg("Failed to load ToDo: ${e.message}")
      }
    }
  }

  /**
   * Edits a ToDo document.
   *
   * @param id id of The ToDo document to be edited.
   */
  fun editTodo(id: String): Boolean {
    _uiState.value =
        _uiState.value.copy(
            titleError = if (_uiState.value.title.isBlank()) "Title cannot be empty" else null,
            descriptionError =
                if (_uiState.value.description.isBlank()) "Description cannot be empty" else null,
            assigneeError =
                if (_uiState.value.assignee.isBlank()) "Assignee cannot be empty" else null,
            dueDateError =
                if (!isValidDate(_uiState.value.dueDate)) "Invalid format (dd/MM/yyyy)" else null,
            dueTimeError =
                if (!isValidTime(_uiState.value.dueTime)) "Invalid time (HH:mm)" else null)
    val state = _uiState.value

    // Abort if validation failed
    if (state.titleError != null ||
        state.descriptionError != null ||
        state.assigneeError != null ||
        state.dueDateError != null ||
        state.dueTimeError != null) {
      setErrorMsg("At least one field is not valid")
      return false
    }
    val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val date = sdfDate.parse(state.dueDate) ?: throw IllegalArgumentException("Invalid date")

    val time =
        if (state.dueTime.isNotBlank()) {
          val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
          Timestamp(sdfTime.parse(state.dueTime)!!)
        } else null

    val ownerId = authProvider().currentUser?.uid ?: ""

    editTodoToRepository(
        todoID = id,
        todo =
            ToDo(
                name = state.title,
                description = state.description,
                assigneeName = state.assignee,
                dueDate = Timestamp(date),
                dueTime = time,
                location = chosenLocation,
                status = state.status,
                uid = id,
                ownerId = ownerId))
    clearErrorMsg()
    return true
  }

  /**
   * Edits a ToDo document in the repository.
   *
   * @param todoID The ID of the ToDo document to be edited.
   * @param todo The ToDo object containing the new values.
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
   * Deletes a ToDo document by its ID.
   *
   * @param todoID The ID of the ToDo document to be deleted.
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
    val errMsg = if (newDescription.isBlank()) "Description cannot be empty" else null
    _uiState.value = _uiState.value.copy(descriptionError = errMsg, description = newDescription)
  }

  /**
   * Updates the assignee field and validates that it is not blank.
   *
   * @param newAssignee The new assignee name entered by the user. If blank, a validation error is
   *   set.
   */
  fun onAssigneeChanged(newAssignee: String) {
    val errMsg = if (newAssignee.isBlank()) "Assignee cannot be empty" else null
    _uiState.value = _uiState.value.copy(assigneeError = errMsg, assignee = newAssignee)
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
    val errMsg = if (!isValidDate(newDate)) "Invalid format (dd/MM/yyyy)" else null
    _uiState.value = _uiState.value.copy(dueDateError = errMsg, dueDate = newDate)
  }

  /**
   * Updates the due time field and validates the format.
   *
   * @param newTime The new due time as a string (expected format: HH:mm).
   */
  fun onTimeChanged(newTime: String) {
    val errMsg = if (!isValidTime(newTime)) "Invalid time (HH:mm)" else null
    _uiState.value = _uiState.value.copy(dueTimeError = errMsg, dueTime = newTime)
  }

  /**
   * Checks whether a given date string is valid.
   *
   * @param date The date string to validate (expected format: dd/MM/yyyy).
   * @return `true` if the format and date are valid, `false` otherwise.
   */
  private fun isValidDate(date: String): Boolean {
    val regex = Regex("""\d{2}/\d{2}/\d{4}""")
    if (!regex.matches(date)) return false
    try {
      val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
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
    val regex = Regex("""\d{2}:\d{2}""")
    if (!regex.matches(time)) return false
    try {
      val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
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
