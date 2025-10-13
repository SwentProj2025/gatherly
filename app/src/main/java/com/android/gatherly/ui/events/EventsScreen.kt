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

/*
import com.android.gatherly.ui.navigation.BottomNavigationMenu
import com.android.gatherly.ui.navigation.NavigationActions
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu

 */

object EventsScreenTestTags {
    const val EVENT_POPUP = "EventPopUp"



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
    //navigationActions: NavigationActions? = null,
) {

    val context = LocalContext.current
    val uiState by eventsViewModel.uiState.collectAsState()
    val listEvents = uiState.fullEventList
    val browserEvents = uiState.globalEventList
    val upcomingEvents = uiState.participatedEventList
    val myOwnEvents = uiState.createdEventList
    /*
    val browserEvents = emptyList<Event>()
    val upcomingEvents = emptyList<Event>()
    val myOwnEvents = emptyList<Event>()
     */

    val currentUser = FirebaseAuth.getInstance().currentUser
    val currentUserId = currentUser?.uid
    LaunchedEffect(Unit) {
        eventsViewModel.refreshEvents(currentUserId.toString())
    }

    val isPopupOn = remember { mutableStateOf(false) }


    /*LaunchedEffect(uiState.signedOut) {
        if (uiState.signedOut) {
            onSignedOut()
            Toast.makeText(context, "Logout successful", Toast.LENGTH_SHORT).show()
        }
    }*/

    Scaffold(
        /*
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
        },*/

        content = { padding ->
            if (listEvents.isNotEmpty()) {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(padding)
                            .testTag(OverviewScreenTestTags.TODO_LIST)) {

                    // BROWSE EVENTS
                    if (browserEvents.isNotEmpty()) {
                        item {
                            Text(
                                text = "Browse Events",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp))
                        }
                        items(browserEvents.size) { index ->
                            BrowserEventsItem(
                                event = browserEvents[index],
                                onClick = {
                                    isPopupOn.value = true
                                }
                            )

                            if (isPopupOn.value){
                                BrowserEventsPopUp(
                                    event = browserEvents[index],
                                    shouldShowDialog = isPopupOn,
                                    participate = {
                                        eventsViewModel.onParticipate(
                                            browserEvents[index].id,
                                            currentUserId = currentUserId.toString()
                                        )
                                    }
                                )
                            }

                        }
                    }

                    // MY UPCOMING EVENTS
                    if (upcomingEvents.isNotEmpty()) {
                        item {
                            Text(
                                text = "My Upcoming Events",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp))
                        }
                        items(upcomingEvents.size) { index ->
                            UpcomingEventsItem(
                                event = upcomingEvents[index],
                                onClick = {
                                    isPopupOn.value = true
                                }
                            )

                            if (isPopupOn.value) {
                                UpComingEventsPopUp(
                                    event = myOwnEvents[index],
                                    shouldShowDialog = isPopupOn,
                                    unparticipate = {
                                        eventsViewModel.onUnregister(
                                            eventId = myOwnEvents[index].id,
                                            currentUserId = currentUserId.toString()
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // MY OWN EVENTS
                    if (myOwnEvents.isNotEmpty()) {
                        item {
                            Text(
                                text = "My Own Events",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
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
                    }

                    item {
                        Button(
                            onClick = { addYourNewEvent() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .padding(vertical = 12.dp),
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
                    fontWeight = FontWeight.Medium)
                Text(
                    text =
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(event.date.toDate()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
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
                    fontWeight = FontWeight.Medium)
                Text(
                    text =
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(event.date.toDate()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
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
                    }
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
                    }
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
                    }
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
                    }
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
                    fontWeight = FontWeight.Medium)
                Text(
                    text =
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(event.date.toDate()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
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
                    }
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
                    }
                ) {
                    Text(
                        text = "Cancel the event",
                        color = Color.White
                    )
                }
            },

    )
}





/*
- "Browse events" register button -> makes the event go into
- "My upcoming events" (and conversely)
- "My organised events": create button to add a new one + edit (on the right) for existing ones
 When clicking on an event, displays a popup listing all the data class's information
 */

