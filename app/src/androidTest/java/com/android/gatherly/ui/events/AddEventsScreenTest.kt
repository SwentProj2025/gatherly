package com.android.gatherly.ui.events

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.gatherly.model.event.Event
import com.android.gatherly.model.event.EventStatus
import com.android.gatherly.model.event.EventsLocalRepository
import com.android.gatherly.model.event.EventsRepository
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.ui.todo.AddToDoScreenTestTags
import com.android.gatherly.utils.FirebaseEmulator
import com.android.gatherly.utils.InMemoryGatherlyTest
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Tests for the AddEventScreen */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class AddEventsScreenTest : InMemoryGatherlyTest() {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  // declare viewModel and repositories
  private lateinit var addEventsViewModel: AddEventViewModel
  private lateinit var eventsRepository: EventsRepository
  private lateinit var profileRepository: ProfileRepository

  private val testDispatcher = UnconfinedTestDispatcher()

  @Before
  override fun setUp() {
    super.setUp()

    Dispatchers.setMain(testDispatcher)

    if (!FirebaseEmulator.isRunning) {
      error("Firebase emulator must be running! Use: firebase emulators:start")
    }
    runTest {
      FirebaseEmulator.auth.signInAnonymously().await()
      val uid = Firebase.auth.currentUser?.uid!!

      profileRepository = ProfileLocalRepository()
      eventsRepository = EventsLocalRepository()

      ownerProfile = ownerProfile.copy(uid = uid)
      profileRepository.addProfile(ownerProfile)
      profileRepository.addProfile(profile1)
      profileRepository.addProfile(profile2)
      profileRepository.addProfile(profile3)
      profileRepository.addProfile(participantProfile)

      eventsRepository.addEvent(
          event.copy(creatorId = uid, participants = listOf(uid, participantProfile.uid)))

      addEventsViewModel = AddEventViewModel(profileRepository, eventsRepository)
    }
    Dispatchers.resetMain()

    composeTestRule.setContent { AddEventScreen(addEventsViewModel) }
  }

  @After
  override fun tearDown() {
    FirebaseEmulator.clearAuthEmulator()
    FirebaseEmulator.clearFirestoreEmulator()
  }

  /*----------------------------------------Profiles--------------------------------------------*/
  val profile1: Profile =
      Profile(
          uid = "1",
          name = "Profile1",
          focusSessionIds = emptyList(),
          eventIds = emptyList(),
          groupIds = emptyList(),
          friendUids = emptyList())

  val profile2: Profile =
      Profile(
          uid = "2",
          name = "Profile2",
          focusSessionIds = emptyList(),
          eventIds = emptyList(),
          groupIds = emptyList(),
          friendUids = emptyList())

  val profile3: Profile =
      Profile(
          uid = "3",
          name = "Profile3",
          focusSessionIds = emptyList(),
          eventIds = emptyList(),
          groupIds = emptyList(),
          friendUids = emptyList())

  val participantProfile: Profile =
      Profile(
          uid = "4",
          name = "Participant",
          focusSessionIds = emptyList(),
          eventIds = emptyList(),
          groupIds = emptyList(),
          friendUids = emptyList())

  var ownerProfile: Profile =
      Profile(
          uid = "0",
          name = "Owner",
          focusSessionIds = emptyList(),
          eventIds = emptyList(),
          groupIds = emptyList(),
          friendUids = emptyList())

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
          status = EventStatus.UPCOMING)

  /** Check that all components are displayed */
  @Test
  fun displayAllComponents() {
    composeTestRule.onNodeWithTag(AddEventScreenTestTags.INPUT_NAME).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventScreenTestTags.INPUT_DESCRIPTION).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventScreenTestTags.INPUT_CREATOR).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventScreenTestTags.INPUT_PARTICIPANT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventScreenTestTags.INPUT_LOCATION).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventScreenTestTags.INPUT_DATE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventScreenTestTags.INPUT_START).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventScreenTestTags.INPUT_END).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(AddToDoScreenTestTags.ERROR_MESSAGE, useUnmergedTree = true)
        .assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(AddEventScreenTestTags.BTN_SAVE)
  }

  /** Check that menus are displayed */
  @Test
  fun displayMenus() {
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

  // This function fills the profile repository with the created profiles, and the event repository
  // with the created event
  fun fill_repositories() {
    runTest {
      profileRepository.addProfile(profile1)
      profileRepository.addProfile(profile2)
      profileRepository.addProfile(profile3)
      profileRepository.addProfile(participantProfile)
      profileRepository.addProfile(ownerProfile)
      eventsRepository.addEvent(event)
    }
  }
}
