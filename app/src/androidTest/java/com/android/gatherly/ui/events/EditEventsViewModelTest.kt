package com.android.gatherly.ui.events

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.gatherly.model.event.Event
import com.android.gatherly.model.event.EventStatus
import com.android.gatherly.model.event.EventsLocalRepository
import com.android.gatherly.model.event.EventsRepository
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class EditEventsViewModelTest {

  // declare viewModel and repositories
  private lateinit var editEventsViewModel: EditEventsViewModel
  private lateinit var eventsRepository: EventsRepository
  private lateinit var profileRepository: ProfileRepository

  // initialize this so that tests control all couroutines and can wait on them
  private val testDispatcher = UnconfinedTestDispatcher()

  @Before
  fun setUp() {
    // so that tests can wait on coroutines
    Dispatchers.setMain(testDispatcher)

    // initialize repos and viewModel
    profileRepository = ProfileLocalRepository()
    eventsRepository = EventsLocalRepository()
    editEventsViewModel = EditEventsViewModel(profileRepository, eventsRepository)

    // fill the profile and events repositories with profiles and event
    runTest {
      fill_repositories()
      advanceUntilIdle()
    }

    // set the event to edit
    editEventsViewModel.setEventValues(event.id)
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

  val ownerProfile: Profile =
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

  /*-------------------------------------Title tests--------------------------------------------*/
  // Title accepts a valid string
  @Test
  fun canEnterEventTitle() {
    runTest {
      val titleString = "This is a title!"
      editEventsViewModel.updateName(titleString)
      assert(!editEventsViewModel.uiState.nameError) { "Entering title should not make an error" }
      assert(editEventsViewModel.uiState.name == titleString) { "Entering title should work" }
    }
  }

  // Title does not accept a blank string
  @Test
  fun cannotEnterEmptyEventTitle() {
    runTest {
      val blankString = " "
      editEventsViewModel.updateName(blankString)
      assert(editEventsViewModel.uiState.nameError) { "Blank title should be wrong" }
    }
  }

  /*-------------------------------Description tests--------------------------------------------*/
  // Description accepts valid string
  @Test
  fun canEnterEventDescription() {
    runTest {
      val descriptionString = "This is the description of this very very cool event"
      editEventsViewModel.updateDescription(descriptionString)
      assert(!editEventsViewModel.uiState.descriptionError) {
        "Entering description should not make an error"
      }
      assert(editEventsViewModel.uiState.description == descriptionString) {
        "Entering description should work"
      }
    }
  }

  // Description does not accept invalid string
  @Test
  fun cannotEnterEmptyEventDescription() {
    runTest {
      val blankString = " "
      editEventsViewModel.updateDescription(blankString)
      assert(editEventsViewModel.uiState.descriptionError) { "Blank description should be wrong" }
    }
  }

  /*------------------------------Creator name tests--------------------------------------------*/
  // Creator name accepts valid string
  @Test
  fun canEnterEventCreatorName() {
    runTest {
      val creatorNameString = "creator"
      editEventsViewModel.updateCreatorName(creatorNameString)
      assert(!editEventsViewModel.uiState.creatorNameError) {
        "Entering a creator name should not make an error"
      }
      assert(editEventsViewModel.uiState.creatorName == creatorNameString) {
        "Entering a creator name should work"
      }
    }
  }

  // Creator name does not accept blank string
  @Test
  fun cannotEnterEmptyEventCreatorName() {
    runTest {
      val blankString = " "
      editEventsViewModel.updateCreatorName(blankString)
      assert(editEventsViewModel.uiState.creatorNameError) { "Blank creator name should be wrong" }
    }
  }

  /*----------------------------------Due date tests--------------------------------------------*/
  // Due date accepts valid due date
  @Test
  fun canEnterEventDueDate() {
    runTest {
      val dateString = "13/12/2025"
      editEventsViewModel.updateDate(dateString)
      assert(!editEventsViewModel.uiState.dateError) { "\'13/12/2025\' should not make an error" }
      assert(editEventsViewModel.uiState.date == dateString) { "\'13/12/2025\' should work" }
    }
  }

  // Due date does not accept words due date
  @Test
  fun cannotEnterInvalidEventDueDate1() {
    runTest {
      val dateString = "This date should not work"
      editEventsViewModel.updateDate(dateString)
      assert(editEventsViewModel.uiState.dateError) {
        "\'This date should not work\' should be wrong"
      }
    }
  }

  // Due date does not accept empty due date
  @Test
  fun cannotEnterInvalidEventDueDate2() {
    runTest {
      val dateString = ""
      editEventsViewModel.updateDate(dateString)
      assert(editEventsViewModel.uiState.dateError) { "Empty date should be wrong" }
    }
  }

  // Due date does not accept inexistent date
  @Test
  fun cannotEnterInvalidEventDueDate3() {
    runTest {
      val dateString = "33/12/2025"
      editEventsViewModel.updateDate(dateString)
      assert(editEventsViewModel.uiState.dateError) { "\'33/12/2025\' should be wrong" }
    }
  }

  /*--------------------------------Start time tests--------------------------------------------*/
  // Start time accepts valid time
  @Test
  fun canEnterEventStartTime() {
    runTest {
      val startTimeString = "13:15"
      editEventsViewModel.updateStartTime(startTimeString)
      assert(!editEventsViewModel.uiState.startTimeError) { "\'13:15\' should work" }
      assert(editEventsViewModel.uiState.startTime == startTimeString) { "\'13:15\' should work" }
    }
  }

  // Start time does not accept words time
  @Test
  fun cannotEnterInvalidEventStartTime1() {
    runTest {
      val startTimeString = "This time should not work"
      editEventsViewModel.updateStartTime(startTimeString)
      assert(editEventsViewModel.uiState.startTimeError) {
        "\'This time should not work\' should be wrong"
      }
    }
  }

  // Start time does not accept empty time
  @Test
  fun cannotEnterInvalidEventStartTime2() {
    runTest {
      val startTimeString = ""
      editEventsViewModel.updateStartTime(startTimeString)
      assert(editEventsViewModel.uiState.startTimeError) { "Empty start time should be wrong" }
    }
  }

  // Start time does not accept inexistent time
  @Test
  fun cannotEnterInvalidEventStartTime3() {
    runTest {
      val startTimeString = "25:77"
      editEventsViewModel.updateStartTime(startTimeString)
      assert(editEventsViewModel.uiState.startTimeError) { "\'25:77\' should be wrong" }
    }
  }

  /*----------------------------------End time tests--------------------------------------------*/
  // Start time accepts valid time
  @Test
  fun canEnterEventEndTime() {
    runTest {
      val endTimeString = "14:15"
      editEventsViewModel.updateEndTime(endTimeString)
      assert(!editEventsViewModel.uiState.endTimeError) { "\'14:15\' should not make an error" }
      assert(editEventsViewModel.uiState.endTime == endTimeString) { "\'14:15\' should work" }
    }
  }

  // Start time does not accept words time
  @Test
  fun cannotEnterInvalidEventEndTime1() {
    runTest {
      val endTimeString = "This time should not work"
      editEventsViewModel.updateEndTime(endTimeString)
      assert(editEventsViewModel.uiState.endTimeError) {
        "\'This time should not work\' should be wrong"
      }
    }
  }

  // Start time does not accept empty time
  @Test
  fun cannotEnterInvalidEventEndTime2() {
    runTest {
      val endTimeString = ""
      editEventsViewModel.updateEndTime(endTimeString)
      assert(editEventsViewModel.uiState.endTimeError) { "Empty end time should be wrong" }
    }
  }

  // Start time does not accept inexistent time
  @Test
  fun cannotEnterInvalidEventEndTime3() {
    runTest {
      val endTimeString = "25:77"
      editEventsViewModel.updateEndTime(endTimeString)
      assert(editEventsViewModel.uiState.endTimeError) { "\'25:77\' should be wrong" }
    }
  }

  /*----------------------------------Participant tests-----------------------------------------*/
  // Entering a participant updates string
  @Test
  fun canEnterEventParticipant() {
    runTest {
      val participantString = "Participant"
      editEventsViewModel.updateParticipant(participantString)
      assert(editEventsViewModel.uiState.participant == participantString) {
        "\'Participant\' should update the search string"
      }
    }
  }

  // Searching for profile returns correct profiles
  @Test
  fun canFindProfiles1() {
    runTest {
      val participantString = "Profile"
      editEventsViewModel.updateParticipant(participantString)
      assert(participantString == editEventsViewModel.uiState.participant) {
        "\'Profile\' should update the search string"
      }

      editEventsViewModel.searchProfileByString(participantString)
      // wait for search coroutine to complete
      advanceUntilIdle()

      assert(editEventsViewModel.uiState.suggestedProfiles.size == 3) {
        "\'Profile\' should find 3 profiles"
      }
      assert(editEventsViewModel.uiState.suggestedProfiles.contains(profile1)) {
        "\'Profile\' should find Profile1"
      }
      assert(editEventsViewModel.uiState.suggestedProfiles.contains(profile2)) {
        "\'Profile\' should find Profile2"
      }
      assert(editEventsViewModel.uiState.suggestedProfiles.contains(profile3)) {
        "\'Profile\' should find Profile3"
      }
    }
  }

  // Searching for inexistent profile returns no suggestions
  @Test
  fun canFindProfiles2() {
    runTest {
      val participantString = "Not a profile"
      editEventsViewModel.updateParticipant(participantString)
      assert(editEventsViewModel.uiState.participant == participantString) {
        "\'Not a profile\' should update the search string"
      }

      editEventsViewModel.searchProfileByString(participantString)
      // wait for search coroutine to complete
      advanceUntilIdle()

      assert(editEventsViewModel.uiState.suggestedProfiles.isEmpty()) {
        "\'Not a profile\' should find no profiles"
      }
    }
  }

  // Can add a participant to an event
  @Test
  fun canAddEventParticipant() {
    runTest {
      editEventsViewModel.updateParticipant(profile1.name)
      editEventsViewModel.addParticipant(profile1)
      assert(editEventsViewModel.uiState.participant == profile1.name) {
        "\'Profile1\' should update the search string"
      }
      assert(editEventsViewModel.uiState.participants.contains(profile1)) {
        "\'Profile1\' should be in participants"
      }
    }
  }

  // Cannot add a participant that is already participating
  @Test
  fun cannotAddParticipatingEventParticipant() {
    runTest {
      editEventsViewModel.updateParticipant(participantProfile.name)
      editEventsViewModel.addParticipant(participantProfile)
      assert(editEventsViewModel.uiState.participant == participantProfile.name) {
        "\'Participant\' should update the search string"
      }
      assert(editEventsViewModel.uiState.displayToast) {
        "Adding \'Participant\' should display toast"
      }
    }
  }

  // Can remove a participant of an event
  @Test
  fun canRemoveEventParticipant() {
    runTest {
      editEventsViewModel.updateParticipant(participantProfile.name)
      editEventsViewModel.deleteParticipant(participantProfile.uid)
      assert(editEventsViewModel.uiState.participant == participantProfile.name) {
        "\'Participant\' should update the search string"
      }
      assert(!editEventsViewModel.uiState.participants.contains(participantProfile)) {
        "Removing \'Participant\' should work"
      }
    }
  }

  // Cannot remove a participant that is not participating
  @Test
  fun cannotRemoveUnparticipatingEventParticipant() {
    runTest {
      editEventsViewModel.updateParticipant(profile1.name)
      editEventsViewModel.deleteParticipant(profile1.uid)
      assert(editEventsViewModel.uiState.participant == profile1.name) {
        "\'Profile1\' should update the search string"
      }
      assert(editEventsViewModel.uiState.displayToast) {
        "Removing \'Profile1\' should display toast"
      }
    }
  }

  // The event owner cannot not be a participant to his own event
  @Test
  fun cannotRemoveEventOwner() {
    runTest {
      editEventsViewModel.updateParticipant(ownerProfile.name)
      editEventsViewModel.addParticipant(ownerProfile)
      assert(editEventsViewModel.uiState.participant == ownerProfile.name) {
        "\'Owner\' should update the search string"
      }
      assert(editEventsViewModel.uiState.displayToast) { "Removing \'Owner\' should display toast" }
    }
  }

  /*---------------------------------------Saving tests-----------------------------------------*/
  // Check that modifying the title and saving works
  @Test
  fun canSaveEvent1() {
    runTest {
      editEventsViewModel.setEventValues(event.id)
      val modifiedEvent = event.copy(title = "Something else")
      editEventsViewModel.updateName(modifiedEvent.title)
      editEventsViewModel.saveEvent()
      // wait
      assert(eventsRepository.getEvent(event.id).title == modifiedEvent.title) {
        "The event is not modified"
      }
    }
  }

  // Check that modifying the creator name and saving works
  @Test
  fun canSaveEvent2() {
    runTest {
      editEventsViewModel.setEventValues(event.id)
      val modifiedEvent = event.copy(creatorName = "creator nameeeee")
      editEventsViewModel.updateCreatorName(modifiedEvent.creatorName)
      editEventsViewModel.saveEvent()
      // wait
      assert(eventsRepository.getEvent(event.id).creatorName == modifiedEvent.creatorName) {
        "The event is not modified"
      }
    }
  }

  /*-------------------------------------Deleting tests-----------------------------------------*/

  // Can delete the event
  @Test
  fun canDeleteEvent() {
    runTest {
      editEventsViewModel.deleteEvent()
      // wait
      assert(editEventsViewModel.uiState.backToOverview) {
        "Successfully deleting should go back to overview events"
      }
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
