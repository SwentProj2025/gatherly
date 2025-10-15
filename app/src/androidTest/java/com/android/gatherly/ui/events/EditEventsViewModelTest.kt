package com.android.gatherly.ui.events

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.gatherly.model.event.Event
import com.android.gatherly.model.event.EventStatus
import com.android.gatherly.model.event.EventsRepository
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileRepository
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditEventsViewModelTest {

    private lateinit var editEventsViewModel: EditEventsViewModel
    private lateinit var eventsRepository: EventsRepository
    private lateinit var profileRepository: ProfileRepository

    @Before
    fun setUp() {
        //editEventsViewModel = EditEventsViewModel()
        // init profile and events repo
    }

    val profile1 : Profile =
        Profile(
            uid = "1",
            name = "Profile1",
            focusSessionIds = emptyList(),
            eventIds = emptyList(),
            groupIds = emptyList(),
            friendUids = emptyList()
        )

    val profile2 : Profile =
        Profile(
            uid = "2",
            name = "Profile2",
            focusSessionIds = emptyList(),
            eventIds = emptyList(),
            groupIds = emptyList(),
            friendUids = emptyList()
        )

    val profile3 : Profile =
        Profile(
            uid = "3",
            name = "Profile3",
            focusSessionIds = emptyList(),
            eventIds = emptyList(),
            groupIds = emptyList(),
            friendUids = emptyList()
        )

    val participantProfile : Profile =
        Profile(
            uid = "4",
            name = "Participant",
            focusSessionIds = emptyList(),
            eventIds = emptyList(),
            groupIds = emptyList(),
            friendUids = emptyList()
        )

    val ownerProfile : Profile =
        Profile(
            uid = "0",
            name = "Owner",
            focusSessionIds = emptyList(),
            eventIds = emptyList(),
            groupIds = emptyList(),
            friendUids = emptyList()
        )

    val event : Event =
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
            status = EventStatus.UPCOMING
        )

    //TODO: also check that the strings in UI are updated
    @Test
    fun canEnterEventTitle() {
        val titleString = "This is a title!"
        editEventsViewModel.updateName(titleString)
        assert(!editEventsViewModel.uiState.nameError) { "Entering title should not make an error" }
        assert(editEventsViewModel.uiState.name == titleString) { "Entering title should work" }
    }

    @Test
    fun cannotEnterEmptyEventTitle() {
        val blankString = " "
        editEventsViewModel.updateName(blankString)
        assert(editEventsViewModel.uiState.nameError) { "Blank title should be wrong" }
    }

    @Test
    fun canEnterEventDescription() {
        val descriptionString = "This is the description of this very very cool event"
        editEventsViewModel.updateDescription(descriptionString)
        assert(!editEventsViewModel.uiState.descriptionError) { "Entering description should not make an error" }
        assert(editEventsViewModel.uiState.description == descriptionString) { "Entering description should work" }
    }

    @Test
    fun cannotEnterEmptyEventDescription() {
        val blankString = " "
        editEventsViewModel.updateDescription(blankString)
        assert(editEventsViewModel.uiState.descriptionError) { "Blank description should be wrong" }
    }

    @Test
    fun canEnterEventCreatorName() {
        val creatorNameString = "creator"
        editEventsViewModel.updateCreatorName(creatorNameString)
        assert(!editEventsViewModel.uiState.creatorNameError) { "Entering a creator name should not make an error" }
        assert(editEventsViewModel.uiState.creatorName == creatorNameString) { "Entering a creator name should work" }
    }

    @Test
    fun cannotEnterEmptyEventCreatorName() {
        val blankString = " "
        editEventsViewModel.updateCreatorName(blankString)
        assert(editEventsViewModel.uiState.creatorNameError) { "Blank creator name should be wrong" }
    }

    @Test
    fun canEnterEventLocation() {}

    @Test
    fun canEnterEventDueDate() {
        val dateString = "13/12/2025"
        editEventsViewModel.updateDate(dateString)
        assert(!editEventsViewModel.uiState.dateError) { "\'13/12/2025\' should not make an error" }
        assert(editEventsViewModel.uiState.date == dateString) { "\'13/12/2025\' should work" }
    }

    @Test
    fun cannotEnterInvalidEventDueDate1() {
        val dateString = "This date should not work"
        editEventsViewModel.updateDate(dateString)
        assert(editEventsViewModel.uiState.dateError) { "\'This date should not work\' should be wrong" }
    }

    @Test
    fun cannotEnterInvalidEventDueDate2() {
        val dateString = ""
        editEventsViewModel.updateDate(dateString)
        assert(editEventsViewModel.uiState.dateError) { "Empty date should be wrong" }
    }

    @Test
    fun cannotEnterInvalidEventDueDate3() {
        val dateString = "33/12/2025"
        editEventsViewModel.updateDate(dateString)
        assert(editEventsViewModel.uiState.dateError) { "\'33/12/2025\' should be wrong" }
    }

    @Test
    fun canEnterEventStartTime() {
        val startTimeString = "13:15"
        editEventsViewModel.updateStartTime(startTimeString)
        assert(!editEventsViewModel.uiState.startTimeError) { "\'13:15\' should work" }
        assert(editEventsViewModel.uiState.startTime == startTimeString) { "\'13:15\' should work" }
    }

    @Test
    fun cannotEnterInvalidEventStartTime1() {
        val startTimeString = "This time should not work"
        editEventsViewModel.updateStartTime(startTimeString)
        assert(editEventsViewModel.uiState.startTimeError) { "\'This time should not work\' should be wrong" }
    }

    @Test
    fun cannotEnterInvalidEventStartTime2() {
        val startTimeString = ""
        editEventsViewModel.updateStartTime(startTimeString)
        assert(editEventsViewModel.uiState.startTimeError) { "Empty start time should be wrong" }
    }

    @Test
    fun cannotEnterInvalidEventStartTime3() {
        val startTimeString = "25:77"
        editEventsViewModel.updateStartTime(startTimeString)
        assert(editEventsViewModel.uiState.startTimeError) { "\'25:77\' should be wrong" }
    }

    @Test
    fun canEnterEventEndTime() {
        val endTimeString = "14:15"
        editEventsViewModel.updateEndTime(endTimeString)
        assert(!editEventsViewModel.uiState.endTimeError) { "\'14:15\' should not make an error" }
        assert(editEventsViewModel.uiState.endTime == endTimeString) { "\'14:15\' should work" }
    }

    @Test
    fun cannotEnterInvalidEventEndTime1() {
        val endTimeString = "This time should not work"
        editEventsViewModel.updateEndTime(endTimeString)
        assert(editEventsViewModel.uiState.endTimeError) { "\'This time should not work\' should be wrong" }
    }

    @Test
    fun cannotEnterInvalidEventEndTime2() {
        val endTimeString = ""
        editEventsViewModel.updateEndTime(endTimeString)
        assert(editEventsViewModel.uiState.endTimeError) { "Empty end time should be wrong" }
    }

    @Test
    fun cannotEnterInvalidEventEndTime3() {
        val endTimeString = "25:77"
        editEventsViewModel.updateEndTime(endTimeString)
        assert(editEventsViewModel.uiState.endTimeError) { "\'25:77\' should be wrong" }
    }

    @Test
    fun canEnterEventParticipant() {
        val participantString = "Participant"
        editEventsViewModel.updateParticipant(participantString)
        assert(editEventsViewModel.uiState.participant == participantString) { "\'Participant\' should update the search string" }
    }

    @Test
    fun canFindProfiles1() {
        //Todo add profile 1, 2, 3...
        val participantString = "Profile"
        editEventsViewModel.updateParticipant(participantString)
        editEventsViewModel.searchProfileByString(participantString)
        assert(editEventsViewModel.uiState.participant == participantString) { "\'Profile\' should update the search string" }
        assert(editEventsViewModel.uiState.suggestedProfiles.size == 3) { "\'Profile\' should find 3 profiles" }
        assert(editEventsViewModel.uiState.suggestedProfiles.contains(profile1)) { "\'Profile\' should find Profile1" }
        assert(editEventsViewModel.uiState.suggestedProfiles.contains(profile2)) { "\'Profile\' should find Profile2" }
        assert(editEventsViewModel.uiState.suggestedProfiles.contains(profile3)) { "\'Profile\' should find Profile3" }
    }

    @Test
    fun canFindProfiles2() {

        val participantString = "Not a profile"
        editEventsViewModel.updateParticipant(participantString)
        editEventsViewModel.searchProfileByString(participantString)
        assert(editEventsViewModel.uiState.participant == participantString) { "\'Not a profile\' should update the search string" }
        assert(editEventsViewModel.uiState.suggestedProfiles.isEmpty()) { "\'Not a profile\' should find no profiles" }
    }

    @Test
    fun canAddEventParticipant() {
        editEventsViewModel.updateParticipant(profile1.name)
        editEventsViewModel.addParticipant(profile1.uid)
        assert(editEventsViewModel.uiState.participant == profile1.name) { "\'Profile1\' should update the search string" }
        assert(editEventsViewModel.uiState.participants.contains(profile1)) { "\'Profile1\' should be in participants" }
    }

    @Test
    fun cannotAddParticipatingEventParticipant() {
        editEventsViewModel.updateParticipant(participantProfile.name)
        editEventsViewModel.addParticipant(participantProfile.uid)
        assert(editEventsViewModel.uiState.participant == participantProfile.name) { "\'Participant\' should update the search string" }
        assert(editEventsViewModel.uiState.displayToast) { "Adding \'Participant\' should display toast" }
    }

    @Test
    fun canRemoveEventParticipant() {
        editEventsViewModel.updateParticipant(participantProfile.name)
        editEventsViewModel.deleteParticipant(participantProfile.uid)
        assert(editEventsViewModel.uiState.participant == participantProfile.name) { "\'Participant\' should update the search string" }
        assert(!editEventsViewModel.uiState.participants.contains(participantProfile)) { "Removing \'Participant\' should work" }
    }

    @Test
    fun cannotRemoveUnparticipatingEventParticipant() {
        editEventsViewModel.updateParticipant(profile1.name)
        editEventsViewModel.deleteParticipant(profile1.uid)
        assert(editEventsViewModel.uiState.participant == profile1.name) { "\'Profile1\' should update the search string" }
        assert(editEventsViewModel.uiState.displayToast) { "Removing \'Profile1\' should display toast" }
    }

    @Test
    fun cannotRemoveEventOwner() {
        editEventsViewModel.updateParticipant(ownerProfile.name)
        editEventsViewModel.addParticipant(ownerProfile.uid)
        assert(editEventsViewModel.uiState.participant == ownerProfile.name) { "\'Owner\' should update the search string" }
        assert(editEventsViewModel.uiState.displayToast) { "Removing \'Owner\' should display toast" }
    }

    @Test
    fun canSaveEvent1() {}

    @Test
    fun canSaveEvent2() {}

    @Test
    fun canDeleteEvent() {}
}