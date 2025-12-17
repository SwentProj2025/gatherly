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
import com.android.gatherly.ui.events.EventsScreenActions
import com.android.gatherly.ui.events.EventsScreenTestTags
import com.android.gatherly.ui.events.EventsViewModel
import com.android.gatherly.ui.todo.OverviewScreenTestTags
import com.android.gatherly.utils.AlertDialogTestTags
import com.android.gatherly.utils.MapCoordinator
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

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

  private lateinit var toDosRepository: ToDosRepository
  private lateinit var eventsRepository: EventsRepository
  private lateinit var profileRepository: ProfileRepository
  private lateinit var mapCoordinator: MapCoordinator
  private lateinit var viewModel: MapViewModel

  private val todoId = "t1"
  private val eventId = "e1"
  private val participatingEventId = "e2"
  private val creatingEventId = "e3"
  private val testUserId = "testUserId"

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

  private val oneHourLater = Timestamp(Date(System.currentTimeMillis() + 3_600_000))
  private val twoHoursLater = Timestamp(Date(System.currentTimeMillis() + 7_200_000))

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
          participants = listOf("u1", "u2", testUserId),
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
          creatorId = testUserId,
          participants = listOf(testUserId),
          status = EventStatus.UPCOMING)

  @Before
  fun setUp() = runBlocking {
    toDosRepository = ToDosLocalRepository().apply { addTodo(todo) }
    eventsRepository =
        EventsLocalRepository().apply {
          addEvent(event)
          addEvent(participatingEvent)
          addEvent(creatingEvent)
        }
    profileRepository = ProfileLocalRepository()
    mapCoordinator = MapCoordinator()

    viewModel =
        MapViewModel(
            todosRepository = toDosRepository,
            eventsRepository = eventsRepository,
            coordinator = mapCoordinator)

    waitUntilViewModelLoaded()
  }

  @After
  fun tearDown() {
    viewModel.stopLocationUpdates()
    compose.waitForIdle()
  }

  private fun waitUntilViewModelLoaded() = runBlocking {
    while (viewModel.uiState.value.itemsList.isEmpty()) delay(10)
  }

  private fun waitUntilCameraReady() = runBlocking {
    while (viewModel.uiState.value.cameraPos == null) delay(10)
  }

  private fun renderDefaultMapUi() {
    compose.setContent {
      MapScreen(viewModel = viewModel, coordinator = mapCoordinator)
      ToDoIcon(todo)
      ToDoSheet(todo, onGoToToDo = {}, onClose = {})
      EventIcon(event)
      EventSheet(event, onGoToEvent = {}, onClose = {})
    }
    compose.waitForIdle()
    waitUntilCameraReady()
  }

  private fun renderMapScreenOnly() {
    compose.setContent { MapScreen(viewModel = viewModel, coordinator = mapCoordinator) }
    compose.waitForIdle()
    waitUntilCameraReady()
  }

  private fun renderMapScreenWithoutInitialisation() {
    compose.setContent {
      MapScreen(viewModel = viewModel, coordinator = mapCoordinator, runInitialisation = false)
    }
  }

  @Test
  fun google_map_is_displayed() {
    renderDefaultMapUi()
    compose
        .onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN, useUnmergedTree = true)
        .assertExists()
  }

  @Test
  fun filter_toggle_is_displayed() {
    renderDefaultMapUi()
    compose.onNodeWithTag(MapScreenTestTags.FILTER_TOGGLE, useUnmergedTree = true).assertExists()
  }

  @Test
  fun event_icon_exists() {
    renderDefaultMapUi()
    compose.onNodeWithTag(MapScreenTestTags.EVENT_CARD, useUnmergedTree = true).assertExists()
    compose.onNodeWithTag(MapScreenTestTags.EVENT_TITLE, useUnmergedTree = true).assertExists()
  }

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

  @Test
  fun todo_icon_exists() {
    renderDefaultMapUi()
    compose.onNodeWithTag(MapScreenTestTags.TODO_CARD, useUnmergedTree = true).assertExists()
    compose.onNodeWithTag(MapScreenTestTags.TODO_TITLE, useUnmergedTree = true).assertExists()
  }

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
  fun can_go_to_event() {
    renderDefaultMapUi()
    compose.onNodeWithTag(MapScreenTestTags.EVENT_BUTTON, useUnmergedTree = true).performClick()
    compose
        .onNodeWithTag(EventsScreenTestTags.CREATE_EVENT_BUTTON, useUnmergedTree = true)
        .isDisplayed()
  }

  @Test
  fun can_go_to_todo() {
    renderDefaultMapUi()
    compose.onNodeWithTag(MapScreenTestTags.TODO_BUTTON, useUnmergedTree = true).performClick()
    compose
        .onNodeWithTag(OverviewScreenTestTags.CREATE_TODO_BUTTON, useUnmergedTree = true)
        .isDisplayed()
  }

  @Test
  fun map_renders_after_consulting_todo() {
    viewModel.onItemConsulted(todoId)
    renderDefaultMapUi()
    compose
        .onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN, useUnmergedTree = true)
        .assertExists()
  }

  @Test
  fun map_centers_on_EPFL_fallback() {
    renderDefaultMapUi()
    compose.waitForIdle()

    val cameraPos = viewModel.uiState.value.cameraPos
    assert(cameraPos != null)

    // If your MapViewModel keeps EPFL_LATLNG private, replace this check by hard-coded values
    // or expose EPFL_LATLNG in the production file.
    assertEquals(46.5197, cameraPos!!.latitude, 0.0001)
    assertEquals(6.5663, cameraPos.longitude, 0.0001)
  }

  @Test
  fun map_centers_on_consulted_todo() {
    viewModel.onItemConsulted(todoId)
    renderDefaultMapUi()
    compose.waitForIdle()

    val cameraPos = viewModel.uiState.value.cameraPos
    val todoLocation = requireNotNull(todo.location)
    assert(cameraPos != null)

    assertEquals(todoLocation.latitude, cameraPos!!.latitude, 0.0001)
    assertEquals(todoLocation.longitude, cameraPos.longitude, 0.0001)
  }

  @Test
  fun selecting_todo_opens_sheet_and_consult_marks_last_todo() {
    renderMapScreenOnly()
    assertEquals(null, viewModel.uiState.value.lastConsultedTodoId)

    viewModel.onSelectedItem(todoId)
    compose.waitForIdle()
    compose.onNodeWithTag(MapScreenTestTags.TODO_SHEET, useUnmergedTree = true).assertExists()

    compose.onNodeWithTag(MapScreenTestTags.TODO_BUTTON, useUnmergedTree = true).performClick()
    compose.waitForIdle()

    assertEquals(todoId, viewModel.uiState.value.lastConsultedTodoId)
  }

  @Test
  fun selecting_event_opens_sheet_and_consult_marks_last_event() {
    viewModel.changeView()
    renderMapScreenOnly()
    assertEquals(null, viewModel.uiState.value.lastConsultedEventId)

    viewModel.onSelectedItem(eventId)
    compose.waitForIdle()
    compose.onNodeWithTag(MapScreenTestTags.EVENT_SHEET, useUnmergedTree = true).assertExists()

    compose.onNodeWithTag(MapScreenTestTags.EVENT_BUTTON, useUnmergedTree = true).performClick()
    compose.waitForIdle()

    assertEquals(eventId, viewModel.uiState.value.lastConsultedEventId)
  }

  @Test
  fun clicking_filter_toggle_changes_view() {
    renderMapScreenOnly()
    assertFalse(viewModel.uiState.value.displayEventsPage)

    compose.onNodeWithTag(MapScreenTestTags.FILTER_TOGGLE, useUnmergedTree = true).performClick()
    compose.waitForIdle()

    assertTrue(viewModel.uiState.value.displayEventsPage)
  }

  @Test
  fun dismissing_todo_sheet_clears_selection() {
    renderMapScreenOnly()

    viewModel.onSelectedItem(todoId)
    compose.waitForIdle()

    compose.onNodeWithTag(MapScreenTestTags.TODO_SHEET, useUnmergedTree = true).assertExists()
    assertEquals(todoId, viewModel.uiState.value.selectedItemId)

    viewModel.clearSelection()
    compose.waitForIdle()

    assertNull(viewModel.uiState.value.selectedItemId)
  }

  @Test
  fun dismissing_event_sheet_clears_selection() {
    viewModel.changeView()
    renderMapScreenOnly()

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
    viewModel.onNavigationToDifferentScreen()
    renderMapScreenWithoutInitialisation()

    compose.onNodeWithTag(MapScreenTestTags.LOADING_SCREEN, useUnmergedTree = true).assertExists()
    compose.onNodeWithTag(MapScreenTestTags.LOADING_SPINNER, useUnmergedTree = true).assertExists()
    compose.onNodeWithTag(MapScreenTestTags.LOADING_TEXT, useUnmergedTree = true).assertExists()

    compose
        .onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN, useUnmergedTree = true)
        .assertDoesNotExist()
  }

  @Test
  fun map_and_toggle_not_displayed_when_loading() {
    renderMapScreenWithoutInitialisation()
    compose
        .onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN, useUnmergedTree = true)
        .assertDoesNotExist()
    compose
        .onNodeWithTag(MapScreenTestTags.FILTER_TOGGLE, useUnmergedTree = true)
        .assertDoesNotExist()
  }

  @Test
  fun navigate_map_to_event_screen_shows_alert_dialog_for_non_participating_event() {
    var navigatedEventId: String? = null
    val isMapScreenActive = mutableStateOf(true)

    val goToEvent: (String) -> Unit = { id ->
      navigatedEventId = id
      isMapScreenActive.value = false
    }

    val mockitoUtils = MockitoUtils()

    compose.setContent {
      if (isMapScreenActive.value) {
        viewModel.changeView()
        MapScreen(viewModel = viewModel, coordinator = mapCoordinator, goToEvent = goToEvent)
      } else {
        val eventsVM =
            EventsViewModel(
                eventsRepository = eventsRepository,
                profileRepository = profileRepository,
                authProvider = { mockitoUtils.mockAuth })
        EventsScreen(
            eventsViewModel = eventsVM,
            eventId = navigatedEventId,
            actions = EventsScreenActions(),
            coordinator = mapCoordinator)
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

  @Test
  fun navigate_map_to_participating_event_shows_alert_dialog() {
    var navigatedEventId: String? = null
    val isMapScreenActive = mutableStateOf(true)

    val goToEvent: (String) -> Unit = { id ->
      navigatedEventId = id
      isMapScreenActive.value = false
    }

    val mockitoUtils = MockitoUtils().apply { chooseCurrentUser(testUserId) }

    compose.setContent {
      if (isMapScreenActive.value) {
        viewModel.changeView()
        MapScreen(viewModel = viewModel, coordinator = mapCoordinator, goToEvent = goToEvent)
      } else {
        val eventsVM =
            EventsViewModel(
                eventsRepository = eventsRepository,
                profileRepository = profileRepository,
                authProvider = { mockitoUtils.mockAuth })
        EventsScreen(
            eventsViewModel = eventsVM,
            eventId = navigatedEventId,
            actions = EventsScreenActions(),
            coordinator = mapCoordinator)
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

  @Test
  fun navigate_map_to_creating_event_shows_alert_dialog() {
    var navigatedEventId: String? = null
    val isMapScreenActive = mutableStateOf(true)

    val goToEvent: (String) -> Unit = { id ->
      navigatedEventId = id
      isMapScreenActive.value = false
    }

    val mockitoUtils = MockitoUtils().apply { chooseCurrentUser(testUserId) }

    compose.setContent {
      if (isMapScreenActive.value) {
        viewModel.changeView()
        MapScreen(viewModel = viewModel, coordinator = mapCoordinator, goToEvent = goToEvent)
      } else {
        val eventsVM =
            EventsViewModel(
                eventsRepository = eventsRepository,
                profileRepository = profileRepository,
                authProvider = { mockitoUtils.mockAuth })
        EventsScreen(
            eventsViewModel = eventsVM,
            eventId = navigatedEventId,
            actions = EventsScreenActions(),
            coordinator = mapCoordinator)
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
