package com.android.gatherly.ui.events

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
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
  private lateinit var mockitoUtils: MockitoUtils

  @Before
  fun setUp() {

    profileRepository = ProfileLocalRepository()
    eventsRepository = EventsLocalRepository()

    fill_repositories()

    // Mock Firebase Auth
    mockitoUtils = MockitoUtils()
    mockitoUtils.chooseCurrentUser("0")

    addEventsViewModel =
        AddEventViewModel(
            profileRepository = profileRepository,
            eventsRepository = eventsRepository,
            authProvider = { mockitoUtils.mockAuth })

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

  var ownerProfile: Profile =
      Profile(
          uid = "0",
          name = "Owner",
          focusSessionIds = emptyList(),
          participatingEventIds = emptyList(),
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

  /** Check that the date input are working */
  @Test
  fun testDatePickerWorkingCorrectly() {
    composeTestRule.openDatePicker(AddEventScreenTestTags.INPUT_DATE)
    composeTestRule.selectDateFromPicker(currentDay, currentMonth, futureYear)
    composeTestRule
        .onNodeWithTag(AddEventScreenTestTags.INPUT_DATE)
        .assertTextContains(futureDate, ignoreCase = true)
    composeTestRule.onNodeWithTag(AddEventScreenTestTags.ERROR_MESSAGE).assertIsNotDisplayed()
  }

  /** Check that if the date is already past shows an error message */
  @Test
  fun testPastDateShowErrror() {
    composeTestRule.openDatePicker(AddEventScreenTestTags.INPUT_DATE)
    composeTestRule.selectDateFromPicker(currentDay, currentMonth, pastYear)
    composeTestRule
        .onNodeWithTag(AddEventScreenTestTags.INPUT_DATE)
        .assertTextContains(pastDate, ignoreCase = true)
    composeTestRule.onNodeWithTag(AddEventScreenTestTags.ERROR_MESSAGE)
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
      profileRepository.createEvent(event.id, ownerProfile.uid)
      advanceUntilIdle()
    }
  }
}
