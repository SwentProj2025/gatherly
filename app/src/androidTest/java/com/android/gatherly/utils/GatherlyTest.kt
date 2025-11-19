package com.android.gatherly.utils

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.android.gatherly.model.map.Location
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todo.ToDosLocalRepository
import com.android.gatherly.model.todo.ToDosRepository
import com.android.gatherly.ui.todo.AddToDoScreenTestTags
import com.android.gatherly.ui.todo.EditToDoScreenTestTags
import com.android.gatherly.ui.todo.OverviewScreenTestTags
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest

// Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the
// SwEnt staff.

const val UI_WAIT_TIMEOUT = 100_000L

/** Base class for Gatherly tests, providing common setup and utility functions. */
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

  fun ComposeTestRule.enterEditTodoTitle(title: String) {
    onNodeWithTag(EditToDoScreenTestTags.INPUT_TODO_TITLE).performTextClearance()
    onNodeWithTag(EditToDoScreenTestTags.INPUT_TODO_TITLE).performTextInput(title)
  }

  fun ComposeTestRule.enterEditTodoDescription(description: String) {
    onNodeWithTag(EditToDoScreenTestTags.INPUT_TODO_DESCRIPTION).performTextClearance()
    onNodeWithTag(EditToDoScreenTestTags.INPUT_TODO_DESCRIPTION).performTextInput(description)
  }

  fun ComposeTestRule.enterEditTodoDate(date: String) {
    onNodeWithTag(EditToDoScreenTestTags.INPUT_TODO_DATE).performTextClearance()
    onNodeWithTag(EditToDoScreenTestTags.INPUT_TODO_DATE).performTextInput(date)
  }

  fun ComposeTestRule.enterEditTodoTime(time: String) {
    onNodeWithTag(EditToDoScreenTestTags.INPUT_TODO_TIME).performTextClearance()
    onNodeWithTag(EditToDoScreenTestTags.INPUT_TODO_TIME).performTextInput(time)
  }

  fun ComposeTestRule.enterEditTodoLocation(location: String) {
    onNodeWithTag(EditToDoScreenTestTags.INPUT_TODO_LOCATION).performTextClearance()
    onNodeWithTag(EditToDoScreenTestTags.INPUT_TODO_LOCATION).performTextInput(location)
  }

  fun ComposeTestRule.checkErrorMessageIsDisplayedForEditTodo() =
      onNodeWithTag(EditToDoScreenTestTags.ERROR_MESSAGE, useUnmergedTree = true)
          .assertIsDisplayed()

  fun ComposeTestRule.enterAddTodoTitle(title: String) =
      onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_TITLE).performTextInput(title)

  fun ComposeTestRule.enterAddTodoDescription(description: String) =
      onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_DESCRIPTION).performTextInput(description)

  fun ComposeTestRule.enterAddTodoDate(date: String) =
      onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_DATE).performTextInput(date)

  fun ComposeTestRule.enterAddTodoTime(time: String) =
      onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_TIME).performTextInput(time)

  fun ComposeTestRule.enterAddTodoLocation(location: String) =
      onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_LOCATION).performTextInput(location)

  fun ComposeTestRule.enterAddTodoDetails(todo: ToDo, date: String = todo.dueDate.toDateString()) {
    enterAddTodoTitle(todo.name)
    enterAddTodoDescription(todo.description)
    enterAddTodoDate(date)
    enterAddTodoLocation(todo.location?.name ?: "Any")
  }

  fun ComposeTestRule.clickOnSaveForAddTodo(waitForRedirection: Boolean = false) {
    onNodeWithTag(AddToDoScreenTestTags.TODO_SAVE).assertExists().performClick()
    waitUntil(UI_WAIT_TIMEOUT) {
      !waitForRedirection ||
          onAllNodesWithTag(AddToDoScreenTestTags.TODO_SAVE).fetchSemanticsNodes().isEmpty()
    }
  }

  private fun ComposeTestRule.waitUntilTodoIsDisplayed(todo: ToDo): SemanticsNodeInteraction {
    waitUntil(UI_WAIT_TIMEOUT) {
      onAllNodesWithTag(OverviewScreenTestTags.getTestTagForTodoItem(todo))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    return checkTodoItemIsDisplayed(todo)
  }

  fun ComposeTestRule.clickOnTodoItem(todo: ToDo) {
    waitUntilTodoIsDisplayed(todo).performClick()
  }

  fun ComposeTestRule.checkTodoItemIsDisplayed(todo: ToDo): SemanticsNodeInteraction =
      onNodeWithTag(OverviewScreenTestTags.getTestTagForTodoItem(todo)).assertIsDisplayed()

  fun ComposeTestRule.checkOverviewScreenIsNotDisplayed() {
    onNodeWithTag(OverviewScreenTestTags.TODO_LIST).assertDoesNotExist()
  }

  fun ComposeTestRule.onTodoItem(todo: ToDo, matcher: SemanticsMatcher) {
    onNode(
            hasTestTag(OverviewScreenTestTags.getTestTagForTodoItem(todo))
                .and(hasAnyDescendant(matcher)),
            useUnmergedTree = true)
        .assertIsDisplayed()
  }

  fun ComposeTestRule.checkErrorMessageIsDisplayedForAddTodo() =
      onNodeWithTag(AddToDoScreenTestTags.ERROR_MESSAGE, useUnmergedTree = true).assertIsDisplayed()

  fun checkNoTodoWereAdded(action: () -> Unit) {
    val numberOfTodos = runBlocking { repository.getAllTodos().size }
    action()
    runTest { assertEquals(numberOfTodos, repository.getAllTodos().size) }
  }

  fun <A : ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<A>, A>
      .checkActivityStateOnPressBack(shouldFinish: Boolean) {
    activityRule.scenario.onActivity { activity ->
      activity.onBackPressedDispatcher.onBackPressed()
    }
    waitUntil { activity.isFinishing == shouldFinish }
    assertEquals(shouldFinish, activity.isFinishing)
  }

  fun ToDo.Equals(other: ToDo): Boolean =
      name == other.name &&
          description == other.description &&
          dueDate.toDateString() == other.dueDate.toDateString() &&
          status == other.status

  fun ToDosRepository.getTodoByName(name: String): ToDo = runBlocking {
    getAllTodos().first { it.name == name }
  }

  companion object {
    fun Timestamp.toDateString(): String {
      val date = this.toDate()
      val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT)
      return dateFormat.format(date)
    }

    fun Timestamp.Companion.fromDate(year: Int, month: Int, day: Int): Timestamp {
      val calendar = Calendar.getInstance()
      calendar.set(year, month, day, 0, 0, 0)
      return Timestamp(calendar.time)
    }
  }
}
