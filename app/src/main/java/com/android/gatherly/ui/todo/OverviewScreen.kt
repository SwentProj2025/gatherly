package com.android.gatherly.ui.todo

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import java.util.Locale

// Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the
// SwEnt staff.

/**
 * Contains test tags used in [OverviewScreen] and its child composables for identifying UI elements
 * during Compose UI testing.
 */
object OverviewScreenTestTags {
  /** Test tag for the FloatingActionButton used to create a new ToDo item. */
  const val CREATE_TODO_BUTTON = "createTodoFab"

  /** Test tag for the message displayed when there are no ToDo items. */
  const val EMPTY_TODO_LIST_MSG = "emptyTodoList"

  /** Test tag for the LazyColumn that displays the list of ToDo items. */
  const val TODO_LIST = "todoList"

  /**
   * Returns a unique test tag for the checkbox associated with a given [ToDo] item.
   *
   * @param todo The [ToDo] item whose checkbox tag will be generated.
   * @return A string uniquely identifying the checkbox of the given ToDo.
   */
  fun getCheckboxTagForTodoItem(todo: ToDo): String = "checkbox${todo.uid}"

  /**
   * Returns a unique test tag for the card or container representing a given [ToDo] item.
   *
   * @param todo The [ToDo] item whose test tag will be generated.
   * @return A string uniquely identifying the ToDo item in the UI.
   */
  fun getTestTagForTodoItem(todo: ToDo): String = "todoItem${todo.uid}"
}

/**
 * Displays an overview of ToDo items, grouped by their status (ongoing or completed).
 *
 * @param overviewViewModel The [OverviewViewModel] that provides the current ToDo list and state.
 * @param onAddTodo A callback invoked when the user taps the Add button.
 * @param onSelectTodo A callback invoked when the user taps a specific ToDo item.
 * @param goHomePage A callback invoked when the user taps the home icon in the top app bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    overviewViewModel: OverviewViewModel = viewModel(),
    onAddTodo: () -> Unit = {},
    onSelectTodo: (ToDo) -> Unit = {},
    goHomePage: () -> Unit = {},
) {

  val uiState by overviewViewModel.uiState.collectAsState()
  val todos = uiState.todos

  // Fetch todos when the screen is recomposed
  LaunchedEffect(Unit) { overviewViewModel.refreshUIState() }

  val ongoingTodos = todos.filter { it.status == ToDoStatus.ONGOING }
  val completedTodos = todos.filter { it.status == ToDoStatus.ENDED }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Todo List") },
            navigationIcon = {
              IconButton(onClick = { goHomePage() }) {
                Icon(imageVector = Icons.Filled.Home, contentDescription = "Home")
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
      floatingActionButton = {
        FloatingActionButton(
            onClick = { onAddTodo() },
            modifier = Modifier.testTag(OverviewScreenTestTags.CREATE_TODO_BUTTON),
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.primary) {
              Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
            }
      },
      content = { pd ->
        if (todos.isNotEmpty()) {
          LazyColumn(
              contentPadding = PaddingValues(vertical = 8.dp),
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(horizontal = 16.dp)
                      .padding(pd)
                      .testTag(OverviewScreenTestTags.TODO_LIST)) {

                // ONGOING SECTION
                if (ongoingTodos.isNotEmpty()) {
                  item {
                    Text(
                        text = "Ongoing",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp))
                  }
                  items(ongoingTodos.size) { index ->
                    ToDoItem(
                        todo = ongoingTodos[index],
                        onClick = { onSelectTodo(ongoingTodos[index]) },
                        isChecked = false,
                        onCheckedChange = { checked ->
                          val newStatus = if (checked) ToDoStatus.ENDED else ToDoStatus.ONGOING
                          overviewViewModel.onCheckboxChanged(ongoingTodos[index].uid, newStatus)
                        })
                  }
                }

                // COMPLETED SECTION
                if (completedTodos.isNotEmpty()) {
                  item {
                    Text(
                        text = "Completed",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp))
                  }
                  items(completedTodos.size) { index ->
                    ToDoItem(
                        todo = completedTodos[index],
                        onClick = { onSelectTodo(completedTodos[index]) },
                        isChecked = true,
                        onCheckedChange = { checked ->
                          val newStatus = if (checked) ToDoStatus.ENDED else ToDoStatus.ONGOING
                          overviewViewModel.onCheckboxChanged(completedTodos[index].uid, newStatus)
                        })
                  }
                }
              }
        } else {
          Text(
              modifier = Modifier.padding(pd).testTag(OverviewScreenTestTags.EMPTY_TODO_LIST_MSG),
              text = "You have no ToDo yet.")
        }
      })
}

// A portion of the code in the ToDoItem composable was generated by an LLM.

/**
 * Displays a single ToDo item inside a [Card] with a checkbox and basic details.
 *
 * @param todo The [ToDo] item to display.
 * @param onClick A callback invoked when the user taps the ToDo card.
 * @param isChecked Whether the checkbox should appear checked (true for completed tasks).
 * @param onCheckedChange A callback invoked when the user clicks on the checkbox state, to mark the
 *   Todo as completed or ongoing.
 */
@Composable
fun ToDoItem(
    todo: ToDo,
    onClick: () -> Unit,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
  Card(
      modifier =
          Modifier.clickable(onClick = onClick)
              .testTag(OverviewScreenTestTags.getTestTagForTodoItem(todo))
              .fillMaxWidth()
              .padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Checkbox(
              checked = isChecked,
              onCheckedChange = onCheckedChange,
              modifier =
                  Modifier.padding(end = 8.dp)
                      .testTag(OverviewScreenTestTags.getCheckboxTagForTodoItem(todo)))

          Column(modifier = Modifier.weight(1f)) {
            Text(
                text = todo.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium)
            Text(
                text =
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(todo.dueDate.toDate()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
          }
        }
      }
}
