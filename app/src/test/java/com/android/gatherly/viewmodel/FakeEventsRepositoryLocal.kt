package com.android.gatherly.viewmodel

import com.android.gatherly.model.event.Event
import com.android.gatherly.model.event.EventsRepository
import java.lang.Exception

class FakeEventsRepositoryLocal : EventsRepository {
    private var events = mutableListOf<Event>()
    private var counter = 0

    override fun getNewId(): String {
        return (counter++).toString()
    }

    override suspend fun getAllEvents(): List<Event> {
        return events.toList()
    }

    override suspend fun getEvent(eventId: String): Event {
        return events.firstOrNull { it.id == eventId } ?: throw Exception("Event with ID $eventId not found")
    }

    override suspend fun addEvent(event: Event) {
        events.add(event)
    }

    override suspend fun editEvent(eventId: String, newValue: Event) {
        events.indexOfFirst { it.id == eventId }.let { index ->
            if (index != -1) {
                events[index] = newValue
            } else {
                throw Exception("Event with ID $eventId not found")
            }
        }
    }

    override suspend fun deleteEvent(eventId: String) {
        events.indexOfFirst { it.id == eventId }.let { index ->
            if (index != -1) {
                events.removeAt(index)
            } else {
                throw Exception("Event with ID $eventId not found")
            }
        }
    }

    override suspend fun addParticipant(eventId: String, userId: String) {
        events.indexOfFirst { it.id == eventId }.let { index ->
            if (index != -1) {
                val event = events[index]
                if (!event.participants.contains(userId)) {
                    val updatedEvent = event.copy(participants = event.participants + userId)
                    events[index] = updatedEvent
                }
            } else {
                throw Exception("Event with ID $eventId not found")
            }
        }
    }

    override suspend fun removeParticipant(eventId: String, userId: String) {
        events.indexOfFirst {it.id == eventId }.let { index ->
            if (index != -1) {
                val event = events[index]
                if (event.participants.contains(userId)) {
                    val updatedEvent = event.copy(participants = event.participants - userId)
                    events[index] = updatedEvent
                }
            } else {
                throw Exception("Event with ID $eventId not found")
            }
        }
    }
}