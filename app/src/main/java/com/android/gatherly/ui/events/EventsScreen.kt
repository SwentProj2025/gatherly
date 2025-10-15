package com.android.gatherly.ui.events

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.model.event.Event
import com.android.gatherly.ui.todo.OverviewScreenTestTags
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Locale
import kotlin.Boolean
import com.android.gatherly.ui.navigation.BottomNavigationMenu
import com.android.gatherly.ui.navigation.NavigationActions
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu

object EventsScreenTestTags {

    const val EVENT_POPUP = "EventPopUp"

    const val ALL_LISTS = "EventsLists"

    const val BROWSER_EVENTS_LIST = "BrowserEvents"

    const val UPCOMING_EVENTS_LIST = "UpcomingEvents"

    const val MY_EVENTS_LIST = "MyOwnEvents"

    const val CREATE_EVENT_BUTTON = "CreateANewEvent"

    const val CANCEL_EVENT_BUTTON = "CancelEvent"

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




    /**
     * Returns a unique test tag for the card or container representing a given [Event] item.
     *
     * @param event The [Event] item whose test tag will be generated.
     * @return A string uniquely identifying the ToDo item in the UI.
     */
    fun getTestTagForEventItem(event: Event): String = "eventItem${event.id}"
}

@Composable
fun EventsScreen(
    eventsViewModel: EventsViewModel = viewModel(),
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current),
    onSignedOut: () -> Unit = {},
    addYourNewEvent: () -> Unit = {},
    navigateToEditEvent: (Event) -> Unit = {},
    navigationActions: NavigationActions? = null,
) {
/* TODO
    val context = LocalContext.current
    val uiState by eventsViewModel.uiState.collectAsState()
    val listEvents = uiState.fullEventList
    val browserEvents = uiState.globalEventList
    val upcomingEvents = uiState.participatedEventList
    val myOwnEvents = uiState.createdEventList

 */
    val listEvents = emptyList<Event>()
    val browserEvents = emptyList<Event>()
    val upcomingEvents = emptyList<Event>()
    val myOwnEvents = emptyList<Event>()


    val currentUser = FirebaseAuth.getInstance().currentUser
    val currentUserId = currentUser?.uid
    /* TODO
    LaunchedEffect(Unit) {
        eventsViewModel.refreshEvents(currentUserId.toString())
    }

     */

    val isPopupOn = remember { mutableStateOf(false) }


    /* TODO
    LaunchedEffect(uiState.signedOut) {
        if (uiState.signedOut) {
            onSignedOut()
            Toast.makeText(context, "Logout successful", Toast.LENGTH_SHORT).show()
        }
    }*/

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
            if (listEvents.isNotEmpty()) {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(padding)
                            .testTag(EventsScreenTestTags.ALL_LISTS)) {

                    // BROWSE EVENTS
                    item {
                        Text(
                            text = "Browse Events",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                                .testTag(EventsScreenTestTags.BROWSE_TITLE))
                    }

                    if (browserEvents.isNotEmpty()) {
                        items(browserEvents.size) { index ->
                            BrowserEventsItem(
                                event = browserEvents[index],
                                onClick = {
                                    isPopupOn.value = true
                                }
                            )
                            if (isPopupOn.value) {
                                BrowserEventsPopUp(
                                    event = browserEvents[index],
                                    shouldShowDialog = isPopupOn,
                                    participate = {} /* TODO {
                                        eventsViewModel.onParticipate(
                                            browserEvents[index].id,
                                            currentUserId = currentUserId.toString()
                                        )
                                    } */

                                )
                            }

                        }
                    } else {
                        item {
                            Text(
                                "No events coming",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .testTag(EventsScreenTestTags.EMPTY_BROWSER_LIST_MSG)
                                ,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // MY UPCOMING EVENTS
                    item {
                        Text(
                            text = "My Upcoming Events",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp).testTag(
                                EventsScreenTestTags.UPCOMING_TITLE))
                    }

                    if (upcomingEvents.isNotEmpty()) {
                        items(upcomingEvents.size) { index ->
                            UpcomingEventsItem(
                                event = upcomingEvents[index],
                                onClick = {
                                    isPopupOn.value = true
                                }
                            )

                            if (isPopupOn.value) {
                                UpComingEventsPopUp(
                                    event = upcomingEvents[index],
                                    shouldShowDialog = isPopupOn,
                                    unparticipate = {} /* TODO{
                                        eventsViewModel.onUnregister(
                                            eventId = upcomingEvents[index].id,
                                            currentUserId = currentUserId.toString()
                                        )
                                    } */
                                )
                            }
                        }
                    } else {
                        item {
                            Text(
                                "You are not register to any upcoming events",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .testTag(EventsScreenTestTags.EMPTY_UPCOMING_LIST_MSG),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // MY OWN EVENTS
                    item {
                        Text(
                            text = "My Own Events",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp).testTag(
                                EventsScreenTestTags.YOUR_EVENTS_TITLE)
                        )
                    }

                    if (myOwnEvents.isNotEmpty()) {
                        items(myOwnEvents.size) { index ->
                            MyOwnEventsItem(
                                event = browserEvents[index],
                                onClick = {
                                    isPopupOn.value = true
                                }
                            )

                            if (isPopupOn.value) {
                                MyOwnEventsPopUp(
                                    event = myOwnEvents[index],
                                    shouldShowDialog = isPopupOn,
                                    cancelYourEvent = { navigateToEditEvent(myOwnEvents[index]) }
                                )
                            }

                        }
                    } else {
                        item {
                            Text(
                                "You did not create any events",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .testTag(EventsScreenTestTags.EMPTY_OUREVENTS_LIST_MSG),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    item {
                        Button(
                            onClick = { addYourNewEvent() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .padding(vertical = 12.dp)
                                .testTag(EventsScreenTestTags.CREATE_EVENT_BUTTON)
                            ,
                            shape = RoundedCornerShape(12.dp),
                            colors = buttonColors(containerColor = Color(0xFF9ADCE5))
                        ) {
                            Text(
                                text = "Create an event",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }

                }
            }

        }
    )
}


/**
 * Displays a single Event item inside a [Card] : PARTICIPATE OPTION

 * @param events The [Event] item to display.
 * @param onClick A callback invoked when the user taps the Event card to open a pop Up with the event's description
 */
@Composable
fun BrowserEventsItem(
    event: Event,
    onClick: () -> Unit
) {
    Card(
        modifier =
            Modifier
                .clickable(onClick = onClick)
                .testTag(EventsScreenTestTags.getTestTagForEventItem(event))
                .fillMaxWidth()
                .padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            ) {

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.testTag(EventsScreenTestTags.EVENT_TITLE))
                Text(
                    text =
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(event.date.toDate()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag(EventsScreenTestTags.EVENT_DATE)
                    )
            }
        }
    }
}


/**
 * Displays a single Event item inside a [Card] : UNREGISTER OPTION

 * @param events The [Event] item to display.
 * @param onClick A callback invoked when the user taps the Event card to open a pop Up with the event's description
 */
@Composable
fun UpcomingEventsItem(
    event: Event,
    onClick: () -> Unit
) {
    Card(
        modifier =
            Modifier
                .clickable(onClick = onClick)
                .testTag(EventsScreenTestTags.getTestTagForEventItem(event))
                .fillMaxWidth()
                .padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.testTag(EventsScreenTestTags.EVENT_TITLE))
                Text(
                    text =
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(event.date.toDate()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag(EventsScreenTestTags.EVENT_DATE)
                )
            }
        }
    }
}

/**
 * Displays PopUp when clicking on an event :  UNPARTICIPATE OPTION click
 * @param events The [Event] item to display.
 */
@Composable
fun UpComingEventsPopUp(
    event: Event,
    shouldShowDialog: MutableState<Boolean>,
    unparticipate: () -> Unit
) {
    AlertDialog(
        title = { event.title },
        text = { event.description },
        icon = { Icons.Outlined.Celebration },
        modifier = Modifier.testTag(EventsScreenTestTags.EVENT_POPUP),
        dismissButton =
            {
                Button(
                    onClick = {
                        shouldShowDialog.value = false
                    },
                    modifier = Modifier.testTag(EventsScreenTestTags.GOBACK_EVENT_BUTTON)
                ) {
                    Text(
                        text = "go Back",
                        color = Color.White
                    )
                }
            },
        onDismissRequest = {shouldShowDialog.value = false} ,

        confirmButton =
            {
                Button(
                    onClick = {
                        unparticipate()
                        shouldShowDialog.value = false
                    },
                    modifier = Modifier.testTag(EventsScreenTestTags.UNREGISTER_BUTTON)
                ) {
                    Text(
                        text = "Unregister",
                        color = Color.White
                    )
                }
            },

        )
}

/**
 * Displays PopUp when clicking on an event :  PARTICIPATE OPTION
 * @param events The [Event] item to display.
 */
@Composable
fun BrowserEventsPopUp(
    event: Event,
    shouldShowDialog: MutableState<Boolean>,
    participate: () -> Unit
) {
    AlertDialog(
        title = { event.title },
        text = { event.description },
        icon = { Icons.Outlined.Celebration },
        modifier = Modifier.testTag(EventsScreenTestTags.EVENT_POPUP),
        dismissButton =
            {
                Button(
                    onClick = {
                        shouldShowDialog.value = false
                    },
                    modifier = Modifier.testTag(EventsScreenTestTags.GOBACK_EVENT_BUTTON)
                ) {
                    Text(
                        text = "go Back",
                        color = Color.White
                    )
                }
            },
        onDismissRequest = {shouldShowDialog.value = false} ,

        confirmButton =
            {
                Button(
                    onClick = {
                        participate()
                        shouldShowDialog.value = false
                    },
                    modifier = Modifier.testTag(EventsScreenTestTags.PARTICIPATE_BUTTON)
                ) {
                    Text(
                        text = "Participate",
                        color = Color.White
                    )
                }
            },

        )
}


/**
 * Displays a single Event item inside a [Card]  CANCELED OPTION text

 * @param events The [Event] item to display.
 * @param onClick A callback invoked when the user taps the Event card to open a pop Up with the event's description
 */
@Composable
fun MyOwnEventsItem(
    event: Event,
    onClick: () -> Unit
) {
    Card(
        modifier =
            Modifier
                .clickable(onClick = onClick)
                .testTag(EventsScreenTestTags.getTestTagForEventItem(event))
                .fillMaxWidth()
                .padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.testTag(EventsScreenTestTags.EVENT_TITLE))
                Text(
                    text =
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(event.date.toDate()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag(EventsScreenTestTags.EVENT_DATE)
                )
            }
        }
    }
}

/**
 * Displays PopUp when clicking on an event :  CANCELED OPTION click
 * @param events The [Event] item to display.
*/
@Composable
fun MyOwnEventsPopUp(
    event: Event,
    shouldShowDialog: MutableState<Boolean>,
    cancelYourEvent: () -> Unit
) {
    AlertDialog(
        title = { event.title },
        text = { event.description },
        icon = { Icons.Outlined.Celebration },
        modifier = Modifier.testTag(EventsScreenTestTags.EVENT_POPUP),
        dismissButton =
            {
                Button(
                    onClick = {
                        shouldShowDialog.value = false
                    },
                    modifier = Modifier.testTag(EventsScreenTestTags.GOBACK_EVENT_BUTTON)
                ) {
                    Text(
                        text = "go Back",
                        color = Color.White
                    )
                }
            },
        onDismissRequest = { shouldShowDialog.value = false } ,

        confirmButton =
            {
                Button(
                    onClick = {
                        cancelYourEvent()
                        shouldShowDialog.value = false
                    },
                    modifier = Modifier.testTag(EventsScreenTestTags.CANCEL_EVENT_BUTTON)
                ) {
                    Text(
                        text = "Cancel the event",
                        color = Color.White
                    )
                }
            },

    )
}

