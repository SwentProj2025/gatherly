package com.android.gatherly.ui.todo

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.R
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu_Goback
import com.android.gatherly.ui.theme.GatherlyTheme
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

  val textFieldColors =
      TextFieldDefaults.colors(
          focusedContainerColor = MaterialTheme.colorScheme.background,
          unfocusedContainerColor = MaterialTheme.colorScheme.background,
          unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
          focusedTextColor = MaterialTheme.colorScheme.onBackground,
          errorTextColor = MaterialTheme.colorScheme.onBackground,
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

  // Observe save success
  LaunchedEffect(todoUIState.saveSuccess) {
    if (todoUIState.saveSuccess) {
      onSave()
      editTodoViewModel.clearSaveSuccess()
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingVal).padding(screenPadding),
            verticalArrangement = Arrangement.spacedBy(fieldSpacing)) {

              // Title Input
              item {
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
              }

              // Description Input
              item {
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
              }

              // Assignee Input
              item {
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
              }

              // Location Input with dropdown
              item {
                LocationSuggestions(
                    location = todoUIState.location,
                    suggestions = todoUIState.suggestions,
                    onLocationChanged = { editTodoViewModel.onLocationChanged(it) },
                    onSelectLocation = { loc -> editTodoViewModel.selectLocation(loc) },
                    modifier = Modifier.fillMaxWidth(),
                    testTagInput = EditToDoScreenTestTags.INPUT_TODO_LOCATION,
                    testTagDropdown = EditToDoScreenTestTags.LOCATION_MENU,
                )
              }

              // Due Date Input
              item {
                DateInputField(
                    onDateChanged = { editTodoViewModel.onDateChanged(it) },
                    dueDateError = todoUIState.dueDateError,
                    textFieldColors = textFieldColors,
                    testTagInput = EditToDoScreenTestTags.INPUT_TODO_DATE,
                    testTagErrorMessage = EditToDoScreenTestTags.ERROR_MESSAGE,
                )
              }

              // Due Time Input
              item {
                TimeInputField(
                    onTimeChanged = { editTodoViewModel.onTimeChanged(it) },
                    dueTimeError = todoUIState.dueTimeError,
                    textFieldColors = textFieldColors,
                    testTagInput = EditToDoScreenTestTags.INPUT_TODO_TIME,
                    testTagErrorMessage = EditToDoScreenTestTags.ERROR_MESSAGE,
                )
              }

              item { Spacer(modifier = Modifier.height(fieldSpacing)) }

              // Save Button
              item {
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
                          color = MaterialTheme.colorScheme.onSecondary)
                    }
              }

              item { Spacer(modifier = Modifier.height(buttonSpacing)) }

              // Observe delete success

              item {
                LaunchedEffect(todoUIState.deleteSuccess) {
                  if (todoUIState.deleteSuccess) {
                    onDelete()
                    editTodoViewModel.clearDeleteSuccess()
                  }
                }
              }

              // Delete Button
              item {
                TextButton(
                    onClick = { editTodoViewModel.deleteToDo(todoUid) },
                    modifier = Modifier.fillMaxWidth().testTag(EditToDoScreenTestTags.TODO_DELETE),
                    // TextButton has no background by default
                    colors =
                        ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error)) {
                      Row {
                        Icon(
                            imageVector = Icons.Filled.DeleteForever,
                            contentDescription = stringResource(R.string.todos_delete_button_text),
                            tint = MaterialTheme.colorScheme.error)
                        Text(
                            text = stringResource(R.string.todos_delete_button_text),
                            color = MaterialTheme.colorScheme.error,
                            modifier =
                                Modifier.padding(start = buttonSpacing)
                                    .align(Alignment.CenterVertically))
                      }
                    }
              }
            }
      })
}

// Helper function to preview the timer screen
@Preview
@Composable
fun EditToDoScreenPreview() {
  GatherlyTheme(darkTheme = false) { EditToDoScreen(todoUid = "1") }
}
