package com.android.gatherly.ui.map

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
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
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todo.ToDosLocalRepository
import com.android.gatherly.model.todo.ToDosRepository
import com.android.gatherly.ui.events.EventsScreen
import com.android.gatherly.ui.events.EventsScreenTestTags
import com.android.gatherly.ui.events.EventsViewModel
import com.android.gatherly.ui.todo.OverviewScreenTestTags
import com.android.gatherly.utils.AlertDialogTestTags
import com.android.gatherly.utils.MockitoUtils
import com.google.firebase.Timestamp
import java.util.Date
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// This file contains code written by an LLM (Claude.ai, Gemini Pro, GitHub Copilot).

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
  private lateinit var profileRepository: ProfileRepository
  private lateinit var mockitoUtils: MockitoUtils
  private lateinit var viewModel: MapViewModel

  private val todoId = "t1"
  private val eventId = "e1"

  private val participatingEventId = "e2"
  private val creatingEventId = "e3"

  private val TEST_USER_ID = "testUserId"

  private val todo =
      ToDo(
          uid = todoId,
          name = "Buy Snacks",
          description = "Chips and soda",
          dueDate = Timestamp(Date()),
          dueTime = null,
          location = Location(46.5191, 6.5668, "EPFL SG"),
          status = ToDoStatus.ONGOING,
          ownerId = "owner-1")

  private val oneHourLater = Timestamp(Date(System.currentTimeMillis() + 3600_000))
  private val twoHoursLater = Timestamp(Date(System.currentTimeMillis() + 7200_000))
  private val event =
      Event(
          id = eventId,
          title = "Board Games Night",
          description = "Starts 20:00",
          creatorName = "CLIC",
          location = Location(46.5210, 6.5690, "EPFL BC"),
          date = Timestamp(Date()),
          startTime = oneHourLater,
          endTime = twoHoursLater,
          creatorId = "org-1",
          participants = listOf("u1", "u2", "org-1"),
          status = EventStatus.UPCOMING)

  private val participatingEvent =
      Event(
          id = participatingEventId,
          title = "Subsonic Party",
          description = "Starts 18:30",
          creatorName = "CLIC",
          location = Location(46.5210, 6.5690, "EPFL BC"),
          date = Timestamp(Date()),
          startTime = oneHourLater,
          endTime = twoHoursLater,
          creatorId = "org-1",
          participants = listOf("u1", "u2", TEST_USER_ID),
          status = EventStatus.UPCOMING)

  private val creatingEvent =
      Event(
          id = creatingEventId,
          title = "ICeLan",
          description = "25h arcade games",
          creatorName = "Game*",
          location = Location(46.5210, 6.5690, "EPFL BC"),
          date = Timestamp(Date()),
          startTime = oneHourLater,
          endTime = twoHoursLater,
          creatorId = TEST_USER_ID,
          participants = listOf(TEST_USER_ID),
          status = EventStatus.UPCOMING)

  @Before
  fun setUp() = runBlocking {
    toDosRepository = ToDosLocalRepository().apply { addTodo(todo) }
    profileRepository = ProfileLocalRepository()
    eventsRepository =
        EventsLocalRepository().apply {
          addEvent(event)
          addEvent(participatingEvent)
          addEvent(creatingEvent)
        }
    viewModel = MapViewModel(todosRepository = toDosRepository, eventsRepository = eventsRepository)

    // Wait for ViewModel init to complete
    while (viewModel.uiState.value.itemsList.isEmpty()) {
      delay(10)
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

    // Wait for camera position to initialize
    compose.waitForIdle()
    runBlocking {
      while (viewModel.uiState.value.cameraPos == null) {
        kotlinx.coroutines.delay(10)
      }
    }
  }

  private fun renderMapScreenOnly() {
    compose.setContent { MapScreen(viewModel = viewModel) }

    // Wait for camera position to initialize
    compose.waitForIdle()
    runBlocking {
      while (viewModel.uiState.value.cameraPos == null) {
        kotlinx.coroutines.delay(10)
      }
    }
  }

  private fun renderMapScreenWithoutInitialisation() {
    compose.setContent { MapScreen(viewModel = viewModel, runInitialisation = false) }
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
  fun clicking_todo_marker_opens_sheet_and_calls_onItemConsulted() {
    renderMapScreenOnly()
    compose.waitForIdle()

    assertEquals(null, viewModel.uiState.value.lastConsultedTodoId)

    // Simulate marker click by calling ViewModel directly
    viewModel.onSelectedItem(todoId)
    compose.waitForIdle()

    // Verify sheet opened
    compose.onNodeWithTag(MapScreenTestTags.TODO_SHEET, useUnmergedTree = true).assertExists()

    // Click the navigation button
    compose.onNodeWithTag(MapScreenTestTags.TODO_BUTTON, useUnmergedTree = true).performClick()
    compose.waitForIdle()

    // Verify onItemConsulted was called
    assertEquals(todoId, viewModel.uiState.value.lastConsultedTodoId)
  }

  @Test
  fun clicking_event_marker_opens_sheet_and_calls_onItemConsulted() {
    viewModel.changeView()
    renderMapScreenOnly()
    compose.waitForIdle()

    assertEquals(null, viewModel.uiState.value.lastConsultedEventId)

    // Simulate marker click
    viewModel.onSelectedItem(eventId)
    compose.waitForIdle()

    // Verify sheet opened
    compose.onNodeWithTag(MapScreenTestTags.EVENT_SHEET, useUnmergedTree = true).assertExists()

    // Click button
    compose.onNodeWithTag(MapScreenTestTags.EVENT_BUTTON, useUnmergedTree = true).performClick()
    compose.waitForIdle()

    // Verify onItemConsulted was called
    assertEquals(eventId, viewModel.uiState.value.lastConsultedEventId)
  }

  @Test
  fun clicking_filter_toggle_changes_view() {
    renderMapScreenOnly()
    compose.waitForIdle()

    // Initially showing todos
    assertFalse(viewModel.uiState.value.displayEventsPage)

    // Click filter toggle
    compose.onNodeWithTag(MapScreenTestTags.FILTER_TOGGLE, useUnmergedTree = true).performClick()
    compose.waitForIdle()

    // Now showing events
    assertTrue(viewModel.uiState.value.displayEventsPage)
  }

  @Test
  fun dismissing_todo_sheet_clears_selection() {
    renderMapScreenOnly()
    compose.waitForIdle()

    // Select a todo
    viewModel.onSelectedItem(todoId)
    compose.waitForIdle()

    // Verify sheet is open and item is selected
    compose.onNodeWithTag(MapScreenTestTags.TODO_SHEET, useUnmergedTree = true).assertExists()
    assertEquals(todoId, viewModel.uiState.value.selectedItemId)

    // The sheet's onDismissRequest will call clearSelection - simulate swipe down
    viewModel.clearSelection()
    compose.waitForIdle()

    // Verify selection cleared
    assertNull(viewModel.uiState.value.selectedItemId)
  }

  @Test
  fun dismissing_event_sheet_clears_selection() {
    viewModel.changeView()
    renderMapScreenOnly()
    compose.waitForIdle()

    viewModel.onSelectedItem(eventId)
    compose.waitForIdle()

    compose.onNodeWithTag(MapScreenTestTags.EVENT_SHEET, useUnmergedTree = true).assertExists()
    assertEquals(eventId, viewModel.uiState.value.selectedItemId)

    viewModel.clearSelection()
    compose.waitForIdle()

    assertNull(viewModel.uiState.value.selectedItemId)
  }

  @Test
  fun loading_screen_displayed_when_camera_position_null() {
    // Set the viewModel state to null
    viewModel.onNavigationToDifferentScreen()

    // Render the composable, but *tell it not* to run the LaunchedEffect that fixes the state.
    renderMapScreenWithoutInitialisation()

    // The UI is now stable in its loading state.
    compose.onNodeWithTag(MapScreenTestTags.LOADING_SCREEN, useUnmergedTree = true).assertExists()
    compose.onNodeWithTag(MapScreenTestTags.LOADING_SPINNER, useUnmergedTree = true).assertExists()
    compose.onNodeWithTag(MapScreenTestTags.LOADING_TEXT, useUnmergedTree = true).assertExists()

    // Sanity check: Map not displayed
    compose
        .onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN, useUnmergedTree = true)
        .assertDoesNotExist()
  }

  @Test
  fun google_map_not_displayed_when_loading() {
    renderMapScreenWithoutInitialisation()

    compose
        .onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN, useUnmergedTree = true)
        .assertDoesNotExist()
  }

  @Test
  fun filter_toggle_not_displayed_when_loading() {
    renderMapScreenWithoutInitialisation()

    compose
        .onNodeWithTag(MapScreenTestTags.FILTER_TOGGLE, useUnmergedTree = true)
        .assertDoesNotExist()
  }
  /**
   * Tests navigation from the MapScreen to the EventScreen when an event marker is clicked,
   * including the display of the AlertDialog with event details. This test specifically checks for
   * an event the user is not participating in.
   */
  @Test
  fun testMapScreenToEventScreenWithAlertDialog() {
    var navigatedEventId: String? = null
    val isMapScreenActive = mutableStateOf(true)

    val goToEvent: (String) -> Unit = { id ->
      navigatedEventId = id
      isMapScreenActive.value = false
    }

    mockitoUtils = MockitoUtils()

    compose.setContent {
      if (isMapScreenActive.value) {
        viewModel.changeView()
        MapScreen(viewModel = viewModel, goToEvent = goToEvent)
      } else {
        val eventsVM =
            EventsViewModel(
                eventsRepository = eventsRepository,
                profileRepository = profileRepository,
                authProvider = { mockitoUtils.mockAuth })
        EventsScreen(
            eventsViewModel = eventsVM,
            eventId = navigatedEventId,
            onSignedOut = {},
            onAddEvent = {},
            navigateToEditEvent = {})
      }
    }
    compose.waitForIdle()

    viewModel.onSelectedItem(eventId)
    compose.waitForIdle()

    compose.onNodeWithTag(MapScreenTestTags.EVENT_BUTTON, useUnmergedTree = true).performClick()
    compose.waitForIdle()

    assertEquals(eventId, navigatedEventId)
    compose.onNodeWithTag(AlertDialogTestTags.ALERT, useUnmergedTree = true).assertIsDisplayed()

    compose
        .onNodeWithTag(AlertDialogTestTags.TITLE, useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextEquals(event.title)

    compose
        .onNodeWithTag(AlertDialogTestTags.BODY, useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextEquals(event.description)

    compose
        .onNodeWithTag(AlertDialogTestTags.CONFIRM_BTN, useUnmergedTree = true)
        .assertIsDisplayed()
    compose
        .onNodeWithTag(AlertDialogTestTags.DISMISS_BTN, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  /**
   * Tests navigation from the MapScreen to the EventScreen when an event marker is clicked,
   * including the display of the AlertDialog with event details. This test specifically checks for
   * an event the user is participating in.
   */
  @Test
  fun testMapScreenToParticipatingEvent() {
    var navigatedEventId: String? = null
    val isMapScreenActive = mutableStateOf(true)

    val goToEvent: (String) -> Unit = { id ->
      navigatedEventId = id
      isMapScreenActive.value = false
    }
    mockitoUtils = MockitoUtils()
    mockitoUtils.chooseCurrentUser(TEST_USER_ID)

    compose.setContent {
      if (isMapScreenActive.value) {
        viewModel.changeView()
        MapScreen(viewModel = viewModel, goToEvent = goToEvent)
      } else {
        val eventsVM =
            EventsViewModel(
                eventsRepository = eventsRepository,
                profileRepository = profileRepository,
                authProvider = { mockitoUtils.mockAuth })
        EventsScreen(
            eventsViewModel = eventsVM,
            eventId = navigatedEventId,
            onSignedOut = {},
            onAddEvent = {},
            navigateToEditEvent = {})
      }
    }
    compose.waitForIdle()

    viewModel.onSelectedItem(participatingEventId)
    compose.waitForIdle()

    compose.onNodeWithTag(MapScreenTestTags.EVENT_BUTTON, useUnmergedTree = true).performClick()
    compose.waitForIdle()

    assertEquals(participatingEventId, navigatedEventId)
    compose.onNodeWithTag(AlertDialogTestTags.ALERT, useUnmergedTree = true).assertIsDisplayed()

    compose
        .onNodeWithTag(AlertDialogTestTags.BODY, useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextEquals(participatingEvent.description)

    compose
        .onNodeWithTag(AlertDialogTestTags.CONFIRM_BTN, useUnmergedTree = true)
        .assertIsDisplayed()
    compose
        .onNodeWithTag(AlertDialogTestTags.DISMISS_BTN, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  /**
   * Tests navigation from the MapScreen to the EventScreen when an event marker is clicked,
   * including the display of the AlertDialog with event details. This test specifically checks for
   * an event the user created.
   */
  @Test
  fun testMapScreenToCreatingEvent() {
    var navigatedEventId: String? = null
    val isMapScreenActive = mutableStateOf(true)

    val goToEvent: (String) -> Unit = { id ->
      navigatedEventId = id
      isMapScreenActive.value = false
    }
    mockitoUtils = MockitoUtils()
    mockitoUtils.chooseCurrentUser(TEST_USER_ID)

    compose.setContent {
      if (isMapScreenActive.value) {
        viewModel.changeView()
        MapScreen(viewModel = viewModel, goToEvent = goToEvent)
      } else {
        val eventsVM =
            EventsViewModel(
                eventsRepository = eventsRepository,
                profileRepository = profileRepository,
                authProvider = { mockitoUtils.mockAuth })
        EventsScreen(
            eventsViewModel = eventsVM,
            eventId = navigatedEventId,
            onSignedOut = {},
            onAddEvent = {},
            navigateToEditEvent = {})
      }
    }
    compose.waitForIdle()

    viewModel.onSelectedItem(creatingEventId)
    compose.waitForIdle()

    compose.onNodeWithTag(MapScreenTestTags.EVENT_BUTTON, useUnmergedTree = true).performClick()
    compose.waitForIdle()

    assertEquals(creatingEventId, navigatedEventId)
    compose.onNodeWithTag(AlertDialogTestTags.ALERT, useUnmergedTree = true).assertIsDisplayed()

    compose
        .onNodeWithTag(AlertDialogTestTags.BODY, useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextEquals(creatingEvent.description)

    compose
        .onNodeWithTag(AlertDialogTestTags.CONFIRM_BTN, useUnmergedTree = true)
        .assertIsDisplayed()
    compose
        .onNodeWithTag(AlertDialogTestTags.DISMISS_BTN, useUnmergedTree = true)
        .assertIsDisplayed()
  }
}
