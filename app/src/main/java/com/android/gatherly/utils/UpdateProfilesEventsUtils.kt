package com.android.gatherly.utils

import com.android.gatherly.model.event.Event
import com.android.gatherly.model.event.EventsRepository
import com.android.gatherly.model.profile.ProfileRepository

/**
 * Utility functions that will help us to synchronize the events functions to the profiles impacted
 * by this functions.
 */

/**
 * Function util: register the user to this event and updates creator/participants counters +
 * badges: from eventsRepository pov : Add the userId to the event participants list from
 * profileRepository pov : Add the event to the profile participantEvents list of the user
 * incrementParticipatedEvent(uid): increments "events participated" once for each participant
 *
 * @param eventsRepository : EventsRepository
 * @param profileRepository : ProfileRepository
 * @param eventId : Event we want to create
 * @param userId : the ID of the user we want to unregister
 */
suspend fun userParticipate(
    eventsRepository: EventsRepository,
    profileRepository: ProfileRepository,
    eventId: String,
    userId: String
) {
  eventsRepository.addParticipant(eventId, userId)
  profileRepository.participateEvent(eventId, userId)
}

/**
 * Function util: unregister the user from this event:
 * * from eventsRepository pov : Delete the userId from the event participants list
 * * from profileRepository pov : Delete the event from the profile participantEvents list of the
 *   user
 *
 * @param eventsRepository : EventsRepository
 * @param profileRepository : ProfileRepository
 * @param eventId : Event we want to create
 * @param userId : the ID of the user we want to unregister
 */
suspend fun userUnregister(
    eventsRepository: EventsRepository,
    profileRepository: ProfileRepository,
    eventId: String,
    userId: String
) {
  eventsRepository.removeParticipant(eventId, userId)
  profileRepository.unregisterEvent(eventId, userId)
}

/**
 * Function util: create a new event and updates creator/participants counters + badges: from
 * eventsRepository pov : create this event from profileRepository pov : register all the
 * participants chosen by the creator and create the event into the ownerEvents list of the creator
 * incrementCreatedEvent(creatorId): increments "events created" once
 * incrementParticipatedEvent(uid): increments "events participated" once for each participant
 *
 * @param eventsRepository : EventsRepository
 * @param profileRepository : ProfileRepository
 * @param event : Event we want to create
 * @param creatorId : the ID of the profile of the creator of this event
 * @param participants : List of the participants ID to unregister from this event
 */
suspend fun createEvent(
    eventsRepository: EventsRepository,
    profileRepository: ProfileRepository,
    event: Event,
    creatorId: String,
    participants: List<String>
) {
  eventsRepository.addEvent(event)
  profileRepository.createEvent(event.id, creatorId)
  profileRepository.allParticipateEvent(event.id, participants)

  profileRepository.incrementCreatedEvent(creatorId)

  participants.forEach { uid -> profileRepository.incrementParticipatedEvent(uid) }
}

/**
 * Function util: that will cancel the event:
 * * from eventsRepository pov : delete this event
 * * from profileRepository pov : unregister all the participants and delete the event from the
 *   ownerEvents list of the creator
 *
 * @param eventsRepository : EventsRepository
 * @param profileRepository : ProfileRepository
 * @param eventId : the ID of the event we want to cancel
 * @param creatorId : the ID of the profile of the creator of this event
 * @param participants : List of the participants ID to unregister from this event
 */
suspend fun cancelEvent(
    eventsRepository: EventsRepository,
    profileRepository: ProfileRepository,
    eventId: String,
    creatorId: String,
    participants: List<String>
) {
  eventsRepository.deleteEvent(eventId)
  profileRepository.deleteEvent(eventId, creatorId)
  profileRepository.allUnregisterEvent(eventId, participants)
}
