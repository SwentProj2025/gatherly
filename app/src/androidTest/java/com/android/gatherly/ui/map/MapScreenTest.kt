package com.android.gatherly.ui.map

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.rule.GrantPermissionRule
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
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Tests for the MapScreen composable. */
class MapScreenTest {

  @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

  // Grant location permissions for the tests (required!)
  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
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

    // Wait for ViewModel init to complete
    while (viewModel.uiState.value.itemsList.isEmpty()) {
      kotlinx.coroutines.delay(10)
    }
  }

  @After
  fun tearDown() {
    viewModel.stopLocationUpdates()
    compose.waitForIdle()
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
  // Navigation tests
  @Test
  fun canGoToEvent() {
    renderDefaultMapUi()
    compose.onNodeWithTag(MapScreenTestTags.EVENT_BUTTON, useUnmergedTree = true).performClick()
    compose
        .onNodeWithTag(EventsScreenTestTags.CREATE_EVENT_BUTTON, useUnmergedTree = true)
        .isDisplayed()
  }

  @Test
  fun canGoToToDo() {
    renderDefaultMapUi()
    compose.onNodeWithTag(MapScreenTestTags.TODO_BUTTON, useUnmergedTree = true).performClick()
    compose
        .onNodeWithTag(OverviewScreenTestTags.CREATE_TODO_BUTTON, useUnmergedTree = true)
        .isDisplayed()
  }

  @Test
  fun mapScreen_renders_with_camera_initialisation() {
    renderDefaultMapUi()
    compose
        .onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN, useUnmergedTree = true)
        .assertExists()
  }

  @Test
  fun mapScreen_renders_after_consulting_todo() {
    viewModel.onItemConsulted(todoId)
    renderDefaultMapUi()
    compose
        .onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN, useUnmergedTree = true)
        .assertExists()
  }

  @Test
  fun mapScreen_renders_with_EPFL_fallback() {
    renderDefaultMapUi()

    // Wait for LaunchedEffect to complete
    compose.waitForIdle()

    // Verify camera position was set to EPFL fallback
    val cameraPos = viewModel.uiState.value.cameraPos
    assert(cameraPos != null)
    assertEquals(EPFL_LATLNG.latitude, cameraPos!!.latitude, 0.0001)
    assertEquals(EPFL_LATLNG.longitude, cameraPos.longitude, 0.0001)
  }

  @Test
  fun mapScreen_renders_on_todo() {
    // Mark todo as consulted
    viewModel.onItemConsulted(todoId)

    renderDefaultMapUi()

    // Wait for LaunchedEffect to complete
    compose.waitForIdle()

    // Verify camera position was set to consulted todo's location
    val cameraPos = viewModel.uiState.value.cameraPos
    assert(cameraPos != null)
    assertEquals(todo.location!!.latitude, cameraPos!!.latitude, 0.0001)
    assertEquals(todo.location!!.longitude, cameraPos.longitude, 0.0001)
  }

  @Test
  fun clicking_todo_marker_opens_sheet_and_navigates() {
    renderDefaultMapUi()
    compose.waitForIdle()

    compose.onNodeWithTag(MapScreenTestTags.TODO_CARD, useUnmergedTree = true).performClick()
    compose.waitForIdle()

    compose.onNodeWithTag(MapScreenTestTags.TODO_SHEET, useUnmergedTree = true).assertExists()
    compose.onNodeWithTag(MapScreenTestTags.TODO_BUTTON, useUnmergedTree = true).performClick()

    compose
        .onNodeWithTag(OverviewScreenTestTags.CREATE_TODO_BUTTON, useUnmergedTree = true)
        .isDisplayed()
  }

  @Test
  fun clicking_event_marker_opens_sheet_and_navigates() {
    renderDefaultMapUi()
    compose.waitForIdle()

    compose.onNodeWithTag(MapScreenTestTags.EVENT_CARD, useUnmergedTree = true).performClick()
    compose.waitForIdle()

    compose.onNodeWithTag(MapScreenTestTags.EVENT_SHEET, useUnmergedTree = true).assertExists()
    compose.onNodeWithTag(MapScreenTestTags.EVENT_BUTTON, useUnmergedTree = true).performClick()

    compose
        .onNodeWithTag(EventsScreenTestTags.CREATE_EVENT_BUTTON, useUnmergedTree = true)
        .isDisplayed()
  }
}
