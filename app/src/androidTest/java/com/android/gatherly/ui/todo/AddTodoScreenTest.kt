package com.android.gatherly.ui.todo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.todo.ToDosLocalRepository
import com.android.gatherly.utils.AlertDialogTestTags
import com.android.gatherly.utils.GatherlyTest
import com.android.gatherly.utils.MockitoUtils
import com.android.gatherly.utils.selectDateFromPicker
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.util.Calendar

class AddTodoScreenTest : GatherlyTest() {
  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var addTodoViewModel: AddTodoViewModel
  private lateinit var profileRepository: ProfileRepository
  private lateinit var mockitoUtils: MockitoUtils

  private val currentMonth = LocalDate.now().month.value
  private val currentDay = LocalDate.now().dayOfMonth
  private val currentYear = LocalDate.now().year
  private val pastYear = currentYear.minus(1)
  private val futureYear = currentYear.plus(1)

  private val futureDate = "$currentDay/$currentMonth/$futureYear"

  val calendar = Calendar.getInstance().apply {
    set(Calendar.YEAR, futureYear)
    set(Calendar.MONTH, currentMonth - 1)
    set(Calendar.DAY_OF_MONTH, currentDay)
  }
  val futureDueDate: Timestamp = Timestamp(calendar.time)

  val calendar2 = Calendar.getInstance().apply {
    set(Calendar.YEAR, pastYear)
    set(Calendar.MONTH, currentMonth - 1)
    set(Calendar.DAY_OF_MONTH, currentDay)
  }
  val pastDueDate: Timestamp = Timestamp(calendar2.time)

  private val pastDate = "$currentDay/$currentMonth/$pastYear"

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
    composeTestRule.selectDateFromPicker(currentDay, currentMonth, futureYear)
    composeTestRule.onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_DATE)
      .assertTextContains(futureDate, ignoreCase = true)
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
    composeTestRule.enterAddTodoDetails(todo = todo1.copy(name = " ", dueDate = futureDueDate))
    composeTestRule.clickOnSaveForAddTodo()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(AddToDoScreenTestTags.TODO_SAVE).assertExists()
  }

  @Test
  fun savingWithInvalidDescriptionShouldDoNothing() = checkNoTodoWereAdded {
    composeTestRule.enterAddTodoDetails(todo = todo1.copy(description = " ", dueDate = futureDueDate))
    composeTestRule.clickOnSaveForAddTodo()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(AddToDoScreenTestTags.TODO_SAVE).assertExists()
  }

  @Test
  fun savingWithInvalidAssigneeShouldDoNothing() = checkNoTodoWereAdded {
    composeTestRule.enterAddTodoDetails(todo = todo1.copy(assigneeName = " ", dueDate = futureDueDate))
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
  fun enteringInvalidTimeShowsErrorMessage() {
    val invalidTime = "14:61" // Invalid time format
    composeTestRule.enterAddTodoTime(invalidTime)
    composeTestRule.checkErrorMessageIsDisplayedForAddTodo()
  }

  @Test
  fun enterPastDate() {
    composeTestRule.enterAddTodoDetails(todo = todo1.copy(dueDate = pastDueDate))
    composeTestRule.onNodeWithTag(AddToDoScreenTestTags.TODO_SAVE).performClick()
    composeTestRule.onNodeWithTag(AlertDialogTestTags.ALERT).assertIsDisplayed()
  }
}
