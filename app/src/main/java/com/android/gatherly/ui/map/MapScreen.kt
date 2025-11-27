package com.android.gatherly.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.R
import com.android.gatherly.model.event.Event
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.ui.navigation.BottomNavigationMenu
import com.android.gatherly.ui.navigation.HandleSignedOutState
import com.android.gatherly.ui.navigation.NavigationActions
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu
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

  // ToDo markers
  fun todoMarker(id: String) = "todoMarker_$id"

  fun todoMarkerExpanded(id: String) = "todoMarkerExpanded_$id"

  // Event markers
  fun eventMarker(id: String) = "eventMarker_$id"
}

/** Dimension constants to avoid magic numbers. */
private object Dimensions {
  val markerWidth = 180.dp
  val markerHeightCollapsed = 50.dp
  val cardPadding = 12.dp

  val spacerPadding = 20.dp

  val textPadding = 8.dp
  val rowColPadding = 16.dp

  val weight = 1f
}

/**
 * A composable screen displaying ToDos and Events as interactive markers on a Google Map.
 *
 * @param viewModel An optional MapViewModel to manage the UI state, used for testing.
 * @param credentialManager The CredentialManager for handling user sign-out.
 * @param onSignedOut Callback invoked when the user signs out.
 * @param navigationActions Navigation actions for switching between app sections.
 * @param goToEvent Callback to navigate to the Event detail page.
 * @param goToToDo Callback to navigate to the [ToDo] detail page.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel? = null,
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current),
    onSignedOut: () -> Unit = {},
    navigationActions: NavigationActions? = null,
    goToEvent: (String) -> Unit = {},
    goToToDo: () -> Unit = {},
    runInitialisation: Boolean = true,
    isLocationPermissionGrantedProvider: (Context) -> Boolean = { ctx ->
      (ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) ==
          PackageManager.PERMISSION_GRANTED) ||
          (ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) ==
              PackageManager.PERMISSION_GRANTED)
    }
) {
  /** Location services setup * */
  val context = LocalContext.current
  val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

  /** ViewModel setup * */
  val vm: MapViewModel =
      viewModel
          ?: viewModel(
              factory = MapViewModel.provideFactory(fusedLocationClient = fusedLocationClient))

  val uiState by vm.uiState.collectAsState()

  /** Coroutine scope for launching permission requests * */
  val scope = rememberCoroutineScope()

  /** Variable to track location permission status */
  var isLocationPermissionGranted by remember { mutableStateOf(false) }

  HandleSignedOutState(uiState.onSignedOut, onSignedOut)

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
            modifier = Modifier.testTag(NavigationTestTags.TOP_NAVIGATION_MENU),
            onSignedOut = { vm.signOut(credentialManager) })
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
                  Spacer(modifier = Modifier.height(16.dp))
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
 * Collapsed Event marker icon
 *
 * @param event The Event data to display in the marker.
 */
@Composable
fun EventIcon(event: Event) {
  Card(
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.tertiary,
              contentColor = MaterialTheme.colorScheme.onTertiary),
      modifier =
          Modifier.size(Dimensions.markerWidth, Dimensions.markerHeightCollapsed)
              .testTag(MapScreenTestTags.EVENT_CARD),
  ) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(Dimensions.cardPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = Modifier.weight(Dimensions.weight)) {
        Text(
            modifier = Modifier.testTag(MapScreenTestTags.EVENT_TITLE),
            text = event.title.uppercase(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onTertiary,
            fontWeight = FontWeight.Medium)
      }
    }
  }
}

/**
 * Event Sheet displayed when an Event marker is tapped
 *
 * @param event The Event data to display in the marker.
 * @param onGoToEvent Go to Event Page when button is clicked.
 * @param onClose closes the sheet when tapped outside.
 */
@Composable
fun EventSheet(event: Event, onGoToEvent: () -> Unit, onClose: () -> Unit) {
  val formattedDate =
      remember(event.date) {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.format(event.date.toDate())
      }

  Column(
      modifier =
          Modifier.fillMaxWidth()
              .padding(Dimensions.rowColPadding)
              .testTag(MapScreenTestTags.EVENT_SHEET)) {
        Text(
            text = event.title.uppercase(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.testTag(MapScreenTestTags.EVENT_TITLE_SHEET))

        Spacer(modifier = Modifier.size(Dimensions.spacerPadding))

        Text(
            modifier =
                Modifier.padding(Dimensions.textPadding).testTag(MapScreenTestTags.EVENT_DATE),
            text = formattedDate,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground)
        Text(
            modifier =
                Modifier.padding(Dimensions.textPadding)
                    .testTag(MapScreenTestTags.EVENT_DESCRIPTION),
            text = event.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground)

        Spacer(modifier = Modifier.size(Dimensions.spacerPadding))

        Row(
            modifier = Modifier.padding(Dimensions.rowColPadding),
            verticalAlignment = Alignment.CenterVertically) {
              Button(
                  modifier =
                      Modifier.weight(Dimensions.weight).testTag(MapScreenTestTags.EVENT_BUTTON),
                  onClick = onGoToEvent,
                  colors =
                      ButtonColors(
                          containerColor = MaterialTheme.colorScheme.tertiary,
                          contentColor = MaterialTheme.colorScheme.onTertiary,
                          disabledContainerColor = MaterialTheme.colorScheme.tertiary,
                          disabledContentColor = MaterialTheme.colorScheme.onTertiary)) {
                    Text(
                        text = stringResource(R.string.go_to_event_page_button),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium)
                  }
            }

        Spacer(modifier = Modifier.size(Dimensions.spacerPadding))
      }
}

/** -------------------------------- [ToDo] icons -------------------------------- * */

/**
 * Collapsed [ToDo] marker icon
 *
 * @param toDo The [ToDo] data to display in the marker.
 */
@Composable
fun ToDoIcon(toDo: ToDo) {
  Card(
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.secondary,
              contentColor = MaterialTheme.colorScheme.onSecondary),
      modifier =
          Modifier.size(Dimensions.markerWidth, Dimensions.markerHeightCollapsed)
              .testTag(MapScreenTestTags.TODO_CARD),
  ) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(Dimensions.cardPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = Modifier.weight(Dimensions.weight)) {
        Text(
            modifier = Modifier.testTag(MapScreenTestTags.TODO_TITLE),
            text = toDo.name.uppercase(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSecondary,
            fontWeight = FontWeight.Medium)
      }
    }
  }
}

/**
 * [ToDo] Sheet displayed when a `toDo` marker is tapped
 *
 * @param toDo The [ToDo] data to display in the marker.
 * @param onGoToToDo Go to [ToDo] Page when button is clicked.
 * @param onClose closes the sheet when tapped outside.
 */
@Composable
fun ToDoSheet(toDo: ToDo, onGoToToDo: () -> Unit, onClose: () -> Unit) {

  val formattedDate =
      remember(toDo.dueDate) {
        toDo.dueDate?.let { date ->
          val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
          sdf.format(date.toDate())
        } ?: ""
      }

  Column(
      modifier =
          Modifier.fillMaxWidth()
              .padding(Dimensions.rowColPadding)
              .testTag(MapScreenTestTags.TODO_SHEET)) {
        Text(
            text = toDo.name.uppercase(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.testTag(MapScreenTestTags.TODO_TITLE_SHEET))

        Spacer(modifier = Modifier.size(Dimensions.spacerPadding))

        Text(
            modifier =
                Modifier.padding(Dimensions.textPadding).testTag(MapScreenTestTags.TODO_DUE_DATE),
            text = formattedDate,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground)
        Text(
            modifier =
                Modifier.padding(Dimensions.textPadding)
                    .testTag(MapScreenTestTags.TODO_DESCRIPTION),
            text = toDo.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground)

        Spacer(modifier = Modifier.size(Dimensions.spacerPadding))

        Row(
            modifier = Modifier.padding(Dimensions.rowColPadding),
            verticalAlignment = Alignment.CenterVertically) {
              Button(
                  modifier =
                      Modifier.weight(Dimensions.weight).testTag(MapScreenTestTags.TODO_BUTTON),
                  onClick = onGoToToDo,
                  colors =
                      ButtonColors(
                          containerColor = MaterialTheme.colorScheme.secondary,
                          contentColor = MaterialTheme.colorScheme.onSecondary,
                          disabledContainerColor = MaterialTheme.colorScheme.secondary,
                          disabledContentColor = MaterialTheme.colorScheme.onSecondary)) {
                    Text(
                        text = stringResource(R.string.go_to_todo_page_button),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium)
                  }
            }

        Spacer(modifier = Modifier.size(Dimensions.spacerPadding))
      }
}
