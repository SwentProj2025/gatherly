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
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

/**
 * A composable screen displaying ToDos and Events as interactive markers on a Google Map.
 *
 * @param viewModel An optional MapViewModel to manage the UI state, used for testing.
 * @param navigationActions Navigation actions for switching between app sections.
 * @param goToEvent Callback to navigate to the Event detail page.
 * @param goToToDo Callback to navigate to the [ToDo] detail page.
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
  /** Location services setup * */
  val context = LocalContext.current
  val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

  /** ViewModel setup * */
  val vm: MapViewModel =
      viewModel
          ?: viewModel(
              factory =
                  MapViewModel.provideFactory(
                      fusedLocationClient = fusedLocationClient, coordinator = coordinator))

  val uiState by vm.uiState.collectAsState()

  /** Coroutine scope for launching permission requests * */
  val scope = rememberCoroutineScope()

  /** Variable to track location permission status */
  var isLocationPermissionGranted by remember { mutableStateOf(false) }

  /** Handle permission request for location access * */
  val permissionLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
          permissions ->
        val isGranted =
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)

        isLocationPermissionGranted = isGranted

        if (isGranted) {
          vm.startLocationUpdates(context)
        }

        // Run initialization regardless of the result
        // If granted, it tries to fetch user location
        // If denied, it catches the error and falls back to EPFL (Default)
        if (runInitialisation) {
          scope.launch { vm.initialiseCameraPosition(context) }
        }
      }

  /** Check permission and start location updates * */
  LaunchedEffect(Unit) {
    // Check if we already have permissions (Fine OR Coarse)

    val hasPermission = isLocationPermissionGrantedProvider(context)

    if (hasPermission) {
      // Already existing permissions
      // Set the blue dot state
      isLocationPermissionGranted = true

      // Start the location data stream
      vm.startLocationUpdates(context)

      // Initialize Camera
      if (runInitialisation) {
        vm.initialiseCameraPosition(context)
      }
    } else {
      // No initial permission granted

      // Disable blue dot to prevent crash
      isLocationPermissionGranted = false

      // Launch the dialog
      permissionLauncher.launch(
          arrayOf(
              Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }
  }

  /** Stop location updates when the composable is disposed * */
  DisposableEffect(Unit) {
    onDispose {
      vm.onNavigationToDifferentScreen()
      vm.stopLocationUpdates()
    }
  }

  // Bottom sheet state for the selected event
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

  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  Scaffold(
      topBar = {
        TopNavigationMenu(
            selectedTab = Tab.Map,
            onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
            modifier = Modifier.testTag(NavigationTestTags.TOP_NAVIGATION_MENU))
      },
      bottomBar = {
        BottomNavigationMenu(
            selectedTab = Tab.Map,
            onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
            modifier = Modifier.testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU))
      },
      // Toggle button to switch between ToDos and Events
      floatingActionButtonPosition = FabPosition.Start,
      floatingActionButton = {
        if (uiState.cameraPos != null) {
          val isEvents = uiState.displayEventsPage
          ExtendedFloatingActionButton(
              onClick = { vm.changeView() },
              icon = {},
              text = {
                Text(
                    if (uiState.displayEventsPage) stringResource(R.string.show_todos_button_title)
                    else stringResource(R.string.show_events_button_title))
              },
              containerColor =
                  if (isEvents) MaterialTheme.colorScheme.secondary
                  else MaterialTheme.colorScheme.tertiary,
              contentColor =
                  if (isEvents) MaterialTheme.colorScheme.onSecondary
                  else MaterialTheme.colorScheme.onTertiary,
              modifier = Modifier.testTag(MapScreenTestTags.FILTER_TOGGLE))
        }
      },
      content = { pd ->
        if (uiState.cameraPos != null) {
          // Camera position state
          val cameraPositionState = rememberCameraPositionState()

          // Handle nullable cameraPos with ?.let {}
          LaunchedEffect(uiState.cameraPos) {
            uiState.cameraPos?.let { pos ->
              cameraPositionState.position = CameraPosition.fromLatLngZoom(pos, 16f)
            }
          }

          GoogleMap(
              modifier =
                  Modifier.fillMaxSize().padding(pd).testTag(MapScreenTestTags.GOOGLE_MAP_SCREEN),
              cameraPositionState = cameraPositionState,
              onMapClick = { _ -> vm.clearSelection() },
              properties = MapProperties(isMyLocationEnabled = isLocationPermissionGranted),
              uiSettings = MapUiSettings(myLocationButtonEnabled = isLocationPermissionGranted)) {
                uiState.itemsList.forEach { item ->
                  when (item) {
                    // -------------------------------- Todo Marker UI
                    // -------------------------------
                    is ToDo -> {
                      val loc = item.location ?: return@forEach
                      val isExpanded = uiState.selectedItemId == item.uid
                      val z = if (isExpanded) 2f else 1f

                      key("todo_${item.uid}_$isExpanded") {
                        val markerState =
                            rememberMarkerState(position = LatLng(loc.latitude, loc.longitude))

                        MarkerComposable(
                            state = markerState,
                            zIndex = z,
                            onClick = {
                              vm.onSelectedItem(item.uid)
                              true
                            }) {
                              ToDoIcon(item)
                            }
                      }
                    }
                    // -------------------------------- Event Marker UI ---------------------------
                    is Event -> {
                      val loc = item.location ?: return@forEach
                      val isExpanded = uiState.selectedItemId == item.id
                      val z = if (isExpanded) 2f else 1f

                      key("event_${item.id}_$isExpanded") {
                        val markerState =
                            rememberMarkerState(position = LatLng(loc.latitude, loc.longitude))

                        MarkerComposable(
                            state = markerState,
                            zIndex = z,
                            onClick = {
                              vm.onSelectedItem(item.id)
                              true
                            }) {
                              EventIcon(item)
                            }
                      }
                    }
                  }
                }
              }

          if (selectedEvent != null) {
            ModalBottomSheet(sheetState = sheetState, onDismissRequest = { vm.clearSelection() }) {
              EventSheet(
                  event = selectedEvent,
                  onGoToEvent = {
                    // Track consulted item before navigation
                    vm.onItemConsulted(selectedEvent.id)
                    vm.clearSelection()
                    goToEvent(selectedEvent.id)
                  },
                  onClose = { vm.clearSelection() })
            }
          } else if (selectedToDo != null) {
            ModalBottomSheet(sheetState = sheetState, onDismissRequest = { vm.clearSelection() }) {
              ToDoSheet(
                  toDo = selectedToDo,
                  onGoToToDo = {
                    // Track consulted item before navigation
                    vm.onItemConsulted(selectedToDo.uid)
                    vm.clearSelection()
                    goToToDo()
                  },
                  onClose = { vm.clearSelection() })
            }
          }
        } else {
          Box(
              modifier =
                  Modifier.fillMaxSize()
                      .padding(pd)
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
      })
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
 * @param onClose Called when the sheet should be dismissed.
 */
@Composable
fun EventSheet(event: Event, onGoToEvent: () -> Unit, onClose: () -> Unit) {
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
 * @param onClose Called when the sheet should be dismissed.
 */
@Composable
fun ToDoSheet(toDo: ToDo, onGoToToDo: () -> Unit, onClose: () -> Unit) {
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
