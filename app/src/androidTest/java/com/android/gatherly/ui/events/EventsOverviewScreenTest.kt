package com.android.gatherly.ui.events

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
import com.android.gatherly.model.event.Event
import com.android.gatherly.model.event.EventStatus
import com.android.gatherly.model.map.Location
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.utils.FirebaseEmulator
import com.android.gatherly.utils.FirestoreEventsGatherlyTest
import com.android.gatherly.utils.GatherlyTest.Companion.fromDate
import com.android.gatherly.utils.UI_WAIT_TIMEOUT
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.NoSuchElementException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EventsOverviewScreenTest : FirestoreEventsGatherlyTest() {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var currentUserId: String

  @Before
  override fun setUp() {
    super.setUp()
    currentUserId =
        Firebase.auth.currentUser?.uid
            ?: throw IllegalStateException("Firebase user is not authenticated after setUp.")
  }

  val dateB = Timestamp.Companion.fromDate(2025, Calendar.OCTOBER, 25)
  val start = SimpleDateFormat("HH:mm").parse("12:00") ?: throw NoSuchElementException("no date ")
  val finish = SimpleDateFormat("HH:mm").parse("23:00") ?: throw NoSuchElementException("no date ")

  /** Helper function: set the content of the composeTestRule without initial events */
  fun setContent() {
    runTest {
      composeTestRule.setContent {
        EventsScreen(
            eventsViewModel =
                EventsViewModel(repository = repository, currentUserId = currentUserId))
      }
    }
  }

  /** Helper function to create an event for the current user */
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
   * Test: Verifies that when there is no event registered, all relevant UI components are displayed
   * correctly.
   */
  @Test
  fun testTagsCorrectlySetWhenListsAreEmpty() {
    setContent()
    composeTestRule
        .onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
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

  /**
   * Test : Log in as Anonym Bob, create her own event. Verifies that Bob does see her event in
   * "Your Events" list. Verifies if the title and date of the event are correctly displayed.
   */
  @Test
  fun testYourEventDisplayCorrectly() = runTest {
    val auth = FirebaseEmulator.auth

    try {

      // Sign in as Bob
      auth.signInAnonymously().await()
      val bobId = auth.currentUser?.uid ?: error("Bob auth failed")
      val eventByBob = createYourEvent(bobId)
      repository.addEvent(eventByBob)

      composeTestRule.setContent {
        EventsScreen(
            eventsViewModel = EventsViewModel(repository = repository, currentUserId = bobId),
        )
      }

      composeTestRule.waitForIdle()

      composeTestRule.onNodeWithTag(EventsScreenTestTags.BROWSE_TITLE).assertIsDisplayed()
      composeTestRule.onNodeWithTag(EventsScreenTestTags.EMPTY_BROWSER_LIST_MSG).assertIsDisplayed()
      composeTestRule.onNodeWithTag(EventsScreenTestTags.UPCOMING_TITLE).assertIsDisplayed()
      composeTestRule
          .onNodeWithTag(EventsScreenTestTags.EMPTY_UPCOMING_LIST_MSG)
          .assertIsDisplayed()
      composeTestRule.onNodeWithTag(EventsScreenTestTags.YOUR_EVENTS_TITLE).assertIsDisplayed()
      composeTestRule
          .onNodeWithTag(EventsScreenTestTags.EMPTY_OUREVENTS_LIST_MSG)
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
   * Test: scenario: Log in as Anonym Alice, create her own event, log out. Log in as Anonym Bob,
   * and get his screen. Verifies that Bob does see Alice's event in browser events list. Verifies
   * if the title and date of the event are correctly displayed.
   */
  @Test
  fun testBrowserEventDisplayCorrectly() = runTest {
    val auth = FirebaseEmulator.auth

    try {
      // Sign in as Alice
      auth.signInAnonymously().await()
      val aliceId = auth.currentUser?.uid ?: error("Alice auth failed")
      // Create event by Alice
      val eventByAlice = createYourEvent(aliceId)
      repository.addEvent(eventByAlice)
      // Sign out Alice
      auth.signOut()

      // Sign in as Bob
      auth.signInAnonymously().await()
      val bobId = auth.currentUser?.uid ?: error("Bob auth failed")

      composeTestRule.setContent {
        EventsScreen(
            eventsViewModel = EventsViewModel(repository = repository, currentUserId = bobId))
      }

      composeTestRule.waitForIdle()

      composeTestRule
          .onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(eventByAlice))
          .assertIsDisplayed()
      // Check that the event created by Alice show up in the Bob's browser list
      composeTestRule
          .onNodeWithTag(EventsScreenTestTags.EMPTY_BROWSER_LIST_MSG)
          .assertIsNotDisplayed()

      composeTestRule.onNodeWithTag(EventsScreenTestTags.UPCOMING_TITLE).assertIsDisplayed()
      composeTestRule
          .onNodeWithTag(EventsScreenTestTags.EMPTY_UPCOMING_LIST_MSG)
          .assertIsDisplayed()
      composeTestRule.onNodeWithTag(EventsScreenTestTags.YOUR_EVENTS_TITLE).assertIsDisplayed()
      composeTestRule
          .onNodeWithTag(EventsScreenTestTags.EMPTY_OUREVENTS_LIST_MSG)
          .assertIsDisplayed()

      // Check that the event details (title and date) are correctly displayed
      composeTestRule.onEventItem(eventByAlice, hasTestTag(EventsScreenTestTags.EVENT_TITLE))
      composeTestRule.onEventItem(eventByAlice, hasTestTag(EventsScreenTestTags.EVENT_DATE))
    } finally {
      // Sign Out
      auth.signOut()
    }
  }

  /**
   * Test: scenario: Log in as Anonym Alice, create her own event, log out. Log in as Anonym Bob,
   * and get his screen. Verifies that Bob does see Alice's event in browser events list. Click on
   * the Alice Event in browser events list. Verifies that the AlertDialog is displayed Click on
   * Participate button. Verifies that Bob is added to the participants list of the event.
   */
  @Test
  fun testUpcomingEventDisplayCorrectly() = runTest {
    val auth = FirebaseEmulator.auth

    try {
      // Sign in as Alice
      auth.signInAnonymously().await()
      val aliceId = auth.currentUser?.uid ?: error("Alice auth failed")
      // Create event by Alice
      val eventByAlice = createYourEvent(aliceId)
      repository.addEvent(eventByAlice)
      // Sign out Alice
      auth.signOut()

      // Sign in as Bob
      auth.signInAnonymously().await()
      val bobId = auth.currentUser?.uid ?: error("Bob auth failed")

      composeTestRule.setContent {
        EventsScreen(
            eventsViewModel = EventsViewModel(repository = repository, currentUserId = bobId))
      }

      composeTestRule.waitForIdle()

      // Check that the event created by Alice show up in the Bob's browser list
      composeTestRule
          .onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(eventByAlice))
          .assertIsDisplayed()
      composeTestRule
          .onNodeWithTag(EventsScreenTestTags.EMPTY_BROWSER_LIST_MSG)
          .assertIsNotDisplayed()
      composeTestRule
          .onNodeWithTag(EventsScreenTestTags.EMPTY_UPCOMING_LIST_MSG)
          .assertIsDisplayed()

      // Click on the event item
      composeTestRule.clickEventItem(eventByAlice)
      // Check that the popup is displayed
      composeTestRule.onNodeWithTag(EventsScreenTestTags.EVENT_POPUP).assertIsDisplayed()
      composeTestRule.onNodeWithTag(EventsScreenTestTags.GOBACK_EVENT_BUTTON).assertIsDisplayed()

      // Click on Participate button
      composeTestRule
          .onNodeWithTag(EventsScreenTestTags.PARTICIPATE_BUTTON)
          .assertIsDisplayed()
          .performClick()

      // Wait until the popup is closed
      composeTestRule.waitUntil(UI_WAIT_TIMEOUT) {
        composeTestRule
            .onAllNodesWithTag(EventsScreenTestTags.EVENT_POPUP)
            .fetchSemanticsNodes()
            .isEmpty()
      }
      composeTestRule.waitForIdle()

      // Verify that Bob is added to the participants list of the event
      kotlinx.coroutines.runBlocking {
        val deadline = System.currentTimeMillis() + UI_WAIT_TIMEOUT
        while (System.currentTimeMillis() < deadline) {
          try {
            val updated = repository.getEvent(eventByAlice.id)
            if (updated.participants.contains(bobId)) break
          } catch (_: Exception) {}
          kotlinx.coroutines.delay(200)
        }
      }

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
    } finally {
      // Sign Out
      auth.signOut()
    }
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
    val auth = FirebaseEmulator.auth

    try {
      // Sign in as Alice
      auth.signInAnonymously().await()
      val aliceId = auth.currentUser?.uid ?: error("Alice auth failed")
      // Create event by Alice
      val eventByAlice = createYourEvent(aliceId)
      repository.addEvent(eventByAlice)
      // Sign out Alice
      auth.signOut()

      // Sign in as Bob
      auth.signInAnonymously().await()
      val bobId = auth.currentUser?.uid ?: error("Bob auth failed")

      composeTestRule.setContent {
        EventsScreen(
            eventsViewModel = EventsViewModel(repository = repository, currentUserId = bobId))
      }

      composeTestRule.waitForIdle()

      composeTestRule
          .onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(eventByAlice))
          .assertIsDisplayed()

      composeTestRule
          .onNodeWithTag(EventsScreenTestTags.EMPTY_BROWSER_LIST_MSG)
          .assertIsNotDisplayed()
      composeTestRule
          .onNodeWithTag(EventsScreenTestTags.EMPTY_UPCOMING_LIST_MSG)
          .assertIsDisplayed()

      // Click on the event item and participate button
      composeTestRule.clickEventItem(eventByAlice)
      composeTestRule.onNodeWithTag(EventsScreenTestTags.EVENT_POPUP).assertIsDisplayed()
      composeTestRule
          .onNodeWithTag(EventsScreenTestTags.PARTICIPATE_BUTTON)
          .assertIsDisplayed()
          .performClick()

      kotlinx.coroutines.runBlocking {
        val deadline = System.currentTimeMillis() + UI_WAIT_TIMEOUT
        while (System.currentTimeMillis() < deadline) {
          val updated = repository.getEvent(eventByAlice.id)
          if (updated.participants.contains(bobId)) break
          kotlinx.coroutines.delay(200)
        }
      }

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
      composeTestRule.onNodeWithTag(EventsScreenTestTags.EVENT_POPUP).assertIsDisplayed()
      composeTestRule.onNodeWithTag(EventsScreenTestTags.POPUP_TITLE).assertIsDisplayed()
      composeTestRule.onNodeWithTag(EventsScreenTestTags.POPUP_DESCRIPTION).assertIsDisplayed()
      composeTestRule.onNodeWithTag(EventsScreenTestTags.GOBACK_EVENT_BUTTON).assertIsDisplayed()
      composeTestRule
          .onNodeWithTag(EventsScreenTestTags.UNREGISTER_BUTTON)
          .assertIsDisplayed()
          .performClick()

      // Verify that Bob is removed from the participants list of the event
      kotlinx.coroutines.runBlocking {
        val deadline = System.currentTimeMillis() + UI_WAIT_TIMEOUT
        while (System.currentTimeMillis() < deadline) {
          val updated = repository.getEvent(eventByAlice.id)
          if (!updated.participants.contains(bobId)) break
          kotlinx.coroutines.delay(200)
        }
      }

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
      composeTestRule
          .onNodeWithTag(EventsScreenTestTags.EMPTY_UPCOMING_LIST_MSG)
          .assertIsDisplayed()
    } finally {
      auth.signOut()
    }
  }

  /**
   * Test: scenario: Log in as Anonym Bob, create his own event. Verifies that Bob does see his
   * event in "Your Events" list. Click on the Bob Event in "Your Events" list. Verifies that the
   * AlertDialog is displayed with the correct information. Verifies that the "Edit" button is
   * displayed. Verifies that Bob can click on the "Edit" button. Verifies that Bob is redirected to
   * the Edit Event screen.
   */
  @Test
  fun testCanEditAnEvent() {
    runTest {
      val auth = FirebaseEmulator.auth
      try {
        // Sign in as Bob
        auth.signInAnonymously().await()
        val bobId = auth.currentUser?.uid ?: error("Bob auth failed")
        // Create event by Bob
        val eventByBob = createYourEvent(bobId)
        repository.addEvent(eventByBob)

        composeTestRule.setContent {
          EventsScreen(
              eventsViewModel = EventsViewModel(repository = repository, currentUserId = bobId))
        }

        // Verify that Bob's event is displayed
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(eventByBob))
            .assertIsDisplayed()
        // Click on Bob's event item and verify the popup
        composeTestRule.clickEventItem(eventByBob)
        composeTestRule.onNodeWithTag(EventsScreenTestTags.EVENT_POPUP).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.GOBACK_EVENT_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.POPUP_TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.POPUP_DESCRIPTION).assertIsDisplayed()
        // Click on the Edit button
        composeTestRule
            .onNodeWithTag(EventsScreenTestTags.EDIT_EVENT_BUTTON)
            .assertIsDisplayed()
            .performClick()
      } finally {
        // Sign Out
        auth.signOut()
      }
    }
  }

  /**
   * Test: scenario: Log in as Anonym Bob, create his own event. Verifies that Bob does see his
   * event in "Your Events" list. Click on the Bob Event in "Your Events" list. Verifies that the
   * AlertDialog is displayed with the correct information. Verifies that the "Cancel" button is
   * displayed. Verifies that Bob can click on the "Cancel" button. Verifies that the pop up is
   * closed and Bob is back to the overview screen.
   */
  @Test
  fun testCanCloseAlertDialogYourEvent() {
    runTest {
      val auth = FirebaseEmulator.auth

      try {
        // Sign in as Bob
        auth.signInAnonymously().await()
        val bobId = auth.currentUser?.uid ?: error("Bob auth failed")
        // Create event by Bob
        val eventByBob = createYourEvent(bobId)
        repository.addEvent(eventByBob)

        composeTestRule.setContent {
          EventsScreen(
              eventsViewModel = EventsViewModel(repository = repository, currentUserId = bobId))
        }

        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithTag(EventsScreenTestTags.getTestTagForEventItem(eventByBob))
            .assertIsDisplayed()
        composeTestRule.clickEventItem(eventByBob)
        composeTestRule.onNodeWithTag(EventsScreenTestTags.EVENT_POPUP).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.POPUP_TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.POPUP_DESCRIPTION).assertIsDisplayed()
        // Click on the Cancel button and verify that the popup is closed
        composeTestRule
            .onNodeWithTag(EventsScreenTestTags.GOBACK_EVENT_BUTTON)
            .assertIsDisplayed()
            .performClick()
        composeTestRule.onNodeWithTag(EventsScreenTestTags.EVENT_POPUP).assertIsNotDisplayed()
      } finally {
        // Sign Out
        auth.signOut()
      }
    }
  }

  /**
   * Test: scenario: Log in as Anonym Bob, want to create an event. Verifies that the "Create Event"
   * button is displayed. Verifies that Bob can click on the "Create Event" button. Verifies that
   * Bob is redirected to the Create Event screen.
   */
  @Test
  fun testCanClickOnCreateEventButton() {

    runTest {
      val auth = FirebaseEmulator.auth

      try {
        // Sign in as Bob
        auth.signInAnonymously().await()
        val bobId = auth.currentUser?.uid ?: error("Bob auth failed")

        composeTestRule.setContent {
          EventsScreen(
              eventsViewModel = EventsViewModel(repository = repository, currentUserId = bobId))
        }

        composeTestRule.waitForIdle()
        // Click on the Create Event button
        composeTestRule
            .onNodeWithTag(EventsScreenTestTags.CREATE_EVENT_BUTTON)
            .assertIsDisplayed()
            .performClick()
      } finally {
        // Sign Out
        auth.signOut()
      }
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
    val eventNode = this.waitUntilEventIsDisplayed(event)
    onNode(
            hasTestTag(EventsScreenTestTags.getTestTagForEventItem(event))
                .and(hasAnyDescendant(matcher)),
            useUnmergedTree = true)
        .assertIsDisplayed()
  }
}
