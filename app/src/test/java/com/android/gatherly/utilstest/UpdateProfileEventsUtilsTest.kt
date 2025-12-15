package com.android.gatherly.utilstest

import com.android.gatherly.model.badge.BadgeType
import com.android.gatherly.model.event.Event
import com.android.gatherly.model.event.EventStatus
import com.android.gatherly.model.event.EventsRepository
import com.android.gatherly.model.points.PointsLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.runUnconfinedTest
import com.android.gatherly.utils.cancelEvent
import com.android.gatherly.utils.createEvent
import com.android.gatherly.utils.userUnregister
import com.google.firebase.Timestamp
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.time.Duration.Companion.seconds
import org.junit.Test

/** Unit tests for event-related profile update functions. */
class UpdateProfileEventsUtilsTest {
  private val eventsRepository = mockk<EventsRepository>(relaxed = true)
  private val profileRepository = mockk<ProfileRepository>(relaxed = true)

  private val creatorId = "creator123"
  private val participantIds = listOf("userA", "userB")
  private val testTimeout = 120.seconds

  private fun buildEvent(
      id: String = "event123",
      creator: String = creatorId,
      participants: List<String> = participantIds
  ): Event {
    val now = Timestamp.now()
    return Event(
        id = id,
        title = "Test event",
        description = "Test description",
        creatorName = "Creator Name",
        location = null, // or Location(0.0, 0.0, "Somewhere") if you prefer
        date = now,
        startTime = now,
        endTime = now,
        creatorId = creator,
        participants = participants,
        status = EventStatus.UPCOMING)
  }

  /** Test creating an event updates repositories and increments badge counters. */
  @Test
  fun createEvent_updateBadges_callsRepositoriesAndIncrementsCounters() =
      runUnconfinedTest(testTimeout) {
        val event = buildEvent()

        val pointsRepository = PointsLocalRepository()

        createEvent(
            eventsRepository = eventsRepository,
            profileRepository = profileRepository,
            pointsRepository = pointsRepository,
            event = event,
            creatorId = creatorId,
            participants = participantIds)

        // Events repository
        coVerify { eventsRepository.addEvent(event) }

        // Profile relationship updates
        coVerify { profileRepository.createEvent(event.id, creatorId) }
        coVerify { profileRepository.allParticipateEvent(event.id, participantIds) }

        // Counters
        coVerify { profileRepository.incrementBadge(creatorId, BadgeType.EVENTS_CREATED) }
      }

  /** Test unregistering a user updates repositories but does not change badge counters. */
  @Test
  fun userUnregister_updateBadges_updatesEventProfileButDoesNotChangeCounters() =
      runUnconfinedTest(testTimeout) {
        val eventId = "eventDEF"
        val userId = "userXYZ"

        userUnregister(
            eventsRepository = eventsRepository,
            profileRepository = profileRepository,
            eventId = eventId,
            userId = userId)

        // Event + profile links
        coVerify { eventsRepository.removeParticipant(eventId, userId) }
        coVerify { profileRepository.unregisterEvent(eventId, userId) }
      }

  /** Test cancelling an event updates profiles but does not change badge counters. */
  @Test
  fun cancelEvent_updateBadges_updatesProfilesButDoesNotChangeCounters() =
      runUnconfinedTest(testTimeout) {
        val eventId = "eventToCancel"
        val participantsToRemove = listOf("user1", "user2", "user3")

        cancelEvent(
            eventsRepository = eventsRepository,
            profileRepository = profileRepository,
            eventId = eventId,
            creatorId = creatorId,
            participants = participantsToRemove)

        // Event + profiles
        coVerify { eventsRepository.deleteEvent(eventId) }
        coVerify { profileRepository.deleteEvent(eventId, creatorId) }
        coVerify { profileRepository.allUnregisterEvent(eventId, participantsToRemove) }
      }
}
