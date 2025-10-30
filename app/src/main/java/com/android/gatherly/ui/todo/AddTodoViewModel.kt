package com.android.gatherly.ui.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.gatherly.model.map.Location
import com.android.gatherly.model.map.NominatimLocationRepository
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todo.ToDosRepository
import com.android.gatherly.model.todo.ToDosRepositoryProvider
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

/**
 * Represents the UI state of the Add ToDo screen.
 *
 * Holds user-entered data, validation errors, and progress flags used by [AddTodoViewModel] to
 * manage the process of creating a new ToDo.
 */
data class AddTodoUiState(
    val title: String = "",
    val description: String = "",
    val assignee: String = "",
    val location: String = "",
    val dueDate: String = "",
    val dueTime: String = "",
    val titleError: String? = null,
    val descriptionError: String? = null,
    val assigneeError: String? = null,
    val dueDateError: String? = null,
    val dueTimeError: String? = null,
    val locationError: String? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false,
    val isLocLoading: Boolean = false,
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
 * ViewModel responsible for managing the "Add ToDo" screen.
 *
 * Handles user input updates, field validation, and saving ToDo items to the Firestore repository
 * through [ToDosRepository].
 *
 * Currently, location handling is limited to plain string input until the Location repository is
 * implemented.
 *
 * @param todoRepository The repository responsible for persisting ToDo items.
 */
class AddTodoViewModel(
    private val todoRepository: ToDosRepository = ToDosRepositoryProvider.repository,
    private val nominatimClient: NominatimLocationRepository = NominatimLocationRepository(client)
) : ViewModel() {
  private val _uiState = MutableStateFlow(AddTodoUiState())

  /** Public immutable access to the Add ToDo UI state. */
  val uiState: StateFlow<AddTodoUiState> = _uiState.asStateFlow()

  // Chosen location
  private var chosenLocation: Location? = null

  /** Clears the error message in the UI state. */
  fun clearErrorMsg() {
    _uiState.value = _uiState.value.copy(saveError = null)
  }

  /** Clears the save success flag in the UI state. */
  fun clearSaveSuccess() {
    _uiState.value = _uiState.value.copy(saveSuccess = false)
  }

  /**
   * Updates the title field and validates that it is not blank.
   *
   * @param newValue The new title entered by the user. If blank, a validation error is set.
   */
  fun onTitleChanged(newValue: String) {
    _uiState.value =
        _uiState.value.copy(
            title = newValue,
            titleError = if (newValue.isBlank()) "Title cannot be empty" else null)
  }

  /**
   * Updates the description field and validates that it is not blank.
   *
   * @param newValue The new description entered by the user. If blank, a validation error is set.
   */
  fun onDescriptionChanged(newValue: String) {
    _uiState.value =
        _uiState.value.copy(
            description = newValue,
            descriptionError = if (newValue.isBlank()) "Description cannot be empty" else null)
  }

  /**
   * Updates the assignee field and validates that it is not blank.
   *
   * @param newValue The new assignee name entered by the user. If blank, a validation error is set.
   */
  fun onAssigneeChanged(newValue: String) {
    _uiState.value =
        _uiState.value.copy(
            assignee = newValue,
            assigneeError = if (newValue.isBlank()) "Assignee cannot be empty" else null)
  }

  /**
   * Updates the due date field and validates the format.
   *
   * @param newValue The new due date as a string (expected format: dd/MM/yyyy).
   */
  fun onDateChanged(newValue: String) {
    _uiState.value =
        _uiState.value.copy(
            dueDate = newValue,
            dueDateError = if (!isValidDate(newValue)) "Invalid format (dd/MM/yyyy)" else null)
  }

  /**
   * Updates the due time field and validates the format.
   *
   * @param newValue The new due time as a string (expected format: HH:mm).
   */
  fun onTimeChanged(newValue: String) {
    _uiState.value =
        _uiState.value.copy(
            dueTime = newValue,
            dueTimeError = if (!isValidTime(newValue)) "Invalid time (HH:mm)" else null)
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
    return try {
      val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
      sdf.isLenient = false
      sdf.parse(date)
      true
    } catch (e: Exception) {
      false
    }
  }

  /**
   * Checks whether a given time string is valid.
   *
   * @param time The time string to validate (expected format: HH:mm). Blank values are allowed.
   * @return `true` if the format and time are valid, `false` otherwise.
   */
  private fun isValidTime(time: String): Boolean {
    if (time.isBlank()) return true // optional
    val regex = Regex("""\d{2}:\d{2}""")
    if (!regex.matches(time)) return false
    return try {
      val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
      sdf.isLenient = false
      sdf.parse(time)
      true
    } catch (e: Exception) {
      false
    }
  }

  /**
   * Updates the event location
   *
   * @param updatedLocation the string with which to update
   */
  fun updateLocation(updatedLocation: String) {
    _uiState.value = _uiState.value.copy(location = updatedLocation)
  }

  /*----------------------------------Location--------------------------------------------------*/

  /**
   * Updates the location to the selected location
   *
   * @param location the selected location
   */
  fun selectLocation(location: Location) {
    _uiState.value = _uiState.value.copy(location = location.name, suggestions = emptyList())
    chosenLocation = location
  }

  /*----------------------------------Helpers---------------------------------------------------*/

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

  /**
   * Attempts to create and save a new [ToDo] entry to the repository.
   *
   * Performs field validation before saving, and updates the UI state to reflect loading, success,
   * and error states.
   *
   * @throws IllegalArgumentException If the provided date or time format is invalid.
   */
  fun saveTodo() {
    val validated =
        _uiState.value.copy(
            titleError = if (_uiState.value.title.isBlank()) "Title cannot be empty" else null,
            descriptionError =
                if (_uiState.value.description.isBlank()) "Description cannot be empty" else null,
            assigneeError =
                if (_uiState.value.assignee.isBlank()) "Assignee cannot be empty" else null,
            locationError =
                if (_uiState.value.location.isBlank()) "Location cannot be empty" else null,
            dueDateError =
                if (!isValidDate(_uiState.value.dueDate)) "Invalid format (dd/MM/yyyy)" else null,
            dueTimeError =
                if (!isValidTime(_uiState.value.dueTime)) "Invalid time (HH:mm)" else null)
    _uiState.value = validated

    // Abort if validation failed
    if (_uiState.value.titleError != null ||
        _uiState.value.descriptionError != null ||
        _uiState.value.assigneeError != null ||
        _uiState.value.dueDateError != null ||
        _uiState.value.dueTimeError != null) {
      return
    }

    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isSaving = true, saveError = null)
      try {
        val uid = todoRepository.getNewUid()
        val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date =
            sdfDate.parse(validated.dueDate) ?: throw IllegalArgumentException("Invalid date")

        val dueDateTimestamp = Timestamp(date)
        val dueTimeTimestamp =
            if (validated.dueTime.isNotBlank()) {
              val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
              Timestamp(sdfTime.parse(validated.dueTime)!!)
            } else null

        val todo =
            ToDo(
                uid = uid,
                name = validated.title,
                description = validated.description,
                assigneeName = validated.assignee,
                dueDate = dueDateTimestamp,
                dueTime = dueTimeTimestamp,
                location = chosenLocation,
                status = ToDoStatus.ONGOING,
                ownerId = "" // will be filled by Firestore repo
                )

        todoRepository.addTodo(todo)
        _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(isSaving = false, saveError = e.message)
      }
    }
  }
}
