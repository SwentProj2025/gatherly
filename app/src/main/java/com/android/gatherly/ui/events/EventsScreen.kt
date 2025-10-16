package com.android.gatherly.ui.events

import android.icu.text.SimpleDateFormat
import android.util.Log
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.R
import com.android.gatherly.model.event.Event
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale
import kotlin.Boolean
import com.android.gatherly.model.event.EventsRepositoryFirestore
import com.android.gatherly.ui.navigation.BottomNavigationMenu
import com.android.gatherly.ui.navigation.HandleSignedOutState
import com.android.gatherly.ui.navigation.NavigationActions
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu
import com.android.gatherly.utils.GenericViewModelFactory
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.android.gatherly.R.string.unregister_button_title
import com.android.gatherly.R.string.participate_button_title
import com.android.gatherly.R.string.edit_button_title
import com.android.gatherly.R.string.goback_button_title
import kotlinx.coroutines.launch


object EventsScreenTestTags {

    const val EVENT_POPUP = "EventPopUp"

    const val ALL_LISTS = "EventsLists"

    const val BROWSER_EVENTS_LIST = "BrowserEvents"

    const val UPCOMING_EVENTS_LIST = "UpcomingEvents"

    const val MY_EVENTS_LIST = "MyOwnEvents"

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
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current),
    onSignedOut: () -> Unit = {},
    addYourNewEvent: () -> Unit = {},
    navigateToEditEvent: (Event) -> Unit = {},
    navigationActions: NavigationActions? = null,
    eventsViewModel: EventsViewModel =
        viewModel(
            factory =
                GenericViewModelFactory<EventsViewModel> {
                  EventsViewModel(
                      EventsRepositoryFirestore(Firebase.firestore),
                      currentUserId = Firebase.auth.currentUser?.uid ?: "")
                }),
) {

    val currentUserIdFromVM = eventsViewModel.currentUserId
    val coroutineScope = rememberCoroutineScope()


    val context = LocalContext.current
    val uiState by eventsViewModel.uiState.collectAsState()
    val listEvents = uiState.fullEventList
    val browserEvents = uiState.globalEventList
    val upcomingEvents = uiState.participatedEventList
    val myOwnEvents = uiState.createdEventList
    Log.e("EventsScreen", "myOwnEvents size: ${myOwnEvents.size}")

    val isPopupOn = remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        Log.e("EventsScreen", "LaunchedEffect: uiState changed")
        eventsViewModel.refreshEvents(currentUserIdFromVM)
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
                                    participate =

                                        {coroutineScope.launch {
                                            eventsViewModel.onParticipate(
                                                browserEvents[index].id,
                                                currentUserId = eventsViewModel.currentUserId
                                            )
                                            kotlinx.coroutines.delay(6000L)

                                        }
                                    }
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
                                    unparticipate = {
                                        eventsViewModel.onUnregister(
                                            eventId = upcomingEvents[index].id,
                                            currentUserId = eventsViewModel.currentUserId
                                        )
                                    }
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
                                event = myOwnEvents[index],
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
                        text = stringResource(R.string.goback_button_title),
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
                        text = stringResource(R.string.unregister_button_title),
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
                        text = stringResource(R.string.goback_button_title),
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
                        text = stringResource(R.string.participate_button_title),
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
                        text = stringResource(R.string.goback_button_title),
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
                    modifier = Modifier.testTag(EventsScreenTestTags.EDIT_EVENT_BUTTON)
                ) {
                    Text(
                        text = stringResource(R.string.edit_button_title),
                        color = Color.White
                    )
                }
            },

    )
}

