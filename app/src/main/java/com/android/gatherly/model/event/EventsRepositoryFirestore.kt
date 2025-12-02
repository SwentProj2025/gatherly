package com.android.gatherly.model.event

import com.android.gatherly.model.map.Location
import com.android.gatherly.utils.updateEventStatus
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

// This class contains code adapted by an LLM (GitHub Copilot, Claude.ai) from the CS-311 bootcamp.

class EventsRepositoryFirestore(private val db: FirebaseFirestore) : EventsRepository {

  /**
   * Firestore collection for events. Note that this is a global collection, not scoped to a user
   * (unlike todos). User-specific access control is enforced in the methods below.
   */
  private val collection = db.collection("events") // Global, not user-scoped

  /**
   * Returns the user ID of the currently signed-in user, or throws an exception if no user is
   * signed in.
   */
  private fun currentUserId(): String {
    return Firebase.auth.currentUser?.uid ?: throw IllegalStateException("No signed in user")
  }

  /** Returns a new unique ID for an event. */
  override fun getNewId(): String {
    return collection.document().id
  }

  /**
   * Returns a list of all events in the system. Note that this does not filter events by user - it
   * returns all events. User-specific access control is enforced in the methods below.
   */
  override suspend fun getAllEvents(): List<Event> {
    val snap = collection.get().await() // No filtering - get ALL events
    return snap.documents.mapNotNull { doc -> snapshotToEvent(doc) }
  }

  /** Returns the event with the given ID, or throws NoSuchElementException if not found. */
  override suspend fun getEvent(eventId: String): Event {
    val doc = collection.document(eventId).get().await()
    return snapshotToEvent(doc) ?: throw NoSuchElementException("Event with id=$eventId not found")
  }

  /** Adds the given event to Firestore. The event's creatorId is set to the current user ID. */
  override suspend fun addEvent(event: Event) {
    // Should probably enforce that creatorId matches currentUserId
    val ownedEvent = event.copy(creatorId = currentUserId())
    collection.document(ownedEvent.id).set(eventToMap(ownedEvent)).await()
  }

  /** Edits the event with the given ID to have the new values. */
  override suspend fun editEvent(eventId: String, newValue: Event) {
    val existing = getEvent(eventId)
    if (existing.creatorId != currentUserId()) {
      throw SecurityException("Only the creator can edit this event")
    }
    val updated = newValue.copy(creatorId = existing.creatorId)
    collection.document(eventId).set(eventToMap(updated)).await()
  }

  /** Deletes the event with the given ID. */
  override suspend fun deleteEvent(eventId: String) {
    val existing = getEvent(eventId)
    if (existing.creatorId != currentUserId()) {
      throw SecurityException("Only the creator can delete this event")
    }
    collection.document(eventId).delete().await()
  }

  /** Adds the given user ID to the participants list of the event with the given ID. */
  override suspend fun addParticipant(eventId: String, userId: String) {
    collection.document(eventId).update("participants", FieldValue.arrayUnion(userId)).await()
  }

  /** Removes the given user ID from the participants list of the event with the given ID. */
  override suspend fun removeParticipant(eventId: String, userId: String) {
    collection.document(eventId).update("participants", FieldValue.arrayRemove(userId)).await()
  }

  /**
   * Converts a Firestore [DocumentSnapshot] into an [Event] object.
   *
   * @param doc The Firestore document representing an [Event].
   * @return The constructed [EVent], or `null` if required fields are missing.
   */
  private fun snapshotToEvent(doc: DocumentSnapshot): Event? {
    val id = doc.getString("id") ?: return null
    val title = doc.getString("title") ?: return null
    val description = doc.getString("description") ?: return null
    val creatorName = doc.getString("creatorName") ?: return null
    val date = doc.getTimestamp("date") ?: return null
    val startTime = doc.getTimestamp("startTime") ?: return null
    val endTime = doc.getTimestamp("endTime") ?: return null
    val locationMap = doc.get("location") as? Map<*, *>
    val location =
        locationMap?.let { locMap ->
          val lat = locMap["latitude"] as? Double
          val lng = locMap["longitude"] as? Double
          val locName = locMap["name"] as? String
          if (lat != null && lng != null && locName != null) {
            Location(lat, lng, locName)
          } else {
            null
          }
        }
    val creatorId = doc.getString("creatorId") ?: return null
    val statusStr = doc.getString("status") ?: return null
    val participants =
        (doc.get("participants") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    val status = EventStatus.valueOf(statusStr)
      val stateStr = doc.getString("state") ?: return null
      val state = EventState.valueOf(stateStr)

    val event =
        Event(
            id,
            title,
            description,
            creatorName,
            location,
            date,
            startTime,
            endTime,
            creatorId,
            participants,
            status,
            state)

    return updateEventStatus(event)
  }

  /**
   * Converts an [Event] into a Firestore-compatible map.
   *
   * Used when adding or editing an event in Firestore.
   *
   * @param event The [Event] to serialize.
   * @return A map of field names to values compatible with Firestore.
   */
  private fun eventToMap(event: Event): Map<String, Any?> {
    return mapOf(
        "id" to event.id,
        "title" to event.title,
        "description" to event.description,
        "creatorName" to event.creatorName,
        "date" to event.date,
        "startTime" to event.startTime,
        "endTime" to event.endTime,
        "location" to
            event.location?.let { loc ->
              mapOf("latitude" to loc.latitude, "longitude" to loc.longitude, "name" to loc.name)
            },
        "creatorId" to event.creatorId,
        "participants" to event.participants,
        "status" to event.status.name,
        "state" to event.state)
  }
}
