package com.android.gatherly.ui.todo

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
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
import com.android.gatherly.utils.DatePickerInputField
import com.android.gatherly.utils.GatherlyAlertDialog
import com.android.gatherly.utils.GatherlyDatePicker
import kotlinx.coroutines.delay

// Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the
// SwEnt staff.

/** Contains test tags used for UI testing on the Edit To-Do screen. */
object EditToDoScreenTestTags {
  /** Tag for the To-Do title input field. */
  const val INPUT_TODO_TITLE = "inputTodoTitle"

  /** Tag for the To-Do description input field. */
  const val INPUT_TODO_DESCRIPTION = "inputTodoDescription"

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
  const val MORE_OPTIONS = "moreOptions"
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
  val shouldShowDialog = remember { mutableStateOf(false) }
  val expandAdvanced = remember { mutableStateOf(false) }

  val screenPadding = dimensionResource(id = R.dimen.padding_screen)
  val fieldSpacing = dimensionResource(id = R.dimen.spacing_between_fields)
  val buttonSpacing = dimensionResource(id = R.dimen.spacing_between_buttons)

  // Date state for the alert dialog visibilty
  var showDatePicker by remember { mutableStateOf(false) }

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
                    colors = toDoTextFieldColors,
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
                    colors = toDoTextFieldColors,
                    modifier =
                        Modifier.fillMaxWidth()
                            .testTag(EditToDoScreenTestTags.INPUT_TODO_DESCRIPTION),
                    minLines = integerResource(R.integer.todo_description_min_lines),
                    maxLines = integerResource(R.integer.todo_description_max_lines))
              }

              item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically) {
                      Icon(
                          imageVector = Icons.Default.ChevronRight,
                          contentDescription = null,
                          modifier =
                              Modifier.rotate(if (expandAdvanced.value) 90f else 0f)
                                  .clickable(
                                      onClick = { expandAdvanced.value = !expandAdvanced.value })
                                  .testTag(EditToDoScreenTestTags.MORE_OPTIONS))

                      Text(
                          text = stringResource(R.string.todos_advanced_settings),
                          modifier = Modifier.weight(1f))
                    }
              }

              // Location Input with dropdown
              item {
                LocationSuggestions(
                    location = todoUIState.location,
                    suggestions = todoUIState.suggestions,
                    onLocationChanged = { editTodoViewModel.onLocationChanged(it) },
                    onSelectLocation = { loc -> editTodoViewModel.selectLocation(loc) },
                    modifier = Modifier.fillMaxWidth(),
                    textFieldColors = toDoTextFieldColors)
              }

              // Due Date Input
              item {
                DatePickerInputField(
                    value = todoUIState.dueDate,
                    label = stringResource(R.string.todos_date_field_label),
                    isErrorMessage = todoUIState.dueDateError,
                    onClick = { showDatePicker = true },
                    colors = toDoTextFieldColors,
                    testTag =
                        Pair(
                            EditToDoScreenTestTags.INPUT_TODO_DATE,
                            EditToDoScreenTestTags.ERROR_MESSAGE))
              }

              // Due Time Input
              item {
                TimeInputField(
                    initialTime = todoUIState.dueTime,
                    onTimeChanged = { editTodoViewModel.onTimeChanged(it) },
                    dueTimeError = todoUIState.dueTimeError,
                    textFieldColors = toDoTextFieldColors,
                    testTagInput = EditToDoScreenTestTags.INPUT_TODO_TIME,
                    testTagErrorMessage = EditToDoScreenTestTags.ERROR_MESSAGE,
                )
              }

              item { Spacer(modifier = Modifier.height(fieldSpacing)) }

              // Save Button
              item {
                Button(
                    onClick = { editTodoViewModel.checkPastTime() },
                    modifier = Modifier.fillMaxWidth().testTag(EditToDoScreenTestTags.TODO_SAVE),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary),
                    enabled = todoUIState.isValid && !todoUIState.isSaving) {
                      Text(
                          text =
                              if (todoUIState.isSaving) {
                                stringResource(R.string.saving)
                              } else {
                                stringResource(R.string.todos_save_button_text)
                              },
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
                    onClick = { shouldShowDialog.value = true },
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

        if (shouldShowDialog.value) {
          GatherlyAlertDialog(
              titleText = stringResource(R.string.todos_delete_warning),
              bodyText = stringResource(R.string.todos_delete_warning_text),
              dismissText = stringResource(R.string.cancel),
              confirmText = stringResource(R.string.delete),
              onDismiss = { shouldShowDialog.value = false },
              onConfirm = {
                editTodoViewModel.deleteToDo(todoID = todoUid)
                shouldShowDialog.value = false
              },
              isImportantWarning = true)
        }

        if (todoUIState.pastTime) {
          GatherlyAlertDialog(
              titleText = stringResource(R.string.todos_past_warning),
              bodyText = stringResource(R.string.todos_past_warning_text),
              dismissText = stringResource(R.string.cancel),
              confirmText = stringResource(R.string.todos_edit),
              onDismiss = { editTodoViewModel.clearPastTime() },
              onConfirm = {
                editTodoViewModel.editTodo(todoUid)
                editTodoViewModel.clearPastTime()
              })
        }
        GatherlyDatePicker(
            show = showDatePicker,
            initialDate = todoUIState.dueDate,
            onDateSelected = { selectedDate -> editTodoViewModel.onDateChanged(selectedDate) },
            onDismiss = { showDatePicker = false })
      })
}

// Helper function to preview the timer screen
@Preview
@Composable
fun EditToDoScreenPreview() {
  GatherlyTheme(darkTheme = false) { EditToDoScreen(todoUid = "1") }
}
