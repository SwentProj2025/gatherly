package com.android.gatherly.ui.map

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.android.gatherly.model.event.Event
import com.android.gatherly.model.event.EventStatus
import com.android.gatherly.model.event.EventsLocalRepository
import com.android.gatherly.model.event.EventsRepository
import com.android.gatherly.model.map.Location
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todo.ToDosLocalRepository
import com.android.gatherly.model.todo.ToDosRepository
import com.android.gatherly.ui.events.EventsScreenTestTags
import com.android.gatherly.ui.todo.OverviewScreenTestTags
import com.google.firebase.Timestamp
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Tests for the MapScreen composable. */
class MapScreenTest {

  @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

  private lateinit var toDosRepository: ToDosRepository
  private lateinit var eventsRepository: EventsRepository
  private lateinit var viewModel: MapViewModel

  private val todoId = "t1"
  private val eventId = "e1"

  private val todo =
      ToDo(
          uid = todoId,
          name = "Buy Snacks",
          description = "Chips and soda",
          assigneeName = "Alice",
          dueDate = Timestamp(Date()),
          dueTime = null,
          location = Location(46.5191, 6.5668, "EPFL SG"),
          status = ToDoStatus.ONGOING,
          ownerId = "owner-1")

  private val event =
      Event(
          id = eventId,
          title = "Board Games Night",
          description = "Starts 20:00",
          creatorName = "CLIC",
          location = Location(46.5210, 6.5690, "EPFL BC"),
          date = Timestamp(Date()),
          startTime = Timestamp(Date()),
          endTime = Timestamp(Date(Date().time + TimeUnit.HOURS.toMillis(2))),
          creatorId = "org-1",
          participants = listOf("u1", "u2"),
          status = EventStatus.UPCOMING)

  @Before
  fun setUp() = runBlocking {
    toDosRepository = ToDosLocalRepository().apply { addTodo(todo) }
    eventsRepository = EventsLocalRepository().apply { addEvent(event) }
    viewModel = MapViewModel(todosRepository = toDosRepository, eventsRepository = eventsRepository)
  }

  // Helper for your existing UI existence tests
  private fun renderDefaultMapUi() {
    compose.setContent {
      MapScreen(viewModel = viewModel)
      ToDoIcon(todo)
      ToDoSheet(todo, onGoToToDo = {}, onClose = {})
      EventIcon(event)
      EventSheet(event, onGoToEvent = {}, onClose = {})
    }
  }

  // Check that the Google Map is displayed
  @Test
  fun google_map_is_displayed() {
    renderDefaultMapUi()
    compose
        .onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN, useUnmergedTree = true)
        .assertExists()
  }

  // Check that the filter toggle button is displayed
  @Test
  fun button_is_displayed() {
    renderDefaultMapUi()
    compose.onNodeWithTag(MapScreenTestTags.FILTER_TOGGLE, useUnmergedTree = true).assertExists()
  }

  // Test Event_Icon exists
  @Test
  fun event_exists() {
    renderDefaultMapUi()
    compose.onNodeWithTag(MapScreenTestTags.EVENT_CARD, useUnmergedTree = true).assertExists()
    compose.onNodeWithTag(MapScreenTestTags.EVENT_TITLE, useUnmergedTree = true).assertExists()
  }

  // Test Event Sheet exists
  @Test
  fun event_sheet_exists() {
    renderDefaultMapUi()
    compose.onNodeWithTag(MapScreenTestTags.EVENT_SHEET, useUnmergedTree = true).assertExists()
    compose.onNodeWithTag(MapScreenTestTags.EVENT_DATE, useUnmergedTree = true).assertExists()
    compose
        .onNodeWithTag(MapScreenTestTags.EVENT_TITLE_SHEET, useUnmergedTree = true)
        .assertExists()
    compose
        .onNodeWithTag(MapScreenTestTags.EVENT_DESCRIPTION, useUnmergedTree = true)
        .assertExists()
    compose.onNodeWithTag(MapScreenTestTags.EVENT_BUTTON, useUnmergedTree = true).assertExists()
  }

  // Test ToDo Icon exists
  @Test
  fun todo_exists() {
    renderDefaultMapUi()
    compose.onNodeWithTag(MapScreenTestTags.TODO_CARD, useUnmergedTree = true).assertExists()
    compose.onNodeWithTag(MapScreenTestTags.TODO_TITLE, useUnmergedTree = true).assertExists()
  }

  // Test ToDo Sheet exists
  @Test
  fun todo_sheet_exists() {
    renderDefaultMapUi()
    compose.onNodeWithTag(MapScreenTestTags.TODO_SHEET, useUnmergedTree = true).assertExists()
    compose.onNodeWithTag(MapScreenTestTags.TODO_DUE_DATE, useUnmergedTree = true).assertExists()
    compose.onNodeWithTag(MapScreenTestTags.TODO_TITLE_SHEET, useUnmergedTree = true).assertExists()
    compose.onNodeWithTag(MapScreenTestTags.TODO_DESCRIPTION, useUnmergedTree = true).assertExists()
    compose.onNodeWithTag(MapScreenTestTags.TODO_BUTTON, useUnmergedTree = true).assertExists()
  }

  @Test
  fun canGoToEvent() {
    compose.setContent {
      val nav = androidx.navigation.compose.rememberNavController()
      NavHost(navController = nav, startDestination = "map") {
        composable("map") {
          MapScreen(viewModel = viewModel)
          EventSheet(event = event, onGoToEvent = { nav.navigate("events") }, onClose = {})
        }
        composable("events") {
          Text("Your Events", Modifier.testTag(EventsScreenTestTags.YOUR_EVENTS_TITLE))
        }
      }
    }
    compose.onNodeWithTag(MapScreenTestTags.EVENT_BUTTON, useUnmergedTree = true).performClick()
    compose.onNodeWithTag(EventsScreenTestTags.YOUR_EVENTS_TITLE).assertIsDisplayed()
  }

  @Test
  fun canGoToToDo() {
    compose.setContent {
      val nav = androidx.navigation.compose.rememberNavController()
      NavHost(navController = nav, startDestination = "map") {
        composable("map") {
          MapScreen(viewModel = viewModel)
          ToDoSheet(toDo = todo, onGoToToDo = { nav.navigate("todos") }, onClose = {})
        }
        composable("todos") {
          Text("ToDo", Modifier.testTag(OverviewScreenTestTags.CREATE_TODO_BUTTON))
        }
      }
    }
    compose.onNodeWithTag(MapScreenTestTags.TODO_BUTTON, useUnmergedTree = true).performClick()
    compose.onNodeWithTag(OverviewScreenTestTags.CREATE_TODO_BUTTON).assertIsDisplayed()
  }
}
