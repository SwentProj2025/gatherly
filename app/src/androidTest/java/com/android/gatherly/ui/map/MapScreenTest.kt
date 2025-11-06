package com.android.gatherly.ui.map

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.gatherly.model.event.Event
import com.android.gatherly.model.event.EventStatus
import com.android.gatherly.model.event.EventsLocalRepository
import com.android.gatherly.model.event.EventsRepository
import com.android.gatherly.model.map.Location
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todo.ToDosLocalRepository
import com.android.gatherly.model.todo.ToDosRepository
import com.google.firebase.Timestamp
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** UI tests for MapScreen */
class MapScreenTest {

  @get:Rule val compose = createComposeRule()

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

    compose.setContent {
      MapScreen(viewModel = viewModel)
      ToDoIcon(todo)
      ToDoExpandedIcon(todo)
      EventIcon(event)
      EventExpandedIcon(event)
    }

    compose.waitUntil(timeoutMillis = 20_000) { viewModel.uiState.value.itemsList.isNotEmpty() }
  }

  // Check that the Google Map is displayed
  @Test
  fun google_map_is_displayed() {
    compose
        .onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN, useUnmergedTree = true)
        .assertExists()
  }

  // Check that the filter toggle button is displayed
  @Test
  fun button_is_displayed() {
    compose.onNodeWithTag(MapScreenTestTags.FILTER_TOGGLE, useUnmergedTree = true).assertExists()
  }

  // Test Todo_Icon exists
  @Test
  fun todo_Icon() {
    compose.onNodeWithTag(MapScreenTestTags.TODO_TITLE, useUnmergedTree = true).assertExists()
    compose.onNodeWithTag(MapScreenTestTags.TODO_CARD, useUnmergedTree = true).assertExists()
  }

  // Test Todo_Expanded_Icon exists
  @Test
  fun todo_Icon_expanded() {
    compose
        .onNodeWithTag(MapScreenTestTags.TODO_EXPANDED_CARD, useUnmergedTree = true)
        .assertExists()
    compose
        .onNodeWithTag(MapScreenTestTags.TODO_TITLE_EXPANDED, useUnmergedTree = true)
        .assertExists()
    compose.onNodeWithTag(MapScreenTestTags.TODO_DUE_DATE, useUnmergedTree = true).assertExists()
    compose.onNodeWithTag(MapScreenTestTags.TODO_DESCRIPTION, useUnmergedTree = true).assertExists()
  }

  // Test Event_Icon exists
  @Test
  fun event_exists() {
    compose.onNodeWithTag(MapScreenTestTags.EVENT_CARD, useUnmergedTree = true).assertExists()
    compose.onNodeWithTag(MapScreenTestTags.EVENT_TITLE, useUnmergedTree = true).assertExists()
  }

  // Test Event_Expanded_Icon exists
  @Test
  fun event_expanded_exists() {
    compose
        .onNodeWithTag(MapScreenTestTags.EVENT_EXPANDED_CARD, useUnmergedTree = true)
        .assertExists()
    compose.onNodeWithTag(MapScreenTestTags.EVENT_DATE, useUnmergedTree = true).assertExists()
    compose
        .onNodeWithTag(MapScreenTestTags.EVENT_TITLE_EXPANDED, useUnmergedTree = true)
        .assertExists()
    compose
        .onNodeWithTag(MapScreenTestTags.EVENT_DESCRIPTION, useUnmergedTree = true)
        .assertExists()
  }
}
