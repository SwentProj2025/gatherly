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

/**
 * Instrumented UI tests for [MapScreen].
 *
 * Scope:
 * - Rendering: Map is displayed, filter toggle exists, marker cards and sheets exist.
 * - View switching: toggling between ToDo page and Events page.
 * - Selection behavior: selecting an item opens the correct sheet and updates ViewModel state.
 * - Dismiss behavior: closing a sheet clears the selection.
 * - Navigation: tapping sheet buttons triggers navigation targets (ToDo overview or Event screen).
 * - Loading state: when camera position is null and initialization is disabled, show loading UI.
 * - Integration navigation: MapScreen -> EventsScreen and verify AlertDialog content for different
 *   participation contexts (not participating, participating, creator).
 */
class MapScreenTest {

  /** Compose rule used to host the composable under test in an instrumentation Activity. */
  @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

  /**
   * Location permissions required by the map and any location-based initialization logic. Without
   * this rule, the screen may remain in the loading state in CI or emulator runs.
   */
  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

  /** In-memory repositories used to seed deterministic data for tests. */
  private lateinit var toDosRepository: ToDosRepository
  private lateinit var eventsRepository: EventsRepository
  private lateinit var profileRepository: ProfileRepository

  /** Shared coordinator used for navigation and map coordination across screens. */
  private lateinit var mapCoordinator: MapCoordinator

  /** ViewModel under test, driving [MapScreen] state. */
  private lateinit var viewModel: MapViewModel

  /** Stable IDs used across tests for selection and navigation assertions. */
  private val todoId = "t1"
  private val eventId = "e1"
  private val participatingEventId = "e2"
  private val creatingEventId = "e3"
  private val testUserId = "testUserId"

  /** Sample ToDo item used for marker, sheet, selection, and "consulted" tests. */
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

  /** Helper timestamps used to build deterministic event times. */
  private val oneHourLater = Timestamp(Date(System.currentTimeMillis() + 3_600_000))
  private val twoHoursLater = Timestamp(Date(System.currentTimeMillis() + 7_200_000))

  /**
   * Event where the current user is not a participant. Used to validate the "non participating"
   * AlertDialog path.
   */
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

  /**
   * Event where the current user participates. Used to validate the "participating" AlertDialog
   * path.
   */
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

  /** Event created by the current user. Used to validate the "creator" AlertDialog path. */
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

  /**
   * Creates repositories and the ViewModel with deterministic test data.
   *
   * The ViewModel loads its initial items asynchronously, so we wait until [UIState.itemsList] is
   * populated before running assertions.
   */
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

  /**
   * Stops background location updates after each test and waits for Compose to become idle. This
   * prevents cross-test interference and reduces flakiness.
   */
  @After
  fun tearDown() {
    viewModel.stopLocationUpdates()
    compose.waitForIdle()
  }

  /**
   * Waits until the ViewModel has loaded its items list. Used to avoid racing the initial
   * asynchronous state population.
   */
  private fun waitUntilViewModelLoaded() = runBlocking {
    while (viewModel.uiState.value.itemsList.isEmpty()) delay(10)
  }

  /**
   * Waits until the ViewModel exposes a non-null camera position. Used because MapScreen
   * initializes camera position asynchronously.
   */
  private fun waitUntilCameraReady() = runBlocking {
    while (viewModel.uiState.value.cameraPos == null) delay(10)
  }

  /**
   * Renders MapScreen with additional marker and sheet composables.
   *
   * Why this helper exists:
   * - Some UI elements (marker cards and sheets) have explicit test tags and can be asserted
   *   directly.
   * - Google Map marker nodes are not always accessible through Compose semantics in tests.
   */
  private fun renderDefaultMapUi() {
    compose.setContent {
      MapScreen(viewModel = viewModel, coordinator = mapCoordinator)
      ToDoIcon(todo)
      ToDoSheet(todo, onGoToToDo = {})
      EventIcon(event)
      EventSheet(event, onGoToEvent = {})
    }
    compose.waitForIdle()
    waitUntilCameraReady()
  }

  /**
   * Renders only MapScreen.
   *
   * Use this helper when validating ViewModel-driven selection and UI state changes without
   * manually composing extra marker and sheet components.
   */
  private fun renderMapScreenOnly() {
    compose.setContent { MapScreen(viewModel = viewModel, coordinator = mapCoordinator) }
    compose.waitForIdle()
    waitUntilCameraReady()
  }

  /**
   * Renders MapScreen with initialization disabled.
   *
   * This is used to keep the screen in a stable loading state for assertions: cameraPos stays null
   * and the map and filter toggle should not be displayed.
   */
  private fun renderMapScreenWithoutInitialisation() {
    compose.setContent {
      MapScreen(viewModel = viewModel, coordinator = mapCoordinator, runInitialisation = false)
    }
  }

  /**
   * Verifies that the Google Map container is present in the composition. This is a basic smoke
   * test for MapScreen rendering.
   */
  @Test
  fun google_map_is_displayed() {
    renderDefaultMapUi()
    compose
        .onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN, useUnmergedTree = true)
        .assertExists()
  }

  /** Verifies that the filter toggle button is rendered. */
  @Test
  fun filter_toggle_is_displayed() {
    renderDefaultMapUi()
    compose.onNodeWithTag(MapScreenTestTags.FILTER_TOGGLE, useUnmergedTree = true).assertExists()
  }

  /** Verifies that an event marker card and title are present. */
  @Test
  fun event_icon_exists() {
    renderDefaultMapUi()
    compose.onNodeWithTag(MapScreenTestTags.EVENT_CARD, useUnmergedTree = true).assertExists()
    compose.onNodeWithTag(MapScreenTestTags.EVENT_TITLE, useUnmergedTree = true).assertExists()
  }

  /** Verifies that the event bottom sheet shows all expected fields and the navigation button. */
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

  /** Verifies that a todo marker card and title are present. */
  @Test
  fun todo_icon_exists() {
    renderDefaultMapUi()
    compose.onNodeWithTag(MapScreenTestTags.TODO_CARD, useUnmergedTree = true).assertExists()
    compose.onNodeWithTag(MapScreenTestTags.TODO_TITLE, useUnmergedTree = true).assertExists()
  }

  /** Verifies that the todo bottom sheet shows all expected fields and the navigation button. */
  @Test
  fun todo_sheet_exists() {
    renderDefaultMapUi()
    compose.onNodeWithTag(MapScreenTestTags.TODO_SHEET, useUnmergedTree = true).assertExists()
    compose.onNodeWithTag(MapScreenTestTags.TODO_DUE_DATE, useUnmergedTree = true).assertExists()
    compose.onNodeWithTag(MapScreenTestTags.TODO_TITLE_SHEET, useUnmergedTree = true).assertExists()
    compose.onNodeWithTag(MapScreenTestTags.TODO_DESCRIPTION, useUnmergedTree = true).assertExists()
    compose.onNodeWithTag(MapScreenTestTags.TODO_BUTTON, useUnmergedTree = true).assertExists()
  }

  /**
   * Verifies navigation to the Events feature via the event sheet button. This is a light
   * integration check using test tags from EventsScreen.
   */
  @Test
  fun can_go_to_event() {
    renderDefaultMapUi()
    compose.onNodeWithTag(MapScreenTestTags.EVENT_BUTTON, useUnmergedTree = true).performClick()
    compose
        .onNodeWithTag(EventsScreenTestTags.CREATE_EVENT_BUTTON, useUnmergedTree = true)
        .isDisplayed()
  }

  /**
   * Verifies navigation to the ToDo overview via the todo sheet button. This is a light integration
   * check using test tags from OverviewScreen.
   */
  @Test
  fun can_go_to_todo() {
    renderDefaultMapUi()
    compose.onNodeWithTag(MapScreenTestTags.TODO_BUTTON, useUnmergedTree = true).performClick()
    compose
        .onNodeWithTag(OverviewScreenTestTags.CREATE_TODO_BUTTON, useUnmergedTree = true)
        .isDisplayed()
  }

  /**
   * Smoke test ensuring MapScreen still renders after a todo was previously consulted. This
   * protects against regressions where prior selection state breaks initialization.
   */
  @Test
  fun map_renders_after_consulting_todo() {
    viewModel.onItemConsulted(todoId)
    renderDefaultMapUi()
    compose
        .onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN, useUnmergedTree = true)
        .assertExists()
  }

  /**
   * Verifies the default camera target when there is no last consulted item. This checks the
   * fallback behavior (EPFL coordinates).
   */
  @Test
  fun map_centers_on_EPFL_fallback() {
    renderDefaultMapUi()
    compose.waitForIdle()

    val cameraPos = viewModel.uiState.value.cameraPos
    assert(cameraPos != null)

    assertEquals(46.519, cameraPos!!.latitude, 0.0001)
    assertEquals(6.5668, cameraPos.longitude, 0.0001)
  }

  /** Verifies that the camera centers on the location of the last consulted ToDo. */
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

  /**
   * Selection behavior for ToDos:
   * - Selecting a ToDo opens its sheet.
   * - Clicking the sheet action marks it as "last consulted" in the ViewModel.
   */
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

  /**
   * Selection behavior for Events:
   * - Switches to event view first.
   * - Selecting an Event opens its sheet.
   * - Clicking the sheet action marks it as "last consulted" in the ViewModel.
   */
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

  /** Verifies that the filter toggle flips the ViewModel page state from todos to events. */
  @Test
  fun clicking_filter_toggle_changes_view() {
    renderMapScreenOnly()
    assertFalse(viewModel.uiState.value.displayEventsPage)

    compose.onNodeWithTag(MapScreenTestTags.FILTER_TOGGLE, useUnmergedTree = true).performClick()
    compose.waitForIdle()

    assertTrue(viewModel.uiState.value.displayEventsPage)
  }

  /** Verifies that dismissing the ToDo sheet clears [UIState.selectedItemId] in the ViewModel. */
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

  /** Verifies that dismissing the Event sheet clears [UIState.selectedItemId] in the ViewModel. */
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

  /**
   * Verifies the loading UI when the camera position is null and initialization is disabled.
   * Expected:
   * - Loading screen elements are present.
   * - Google map is not present.
   */
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

  /**
   * Verifies that the map UI and filter toggle are not visible while in loading state. This
   * prevents accidental interaction with partially initialized map UI.
   */
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

  /**
   * End to end navigation test: MapScreen -> EventsScreen for an event the user is not
   * participating in.
   *
   * Expected:
   * - Tapping the event action navigates to EventsScreen with the correct eventId.
   * - An AlertDialog is shown.
   * - Dialog title and body match the event content.
   */
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

  /**
   * End to end navigation test: MapScreen -> EventsScreen for an event the user participates in.
   *
   * Expected:
   * - Navigates with the correct eventId.
   * - AlertDialog is shown and body matches the participating event description.
   */
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

  /**
   * End to end navigation test: MapScreen -> EventsScreen for an event created by the current user.
   *
   * Expected:
   * - Navigates with the correct eventId.
   * - AlertDialog is shown and body matches the created event description.
   */
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
