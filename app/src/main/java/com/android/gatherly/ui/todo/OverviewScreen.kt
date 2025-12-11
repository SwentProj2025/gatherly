package com.android.gatherly.ui.todo

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.R
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoPriority
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todoCategory.ToDoCategory
import com.android.gatherly.ui.navigation.BottomNavigationMenu
import com.android.gatherly.ui.navigation.NavigationActions
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu
import com.android.gatherly.utils.priorityLevelColor
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

  /** Test tag for the search bar used to search for todos */
  const val SEARCH_BAR = "searchBar"

  /** Test tag for the sort menu button */
  const val SORT_MENU_BUTTON = "sortMenuButton"

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

  /**
   * Returns a unique test tag for the button representing a given [ToDoCategory] item.
   *
   * @param category The [ToDoCategory] item whose test tag will be generated.
   * @return A string uniquely identifying the ToDo item in the UI.
   */
  fun getTestTagForTagButton(category: ToDoCategory): String = "tagItem${category.id}"

  const val ALL_TAG_BUTTON = "allTagButton"
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
    navigationActions: NavigationActions? = null
) {

  val uiState by overviewViewModel.uiState.collectAsState()
  val todos = uiState.todos
  val focusManager: FocusManager = LocalFocusManager.current

  val selectedTagFilter = remember { mutableStateOf<ToDoCategory?>(null) }
  val categoriesList by overviewViewModel.categories.collectAsStateWithLifecycle()

  // Fetch todos when the screen is recomposed
  LaunchedEffect(Unit) { overviewViewModel.refreshUIState() }

  val ongoingTodos = todos.filter { it.status == ToDoStatus.ONGOING }
  val completedTodos = todos.filter { it.status == ToDoStatus.ENDED }

  Scaffold(
      topBar = {
        TopNavigationMenu(
            selectedTab = Tab.Overview,
            onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
            modifier = Modifier.testTag(NavigationTestTags.TOP_NAVIGATION_MENU))
      },
      bottomBar = {
        BottomNavigationMenu(
            selectedTab = Tab.Overview,
            onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
            modifier = Modifier.testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU))
      },
      floatingActionButton = {
        FloatingActionButton(
            onClick = { onAddTodo() },
            modifier = Modifier.testTag(OverviewScreenTestTags.CREATE_TODO_BUTTON),
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary) {
              Icon(
                  imageVector = Icons.Default.Add,
                  contentDescription = stringResource(R.string.todos_add_button_text))
            }
      },
      content = { pd ->
        if (uiState.isLoading) {
          Box(contentAlignment = Alignment.Center) {
            Text(modifier = Modifier.padding(pd), text = stringResource(R.string.todos_loading))
          }
        } else {
          Box(
              modifier =
                  Modifier.fillMaxSize().clickable(
                      interactionSource = remember { MutableInteractionSource() },
                      indication = null) {
                        focusManager.clearFocus()
                      }) {
                var searchQuery by remember { mutableStateOf("") }

                Column(modifier = Modifier.padding(pd)) {
                  Row(
                      modifier =
                          Modifier.fillMaxWidth()
                              .height(dimensionResource(R.dimen.todo_overview_top_row_height))
                              .padding(
                                  bottom =
                                      dimensionResource(R.dimen.todos_overview_vertical_padding))) {

                        // -- Search Bar --
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { newText ->
                              searchQuery = newText
                              overviewViewModel.searchTodos(newText)
                            },
                            modifier =
                                Modifier.weight(1f)
                                    .padding(
                                        horizontal =
                                            dimensionResource(
                                                R.dimen.todos_overview_horizontal_padding))
                                    .testTag(OverviewScreenTestTags.SEARCH_BAR),
                            label = { Text(stringResource(R.string.todos_search_bar_label)) },
                            singleLine = true,
                            colors =
                                OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.background,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                ))
                        SortMenu(
                            currentOrder = uiState.sortOrder,
                            onSortSelected = { overviewViewModel.setSortOrder(it) })
                      }

                  // -- Tag filter bar --
                  FilterTagBar(selectedTagFilter, categoriesList, overviewViewModel)

                  if (todos.isNotEmpty()) {
                    LazyColumn(
                        contentPadding =
                            PaddingValues(
                                vertical =
                                    dimensionResource(
                                        id = R.dimen.todos_overview_vertical_padding)),
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(
                                    horizontal =
                                        dimensionResource(
                                            id = R.dimen.todos_overview_horizontal_padding))
                                .testTag(OverviewScreenTestTags.TODO_LIST)) {

                          // ONGOING SECTION
                          if (ongoingTodos.isNotEmpty()) {
                            item {
                              Text(
                                  text = stringResource(R.string.todos_ongoing_section_label),
                                  style = MaterialTheme.typography.titleMedium,
                                  fontWeight = FontWeight.Bold,
                                  modifier =
                                      Modifier.padding(
                                          vertical =
                                              dimensionResource(
                                                  id =
                                                      R.dimen
                                                          .todos_overview_section_text_vertical_padding)))
                            }
                            items(ongoingTodos.size) { index ->
                              ToDoItem(
                                  todo = ongoingTodos[index],
                                  onClick = { onSelectTodo(ongoingTodos[index]) },
                                  isChecked = false,
                                  onCheckedChange = { checked ->
                                    val newStatus =
                                        if (checked) ToDoStatus.ENDED else ToDoStatus.ONGOING
                                    overviewViewModel.onCheckboxChanged(
                                        ongoingTodos[index].uid, newStatus)
                                  })
                            }
                          }

                          // COMPLETED SECTION
                          if (completedTodos.isNotEmpty()) {
                            item {
                              Text(
                                  text = stringResource(R.string.todos_completed_section_label),
                                  style = MaterialTheme.typography.titleMedium,
                                  fontWeight = FontWeight.Bold,
                                  modifier =
                                      Modifier.padding(
                                          vertical =
                                              dimensionResource(
                                                  id =
                                                      R.dimen
                                                          .todos_overview_section_text_vertical_padding)))
                            }
                            items(completedTodos.size) { index ->
                              ToDoItem(
                                  todo = completedTodos[index],
                                  onClick = { onSelectTodo(completedTodos[index]) },
                                  isChecked = true,
                                  onCheckedChange = { checked ->
                                    val newStatus =
                                        if (checked) ToDoStatus.ENDED else ToDoStatus.ONGOING
                                    overviewViewModel.onCheckboxChanged(
                                        completedTodos[index].uid, newStatus)
                                  })
                            }
                          }
                        }
                  } else {
                    Text(
                        modifier =
                            Modifier.padding(pd)
                                .testTag(OverviewScreenTestTags.EMPTY_TODO_LIST_MSG),
                        text = stringResource(R.string.no_todos_text))
                  }
                }
              }
        }
      })
}

/**
 * Displays a button that opens a dropdown menu allowing the user to choose a sorting order for the
 * ToDo list.
 *
 * When the icon button is tapped, a dropdown menu expands with three sorting options:
 * - Date descending
 * - Date ascending
 * - Alphabetical
 *
 * @param onSortSelected Callback invoked when the user selects a new [TodoSortOrder].
 */
@Composable
fun SortMenu(currentOrder: TodoSortOrder, onSortSelected: (TodoSortOrder) -> Unit) {
  var expanded by remember { mutableStateOf(false) }

  Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxHeight()) {
    IconButton(
        modifier = Modifier.fillMaxHeight().testTag(OverviewScreenTestTags.SORT_MENU_BUTTON),
        onClick = { expanded = true },
    ) {
      Icon(
          imageVector = Icons.Filled.FilterList,
          modifier =
              Modifier.size(dimensionResource(R.dimen.todo_overview_sort_icon_size)).fillMaxSize(),
          contentDescription = stringResource(R.string.todos_sort_button_label),
          tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        containerColor = MaterialTheme.colorScheme.surfaceVariant) {
          DropdownMenuItem(
              text = {
                Text(
                    text = stringResource(R.string.todos_date_descending_sort_button_text),
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
              },
              onClick = {
                onSortSelected(TodoSortOrder.DATE_DESC)
                expanded = false
              },
              trailingIcon = {
                if (currentOrder == TodoSortOrder.DATE_DESC) {
                  Icon(
                      Icons.Default.Check,
                      contentDescription =
                          stringResource(R.string.todos_sort_menu_check_icon_label))
                }
              })
          DropdownMenuItem(
              text = {
                Text(
                    text = stringResource(R.string.todos_date_ascending_sort_button_text),
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
              },
              onClick = {
                onSortSelected(TodoSortOrder.DATE_ASC)
                expanded = false
              },
              trailingIcon = {
                if (currentOrder == TodoSortOrder.DATE_ASC) {
                  Icon(
                      Icons.Default.Check,
                      contentDescription =
                          stringResource(R.string.todos_sort_menu_check_icon_label))
                }
              })
          DropdownMenuItem(
              text = {
                Text(
                    text = stringResource(R.string.todos_alphabetical_sort_button_text),
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
              },
              onClick = {
                onSortSelected(TodoSortOrder.ALPHABETICAL)
                expanded = false
              },
              trailingIcon = {
                if (currentOrder == TodoSortOrder.ALPHABETICAL) {
                  Icon(
                      Icons.Default.Check,
                      contentDescription =
                          stringResource(R.string.todos_sort_menu_check_icon_label))
                }
              })

          DropdownMenuItem(
              text = {
                Text(
                    text = stringResource(R.string.todos_priority_sort_button_text),
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
              },
              onClick = {
                onSortSelected(TodoSortOrder.PRIORITY_LEVEL)
                expanded = false
              },
              trailingIcon = {
                if (currentOrder == TodoSortOrder.PRIORITY_LEVEL) {
                  Icon(
                      Icons.Default.Check,
                      contentDescription =
                          stringResource(R.string.todos_sort_menu_check_icon_label))
                }
              })
        }
  }
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
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant,
              contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
      modifier =
          Modifier.clickable(onClick = onClick)
              .testTag(OverviewScreenTestTags.getTestTagForTodoItem(todo))
              .fillMaxWidth()
              .padding(vertical = dimensionResource(id = R.dimen.todo_item_vertical_padding))) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(dimensionResource(id = R.dimen.todo_item_row_padding)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Checkbox(
              checked = isChecked,
              onCheckedChange = onCheckedChange,
              modifier =
                  Modifier.padding(end = dimensionResource(id = R.dimen.todo_item_checkbox_padding))
                      .testTag(OverviewScreenTestTags.getCheckboxTagForTodoItem(todo)))

          Column(
              modifier =
                  Modifier.weight(
                      integerResource(id = R.integer.todo_item_column_weight).toFloat())) {
                Row {
                  Text(
                      text = todo.name,
                      style = MaterialTheme.typography.bodyLarge,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      fontWeight = FontWeight.Medium)

                  Spacer(modifier = Modifier.width(10.dp))

                  if (todo.tag != null) {
                    Icon(
                        imageVector = Icons.Filled.Folder,
                        modifier = Modifier.size(25.dp).fillMaxSize(),
                        contentDescription = "Category icon",
                        tint = todo.tag.color,
                    )
                  }
                }

                if (todo.dueDate != null) {
                  Text(
                      text =
                          todo.dueDate.let {
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                .format(todo.dueDate.toDate())
                          } ?: "",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }

          if (todo.priorityLevel != ToDoPriority.NONE) {
            Icon(
                imageVector = Icons.Filled.Error,
                modifier = Modifier.size(30.dp).fillMaxSize(),
                contentDescription = "Priority level icon",
                tint = priorityLevelColor(todo.priorityLevel))
          }
        }
      }
}

/** Displays a filter bar with buttons to filter events by their status. */
@Composable
private fun FilterTagBar(
    selectedTagFilter: MutableState<ToDoCategory?>,
    categoriesList: List<ToDoCategory>,
    overviewViewModel: OverviewViewModel
) {
  LazyRow(
      modifier =
          Modifier.fillMaxWidth()
              .padding(vertical = dimensionResource(R.dimen.events_filter_bar_vertical_size)),
      horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
          FilterTagButton(
              "All",
              null,
              selectedTagFilter,
              overviewViewModel,
              Modifier.testTag(OverviewScreenTestTags.ALL_TAG_BUTTON))
        }

        items(categoriesList.size) { index ->
          val category = categoriesList[index]
          FilterTagButton(
              category.name,
              category,
              selectedTagFilter,
              overviewViewModel,
              Modifier.testTag(OverviewScreenTestTags.getTestTagForTagButton(category)))
        }
      }
}

/**
 * A button used in the filter bar to select an event filter.
 *
 * @param label The text label for the button.
 * @param category The [ToDoCategory] associated with this button.
 * @param selectedTagFilter The currently selected [ToDoCategory].
 */
@Composable
private fun FilterTagButton(
    label: String,
    category: ToDoCategory?,
    selectedTagFilter: MutableState<ToDoCategory?>,
    overviewViewModel: OverviewViewModel,
    modifier: Modifier
) {
  val isSelected = selectedTagFilter.value == category

  Button(
      onClick = {
        selectedTagFilter.value = category
        overviewViewModel.setCategoryFilter(category)
      },
      colors =
          buttonColors(
              containerColor =
                  if (isSelected) category?.color ?: MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.surfaceVariant,
              contentColor =
                  if (isSelected) MaterialTheme.colorScheme.onPrimary
                  else MaterialTheme.colorScheme.background),
      shape = RoundedCornerShape(dimensionResource(R.dimen.rounded_corner_shape_large)),
      modifier = modifier.height(dimensionResource(R.dimen.events_filter_button_height))) {
        Text(text = label)
      }
}
