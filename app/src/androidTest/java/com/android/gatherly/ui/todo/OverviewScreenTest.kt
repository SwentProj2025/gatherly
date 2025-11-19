package com.android.gatherly.ui.todo

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
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
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todo.ToDosLocalRepository
import com.android.gatherly.model.todo.ToDosRepository
import com.android.gatherly.utils.GatherlyTest
import com.google.firebase.Timestamp
import java.util.Calendar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
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

  /** If todos take a long time to load, the loading message appears */
  @Test
  fun loadingTodosShowsCorrectly() {
    class SlowTodosRepo() : ToDosRepository {
      override fun getNewUid(): String {
        TODO("Not yet implemented")
      }

      override suspend fun getAllTodos(): List<ToDo> {
        delay(2_000L)
        return emptyList()
      }

      override suspend fun getTodo(todoID: String): ToDo {
        TODO("Not yet implemented")
      }

      override suspend fun addTodo(toDo: ToDo) {
        TODO("Not yet implemented")
      }

      override suspend fun editTodo(todoID: String, newValue: ToDo) {
        TODO("Not yet implemented")
      }

      override suspend fun deleteTodo(todoID: String) {
        TODO("Not yet implemented")
      }

      override suspend fun getAllEndedTodos(): List<ToDo> {
        TODO("Not yet implemented")
      }

      override suspend fun toggleStatus(todoID: String) {
        TODO("Not yet implemented")
      }
    }

    composeTestRule.setContent {
      OverviewScreen(
          overviewViewModel =
              OverviewViewModel(
                  todoRepository = SlowTodosRepo(), profileRepository = ProfileLocalRepository()))
    }

    // Check that when loading, the loading text is displayed
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.TODO_LOADING).assertIsDisplayed()
  }
}
