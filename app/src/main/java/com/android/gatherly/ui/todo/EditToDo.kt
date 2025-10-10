package com.android.gatherly.ui.todo

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

object EditToDoScreenTestTags {
  const val INPUT_TODO_TITLE = "inputTodoTitle"
  const val INPUT_TODO_DESCRIPTION = "inputTodoDescription"
  const val INPUT_TODO_ASSIGNEE = "inputTodoAssignee"
  const val INPUT_TODO_LOCATION = "inputTodoLocation"
  const val INPUT_TODO_DATE = "inputTodoDate"
  const val TODO_SAVE = "todoSave"
  const val TODO_DELETE = "todoDelete"
  const val ERROR_MESSAGE = "errorMessage"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditToDoScreen(
    todoUid: String,
    editTodoViewModel: EditTodoViewModel = viewModel(),
    onEdit: () -> Unit = {},
    goBack: () -> Unit = {},
) {
  LaunchedEffect(todoUid) { editTodoViewModel.loadTodo(todoUid) }

  val todoUIState by editTodoViewModel.uiState.collectAsState()
  val errorMsg = todoUIState.errorMsg

  val context = LocalContext.current

  LaunchedEffect(errorMsg) {
    if (errorMsg != null) {
      Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
      editTodoViewModel.clearErrorMsg()
    }
  }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Edit To-Do") },
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
      content = { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
              // Title Input
              OutlinedTextField(
                  value = todoUIState.title,
                  onValueChange = { editTodoViewModel.onTitleChanged(it) },
                  label = { Text("Title") },
                  placeholder = { Text("Task Title") },
                  isError = todoUIState.invalidTitleMsg != null,
                  supportingText = {
                    todoUIState.invalidTitleMsg?.let {
                      Text(it, modifier = Modifier.testTag(EditToDoScreenTestTags.ERROR_MESSAGE))
                    }
                  },
                  modifier =
                      Modifier.fillMaxWidth().testTag(EditToDoScreenTestTags.INPUT_TODO_TITLE))

              // Description Input
              OutlinedTextField(
                  value = todoUIState.description,
                  onValueChange = { editTodoViewModel.onDescriptionChanged(it) },
                  label = { Text("Description") },
                  placeholder = { Text("Describe the task") },
                  isError = todoUIState.invalidDescriptionMsg != null,
                  supportingText = {
                    todoUIState.invalidDescriptionMsg?.let {
                      Text(it, modifier = Modifier.testTag(EditToDoScreenTestTags.ERROR_MESSAGE))
                    }
                  },
                  modifier =
                      Modifier.fillMaxWidth()
                          .height(100.dp)
                          .testTag(EditToDoScreenTestTags.INPUT_TODO_DESCRIPTION))

              // Assignee Input
              OutlinedTextField(
                  value = todoUIState.assignee,
                  onValueChange = { editTodoViewModel.onAssigneeChanged(it) },
                  label = { Text("Assignee") },
                  placeholder = { Text("Assign a person") },
                  isError = todoUIState.invalidAssigneeNameMsg != null,
                  supportingText = {
                    todoUIState.invalidAssigneeNameMsg?.let {
                      Text(it, modifier = Modifier.testTag(EditToDoScreenTestTags.ERROR_MESSAGE))
                    }
                  },
                  modifier =
                      Modifier.fillMaxWidth().testTag(EditToDoScreenTestTags.INPUT_TODO_ASSIGNEE))

              // Placeholder Location Input
              OutlinedTextField(
                  value = todoUIState.location,
                  onValueChange = { editTodoViewModel.onLocationChanged(it) },
                  label = { Text("Location") },
                  placeholder = { Text("Enter an Address or Location") },
                  modifier =
                      Modifier.fillMaxWidth().testTag(EditToDoScreenTestTags.INPUT_TODO_LOCATION),
              )

              // Due Date Input
              OutlinedTextField(
                  value = todoUIState.dueDate,
                  onValueChange = { editTodoViewModel.onDateChanged(it) },
                  label = { Text("Due date") },
                  placeholder = { Text("10/10/2025") },
                  isError = todoUIState.invalidDueDateMsg != null,
                  supportingText = {
                    todoUIState.invalidDueDateMsg?.let {
                      Text(it, modifier = Modifier.testTag(EditToDoScreenTestTags.ERROR_MESSAGE))
                    }
                  },
                  modifier =
                      Modifier.fillMaxWidth().testTag(EditToDoScreenTestTags.INPUT_TODO_DATE))

              Spacer(modifier = Modifier.height(8.dp))

              // Save Button
              Button(
                  onClick = {
                    editTodoViewModel.editTodo(todoUid)
                    onEdit()
                  },
                  modifier = Modifier.fillMaxWidth().testTag(EditToDoScreenTestTags.TODO_SAVE),
                  colors =
                      ButtonDefaults.buttonColors(
                          containerColor = MaterialTheme.colorScheme.secondary),
                  enabled = todoUIState.isValid,
              ) {
                Text("Save", color = MaterialTheme.colorScheme.primary)
              }

              Spacer(modifier = Modifier.height(4.dp))

              // Delete Button
              TextButton(
                  onClick = {
                    editTodoViewModel.deleteToDo(todoUid)
                    onEdit()
                  },
                  modifier = Modifier.fillMaxWidth().testTag(EditToDoScreenTestTags.TODO_DELETE),
                  // TextButton has no background by default
                  colors =
                      ButtonDefaults.textButtonColors(
                          contentColor = MaterialTheme.colorScheme.tertiary)) {
                    Row {
                      Icon(
                          imageVector = Icons.Filled.DeleteForever,
                          contentDescription = "Delete",
                          tint = MaterialTheme.colorScheme.tertiary)
                      Text(
                          text = "Delete",
                          color = MaterialTheme.colorScheme.tertiary,
                          modifier = Modifier.padding(start = 4.dp))
                    }
                  }
            }
      })
}
