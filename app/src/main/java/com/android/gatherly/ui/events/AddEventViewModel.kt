package com.android.gatherly.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.gatherly.model.event.EventsRepository
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.ui.todo.AddTodoUiState
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale


data class AddEventUiState(
    val title: String = "",
    val description: String = "",
    val creatorName: String = "",
    val location: String = "",
    val dueDate: String = "",
    val startTime: String = "",
    val endTime: String = "",

    val titleError: String? = null,
    val descriptionError: String? = null,
    val creatorNameError: String? = null,
    val locationError: String? = null,
    val dueDateError: String? = null,
    val startTimeError: String? = null,
    val endTimeError: String? = null,

    val isCreated: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false,
)

/**
 * ViewModel responsible for managing the "Add Event" screen.
 *
 * Handles user input updates, field validation, and saving Event items to the Firestore repository
 * through [EventRepository].
 *
 * @param eventRepository The repository responsible for persisting Event items.
 */
class AddEventViewModel(
    //private val eventRepository: EventsRepository = EventsRepositoryProvider.repository,
) : ViewModel() {

}
    /*

    private val _uiState = MutableStateFlow(AddEventUiState())

    /** Public immutable access to the Add Event UI state. */
    val uiState: StateFlow<AddEventUiState> = _uiState.asStateFlow()

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
     * Temporarily updates the location field.
     *
     * Currently stores it as a raw string until the Location class and repository are implemented.
     *
     * @param newValue The name or description of the location.
     */
    fun onLocationChanged(newValue: String) {
        _uiState.value =
            _uiState.value.copy(
                location = newValue,
                locationError = if (newValue.isBlank()) "Location cannot be empty" else null)
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
     * Attempts to create and save a new [Event] entry to the repository.
     *
     * Performs field validation before saving, and updates the UI state to reflect loading, success,
     * and error states.
     *
     * @throws IllegalArgumentException If the provided date or time format is invalid.
     */
    fun saveEvent() {
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
                startTimeError =
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



                eventRepository.addEvent(event)
                _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, saveError = e.message)
            }
        }
    }

}

 */