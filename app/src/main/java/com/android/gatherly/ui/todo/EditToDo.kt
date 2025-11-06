package com.android.gatherly.ui.todo

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.R
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu_Goback
import kotlinx.coroutines.delay

// Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the
// SwEnt staff.

/** Contains test tags used for UI testing on the Edit To-Do screen. */
object EditToDoScreenTestTags {
  /** Tag for the To-Do title input field. */
  const val INPUT_TODO_TITLE = "inputTodoTitle"

  /** Tag for the To-Do description input field. */
  const val INPUT_TODO_DESCRIPTION = "inputTodoDescription"

  /** Tag for the To-Do assignee input field. */
  const val INPUT_TODO_ASSIGNEE = "inputTodoAssignee"

  /** Tag for the To-Do location input field. */
  const val INPUT_TODO_LOCATION = "inputTodoLocation"

  /** Tag for the To-Do due date input field. */
  const val INPUT_TODO_DATE = "inputTodoDate"

  /** Tag for the To-Do due time input field. */
  const val INPUT_TODO_TIME = "inputTodoTime"

  /** Tag for the Save button that submits the To-Do. */
  const val TODO_SAVE = "todoSave"

  /** Tag for the Delete button that deletes the To-Do. */
  const val TODO_DELETE = "todoDelete"

  /** Tag for displaying error messages under text fields. */
  const val ERROR_MESSAGE = "errorMessage"

  /** Tag for displaying error messages under text fields. */
  const val LOCATION_MENU = "locationMenu"
}

private const val DELAY = 1000L

/**
 * Displays the screen for editing an existing [ToDo].
 *
 * @param editTodoViewModel The [EditTodoViewModel] that provides the current ToDo state.
 * @param onSave Callback invoked after a To-Do has been successfully edited (saved).
 * @param onDelete Callback invoked after a To-Do has been successfully deleted.
 * @param goBack Callback triggered when the back arrow in the top app bar is pressed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditToDoScreen(
    todoUid: String,
    editTodoViewModel: EditTodoViewModel = viewModel(),
    onSave: () -> Unit = {},
    onDelete: () -> Unit = {},
    goBack: () -> Unit = {},
) {
  LaunchedEffect(todoUid) { editTodoViewModel.loadTodo(todoUid) }

  val todoUIState by editTodoViewModel.uiState.collectAsState()
  val errorMsg = todoUIState.errorMsg
  val context = LocalContext.current

  val screenPadding = dimensionResource(id = R.dimen.padding_screen)
  val fieldSpacing = dimensionResource(id = R.dimen.spacing_between_fields)
  val buttonSpacing = dimensionResource(id = R.dimen.spacing_between_buttons)

  // Local state for the dropdown visibility
  var showLocationDropdown by remember { mutableStateOf(false) }

  val textFieldColors =
      TextFieldDefaults.colors(
          focusedContainerColor = MaterialTheme.colorScheme.background,
          unfocusedContainerColor = MaterialTheme.colorScheme.background,
          unfocusedTextColor = MaterialTheme.colorScheme.primary,
          focusedTextColor = MaterialTheme.colorScheme.primary,
      )

  // Search location when input changes
  LaunchedEffect(todoUIState.location) {
    if (todoUIState.location.isNotBlank()) {
      delay(DELAY)
      editTodoViewModel.searchLocationByString(todoUIState.location)
    }
  }

  LaunchedEffect(errorMsg) {
    if (errorMsg != null) {
      Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
      editTodoViewModel.clearErrorMsg()
    }
  }

  Scaffold(
      topBar = {
        TopNavigationMenu_Goback(
            selectedTab = Tab.EditTodo,
            modifier = Modifier.testTag(NavigationTestTags.TOP_NAVIGATION_MENU),
            goBack = goBack)
      },
      content = { paddingVal ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingVal).padding(screenPadding),
            verticalArrangement = Arrangement.spacedBy(fieldSpacing)) {
              // Title Input
              OutlinedTextField(
                  value = todoUIState.title,
                  onValueChange = { editTodoViewModel.onTitleChanged(it) },
                  label = { Text(stringResource(R.string.todos_title_field_label)) },
                  placeholder = { Text(stringResource(R.string.todos_title_field_placeholder)) },
                  isError = todoUIState.titleError != null,
                  supportingText = {
                    todoUIState.titleError?.let {
                      Text(it, modifier = Modifier.testTag(EditToDoScreenTestTags.ERROR_MESSAGE))
                    }
                  },
                  colors = textFieldColors,
                  modifier =
                      Modifier.fillMaxWidth().testTag(EditToDoScreenTestTags.INPUT_TODO_TITLE))

              // Description Input
              OutlinedTextField(
                  value = todoUIState.description,
                  onValueChange = { editTodoViewModel.onDescriptionChanged(it) },
                  label = { Text(stringResource(R.string.todos_description_field_label)) },
                  placeholder = { Text(stringResource(R.string.todos_description_placeholder)) },
                  isError = todoUIState.descriptionError != null,
                  supportingText = {
                    todoUIState.descriptionError?.let {
                      Text(it, modifier = Modifier.testTag(EditToDoScreenTestTags.ERROR_MESSAGE))
                    }
                  },
                  colors = textFieldColors,
                  modifier =
                      Modifier.fillMaxWidth()
                          .testTag(EditToDoScreenTestTags.INPUT_TODO_DESCRIPTION),
                  minLines = integerResource(R.integer.todo_description_min_lines),
                  maxLines = integerResource(R.integer.todo_description_max_lines))

              // Assignee Input
              OutlinedTextField(
                  value = todoUIState.assignee,
                  onValueChange = { editTodoViewModel.onAssigneeChanged(it) },
                  label = { Text(stringResource(R.string.todos_assignee_field_label)) },
                  placeholder = { Text(stringResource(R.string.todos_assignee_placeholder)) },
                  isError = todoUIState.assigneeError != null,
                  supportingText = {
                    todoUIState.assigneeError?.let {
                      Text(it, modifier = Modifier.testTag(EditToDoScreenTestTags.ERROR_MESSAGE))
                    }
                  },
                  colors = textFieldColors,
                  modifier =
                      Modifier.fillMaxWidth().testTag(EditToDoScreenTestTags.INPUT_TODO_ASSIGNEE))

              // Location Input with dropdown
              Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = todoUIState.location,
                    onValueChange = {
                      editTodoViewModel.onLocationChanged(it)
                      showLocationDropdown = it.isNotBlank()
                    },
                    label = { Text(stringResource(R.string.todos_location_field_label)) },
                    placeholder = {
                      Text(stringResource(R.string.todos_location_field_placeholder))
                    },
                    colors = textFieldColors,
                    modifier =
                        Modifier.fillMaxWidth().testTag(EditToDoScreenTestTags.INPUT_TODO_LOCATION))

                DropdownMenu(
                    expanded = showLocationDropdown && todoUIState.suggestions.isNotEmpty(),
                    onDismissRequest = { showLocationDropdown = false },
                    properties = PopupProperties(focusable = false),
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    modifier =
                        Modifier.testTag(EditToDoScreenTestTags.LOCATION_MENU)
                            .fillMaxWidth()
                            .heightIn(dimensionResource(R.dimen.todo_location_dropdown_height))) {
                      todoUIState.suggestions
                          .filterNotNull()
                          .take(integerResource(R.integer.todo_location_number_of_suggestions))
                          .forEach { loc ->
                            DropdownMenuItem(
                                text = {
                                  Text(
                                      color = MaterialTheme.colorScheme.outline,
                                      maxLines = 1, // Ensure name doesn't overflow
                                      text =
                                          loc.name.take(
                                              integerResource(
                                                  R.integer.todo_location_suggestion_length)) +
                                              if (loc.name.length >
                                                  integerResource(
                                                      R.integer.todo_location_suggestion_length))
                                                  stringResource(R.string.todos_location_text_etc)
                                              else "")
                                },
                                onClick = {
                                  editTodoViewModel.selectLocation(loc)
                                  showLocationDropdown = false
                                },
                                modifier =
                                    Modifier.padding(
                                            dimensionResource(
                                                R.dimen.todo_location_dropdown_padding))
                                        .testTag(EditToDoScreenTestTags.INPUT_TODO_LOCATION))
                            Divider() // Separate items with a divider
                          }
                      if (todoUIState.suggestions.size >
                          integerResource(R.integer.todo_location_number_of_suggestions)) {
                        DropdownMenuItem(
                            text = {
                              Text(stringResource(R.string.todos_location_suggestions_more))
                            },
                            onClick = {})
                      }
                    }
              }
              // Due Date Input
              OutlinedTextField(
                  value = todoUIState.dueDate,
                  onValueChange = { editTodoViewModel.onDateChanged(it) },
                  label = { Text(stringResource(R.string.todos_date_field_label)) },
                  placeholder = { Text(stringResource(R.string.todos_date_field_placeholder)) },
                  isError = todoUIState.dueDateError != null,
                  supportingText = {
                    todoUIState.dueDateError?.let {
                      Text(it, modifier = Modifier.testTag(EditToDoScreenTestTags.ERROR_MESSAGE))
                    }
                  },
                  colors = textFieldColors,
                  modifier =
                      Modifier.fillMaxWidth().testTag(EditToDoScreenTestTags.INPUT_TODO_DATE))

              // Due Time Input
              OutlinedTextField(
                  value = todoUIState.dueTime,
                  onValueChange = { editTodoViewModel.onTimeChanged(it) },
                  label = { Text(stringResource(R.string.todos_time_field_label)) },
                  placeholder = { Text(stringResource(R.string.todos_time_field_placeholder)) },
                  isError = todoUIState.dueTimeError != null,
                  supportingText = {
                    todoUIState.dueTimeError?.let {
                      Text(it, modifier = Modifier.testTag(EditToDoScreenTestTags.ERROR_MESSAGE))
                    }
                  },
                  colors = textFieldColors,
                  modifier =
                      Modifier.fillMaxWidth().testTag(EditToDoScreenTestTags.INPUT_TODO_TIME))

              Spacer(modifier = Modifier.height(fieldSpacing))

              // Observe save success
              LaunchedEffect(todoUIState.saveSuccess) {
                if (todoUIState.saveSuccess) {
                  onSave()
                  editTodoViewModel.clearSaveSuccess()
                }
              }
              // Save Button
              Button(
                  onClick = { editTodoViewModel.editTodo(todoUid) },
                  modifier = Modifier.fillMaxWidth().testTag(EditToDoScreenTestTags.TODO_SAVE),
                  colors =
                      ButtonDefaults.buttonColors(
                          containerColor = MaterialTheme.colorScheme.secondary),
                  enabled =
                      todoUIState.dueDateError == null &&
                          todoUIState.assigneeError == null &&
                          todoUIState.descriptionError == null &&
                          todoUIState.titleError == null &&
                          todoUIState.dueTimeError == null &&
                          !todoUIState.isSaving) {
                    Text(
                        stringResource(R.string.todos_save_button_text),
                        color = MaterialTheme.colorScheme.primary)
                  }

              Spacer(modifier = Modifier.height(buttonSpacing))

              // Observe delete success
              LaunchedEffect(todoUIState.deleteSuccess) {
                if (todoUIState.deleteSuccess) {
                  onDelete()
                  editTodoViewModel.clearDeleteSuccess()
                }
              }
              // Delete Button
              TextButton(
                  onClick = { editTodoViewModel.deleteToDo(todoUid) },
                  modifier = Modifier.fillMaxWidth().testTag(EditToDoScreenTestTags.TODO_DELETE),
                  // TextButton has no background by default
                  colors =
                      ButtonDefaults.textButtonColors(
                          contentColor = MaterialTheme.colorScheme.tertiary)) {
                    Row {
                      Icon(
                          imageVector = Icons.Filled.DeleteForever,
                          contentDescription = stringResource(R.string.todos_delete_button_text),
                          tint = MaterialTheme.colorScheme.tertiary)
                      Text(
                          text = stringResource(R.string.todos_delete_button_text),
                          color = MaterialTheme.colorScheme.tertiary,
                          modifier = Modifier.padding(start = buttonSpacing))
                    }
                  }
            }
      })
}
