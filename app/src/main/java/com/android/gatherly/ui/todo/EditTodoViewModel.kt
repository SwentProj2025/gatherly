package com.android.gatherly.ui.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.gatherly.model.todo.DateParser
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
    val errorMsg: String? = null,
    val invalidTitleMsg: String? = null,
    val invalidDescriptionMsg: String? = null,
    val invalidAssigneeNameMsg: String? = null,
    val invalidDueDateMsg: String? = null,
) {
  val isValid: Boolean
    get() =
        invalidTitleMsg == null &&
            invalidDescriptionMsg == null &&
            invalidAssigneeNameMsg == null &&
            invalidDueDateMsg == null &&
            title.isNotEmpty() &&
            description.isNotEmpty() &&
            assignee.isNotEmpty() &&
            dueDate.isNotEmpty()
}

class EditTodoViewModel(
    private val todoRepository: ToDosRepository = ToDosRepositoryProvider.repository,
) : ViewModel() {
  // EditToDo UI state
  private val _uiState = MutableStateFlow(EditTodoUIState())
  val uiState: StateFlow<EditTodoUIState> = _uiState.asStateFlow()

  /** Clears the error message in the UI state. */
  fun clearErrorMsg() {
    _uiState.value = _uiState.value.copy(errorMsg = null)
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
  fun editTodo(id: String) {
    val validated =
        _uiState.value.copy(
            errorMsg = if (_uiState.value.title.isBlank()) "Title cannot be empty" else null)
    _uiState.value = validated

    // Abort if validation failed
    if (_uiState.value.title.isBlank() ||
        _uiState.value.description.isBlank() ||
        _uiState.value.assignee.isBlank() ||
        !isValidDate(_uiState.value.dueDate)) {
      return
    }

    viewModelScope.launch {
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
                location = null,
                status = ToDoStatus.ONGOING,
                ownerId = "" // will be filled by Firestore repo
                )

        todoRepository.editTodo(todo.uid, todo)
      } catch (e: Exception) {
        setErrorMsg("Failed to edit ToDo: ${e.message}")
      }
    }
  }

  /**
   * Edits a ToDo document in the repository.
   *
   * @param id The ID of the ToDo document to be edited.
   * @param todo The ToDo object containing the new values.
   */
  private fun editTodoToRepository(id: String, todo: ToDo) {
    viewModelScope.launch {
      try {
        todoRepository.editTodo(todoID = id, newValue = todo)
      } catch (e: Exception) {
        setErrorMsg("Failed to edit ToDo: ${e.message}")
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
      try {
        todoRepository.deleteTodo(todoID = todoID)
      } catch (e: Exception) {
        setErrorMsg("Failed to delete ToDo: ${e.message}")
      }
    }
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

  // Functions to update the UI state.

  fun onTitleChanged(title: String) {
    _uiState.value =
        _uiState.value.copy(
            title = title, invalidTitleMsg = if (title.isBlank()) "Title cannot be empty" else null)
  }

  fun onDescriptionChanged(description: String) {
    _uiState.value =
        _uiState.value.copy(
            description = description,
            invalidDescriptionMsg =
                if (description.isBlank()) "Description cannot be empty" else null)
  }

  fun onAssigneeChanged(assigneeName: String) {
    _uiState.value =
        _uiState.value.copy(
            assignee = assigneeName,
            invalidAssigneeNameMsg =
                if (assigneeName.isBlank()) "Assignee cannot be empty" else null)
  }

  fun onDateChanged(dueDate: String) {
    _uiState.value =
        _uiState.value.copy(
            dueDate = dueDate,
            invalidDueDateMsg =
                if (DateParser.parse(dueDate) == null) "Date is not valid (format: dd/mm/yyyy)"
                else null)
  }

  fun onLocationChanged(location: String) {
    _uiState.value = _uiState.value.copy(location = location)
  }
}
