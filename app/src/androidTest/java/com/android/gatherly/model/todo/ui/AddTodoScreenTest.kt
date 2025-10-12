package com.android.gatherly.ui.todo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.gatherly.utils.InMemoryGatherlyTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the
// SwEnt staff.

class AddTodoScreenTest : InMemoryGatherlyTest() {
  @get:Rule val composeTestRule = createComposeRule()

  @Before
  override fun setUp() {
    super.setUp()
    composeTestRule.setContent { AddToDoScreen() }
  }

  @Test
  fun displayAllComponents() {
    composeTestRule
        .onNodeWithTag(AddToDoScreenTestTags.TODO_SAVE)
        .assertTextContains("Save", substring = true, ignoreCase = true)
    composeTestRule.onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_DESCRIPTION).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_ASSIGNEE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_LOCATION).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_DATE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_TIME).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(AddToDoScreenTestTags.ERROR_MESSAGE, useUnmergedTree = true)
        .assertIsNotDisplayed()
  }

  @Test
  fun canEnterTitle() {
    val text = "title"
    composeTestRule.enterAddTodoTitle(text)
    composeTestRule.onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_TITLE).assertTextContains(text)
    composeTestRule
        .onNodeWithTag(AddToDoScreenTestTags.ERROR_MESSAGE, useUnmergedTree = true)
        .assertIsNotDisplayed()
  }

  @Test
  fun canEnterDescription() {
    val text = "description"
    composeTestRule.enterAddTodoDescription(text)
    composeTestRule
        .onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_DESCRIPTION)
        .assertTextContains(text)
    composeTestRule
        .onNodeWithTag(AddToDoScreenTestTags.ERROR_MESSAGE, useUnmergedTree = true)
        .assertIsNotDisplayed()
  }

  @Test
  fun canEnterAssigneeName() {
    val text = "assignee"
    composeTestRule.enterAddTodoAssignee(text)
    composeTestRule
        .onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_ASSIGNEE)
        .assertTextContains(text)
    composeTestRule
        .onNodeWithTag(AddToDoScreenTestTags.ERROR_MESSAGE, useUnmergedTree = true)
        .assertIsNotDisplayed()
  }

  @Test
  fun canEnterLocation() {
    val text = "location"
    composeTestRule.enterAddTodoLocation(text)
    composeTestRule
        .onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_LOCATION)
        .assertTextContains(text)
    composeTestRule
        .onNodeWithTag(AddToDoScreenTestTags.ERROR_MESSAGE, useUnmergedTree = true)
        .assertIsNotDisplayed()
  }

  @Test
  fun canEnterAValidDate() {
    val text = "31/02/2023"
    composeTestRule.enterAddTodoDate(text)
    composeTestRule.onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_DATE).assertTextContains(text)
  }

  @Test
  fun canEnterAnInvalidDate() {
    val text = "This date is not valid"
    composeTestRule.enterAddTodoDate(text)
    composeTestRule.onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_DATE).assertTextContains(text)
  }

  @Test
  fun canEnterAValidTime() {
    val text = "14:00"
    composeTestRule.enterAddTodoTime(text)
    composeTestRule.onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_TIME).assertTextContains(text)
  }

  @Test
  fun canEnterAnInvalidTime() {
    val text = "This time is not valid"
    composeTestRule.enterAddTodoTime(text)
    composeTestRule.onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_TIME).assertTextContains(text)
  }

  @Test
  fun savingWithInvalidTitleShouldDoNothing() = checkNoTodoWereAdded {
    composeTestRule.enterAddTodoDetails(todo = todo1.copy(name = " "))
    composeTestRule.clickOnSaveForAddTodo()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(AddToDoScreenTestTags.TODO_SAVE).assertExists()
  }

  @Test
  fun savingWithInvalidDescriptionShouldDoNothing() = checkNoTodoWereAdded {
    composeTestRule.enterAddTodoDetails(todo = todo1.copy(description = " "))
    composeTestRule.clickOnSaveForAddTodo()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(AddToDoScreenTestTags.TODO_SAVE).assertExists()
  }

  @Test
  fun savingWithInvalidAssigneeShouldDoNothing() = checkNoTodoWereAdded {
    composeTestRule.enterAddTodoDetails(todo = todo1.copy(assigneeName = " "))
    composeTestRule.clickOnSaveForAddTodo()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(AddToDoScreenTestTags.TODO_SAVE).assertExists()
  }

  @Test
  fun savingWithInvalidDateShouldDoNothing() = checkNoTodoWereAdded {
    composeTestRule.enterAddTodoDetails(
        todo = todo1, date = "This is not a date" // Invalid date format
        )
    composeTestRule.clickOnSaveForAddTodo()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(AddToDoScreenTestTags.TODO_SAVE).assertExists()
  }

  @Test
  fun enteringEmptyTitleShowsErrorMessage() {
    val invalidTitle = " "
    composeTestRule.enterAddTodoTitle(invalidTitle)
    composeTestRule.checkErrorMessageIsDisplayedForAddTodo()
  }

  @Test
  fun enteringEmptyDescriptionShowsErrorMessage() {
    val invalidDescription = " "
    composeTestRule.enterAddTodoDescription(invalidDescription)
    composeTestRule.checkErrorMessageIsDisplayedForAddTodo()
  }

  @Test
  fun enteringEmptyAssigneeNameShowsErrorMessage() {
    val invalidAssigneeName = " "
    composeTestRule.enterAddTodoAssignee(invalidAssigneeName)
    composeTestRule.checkErrorMessageIsDisplayedForAddTodo()
  }

  @Test
  fun enteringInvalidDateShowsErrorMessage() {
    val invalidDate = "This is not a date" // Invalid date format
    composeTestRule.enterAddTodoDate(invalidDate)
    composeTestRule.checkErrorMessageIsDisplayedForAddTodo()
  }

  @Test
  fun enteringInvalidTimeShowsErrorMessage() {
    val invalidTime = "This is not a time" // Invalid time format
    composeTestRule.enterAddTodoTime(invalidTime)
    composeTestRule.checkErrorMessageIsDisplayedForAddTodo()
  }
}
