package com.android.gatherly.ui.todo

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

// Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the
// SwEnt staff.

object AddToDoScreenTestTags {
  const val INPUT_TODO_TITLE = "inputTodoTitle"
  const val INPUT_TODO_DESCRIPTION = "inputTodoDescription"
  const val INPUT_TODO_ASSIGNEE = "inputTodoAssignee"
  const val INPUT_TODO_LOCATION = "inputTodoLocation"
  const val INPUT_TODO_DATE = "inputTodoDate"
  const val INPUT_TODO_TIME = "inputTodoTime"
  const val TODO_SAVE = "todoSave"
  const val ERROR_MESSAGE = "errorMessage"
}

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

  LaunchedEffect(errorMsg) {
    if (errorMsg != null) {
      Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
      addTodoViewModel.clearErrorMsg()
    }
  }

  Scaffold(
      // TODO: modify this part with the specific top bar implemented in the navigation menu.
      topBar = {
        TopAppBar(
            title = { Text("Add To-Do") },
            navigationIcon = {
              IconButton(onClick = { goBack() }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
              }
            },
            colors =
                TopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = MaterialTheme.colorScheme.primary,
                ))
      },
      // TODO: add the bottom bar for navigation, once everything is merged.
      content = { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
              // Title Input
              OutlinedTextField(
                  value = todoUIState.title,
                  onValueChange = { addTodoViewModel.onTitleChanged(it) },
                  label = { Text("Title") },
                  placeholder = { Text("Task Title") },
                  isError = todoUIState.titleError != null,
                  supportingText = {
                    todoUIState.titleError?.let {
                      Text(it, modifier = Modifier.testTag(AddToDoScreenTestTags.ERROR_MESSAGE))
                    }
                  },
                  modifier =
                      Modifier.fillMaxWidth().testTag(AddToDoScreenTestTags.INPUT_TODO_TITLE))

              // Description Input
              OutlinedTextField(
                  value = todoUIState.description,
                  onValueChange = { addTodoViewModel.onDescriptionChanged(it) },
                  label = { Text("Description") },
                  placeholder = { Text("Describe the task") },
                  isError = todoUIState.descriptionError != null,
                  supportingText = {
                    todoUIState.descriptionError?.let {
                      Text(it, modifier = Modifier.testTag(AddToDoScreenTestTags.ERROR_MESSAGE))
                    }
                  },
                  modifier =
                      Modifier.fillMaxWidth()
                          .height(100.dp)
                          .testTag(AddToDoScreenTestTags.INPUT_TODO_DESCRIPTION))

              // Assignee Input
              OutlinedTextField(
                  value = todoUIState.assignee,
                  onValueChange = { addTodoViewModel.onAssigneeChanged(it) },
                  label = { Text("Assignee") },
                  placeholder = { Text("Assign a person") },
                  isError = todoUIState.assigneeError != null,
                  supportingText = {
                    todoUIState.assigneeError?.let {
                      Text(it, modifier = Modifier.testTag(AddToDoScreenTestTags.ERROR_MESSAGE))
                    }
                  },
                  modifier =
                      Modifier.fillMaxWidth().testTag(AddToDoScreenTestTags.INPUT_TODO_ASSIGNEE))

              // Placeholder Location Input
              OutlinedTextField(
                  value = todoUIState.location,
                  onValueChange = { addTodoViewModel.onLocationChanged(it) },
                  label = { Text("Location") },
                  placeholder = { Text("Enter an Address or Location") },
                  modifier =
                      Modifier.fillMaxWidth().testTag(AddToDoScreenTestTags.INPUT_TODO_LOCATION),
              )

              // Due Date Input
              OutlinedTextField(
                  value = todoUIState.dueDate,
                  onValueChange = { addTodoViewModel.onDateChanged(it) },
                  label = { Text("Due date") },
                  placeholder = { Text("10/10/2025") },
                  isError = todoUIState.dueDateError != null,
                  supportingText = {
                    todoUIState.dueDateError?.let {
                      Text(it, modifier = Modifier.testTag(AddToDoScreenTestTags.ERROR_MESSAGE))
                    }
                  },
                  modifier = Modifier.fillMaxWidth().testTag(AddToDoScreenTestTags.INPUT_TODO_DATE))

              // Due Time Input
              OutlinedTextField(
                  value = todoUIState.dueTime,
                  onValueChange = { addTodoViewModel.onTimeChanged(it) },
                  label = { Text("Due time") },
                  placeholder = { Text("14:00") },
                  isError = todoUIState.dueTimeError != null,
                  supportingText = {
                    todoUIState.dueTimeError?.let {
                      Text(it, modifier = Modifier.testTag(AddToDoScreenTestTags.ERROR_MESSAGE))
                    }
                  },
                  modifier = Modifier.fillMaxWidth().testTag(AddToDoScreenTestTags.INPUT_TODO_TIME))

              Spacer(modifier = Modifier.height(8.dp))

              // Save Button
              Button(
                  onClick = {
                    addTodoViewModel.saveTodo()
                    onAdd()
                  },
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
                    Text("Save", color = MaterialTheme.colorScheme.primary)
                  }
            }
      })
}
