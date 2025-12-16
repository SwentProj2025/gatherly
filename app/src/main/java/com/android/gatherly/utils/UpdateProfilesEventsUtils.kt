package com.android.gatherly.utils

import com.android.gatherly.model.badge.BadgeType
import com.android.gatherly.model.event.Event
import com.android.gatherly.model.event.EventsRepository
import com.android.gatherly.model.notification.Notification
import com.android.gatherly.model.notification.NotificationType
import com.android.gatherly.model.notification.NotificationsRepository
import com.android.gatherly.model.points.PointsRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

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
 * Also creates an [NotificationType.EVENT_PARTICIPATION] notification for the participant, so the
 * receiver can later increment their [BadgeType.EVENTS_PARTICIPATED] badge when syncing
 * notifications.
 *
 * @param eventsRepository : EventsRepository
 * @param profileRepository : ProfileRepository
 * @param eventId : Event we want to create
 * @param userId : the ID of the user we want to unregister
 * @param notificationsRepository : NotificationsRepository used to add the notification
 * @param senderId : optional initiator of the action (stored as Notification.senderId)
 */
suspend fun userParticipate(
    eventsRepository: EventsRepository,
    profileRepository: ProfileRepository,
    eventId: String,
    userId: String,
    notificationsRepository: NotificationsRepository,
    senderId: String? = null
) {
  eventsRepository.addParticipant(eventId, userId)
  profileRepository.participateEvent(eventId, userId)

  notifyEventParticipation(
      notificationsRepository = notificationsRepository,
      eventId = eventId,
      senderId = senderId,
      recipientIds = listOf(userId),
      excludeRecipientId = null)
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
 * Also creates an [NotificationType.EVENT_PARTICIPATION] notification for each participant
 * (excluding the creator by default), so each receiver can later increment their
 * [BadgeType.EVENTS_PARTICIPATED] badge when syncing notifications.
 *
 * @param eventsRepository : EventsRepository
 * @param profileRepository : ProfileRepository
 * @param event : Event we want to create
 * @param creatorId : the ID of the profile of the creator of this event
 * @param participants : List of the participants ID to unregister from this event
 * @param notificationsRepository : NotificationsRepository used to add the notifications
 */
suspend fun createEvent(
    eventsRepository: EventsRepository,
    profileRepository: ProfileRepository,
    pointsRepository: PointsRepository,
    event: Event,
    creatorId: String,
    participants: List<String>,
    notificationsRepository: NotificationsRepository
) {
  eventsRepository.addEvent(event)
  profileRepository.createEvent(event.id, creatorId)
  profileRepository.allParticipateEvent(event.id, participants)

  notifyEventParticipation(
      notificationsRepository = notificationsRepository,
      eventId = event.id,
      senderId = creatorId,
      recipientIds = participants,
      excludeRecipientId = creatorId)

  incrementBadgeCheckPoints(
      profileRepository, pointsRepository, creatorId, BadgeType.EVENTS_CREATED)
  if (participants.contains(creatorId)) {
    incrementBadgeCheckPoints(
        profileRepository, pointsRepository, creatorId, BadgeType.EVENTS_PARTICIPATED)
  }
}

/**
 * Function util: that will cancel the event:
 * * from eventsRepository pov : delete this event
 * * from profileRepository pov : unregister all the participants and delete the event from the
 *   ownerEvents list of the creator
 *
 * No notification is created here (cancellation is currently silent).
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

/**
 * Helper: creates EVENT_PARTICIPATION notifications for the provided recipients.
 * - [Notification.relatedEntityId] is set to [eventId] (used later during notification sync).
 * - [excludeRecipientId] can be used to avoid notifying the creator about their own event.
 */
private suspend fun notifyEventParticipation(
    notificationsRepository: NotificationsRepository,
    eventId: String,
    senderId: String?,
    recipientIds: Collection<String>,
    excludeRecipientId: String? = null,
) = coroutineScope {
  recipientIds
      .asSequence()
      .filter { it.isNotBlank() }
      .distinct()
      .filter { excludeRecipientId == null || it != excludeRecipientId }
      .forEach { recipientId ->
        launch {
          val notification =
              Notification(
                  id = notificationsRepository.getNewId(),
                  type = NotificationType.EVENT_PARTICIPATION,
                  emissionTime = Timestamp.now(),
                  senderId = senderId,
                  relatedEntityId = eventId,
                  recipientId = recipientId,
                  wasRead = false)
          notificationsRepository.addNotification(notification)
        }
      }
}
