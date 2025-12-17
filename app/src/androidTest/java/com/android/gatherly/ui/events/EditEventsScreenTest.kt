package com.android.gatherly.ui.events

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.gatherly.model.event.Event
import com.android.gatherly.model.event.EventState
import com.android.gatherly.model.event.EventStatus
import com.android.gatherly.model.event.EventsLocalRepository
import com.android.gatherly.model.event.EventsRepository
import com.android.gatherly.model.group.Group
import com.android.gatherly.model.group.GroupsLocalRepository
import com.android.gatherly.model.group.GroupsRepository
import com.android.gatherly.model.map.FakeNominatimLocationRepository
import com.android.gatherly.model.map.Location
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.ui.todo.AddTodoScreenTestTags
import com.android.gatherly.utils.AlertDialogTestTags
import com.android.gatherly.utils.EventsParticipantsSuggestionTestTag
import com.google.firebase.Timestamp
import java.util.Date
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Tests for the EditEventsScreen */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class EditEventsScreenTest {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  // declare viewModel and repositories
  private lateinit var editEventsViewModel: EditEventsViewModel
  private lateinit var eventsRepository: EventsRepository
  private lateinit var profileRepository: ProfileRepository
  private lateinit var fakeNominatimClient: FakeNominatimLocationRepository
  private lateinit var groupsRepository: GroupsRepository

  @Before
  fun setUp() {
    // initialize repos and viewModel
    profileRepository = ProfileLocalRepository()
    eventsRepository = EventsLocalRepository()
    fakeNominatimClient = FakeNominatimLocationRepository()
    groupsRepository = GroupsLocalRepository()
    editEventsViewModel =
        EditEventsViewModel(
            profileRepository = profileRepository,
            eventsRepository = eventsRepository,
            nominatimClient = fakeNominatimClient,
            groupsRepository = groupsRepository)
  }

  fun setUpEvent(event: Event) {
    fill_repositories(event)

    composeTestRule.setContent { EditEventsScreen(event.id, editEventsViewModel) }
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

  val ownerProfile: Profile =
      Profile(
          uid = "0",
          name = "Owner",
          focusSessionIds = emptyList(),
          participatingEventIds = emptyList(),
          groupIds = emptyList(),
          friendUids = listOf(friendProfile.uid))

  /*----------------------------------------Event-----------------------------------------------*/
  private val oneHourLater = Timestamp(Date(System.currentTimeMillis() + 3600_000))
  private val twoHoursLater = Timestamp(Date(System.currentTimeMillis() + 7200_000))
  val event: Event =
      Event(
          id = "0",
          title = "Event title",
          description = "Describing this great event",
          creatorName = "my name :)",
          location = null,
          date = Timestamp.now(),
          startTime = oneHourLater,
          endTime = twoHoursLater,
          creatorId = ownerProfile.uid,
          participants = listOf(ownerProfile.uid, participantProfile.uid),
          status = EventStatus.UPCOMING,
          state = EventState.PUBLIC)

  val privateFriendsEvent: Event = event.copy(state = EventState.PRIVATE_FRIENDS)
  val privateGroupEvent = event.copy(state = EventState.PRIVATE_GROUP)

  /** Check that all components are displayed */
  @Test
  fun displayAllComponents() {
    setUpEvent(event)
    composeTestRule.onNodeWithTag(EditEventsScreenTestTags.INPUT_NAME).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EditEventsScreenTestTags.INPUT_DESCRIPTION).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EditEventsScreenTestTags.INPUT_PARTICIPANT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EditEventsScreenTestTags.INPUT_LOCATION).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EditEventsScreenTestTags.INPUT_DATE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EditEventsScreenTestTags.INPUT_START).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EditEventsScreenTestTags.INPUT_END).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(AddTodoScreenTestTags.ERROR_MESSAGE, useUnmergedTree = true)
        .assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(EditEventsScreenTestTags.BTN_SAVE)
    composeTestRule.onNodeWithTag(EditEventsScreenTestTags.BTN_DELETE)
  }

  /** Check that menus are displayed */
  @Test
  fun displayMenus() {
    setUpEvent(event)
    fakeNominatimClient.setSearchResults(
        "Paris",
        listOf(
            Location(latitude = 48.8566, longitude = 2.3522, name = "Paris, France"),
            Location(
                latitude = 48.8534, longitude = 2.3488, name = "Paris, Île-de-France, France")))

    composeTestRule.onNodeWithTag(EditEventsScreenTestTags.INPUT_LOCATION).performTextInput("Paris")
    composeTestRule.waitUntil(timeoutMillis = 5000L) {
      composeTestRule.onNodeWithTag(EditEventsScreenTestTags.LOCATION_MENU).isDisplayed()
    }

    composeTestRule
        .onNodeWithTag(EditEventsScreenTestTags.INPUT_PARTICIPANT)
        .performTextInput("Participant")
    composeTestRule.waitUntil(timeoutMillis = 5000L) {
      composeTestRule.onNodeWithTag(EditEventsScreenTestTags.PARTICIPANT_MENU).isDisplayed()
    }
  }

  /**
   * Check that when scrolling to the delete button, then pressing it shows the delete alert dialog
   */
  @Test
  fun deleteEventShowsAlertDialog() {
    setUpEvent(event)
    composeTestRule
        .onNodeWithTag(EditEventsScreenTestTags.LIST)
        .performScrollToNode(hasTestTag(EditEventsScreenTestTags.BTN_DELETE))
    composeTestRule
        .onNodeWithTag(EditEventsScreenTestTags.BTN_DELETE)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.onNodeWithTag(AlertDialogTestTags.ALERT).assertIsDisplayed()
  }

  /** Test: Verifies that we can turn an private event to a public one */
  @Test
  fun testCheckOnPublicFromPrivate() {
    setUpEvent(privateGroupEvent)
    composeTestRule
        .onNodeWithTag(EditEventsScreenTestTags.SWITCH_PUBLIC_PRIVATE_EVENT)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.onNodeWithTag(AlertDialogTestTags.ALERT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AlertDialogTestTags.CONFIRM_BTN).performClick()
    composeTestRule
        .onNodeWithTag(EditEventsScreenTestTags.SWITCH_PUBLIC_PRIVATE_EVENT)
        .assertIsNotDisplayed()
  }

  /** Test: Verifies that group suggestion working correctly */
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
    val group = groupsRepository.getGroupByName("MyGroup")
    setUpEvent(privateGroupEvent)

    composeTestRule
        .onNodeWithTag(EventsParticipantsSuggestionTestTag.INPUT_GROUP)
        .performTextInput("My")

    composeTestRule.waitUntil(timeoutMillis = 10000L) {
      composeTestRule.onNodeWithTag(EventsParticipantsSuggestionTestTag.GROUP_MENU).isDisplayed()

      composeTestRule
          .onNodeWithTag(
              EventsParticipantsSuggestionTestTag.getTestTagGroupSuggestionItem(group.gid),
              useUnmergedTree = true)
          .isDisplayed()
    }

    composeTestRule
        .onNodeWithTag(EventsParticipantsSuggestionTestTag.getTestTagGroupSuggestionAdd(group.gid))
        .assertIsDisplayed()
        .performClick()

    composeTestRule
        .onNodeWithTag(EventsParticipantsSuggestionTestTag.getTestTagGroupSuggestionAdd(group.gid))
        .assertIsNotDisplayed()

    composeTestRule
        .onNodeWithTag(
            EventsParticipantsSuggestionTestTag.getTestTagGroupSuggestionRemove(group.gid),
            useUnmergedTree = true)
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(EventsParticipantsSuggestionTestTag.BUTTON_SEE_ADDED_GROUP)
        .assertIsDisplayed()
        .performClick()
  }

  /** Test: Verify that the suggestion works for private friends event */
  @Test
  fun testFriendsSuggestionsAppear() {
    val nameParticipant = friendProfile.name
    val uidParticipant = friendProfile.uid

    setUpEvent(privateFriendsEvent)

    composeTestRule
        .onNodeWithTag(EventsParticipantsSuggestionTestTag.INPUT_PARTICIPANT)
        .performTextInput(nameParticipant)

    composeTestRule.waitUntil(timeoutMillis = 10000L) {
      composeTestRule
          .onAllNodes(hasTestTag(EventsParticipantsSuggestionTestTag.PARTICIPANT_MENU))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule
        .onNodeWithTag(
            EventsParticipantsSuggestionTestTag.getTestTagProfileSuggestionItem(uidParticipant),
            useUnmergedTree = true)
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(EventsParticipantsSuggestionTestTag.getTestTagProfileAddItem(uidParticipant))
        .assertIsDisplayed()
        .performClick()

    composeTestRule
        .onNodeWithTag(EventsParticipantsSuggestionTestTag.BUTTON_SEE_ADDED_PARTICIPANT)
        .assertIsDisplayed()
        .performClick()

    composeTestRule
        .onNodeWithTag(
            EventsParticipantsSuggestionTestTag.getTestTagAddedProfileItem(uidParticipant),
            useUnmergedTree = true)
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(
            EventsParticipantsSuggestionTestTag.getTestTagAddedProfileRemoveItem(uidParticipant),
            useUnmergedTree = true)
        .assertIsDisplayed()
  }

  // This function fills the profile repository with the created profiles, and the event repository
  // with the created event
  fun fill_repositories(event: Event) {
    runTest {
      profileRepository.addProfile(profile1)
      profileRepository.addProfile(profile2)
      profileRepository.addProfile(profile3)
      profileRepository.addProfile(participantProfile)
      profileRepository.addProfile(friendProfile)
      profileRepository.addProfile(ownerProfile)
      profileRepository.addFriend(friendProfile.username, ownerProfile.uid)
      eventsRepository.addEvent(event)
      profileRepository.createEvent(event.id, ownerProfile.uid)
      profileRepository.participateEvent(event.id, participantProfile.uid)

      advanceUntilIdle()
    }
  }
}
