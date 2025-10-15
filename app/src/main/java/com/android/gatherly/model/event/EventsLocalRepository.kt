package com.android.gatherly.model.event

class EventsLocalRepository : EventsRepository {


    var counter = 0
    private val events: MutableList<Event> = mutableListOf()

    override fun getNewId(): String {
        return (counter++).toString()
    }

    override suspend fun getAllEvents(): List<Event> {
        return events.toList()
    }

    override suspend fun getEvent(eventId: String): Event {
        return events.find { it.id == eventId }!!
    }

    override suspend fun addEvent(event: Event) {
        events += event
    }

    override suspend fun editEvent(eventId: String, newValue: Event) {
        val index = events.indexOf(getEvent(eventId))
        if (index == -1) {
            throw IllegalArgumentException()
        } else {
            events[index] = newValue
        }
    }

    override suspend fun deleteEvent(eventId: String) {
        events.filter { it.id != eventId }
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