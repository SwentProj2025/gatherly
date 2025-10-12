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
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.android.gatherly.model.map.Location
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todo.ToDosRepository
import com.android.gatherly.model.todo.ToDosRepositoryProvider
import com.android.gatherly.ui.todo.OverviewScreenTestTags
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before

// Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the
// SwEnt staff.

const val UI_WAIT_TIMEOUT = 5_000L

/** Base class for Gatherly tests, providing common setup and utility functions. */
abstract class GatherlyTest() {

  abstract fun createInitializedRepository(): ToDosRepository

  val repository: ToDosRepository
    get() = ToDosRepositoryProvider.repository

  val shouldSignInAnounymously: Boolean = true

  open val todo1 =
      ToDo(
          uid = "0",
          name = "Buy groceries",
          description = "Milk, eggs, bread, and butter",
          assigneeName = "Alice",
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
          assigneeName = "Bob",
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
          assigneeName = "Charlie",
          dueDate = Timestamp.Companion.fromDate(2025, Calendar.NOVEMBER, 10),
          location = Location(46.5200, 6.5800, "City Library"),
          dueTime = Timestamp.now(),
          status = ToDoStatus.ENDED,
          ownerId = "user")

  @Before
  open fun setUp() {
    ToDosRepositoryProvider.repository = createInitializedRepository()
    if (shouldSignInAnounymously) {
      runTest { FirebaseEmulator.auth.signInAnonymously().await() }
    }
  }

  @After
  open fun tearDown() {
    if (FirebaseEmulator.isRunning) {
      FirebaseEmulator.auth.signOut()
      FirebaseEmulator.clearAuthEmulator()
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
          assigneeName == other.assigneeName &&
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
