package com.android.gatherly.ui.events

import android.util.Log
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.gatherly.GatherlyApp
import com.android.gatherly.model.event.Event
import com.android.gatherly.model.event.EventStatus
import com.android.gatherly.model.map.Location
import com.android.gatherly.ui.authentication.SignInScreenTestTags
import com.android.gatherly.ui.focusTimer.FocusTimerScreen
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.utils.FirebaseEmulator
import com.android.gatherly.utils.FirestoreEventsGatherlyTest
import com.android.gatherly.utils.GatherlyTest.Companion.fromDate
import com.android.gatherly.utils.UI_WAIT_TIMEOUT
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.NoSuchElementException
import kotlin.collections.forEach


class EventsOverviewScreenTest : FirestoreEventsGatherlyTest() {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var currentUserId: String

    @Before
    override fun setUp() {
        super.setUp()
        currentUserId = Firebase.auth.currentUser?.uid
            ?: throw IllegalStateException("Firebase user is not authenticated after setUp.")

    }

    val dateB = Timestamp.Companion.fromDate(2025, Calendar.OCTOBER, 25)
    val start = SimpleDateFormat("HH:mm").parse("12:00") ?: throw NoSuchElementException("no date ")
    val finish =
        SimpleDateFormat("HH:mm").parse("23:00") ?: throw NoSuchElementException("no date ")

    fun setContent(withInitialEvents: List<Event> = emptyList()) {
        runTest {
            withInitialEvents.forEach { event ->
                repository.addEvent(event)
            }
            composeTestRule.setContent {
                EventsScreen(
                    eventsViewModel = EventsViewModel(
                        repository = repository,
                        currentUserId = currentUserId
                    )
                )
            }
        }
    }

    fun createBrowserEvent(currentUserId: String): Event {
        return Event(
            id = "browser",
            title = "DOG Event",
            description = "Public Event who allows only people who comes with their dog",
            creatorName = "Gersende",
            location = Location(46.520278, 6.565556, "EPFL"),
            date = dateB,
            startTime = Timestamp(start),
            endTime = Timestamp(finish),
            creatorId = "GersendeID",
            participants = listOf("Colombe", "Valentin", "Gersende"),
            status = EventStatus.UPCOMING,
        )
    }

    fun createUpcomingEvent(currentUserId: String): Event {
        return Event(
            id = "upcoming",
            title = "Hiking Event",
            description = "Hiking Event in the Jura mountains, with a lot of walking and a picnic",
            creatorName = "Alice",
            location = Location(46.520278, 6.565556, "EPFL"),
            date = dateB,
            startTime = Timestamp(start),
            endTime = Timestamp(finish),
            creatorId = "AliceID",
            participants = listOf("Alice", "Bob", currentUserId),
            status = EventStatus.UPCOMING,
        )
    }

    fun createYourEvent(currentUserId: String): Event {
        return Event(
            id = "mine",
            title = "working session with Gab",
            description = "Need to work in the CO with Gab, in order to finish the TDS homework",
            creatorName = "Sofija",
            location = Location(46.520278, 6.565556, "EPFL"),
            date = dateB,
            startTime = Timestamp(start),
            endTime = Timestamp(finish),
            creatorId = currentUserId,
            participants = listOf("Gabriel", "Sofija"),
            status = EventStatus.UPCOMING,
        )

    }


    /**
     * Test: Verifies that when there is no event registered,
     *      all relevant UI components are displayed correctly.
     */
    @Test
    fun testTagsCorrectlySetWhenListsAreEmpty() {
        setContent()
        composeTestRule.onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
            .assertTextContains("Events", substring = true, ignoreCase = true)
        composeTestRule.onNodeWithTag(EventsScreenTestTags.EVENT_POPUP).assertIsNotDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.BROWSE_TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_BROWSER_LIST_MSG)
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.UPCOMING_TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_UPCOMING_LIST_MSG)
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.YOUR_EVENTS_TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_OUREVENTS_LIST_MSG)
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.CREATE_EVENT_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.ALL_LISTS).assertIsDisplayed()
    }

    /**
     * Test :
     * Log in as Anonym Bob, create her own event.
     * Verifies that Bob does see her event in "Your Events" list.
     * Verifies if the title and date of the event are correctly displayed.
     */
    @Test
    fun testYourEventDisplayCorrectly() = runTest {
        val auth = FirebaseEmulator.auth

        try {

            auth.signInAnonymously().await()
            val bobId = auth.currentUser?.uid ?: error("Bob auth failed")
            val eventByBob = createYourEvent(bobId)
            repository.addEvent(eventByBob)

            composeTestRule.setContent {
                EventsScreen(
                    eventsViewModel = EventsViewModel(
                        repository = repository,
                        currentUserId = bobId
                    )
                )
            }

            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithTag(EventsScreenTestTags.BROWSE_TITLE).assertIsDisplayed()
            composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_BROWSER_LIST_MSG)
                .assertIsDisplayed()
            composeTestRule.onNodeWithTag(EventsScreenTestTags.UPCOMING_TITLE).assertIsDisplayed()
            composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_UPCOMING_LIST_MSG)
                .assertIsDisplayed()
            composeTestRule.onNodeWithTag(EventsScreenTestTags.YOUR_EVENTS_TITLE).assertIsDisplayed()
            composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_OUREVENTS_LIST_MSG)
                .assertIsNotDisplayed()
            composeTestRule
                .onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(eventByBob))
                .assertIsDisplayed()
            composeTestRule.onEventItem(eventByBob, hasTestTag(EventsScreenTestTags.EVENT_TITLE))
            composeTestRule.onEventItem(eventByBob, hasTestTag(EventsScreenTestTags.EVENT_DATE))


        } finally {
            // Sign Out
            auth.signOut()
        }
    }


    /**
     * Test: scenario:
     * Log in as Anonym Alice, create her own event, log out.
     * Log in as Anonym Bob, and get his screen.
     * Verifies that Bob does see Alice's event in browser events list.
     * Verifies if the title and date of the event are correctly displayed.
     */
    @Test
    fun testBrowserEventDisplayCorrectly() = runTest {
        val auth = FirebaseEmulator.auth

        try {
            auth.signInAnonymously().await()
            val aliceId = auth.currentUser?.uid ?: error("Alice auth failed")
            val eventByAlice = createYourEvent(aliceId)
            repository.addEvent(eventByAlice)

            auth.signOut()
            delay(100)

            auth.signInAnonymously().await()
            val bobId = auth.currentUser?.uid ?: error("Bob auth failed")

            composeTestRule.setContent {
                EventsScreen(
                    eventsViewModel = EventsViewModel(
                        repository = repository,
                        currentUserId = bobId
                    )
                )
            }

            composeTestRule.waitForIdle()

            composeTestRule
                .onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(eventByAlice))
                .assertIsDisplayed()
            composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_BROWSER_LIST_MSG)
                .assertIsNotDisplayed()

            composeTestRule.onNodeWithTag(EventsScreenTestTags.UPCOMING_TITLE).assertIsDisplayed()
            composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_UPCOMING_LIST_MSG)
                .assertIsDisplayed()
            composeTestRule.onNodeWithTag(EventsScreenTestTags.YOUR_EVENTS_TITLE).assertIsDisplayed()
            composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_OUREVENTS_LIST_MSG)
                .assertIsDisplayed()

            composeTestRule.onEventItem(eventByAlice, hasTestTag(EventsScreenTestTags.EVENT_TITLE))
            composeTestRule.onEventItem(eventByAlice, hasTestTag(EventsScreenTestTags.EVENT_DATE))


        } finally {
            // Sign Out
            auth.signOut()
        }
    }

    /**
     * Test: scenario:
     * Log in as Anonym Alice, create her own event, log out.
     * Log in as Anonym Bob, and get his screen.
     * Click on the Alice Event in browser events list.
     * Verifies that the AlertDialog is displayed with the correct information.
     * Verifies that the "Participate" button is displayed.
     * Verifies that Bob can participate to the event.
     * Verifies that after participation, the event is no longer in the browser events list.
     * Verifies that after participation, the event is in the upcoming events list.
     */
    @Test
    fun AlertDialogDisplayWhenClickingOnBrowserEventAndCanParticipate() { runTest {
        val auth = FirebaseEmulator.auth

        try {
            auth.signInAnonymously().await()
            val aliceId = auth.currentUser?.uid ?: error("Alice auth failed")
            val eventByAlice = createYourEvent(aliceId)
            repository.addEvent(eventByAlice)

            auth.signOut()
            delay(100)

            auth.signInAnonymously().await()
            val bobId = auth.currentUser?.uid ?: error("Bob auth failed")

            composeTestRule.setContent {
                EventsScreen(
                    eventsViewModel = EventsViewModel(
                        repository = repository,
                        currentUserId = bobId
                    )
                )
            }

            composeTestRule.waitForIdle()

            composeTestRule
                .onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(eventByAlice))
                .assertIsDisplayed()
            composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_UPCOMING_LIST_MSG)
                .assertIsDisplayed()

            composeTestRule.clickEventItem(eventByAlice)
            composeTestRule.onNodeWithTag(EventsScreenTestTags.EVENT_POPUP).assertIsDisplayed()
            composeTestRule.onNodeWithTag(EventsScreenTestTags.GOBACK_EVENT_BUTTON).assertIsDisplayed()
            composeTestRule.onNodeWithTag(EventsScreenTestTags.PARTICIPATE_BUTTON)
                .assertIsDisplayed()
                .performClick()

            advanceUntilIdle()

            composeTestRule.onNodeWithTag(EventsScreenTestTags.EVENT_POPUP).assertIsNotDisplayed()
            composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_BROWSER_LIST_MSG)
                .assertIsDisplayed()
            composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_UPCOMING_LIST_MSG)
                .assertIsNotDisplayed()
            composeTestRule
                .onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(eventByAlice))
                .assertIsDisplayed()

        } finally {
            // Sign Out
            auth.signOut()
        }
    }

    }

    /**
     * Test: scenario:
     * Log in as Anonym Alice, create her own event, log out.
     * Log in as Anonym Bob, and get his screen.
     * Click on the Alice Event in browser events list.
     * Verifies that the AlertDialog is displayed with the correct information.
     * Verifies that the "Cancel" button is displayed.
     * Verifies that Bob can go back to the overview screen without participating to the event.
     */
    @Test
    fun AlertDialogDisplayWhenClickingOnBrowserEventAndReturnToOverview() { runTest {
        val auth = FirebaseEmulator.auth

        try {
            auth.signInAnonymously().await()
            val aliceId = auth.currentUser?.uid ?: error("Alice auth failed")
            val eventByAlice = createYourEvent(aliceId)
            repository.addEvent(eventByAlice)

            auth.signOut()
            delay(100)

            auth.signInAnonymously().await()
            val bobId = auth.currentUser?.uid ?: error("Bob auth failed")

            composeTestRule.setContent {
                EventsScreen(
                    eventsViewModel = EventsViewModel(
                        repository = repository,
                        currentUserId = bobId
                    )
                )
            }

            composeTestRule.waitForIdle()

            composeTestRule
                .onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(eventByAlice))
                .assertIsDisplayed()
            composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_UPCOMING_LIST_MSG)
                .assertIsDisplayed()

            composeTestRule.clickEventItem(eventByAlice)
            composeTestRule.onNodeWithTag(EventsScreenTestTags.EVENT_POPUP).assertIsDisplayed()
            composeTestRule.onNodeWithTag(EventsScreenTestTags.GOBACK_EVENT_BUTTON).assertIsDisplayed()
            composeTestRule.onNodeWithTag(EventsScreenTestTags.PARTICIPATE_BUTTON).assertIsDisplayed()
            composeTestRule.onNodeWithTag(EventsScreenTestTags.GOBACK_EVENT_BUTTON).performClick()

            composeTestRule.onNodeWithTag(EventsScreenTestTags.EVENT_POPUP).assertIsNotDisplayed()
            composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_BROWSER_LIST_MSG)
                .assertIsNotDisplayed()
            composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_UPCOMING_LIST_MSG)
                .assertIsDisplayed()
            composeTestRule
                .onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(eventByAlice))
                .assertIsDisplayed()

        } finally {
            // Sign Out
            auth.signOut()
        }
    }

    }

    /**
     * Test: scenario:
     * Log in as Anonym Alice, create her own event, log out.
     * Log in as Anonym Bob, and get his screen.
     * Click on the Alice Event in browser events list so that Bob can participate to it.
     * Verifies that the event is now in the upcoming events list.
     * Click on the Alice Event in upcoming events list.
     * Verifies that the AlertDialog is displayed with the correct information.
     * Verifies that the "Unregister" button is displayed.
     * Verifies that Bob can be unregister and that the event is now in the browser events list.
     */
    @Test
    fun testUserCanUnregisterFromUpcomingEvent() { runTest {
        val auth = FirebaseEmulator.auth

        try {
            auth.signInAnonymously().await()
            val aliceId = auth.currentUser?.uid ?: error("Alice auth failed")
            val eventByAlice = createYourEvent(aliceId)
            repository.addEvent(eventByAlice)

            auth.signOut()
            delay(100)

            auth.signInAnonymously().await()
            val bobId = auth.currentUser?.uid ?: error("Bob auth failed")

            composeTestRule.setContent {
                EventsScreen(
                    eventsViewModel = EventsViewModel(
                        repository = repository,
                        currentUserId = bobId
                    )
                )
            }

            composeTestRule.waitForIdle()

            composeTestRule
                .onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(eventByAlice))
                .assertIsDisplayed()
            composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_UPCOMING_LIST_MSG)
                .assertIsDisplayed()
            composeTestRule.clickEventItem(eventByAlice)
            composeTestRule.onNodeWithTag(EventsScreenTestTags.EVENT_POPUP).assertIsDisplayed()
            composeTestRule.onNodeWithTag(EventsScreenTestTags.PARTICIPATE_BUTTON)
                .assertIsDisplayed().performClick()

            composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_UPCOMING_LIST_MSG)
                .assertIsNotDisplayed()

            composeTestRule.clickEventItem(eventByAlice)
            composeTestRule.onNodeWithTag(EventsScreenTestTags.EVENT_POPUP).assertIsDisplayed()
            composeTestRule.onNodeWithTag(EventsScreenTestTags.UNREGISTER_BUTTON)
                .assertIsDisplayed().performClick()
            composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_UPCOMING_LIST_MSG)
                .assertIsDisplayed()
            composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_BROWSER_LIST_MSG)
                .assertIsNotDisplayed()

        } finally {
            // Sign Out
            auth.signOut()
        }
    }

    }

    /**
     * Test: scenario:
     * Log in as Anonym Alice, create her own event, log out.
     * Log in as Anonym Bob, and get his screen.
     * Click on the Alice Event in browser events list so that Bob can participate to it.
     * Verifies that the event is now in the upcoming events list.
     * Click on the Alice Event in upcoming events list.
     * Verifies that the AlertDialog is displayed with the correct information.
     * Verifies that the "Cancel" button is displayed.
     * Verifies that Bob can still be register and goes back to overview.
     */
    @Test
    fun testPopUpCancelButtonFromUpcomingEventWorks() {
        runTest {
            val auth = FirebaseEmulator.auth

            try {
                auth.signInAnonymously().await()
                val aliceId = auth.currentUser?.uid ?: error("Alice auth failed")
                val eventByAlice = createYourEvent(aliceId)
                repository.addEvent(eventByAlice)

                auth.signOut()
                delay(100)

                auth.signInAnonymously().await()
                val bobId = auth.currentUser?.uid ?: error("Bob auth failed")

                composeTestRule.setContent {
                    EventsScreen(
                        eventsViewModel = EventsViewModel(
                            repository = repository,
                            currentUserId = bobId
                        )
                    )
                }

                composeTestRule.waitForIdle()

                composeTestRule
                    .onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(eventByAlice))
                    .assertIsDisplayed()
                composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_UPCOMING_LIST_MSG)
                    .assertIsDisplayed()
                composeTestRule.clickEventItem(eventByAlice)
                composeTestRule.onNodeWithTag(EventsScreenTestTags.EVENT_POPUP).assertIsDisplayed()
                composeTestRule.onNodeWithTag(EventsScreenTestTags.PARTICIPATE_BUTTON)
                    .assertIsDisplayed().performClick()

                composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_UPCOMING_LIST_MSG)
                    .assertIsNotDisplayed()

                composeTestRule.clickEventItem(eventByAlice)
                composeTestRule.onNodeWithTag(EventsScreenTestTags.EVENT_POPUP).assertIsDisplayed()
                composeTestRule.onNodeWithTag(EventsScreenTestTags.GOBACK_EVENT_BUTTON)
                    .assertIsDisplayed().performClick()
                composeTestRule.onNodeWithTag(EventsScreenTestTags.EVENT_POPUP)
                    .assertIsNotDisplayed()
                composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_BROWSER_LIST_MSG)
                    .assertIsDisplayed()

            } finally {
                // Sign Out
                auth.signOut()
            }
        }
    }

    /**
     * Test: scenario:
     * Log in as Anonym Bob, create his own event.
     * Verifies that Bob does see his event in "Your Events" list.
     * Click on the Bob Event in "Your Events" list.
     * Verifies that the AlertDialog is displayed with the correct information.
     * Verifies that the "Edit" button is displayed.
     * Verifies that Bob can click on the "Edit" button.
     * Verifies that Bob is redirected to the Edit Event screen.
     */
    @Test
    fun testCanEditAnEvent(){
        runTest {
            val auth = FirebaseEmulator.auth

            try {

                auth.signInAnonymously().await()
                val bobId = auth.currentUser?.uid ?: error("Bob auth failed")
                val eventByBob = createYourEvent(bobId)
                repository.addEvent(eventByBob)

                composeTestRule.setContent {
                    EventsScreen(
                        eventsViewModel = EventsViewModel(
                            repository = repository,
                            currentUserId = bobId
                        )
                    )
                }

                composeTestRule.waitForIdle()
                composeTestRule
                    .onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(eventByBob))
                    .assertIsDisplayed()
                composeTestRule.clickEventItem(eventByBob)
                composeTestRule.onNodeWithTag(EventsScreenTestTags.EVENT_POPUP).assertIsDisplayed()
                composeTestRule.onNodeWithTag(EventsScreenTestTags.EDIT_EVENT_BUTTON)
                    .assertIsDisplayed()
                    .performClick()
                composeTestRule.checkEditEventScreenIsDisplayed()


            } finally {
                // Sign Out
                auth.signOut()
            }
        }

    }

    /**
     * Test: scenario:
     * Log in as Anonym Bob, create his own event.
     * Verifies that Bob does see his event in "Your Events" list.
     * Click on the Bob Event in "Your Events" list.
     * Verifies that the AlertDialog is displayed with the correct information.
     * Verifies that the "Cancel" button is displayed.
     * Verifies that Bob can click on the "Cancel" button.
     * Verifies that the pop up is closed and Bob is back to the overview screen.
     */
    @Test
    fun testCanCloseAlertDialogYourEvent() {

        runTest {
            val auth = FirebaseEmulator.auth

            try {

                auth.signInAnonymously().await()
                val bobId = auth.currentUser?.uid ?: error("Bob auth failed")
                val eventByBob = createYourEvent(bobId)
                repository.addEvent(eventByBob)

                composeTestRule.setContent {
                    EventsScreen(
                        eventsViewModel = EventsViewModel(
                            repository = repository,
                            currentUserId = bobId
                        )
                    )
                }

                composeTestRule.waitForIdle()
                composeTestRule
                    .onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(eventByBob))
                    .assertIsDisplayed()
                composeTestRule.clickEventItem(eventByBob)
                composeTestRule.onNodeWithTag(EventsScreenTestTags.EVENT_POPUP).assertIsDisplayed()
                composeTestRule.onNodeWithTag(EventsScreenTestTags.GOBACK_EVENT_BUTTON)
                    .assertIsDisplayed()
                    .performClick()
                composeTestRule.onNodeWithTag(EventsScreenTestTags.EVENT_POPUP)
                    .assertIsNotDisplayed()


            } finally {
                // Sign Out
                auth.signOut()
            }
        }




    }


    /**
     * Test: scenario:
     * Log in as Anonym Bob, want to create an event.
     * Verifies that the "Create Event" button is displayed.
     * Verifies that Bob can click on the "Create Event" button.
     * Verifies that Bob is redirected to the Create Event screen.
     */
    @Test
    fun testCanClickOnCreateEventButton(){

        runTest {
            val auth = FirebaseEmulator.auth

            try {

                auth.signInAnonymously().await()
                val bobId = auth.currentUser?.uid ?: error("Bob auth failed")

                composeTestRule.setContent {
                    EventsScreen(
                        eventsViewModel = EventsViewModel(
                            repository = repository,
                            currentUserId = bobId
                        )
                    )
                }

                composeTestRule.waitForIdle()
                composeTestRule.onNodeWithTag(EventsScreenTestTags.CREATE_EVENT_BUTTON)
                    .assertIsDisplayed().performClick()
                composeTestRule.checkAddEventScreenIsDisplayed()



            } finally {
                // Sign Out
                auth.signOut()
            }
        }

    }

    // ///////////////////// UTILS

    /**
     * Helper function to use when we want to click on a specific event item
     */
    fun ComposeTestRule.clickEventItem(event : Event){
        waitUntilEventIsDisplayed(event).performClick()
    }

    /**
     * Helper function to use when we want to check if the current screen displaying is the events overview
     * screen
     */
    private fun ComposeTestRule.waitUntilEventIsDisplayed(event: Event): SemanticsNodeInteraction {
        composeTestRule.checkEventsScreenIsDisplayed()
        waitUntil(UI_WAIT_TIMEOUT) {
            onAllNodesWithTag(EventsScreenTestTags.getTestTagForEventItem(event))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        return checkEventItemIsDisplayed(event)
    }

    /**
     * Helper function to use when we want to check if a specific event
     * item is displayed on the screen
     */
    fun ComposeTestRule.checkEventItemIsDisplayed(event: Event): SemanticsNodeInteraction =
        onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(event)).assertIsDisplayed()

    /**
     * Helper function to use when we want to check if the current screen displaying is the events overview
     * screen
     */
    fun ComposeTestRule.checkEventsScreenIsDisplayed() {
        onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
            .assertTextContains("Events", substring = true, ignoreCase = true)
    }

    /**
     * Helper function to use when we want to check if the current screen displaying is the edit event
     * screen
     */
    fun ComposeTestRule.checkEditEventScreenIsDisplayed() {
         onNodeWithTag(EditEventScreenTestTags.EDITEVENTTEXT).assertIsDisplayed()}


    /**
     * Helper function to use when we want to check if the current screen displaying is the add event
     * screen
     */
    fun ComposeTestRule.checkAddEventScreenIsDisplayed() {
        onNodeWithTag(AddEventScreenTestTags.ADDEVENTTEXT).assertIsDisplayed()
    }

    /**
     * Helper function to use when we want to check if a specific event
     * item is displayed on the screen with a specific matcher
     */
    fun ComposeTestRule.onEventItem(event: Event, matcher: SemanticsMatcher) {
        val eventNode = this.waitUntilEventIsDisplayed(event)
        onNode(
            hasTestTag(EventsScreenTestTags.getTestTagForEventItem(event))
                .and(hasAnyDescendant(matcher)),
            useUnmergedTree = true)
            .assertIsDisplayed()
    }

}
