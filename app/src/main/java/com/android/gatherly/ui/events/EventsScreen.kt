package com.android.gatherly.ui.events

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.R
import com.android.gatherly.model.event.Event
import com.android.gatherly.ui.navigation.BottomNavigationMenu
import com.android.gatherly.ui.navigation.HandleSignedOutState
import com.android.gatherly.ui.navigation.NavigationActions
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu
import com.android.gatherly.ui.theme.GatherlyTheme
import java.util.Locale
import kotlinx.coroutines.launch

object EventsScreenTestTags {

  const val EVENT_POPUP = "EventPopUp"

  const val ALL_LISTS = "EventsLists"

  const val CREATE_EVENT_BUTTON = "CreateANewEvent"

  const val EDIT_EVENT_BUTTON = "EditEvent"

  const val GOBACK_EVENT_BUTTON = "GoBackOverview"

  const val PARTICIPATE_BUTTON = "Participate"

  const val UNREGISTER_BUTTON = "Unregister"

  const val EMPTY_BROWSER_LIST_MSG = "EmptyBrowserEvents"

  const val EMPTY_UPCOMING_LIST_MSG = "EmptyUpcomingEvents"

  const val EMPTY_OUREVENTS_LIST_MSG = "EmptyOurEvents"

  const val BROWSE_TITLE = "TitleBrowserEvents"

  const val UPCOMING_TITLE = "TitleUpcomingEvents"

  const val YOUR_EVENTS_TITLE = "TitleYourEvents"

  const val EVENT_DATE = "EventDate"

  const val EVENT_TITLE = "EventTitle"

  const val POPUP_DESCRIPTION = "EventDescription"

  const val POPUP_TITLE = "PopupTitle"

  /**
   * Returns a unique test tag for the card or container representing a given [Event] item.
   *
   * @param event The [Event] item whose test tag will be generated.
   * @return A string uniquely identifying the Event item in the UI.
   */
  fun getTestTagForEventItem(event: Event): String = "eventItem${event.id}"
}

/**
 * The Events screen displays a list of events categorized into three sections:
 * - Browse Events: Events neither created by nor participated in by the current user.
 * - My Upcoming Events: Events the current user is participating in.
 * - My Own Events: Events created by the current user.
 *
 * Each section allows interaction with the events, such as participating, unregistering, or editing
 * events. The screen also includes navigation menus and a button to create new events.
 *
 * @param credentialManager Manages user credentials for sign-in/sign-out operations.
 * @param onSignedOut Callback invoked when the user signs out.
 * @param onAddEvent Callback to navigate to the event creation screen.
 * @param navigateToEditEvent Callback to navigate to the event editing screen with the selected
 *   event
 * @param navigationActions Handles navigation between different tabs/screens.
 * @param eventsViewModel The ViewModel managing the state and logic for the Events screen,
 *   instantiated with a factory provider defined in the ViewModel's companion object.
 */
@Composable
fun EventsScreen(
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current),
    onSignedOut: () -> Unit = {},
    onAddEvent: () -> Unit = {},
    navigateToEditEvent: (Event) -> Unit = {},
    navigationActions: NavigationActions? = null,
    eventsViewModel: EventsViewModel = viewModel(factory = EventsViewModel.provideFactory()),
    eventId: String? = null,
) {

  val coroutineScope = rememberCoroutineScope()

  val uiState by eventsViewModel.uiState.collectAsState()
  val browserEvents = uiState.globalEventList
  val upcomingEvents = uiState.participatedEventList
  val myOwnEvents = uiState.createdEventList
  val currentUserIdFromVM = uiState.currentUserId

  val selectedBrowserEvent = remember { mutableStateOf<Event?>(null) }
  val selectedUpcomingEvent = remember { mutableStateOf<Event?>(null) }
  val selectedYourEvent = remember { mutableStateOf<Event?>(null) }

  val isPopupOnBrowser = remember { mutableStateOf(false) }
  val isPopupOnUpcoming = remember { mutableStateOf(false) }
  val isPopupOnYourE = remember { mutableStateOf(false) }

  // Handle deep linking to a specific event if eventId is provided
  val eventIdAlreadyProcessed = remember(eventId) { mutableStateOf(false) }
  if (eventId != null && !eventIdAlreadyProcessed.value) {

    // Check if the eventId exists in any of the event lists
    val eventIdIsBrowser = browserEvents.find { it.id == eventId }
    val eventIdIsUpcoming = upcomingEvents.find { it.id == eventId }
    val eventIdIsYourEvent = myOwnEvents.find { it.id == eventId }

    // Open the corresponding pop-up based on where the event was found
    val eventFound =
        if (eventIdIsBrowser != null) {
          selectedBrowserEvent.value = eventIdIsBrowser
          isPopupOnBrowser.value = true
          true
        } else if (eventIdIsUpcoming != null) {
          selectedUpcomingEvent.value = eventIdIsUpcoming
          isPopupOnUpcoming.value = true
          true
        } else if (eventIdIsYourEvent != null) {
          selectedYourEvent.value = eventIdIsYourEvent
          isPopupOnYourE.value = true
          true
        } else {
          false
        }

    // Mark the eventId as processed to avoid reopening the pop-up on recomposition
    if (eventFound) {
      eventIdAlreadyProcessed.value = true
    }
  }

  LaunchedEffect(Unit, currentUserIdFromVM) {
    if (currentUserIdFromVM.isNotBlank()) {
      eventsViewModel.refreshEvents(currentUserIdFromVM)
    }
  }

  HandleSignedOutState(uiState.signedOut, onSignedOut)

  Scaffold(
      topBar = {
        TopNavigationMenu(
            selectedTab = Tab.Events,
            onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
            modifier = Modifier.testTag(NavigationTestTags.TOP_NAVIGATION_MENU),
            onSignedOut = { eventsViewModel.signOut(credentialManager) })
      },
      bottomBar = {
        BottomNavigationMenu(
            selectedTab = Tab.Events,
            onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
            modifier = Modifier.testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU))
      },
      content = { padding ->
        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp),
            modifier =
                Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(padding)
                    .testTag(EventsScreenTestTags.ALL_LISTS)) {

              // --  BROWSE EVENTS LIST --
              item {
                Text(
                    text = stringResource(R.string.browseEvents_list_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier =
                        Modifier.padding(vertical = 10.dp)
                            .testTag(EventsScreenTestTags.BROWSE_TITLE),
                    color = MaterialTheme.colorScheme.onBackground)
              }

              if (browserEvents.isNotEmpty()) {
                items(browserEvents.size) { index ->
                  BrowserEventsItem(
                      event = browserEvents[index],
                      onClick = {
                        selectedBrowserEvent.value = browserEvents[index]
                        isPopupOnBrowser.value = true
                      })
                }
              } else { // When there is no events in the browser list
                item {
                  Text(
                      stringResource(R.string.browseEvents_emptylist_msg),
                      modifier =
                          Modifier.fillMaxWidth()
                              .padding(8.dp)
                              .testTag(EventsScreenTestTags.EMPTY_BROWSER_LIST_MSG),
                      textAlign = TextAlign.Center,
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onBackground)
                }
              }

              // Spacer between Browse and Upcoming
              item { Spacer(modifier = Modifier.height(24.dp)) }

              // -- MY UPCOMING EVENTS LIST --
              item {
                Text(
                    text = stringResource(R.string.upcomingEvents_list_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier =
                        Modifier.padding(vertical = 10.dp)
                            .testTag(EventsScreenTestTags.UPCOMING_TITLE),
                    color = MaterialTheme.colorScheme.onBackground)
              }

              if (upcomingEvents.isNotEmpty()) {
                items(upcomingEvents.size) { index ->
                  UpcomingEventsItem(
                      event = upcomingEvents[index],
                      onClick = {
                        selectedUpcomingEvent.value = upcomingEvents[index]
                        isPopupOnUpcoming.value = true
                      })
                }
              } else { // When there is no events in the upcoming list
                item {
                  Text(
                      stringResource(R.string.upcomingEvents_emptylist_msg),
                      modifier =
                          Modifier.fillMaxWidth()
                              .padding(8.dp)
                              .testTag(EventsScreenTestTags.EMPTY_UPCOMING_LIST_MSG),
                      textAlign = TextAlign.Center,
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onBackground)
                }
              }

              // Spacer between Upcoming and My Own Events
              item { Spacer(modifier = Modifier.height(24.dp)) }

              // MY OWN EVENTS
              item {
                Text(
                    text = stringResource(R.string.userEvents_list_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier =
                        Modifier.padding(vertical = 8.dp)
                            .testTag(EventsScreenTestTags.YOUR_EVENTS_TITLE),
                    color = MaterialTheme.colorScheme.onBackground)
              }

              if (myOwnEvents.isNotEmpty()) {
                items(myOwnEvents.size) { index ->
                  MyOwnEventsItem(
                      event = myOwnEvents[index],
                      onClick = {
                        selectedYourEvent.value = myOwnEvents[index]
                        isPopupOnYourE.value = true
                      })
                }
              } else {
                item {
                  Text(
                      stringResource(R.string.userEvents_emptylist_msg),
                      modifier =
                          Modifier.fillMaxWidth()
                              .padding(8.dp)
                              .testTag(EventsScreenTestTags.EMPTY_OUREVENTS_LIST_MSG),
                      textAlign = TextAlign.Center,
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onBackground)
                }
              }

              // Spacer before the Create Event button
              item { Spacer(modifier = Modifier.height(24.dp)) }

              // CREATE A NEW EVENT BUTTON

              item {
                Button(
                    onClick = { onAddEvent() },
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(80.dp)
                            .padding(vertical = 12.dp)
                            .testTag(EventsScreenTestTags.CREATE_EVENT_BUTTON),
                    shape = RoundedCornerShape(12.dp),
                    colors = buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                      Text(
                          text = stringResource(R.string.create_event_button_title),
                          fontSize = 16.sp,
                          fontWeight = FontWeight.Medium,
                          color = MaterialTheme.colorScheme.onSecondary)
                    }
              }
            }

        // -- EVENT POP UPS --

        selectedBrowserEvent.value?.let { event ->
          BrowserEventsPopUp(
              event = event,
              shouldShowDialog = isPopupOnBrowser,
              participate = {
                eventsViewModel.onParticipate(
                    eventId = event.id, currentUserId = currentUserIdFromVM)
                coroutineScope.launch { eventsViewModel.refreshEvents(currentUserIdFromVM) }
              })
          selectedBrowserEvent.value = if (isPopupOnBrowser.value) event else null
        }

        selectedUpcomingEvent.value?.let { event ->
          UpComingEventsPopUp(
              event = event,
              shouldShowDialog = isPopupOnUpcoming,
              unparticipate = {
                eventsViewModel.onUnregister(
                    eventId = event.id, currentUserId = currentUserIdFromVM)

                coroutineScope.launch { eventsViewModel.refreshEvents(currentUserIdFromVM) }
              })
          selectedUpcomingEvent.value = if (isPopupOnUpcoming.value) event else null
        }

        selectedYourEvent.value?.let { event ->
          MyOwnEventsPopUp(
              event = event,
              shouldShowDialog = isPopupOnYourE,
              cancelYourEvent = {
                navigateToEditEvent(event)
                coroutineScope.launch { eventsViewModel.refreshEvents(currentUserIdFromVM) }
              })
          selectedYourEvent.value = if (isPopupOnYourE.value) event else null
        }
      })
}

/**
 * Displays a single Event item inside a [Card] : PARTICIPATE OPTION
 *
 * @param events The [Event] item to display.
 * @param onClick A callback invoked when the user taps the Event card to open a pop Up with the
 *   event's description
 */
@Composable
fun BrowserEventsItem(event: Event, onClick: () -> Unit) {
  Card(
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant),
      shape = RoundedCornerShape(8.dp),
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant,
              contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
      modifier =
          Modifier.clickable(onClick = onClick)
              .testTag(EventsScreenTestTags.getTestTagForEventItem(event))
              .fillMaxWidth()
              .padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.testTag(EventsScreenTestTags.EVENT_TITLE))
            Text(
                text =
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(event.date.toDate()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag(EventsScreenTestTags.EVENT_DATE))
          }
        }
      }
}

/**
 * Displays a single Event item inside a [Card] : UNREGISTER OPTION
 *
 * @param events The [Event] item to display.
 * @param onClick A callback invoked when the user taps the Event card to open a pop Up with the
 *   event's description
 */
@Composable
fun UpcomingEventsItem(event: Event, onClick: () -> Unit) {
  Card(
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant),
      shape = RoundedCornerShape(8.dp),
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant,
              contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
      modifier =
          Modifier.clickable(onClick = onClick)
              .testTag(EventsScreenTestTags.getTestTagForEventItem(event))
              .fillMaxWidth()
              .padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.testTag(EventsScreenTestTags.EVENT_TITLE))
            Text(
                text =
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(event.date.toDate()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag(EventsScreenTestTags.EVENT_DATE))
          }
        }
      }
}

/**
 * Displays a single Event item inside a [Card] CANCELED OPTION text
 *
 * @param events The [Event] item to display.
 * @param onClick A callback invoked when the user taps the Event card to open a pop Up with the
 *   event's description
 */
@Composable
fun MyOwnEventsItem(event: Event, onClick: () -> Unit) {
  Card(
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant),
      shape = RoundedCornerShape(8.dp),
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant,
              contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
      modifier =
          Modifier.clickable(onClick = onClick)
              .testTag(EventsScreenTestTags.getTestTagForEventItem(event))
              .fillMaxWidth()
              .padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.testTag(EventsScreenTestTags.EVENT_TITLE))
            Text(
                text =
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(event.date.toDate()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag(EventsScreenTestTags.EVENT_DATE))
          }
        }
      }
}
/**
 * Displays PopUp when clicking on an event : UNPARTICIPATE OPTION click
 *
 * @param events The [Event] item to display.
 */
@Composable
fun UpComingEventsPopUp(
    event: Event,
    shouldShowDialog: MutableState<Boolean>,
    unparticipate: () -> Unit
) {
  AlertDialog(
      containerColor = MaterialTheme.colorScheme.surfaceVariant,
      titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
      textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
      title = {
        Text(
            text = event.title,
            modifier = Modifier.testTag(EventsScreenTestTags.POPUP_TITLE),
            textAlign = TextAlign.Center)
      },
      text = {
        Text(
            text = event.description,
            modifier = Modifier.testTag(EventsScreenTestTags.POPUP_DESCRIPTION),
            textAlign = TextAlign.Center,
        )
      },
      icon = { Icons.Outlined.Celebration },
      modifier = Modifier.testTag(EventsScreenTestTags.EVENT_POPUP),
      dismissButton = {
        Button(
            colors =
                buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
            onClick = { shouldShowDialog.value = false },
            modifier = Modifier.testTag(EventsScreenTestTags.GOBACK_EVENT_BUTTON)) {
              Text(
                  text = stringResource(R.string.goback_button_title),
                  color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
      },
      onDismissRequest = { shouldShowDialog.value = false },
      confirmButton = {
        Button(
            colors =
                buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
            onClick = {
              unparticipate()
              shouldShowDialog.value = false
            },
            modifier = Modifier.testTag(EventsScreenTestTags.UNREGISTER_BUTTON)) {
              Text(
                  text = stringResource(R.string.unregister_button_title),
                  color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
      },
  )
}

/**
 * Displays PopUp when clicking on an event : PARTICIPATE OPTION
 *
 * @param events The [Event] item to display.
 */
@Composable
fun BrowserEventsPopUp(
    event: Event,
    shouldShowDialog: MutableState<Boolean>,
    participate: () -> Unit
) {
  AlertDialog(
      containerColor = MaterialTheme.colorScheme.surfaceVariant,
      titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
      textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
      title = {
        Text(
            text = event.title,
            modifier = Modifier.testTag(EventsScreenTestTags.POPUP_TITLE),
            textAlign = TextAlign.Center,
        )
      },
      text = {
        Text(
            text = event.description,
            modifier = Modifier.testTag(EventsScreenTestTags.POPUP_DESCRIPTION),
            textAlign = TextAlign.Center,
        )
      },
      icon = { Icons.Outlined.Celebration },
      modifier = Modifier.testTag(EventsScreenTestTags.EVENT_POPUP),
      dismissButton = {
        Button(
            colors =
                buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
            onClick = { shouldShowDialog.value = false },
            modifier = Modifier.testTag(EventsScreenTestTags.GOBACK_EVENT_BUTTON)) {
              Text(
                  text = stringResource(R.string.goback_button_title),
                  color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
      },
      onDismissRequest = { shouldShowDialog.value = false },
      confirmButton = {
        Button(
            colors =
                buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
            onClick = {
              participate()
              shouldShowDialog.value = false
            },
            modifier = Modifier.testTag(EventsScreenTestTags.PARTICIPATE_BUTTON)) {
              Text(
                  text = stringResource(R.string.participate_button_title),
                  color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
      },
  )
}

/**
 * Displays PopUp when clicking on an event : CANCELED OPTION click
 *
 * @param events The [Event] item to display.
 */
@Composable
fun MyOwnEventsPopUp(
    event: Event,
    shouldShowDialog: MutableState<Boolean>,
    cancelYourEvent: () -> Unit
) {
  AlertDialog(
      containerColor = MaterialTheme.colorScheme.surfaceVariant,
      titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
      textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
      title = {
        Text(
            text = event.title,
            modifier = Modifier.testTag(EventsScreenTestTags.POPUP_TITLE),
            textAlign = TextAlign.Center,
        )
      },
      text = {
        Text(
            text = event.description,
            modifier = Modifier.testTag(EventsScreenTestTags.POPUP_DESCRIPTION),
            textAlign = TextAlign.Center,
        )
      },
      icon = { Icons.Outlined.Celebration },
      modifier = Modifier.testTag(EventsScreenTestTags.EVENT_POPUP),
      dismissButton = {
        Button(
            colors =
                buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
            onClick = { shouldShowDialog.value = false },
            modifier = Modifier.testTag(EventsScreenTestTags.GOBACK_EVENT_BUTTON)) {
              Text(
                  text = stringResource(R.string.goback_button_title),
                  color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
      },
      onDismissRequest = { shouldShowDialog.value = false },
      confirmButton = {
        Button(
            colors =
                buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
            onClick = {
              cancelYourEvent()
              shouldShowDialog.value = false
            },
            modifier = Modifier.testTag(EventsScreenTestTags.EDIT_EVENT_BUTTON)) {
              Text(
                  text = stringResource(R.string.edit_button_title),
                  color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
      },
  )
}

@Preview(showBackground = true)
@Composable
fun EventsScreenPreview() {
  GatherlyTheme(darkTheme = true) { EventsScreen() }
}
