package com.android.gatherly.utils

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.android.gatherly.model.map.Location
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todo.ToDosLocalRepository
import com.android.gatherly.model.todo.ToDosRepository
import com.android.gatherly.ui.todo.AddToDoScreenTestTags
import com.android.gatherly.ui.todo.EditToDoScreenTestTags
import com.android.gatherly.ui.todo.LocationSuggestionsTestTags
import com.android.gatherly.ui.todo.OverviewScreenTestTags
import com.google.firebase.Timestamp
import java.util.Calendar
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest

// Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the
// SwEnt staff.

const val UI_WAIT_TIMEOUT = 100_000L

/**
 * Base class for Gatherly tests, providing common setup and utility functions.
 *
 * It includes predefined [ToDo] items and helper methods for interacting with the UI during tests.
 */
abstract class GatherlyTest() {

  var repository: ToDosRepository = ToDosLocalRepository()

  open val todo1 =
      ToDo(
          uid = "0",
          name = "Buy groceries",
          description = "Milk, eggs, bread, and butter",
          dueDate = Timestamp.Companion.fromDate(2025, Calendar.SEPTEMBER, 1),
          dueTime = Timestamp.now(),
          location = Location(46.5191, 6.5668, "Lausanne Coop"),
          status = ToDoStatus.ONGOING,
          ownerId = "user")

  open val todo2 =
      ToDo(
          uid = "1",
          name = "Walk the dog",
          description = "Take Fido for a walk in the park",
          dueDate = Timestamp.Companion.fromDate(2025, Calendar.OCTOBER, 15),
          location = Location(46.5210, 6.5790, "Parc de Mon Repos"),
          dueTime = Timestamp.now(),
          status = ToDoStatus.ONGOING,
          ownerId = "user")

  open val todo3 =
      ToDo(
          uid = "2",
          name = "Read a book",
          description = "Finish reading 'Clean Code'",
          dueDate = Timestamp.Companion.fromDate(2025, Calendar.NOVEMBER, 10),
          location = Location(46.5200, 6.5800, "City Library"),
          dueTime = Timestamp.now(),
          status = ToDoStatus.ENDED,
          ownerId = "user")

  /** Enters the title of a [ToDo] into the EditTodo screen fields. */
  fun ComposeTestRule.enterEditTodoTitle(title: String) {
    onNodeWithTag(EditToDoScreenTestTags.INPUT_TODO_TITLE).performTextClearance()
    onNodeWithTag(EditToDoScreenTestTags.INPUT_TODO_TITLE).performTextInput(title)
  }

  /** Enters the description of a [ToDo] into the EditTodo screen fields. */
  fun ComposeTestRule.enterEditTodoDescription(description: String) {
    onNodeWithTag(EditToDoScreenTestTags.INPUT_TODO_DESCRIPTION).performTextClearance()
    onNodeWithTag(EditToDoScreenTestTags.INPUT_TODO_DESCRIPTION).performTextInput(description)
  }

  /** Enters the time of a [ToDo] into the EditTodo screen fields. */
  fun ComposeTestRule.enterEditTodoTime(time: String) {
    onNodeWithTag(EditToDoScreenTestTags.INPUT_TODO_TIME).performTextClearance()
    onNodeWithTag(EditToDoScreenTestTags.INPUT_TODO_TIME).performTextInput(time)
  }

  /** Enters the location of a [ToDo] into the EditTodo screen fields. */
  fun ComposeTestRule.enterEditTodoLocation(location: String) {
    onNodeWithTag(LocationSuggestionsTestTags.INPUT).performTextClearance()
    onNodeWithTag(LocationSuggestionsTestTags.INPUT).performTextInput(location)
  }

  /** Clicks the Save button on the EditTodo screen. */
  fun ComposeTestRule.checkErrorMessageIsDisplayedForEditTodo() =
      onNodeWithTag(EditToDoScreenTestTags.ERROR_MESSAGE, useUnmergedTree = true)
          .assertIsDisplayed()

  /** Enters the title of a [ToDo] into the AddTodo screen fields. */
  fun ComposeTestRule.enterAddTodoTitle(title: String) =
      onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_TITLE).performTextInput(title)

  /** Enters the description of a [ToDo] into the AddTodo screen fields. */
  fun ComposeTestRule.enterAddTodoDescription(description: String) =
      onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_DESCRIPTION).performTextInput(description)

  /** Enters the date of a [ToDo] into the AddTodo screen fields. */
  fun ComposeTestRule.enterAddTodoDate(date: String) = {
    openDatePicker(AddToDoScreenTestTags.INPUT_TODO_DATE)
    val parts = date.split("/")
    if (parts.size == 3) {
      val day = parts[0].toInt()
      val month = parts[1].toInt()
      val year = parts[2].toInt()
      selectDateFromPicker(day, month, year)
    }
  }

  /** Enters the time of a [ToDo] into the AddTodo screen fields. */
  fun ComposeTestRule.enterAddTodoTime(time: String) =
      onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_TIME).performTextInput(time)

  /** Enters the location of a [ToDo] into the AddTodo screen fields. */
  fun ComposeTestRule.enterAddTodoLocation(location: String) =
      onNodeWithTag(LocationSuggestionsTestTags.INPUT).performTextInput(location)

  /** Enters all details of a [ToDo] into the AddTodo screen fields. */
  fun ComposeTestRule.enterAddTodoDetails(todo: ToDo) {
    enterAddTodoTitle(todo.name)
    enterAddTodoDescription(todo.description)
    enterAddTodoLocation(todo.location?.name ?: "Any")
  }

  /** Clicks the Save button on the AddTodo screen. */
  fun ComposeTestRule.clickOnSaveForAddTodo(waitForRedirection: Boolean = false) {
    onNodeWithTag(AddToDoScreenTestTags.TODO_SAVE).assertExists().performClick()
    waitUntil(UI_WAIT_TIMEOUT) {
      !waitForRedirection ||
          onAllNodesWithTag(AddToDoScreenTestTags.TODO_SAVE).fetchSemanticsNodes().isEmpty()
    }
  }

  /** Asserts that a [ToDo] item is displayed in the [ToDo] overview screen. */
  fun ComposeTestRule.onTodoItem(todo: ToDo, matcher: SemanticsMatcher) {
    onNode(
            hasTestTag(OverviewScreenTestTags.getTestTagForTodoItem(todo))
                .and(hasAnyDescendant(matcher)),
            useUnmergedTree = true)
        .assertIsDisplayed()
  }

  /** Utility function to check that an error message is displayed on the Add ToDo screen. */
  fun ComposeTestRule.checkErrorMessageIsDisplayedForAddTodo() =
      onNodeWithTag(AddToDoScreenTestTags.ERROR_MESSAGE, useUnmergedTree = true).assertIsDisplayed()

  /**
   * Utility function to check that no [ToDo] items were added during the execution of an action.
   */
  fun checkNoTodoWereAdded(action: () -> Unit) {
    val numberOfTodos = runBlocking { repository.getAllTodos().size }
    action()
    runTest { assertEquals(numberOfTodos, repository.getAllTodos().size) }
  }

  companion object {
    /** Helper function to create a Timestamp from year, month, and day. */
    fun Timestamp.Companion.fromDate(year: Int, month: Int, day: Int): Timestamp {
      val calendar = Calendar.getInstance()
      calendar.set(year, month, day, 0, 0, 0)
      return Timestamp(calendar.time)
    }
  }
}
