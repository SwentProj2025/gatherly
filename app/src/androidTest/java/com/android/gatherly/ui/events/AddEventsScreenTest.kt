package com.android.gatherly.ui.events

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.gatherly.model.event.Event
import com.android.gatherly.model.event.EventState
import com.android.gatherly.model.event.EventStatus
import com.android.gatherly.model.event.EventsLocalRepository
import com.android.gatherly.model.event.EventsRepository
import com.android.gatherly.model.group.GroupsLocalRepository
import com.android.gatherly.model.group.GroupsRepository
import com.android.gatherly.model.map.FakeNominatimLocationRepository
import com.android.gatherly.model.map.Location
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.utils.MockitoUtils
import com.android.gatherly.utils.TestDates.currentDay
import com.android.gatherly.utils.TestDates.currentMonth
import com.android.gatherly.utils.TestDates.futureDate
import com.android.gatherly.utils.TestDates.futureYear
import com.android.gatherly.utils.TestDates.pastDate
import com.android.gatherly.utils.TestDates.pastYear
import com.android.gatherly.utils.openDatePicker
import com.android.gatherly.utils.selectDateFromPicker
import com.google.firebase.Timestamp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Tests for the AddEventScreen */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class AddEventsScreenTest {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  // declare viewModel and repositories
  private lateinit var addEventsViewModel: AddEventViewModel
  private lateinit var eventsRepository: EventsRepository
  private lateinit var profileRepository: ProfileRepository
  private lateinit var groupsRepository: GroupsRepository
  private lateinit var mockitoUtils: MockitoUtils

  private lateinit var fakeNominatimClient: FakeNominatimLocationRepository

  @Before
  fun setUp() {
    profileRepository = ProfileLocalRepository()
    eventsRepository = EventsLocalRepository()
    groupsRepository = GroupsLocalRepository()
    fakeNominatimClient = FakeNominatimLocationRepository()

    fill_repositories()

    mockitoUtils = MockitoUtils()
    mockitoUtils.chooseCurrentUser("0")

    addEventsViewModel =
        AddEventViewModel(
            profileRepository = profileRepository,
            eventsRepository = eventsRepository,
            nominatimClient = fakeNominatimClient,
            authProvider = { mockitoUtils.mockAuth },
            groupsRepository = groupsRepository)

    composeTestRule.setContent { AddEventScreen(addEventsViewModel) }
  }

  /*----------------------------------------Profiles--------------------------------------------*/
  val profile1: Profile =
      Profile(
          uid = "1",
          name = "Profile1",
          focusSessionIds = emptyList(),
          participatingEventIds = emptyList(),
          groupIds = emptyList(),
          friendUids = emptyList())

  val profile2: Profile =
      Profile(
          uid = "2",
          name = "Profile2",
          focusSessionIds = emptyList(),
          participatingEventIds = emptyList(),
          groupIds = emptyList(),
          friendUids = emptyList())

  val profile3: Profile =
      Profile(
          uid = "3",
          name = "Profile3",
          focusSessionIds = emptyList(),
          participatingEventIds = emptyList(),
          groupIds = emptyList(),
          friendUids = emptyList())

  val participantProfile: Profile =
      Profile(
          uid = "4",
          name = "Participant",
          focusSessionIds = emptyList(),
          participatingEventIds = emptyList(),
          groupIds = emptyList(),
          friendUids = emptyList())

  val friendProfile: Profile =
      Profile(
          uid = "f1",
          name = "friend",
          focusSessionIds = emptyList(),
          participatingEventIds = emptyList(),
          groupIds = emptyList(),
          friendUids = listOf("0"))

  var ownerProfile: Profile =
      Profile(
          uid = "0",
          name = "Owner",
          focusSessionIds = emptyList(),
          participatingEventIds = emptyList(),
          groupIds = emptyList(),
          friendUids = listOf("f1"))

  /*----------------------------------------Event-----------------------------------------------*/

  val event: Event =
      Event(
          id = "0",
          title = "Event title",
          description = "Describing this great event",
          creatorName = "my name :)",
          location = null,
          date = Timestamp.now(),
          startTime = Timestamp.now(),
          endTime = Timestamp.now(),
          creatorId = ownerProfile.uid,
          participants = listOf(ownerProfile.uid, participantProfile.uid),
          status = EventStatus.UPCOMING,
          state = EventState.PUBLIC)

  /** Check that all components are displayed */
  @Test
  fun displayAllComponents() {
    composeTestRule.onNodeWithTag(AddEventScreenTestTags.INPUT_NAME).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventScreenTestTags.INPUT_DESCRIPTION).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventScreenTestTags.INPUT_PARTICIPANT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventScreenTestTags.INPUT_LOCATION).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventScreenTestTags.INPUT_DATE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventScreenTestTags.INPUT_START).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventScreenTestTags.INPUT_END).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(AddEventScreenTestTags.ERROR_MESSAGE, useUnmergedTree = true)
        .assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(AddEventScreenTestTags.BTN_SAVE)
  }

  /** Check that menus are displayed */
  @Test
  fun displayMenus() {
    fakeNominatimClient.setSearchResults(
        "Paris",
        listOf(
            Location(latitude = 48.8566, longitude = 2.3522, name = "Paris, France"),
            Location(
                latitude = 48.8534, longitude = 2.3488, name = "Paris, Île-de-France, France")))

    // Then we can perform the test
    composeTestRule.onNodeWithTag(AddEventScreenTestTags.INPUT_LOCATION).performTextInput("Paris")
    composeTestRule.waitUntil(timeoutMillis = 5000L) {
      composeTestRule.onNodeWithTag(AddEventScreenTestTags.LOCATION_MENU).isDisplayed()
    }

    composeTestRule
        .onNodeWithTag(AddEventScreenTestTags.INPUT_PARTICIPANT)
        .performTextInput("Participant")
    composeTestRule.waitUntil(timeoutMillis = 5000L) {
      composeTestRule.onNodeWithTag(AddEventScreenTestTags.PARTICIPANT_MENU).isDisplayed()
    }
  }

  /** Check that the date inputs are working correctly */
  @Test
  fun testDatePickerWorkingCorrectly() {
    composeTestRule.openDatePicker(AddEventScreenTestTags.INPUT_DATE)
    composeTestRule.selectDateFromPicker(currentDay, currentMonth, futureYear)

    // Find nodes with the date text, filter to one within our tagged container
    composeTestRule
        .onAllNodes(hasText(futureDate, substring = true, ignoreCase = true))
        .filterToOne(hasAnyAncestor(hasTestTag(AddEventScreenTestTags.INPUT_DATE)))
        .assertExists()

    composeTestRule.onNodeWithTag(AddEventScreenTestTags.ERROR_MESSAGE).assertIsNotDisplayed()
  }

  /** Check that inputting a past date shows an error message */
  @Test
  fun testPastDateShowError() {
    composeTestRule.openDatePicker(AddEventScreenTestTags.INPUT_DATE)
    composeTestRule.selectDateFromPicker(currentDay, currentMonth, pastYear)

    // Verify the past date was set
    composeTestRule
        .onAllNodes(hasText(pastDate, substring = true, ignoreCase = true))
        .filterToOne(hasAnyAncestor(hasTestTag(AddEventScreenTestTags.INPUT_DATE)))
        .assertExists()

    // Check that the date field shows an error state
    composeTestRule
        .onNode(
            hasAnyAncestor(hasTestTag(AddEventScreenTestTags.INPUT_DATE)) and
                hasText("Invalid format or past date", substring = true, ignoreCase = true))
        .assertExists()
  }

  /** Test: verifies that switch private/public works correctly */
  @Test
  fun testPrivateButtonsAreDisplayedWhenSwitchOff() {
    composeTestRule
        .onNodeWithTag(AddEventScreenTestTags.SWITCH_PUBLIC_PRIVATE_EVENT)
        .assertIsDisplayed()
        .performClick()

    composeTestRule
        .onNodeWithTag(AddEventScreenTestTags.BUTTON_PRIVATE_FRIENDS_EVENT)
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(AddEventScreenTestTags.BUTTON_PRIVATE_GROUP_EVENT)
        .assertIsDisplayed()

    composeTestRule.onNodeWithTag(AddEventScreenTestTags.SWITCH_PUBLIC_PRIVATE_EVENT).performClick()

    composeTestRule
        .onNodeWithTag(AddEventScreenTestTags.BUTTON_PRIVATE_FRIENDS_EVENT)
        .assertIsNotDisplayed()

    composeTestRule
        .onNodeWithTag(AddEventScreenTestTags.BUTTON_PRIVATE_GROUP_EVENT)
        .assertIsNotDisplayed()
  }

  /** Test: verifies that friends only and group buttons work correctly */
  @Test
  fun testPrivateButtonsAreDisplayed() {
    composeTestRule
        .onNodeWithTag(AddEventScreenTestTags.SWITCH_PUBLIC_PRIVATE_EVENT)
        .assertIsDisplayed()
        .performClick()

    composeTestRule
        .onNodeWithTag(AddEventScreenTestTags.BUTTON_PRIVATE_FRIENDS_EVENT)
        .assertIsDisplayed()

    composeTestRule.onNodeWithTag(AddEventScreenTestTags.INPUT_PARTICIPANT).assertIsDisplayed()

    composeTestRule.onNodeWithTag(AddEventScreenTestTags.INPUT_GROUP).assertIsNotDisplayed()

    composeTestRule
        .onNodeWithTag(AddEventScreenTestTags.BUTTON_PRIVATE_GROUP_EVENT)
        .assertIsDisplayed()
        .performClick()

    composeTestRule.onNodeWithTag(AddEventScreenTestTags.INPUT_PARTICIPANT).assertIsNotDisplayed()

    composeTestRule.onNodeWithTag(AddEventScreenTestTags.INPUT_GROUP).assertIsDisplayed()
  }

  /** Test: Verifies that group suggestion working correctly */
  /* TODO : NEED TO FIX THE GROUP IMPLEMENTATION
  @Test
  fun testGroupSuggestionsAppear() = runTest {
    groupsRepository.addGroup(
        Group(
            gid = "G1",
            name = "MyGroup",
            memberIds = listOf(ownerProfile.uid),
            creatorId = ownerProfile.uid,
            description = "",
            adminIds = listOf(ownerProfile.uid)))

    composeTestRule.onNodeWithTag(AddEventScreenTestTags.SWITCH_PUBLIC_PRIVATE_EVENT).performClick()
    composeTestRule.onNodeWithTag(AddEventScreenTestTags.BUTTON_PRIVATE_GROUP_EVENT).performClick()

    composeTestRule.onNodeWithTag(AddEventScreenTestTags.INPUT_GROUP).performTextInput("My")

    composeTestRule.waitUntil(timeoutMillis = 5000L) {
      composeTestRule.onNodeWithTag(AddEventScreenTestTags.GROUP_MENU).isDisplayed()
    }
      composeTestRule.onNodeWithTag(AddEventScreenTestTags
          .getTestTagGroupSuggestionItem("G1")).assertIsDisplayed()

      composeTestRule.onNodeWithTag(AddEventScreenTestTags
          .getTestTagGroupSuggestionAdd("G1")).assertIsDisplayed().performClick()

      composeTestRule.onNodeWithTag(AddEventScreenTestTags
          .getTestTagGroupSuggestionAdd("G1")).assertIsNotDisplayed()

      composeTestRule.onNodeWithTag(AddEventScreenTestTags
          .getTestTagProfileRemoveItem("G1")).assertIsDisplayed()
    }
   */

  /** Test: Verify that the suggestion works for private friends event */
  @Test
  fun testFriendsSuggestionsAppear() {
    val nameParticipant = friendProfile.name
    val uidParticipant = friendProfile.uid

    composeTestRule.onNodeWithTag(AddEventScreenTestTags.SWITCH_PUBLIC_PRIVATE_EVENT).performClick()
    composeTestRule
        .onNodeWithTag(AddEventScreenTestTags.BUTTON_PRIVATE_FRIENDS_EVENT)
        .performClick()

    composeTestRule
        .onNodeWithTag(AddEventScreenTestTags.INPUT_PARTICIPANT)
        .performTextInput(nameParticipant)

    composeTestRule.waitUntil(timeoutMillis = 10000L) {
      composeTestRule
          .onAllNodes(hasTestTag(AddEventScreenTestTags.PARTICIPANT_MENU))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule
        .onNodeWithTag(
            AddEventScreenTestTags.getTestTagProfileSuggestionItem(uidParticipant),
            useUnmergedTree = true)
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(AddEventScreenTestTags.getTestTagProfileAddItem(uidParticipant))
        .assertIsDisplayed()
        .performClick()

    composeTestRule
        .onNodeWithTag(AddEventScreenTestTags.BUTTON_SEE_ADDED_PARTICIPANT)
        .assertIsDisplayed()
        .performClick()

    composeTestRule
        .onNodeWithTag(
            AddEventScreenTestTags.getTestTagAddedProfileItem(uidParticipant),
            useUnmergedTree = true)
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(
            AddEventScreenTestTags.getTestTagAddedProfileRemoveItem(uidParticipant),
            useUnmergedTree = true)
        .assertIsDisplayed()
  }

  // This function fills the profile repository with the created profiles, and the event repository
  // with the created event
  fun fill_repositories() {
    runTest {
      profileRepository.addProfile(profile1)
      profileRepository.addProfile(profile2)
      profileRepository.addProfile(profile3)
      profileRepository.addProfile(participantProfile)
      profileRepository.addProfile(friendProfile)
      profileRepository.addProfile(ownerProfile)
      eventsRepository.addEvent(event)
      profileRepository.createEvent(event.id, ownerProfile.uid)
      profileRepository.addFriend(friendProfile.username, ownerProfile.uid)
      advanceUntilIdle()
    }
  }
}
