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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.R
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todoCategory.ToDoCategory
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu_Goback
import com.android.gatherly.ui.theme.GatherlyTheme
import com.android.gatherly.utils.AlertDialogCreateTag
import com.android.gatherly.utils.AlertDialogWarningDeleteTag
import com.android.gatherly.utils.CategoriesDropDown
import com.android.gatherly.utils.DatePickerInputField
import com.android.gatherly.utils.GatherlyAlertDialog
import com.android.gatherly.utils.GatherlyAlertDialogActions
import com.android.gatherly.utils.GatherlyDatePicker
import com.android.gatherly.utils.PriorityDropDown
import com.android.gatherly.utils.TimeInputField
import com.android.gatherly.utils.ToDoLocationSuggestionsUtils
import kotlinx.coroutines.delay

// Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the
// SwEnt staff.

private const val DELAY = 1000L

/** Contains test tags used for UI testing on the [AddTodoScreen]. */
object AddToDoScreenTestTags {
  /** Tag for the [ToDo] title input field. */
  const val INPUT_TODO_TITLE = "inputTodoTitle"

  /** Tag for the [ToDo] description input field. */
  const val INPUT_TODO_DESCRIPTION = "inputTodoDescription"

  /** Tag for the [ToDo] due date input field. */
  const val INPUT_TODO_DATE = "inputTodoDate"

  /** Tag for the [ToDo] due time input field. */
  const val INPUT_TODO_TIME = "inputTodoTime"

  /** Tag for the Save button that submits the [ToDo]. */
  const val TODO_SAVE = "todoSave"

  /** Tag for displaying error messages under text fields. */
  const val ERROR_MESSAGE = "errorMessage"

  /** Tag for the extra options button */
  const val MORE_OPTIONS = "moreOptions"
}

/**
 * Text field colors defined outside the composable scope to be shared between [AddTodoScreen] and
 * [EditTodoScreen]
 */
val toDoTextFieldColors
  @Composable
  get() =
      OutlinedTextFieldDefaults.colors(
          focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
          unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
          errorContainerColor = MaterialTheme.colorScheme.surfaceVariant,
          unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
          focusedTextColor = MaterialTheme.colorScheme.onBackground,
          errorTextColor = MaterialTheme.colorScheme.onBackground,
          focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
          unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
          focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
          unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
          unfocusedBorderColor = Color.Transparent,
          focusedBorderColor = Color.Transparent,
          disabledBorderColor = Color.Transparent,
          errorBorderColor = Color.Transparent)

/**
 * Displays the screen for creating and saving a new [ToDo].
 *
 * @param addTodoViewModel The [AddTodoViewModel] that provides the current [ToDo] state.
 * @param onAdd Callback invoked after a To-Do has been successfully added.
 * @param goBack Callback triggered when the back arrow in the top app bar is pressed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTodoScreen(
    addTodoViewModel: AddTodoViewModel = viewModel(),
    onAdd: () -> Unit = {},
    goBack: () -> Unit = {},
) {
  val todoUIState by addTodoViewModel.uiState.collectAsState()
  val errorMsg = todoUIState.saveError

  val context = LocalContext.current
  val expandAdvanced = remember { mutableStateOf(false) }

  val screenPadding = dimensionResource(id = R.dimen.padding_screen)
  val fieldSpacing = dimensionResource(id = R.dimen.spacing_between_fields)
  val inputHeight = dimensionResource(id = R.dimen.input_height)

  // Date state for the alert dialog visibility
  var showDatePicker by remember { mutableStateOf(false) }

  // Create a new category alert dialog visibility
  val showCreateTagDialog = remember { mutableStateOf(false) }
  val showWarningDeleteTagDialog = remember { mutableStateOf<ToDoCategory?>(null) }
  val categoriesList by addTodoViewModel.categories.collectAsStateWithLifecycle()

  LaunchedEffect(errorMsg) {
    if (errorMsg != null) {
      Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
      addTodoViewModel.clearErrorMsg()
    }
  }

  // Search location when input changes
  LaunchedEffect(todoUIState.location) {
    if (todoUIState.location.isNotBlank()) {
      delay(DELAY)
      addTodoViewModel.searchLocationByString(todoUIState.location)
    }
  }

  // Observe save success
  LaunchedEffect(todoUIState.saveSuccess) {
    if (todoUIState.saveSuccess) {
      onAdd()
      addTodoViewModel.clearSaveSuccess()
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(screenPadding),
            verticalArrangement = Arrangement.spacedBy(fieldSpacing)) {
              // Title Input
              item {
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
                    colors = toDoTextFieldColors,
                    modifier =
                        Modifier.fillMaxWidth().testTag(AddToDoScreenTestTags.INPUT_TODO_TITLE))
              }

              // Description Input
              item {
                OutlinedTextField(
                    value = todoUIState.description,
                    onValueChange = { addTodoViewModel.onDescriptionChanged(it) },
                    label = { Text(stringResource(R.string.todos_description_field_label)) },
                    placeholder = { Text(stringResource(R.string.todos_description_placeholder)) },
                    colors = toDoTextFieldColors,
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(inputHeight)
                            .testTag(AddToDoScreenTestTags.INPUT_TODO_DESCRIPTION),
                    minLines = integerResource(R.integer.todo_description_min_lines),
                    maxLines = integerResource(R.integer.todo_description_max_lines))
              }

              // More options bar
              item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically) {
                      Icon(
                          imageVector = Icons.Default.ChevronRight,
                          contentDescription = null,
                          modifier =
                              Modifier.rotate(90f * expandAdvanced.value.compareTo(false))
                                  .clickable(
                                      onClick = { expandAdvanced.value = !expandAdvanced.value })
                                  .testTag(AddToDoScreenTestTags.MORE_OPTIONS))

                      Text(
                          text = stringResource(R.string.todos_advanced_settings),
                          modifier =
                              Modifier.weight(
                                  integerResource(R.integer.todo_options_bar_weight).toFloat()))
                    }
              }

              if (expandAdvanced.value) {

                // Buttons row
                item {
                  Row(
                      horizontalArrangement =
                          Arrangement.spacedBy(
                              dimensionResource(
                                  R.dimen.todo_buttons_row_horizontal_arrangement_space))) {

                        // Category drop down
                        CategoriesDropDown(
                            { category -> addTodoViewModel.selectTodoTag(category) },
                            showCreateTagDialog,
                            todoUIState.tag,
                            showWarningDeleteTagDialog,
                            categoriesList)
                        // Priority level drop down
                        PriorityDropDown(
                            onSelectPriorityLevel = { level ->
                              addTodoViewModel.selectPriorityLevel(level)
                            },
                            currentPriorityLevel = todoUIState.priorityLevel)
                      }
                }

                // Location Input with dropdown
                item {
                  ToDoLocationSuggestionsUtils(
                      location = todoUIState.location,
                      suggestions = todoUIState.suggestions,
                      onLocationChanged = { addTodoViewModel.onLocationChanged(it) },
                      onSelectLocation = { loc -> addTodoViewModel.selectLocation(loc) },
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
                              AddToDoScreenTestTags.INPUT_TODO_DATE,
                              AddToDoScreenTestTags.ERROR_MESSAGE))
                }

                // Due Time Input
                item {
                  TimeInputField(
                      initialTime = todoUIState.dueTime,
                      onTimeChanged = { addTodoViewModel.onTimeChanged(it) },
                      dueTimeError = (todoUIState.dueTimeError != null),
                      label = stringResource(R.string.todos_time_field_label),
                      textFieldColors = toDoTextFieldColors,
                      testTagInput = AddToDoScreenTestTags.INPUT_TODO_TIME,
                      testTagErrorMessage = AddToDoScreenTestTags.ERROR_MESSAGE,
                  )
                }
              }

              item { Spacer(modifier = Modifier.height(fieldSpacing)) }

              // Save Button
              item {
                Button(
                    onClick = { addTodoViewModel.checkTodoTime() },
                    modifier = Modifier.fillMaxWidth().testTag(AddToDoScreenTestTags.TODO_SAVE),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary),
                    enabled = todoUIState.isValid) {
                      SavingText(todoUIState = todoUIState)
                    }
              }
            }
        if (todoUIState.pastTime) {
          GatherlyAlertDialog(
              titleText = stringResource(R.string.todos_past_warning),
              bodyText = stringResource(R.string.todos_past_warning_text),
              dismissText = stringResource(R.string.cancel),
              confirmText = stringResource(R.string.todos_create),
              actions =
                  GatherlyAlertDialogActions(
                      onDismiss = { addTodoViewModel.clearPastTime() },
                      onConfirm = {
                        addTodoViewModel.saveTodo()
                        addTodoViewModel.clearPastTime()
                      }),
          )
        }

        GatherlyDatePicker(
            show = showDatePicker,
            initialDate = todoUIState.dueDate,
            onDateSelected = { selectedDate -> addTodoViewModel.onDateChanged(selectedDate) },
            onDismiss = { showDatePicker = false })
      })

  AlertDialogCreateTag(
      showCreateTagDialog,
      onCreateTag = { name, color -> addTodoViewModel.addCategory(name, color) })

  AlertDialogWarningDeleteTag(
      showWarningDeleteTagDialog,
      onConfirmDelete = { category -> addTodoViewModel.deleteCategory(category) })
}

/**
 * Displays the text inside the Save button, changing it to "Saving..." when a save operation is in
 * progress.
 *
 * @param todoUIState The current UI state of the Add To-Do screen.
 */
@Composable
fun SavingText(todoUIState: AddTodoUiState) {
  Text(
      text =
          if (todoUIState.isSaving) {
            stringResource(R.string.saving)
          } else {
            stringResource(R.string.todos_save_button_text)
          },
      color = MaterialTheme.colorScheme.onSecondary)
}

/** Preview of the AddToDoScreen in dark theme. */
@Preview
@Composable
fun AddToDoScreenPreview() {
  GatherlyTheme(darkTheme = true) { AddTodoScreen() }
}
