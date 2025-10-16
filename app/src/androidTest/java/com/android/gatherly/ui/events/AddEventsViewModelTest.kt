package com.android.gatherly.ui.events

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.gatherly.model.event.Event
import com.android.gatherly.model.event.EventStatus
import com.android.gatherly.model.event.EventsLocalRepository
import com.android.gatherly.model.event.EventsRepository
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.utils.FirebaseEmulator
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import java.text.SimpleDateFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class AddEventsViewModelTest {

  // declare viewModel and repositories
  private lateinit var addEventViewModel: AddEventViewModel
  private lateinit var eventsRepository: EventsRepository
  private lateinit var profileRepository: ProfileRepository

  // initialize this so that tests control all coroutines and can wait on them
  private val testDispatcher = UnconfinedTestDispatcher()

  @Before
  fun setUp() {
    // so that tests can wait on coroutines
    Dispatchers.setMain(testDispatcher)

    if (!FirebaseEmulator.isRunning) {
      error("Firebase emulator must be running! Use: firebase emulators:start")
    }
    runTest {
      FirebaseEmulator.auth.signInAnonymously().await()
      ownerProfile = ownerProfile.copy(uid = Firebase.auth.currentUser?.uid!!)

      // initialize repos and viewModel
      profileRepository = ProfileLocalRepository()
      eventsRepository = EventsLocalRepository()

      // fill the profile and events repositories with profiles and event
      fill_repositories()
      advanceUntilIdle()

      addEventViewModel = AddEventViewModel(profileRepository, eventsRepository)
    }
  }

  @After
  fun tearDown() {
    FirebaseEmulator.clearAuthEmulator()
    FirebaseEmulator.clearFirestoreEmulator()
    Dispatchers.resetMain()
  }

  /*----------------------------------------Profiles--------------------------------------------*/
  private val profile1: Profile =
      Profile(
          uid = "1",
          name = "Profile1",
          focusSessionIds = emptyList(),
          eventIds = emptyList(),
          groupIds = emptyList(),
          friendUids = emptyList())

  private val profile2: Profile =
      Profile(
          uid = "2",
          name = "Profile2",
          focusSessionIds = emptyList(),
          eventIds = emptyList(),
          groupIds = emptyList(),
          friendUids = emptyList())

  private val profile3: Profile =
      Profile(
          uid = "3",
          name = "Profile3",
          focusSessionIds = emptyList(),
          eventIds = emptyList(),
          groupIds = emptyList(),
          friendUids = emptyList())

  private val participantProfile: Profile =
      Profile(
          uid = "4",
          name = "Participant",
          focusSessionIds = emptyList(),
          eventIds = emptyList(),
          groupIds = emptyList(),
          friendUids = emptyList())

  private var ownerProfile: Profile =
      Profile(
          uid = "0",
          name = "Owner",
          focusSessionIds = emptyList(),
          eventIds = emptyList(),
          groupIds = emptyList(),
          friendUids = emptyList())

  /*----------------------------------------Event-----------------------------------------------*/
  val event1: Event =
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

  /*-------------------------------------Title tests--------------------------------------------*/
  // Title accepts a valid string
  @Test
  fun canEnterEventTitle() {
    runTest {
      val titleString = "This is a title!"
      addEventViewModel.updateName(titleString)
      assert(!addEventViewModel.uiState.nameError) { "Entering title should not make an error" }
      assert(addEventViewModel.uiState.name == titleString) { "Entering title should work" }
    }
  }

  // Title does not accept a blank string
  @Test
  fun cannotEnterEmptyEventTitle() {
    runTest {
      val blankString = " "
      addEventViewModel.updateName(blankString)
      assert(addEventViewModel.uiState.nameError) { "Blank title should be wrong" }
    }
  }

  /*-------------------------------Description tests--------------------------------------------*/
  // Description accepts valid string
  @Test
  fun canEnterEventDescription() {
    runTest {
      val descriptionString = "This is the description of this very very cool event"
      addEventViewModel.updateDescription(descriptionString)
      assert(!addEventViewModel.uiState.descriptionError) {
        "Entering description should not make an error"
      }
      assert(addEventViewModel.uiState.description == descriptionString) {
        "Entering description should work"
      }
    }
  }

  // Description does not accept invalid string
  @Test
  fun cannotEnterEmptyEventDescription() {
    runTest {
      val blankString = " "
      addEventViewModel.updateDescription(blankString)
      assert(addEventViewModel.uiState.descriptionError) { "Blank description should be wrong" }
    }
  }

  /*------------------------------Creator name tests--------------------------------------------*/
  // Creator name accepts valid string
  @Test
  fun canEnterEventCreatorName() {
    runTest {
      val creatorNameString = "creator"
      addEventViewModel.updateCreatorName(creatorNameString)
      assert(!addEventViewModel.uiState.creatorNameError) {
        "Entering a creator name should not make an error"
      }
      assert(addEventViewModel.uiState.creatorName == creatorNameString) {
        "Entering a creator name should work"
      }
    }
  }

  // Creator name does not accept blank string
  @Test
  fun cannotEnterEmptyEventCreatorName() {
    runTest {
      val blankString = " "
      addEventViewModel.updateCreatorName(blankString)
      assert(addEventViewModel.uiState.creatorNameError) { "Blank creator name should be wrong" }
    }
  }

  /*----------------------------------Due date tests--------------------------------------------*/
  // Due date accepts valid due date
  @Test
  fun canEnterEventDueDate() {
    runTest {
      val dateString = "13/12/2025"
      addEventViewModel.updateDate(dateString)
      assert(!addEventViewModel.uiState.dateError) { "\'13/12/2025\' should not make an error" }
      assert(addEventViewModel.uiState.date == dateString) { "\'13/12/2025\' should work" }
    }
  }

  // Due date does not accept words due date
  @Test
  fun cannotEnterInvalidEventDueDate1() {
    runTest {
      val dateString = "This date should not work"
      addEventViewModel.updateDate(dateString)
      assert(addEventViewModel.uiState.dateError) {
        "\'This date should not work\' should be wrong"
      }
    }
  }

  // Due date does not accept empty due date
  @Test
  fun cannotEnterInvalidEventDueDate2() {
    runTest {
      val dateString = ""
      addEventViewModel.updateDate(dateString)
      assert(addEventViewModel.uiState.dateError) { "Empty date should be wrong" }
    }
  }

  // Due date does not accept inexistent date
  @Test
  fun cannotEnterInvalidEventDueDate3() {
    runTest {
      val dateString = "33/12/2025"
      addEventViewModel.updateDate(dateString)
      assert(addEventViewModel.uiState.dateError) { "\'33/12/2025\' should be wrong" }
    }
  }

  /*--------------------------------Start time tests--------------------------------------------*/
  // Start time accepts valid time
  @Test
  fun canEnterEventStartTime() {
    runTest {
      val startTimeString = "13:15"
      addEventViewModel.updateStartTime(startTimeString)
      assert(!addEventViewModel.uiState.startTimeError) { "\'13:15\' should work" }
      assert(addEventViewModel.uiState.startTime == startTimeString) { "\'13:15\' should work" }
    }
  }

  // Start time does not accept words time
  @Test
  fun cannotEnterInvalidEventStartTime1() {
    runTest {
      val startTimeString = "This time should not work"
      addEventViewModel.updateStartTime(startTimeString)
      assert(addEventViewModel.uiState.startTimeError) {
        "\'This time should not work\' should be wrong"
      }
    }
  }

  // Start time does not accept empty time
  @Test
  fun cannotEnterInvalidEventStartTime2() {
    runTest {
      val startTimeString = ""
      addEventViewModel.updateStartTime(startTimeString)
      assert(addEventViewModel.uiState.startTimeError) { "Empty start time should be wrong" }
    }
  }

  // Start time does not accept inexistent time
  @Test
  fun cannotEnterInvalidEventStartTime3() {
    runTest {
      val startTimeString = "25:77"
      addEventViewModel.updateStartTime(startTimeString)
      assert(addEventViewModel.uiState.startTimeError) { "\'25:77\' should be wrong" }
    }
  }

  /*----------------------------------End time tests--------------------------------------------*/
  // End time accepts valid time
  @Test
  fun canEnterEventEndTime() {
    runTest {
      val endTimeString = "14:15"
      addEventViewModel.updateEndTime(endTimeString)
      assert(!addEventViewModel.uiState.endTimeError) { "\'14:15\' should not make an error" }
      assert(addEventViewModel.uiState.endTime == endTimeString) { "\'14:15\' should work" }
    }
  }

  // End time does not accept words time
  @Test
  fun cannotEnterInvalidEventEndTime1() {
    runTest {
      val endTimeString = "This time should not work"
      addEventViewModel.updateEndTime(endTimeString)
      assert(addEventViewModel.uiState.endTimeError) {
        "\'This time should not work\' should be wrong"
      }
    }
  }

  // End time does not accept empty time
  @Test
  fun cannotEnterInvalidEventEndTime2() {
    runTest {
      val endTimeString = ""
      addEventViewModel.updateEndTime(endTimeString)
      assert(addEventViewModel.uiState.endTimeError) { "Empty end time should be wrong" }
    }
  }

  // End time does not accept inexistent time
  @Test
  fun cannotEnterInvalidEventEndTime3() {
    runTest {
      val endTimeString = "25:77"
      addEventViewModel.updateEndTime(endTimeString)
      assert(addEventViewModel.uiState.endTimeError) { "\'25:77\' should be wrong" }
    }
  }

  /*----------------------------------Participant tests-----------------------------------------*/
  // Entering a participant updates string
  @Test
  fun canEnterEventParticipant() {
    runTest {
      val participantString = "Participant"
      addEventViewModel.updateParticipant(participantString)
      assert(addEventViewModel.uiState.participant == participantString) {
        "\'Participant\' should update the search string"
      }
    }
  }

  // Searching for profile returns correct profiles
  @Test
  fun canFindProfiles1() {
    runTest {
      val participantString = "Profile"
      addEventViewModel.updateParticipant(participantString)
      assert(participantString == addEventViewModel.uiState.participant) {
        "\'Profile\' should update the search string"
      }

      addEventViewModel.searchProfileByString(participantString)
      // wait for search coroutine to complete
      advanceUntilIdle()

      assert(addEventViewModel.uiState.suggestedProfiles.size == 3) {
        "\'Profile\' should find 3 profiles"
      }
      assert(addEventViewModel.uiState.suggestedProfiles.contains(profile1)) {
        "\'Profile\' should find Profile1"
      }
      assert(addEventViewModel.uiState.suggestedProfiles.contains(profile2)) {
        "\'Profile\' should find Profile2"
      }
      assert(addEventViewModel.uiState.suggestedProfiles.contains(profile3)) {
        "\'Profile\' should find Profile3"
      }
    }
  }

  // Searching for inexistent profile returns no suggestions
  @Test
  fun canFindProfiles2() {
    runTest {
      val participantString = "Not a profile"
      addEventViewModel.updateParticipant(participantString)
      assert(addEventViewModel.uiState.participant == participantString) {
        "\'Not a profile\' should update the search string"
      }

      addEventViewModel.searchProfileByString(participantString)
      // wait for search coroutine to complete
      advanceUntilIdle()

      assert(addEventViewModel.uiState.suggestedProfiles.isEmpty()) {
        "\'Not a profile\' should find no profiles"
      }
    }
  }

  // Can add a participant to an event
  @Test
  fun canAddEventParticipant() {
    runTest {
      addEventViewModel.updateParticipant(profile1.name)
      assert(addEventViewModel.uiState.participant == profile1.name) {
        "\'Profile1\' should update the search string"
      }

      addEventViewModel.addParticipant(profile1)
      assert(addEventViewModel.uiState.participants.contains(profile1)) {
        "\'Profile1\' should be in participants"
      }
    }
  }

  // Cannot add a participant that is already participating
  @Test
  fun cannotAddParticipatingEventParticipant() {
    runTest {
      addEventViewModel.addParticipant(participantProfile)

      addEventViewModel.updateParticipant(participantProfile.name)
      assert(addEventViewModel.uiState.participant == participantProfile.name) {
        "\'Participant\' should update the search string"
      }

      addEventViewModel.addParticipant(participantProfile)
      assert(addEventViewModel.uiState.displayToast) {
        "Adding \'Participant\' should display toast"
      }
    }
  }

  // Can remove a participant of an event
  @Test
  fun canRemoveEventParticipant() {
    runTest {
      addEventViewModel.addParticipant(participantProfile)
      addEventViewModel.updateParticipant(participantProfile.name)
      assert(addEventViewModel.uiState.participant == participantProfile.name) {
        "\'Participant\' should update the search string"
      }

      addEventViewModel.deleteParticipant(participantProfile.uid)
      assert(!addEventViewModel.uiState.participants.contains(participantProfile)) {
        "Removing \'Participant\' should work"
      }
    }
  }

  // Cannot remove a participant that is not participating
  @Test
  fun cannotRemoveUnparticipatingEventParticipant() {
    runTest {
      addEventViewModel.updateParticipant(profile1.name)
      assert(addEventViewModel.uiState.participant == profile1.name) {
        "\'Profile1\' should update the search string"
      }

      addEventViewModel.deleteParticipant(profile1.uid)
      assert(addEventViewModel.uiState.displayToast) {
        "Removing \'Profile1\' should display toast"
      }
    }
  }

  // The event owner cannot not be a participant to his own event
  @Test
  fun cannotRemoveEventOwner() {
    runTest {
      addEventViewModel.updateParticipant(ownerProfile.name)
      assert(addEventViewModel.uiState.participant == ownerProfile.name) {
        "\'Owner\' should update the search string"
      }

      addEventViewModel.addParticipant(ownerProfile)
      assert(addEventViewModel.uiState.displayToast) { "Removing \'Owner\' should display toast" }
    }
  }

  /*---------------------------------------Saving tests-----------------------------------------*/
  // Check that modifying the title and saving works
  @Test
  fun canSaveEvent1() {
    runTest {
      addEventViewModel.updateName(event1.title)
      addEventViewModel.updateDescription(event1.description)
      addEventViewModel.updateCreatorName(event1.creatorName)
      addEventViewModel.updateDate(SimpleDateFormat("dd/MM/yyyy").format(event1.date.toDate()))
      addEventViewModel.updateStartTime(SimpleDateFormat("HH:mm").format(event1.startTime.toDate()))
      addEventViewModel.updateEndTime(SimpleDateFormat("HH:mm").format(event1.endTime.toDate()))
      addEventViewModel.saveEvent()
      // wait
      assert(eventsRepository.getAllEvents().size == 1) { "The event is not added" }
    }
  }

  /*-------------------------------------Deleting tests-----------------------------------------*/

  // This function fills the profile repository with the created profiles
  fun fill_repositories() {
    runTest {
      profileRepository.addProfile(profile1)
      profileRepository.addProfile(profile2)
      profileRepository.addProfile(profile3)
      profileRepository.addProfile(participantProfile)
      profileRepository.addProfile(ownerProfile)
      advanceUntilIdle()
    }
  }
}
