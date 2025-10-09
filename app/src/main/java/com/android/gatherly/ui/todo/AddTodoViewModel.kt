package com.android.gatherly.ui.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false,

    // val isLocLoading: Boolean = false,
    // val suggestions: List<Location> = emptyList()
)

class AddTodoViewModel(
    private val todoRepository: ToDosRepository = ToDosRepositoryProvider.repository,
    // private val locationRepository: LocationRepository =
    // NominatimLocationRepository(HttpClientProvider.client)
) : ViewModel() {
  private val _uiState = MutableStateFlow(AddTodoUiState())
  val uiState: StateFlow<AddTodoUiState> = _uiState.asStateFlow()

  // private var selectedLocation: Location? = null
  // private var searchJob: Job? = null

  fun onTitleChanged(newValue: String) {
    _uiState.value =
        _uiState.value.copy(
            title = newValue,
            titleError = if (newValue.isBlank()) "Title cannot be empty" else null)
  }

  fun onDescriptionChanged(newValue: String) {
    _uiState.value =
        _uiState.value.copy(
            description = newValue,
            descriptionError = if (newValue.isBlank()) "Description cannot be empty" else null)
  }

  fun onAssigneeChanged(newValue: String) {
    _uiState.value =
        _uiState.value.copy(
            assignee = newValue,
            assigneeError = if (newValue.isBlank()) "Assignee cannot be empty" else null)
  }

  fun onDateChanged(newValue: String) {
    _uiState.value =
        _uiState.value.copy(
            dueDate = newValue,
            dueDateError = if (!isValidDate(newValue)) "Invalid format (dd/MM/yyyy)" else null)
  }

  fun onTimeChanged(newValue: String) {
    _uiState.value =
        _uiState.value.copy(
            dueTime = newValue,
            dueTimeError = if (!isValidTime(newValue)) "Invalid time (HH:mm)" else null)
  }

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

  /*
  fun onLocationChanged(newValue: String) {
      selectedLocation = null
      _uiState.value = _uiState.value.copy(location = newValue)

      searchJob?.cancel()
      if (newValue.isBlank()) {
          _uiState.value = _uiState.value.copy(suggestions = emptyList(), isLocLoading = false)
          return
      }

      searchJob = viewModelScope.launch {
          _uiState.value = _uiState.value.copy(isLocLoading = true)
          delay(300)
          try {
              val results = locationRepository.search(newValue).take(5)
              _uiState.value = _uiState.value.copy(suggestions = results, isLocLoading = false)
          } catch (e: Exception) {
              _uiState.value = _uiState.value.copy(suggestions = emptyList(), isLocLoading = false)
          }
      }
  }

  fun onSelectLocation(loc: Location) {
      selectedLocation = loc
      _uiState.value = _uiState.value.copy(
          location = loc.name,
          suggestions = emptyList()
      )
  }
   */

  fun saveTodo() {
    val validated =
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

        // val loc = selectedLocation?.let { l ->
        //  Location(latitude = l.latitude, longitude = l.longitude, name = l.name)
        // }

        val todo =
            ToDo(
                uid = uid,
                name = validated.title,
                description = validated.description,
                assigneeName = validated.assignee,
                dueDate = dueDateTimestamp,
                dueTime = dueTimeTimestamp,
                location = null, // selectedLocation,
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
