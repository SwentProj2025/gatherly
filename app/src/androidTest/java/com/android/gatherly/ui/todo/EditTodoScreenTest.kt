package com.android.gatherly.ui.todo

import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.gatherly.model.todo.ToDosLocalRepository
import com.android.gatherly.utils.GatherlyTest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class EditTodoScreenTest : GatherlyTest() {
  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var editTodoViewModel: EditTodoViewModel
  private lateinit var mockAuth: FirebaseAuth
  private lateinit var mockUser: FirebaseUser

  @Before
  fun setUp() {
    repository = ToDosLocalRepository()
    fill_repository()

    // Mock Firebase Auth
    mockAuth = mock(FirebaseAuth::class.java)
    mockUser = mock(FirebaseUser::class.java)
    `when`(mockAuth.currentUser).thenReturn(mockUser)
    `when`(mockUser.uid).thenReturn("user")
    `when`(mockUser.isAnonymous).thenReturn(false)

    editTodoViewModel = EditTodoViewModel(todoRepository = repository, authProvider = { mockAuth })
    composeTestRule.setContent {
      EditToDoScreen(todoUid = todo1.uid, editTodoViewModel = editTodoViewModel)
    }
  }

  private fun fill_repository() = runTest {
    repository.addTodo(todo1)
    advanceUntilIdle()
  }

  @Test
  fun displayAllComponents() {
    composeTestRule
        .onNodeWithTag(EditToDoScreenTestTags.TODO_SAVE)
        .assertExists()
        .assertTextContains("Save", substring = true, ignoreCase = true)
    composeTestRule
        .onNodeWithTag(EditToDoScreenTestTags.TODO_DELETE)
        .assertExists()
        .assertTextContains("Delete", substring = true, ignoreCase = true)

    composeTestRule.onNodeWithTag(EditToDoScreenTestTags.TODO_DELETE).assertExists()
    composeTestRule.onNodeWithTag(EditToDoScreenTestTags.INPUT_TODO_TITLE).assertExists()
    composeTestRule.onNodeWithTag(EditToDoScreenTestTags.INPUT_TODO_DESCRIPTION).assertExists()
    composeTestRule.onNodeWithTag(EditToDoScreenTestTags.INPUT_TODO_ASSIGNEE).assertExists()
    composeTestRule.onNodeWithTag(EditToDoScreenTestTags.INPUT_TODO_LOCATION).assertExists()
    composeTestRule.onNodeWithTag(EditToDoScreenTestTags.INPUT_TODO_DATE).assertExists()
    composeTestRule.onNodeWithTag(EditToDoScreenTestTags.ERROR_MESSAGE).assertIsNotDisplayed()
  }

  @Test
  fun canEnterTitle() {
    val text = "testTitle"
    composeTestRule.enterEditTodoTitle(text)
    composeTestRule.onNodeWithTag(EditToDoScreenTestTags.INPUT_TODO_TITLE).assertTextContains(text)
    composeTestRule
        .onNodeWithTag(EditToDoScreenTestTags.ERROR_MESSAGE, useUnmergedTree = true)
        .assertIsNotDisplayed()
  }

  @Test
  fun canEnterDescription() {
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
  fun canEnterAssigneeName() {
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
  fun canEnterLocation() {
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
  fun canEnterAValidDate() {
    val text = "10/02/2023"
    composeTestRule.enterEditTodoDate(text)
    composeTestRule.onNodeWithTag(EditToDoScreenTestTags.INPUT_TODO_DATE).assertTextContains(text)
  }

  @Test
  fun canEnterAnInvalidDate() {
    val invalidDate = "invalid date" // Invalid date format
    composeTestRule.enterEditTodoDate(invalidDate)
    composeTestRule.checkErrorMessageIsDisplayedForEditTodo()
  }

  @Test
  fun canEnterAValidTime() {
    val text = "14:30"
    composeTestRule.enterEditTodoTime(text)
    composeTestRule.onNodeWithTag(EditToDoScreenTestTags.INPUT_TODO_TIME).assertTextContains(text)
  }

  @Test
  fun canEnterAnInvalidTime() {
    val invalidTime = "invalid time" // Invalid time format
    composeTestRule.enterEditTodoTime(invalidTime)
    composeTestRule.checkErrorMessageIsDisplayedForEditTodo()
  }
}
