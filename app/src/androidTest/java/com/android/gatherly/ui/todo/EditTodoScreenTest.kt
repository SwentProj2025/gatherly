package com.android.gatherly.ui.todo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.todo.ToDosLocalRepository
import com.android.gatherly.utils.AlertDialogTestTags
import com.android.gatherly.utils.GatherlyTest
import com.android.gatherly.utils.MockitoUtils
import com.android.gatherly.utils.TestDates.currentDateTimestamp
import com.android.gatherly.utils.TestDates.currentDay
import com.android.gatherly.utils.TestDates.currentMonth
import com.android.gatherly.utils.TestDates.futureDate
import com.android.gatherly.utils.TestDates.futureYear
import com.android.gatherly.utils.TestDates.pastYear
import com.android.gatherly.utils.openDatePicker
import com.android.gatherly.utils.selectDateFromPicker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditTodoScreenTest : GatherlyTest() {
  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var editTodoViewModel: EditTodoViewModel
  private lateinit var profileRepository: ProfileRepository
  private lateinit var mockitoUtils: MockitoUtils

  @Before
  fun setUp() {
    repository = ToDosLocalRepository()
    profileRepository = ProfileLocalRepository()
    fill_repository()

    // Mock Firebase Auth
    mockitoUtils = MockitoUtils()
    mockitoUtils.chooseCurrentUser("user")

    editTodoViewModel =
        EditTodoViewModel(todoRepository = repository, profileRepository = profileRepository)
    composeTestRule.setContent {
      EditToDoScreen(todoUid = todo1.uid, editTodoViewModel = editTodoViewModel)
    }
    composeTestRule
        .onNodeWithTag(EditToDoScreenTestTags.MORE_OPTIONS)
        .assertIsDisplayed()
        .performClick()
  }

  private fun fill_repository() = runTest {
    repository.addTodo(toDo = todo1.copy(dueDate = currentDateTimestamp))
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
    composeTestRule.onNodeWithTag(LocationSuggestionsTestTags.INPUT).assertExists()
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
  fun canEnterLocation() {
    val text = "testLocation"
    composeTestRule.enterEditTodoLocation(text)
    composeTestRule.onNodeWithTag(LocationSuggestionsTestTags.INPUT).assertTextContains(text)
    composeTestRule
        .onNodeWithTag(EditToDoScreenTestTags.ERROR_MESSAGE, useUnmergedTree = true)
        .assertIsNotDisplayed()
  }

  @Test
  fun canEnterAValidDate() {
    composeTestRule.openDatePicker(EditToDoScreenTestTags.INPUT_TODO_DATE)
    composeTestRule.selectDateFromPicker(currentDay, currentMonth, futureYear)
    composeTestRule
        .onAllNodes(hasText(futureDate, substring = true, ignoreCase = true))
        .filterToOne(hasAnyAncestor(hasTestTag(EditToDoScreenTestTags.INPUT_TODO_DATE)))
        .assertExists()
  }

  @Test
  fun canEnterAValidTime() {
    val text = "14:30"
    composeTestRule.enterEditTodoTime(text)
    composeTestRule.onNodeWithTag(EditToDoScreenTestTags.INPUT_TODO_TIME).assertTextContains(text)
  }

  @Test
  fun canEnterAnInvalidTime() {
    val invalidTime = "25:80" // Invalid time format
    composeTestRule.enterEditTodoTime(invalidTime)
    composeTestRule.checkErrorMessageIsDisplayedForEditTodo()
  }

  @Test
  fun enterPastDate() {
    composeTestRule.openDatePicker(EditToDoScreenTestTags.INPUT_TODO_DATE)
    composeTestRule.selectDateFromPicker(currentDay, currentMonth, pastYear)
    composeTestRule.onNodeWithTag(EditToDoScreenTestTags.TODO_SAVE).performClick()
    composeTestRule.onNodeWithTag(AlertDialogTestTags.ALERT).assertIsDisplayed()
  }

  /** Check that pressing the delete button shows the alert dialog */
  @Test
  fun deleteTodoShowsAlertDialog() {
    composeTestRule
        .onNodeWithTag(EditToDoScreenTestTags.TODO_DELETE)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.onNodeWithTag(AlertDialogTestTags.ALERT).assertIsDisplayed()
  }
}
