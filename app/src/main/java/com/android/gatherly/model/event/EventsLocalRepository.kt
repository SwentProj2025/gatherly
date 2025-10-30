package com.android.gatherly.model.event

/**
 * Local in-memory implementation of [EventsRepository].
 *
 * This repository stores events in a mutable list and is intended for testing or temporary storage.
 * Data is not persisted and will be lost when the instance is destroyed.
 *
 * @property counter Internal counter used to generate unique sequential IDs for new events.
 * @property events Mutable list that holds all events in memory.
 */
class EventsLocalRepository : EventsRepository {

  /*  private val todos =
  mutableListOf(
      ToDo(
          uid = "1",
          name = "Buy groceries",
          description = "Milk, Bread, Eggs, Butter",
          assigneeName = "Alice",
          dueDate = Timestamp.now(),
          dueTime = null,
          location = Location(46.5238, 6.5627, "Bassenges"),
          status = ToDoStatus.ONGOING,
          ownerId = "user1"),
      ToDo(
          uid = "2",
          name = "Finish swent",
          description = "FInish map ui",
          assigneeName = "Colombe",
          dueDate = Timestamp.now(),
          dueTime = null,
          location = Location(46.5197, 6.5663, "EPFL"),
          status = ToDoStatus.ONGOING,
          ownerId = "user1"))*/

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
