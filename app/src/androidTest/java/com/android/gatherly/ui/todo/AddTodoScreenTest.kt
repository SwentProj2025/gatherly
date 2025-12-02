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
import com.android.gatherly.utils.TestDates.currentDay
import com.android.gatherly.utils.TestDates.currentMonth
import com.android.gatherly.utils.TestDates.futureDate
import com.android.gatherly.utils.TestDates.futureYear
import com.android.gatherly.utils.TestDates.pastYear
import com.android.gatherly.utils.openDatePicker
import com.android.gatherly.utils.selectDateFromPicker
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddTodoScreenTest : GatherlyTest() {
  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var addTodoViewModel: AddTodoViewModel
  private lateinit var profileRepository: ProfileRepository
  private lateinit var mockitoUtils: MockitoUtils

  @Before
  fun setUp() {
    repository = ToDosLocalRepository()
    profileRepository = ProfileLocalRepository()

    // Mock Firebase Auth
    mockitoUtils = MockitoUtils()
    mockitoUtils.chooseCurrentUser("user")

    addTodoViewModel =
        AddTodoViewModel(
            todoRepository = repository,
            profileRepository = profileRepository,
            authProvider = { mockitoUtils.mockAuth })
    composeTestRule.setContent { AddToDoScreen(addTodoViewModel = addTodoViewModel) }
    composeTestRule
        .onNodeWithTag(AddToDoScreenTestTags.MORE_OPTIONS)
        .assertIsDisplayed()
        .performClick()
  }

  @Test
  fun displayAllComponents() {
    composeTestRule
        .onNodeWithTag(AddToDoScreenTestTags.TODO_SAVE)
        .assertTextContains("Save", substring = true, ignoreCase = true)
    composeTestRule.onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_DESCRIPTION).assertIsDisplayed()
    composeTestRule.onNodeWithTag(LocationSuggestionsTestTags.INPUT).assertIsDisplayed()
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
  fun canEnterLocation() {
    val text = "location"
    composeTestRule.enterAddTodoLocation(text)
    composeTestRule.onNodeWithTag(LocationSuggestionsTestTags.INPUT).assertTextContains(text)
    composeTestRule
        .onNodeWithTag(AddToDoScreenTestTags.ERROR_MESSAGE, useUnmergedTree = true)
        .assertIsNotDisplayed()
  }

  @Test
  fun canEnterAValidDate() {
    composeTestRule.openDatePicker(AddToDoScreenTestTags.INPUT_TODO_DATE)
    composeTestRule.selectDateFromPicker(currentDay, currentMonth, futureYear)
    composeTestRule
        .onAllNodes(hasText(futureDate, substring = true, ignoreCase = true))
        .filterToOne(hasAnyAncestor(hasTestTag(AddToDoScreenTestTags.INPUT_TODO_DATE)))
        .assertExists()
  }

  @Test
  fun canEnterAValidTime() {
    val text = "14:01"
    composeTestRule.enterAddTodoTime(text)
    composeTestRule.onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_TIME).assertTextContains(text)
  }

  @Test
  fun canEnterAnInvalidTime() {
    val text = "13:99"
    composeTestRule.enterAddTodoTime(text)
    composeTestRule.onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_TIME).assertTextContains(text)
  }

  @Test
  fun savingWithInvalidTitleShouldDoNothing() = checkNoTodoWereAdded {
    composeTestRule.enterAddTodoDetails(todo = todo1.copy(name = " "))
    composeTestRule.enterAddTodoDate(futureDate)
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
  fun enteringInvalidTimeShowsErrorMessage() {
    val invalidTime = "14:61" // Invalid time format
    composeTestRule.enterAddTodoTime(invalidTime)
    composeTestRule.checkErrorMessageIsDisplayedForAddTodo()
  }

  @Test
  fun enterPastDate() =
      runTest(timeout = 60.seconds) {
        composeTestRule.enterAddTodoDetails(todo1)
        composeTestRule.openDatePicker(AddToDoScreenTestTags.INPUT_TODO_DATE)
        composeTestRule.selectDateFromPicker(currentDay, currentMonth, pastYear)
        composeTestRule.onNodeWithTag(AddToDoScreenTestTags.TODO_SAVE).performClick()
        composeTestRule.onNodeWithTag(AlertDialogTestTags.ALERT).assertIsDisplayed()
      }
}
