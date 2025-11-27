package com.android.gatherly.ui.todo

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todo.ToDosLocalRepository
import com.android.gatherly.utils.GatherlyTest
import com.google.firebase.Timestamp
import java.util.Calendar
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OverviewScreenTest : GatherlyTest() {
  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var overviewViewModel: OverviewViewModel
  private lateinit var profileRepository: ProfileRepository

  @Before
  fun setUp() {
    repository = ToDosLocalRepository()
    profileRepository = ProfileLocalRepository()
  }

  fun setContent(withInitialTodos: List<ToDo> = emptyList()) = runTest {
    withInitialTodos.forEach { repository.addTodo(it) }
    overviewViewModel =
        OverviewViewModel(todoRepository = repository, profileRepository = profileRepository)
    composeTestRule.setContent { OverviewScreen(overviewViewModel = overviewViewModel) }
    profileRepository.addProfile(Profile(uid = "user", name = "Test User", profilePicture = ""))
    advanceUntilIdle()
  }

  @Test
  fun testTagsCorrectlySetWhenListIsEmpty() {
    setContent()
    composeTestRule
        .onNodeWithTag(OverviewScreenTestTags.TODO_LIST, useUnmergedTree = true)
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag(OverviewScreenTestTags.EMPTY_TODO_LIST_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun testTagsCorrectlySetWhenListIsNotEmpty() {
    setContent(withInitialTodos = listOf(todo1, todo2, todo3))
    composeTestRule
        .onNodeWithTag(OverviewScreenTestTags.TODO_LIST, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(OverviewScreenTestTags.getTestTagForTodoItem(todo1), useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(OverviewScreenTestTags.getTestTagForTodoItem(todo2), useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(OverviewScreenTestTags.getTestTagForTodoItem(todo3), useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun todoListDisplaysTaskName() {
    val todoList = listOf(todo1)
    setContent(withInitialTodos = todoList)
    composeTestRule.onTodoItem(todo1, hasText(todo1.name))
  }

  @Test
  fun todoListDisplaysDueDate() {
    val todo = todo1.copy(dueDate = Timestamp.Companion.fromDate(2023, Calendar.DECEMBER, 25))
    val todoList = listOf(todo)
    val dueDate = "25/12/2023"
    setContent(withInitialTodos = todoList)
    composeTestRule.onTodoItem(todo, hasText(dueDate))
  }

  @Test
  fun todoListDisplaysExistingTodos() {
    val todoList = listOf(todo1, todo2)
    setContent(withInitialTodos = todoList)
    // Check that each todo item is displayed.
    todoList.forEach { composeTestRule.onTodoItem(it, hasText(it.name)) }
  }

  @Test
  fun dueDateIsCorrectlyFormatted() {
    val todo1 =
        todo1.copy(uid = "1", dueDate = Timestamp.Companion.fromDate(2023, Calendar.DECEMBER, 25))
    val todoList = listOf(todo1)
    val dueDate1 = "25/12/2023"
    setContent(withInitialTodos = todoList)
    composeTestRule.onTodoItem(todo1, hasText(dueDate1))
  }

  @Test
  fun canScrollOnTheTodoList() {
    val todos =
        (1..50).toList<Int>().map { todo1.copy(uid = it.toString(), name = "${todo1.name} #$it") }
    setContent(withInitialTodos = todos)
    composeTestRule
        .onNodeWithTag(
            OverviewScreenTestTags.getTestTagForTodoItem(todos.first()), useUnmergedTree = true)
        .assertIsDisplayed()
    val lastNode =
        composeTestRule.onNodeWithTag(
            OverviewScreenTestTags.getTestTagForTodoItem(todos.last()), useUnmergedTree = true)
    lastNode.assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag(OverviewScreenTestTags.TODO_LIST, useUnmergedTree = true)
        .performScrollToNode(hasTestTag(OverviewScreenTestTags.getTestTagForTodoItem(todos.last())))
    lastNode.assertIsDisplayed()
  }

  // Portions of the code in this test were generated by an LLM.
  @Test
  fun checkboxTogglesTodoStatusBetweenOngoingAndCompleted() {
    // create a single ongoing todo
    val todo = todo1.copy(status = ToDoStatus.ONGOING)
    setContent(withInitialTodos = listOf(todo))

    val checkboxTag = OverviewScreenTestTags.getCheckboxTagForTodoItem(todo)

    // checkbox is not checked (Ongoing section)
    composeTestRule.onNodeWithTag(checkboxTag).assertExists().assertIsOff()

    // click the checkbox to mark it as completed
    composeTestRule.onNodeWithTag(checkboxTag).performClick()

    // todo should now appear in the Completed section (checked)
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      // Wait until UI recomposes with updated status
      composeTestRule.onAllNodesWithTag(checkboxTag).fetchSemanticsNodes().any {
        it.config.contains(SemanticsProperties.ToggleableState)
      }
    }

    composeTestRule.onNodeWithTag(checkboxTag).assertIsOn()

    // uncheck to mark it as ongoing again
    composeTestRule.onNodeWithTag(checkboxTag).performClick()

    // should return to Ongoing section (unchecked)
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodesWithTag(checkboxTag).fetchSemanticsNodes().isNotEmpty()
    }

    composeTestRule.onNodeWithTag(checkboxTag).assertIsOff()
  }

  @Test
  fun searchBarFiltersTodosByName() = runTest {
    val todos =
        listOf(
            todo1.copy(name = "Buy milk"),
            todo2.copy(name = "Walk dog"),
            todo3.copy(name = "Read book"))

    setContent(withInitialTodos = todos)

    // Type a query that matches only one item
    composeTestRule
        .onNodeWithTag(OverviewScreenTestTags.SEARCH_BAR)
        .performClick()
        .performTextInput("dog")

    // Only "Walk dog" should remain visible
    composeTestRule
        .onNodeWithTag(OverviewScreenTestTags.getTestTagForTodoItem(todos[1]))
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(OverviewScreenTestTags.getTestTagForTodoItem(todos[0]))
        .assertIsNotDisplayed()

    composeTestRule
        .onNodeWithTag(OverviewScreenTestTags.getTestTagForTodoItem(todos[2]))
        .assertIsNotDisplayed()
  }

  @Test
  fun searchMatchesTodoDescription() = runTest {
    val todos =
        listOf(
            todo1.copy(name = "Test", description = "Important meeting"),
            todo2.copy(name = "Another", description = "Just chilling"))

    setContent(withInitialTodos = todos)

    // Search a word found only in the description of todo1
    composeTestRule
        .onNodeWithTag(OverviewScreenTestTags.SEARCH_BAR)
        .performClick()
        .performTextInput("meeting")

    composeTestRule
        .onNodeWithTag(OverviewScreenTestTags.getTestTagForTodoItem(todos[0]))
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(OverviewScreenTestTags.getTestTagForTodoItem(todos[1]))
        .assertIsNotDisplayed()
  }

  @Test
  fun clearingSearchRestoresFullList() = runTest {
    val todos = listOf(todo1, todo2, todo3)

    setContent(withInitialTodos = todos)

    val searchBar = composeTestRule.onNodeWithTag(OverviewScreenTestTags.SEARCH_BAR)

    // Filter down to a single match
    searchBar.performClick().performTextInput("abc")
    advanceUntilIdle()

    // Now clear the search
    searchBar.performTextClearance()
    advanceUntilIdle()

    // All todos should reappear
    todos.forEach { todo ->
      composeTestRule
          .onNodeWithTag(OverviewScreenTestTags.getTestTagForTodoItem(todo))
          .assertIsDisplayed()
    }
  }

  @Test
  fun sortMenu_opensOnClick() = runTest {
    setContent(withInitialTodos = listOf(todo1))

    composeTestRule.onNodeWithTag(OverviewScreenTestTags.SORT_MENU_BUTTON).performClick()

    composeTestRule.onNode(hasText("Date ascending")).assertIsDisplayed()
    composeTestRule.onNode(hasText("Date descending")).assertIsDisplayed()
    composeTestRule.onNode(hasText("Alphabetical")).assertIsDisplayed()
  }

  @Test
  fun alphabeticalSort_changesOrderCorrectly() = runTest {
    val todos =
        listOf(
            todo1.copy(name = "Charlie", status = ToDoStatus.ONGOING),
            todo2.copy(name = "Alpha", status = ToDoStatus.ONGOING),
            todo3.copy(name = "Bravo", status = ToDoStatus.ONGOING))
    setContent(todos)

    composeTestRule.onNodeWithTag(OverviewScreenTestTags.SORT_MENU_BUTTON).performClick()
    composeTestRule.onNode(hasText("Alphabetical")).performClick()
    advanceUntilIdle()

    val posAlpha = positionOf(todos[1])
    val posBravo = positionOf(todos[2])
    val posCharlie = positionOf(todos[0])

    assertTrue(posAlpha < posBravo, "Alpha should appear above Bravo")
    assertTrue(posBravo < posCharlie, "Bravo should appear above Charlie")
  }

  @Test
  fun sortDateAscending_changesOrderCorrectly() = runTest {
    val todoA = todo1.copy(name = "A", status = ToDoStatus.ONGOING, dueDate = Timestamp(1000, 0))
    val todoB = todo2.copy(name = "B", status = ToDoStatus.ONGOING, dueDate = Timestamp(3000, 0))
    val todoC = todo3.copy(name = "C", status = ToDoStatus.ONGOING, dueDate = Timestamp(2000, 0))

    val todos = listOf(todoA, todoC, todoB)
    setContent(todos)

    composeTestRule.onNodeWithTag(OverviewScreenTestTags.SORT_MENU_BUTTON).performClick()
    composeTestRule.onNode(hasText("Date ascending")).performClick()
    advanceUntilIdle()

    val posA = positionOf(todoA)
    val posC = positionOf(todoC)
    val posB = positionOf(todoB)

    assertTrue(posA < posC, "A should appear above C")
    assertTrue(posC < posB, "C should appear above B")
  }

  @Test
  fun sortDateDescending_changesOrderCorrectly() = runTest {
    val a = todo1.copy(name = "A", dueDate = Timestamp(1000, 0), status = ToDoStatus.ONGOING)
    val b = todo2.copy(name = "B", dueDate = Timestamp(3000, 0), status = ToDoStatus.ONGOING)
    val c = todo3.copy(name = "C", dueDate = Timestamp(2000, 0), status = ToDoStatus.ENDED)

    val todos = listOf(a, b, c)
    setContent(withInitialTodos = todos)

    composeTestRule.onNodeWithTag(OverviewScreenTestTags.SORT_MENU_BUTTON).performClick()
    composeTestRule.onNode(hasText("Date descending")).performClick()
    advanceUntilIdle()

    val posA = positionOf(a)
    val posB = positionOf(b)
    val posC = positionOf(c)

    // Within ONGOING section: B (3000) above A (1000)
    assertTrue(posB < posA)

    // Completed section appears after all ongoing items
    assertTrue(posA < posC)
  }

  @Test
  fun searchAndSortInteractCorrectly() = runTest {
    val banana = todo1.copy(name = "Banana")
    val apple = todo2.copy(name = "Apple")
    val apricot = todo3.copy(name = "Apricot")
    setContent(listOf(banana, apple, apricot))

    // Search "ap"
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.SEARCH_BAR).performTextInput("ap")
    advanceUntilIdle()

    // Sort alphabetical
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.SORT_MENU_BUTTON).performClick()
    composeTestRule.onNode(hasText("Alphabetical")).performClick()
    advanceUntilIdle()

    val posApple = positionOf(apple)
    val posApricot = positionOf(apricot)

    assertTrue(posApple < posApricot)
  }

  @Test
  fun ongoingAndCompletedAreSortedIndependently() = runTest {
    val a = todo1.copy(name = "A", status = ToDoStatus.ONGOING, dueDate = Timestamp(3000, 0))
    val b = todo2.copy(name = "B", status = ToDoStatus.ONGOING, dueDate = Timestamp(1000, 0))

    val c = todo3.copy(name = "C", status = ToDoStatus.ENDED, dueDate = Timestamp(4000, 0))
    val d =
        todo1.copy(uid = "X", name = "D", status = ToDoStatus.ENDED, dueDate = Timestamp(2000, 0))

    setContent(listOf(a, b, c, d))

    composeTestRule.onNodeWithTag(OverviewScreenTestTags.SORT_MENU_BUTTON).performClick()
    composeTestRule.onNode(hasText("Date ascending")).performClick()
    advanceUntilIdle()

    val posA = positionOf(a)
    val posB = positionOf(b)

    val posC = positionOf(c)
    val posD = positionOf(d)

    // ---- Ongoing sorted internally ----
    assertTrue(posB < posA)

    // ---- Completed sorted internally ----
    assertTrue(posD < posC)

    // ---- Completed section must be below ongoing section ----
    assertTrue(posA < posC)
  }

  private fun positionOf(todo: ToDo): Float {
    val tag = OverviewScreenTestTags.getTestTagForTodoItem(todo)

    // Ensure it's brought into view first
    composeTestRule.onNodeWithTag(tag, useUnmergedTree = true).performScrollTo()

    return composeTestRule
        .onNodeWithTag(tag, useUnmergedTree = true)
        .fetchSemanticsNode()
        .boundsInRoot
        .top
  }
}
