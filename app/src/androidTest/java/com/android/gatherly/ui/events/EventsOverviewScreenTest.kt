package com.android.gatherly.ui.events

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.android.gatherly.model.event.Event
import com.android.gatherly.model.event.EventStatus
import com.android.gatherly.model.event.EventsLocalRepository
import com.android.gatherly.model.event.EventsRepository
import com.android.gatherly.model.map.Location
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.utils.AlertDialogTestTags
import com.android.gatherly.utils.MapCoordinator
import com.android.gatherly.utils.MockitoUtils
import com.android.gatherly.utils.UI_WAIT_TIMEOUT
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.NoSuchElementException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EventsOverviewScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var currentUserId: String
  private lateinit var eventsRepository: EventsRepository

  private lateinit var profileRepository: ProfileRepository
  private lateinit var eventsViewModel: EventsViewModel
  private lateinit var mockitoUtils: MockitoUtils
  private lateinit var mapCoordinator: MapCoordinator

  @Before
  fun setUp() {
    eventsRepository = EventsLocalRepository()
    profileRepository = ProfileLocalRepository()
    currentUserId = ""

    mapCoordinator = MapCoordinator()

    // Mock Firebase Auth
    mockitoUtils = MockitoUtils()
  }

  private val start =
      SimpleDateFormat("HH:mm").parse("12:00") ?: throw NoSuchElementException("no date ")
  private val finish =
      SimpleDateFormat("HH:mm").parse("23:00") ?: throw NoSuchElementException("no date ")

  /** Helper function: set the content of the composeTestRule without initial events */
  private fun setContent(uid: String = currentUserId) {
    mockitoUtils.chooseCurrentUser(uid)
    currentUserId = uid
    eventsViewModel =
        EventsViewModel(
            eventsRepository = eventsRepository,
            profileRepository = profileRepository,
            authProvider = { mockitoUtils.mockAuth })
    composeTestRule.setContent {
      EventsScreen(
          eventsViewModel = eventsViewModel,
          actions = EventsScreenActions(),
          coordinator = mapCoordinator)
    }
  }

  /** Helper function to create an event for the current user */
  private fun createYourEvent(currentUserId: String): Event {
    return Event(
        id = "mine",
        title = "working session with Gab",
        description = "Need to work in the CO with Gab, in order to finish the TDS homework",
        creatorName = "Sofija",
        location = Location(46.520278, 6.565556, "EPFL"),
        date = tomorrowTimestamp,
        startTime = Timestamp(start),
        endTime = Timestamp(finish),
        creatorId = currentUserId,
        participants = listOf("Gabriel", "Sofija"),
        status = EventStatus.UPCOMING)
  }

  // Upcoming EVENT
  private val tomorrowTimestamp = Timestamp(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)))
  private val upcomingEvent =
      Event(
          id = "upcomingEventId1",
          title = "Tomorrow's Event 1",
          description = "An event happening tomorrow",
          creatorName = "Alice",
          location = Location(46.520278, 6.565556, "EPFL"),
          date = tomorrowTimestamp,
          startTime = Timestamp(start),
          endTime = Timestamp(finish),
          creatorId = "aliceId",
          participants = listOf(),
          status = EventStatus.UPCOMING)

  private val upcomingEventParticipate =
      Event(
          id = "upcomingEventIdParticipate",
          title = "Tomorrow's Event 2",
          description = "An event happening tomorrow",
          creatorName = "Alice",
          location = Location(46.520278, 6.565556, "EPFL"),
          date = tomorrowTimestamp,
          startTime = Timestamp(start),
          endTime = Timestamp(finish),
          creatorId = "aliceId",
          participants = listOf("bobId"),
          status = EventStatus.UPCOMING)

  private val upcomingEventCreated =
      Event(
          id = "CreatedUpcomingEventId",
          title = "Tomorrow's Event 3",
          description = "An event happening tomorrow",
          creatorName = "Bob",
          location = Location(46.520278, 6.565556, "EPFL"),
          date = tomorrowTimestamp,
          startTime = Timestamp(start),
          endTime = Timestamp(finish),
          creatorId = "bobId",
          participants = listOf(),
          status = EventStatus.UPCOMING)

  // ONGOING EVENT
  private val oneHourAgo = Timestamp(Date(System.currentTimeMillis() - 3600_000))
  private val oneHourLater = Timestamp(Date(System.currentTimeMillis() + 3600_000))
  private val ongoingEvent =
      Event(
          id = "ongoing",
          title = "Ongoing Event 1",
          description = "An event currently happening",
          creatorName = "Alice",
          location = Location(46.520278, 6.565556, "EPFL"),
          date = Timestamp.now(),
          startTime = oneHourAgo,
          endTime = oneHourLater,
          creatorId = "aliceId",
          participants = listOf(),
          status = EventStatus.ONGOING)
  private val ongoingEventParticipating =
      Event(
          id = "ongoing2",
          title = "Ongoing Event 2",
          description = "An event currently happening",
          creatorName = "Alice",
          location = Location(46.520278, 6.565556, "EPFL"),
          date = Timestamp.now(),
          startTime = oneHourAgo,
          endTime = oneHourLater,
          creatorId = "aliceId",
          participants = listOf("bobId"),
          status = EventStatus.ONGOING)
  private val ongoingEventCreated =
      Event(
          id = "ongoing3",
          title = "Ongoing Event 3",
          description = "An event currently happening",
          creatorName = "Bob",
          location = Location(46.520278, 6.565556, "EPFL"),
          date = Timestamp.now(),
          startTime = oneHourAgo,
          endTime = oneHourLater,
          creatorId = "bobId",
          participants = listOf(),
          status = EventStatus.ONGOING)

  // PAST EVENT
  private val yesterdayTimestamp = Timestamp(Date.from(Instant.now().minus(1, ChronoUnit.DAYS)))
  private val pastEvent =
      Event(
          id = "past",
          title = "Past Event 1",
          description = "An event is already past",
          creatorName = "Alice",
          location = Location(46.520278, 6.565556, "EPFL"),
          date = yesterdayTimestamp,
          startTime = Timestamp(start),
          endTime = Timestamp(finish),
          creatorId = "aliceId",
          participants = listOf(),
          status = EventStatus.PAST)

  private val pastEventParticipating =
      Event(
          id = "past2",
          title = "Past Event 2",
          description = "An event is already past",
          creatorName = "Alice",
          location = Location(46.520278, 6.565556, "EPFL"),
          date = yesterdayTimestamp,
          startTime = Timestamp(start),
          endTime = Timestamp(finish),
          creatorId = "aliceId",
          participants = listOf("bobId"),
          status = EventStatus.PAST)
  private val pastEventCreated =
      Event(
          id = "past3",
          title = "Past Event 3",
          description = "An event is already past",
          creatorName = "Bob",
          location = Location(46.520278, 6.565556, "EPFL"),
          date = yesterdayTimestamp,
          startTime = Timestamp(start),
          endTime = Timestamp(finish),
          creatorId = "bobId",
          participants = listOf(),
          status = EventStatus.PAST)

  /**
   * Test: Verifies that when there is no event registered, all relevant UI components are displayed
   * correctly.
   */
  @Test
  fun testTagsCorrectlySetWhenListsAreEmpty() {
    setContent()
    composeTestRule
        .onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
        .assertTextContains("Events", substring = true, ignoreCase = true)
    composeTestRule.onNodeWithTag(AlertDialogTestTags.ALERT).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(EventsScreenTestTags.BROWSE_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_BROWSER_LIST_MSG).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventsScreenTestTags.UPCOMING_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_UPCOMING_LIST_MSG).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventsScreenTestTags.YOUR_EVENTS_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_OUREVENTS_LIST_MSG).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventsScreenTestTags.CREATE_EVENT_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventsScreenTestTags.ALL_LISTS).assertIsDisplayed()
  }

  /**
   * Test : Log in as Anonym Bob, create her own event. Verifies that Bob does see her event in
   * "Your Events" list. Verifies if the title and date of the event are correctly displayed.
   */
  @Test
  fun testYourEventDisplayCorrectly() = runTest {
    // Sign in as Bob
    val bobId = "bobId"
    val eventByBob = createYourEvent(bobId)
    eventsRepository.addEvent(eventByBob)

    setContent(bobId)

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(EventsScreenTestTags.BROWSE_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_BROWSER_LIST_MSG).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventsScreenTestTags.UPCOMING_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_UPCOMING_LIST_MSG).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventsScreenTestTags.YOUR_EVENTS_TITLE).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(EventsScreenTestTags.EMPTY_OUREVENTS_LIST_MSG)
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(eventByBob))
        .assertIsDisplayed()
    composeTestRule.onEventItem(eventByBob, hasTestTag(EventsScreenTestTags.EVENT_TITLE))
    composeTestRule.onEventItem(eventByBob, hasTestTag(EventsScreenTestTags.EVENT_DATE))
  }

  /**
   * Test: scenario: Log in as Anonym Alice, create her own event, log out. Log in as Anonym Bob,
   * and get his screen. Verifies that Bob does see Alice's event in browser events list. Verifies
   * if the title and date of the event are correctly displayed.
   */
  @Test
  fun testBrowserEventDisplayCorrectly() = runTest {
    // Sign in as Alice
    val aliceId = "aliceId"
    // Create event by Alice
    val eventByAlice = createYourEvent(aliceId)
    eventsRepository.addEvent(eventByAlice)

    // Sign in as Bob
    val bobId = "bobId"

    setContent(bobId)

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(eventByAlice))
        .assertIsDisplayed()
    // Check that the event created by Alice show up in the Bob's browser list
    composeTestRule
        .onNodeWithTag(EventsScreenTestTags.EMPTY_BROWSER_LIST_MSG)
        .assertIsNotDisplayed()

    composeTestRule.onNodeWithTag(EventsScreenTestTags.UPCOMING_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_UPCOMING_LIST_MSG).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventsScreenTestTags.YOUR_EVENTS_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_OUREVENTS_LIST_MSG).assertIsDisplayed()

    // Check that the event details (title and date) are correctly displayed
    composeTestRule.onEventItem(eventByAlice, hasTestTag(EventsScreenTestTags.EVENT_TITLE))
    composeTestRule.onEventItem(eventByAlice, hasTestTag(EventsScreenTestTags.EVENT_DATE))
  }

  /**
   * Test: scenario: Log in as Anonym Alice, create her own event, log out. Log in as Anonym Bob,
   * and get his screen. Verifies that Bob does see Alice's event in browser events list. Click on
   * the Alice Event in browser events list. Verifies that the AlertDialog is displayed Click on
   * Participate button. Verifies that Bob is added to the participants list of the event.
   */
  @Test
  fun testUpcomingEventDisplayCorrectly() = runTest {
    // Sign in as Alice
    val aliceId = "aliceId"
    // Create event by Alice
    val eventByAlice = createYourEvent(aliceId)
    eventsRepository.addEvent(eventByAlice)

    // Sign in as Bob
    val bobId = "bobId"

    setContent(bobId)

    profileRepository.addProfile(Profile(uid = "bobId", name = "Test User", profilePicture = ""))

    composeTestRule.waitForIdle()

    // Check that the event created by Alice show up in the Bob's browser list
    composeTestRule
        .onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(eventByAlice))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(EventsScreenTestTags.EMPTY_BROWSER_LIST_MSG)
        .assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_UPCOMING_LIST_MSG).assertIsDisplayed()

    // Click on the event item
    composeTestRule.clickEventItem(eventByAlice)
    // Check that the popup is displayed
    composeTestRule.onNodeWithTag(AlertDialogTestTags.ALERT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AlertDialogTestTags.DISMISS_BTN).assertIsDisplayed()

    // Click on Participate button
    composeTestRule
        .onNodeWithTag(AlertDialogTestTags.CONFIRM_BTN)
        .assertIsDisplayed()
        .performClick()

    // Wait until the popup is closed
    composeTestRule.waitUntil(UI_WAIT_TIMEOUT) {
      composeTestRule.onAllNodesWithTag(AlertDialogTestTags.ALERT).fetchSemanticsNodes().isEmpty()
    }
    composeTestRule.waitForIdle()

    // Verify that Bob is added to the participants list of the event
    val updated = eventsRepository.getEvent(eventByAlice.id)
    composeTestRule.waitUntil(UI_WAIT_TIMEOUT) { updated.participants.contains(bobId) }

    // Verify that the event is no longer in the browser events list
    // and is now in the upcoming events list
    composeTestRule.waitUntil(UI_WAIT_TIMEOUT) {
      composeTestRule
          .onAllNodesWithTag(EventsScreenTestTags.EMPTY_BROWSER_LIST_MSG)
          .fetchSemanticsNodes()
          .isNotEmpty() &&
          composeTestRule
              .onAllNodesWithTag(EventsScreenTestTags.EMPTY_UPCOMING_LIST_MSG)
              .fetchSemanticsNodes()
              .isEmpty()
    }

    composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_BROWSER_LIST_MSG).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(EventsScreenTestTags.EMPTY_UPCOMING_LIST_MSG)
        .assertIsNotDisplayed()
  }

  /**
   * Test: scenario: Log in as Anonym Alice, create her own event, log out. Log in as Anonym Bob,
   * and get his screen. Bob want to participate to Alice's event. Clicks on the event. Click on
   * Participate button. Verifies that Bob is added to the participants list of the event. Then Bob
   * want to unregister from the event. Clicks on the event. Click on Unregister button. Verifies
   * that Bob is removed from the participants. Verifies that the event is back in the browser
   * events list.
   */
  @Test
  fun testUnregisterUpcomingEventCorrectly() = runTest {
    // Sign in as Alice
    val aliceId = "aliceId"
    // Create event by Alice
    val eventByAlice = createYourEvent(aliceId)
    eventsRepository.addEvent(eventByAlice)

    // Sign in as Bob
    val bobId = "bobId"

    setContent(bobId)

    profileRepository.addProfile(Profile(uid = "bobId", name = "Test User", profilePicture = ""))

    // Verify that Alice's event is displayed
    composeTestRule.waitUntil(UI_WAIT_TIMEOUT) {
      runBlocking { eventsRepository.getAllEvents().contains(eventByAlice) }
    }

    composeTestRule
        .onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(eventByAlice))
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(EventsScreenTestTags.EMPTY_BROWSER_LIST_MSG)
        .assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_UPCOMING_LIST_MSG).assertIsDisplayed()

    // Click on the event item and participate button
    composeTestRule.clickEventItem(eventByAlice)
    composeTestRule.onNodeWithTag(AlertDialogTestTags.ALERT).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(AlertDialogTestTags.CONFIRM_BTN)
        .assertIsDisplayed()
        .performClick()

    // Wait to add Bob as participant
    val updatedAliceEvent = eventsRepository.getEvent(eventByAlice.id)
    composeTestRule.waitUntil(UI_WAIT_TIMEOUT) { updatedAliceEvent.participants.contains(bobId) }

    // Verify that Bob is registered to the event
    composeTestRule.waitUntil(UI_WAIT_TIMEOUT) {
      val browserEmpty =
          composeTestRule
              .onAllNodesWithTag(EventsScreenTestTags.EMPTY_BROWSER_LIST_MSG)
              .fetchSemanticsNodes()
              .isNotEmpty()
      val upcomingNotEmpty =
          composeTestRule
              .onAllNodesWithTag(EventsScreenTestTags.EMPTY_UPCOMING_LIST_MSG)
              .fetchSemanticsNodes()
              .isEmpty()
      browserEmpty && upcomingNotEmpty
    }

    // Click on the event item and unregister button
    composeTestRule.clickEventItem(eventByAlice)
    composeTestRule.onNodeWithTag(AlertDialogTestTags.ALERT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AlertDialogTestTags.TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AlertDialogTestTags.DATE_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AlertDialogTestTags.CREATOR_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AlertDialogTestTags.BODY).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AlertDialogTestTags.DISMISS_BTN).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(AlertDialogTestTags.CONFIRM_BTN)
        .assertIsDisplayed()
        .performClick()

    // Verify that Bob is removed from the participants list of the event
    val updatedBobRemoved = eventsRepository.getEvent(eventByAlice.id)
    composeTestRule.waitUntil { !updatedBobRemoved.participants.contains(bobId) }

    // Verify that the event is back in the browser events list
    composeTestRule.waitUntil(UI_WAIT_TIMEOUT) {
      val browserNotEmpty =
          composeTestRule
              .onAllNodesWithTag(EventsScreenTestTags.EMPTY_BROWSER_LIST_MSG)
              .fetchSemanticsNodes()
              .isEmpty()
      val upcomingEmpty =
          composeTestRule
              .onAllNodesWithTag(EventsScreenTestTags.EMPTY_UPCOMING_LIST_MSG)
              .fetchSemanticsNodes()
              .isNotEmpty()
      browserNotEmpty && upcomingEmpty
    }

    composeTestRule
        .onNodeWithTag(EventsScreenTestTags.EMPTY_BROWSER_LIST_MSG)
        .assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_UPCOMING_LIST_MSG).assertIsDisplayed()
  }

  /**
   * Test: scenario: Log in as Anonym Bob, create his own event. Verifies that Bob does see his
   * event in "Your Events" list. Click on the Bob Event in "Your Events" list. Verifies that the
   * AlertDialog is displayed with the correct information. Verifies that the "Edit" button is
   * displayed. Verifies that Bob can click on the "Edit" button. Verifies that Bob is redirected to
   * the Edit Event screen.
   */
  @Test
  fun testCanEditAnEvent() = runTest {
    // Sign in as Bob
    val bobId = "bobId"
    // Create event by Bob
    val eventByBob = createYourEvent(bobId)
    eventsRepository.addEvent(eventByBob)

    setContent(bobId)

    // Verify that Bob's event is displayed
    composeTestRule.waitUntil(UI_WAIT_TIMEOUT) {
      runBlocking { eventsRepository.getAllEvents().contains(eventByBob) }
    }

    composeTestRule
        .onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(eventByBob))
        .assertIsDisplayed()
    // Click on Bob's event item and verify the popup
    composeTestRule.clickEventItem(eventByBob)
    composeTestRule.onNodeWithTag(AlertDialogTestTags.ALERT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AlertDialogTestTags.DISMISS_BTN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AlertDialogTestTags.TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AlertDialogTestTags.DATE_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AlertDialogTestTags.CREATOR_TEXT).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(AlertDialogTestTags.BODY).assertIsDisplayed()
    // Click on the Edit button
    composeTestRule
        .onNodeWithTag(AlertDialogTestTags.CONFIRM_BTN)
        .assertIsDisplayed()
        .performClick()
  }

  /**
   * Test: scenario: Log in as Anonym Bob, create his own event. Verifies that Bob does see his
   * event in "Your Events" list. Click on the Bob Event in "Your Events" list. Verifies that the
   * AlertDialog is displayed with the correct information. Verifies that the "Cancel" button is
   * displayed. Verifies that Bob can click on the "Cancel" button. Verifies that the pop up is
   * closed and Bob is back to the overview screen.
   */
  @Test
  fun testCanCloseAlertDialogYourEvent() = runTest {
    // Sign in as Bob
    val bobId = "bobId"
    // Create event by Bob
    val eventByBob = createYourEvent(bobId)
    eventsRepository.addEvent(eventByBob)

    setContent(bobId)

    // Verify that Bob's event is displayed
    composeTestRule.waitUntil(UI_WAIT_TIMEOUT) {
      runBlocking { eventsRepository.getAllEvents().contains(eventByBob) }
    }

    composeTestRule
        .onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(eventByBob))
        .assertIsDisplayed()
    composeTestRule.clickEventItem(eventByBob)
    composeTestRule.onNodeWithTag(AlertDialogTestTags.ALERT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AlertDialogTestTags.TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AlertDialogTestTags.DATE_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AlertDialogTestTags.CREATOR_TEXT).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(AlertDialogTestTags.BODY).assertIsDisplayed()
    // Click on the Cancel button and verify that the popup is closed
    composeTestRule
        .onNodeWithTag(AlertDialogTestTags.DISMISS_BTN)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.onNodeWithTag(AlertDialogTestTags.ALERT).assertIsNotDisplayed()
  }

  /**
   * Test: scenario: Log in as Anonym Bob, want to create an event. Verifies that the "Create Event"
   * button is displayed. Verifies that Bob can click on the "Create Event" button. Verifies that
   * Bob is redirected to the Create Event screen.
   */
  @Test
  fun testCanClickOnCreateEventButton() = runTest {
    // Sign in as Bob
    val bobId = "bobId"
    setContent(bobId)

    composeTestRule.waitForIdle()
    // Click on the Create Event button
    composeTestRule
        .onNodeWithTag(EventsScreenTestTags.CREATE_EVENT_BUTTON)
        .assertIsDisplayed()
        .performClick()
  }

  /**
   * Test: Verifies that when there are events with different statuses (upcoming, ongoing, past),
   * all events are displayed with the correct status indicators.
   */
  @Test
  fun testEventDisplayAllStatusCorrectly() = runTest {
    val currentUserId = "bobId"

    profileRepository.addProfile(Profile(uid = "bobId", name = "Test User", profilePicture = ""))

    val listEvents: List<Event> =
        listOf(
            upcomingEvent,
            upcomingEventCreated,
            upcomingEventParticipate,
            pastEvent,
            pastEventCreated,
            pastEventParticipating,
            ongoingEvent,
            ongoingEventCreated,
            ongoingEventParticipating)

    listEvents.forEach { event -> eventsRepository.addEvent(event) }

    setContent(currentUserId)

    composeTestRule.waitForIdle()

    listEvents.forEach { event ->
      composeTestRule.scrollToEvent(event)
      composeTestRule
          .onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(event))
          .assertIsDisplayed()
    }

    composeTestRule.scrollToEvent(upcomingEvent)
    composeTestRule.onEventItem(
        upcomingEvent, hasTestTag(EventsScreenTestTags.EVENT_STATUS_INDICATOR_UPCOMING))
    composeTestRule.scrollToEvent(upcomingEventParticipate)
    composeTestRule.onEventItem(
        upcomingEventParticipate, hasTestTag(EventsScreenTestTags.EVENT_STATUS_INDICATOR_UPCOMING))
    composeTestRule.scrollToEvent(upcomingEventCreated)
    composeTestRule.onEventItem(
        upcomingEventCreated, hasTestTag(EventsScreenTestTags.EVENT_STATUS_INDICATOR_UPCOMING))
    composeTestRule.scrollToEvent(ongoingEvent)
    composeTestRule.onEventItem(
        ongoingEvent, hasTestTag(EventsScreenTestTags.EVENT_STATUS_INDICATOR_ONGOING))
    composeTestRule.scrollToEvent(ongoingEventParticipating)
    composeTestRule.onEventItem(
        ongoingEventParticipating, hasTestTag(EventsScreenTestTags.EVENT_STATUS_INDICATOR_ONGOING))
    composeTestRule.scrollToEvent(ongoingEventCreated)
    composeTestRule.onEventItem(
        ongoingEventCreated, hasTestTag(EventsScreenTestTags.EVENT_STATUS_INDICATOR_ONGOING))
    composeTestRule.scrollToEvent(pastEvent)
    composeTestRule.onEventItem(
        pastEvent, hasTestTag(EventsScreenTestTags.EVENT_STATUS_INDICATOR_PAST))
    composeTestRule.scrollToEvent(pastEventParticipating)
    composeTestRule.onEventItem(
        pastEventParticipating, hasTestTag(EventsScreenTestTags.EVENT_STATUS_INDICATOR_PAST))
    composeTestRule.scrollToEvent(pastEventCreated)
    composeTestRule.onEventItem(
        pastEventCreated, hasTestTag(EventsScreenTestTags.EVENT_STATUS_INDICATOR_PAST))
  }

  /** Test: Verifies that the filter bar correctly filters events based on their status */
  @Test
  fun testFilterBarWorksCorrectly() = runTest {
    val currentUserId = "bobId"

    profileRepository.addProfile(Profile(uid = "bobId", name = "Test User", profilePicture = ""))

    val listUpcoming: List<Event> =
        listOf(upcomingEvent, upcomingEventCreated, upcomingEventParticipate)
    val listOngoing: List<Event> =
        listOf(ongoingEvent, ongoingEventCreated, ongoingEventParticipating)
    val listPast: List<Event> = listOf(pastEvent, pastEventCreated, pastEventParticipating)

    val listEvents = listUpcoming + listOngoing + listPast

    listEvents.forEach { event -> eventsRepository.addEvent(event) }

    setContent(currentUserId)

    composeTestRule.waitForIdle()

    // Initially, all events should be displayed
    listEvents.forEach { event ->
      composeTestRule.scrollToEvent(event)
      composeTestRule
          .onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(event))
          .assertIsDisplayed()
    }
    // Apply Upcoming filter
    composeTestRule.scrollToEvent(pastEvent) // Ensure the filter bar is visible
    composeTestRule
        .onNodeWithTag(EventsScreenTestTags.FILTER_UPCOMING_BUTTON)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.waitForIdle()
    listUpcoming.forEach { event ->
      composeTestRule.scrollToEvent(event)
      composeTestRule
          .onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(event))
          .assertIsDisplayed()
    }
    // Apply Ongoing filter
    composeTestRule.scrollToEvent(upcomingEvent) // Ensure the filter bar is visible
    composeTestRule
        .onNodeWithTag(EventsScreenTestTags.FILTER_ONGOING_BUTTON)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.waitForIdle()
    listOngoing.forEach { event ->
      composeTestRule
          .onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(event))
          .assertIsDisplayed()
    }
    // Apply Past filter
    composeTestRule.scrollToEvent(ongoingEvent) // Ensure the filter bar is visible
    composeTestRule
        .onNodeWithTag(EventsScreenTestTags.FILTER_PAST_BUTTON)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.waitForIdle()
    listPast.forEach { event ->
      composeTestRule
          .onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(event))
          .assertIsDisplayed()
    }
  }

  // ///////////////////// UTILS

  /** Helper function to use when we want to click on a specific event item */
  private fun ComposeTestRule.clickEventItem(event: Event) {
    waitUntilEventIsDisplayed(event).performClick()
  }

  /**
   * Helper function to use when we want to check if the current screen displaying is the events
   * overview screen
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
   * Helper function to use when we want to check if a specific event item is displayed on the
   * screen
   */
  private fun ComposeTestRule.checkEventItemIsDisplayed(event: Event): SemanticsNodeInteraction =
      onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(event)).assertIsDisplayed()

  /**
   * Helper function to use when we want to check if the current screen displaying is the events
   * overview screen
   */
  private fun ComposeTestRule.checkEventsScreenIsDisplayed() {
    onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
        .assertTextContains("Events", substring = true, ignoreCase = true)
  }

  /**
   * Helper function to use when we want to check if a specific event item is displayed on the
   * screen with a specific matcher
   */
  private fun ComposeTestRule.onEventItem(event: Event, matcher: SemanticsMatcher) {
    waitUntilEventIsDisplayed(event)
    onNode(
            hasTestTag(EventsScreenTestTags.getTestTagForEventItem(event))
                .and(hasAnyDescendant(matcher)),
            useUnmergedTree = true)
        .assertIsDisplayed()
  }

  /** Check that the anonymous user sees only the browse events section */
  @Test
  fun anonUserSeesOnlyBrowseSection() {
    mockitoUtils.chooseCurrentUser("anon", true)

    eventsViewModel =
        EventsViewModel(
            eventsRepository = eventsRepository,
            profileRepository = profileRepository,
            authProvider = { mockitoUtils.mockAuth })
    composeTestRule.setContent {
      EventsScreen(
          eventsViewModel = eventsViewModel,
          actions = EventsScreenActions(),
          coordinator = mapCoordinator)
    }

    composeTestRule.onNodeWithTag(EventsScreenTestTags.BROWSE_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventsScreenTestTags.UPCOMING_TITLE).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(EventsScreenTestTags.YOUR_EVENTS_TITLE).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(EventsScreenTestTags.CREATE_EVENT_BUTTON).assertIsNotDisplayed()
  }

  /**
   * Test: scenario: Log in as Bob, view an event with a location. Click on the event to open
   * details. Click "See on Map". Verifies that the MapCoordinator receives the request to center on
   * that event.
   */
  @Test
  fun testSeeOnMapButtonTriggersCoordinator() = runTest {
    val bobId = "bobId"

    // Create an event with a valid location
    val eventWithLocation =
        createYourEvent(bobId).copy(id = "loc_event", location = Location(46.5, 6.5, "Test Place"))
    eventsRepository.addEvent(eventWithLocation)

    setContent(bobId)

    composeTestRule.waitForIdle()

    // Open the event dialog
    composeTestRule.clickEventItem(eventWithLocation)

    // Verify the "See on map" button (Neutral button) is displayed
    composeTestRule
        .onNodeWithTag(AlertDialogTestTags.NEUTRAL_BTN) //
        .assertIsDisplayed()
        .performClick()

    // Verify the coordinator received the ID
    // We check the internal state of our local coordinator instance
    assert(mapCoordinator.getUnconsumedEventId() == eventWithLocation.id)
  }

  /**
   * Test: Verifies that the "See on Map" button is disabled when the event does not have a valid
   * location.
   */
  @Test
  fun testSeeOnMapButtonDisabledWhenNoLocation() = runTest {
    val bobId = "bobId"

    // Create an event with NO location
    val eventNoLocation = createYourEvent(bobId).copy(id = "no_loc_event", location = null)
    eventsRepository.addEvent(eventNoLocation)

    setContent(bobId)

    composeTestRule.waitForIdle()

    // Open the event dialog
    composeTestRule.clickEventItem(eventNoLocation)

    // Find the button and assert it is displayed but disabled
    composeTestRule
        .onNodeWithTag(AlertDialogTestTags.NEUTRAL_BTN)
        .assertIsDisplayed()
        .assertIsNotEnabled()
  }

  /**
   * Test: Verifies that clicking "See on Map" performs two actions:
   * 1. Sends the event ID to the MapCoordinator.
   * 2. Dismisses (closes) the Alert Dialog.
   */
  @Test
  fun testSeeOnMapDismissesDialog() = runTest {
    val bobId = "bobId"
    val eventWithLocation =
        createYourEvent(bobId)
            .copy(id = "loc_event_dismiss", location = Location(46.5, 6.5, "Test Place"))
    eventsRepository.addEvent(eventWithLocation)

    setContent(bobId)
    composeTestRule.waitForIdle()

    // Open the event dialog
    composeTestRule.clickEventItem(eventWithLocation)

    // Verify dialog is open
    composeTestRule.onNodeWithTag(AlertDialogTestTags.ALERT).assertIsDisplayed()

    // Click "See on map"
    composeTestRule
        .onNodeWithTag(AlertDialogTestTags.NEUTRAL_BTN) //
        .performClick()

    // Assert that coordinator received the ID
    assert(mapCoordinator.getUnconsumedEventId() == eventWithLocation.id)

    // Assert that dialog is closed
    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag(AlertDialogTestTags.ALERT) //
        .assertIsNotDisplayed()
  }

  @Test
  fun testNumbersAttendeesVisualOnOverviewScreen() = runTest {
    val currentUserId = "bobId"
    val participant1 = Profile(uid = "p1", name = "participant1", profilePicture = "")
    val participant2 = Profile(uid = "p2", name = "participant2", profilePicture = "")
    val participant3 = Profile(uid = "p3", name = "participant3", profilePicture = "")

    profileRepository.addProfile(Profile(uid = "bobId", name = "Test User", profilePicture = ""))
    profileRepository.addProfile(participant1)
    profileRepository.addProfile(participant2)
    profileRepository.addProfile(participant3)

    val event2p = upcomingEvent.copy(participants = listOf("p1", "p2"))
    val event3p = upcomingEventCreated.copy(participants = listOf("p1", "p2", "p3"))

    val listUpcoming: List<Event> = listOf(event2p, event3p, upcomingEventParticipate)

    listUpcoming.forEach { event -> eventsRepository.addEvent(event) }

    setContent(currentUserId)

    composeTestRule.waitForIdle()

    // Initially, all events should be displayed
    listUpcoming.forEach { event ->
      composeTestRule.scrollToEvent(event)
      composeTestRule
          .onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(event))
          .assertIsDisplayed()
    }

    // We can see the number of attendees in overview point of view
    listUpcoming.forEach { event ->
      composeTestRule.scrollToEvent(event)
      composeTestRule
          .onNodeWithTag(
              EventsScreenTestTags.getTestTagForEventNumberAttendees(event), useUnmergedTree = true)
          .assertIsDisplayed()
    }

    composeTestRule
        .onNodeWithTag(EventsScreenTestTags.ATTENDEES_ALERT_DIALOG)
        .assertIsNotDisplayed()

    // Open Alert Dialog
    composeTestRule.scrollToEvent(event2p)
    composeTestRule
        .onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(event2p))
        .assertIsDisplayed()
        .performClick()

    // Open alert dialog attendees
    composeTestRule
        .onNodeWithTag(AlertDialogTestTags.ATTENDEES_BTN)
        .assertIsDisplayed()
        .performClick()

    composeTestRule.onNodeWithTag(EventsScreenTestTags.ATTENDEES_ALERT_DIALOG).assertIsDisplayed()

    // Go back
    composeTestRule
        .onNodeWithTag(EventsScreenTestTags.ATTENDEES_ALERT_DIALOG_CANCEL)
        .assertIsDisplayed()
        .performClick()

    composeTestRule
        .onNodeWithTag(EventsScreenTestTags.ATTENDEES_ALERT_DIALOG)
        .assertIsNotDisplayed()
  }

  /** Helper function to scroll to a specific event item in a list */
  private fun ComposeTestRule.scrollToEvent(event: Event) {
    onNodeWithTag(EventsScreenTestTags.ALL_LISTS)
        .performScrollToNode(hasTestTag(EventsScreenTestTags.getTestTagForEventItem(event)))
  }

  // --- TEST SEARCH / FILTER EVENTS ---
  private val eventA =
      Event(
          id = "eventA",
          title = "Alpha Event",
          description = "Event starting with A",
          creatorName = "User1",
          location = Location(46.520278, 6.565556, "EPFL"),
          date = tomorrowTimestamp,
          startTime = Timestamp(start),
          endTime = Timestamp(finish),
          creatorId = "user1",
          participants = listOf(),
          status = EventStatus.UPCOMING)

  private val eventB =
      Event(
          id = "eventB",
          title = "Beta Event",
          description = "Event starting with B",
          creatorName = "User2",
          location = Location(46.520278, 6.565556, "EPFL"),
          date = Timestamp(Date.from(Instant.now().plus(2, ChronoUnit.DAYS))), // Later date
          startTime = Timestamp(start),
          endTime = Timestamp(finish),
          creatorId = "user2",
          participants = listOf(),
          status = EventStatus.UPCOMING)

  private val eventC =
      Event(
          id = "eventC",
          title = "Gamma Event",
          description = "Event starting with C",
          creatorName = "User3",
          location = Location(46.520278, 6.565556, "EPFL"),
          date = Timestamp(Date.from(Instant.now().plus(3, ChronoUnit.DAYS))), // Latest date
          startTime = Timestamp(start),
          endTime = Timestamp(finish),
          creatorId = "user3",
          participants = listOf(),
          status = EventStatus.UPCOMING)

  private val eventZebra =
      Event(
          id = "zebra",
          title = "Zebra Event",
          description = "Animal themed event",
          creatorName = "User4",
          location = Location(46.520278, 6.565556, "EPFL"),
          date = tomorrowTimestamp,
          startTime = Timestamp(start),
          endTime = Timestamp(finish),
          creatorId = "user4",
          participants = listOf(),
          status = EventStatus.UPCOMING)

  private val eventAlphaBeta =
      Event(
          id = "alphabeta",
          title = "Alpha Beta Event",
          description = "Combined name event",
          creatorName = "User5",
          location = Location(46.520278, 6.565556, "EPFL"),
          date = tomorrowTimestamp,
          startTime = Timestamp(start),
          endTime = Timestamp(finish),
          creatorId = "user5",
          participants = listOf(),
          status = EventStatus.UPCOMING)

  /** Test: Search bar filters events correctly by title */
  @Test
  fun searchBarFiltersEventsByTitle() = runTest {
    val events = listOf(eventA, eventB, eventC, eventZebra, eventAlphaBeta)
    events.forEach { eventsRepository.addEvent(it) }
    profileRepository.addProfile(
        Profile(uid = currentUserId, name = "Test User", profilePicture = ""))

    setContent(currentUserId)
    composeTestRule.waitForIdle()

    events.forEach { event ->
      composeTestRule
          .onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(event))
          .assertIsDisplayed()
    }

    composeTestRule.onNodeWithTag(EventsScreenTestTags.SEARCH_BAR).performTextClearance()
    composeTestRule.onNodeWithTag(EventsScreenTestTags.SEARCH_BAR).performTextInput("Alpha")

    composeTestRule.waitUntil(timeoutMillis = 10_000L) {
      val aVisible =
          composeTestRule
              .onAllNodesWithTag(EventsScreenTestTags.getTestTagForEventItem(eventA))
              .fetchSemanticsNodes()
              .isNotEmpty()
      val alphaVisible =
          composeTestRule
              .onAllNodesWithTag(EventsScreenTestTags.getTestTagForEventItem(eventAlphaBeta))
              .fetchSemanticsNodes()
              .isNotEmpty()
      val bAbsent =
          composeTestRule
              .onAllNodesWithTag(EventsScreenTestTags.getTestTagForEventItem(eventB))
              .fetchSemanticsNodes()
              .isEmpty()
      val cAbsent =
          composeTestRule
              .onAllNodesWithTag(EventsScreenTestTags.getTestTagForEventItem(eventC))
              .fetchSemanticsNodes()
              .isEmpty()
      val zebraAbsent =
          composeTestRule
              .onAllNodesWithTag(EventsScreenTestTags.getTestTagForEventItem(eventZebra))
              .fetchSemanticsNodes()
              .isEmpty()

      aVisible && alphaVisible && bAbsent && cAbsent && zebraAbsent
    }

    // Asserts finales
    composeTestRule
        .onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(eventA))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(eventAlphaBeta))
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(eventB))
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(eventC))
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(eventZebra))
        .assertIsNotDisplayed()
  }

  /** Test: Empty search shows all events */
  @Test
  fun emptySearchShowsAllEvents() = runTest {
    val events = listOf(eventA, eventB, eventC)
    events.forEach { eventsRepository.addEvent(it) }
    profileRepository.addProfile(
        Profile(uid = currentUserId, name = "Test User", profilePicture = ""))

    setContent(currentUserId)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(EventsScreenTestTags.SEARCH_BAR).performTextClearance()
    composeTestRule.onNodeWithTag(EventsScreenTestTags.SEARCH_BAR).performTextInput("")

    composeTestRule.waitForIdle()

    events.forEach { event ->
      composeTestRule
          .onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(event))
          .assertIsDisplayed()
    }
  }

  /** Test: Sorting by alphabetical order works correctly */
  @Test
  fun sortByAlphabeticalOrderWorks() = runTest {
    val events = listOf(eventZebra, eventC, eventB, eventA, eventAlphaBeta)
    events.forEach { eventsRepository.addEvent(it) }
    profileRepository.addProfile(
        Profile(uid = currentUserId, name = "Test User", profilePicture = ""))

    setContent(currentUserId)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(EventsScreenTestTags.SORT_MENU_BUTTON).performClick()

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventsScreenTestTags.SORT_ALPHABETIC_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventsScreenTestTags.SORT_PROX_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventsScreenTestTags.SORT_DATE_BUTTON).performClick()

    composeTestRule.waitForIdle()
  }
}
