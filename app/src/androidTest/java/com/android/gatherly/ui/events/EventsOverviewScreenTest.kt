package com.android.gatherly.ui.events

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.model.event.Event
import com.android.gatherly.model.event.EventStatus
import com.android.gatherly.model.map.Location
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.utils.FirestoreEventsGatherlyTest
import com.android.gatherly.utils.GatherlyTest.Companion.fromDate
import com.android.gatherly.utils.UI_WAIT_TIMEOUT
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Calendar
import java.util.NoSuchElementException
import kotlin.collections.forEach


class EventsOverviewScreenTest : FirestoreEventsGatherlyTest()   {

    @get:Rule val composeTestRule = createComposeRule()

    val dateB = Timestamp.Companion.fromDate(2025, Calendar.OCTOBER, 25)
    val start = SimpleDateFormat("HH:mm").parse("12:00") ?: throw NoSuchElementException("no date ")
    val finish = SimpleDateFormat("HH:mm").parse("23:00") ?: throw NoSuchElementException("no date ")

    val eventB =
        Event(
            id = "browser",
            title = "DOG Event",
            description = "Public Event who allows only people who comes with their dog",
            creatorName = "Gersende",
            location = Location(46.520278, 6.565556, "EPFL"),
            date = dateB,
            startTime = Timestamp(start),
            endTime = Timestamp(finish),
            creatorId = "GersendeID",
            participants = listOf("Colombe", "Valentin"),
            status = EventStatus.UPCOMING,
        )

    val eventU =
        Event(
            id = "upcoming",
            title = "Swent meeting",
            description = "Swent meeting with team22, need to go the special room in MED building",
            creatorName = "COACH",
            location = Location(46.520278, 6.565556, "EPFL"),
            date = dateB,
            startTime = Timestamp(start),
            endTime = Timestamp(finish),
            creatorId = "coachID",
            participants = listOf("Colombe", "Gersende", Firebase.auth.currentUser.toString()),
            status = EventStatus.UPCOMING,
        )

    val eventY =
        Event(
            id = "mine",
            title = "working session with Gab",
            description = "Need to work in the CO with Gab, in order to finish the TDS homework",
            creatorName = "Sofija",
            location = Location(46.520278, 6.565556, "EPFL"),
            date = dateB,
            startTime = Timestamp(start),
            endTime = Timestamp(finish),
            creatorId = Firebase.auth.currentUser.toString(),
            participants = listOf("Gabriel"),
            status = EventStatus.UPCOMING,
        )


    fun setContent(withInitialEvents: List<Event> = emptyList()) {
        runTest { withInitialEvents.forEach { repository.addEvent(it) } }
        composeTestRule.setContent { EventsScreen() }
    }

    @Test
    fun testTagsCorrectlySetWhenListsAreEmpty() {
        setContent()
        composeTestRule.onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
            .assertTextContains("Events", substring = true, ignoreCase = true)
        composeTestRule.onNodeWithTag(EventsScreenTestTags.EVENT_POPUP).assertIsNotDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.BROWSE_TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_BROWSER_LIST_MSG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.UPCOMING_TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_UPCOMING_LIST_MSG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.YOUR_EVENTS_TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_OUREVENTS_LIST_MSG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.CREATE_EVENT_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.ALL_LISTS).assertIsDisplayed()
    }

    @Test
    fun testTagsCorrectlySetWhenListsAreNotEmpty() {
        setContent(withInitialEvents = listOf(eventB, eventU, eventY))
        composeTestRule.onNodeWithTag(EventsScreenTestTags.ALL_LISTS).assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(eventB))
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_BROWSER_LIST_MSG)
            .assertIsNotDisplayed()
        composeTestRule
            .onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(eventU))
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_UPCOMING_LIST_MSG)
            .assertIsNotDisplayed()
        composeTestRule
            .onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(eventY))
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_OUREVENTS_LIST_MSG)
            .assertIsNotDisplayed()
    }


    @Test
    fun EventItemDisplaysEventTitle() {
        val eventList = listOf(eventB, eventU, eventY)
        setContent(withInitialEvents = eventList)
        composeTestRule.onEventItem(eventB, hasTestTag(EventsScreenTestTags.EVENT_TITLE))
        composeTestRule.onEventItem(eventY, hasTestTag(EventsScreenTestTags.EVENT_TITLE))
        composeTestRule.onEventItem(eventU, hasTestTag(EventsScreenTestTags.EVENT_TITLE))
    }

    @Test
    fun EventItemDisplaysStartDate() {
        val eventList = listOf(eventB, eventY, eventU)
        setContent(withInitialEvents = eventList)
        composeTestRule.onEventItem(eventB, hasTestTag(EventsScreenTestTags.EVENT_DATE))
        composeTestRule.onEventItem(eventY, hasTestTag(EventsScreenTestTags.EVENT_DATE))
        composeTestRule.onEventItem(eventU, hasTestTag(EventsScreenTestTags.EVENT_DATE))
    }



    fun ComposeTestRule.onEventItem(event: Event, matcher: SemanticsMatcher) {
        val eventNode = this.waitUntilEventIsDisplayed(event)
        onNode(
            hasTestTag(EventsScreenTestTags.getTestTagForEventItem(event))
                .and(hasAnyDescendant(matcher)),
            useUnmergedTree = true)
            .assertIsDisplayed()
    }


    @Test
    fun AlertDialogDisplayWhenClicking() {
        val eventList = listOf(eventB, eventY, eventU)
        setContent(withInitialEvents = eventList)
        composeTestRule.clickEventItem(eventB)
        composeTestRule.onNodeWithTag(EventsScreenTestTags.EVENT_POPUP).assertIsDisplayed()
    }

    @Test
    fun CanCloseAlertDialogBrowserEvent() {
        val eventList = listOf(eventB, eventY, eventU)
        setContent(withInitialEvents = eventList)
        composeTestRule.clickEventItem(eventB)
        composeTestRule.onNodeWithTag(EventsScreenTestTags.EVENT_POPUP).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.PARTICIPATE_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.GOBACK_EVENT_BUTTON)
            .assertIsDisplayed()
            .performClick()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.EVENT_POPUP).assertIsNotDisplayed()

    }
    @Test
    fun CanParticipateBrowserEvent() {
        val eventList = listOf(eventB, eventY,)
        setContent(withInitialEvents = eventList)
        composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_UPCOMING_LIST_MSG).assertIsDisplayed()
        composeTestRule.clickEventItem(eventB)
        composeTestRule.onNodeWithTag(EventsScreenTestTags.EVENT_POPUP).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.GOBACK_EVENT_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.PARTICIPATE_BUTTON)
            .assertIsDisplayed()
            .performClick()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.EVENT_POPUP).assertIsNotDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_BROWSER_LIST_MSG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_UPCOMING_LIST_MSG).assertIsNotDisplayed()
    }

    @Test
    fun CanCloseAlertDialogUpcomingEvent() {
        val eventList = listOf(eventB, eventY, eventU)
        setContent(withInitialEvents = eventList)
        composeTestRule.clickEventItem(eventU)
        composeTestRule.onNodeWithTag(EventsScreenTestTags.EVENT_POPUP).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.UNREGISTER_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.GOBACK_EVENT_BUTTON)
            .assertIsDisplayed()
            .performClick()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.EVENT_POPUP).assertIsNotDisplayed()
    }
    @Test

    fun CanUnregisterUpcomigEvent() {
        val eventList = listOf(eventY, eventU)
        setContent(withInitialEvents = eventList)
        composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_BROWSER_LIST_MSG).assertIsDisplayed()
        composeTestRule.clickEventItem(eventU)
        composeTestRule.onNodeWithTag(EventsScreenTestTags.EVENT_POPUP).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.GOBACK_EVENT_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.UNREGISTER_BUTTON)
            .assertIsDisplayed()
            .performClick()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.EVENT_POPUP).assertIsNotDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_UPCOMING_LIST_MSG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_BROWSER_LIST_MSG).assertIsNotDisplayed()
    }

    @Test
    fun CanCloseAlertDialogYourOwnEvent() {
        val eventList = listOf(eventB, eventY, eventU)
        setContent(withInitialEvents = eventList)
        composeTestRule.clickEventItem(eventY)
        composeTestRule.onNodeWithTag(EventsScreenTestTags.EVENT_POPUP).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.CANCEL_EVENT_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.GOBACK_EVENT_BUTTON)
            .assertIsDisplayed()
            .performClick()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.EVENT_POPUP).assertIsNotDisplayed()
    }

    @Test
    fun CanCancelYourOwnEvent() {
        val eventList = listOf(eventY, eventU, eventB)
        setContent(withInitialEvents = eventList)
        composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_BROWSER_LIST_MSG).assertIsDisplayed()
        composeTestRule.clickEventItem(eventY)
        composeTestRule.onNodeWithTag(EventsScreenTestTags.EVENT_POPUP).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.GOBACK_EVENT_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.CANCEL_EVENT_BUTTON)
            .assertIsDisplayed()
            .performClick()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.EVENT_POPUP).assertIsNotDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_OUREVENTS_LIST_MSG).assertIsDisplayed()
    }

    @Test
    fun ClickOnCreateEventButtonNavigateToAddEventScreen() {
        val eventList = listOf(eventY, eventU, eventB)
        setContent(withInitialEvents = eventList)
        composeTestRule.onNodeWithTag(EventsScreenTestTags.CREATE_EVENT_BUTTON).assertIsDisplayed().performClick()
        //TODO: composeTestRule.checkIsAddEventIsDisplay()

    }




    // ///////////////////// UTILS
    fun ComposeTestRule.clickEventItem(event : Event){
        waitUntilEventIsDisplayed(event).performClick()
    }
    private fun ComposeTestRule.waitUntilEventIsDisplayed(event: Event): SemanticsNodeInteraction {
        //checkEventsScreenIsDisplayed()
        waitUntil(UI_WAIT_TIMEOUT) {
            onAllNodesWithTag(EventsScreenTestTags.getTestTagForEventItem(event))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        return checkEventItemIsDisplayed(event)
    }
    fun ComposeTestRule.checkEventItemIsDisplayed(event: Event): SemanticsNodeInteraction =
        onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(event)).assertIsDisplayed()

}
