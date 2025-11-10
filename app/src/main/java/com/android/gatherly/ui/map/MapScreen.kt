package com.android.gatherly.ui.map

import android.widget.Button
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import java.text.SimpleDateFormat
import java.util.Locale

// Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the
// SwEnt staff.

/** Test tags for MapScreen. */
object MapScreenTestTags {
  const val GOOGLE_MAP_SCREEN = "mapScreen"
  const val FILTER_TOGGLE = "filterToggle"

  const val TODO_CARD = "todoCard"
  const val EVENT_CARD = "eventCard"
  const val TODO_EXPANDED_CARD = "todoExpandedCard"
  const val EVENT_SHEET = "eventModal"
  const val TODO_TITLE = "todoTitle"
  const val TODO_TITLE_EXPANDED = "todoTitleExpanded"

  const val EVENT_BUTTON = "eventButton"

  const val EVENT_TITLE = "eventTitle"
  const val EVENT_TITLE_SHEET = "eventTitleSheet"

  const val TODO_DUE_DATE = "todoDueDate"
  const val EVENT_DATE = "eventDate"
  const val TODO_DESCRIPTION = "todoDescription"
  const val EVENT_DESCRIPTION = "eventDescription"

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
  val markerHeightExpanded = 150.dp
  val cardPadding = 12.dp

  val spacerPadding = 20.dp

  val textPadding = 8.dp
  val rowColPadding = 16.dp

  val weight = 1f
}

/**
 * A composable screen displaying ToDos and Events as interactive markers on a Google Map.
 *
 * @param viewModel The MapViewModel instance providing the list of ToDos, the current camera
 *   position, and marker interaction handlers.
 * @param credentialManager The CredentialManager for handling user sign-out.
 * @param onSignedOut Callback invoked when the user signs out.
 * @param navigationActions Navigation actions for switching between app sections.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel(),
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current),
    onSignedOut: () -> Unit = {},
    navigationActions: NavigationActions? = null,
    goToEvent: () -> Unit = {}
) {

  val uiState by viewModel.uiState.collectAsState()
  HandleSignedOutState(uiState.onSignedOut, onSignedOut)

  // Bottom sheet state for the selected event
  var sheetEvent by remember { mutableStateOf<Event?>(null) }
  var showSheet by remember { mutableStateOf(false) }
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  Scaffold(
      topBar = {
        TopNavigationMenu(
            selectedTab = Tab.Map,
            onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
            modifier = Modifier.testTag(NavigationTestTags.TOP_NAVIGATION_MENU),
            onSignedOut = { viewModel.signOut(credentialManager) })
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
        val isEvents = uiState.displayEventsPage
        ExtendedFloatingActionButton(
            onClick = { viewModel.changeView() },
            icon = {},
            text = {
              Text(
                  if (uiState.displayEventsPage) stringResource(R.string.show_todos_button_title)
                  else stringResource(R.string.show_events_button_title))
            },
            containerColor =
                if (isEvents) MaterialTheme.colorScheme.secondary
                else MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.testTag(MapScreenTestTags.FILTER_TOGGLE))
      },
      content = { pd ->
        // Camera position state, using the first ToDo location if available
        val cameraPositionState = rememberCameraPositionState()

        LaunchedEffect(uiState.cameraPos) {
          cameraPositionState.position = CameraPosition.fromLatLngZoom(uiState.cameraPos, 14f)
        }

        GoogleMap(
            modifier =
                Modifier.fillMaxSize().padding(pd).testTag(MapScreenTestTags.GOOGLE_MAP_SCREEN),
            cameraPositionState = cameraPositionState,
            onMapClick = { _ ->
              // Close sheet and clear selection on map taps
              showSheet = false
              sheetEvent = null
              viewModel.onMarkerDismissed()
            }) {
              uiState.itemsList.forEach { item ->
                when (item) {
                  // -------------------------------- Todo Marker UI -------------------------------
                  is ToDo -> {
                    val loc = item.location ?: return@forEach
                    val isExpanded = uiState.expandedItemId == item.uid
                    val z = if (isExpanded) 2f else 1f

                    key("todo_${item.uid}_$isExpanded") {
                      val markerState =
                          rememberMarkerState(position = LatLng(loc.latitude, loc.longitude))

                      MarkerComposable(
                          state = markerState,
                          zIndex = z,
                          onClick = {
                            if (isExpanded) viewModel.onMarkerDismissed()
                            else viewModel.onMarkerTapped(item.uid)
                            true
                          }) {
                            if (isExpanded) {
                              Box(
                                  Modifier.testTag(
                                      MapScreenTestTags.todoMarkerExpanded(item.uid))) {
                                    ToDoExpandedIcon(item)
                                  }
                            } else {
                              Box(Modifier.testTag(MapScreenTestTags.todoMarker(item.uid))) {
                                ToDoIcon(item)
                              }
                            }
                          }
                    }
                  }
                  // -------------------------------- Event Marker UI ---------------------------
                  is Event -> {
                    val loc = item.location ?: return@forEach
                    val isExpanded = uiState.expandedItemId == item.id
                    val z = if (isExpanded) 2f else 1f

                    key("event_${item.id}_$isExpanded") {
                      val markerState =
                          rememberMarkerState(position = LatLng(loc.latitude, loc.longitude))

                      MarkerComposable(
                          state = markerState,
                          zIndex = z,
                          onClick = {
                            // Open bottom sheet with this event
                            sheetEvent = item
                            showSheet = true
                            viewModel.onMarkerTapped(item.id) // optional tracking
                            true
                          }) {
                            Box(Modifier.testTag(MapScreenTestTags.eventMarker(item.id))) {
                              EventIcon(item)
                            }
                          }
                    }
                  }
                }
              }
            }

        if (showSheet && sheetEvent != null) {
          ModalBottomSheet(
              sheetState = sheetState,
              onDismissRequest = {
                showSheet = false
                sheetEvent = null
                viewModel.onMarkerDismissed()
              }) {
                EventSheet(
                    event = sheetEvent!!,
                    onGoToEvent = {
                      showSheet = false
                      sheetEvent = null
                      goToEvent()
                    },
                    onClose = {
                      showSheet = false
                      sheetEvent = null
                      viewModel.onMarkerDismissed()
                    })
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
              contentColor = MaterialTheme.colorScheme.primary),
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
            color = MaterialTheme.colorScheme.primary,
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
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.testTag(MapScreenTestTags.EVENT_TITLE_SHEET))

        Spacer(modifier = Modifier.size(Dimensions.spacerPadding))

        Text(
            modifier =
                Modifier.padding(Dimensions.textPadding).testTag(MapScreenTestTags.EVENT_DATE),
            text = formattedDate,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary)
        Text(
            modifier =
                Modifier.padding(Dimensions.textPadding)
                    .testTag(MapScreenTestTags.EVENT_DESCRIPTION),
            text = event.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary)

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
                          contentColor = MaterialTheme.colorScheme.primary,
                          disabledContainerColor = MaterialTheme.colorScheme.onTertiary,
                          disabledContentColor = MaterialTheme.colorScheme.onPrimary)) {
                    Text(
                        text = stringResource(R.string.go_to_event_page_button),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium)
                  }
            }

        Spacer(modifier = Modifier.size(Dimensions.spacerPadding))
      }
}

// -------------------------------- ToDo icons --------------------------------

/**
 * Collapsed ToDo marker icon
 *
 * @param toDo The ToDo data to display in the marker.
 */
@Composable
fun ToDoIcon(toDo: ToDo) {
  Card(
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.secondary,
              contentColor = MaterialTheme.colorScheme.primary),
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
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium)
      }
    }
  }
}

/**
 * Expanded ToDo marker icon
 *
 * @param toDo The ToDo data to display in the marker.
 */
@Composable
fun ToDoExpandedIcon(toDo: ToDo) {
  val formattedDate =
      remember(toDo.dueDate) {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.format(toDo.dueDate.toDate())
      }

  Card(
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.secondary,
              contentColor = MaterialTheme.colorScheme.primary),
      modifier =
          Modifier.size(Dimensions.markerWidth, Dimensions.markerHeightExpanded)
              .testTag(MapScreenTestTags.TODO_EXPANDED_CARD)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Dimensions.cardPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(Dimensions.weight)) {
            Text(
                modifier = Modifier.testTag(MapScreenTestTags.TODO_TITLE_EXPANDED),
                text = toDo.name.uppercase(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium)
            Text(
                modifier = Modifier.testTag(MapScreenTestTags.TODO_DUE_DATE),
                text = formattedDate,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium)
            Text(
                modifier = Modifier.testTag(MapScreenTestTags.TODO_DESCRIPTION),
                text = toDo.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium)
          }
        }
      }
}
