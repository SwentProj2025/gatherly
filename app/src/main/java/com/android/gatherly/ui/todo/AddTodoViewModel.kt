package com.android.gatherly.ui.todo

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.gatherly.model.map.Location
import com.android.gatherly.model.map.NominatimLocationRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.profile.ProfileRepositoryProvider
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todo.ToDosRepository
import com.android.gatherly.model.todo.ToDosRepositoryProvider
import com.android.gatherly.utils.addTodo
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
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
    val location: String = "",
    val dueDate: String = "",
    val dueTime: String = "",
    val titleError: String? = null,
    val dueDateError: String? = null,
    val dueTimeError: String? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false,
    val isLocLoading: Boolean = false,
    val suggestions: List<Location> = emptyList(),
    val pastTime: Boolean = false
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
@SuppressLint("SimpleDateFormat")
class AddTodoViewModel(
    private val todoRepository: ToDosRepository = ToDosRepositoryProvider.repository,
    private val profileRepository: ProfileRepository = ProfileRepositoryProvider.repository,
    private val authProvider: () -> FirebaseAuth = { Firebase.auth },
    private val nominatimClient: NominatimLocationRepository = NominatimLocationRepository(client)
) : ViewModel() {
  private val _uiState = MutableStateFlow(AddTodoUiState())

  /** Public immutable access to the Add ToDo UI state. */
  val uiState: StateFlow<AddTodoUiState> = _uiState.asStateFlow()

  // Chosen location
  private var chosenLocation: Location? = null
  private val dateSDF = SimpleDateFormat("dd/MM/yyyy")

  /** Clears the error message in the UI state. */
  fun clearErrorMsg() {
    _uiState.value = _uiState.value.copy(saveError = null)
  }

  /** Clears the save success flag in the UI state. */
  fun clearSaveSuccess() {
    _uiState.value = _uiState.value.copy(saveSuccess = false)
  }

  /** Clear the past time error */
  fun clearPastTime() {
    _uiState.value = _uiState.value.copy(pastTime = false)
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
    _uiState.value = _uiState.value.copy(description = newValue)
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
    if (date.isBlank()) return true // optional
    val regex = Regex("""\d{2}/\d{2}/\d{4}""")
    if (!regex.matches(date)) return false
    return try {
      dateSDF.isLenient = false
      dateSDF.parse(date)
      true
    } catch (_: Exception) {
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
    } catch (_: Exception) {
      false
    }
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

  /** Checks that the todo time is valid before saving */
  fun checkTodoTime() {
    val validated =
        _uiState.value.copy(
            titleError = if (_uiState.value.title.isBlank()) "Title cannot be empty" else null,
            dueDateError =
                if (!isValidDate(_uiState.value.dueDate)) "Invalid format (dd/MM/yyyy)" else null,
            dueTimeError =
                if (!isValidTime(_uiState.value.dueTime)) "Invalid time (HH:mm)" else null)
    _uiState.value = validated

    // Abort if validation failed
    if (!uiState.value.isValid) {
      return
    }

    lateinit var dateAndTime: Date
    if (validated.dueDate.isBlank()) {
      saveTodo()
      return
    }
    if (validated.dueTime.isBlank()) {
      dateAndTime =
          dateSDF.parse(validated.dueDate) ?: throw IllegalArgumentException("Invalid date")
    } else {
      val sdfDateAndTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
      dateAndTime =
          sdfDateAndTime.parse(validated.dueDate + " " + validated.dueTime)
              ?: throw IllegalArgumentException("Invalid date or time")
    }
    val dueDateAndTime = Timestamp(dateAndTime)
    val currentTimestamp = Timestamp.now()

    if (dueDateAndTime < currentTimestamp) {
      _uiState.value = _uiState.value.copy(pastTime = true)
    } else {
      saveTodo()
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
    val validated = uiState.value
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isSaving = true, saveError = null)
      try {
        val ownerId =
            authProvider().currentUser?.uid
                ?: throw IllegalStateException("User not authenticated.")

        val uid = todoRepository.getNewUid()

        val dueDateTimestamp =
            if (validated.dueDate.isNotBlank()) {
              val sdfTime = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
              Timestamp(sdfTime.parse(validated.dueDate)!!)
            } else null

        val dueTimeTimestamp =
            if (validated.dueTime.isNotBlank()) {
              val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
              Timestamp(sdfTime.parse(validated.dueTime)!!)
            } else null

        val todo =
            ToDo(
                uid = uid,
                name = validated.title,
                description = validated.description.ifBlank { validated.title },
                dueDate = dueDateTimestamp,
                dueTime = dueTimeTimestamp,
                location = chosenLocation,
                status = ToDoStatus.ONGOING,
                ownerId = ownerId)

        addTodo(todoRepository, profileRepository, todo, ownerId)
        _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(isSaving = false, saveError = e.message)
      }
    }
  }
}
