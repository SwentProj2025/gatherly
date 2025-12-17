package com.android.gatherly.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.R
import com.android.gatherly.model.event.Event
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.ui.navigation.BottomNavigationMenu
import com.android.gatherly.ui.navigation.NavigationActions
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu
import com.android.gatherly.utils.MapCoordinator
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.launch

// Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the
// SwEnt staff.

/** Test tags for MapScreen. */
object MapScreenTestTags {
  const val GOOGLE_MAP_SCREEN = "mapScreen"
  const val FILTER_TOGGLE = "filterToggle"

  const val TODO_CARD = "todoCard"
  const val EVENT_CARD = "eventCard"
  const val TODO_SHEET = "todoModal"
  const val EVENT_SHEET = "eventModal"
  const val TODO_TITLE = "todoTitle"
  const val TODO_TITLE_SHEET = "todoTitleSheet"

  const val EVENT_BUTTON = "eventButton"
  const val TODO_BUTTON = "todoButton"

  const val EVENT_TITLE = "eventTitle"
  const val EVENT_TITLE_SHEET = "eventTitleSheet"

  const val TODO_DUE_DATE = "todoDueDate"
  const val EVENT_DATE = "eventDate"
  const val TODO_DESCRIPTION = "todoDescription"
  const val EVENT_DESCRIPTION = "eventDescription"
  const val LOADING_SCREEN = "loadingScreen"
  const val LOADING_SPINNER = "loadingSpinner"
  const val LOADING_TEXT = "loadingText"
}

private const val FULL_WIDTH_WEIGHT = 1f
private const val DEFAULT_MAP_ZOOM = 16f
private const val EXPANDED_Z_INDEX = 2f
private const val DEFAULT_Z_INDEX = 1f

/**
 * A composable screen displaying ToDos and Events as interactive markers on a Google Map.
 *
 * @param viewModel An optional MapViewModel to manage the UI state, used for testing.
 * @param navigationActions Navigation actions for switching between app sections.
 * @param goToEvent Callback to navigate to the Event detail page.
 * @param goToToDo Callback to navigate to the [ToDo] detail page.
 * @param runInitialisation Whether the screen should initialise the camera position on start.
 * @param isLocationPermissionGrantedProvider Function used to check whether location permission is
 *   granted.
 * @param coordinator Coordinator used to pass one-shot navigation requests to the map (required).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel? = null,
    navigationActions: NavigationActions? = null,
    goToEvent: (String) -> Unit = {},
    goToToDo: () -> Unit = {},
    runInitialisation: Boolean = true,
    isLocationPermissionGrantedProvider: (Context) -> Boolean = { ctx ->
      (ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) ==
          PackageManager.PERMISSION_GRANTED) ||
          (ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) ==
              PackageManager.PERMISSION_GRANTED)
    },
    coordinator: MapCoordinator
) {
  val context = LocalContext.current
  val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

  val vm: MapViewModel =
      viewModel
          ?: viewModel(
              factory =
                  MapViewModel.provideFactory(
                      fusedLocationClient = fusedLocationClient, coordinator = coordinator))

  val uiState by vm.uiState.collectAsState()

  var isLocationPermissionGranted by remember { mutableStateOf(false) }

  LocationPermissionEffect(
      context = context,
      viewModel = vm,
      runInitialisation = runInitialisation,
      isLocationPermissionGrantedProvider = isLocationPermissionGrantedProvider,
      onPermissionStateChanged = { isLocationPermissionGranted = it },
  )

  DisposableEffect(Unit) {
    onDispose {
      vm.onNavigationToDifferentScreen()
      vm.stopLocationUpdates()
    }
  }

  Scaffold(
      topBar = { MapTopBar(navigationActions) },
      bottomBar = { MapBottomBar(navigationActions) },
      floatingActionButtonPosition = FabPosition.Start,
      floatingActionButton = {
        MapToggleFab(
            isVisible = uiState.cameraPos != null,
            isEvents = uiState.displayEventsPage,
            onToggle = { vm.changeView() })
      },
      content = { pd ->
        MapScreenContent(
            modifier = Modifier.padding(pd),
            uiState = uiState,
            isLocationPermissionGranted = isLocationPermissionGranted,
            onClearSelection = { vm.clearSelection() },
            onSelectItem = { vm.onSelectedItem(it) },
            onGoToEvent = { eventId ->
              vm.onItemConsulted(eventId)
              vm.clearSelection()
              goToEvent(eventId)
            },
            onGoToToDo = { todoId ->
              vm.onItemConsulted(todoId)
              vm.clearSelection()
              goToToDo()
            },
        )
      })
}

/**
 * Handles location permission request flow and location initialisation side effects.
 *
 * @param context Android context used for permission checks and location updates.
 * @param viewModel ViewModel used to start location updates and initialise the camera.
 * @param runInitialisation Whether camera initialisation should be triggered.
 * @param isLocationPermissionGrantedProvider Function used to check whether location permission is
 *   granted.
 * @param onPermissionStateChanged Callback invoked whenever the permission state changes.
 */
@Composable
private fun LocationPermissionEffect(
    context: Context,
    viewModel: MapViewModel,
    runInitialisation: Boolean,
    isLocationPermissionGrantedProvider: (Context) -> Boolean,
    onPermissionStateChanged: (Boolean) -> Unit,
) {
  val scope = rememberCoroutineScope()

  val runInit: () -> Unit =
      remember(runInitialisation, viewModel, context) {
        {
          if (runInitialisation) {
            viewModel.initialiseCameraPosition(context)
          }
        }
      }

  val onGranted: () -> Unit =
      remember(viewModel, context) { { viewModel.startLocationUpdates(context) } }

  val permissionLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
          permissions ->
        val isGranted =
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)

        onPermissionStateChanged(isGranted)
        if (isGranted) onGranted()

        scope.launch { runInit() }
      }

  LaunchedEffect(Unit) {
    val hasPermission = isLocationPermissionGrantedProvider(context)
    onPermissionStateChanged(hasPermission)

    if (hasPermission) {
      onGranted()
      runInit()
    } else {
      permissionLauncher.launch(
          arrayOf(
              Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }
  }
}

/**
 * Top app navigation bar for the map screen.
 *
 * @param navigationActions Navigation actions for switching between app sections.
 */
@Composable
private fun MapTopBar(navigationActions: NavigationActions?) {
  TopNavigationMenu(
      selectedTab = Tab.Map,
      onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
      modifier = Modifier.testTag(NavigationTestTags.TOP_NAVIGATION_MENU))
}

/**
 * Bottom app navigation bar for the map screen.
 *
 * @param navigationActions Navigation actions for switching between app sections.
 */
@Composable
private fun MapBottomBar(navigationActions: NavigationActions?) {
  BottomNavigationMenu(
      selectedTab = Tab.Map,
      onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
      modifier = Modifier.testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU))
}

/**
 * Floating action button to toggle between ToDos and Events.
 *
 * @param isVisible Whether the FAB should be displayed.
 * @param isEvents Whether the current mode is Events mode.
 * @param onToggle Callback invoked when user taps the toggle.
 */
@Composable
private fun MapToggleFab(isVisible: Boolean, isEvents: Boolean, onToggle: () -> Unit) {
  if (!isVisible) return

  ExtendedFloatingActionButton(
      onClick = onToggle,
      icon = {},
      text = {
        Text(
            if (isEvents) stringResource(R.string.show_todos_button_title)
            else stringResource(R.string.show_events_button_title))
      },
      containerColor =
          if (isEvents) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary,
      contentColor =
          if (isEvents) MaterialTheme.colorScheme.onSecondary
          else MaterialTheme.colorScheme.onTertiary,
      modifier = Modifier.testTag(MapScreenTestTags.FILTER_TOGGLE))
}

/**
 * Main content area for the map screen.
 *
 * @param modifier Modifier holding scaffold padding.
 * @param uiState Current UI state from the ViewModel.
 * @param isLocationPermissionGranted Whether location permission is granted (enables blue dot).
 * @param onClearSelection Callback invoked when selection should be cleared.
 * @param onSelectItem Callback invoked when a marker is tapped.
 * @param onGoToEvent Callback invoked when user taps "Go to event".
 * @param onGoToToDo Callback invoked when user taps "Go to todo".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MapScreenContent(
    modifier: Modifier,
    uiState: UIState,
    isLocationPermissionGranted: Boolean,
    onClearSelection: () -> Unit,
    onSelectItem: (String) -> Unit,
    onGoToEvent: (String) -> Unit,
    onGoToToDo: (String) -> Unit,
) {
  if (uiState.cameraPos == null) {
    LoadingContent(modifier)
    return
  }

  val selectedEvent =
      remember(uiState.selectedItemId, uiState.itemsList) {
        uiState.itemsList.asSequence().filterIsInstance<Event>().firstOrNull {
          it.id == uiState.selectedItemId
        }
      }

  val selectedToDo =
      remember(uiState.selectedItemId, uiState.itemsList) {
        uiState.itemsList.asSequence().filterIsInstance<ToDo>().firstOrNull {
          it.uid == uiState.selectedItemId
        }
      }

  // Created here to reduce parameter count and keep state local to the content.
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  MapWithMarkers(
      modifier = Modifier.fillMaxSize().then(modifier).testTag(MapScreenTestTags.GOOGLE_MAP_SCREEN),
      cameraTarget = uiState.cameraPos,
      isMyLocationEnabled = isLocationPermissionGranted,
      items = uiState.itemsList,
      selectedItemId = uiState.selectedItemId,
      onMapClick = onClearSelection,
      onSelectItem = onSelectItem,
  )

  SelectedItemSheets(
      selectedEvent = selectedEvent,
      selectedToDo = selectedToDo,
      sheetState = sheetState,
      onDismiss = onClearSelection,
      onGoToEvent = onGoToEvent,
      onGoToToDo = onGoToToDo,
  )
}

/**
 * Loading state content shown while camera position is not initialised.
 *
 * @param modifier Modifier carrying scaffold padding and sizing.
 */
@Composable
private fun LoadingContent(modifier: Modifier) {
  Box(
      modifier =
          Modifier.fillMaxSize()
              .then(modifier)
              .background(MaterialTheme.colorScheme.background)
              .testTag(MapScreenTestTags.LOADING_SCREEN),
      contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          CircularProgressIndicator(
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.testTag(MapScreenTestTags.LOADING_SPINNER))
          Spacer(modifier = Modifier.height(dimensionResource(R.dimen.map_loading_spacer)))
          Text(
              text = stringResource(R.string.loading_map),
              color = MaterialTheme.colorScheme.onBackground,
              style = MaterialTheme.typography.bodyLarge,
              modifier = Modifier.testTag(MapScreenTestTags.LOADING_TEXT))
        }
      }
}

/**
 * Google Map composable with markers rendered from the provided items.
 *
 * @param modifier Modifier applied to the GoogleMap.
 * @param cameraTarget Initial camera target to center the map on.
 * @param isMyLocationEnabled Whether "my location" blue dot is enabled.
 * @param items List of items to render as markers (Events and ToDos).
 * @param selectedItemId Currently selected item id (controls zIndex).
 * @param onMapClick Callback invoked when map background is tapped.
 * @param onSelectItem Callback invoked when a marker is tapped.
 */
@Composable
private fun MapWithMarkers(
    modifier: Modifier,
    cameraTarget: LatLng?,
    isMyLocationEnabled: Boolean,
    items: List<com.android.gatherly.model.map.DisplayedMapElement>,
    selectedItemId: String?,
    onMapClick: () -> Unit,
    onSelectItem: (String) -> Unit,
) {
  val cameraPositionState = rememberCameraPositionState()

  LaunchedEffect(cameraTarget) {
    cameraTarget?.let { pos ->
      cameraPositionState.position = CameraPosition.fromLatLngZoom(pos, DEFAULT_MAP_ZOOM)
    }
  }

  GoogleMap(
      modifier = modifier,
      cameraPositionState = cameraPositionState,
      onMapClick = { onMapClick() },
      properties = MapProperties(isMyLocationEnabled = isMyLocationEnabled),
      uiSettings = MapUiSettings(myLocationButtonEnabled = isMyLocationEnabled)) {
        MapMarkers(items = items, selectedItemId = selectedItemId, onSelectItem = onSelectItem)
      }
}

/**
 * Renders markers for Events and ToDos.
 *
 * @param items List of displayed map elements (Events and ToDos).
 * @param selectedItemId Currently selected item id.
 * @param onSelectItem Callback invoked when a marker is tapped.
 */
@Composable
private fun MapMarkers(
    items: List<com.android.gatherly.model.map.DisplayedMapElement>,
    selectedItemId: String?,
    onSelectItem: (String) -> Unit,
) {
  items.forEach { item ->
    when (item) {
      is ToDo ->
          ToDoMarker(item = item, isSelected = selectedItemId == item.uid, onSelect = onSelectItem)
      is Event ->
          EventMarker(item = item, isSelected = selectedItemId == item.id, onSelect = onSelectItem)
    }
  }
}

/**
 * Marker UI for a ToDo item.
 *
 * @param item ToDo item to display as a marker.
 * @param isSelected Whether this marker is currently selected.
 * @param onSelect Callback invoked when the marker is tapped.
 */
@Composable
private fun ToDoMarker(item: ToDo, isSelected: Boolean, onSelect: (String) -> Unit) {
  val loc = item.location ?: return
  val z = if (isSelected) EXPANDED_Z_INDEX else DEFAULT_Z_INDEX

  key("todo_${item.uid}_$isSelected") {
    val markerState = rememberMarkerState(position = LatLng(loc.latitude, loc.longitude))
    MarkerComposable(
        state = markerState,
        zIndex = z,
        onClick = {
          onSelect(item.uid)
          true
        }) {
          ToDoIcon(item)
        }
  }
}

/**
 * Marker UI for an Event item.
 *
 * @param item Event item to display as a marker.
 * @param isSelected Whether this marker is currently selected.
 * @param onSelect Callback invoked when the marker is tapped.
 */
@Composable
private fun EventMarker(item: Event, isSelected: Boolean, onSelect: (String) -> Unit) {
  val loc = item.location ?: return
  val z = if (isSelected) EXPANDED_Z_INDEX else DEFAULT_Z_INDEX

  key("event_${item.id}_$isSelected") {
    val markerState = rememberMarkerState(position = LatLng(loc.latitude, loc.longitude))
    MarkerComposable(
        state = markerState,
        zIndex = z,
        onClick = {
          onSelect(item.id)
          true
        }) {
          EventIcon(item)
        }
  }
}

/**
 * Bottom sheets for selected items.
 *
 * @param selectedEvent Selected event if present.
 * @param selectedToDo Selected todo if present.
 * @param sheetState State object for the bottom sheet.
 * @param onDismiss Callback invoked when a sheet is dismissed.
 * @param onGoToEvent Callback invoked when user navigates to event.
 * @param onGoToToDo Callback invoked when user navigates to todo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectedItemSheets(
    selectedEvent: Event?,
    selectedToDo: ToDo?,
    sheetState: androidx.compose.material3.SheetState,
    onDismiss: () -> Unit,
    onGoToEvent: (String) -> Unit,
    onGoToToDo: (String) -> Unit,
) {
  when {
    selectedEvent != null -> {
      ModalBottomSheet(sheetState = sheetState, onDismissRequest = onDismiss) {
        EventSheet(event = selectedEvent, onGoToEvent = { onGoToEvent(selectedEvent.id) })
      }
    }
    selectedToDo != null -> {
      ModalBottomSheet(sheetState = sheetState, onDismissRequest = onDismiss) {
        ToDoSheet(toDo = selectedToDo, onGoToToDo = { onGoToToDo(selectedToDo.uid) })
      }
    }
  }
}

// -------------------------------- Event icons --------------------------------
/**
 * Pin-style marker icon for an [Event].
 *
 * Uses allowed design-system colors: surfaceVariant/onSurfaceVariant for marker containers. If you
 * want events to differ from todos, keep that difference in the text style only.
 *
 * @param event The [Event] displayed by this marker.
 * @param scale Optional scaling factor (for future zoom-based sizing).
 */
@Composable
fun EventIcon(event: Event, scale: Float = 1f) {
  val containerColor = MaterialTheme.colorScheme.tertiary
  val contentColor = MaterialTheme.colorScheme.onTertiary
  val borderColor = contentColor.copy(alpha = 0.5f)

  PinMarkerIcon(
      title = event.title,
      containerColor = containerColor,
      contentColor = contentColor,
      borderColor = borderColor,
      cardTestTag = MapScreenTestTags.EVENT_CARD,
      titleTestTag = MapScreenTestTags.EVENT_TITLE,
      scale = scale,
  )
}

/**
 * Bottom sheet shown when an [Event] marker is tapped.
 *
 * @param event Selected [Event].
 * @param onGoToEvent Called when the CTA button is pressed.
 */
@Composable
fun EventSheet(event: Event, onGoToEvent: () -> Unit) {
  val datePattern = stringResource(R.string.map_date_format)
  val formattedDate =
      remember(event.date, datePattern) {
        SimpleDateFormat(datePattern, Locale.getDefault()).format(event.date.toDate())
      }

  val sheetPadding = dimensionResource(R.dimen.map_sheet_padding)
  val textPadding = dimensionResource(R.dimen.map_sheet_text_padding)
  val spacer = dimensionResource(R.dimen.map_sheet_spacer)

  Column(
      modifier =
          Modifier.fillMaxWidth().padding(sheetPadding).testTag(MapScreenTestTags.EVENT_SHEET)) {
        Text(
            text = event.title.uppercase(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.testTag(MapScreenTestTags.EVENT_TITLE_SHEET))

        Spacer(modifier = Modifier.height(spacer))

        Text(
            modifier = Modifier.padding(textPadding).testTag(MapScreenTestTags.EVENT_DATE),
            text = formattedDate,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground)

        Text(
            modifier = Modifier.padding(textPadding).testTag(MapScreenTestTags.EVENT_DESCRIPTION),
            text = event.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground)

        Spacer(modifier = Modifier.height(spacer))

        Row(
            modifier = Modifier.padding(sheetPadding),
            verticalAlignment = Alignment.CenterVertically) {
              Button(
                  modifier =
                      Modifier.weight(FULL_WIDTH_WEIGHT).testTag(MapScreenTestTags.EVENT_BUTTON),
                  onClick = onGoToEvent,
                  colors =
                      ButtonDefaults.buttonColors(
                          containerColor = MaterialTheme.colorScheme.tertiary,
                          contentColor = MaterialTheme.colorScheme.onTertiary,
                      )) {
                    Text(
                        text = stringResource(R.string.go_to_event_page_button),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium)
                  }
            }

        Spacer(modifier = Modifier.height(spacer))
      }
}

/** -------------------------------- [ToDo] icons -------------------------------- * */

/**
 * Pin-style marker icon for a [ToDo].
 *
 * Uses allowed design-system colors: surfaceVariant/onSurfaceVariant.
 *
 * @param toDo The [ToDo] displayed by this marker.
 * @param scale Optional scaling factor (for future zoom-based sizing).
 */
@Composable
fun ToDoIcon(toDo: ToDo, scale: Float = 1f) {
  val containerColor = MaterialTheme.colorScheme.secondary
  val contentColor = MaterialTheme.colorScheme.onSecondary
  val borderColor = contentColor.copy(alpha = 0.5f)

  PinMarkerIcon(
      title = toDo.name,
      containerColor = containerColor,
      contentColor = contentColor,
      borderColor = borderColor,
      cardTestTag = MapScreenTestTags.TODO_CARD,
      titleTestTag = MapScreenTestTags.TODO_TITLE,
      scale = scale,
  )
}

/**
 * Bottom sheet shown when a [ToDo] marker is tapped.
 *
 * @param toDo Selected [ToDo].
 * @param onGoToToDo Called when the CTA button is pressed.
 */
@Composable
fun ToDoSheet(toDo: ToDo, onGoToToDo: () -> Unit) {
  val datePattern = stringResource(R.string.map_date_format)
  val formattedDate =
      remember(toDo.dueDate, datePattern) {
        toDo.dueDate
            ?.let { date ->
              SimpleDateFormat(datePattern, Locale.getDefault()).format(date.toDate())
            }
            .orEmpty()
      }

  val sheetPadding = dimensionResource(R.dimen.map_sheet_padding)
  val textPadding = dimensionResource(R.dimen.map_sheet_text_padding)
  val spacer = dimensionResource(R.dimen.map_sheet_spacer)

  Column(
      modifier =
          Modifier.fillMaxWidth().padding(sheetPadding).testTag(MapScreenTestTags.TODO_SHEET)) {
        Text(
            text = toDo.name.uppercase(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.testTag(MapScreenTestTags.TODO_TITLE_SHEET))

        Spacer(modifier = Modifier.height(spacer))

        Text(
            modifier = Modifier.padding(textPadding).testTag(MapScreenTestTags.TODO_DUE_DATE),
            text = formattedDate,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground)

        Text(
            modifier = Modifier.padding(textPadding).testTag(MapScreenTestTags.TODO_DESCRIPTION),
            text = toDo.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground)

        Spacer(modifier = Modifier.height(spacer))

        Row(
            modifier = Modifier.padding(sheetPadding),
            verticalAlignment = Alignment.CenterVertically) {
              Button(
                  modifier =
                      Modifier.weight(FULL_WIDTH_WEIGHT).testTag(MapScreenTestTags.TODO_BUTTON),
                  onClick = onGoToToDo,
                  colors =
                      ButtonDefaults.buttonColors(
                          containerColor = MaterialTheme.colorScheme.secondary,
                          contentColor = MaterialTheme.colorScheme.onSecondary,
                      )) {
                    Text(
                        text = stringResource(R.string.go_to_todo_page_button),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium)
                  }
            }

        Spacer(modifier = Modifier.height(spacer))
      }
}

/**
 * Reusable pin-style marker UI for Google Maps markers.
 *
 * Uses dimens resources to avoid magic numbers and keep sizing consistent across markers.
 *
 * @param title Text shown inside the marker (uppercased).
 * @param containerColor Background of the pill + filled triangle.
 * @param contentColor Text color inside the pill.
 * @param borderColor Border stroke color (pill + triangle border).
 * @param cardTestTag Test tag applied to the marker container.
 * @param titleTestTag Test tag applied to the title [Text].
 * @param scale Optional multiplier applied to the marker sizes.
 */
@Composable
private fun PinMarkerIcon(
    title: String,
    containerColor: Color,
    contentColor: Color,
    borderColor: Color,
    cardTestTag: String,
    titleTestTag: String,
    scale: Float = 1f,
) {
  val markerWidth = dimensionResource(R.dimen.map_marker_width) * scale
  val markerHeight = dimensionResource(R.dimen.map_marker_height_collapsed) * scale
  val cornerRadius = dimensionResource(R.dimen.map_marker_corner_radius) * scale
  val elevation = dimensionResource(R.dimen.map_marker_elevation) * scale
  val borderWidth = dimensionResource(R.dimen.map_marker_border_width) * scale
  val triW = dimensionResource(R.dimen.map_marker_triangle_width) * scale
  val triH = dimensionResource(R.dimen.map_marker_triangle_height) * scale
  val padH = dimensionResource(R.dimen.map_marker_horizontal_padding) * scale
  val padV = dimensionResource(R.dimen.map_marker_vertical_padding) * scale

  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.testTag(cardTestTag)) {
        Card(
            border = BorderStroke(borderWidth, borderColor),
            shape = RoundedCornerShape(cornerRadius),
            colors =
                CardDefaults.cardColors(
                    containerColor = containerColor, contentColor = contentColor),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation),
            modifier = Modifier.size(width = markerWidth, height = markerHeight),
        ) {
          Column(
              modifier = Modifier.padding(horizontal = padH, vertical = padV),
              horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Text(
                text = title.uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = contentColor,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth().testTag(titleTestTag),
            )
          }
        }

        Canvas(modifier = Modifier.size(width = triW, height = triH)) {
          val borderPath =
              Path().apply {
                moveTo(size.width / 2f, size.height)
                lineTo(0f, 0f)
                lineTo(size.width, 0f)
                close()
              }
          drawPath(borderPath, color = borderColor)

          val inset = borderWidth.toPx()
          val fillPath =
              Path().apply {
                moveTo(size.width / 2f, size.height - inset)
                lineTo(inset, 0f)
                lineTo(size.width - inset, 0f)
                close()
              }
          drawPath(fillPath, color = containerColor)
        }
      }
}
