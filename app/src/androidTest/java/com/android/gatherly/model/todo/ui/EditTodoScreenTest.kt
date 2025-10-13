package com.android.gatherly.ui.todo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.utils.InMemoryGatherlyTest
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

// Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the
// SwEnt staff.

class EditTodoScreenTest : InMemoryGatherlyTest() {
  @get:Rule val composeTestRule = createComposeRule()

  val todo = todo1

  private fun withContent(
      editedTodo: ToDo = todo,
      todoList: List<ToDo> = listOf<ToDo>(editedTodo),
      block: (ToDo) -> Unit
  ) {
    runTest {
      for (todo in todoList) {
        repository.addTodo(todo)
      }
    }

    composeTestRule.setContent { EditToDoScreen(todoUid = editedTodo.uid) }
    block(editedTodo)
  }

  @Test
  fun displayAllComponents() = withContent {
    composeTestRule
        .onNodeWithTag(EditToDoScreenTestTags.TODO_SAVE)
        .assertIsDisplayed()
        .assertTextContains("Save", substring = true, ignoreCase = true)
    composeTestRule
        .onNodeWithTag(EditToDoScreenTestTags.TODO_DELETE)
        .assertIsDisplayed()
        .assertTextContains("Delete", substring = true, ignoreCase = true)

    composeTestRule.onNodeWithTag(EditToDoScreenTestTags.TODO_DELETE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EditToDoScreenTestTags.INPUT_TODO_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EditToDoScreenTestTags.INPUT_TODO_DESCRIPTION).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EditToDoScreenTestTags.INPUT_TODO_ASSIGNEE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EditToDoScreenTestTags.INPUT_TODO_LOCATION).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EditToDoScreenTestTags.INPUT_TODO_DATE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EditToDoScreenTestTags.ERROR_MESSAGE).assertIsNotDisplayed()
  }

  @Test
  fun canEnterTitle() = withContent {
    val text = "testTitle"
    composeTestRule.enterEditTodoTitle(text)
    composeTestRule.onNodeWithTag(EditToDoScreenTestTags.INPUT_TODO_TITLE).assertTextContains(text)
    composeTestRule
        .onNodeWithTag(EditToDoScreenTestTags.ERROR_MESSAGE, useUnmergedTree = true)
        .assertIsNotDisplayed()
  }

  @Test
  fun canEnterDescription() = withContent {
    val text = "testDescription"
    composeTestRule.enterEditTodoDescription(text)
    composeTestRule
        .onNodeWithTag(EditToDoScreenTestTags.INPUT_TODO_DESCRIPTION)
        .assertTextContains(text)
    composeTestRule
        .onNodeWithTag(EditToDoScreenTestTags.ERROR_MESSAGE, useUnmergedTree = true)
        .assertIsNotDisplayed()
  }

  @Test
  fun canEnterAssigneeName() = withContent {
    val text = "testAssignee"
    composeTestRule.enterEditTodoAssignee(text)
    composeTestRule
        .onNodeWithTag(EditToDoScreenTestTags.INPUT_TODO_ASSIGNEE)
        .assertTextContains(text)
    composeTestRule
        .onNodeWithTag(EditToDoScreenTestTags.ERROR_MESSAGE, useUnmergedTree = true)
        .assertIsNotDisplayed()
  }

  @Test
  fun canEnterLocation() = withContent {
    val text = "testLocation"
    composeTestRule.enterEditTodoLocation(text)
    composeTestRule
        .onNodeWithTag(EditToDoScreenTestTags.INPUT_TODO_LOCATION)
        .assertTextContains(text)
    composeTestRule
        .onNodeWithTag(EditToDoScreenTestTags.ERROR_MESSAGE, useUnmergedTree = true)
        .assertIsNotDisplayed()
  }

  @Test
  fun canEnterAValidDate() = withContent {
    val text = "10/02/2023"
    composeTestRule.enterEditTodoDate(text)
    composeTestRule.onNodeWithTag(EditToDoScreenTestTags.INPUT_TODO_DATE).assertTextContains(text)
  }

  @Test
  fun canEnterAnInvalidDate() = withContent {
    val invalidDate = "invalid date" // Invalid date format
    composeTestRule.enterEditTodoDate(invalidDate)
    composeTestRule.checkErrorMessageIsDisplayedForEditTodo()
  }

  @Test
  fun canEnterAValidTime() = withContent {
    val text = "14:30"
    composeTestRule.enterEditTodoTime(text)
    composeTestRule.onNodeWithTag(EditToDoScreenTestTags.INPUT_TODO_TIME).assertTextContains(text)
  }

  @Test
  fun canEnterAnInvalidTime() = withContent {
    val invalidTime = "invalid time" // Invalid time format
    composeTestRule.enterEditTodoTime(invalidTime)
    composeTestRule.checkErrorMessageIsDisplayedForEditTodo()
  }
}
