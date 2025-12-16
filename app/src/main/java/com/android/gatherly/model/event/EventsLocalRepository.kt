package com.android.gatherly.model.event

import com.android.gatherly.utils.updateEventStatus

/**
 * Local in-memory implementation of [EventsRepository].
 *
 * This repository stores events in a mutable list and is intended for testing or temporary storage.
 * Data is not persisted and will be lost when the instance is destroyed.
 *
 * @property counter Internal counter used to generate unique sequential IDs for new events.
 * @property events Mutable list that holds all events in memory.
 * @throws IllegalArgumentException if attempting to edit an event that does not exist.
 */
class EventsLocalRepository : EventsRepository {

  var counter = 0
  private val events: MutableList<Event> = mutableListOf()

  override fun getNewId(): String {
    return (counter++).toString()
  }

  override suspend fun getAllEvents(): List<Event> {
    val findEvents = events.toList()
    return findEvents.map { updateEventStatus(it) }
  }

  override suspend fun getEvent(eventId: String): Event {
    val findEvent = events.find { it.id == eventId }!!
    return updateEventStatus(findEvent)
  }

  override suspend fun addEvent(event: Event) {
    val updatedEvent = updateEventStatus(event)
    events += updatedEvent
  }

  override suspend fun editEvent(eventId: String, newValue: Event) {
    val index = events.indexOf(getEvent(eventId))
    val updatedEvent = updateEventStatus(newValue)
    require(index != -1) { "EventsLocalRepository.editEvent: Event with ID $eventId not found" }
    events[index] = updatedEvent
  }

  override suspend fun deleteEvent(eventId: String) {
    events.removeIf { it.id == eventId }
  }

  override suspend fun addParticipant(eventId: String, userId: String) {
    val event = getEvent(eventId)
    editEvent(eventId, event.copy(participants = event.participants + userId))
  }

  override suspend fun removeParticipant(eventId: String, userId: String) {
    val event = getEvent(eventId)
    editEvent(eventId, event.copy(participants = event.participants.filter { it != userId }))
  }
}
