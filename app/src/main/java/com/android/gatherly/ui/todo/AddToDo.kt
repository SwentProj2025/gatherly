package com.android.gatherly.ui.todo

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.R
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu_Goback
import kotlinx.coroutines.delay

// Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the
// SwEnt staff.

/** Contains test tags used for UI testing on the Add To-Do screen. */
object AddToDoScreenTestTags {
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

  /** Tag for displaying error messages under text fields. */
  const val ERROR_MESSAGE = "errorMessage"

  /** Tag for displaying error messages under text fields. */
  const val LOCATION_MENU = "locationMenu"
}

/**
 * Displays the screen for creating and saving a new [ToDo].
 *
 * @param addTodoViewModel The [AddTodoViewModel] that provides the current ToDo state.
 * @param onAdd Callback invoked after a To-Do has been successfully added.
 * @param goBack Callback triggered when the back arrow in the top app bar is pressed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToDoScreen(
    addTodoViewModel: AddTodoViewModel = viewModel(),
    onAdd: () -> Unit = {},
    goBack: () -> Unit = {},
) {
  val todoUIState by addTodoViewModel.uiState.collectAsState()
  val errorMsg = todoUIState.saveError

  val context = LocalContext.current

  val screenPadding = dimensionResource(id = R.dimen.padding_screen)
  val fieldSpacing = dimensionResource(id = R.dimen.spacing_between_fields)
  val inputHeight = dimensionResource(id = R.dimen.input_height)

  val textFieldColors =
      TextFieldDefaults.colors(
          focusedContainerColor = MaterialTheme.colorScheme.background,
          unfocusedContainerColor = MaterialTheme.colorScheme.background,
          unfocusedTextColor = MaterialTheme.colorScheme.primary,
          focusedTextColor = MaterialTheme.colorScheme.primary,
      )

  LaunchedEffect(errorMsg) {
    if (errorMsg != null) {
      Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
      addTodoViewModel.clearErrorMsg()
    }
  }

  // Search location when input changes
  LaunchedEffect(todoUIState.location) {
    if (todoUIState.location.isNotBlank()) {
      delay(1000)
      addTodoViewModel.searchLocationByString(todoUIState.location)
    }
  }

  Scaffold(
      topBar = {
        TopNavigationMenu_Goback(
            selectedTab = Tab.AddTodo,
            modifier = Modifier.testTag(NavigationTestTags.TOP_NAVIGATION_MENU),
            goBack = goBack)
      },
      content = { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(screenPadding),
            verticalArrangement = Arrangement.spacedBy(fieldSpacing)) {
              // Title Input
              OutlinedTextField(
                  value = todoUIState.title,
                  onValueChange = { addTodoViewModel.onTitleChanged(it) },
                  label = { Text(stringResource(R.string.todos_title_field_label)) },
                  placeholder = { Text(stringResource(R.string.todos_title_field_placeholder)) },
                  isError = todoUIState.titleError != null,
                  supportingText = {
                    todoUIState.titleError?.let {
                      Text(it, modifier = Modifier.testTag(AddToDoScreenTestTags.ERROR_MESSAGE))
                    }
                  },
                  colors = textFieldColors,
                  modifier =
                      Modifier.fillMaxWidth().testTag(AddToDoScreenTestTags.INPUT_TODO_TITLE))

              // Description Input
              OutlinedTextField(
                  value = todoUIState.description,
                  onValueChange = { addTodoViewModel.onDescriptionChanged(it) },
                  label = { Text(stringResource(R.string.todos_description_field_label)) },
                  placeholder = { Text(stringResource(R.string.todos_description_placeholder)) },
                  isError = todoUIState.descriptionError != null,
                  supportingText = {
                    todoUIState.descriptionError?.let {
                      Text(it, modifier = Modifier.testTag(AddToDoScreenTestTags.ERROR_MESSAGE))
                    }
                  },
                  colors = textFieldColors,
                  modifier =
                      Modifier.fillMaxWidth()
                          .height(inputHeight)
                          .testTag(AddToDoScreenTestTags.INPUT_TODO_DESCRIPTION),
                  minLines = integerResource(R.integer.todo_description_min_lines),
                  maxLines = integerResource(R.integer.todo_description_max_lines))

              // Assignee Input
              OutlinedTextField(
                  value = todoUIState.assignee,
                  onValueChange = { addTodoViewModel.onAssigneeChanged(it) },
                  label = { Text(stringResource(R.string.todos_assignee_field_label)) },
                  placeholder = { Text(stringResource(R.string.todos_assignee_placeholder)) },
                  isError = todoUIState.assigneeError != null,
                  supportingText = {
                    todoUIState.assigneeError?.let {
                      Text(it, modifier = Modifier.testTag(AddToDoScreenTestTags.ERROR_MESSAGE))
                    }
                  },
                  colors = textFieldColors,
                  modifier =
                      Modifier.fillMaxWidth().testTag(AddToDoScreenTestTags.INPUT_TODO_ASSIGNEE))

              // Location Input with dropdown
              LocationSuggestions(
                  location = todoUIState.location,
                  suggestions = todoUIState.suggestions,
                  onLocationChanged = { addTodoViewModel.onLocationChanged(it) },
                  onSelectLocation = { loc -> addTodoViewModel.selectLocation(loc) },
                  modifier = Modifier.fillMaxWidth(),
                  testTagInput = AddToDoScreenTestTags.INPUT_TODO_LOCATION,
                  testTagDropdown = AddToDoScreenTestTags.LOCATION_MENU,
              )

              // Due Date Input
              OutlinedTextField(
                  value = todoUIState.dueDate,
                  onValueChange = { addTodoViewModel.onDateChanged(it) },
                  label = { Text(stringResource(R.string.todos_date_field_label)) },
                  placeholder = { Text(stringResource(R.string.todos_date_field_placeholder)) },
                  isError = todoUIState.dueDateError != null,
                  supportingText = {
                    todoUIState.dueDateError?.let {
                      Text(it, modifier = Modifier.testTag(AddToDoScreenTestTags.ERROR_MESSAGE))
                    }
                  },
                  colors = textFieldColors,
                  modifier = Modifier.fillMaxWidth().testTag(AddToDoScreenTestTags.INPUT_TODO_DATE))

              // Due Time Input
              OutlinedTextField(
                  value = todoUIState.dueTime,
                  onValueChange = { addTodoViewModel.onTimeChanged(it) },
                  label = { Text(stringResource(R.string.todos_time_field_label)) },
                  placeholder = { Text(stringResource(R.string.todos_time_field_placeholder)) },
                  isError = todoUIState.dueTimeError != null,
                  supportingText = {
                    todoUIState.dueTimeError?.let {
                      Text(it, modifier = Modifier.testTag(AddToDoScreenTestTags.ERROR_MESSAGE))
                    }
                  },
                  colors = textFieldColors,
                  modifier = Modifier.fillMaxWidth().testTag(AddToDoScreenTestTags.INPUT_TODO_TIME))

              Spacer(modifier = Modifier.height(fieldSpacing))

              // Observe save success
              LaunchedEffect(todoUIState.saveSuccess) {
                if (todoUIState.saveSuccess) {
                  onAdd()
                  addTodoViewModel.clearSaveSuccess()
                }
              }
              // Save Button
              Button(
                  onClick = { addTodoViewModel.saveTodo() },
                  modifier = Modifier.fillMaxWidth().testTag(AddToDoScreenTestTags.TODO_SAVE),
                  colors =
                      ButtonDefaults.buttonColors(
                          containerColor = MaterialTheme.colorScheme.secondary),
                  enabled =
                      todoUIState.dueDateError == null &&
                          todoUIState.assigneeError == null &&
                          todoUIState.descriptionError == null &&
                          todoUIState.titleError == null &&
                          todoUIState.dueTimeError == null &&
                          todoUIState.locationError == null) {
                    Text(
                        stringResource(R.string.todos_save_button_text),
                        color = MaterialTheme.colorScheme.primary)
                  }
            }
      })
}
