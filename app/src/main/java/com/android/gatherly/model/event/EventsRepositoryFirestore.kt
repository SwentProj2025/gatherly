package com.android.gatherly.model.event

import com.android.gatherly.model.group.Group
import com.android.gatherly.model.map.Location
import com.android.gatherly.utils.updateEventStatus
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.protobuf.LazyStringArrayList.emptyList
import kotlinx.coroutines.tasks.await

// This class contains code adapted by an LLM (GitHub Copilot, Claude.ai) from the CS-311 bootcamp.

/**
 * Firebase Firestore implementation of [EventsRepository].
 *
 * This implementation enforces security rules requiring the event creator to edit or delete events.
 * Uses Firebase Authentication to identify the current user.
 *
 * @property db The Firestore database instance.
 * @throws SecurityException for operations requiring creator privileges
 * @throws NoSuchElementException when requested events are not found
 * @throws IllegalStateException when no user is currently signed in
 */
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

  override fun getNewId(): String {
    return collection.document().id
  }

  override suspend fun getAllEvents(): List<Event> {
    val snap = collection.get().await() // No filtering - get ALL events
    return snap.documents.mapNotNull { doc -> snapshotToEvent(doc) }
  }

  override suspend fun getEvent(eventId: String): Event {
    val doc = collection.document(eventId).get().await()
    return snapshotToEvent(doc) ?: throw NoSuchElementException("Event with id=$eventId not found")
  }

  override suspend fun addEvent(event: Event) {
    val ownedEvent = event.copy(creatorId = currentUserId())
    collection.document(ownedEvent.id).set(eventToMap(ownedEvent)).await()
  }

  override suspend fun editEvent(eventId: String, newValue: Event) {
    val existing = getEvent(eventId)
    if (existing.creatorId != currentUserId()) {
      throw SecurityException("Only the creator can edit this event")
    }
    val updated = newValue.copy(creatorId = existing.creatorId)
    collection.document(eventId).set(eventToMap(updated)).await()
  }

  override suspend fun deleteEvent(eventId: String) {
    val existing = getEvent(eventId)
    if (existing.creatorId != currentUserId()) {
      throw SecurityException("Only the creator can delete this event")
    }
    collection.document(eventId).delete().await()
  }

  override suspend fun addParticipant(eventId: String, userId: String) {
    collection.document(eventId).update("participants", FieldValue.arrayUnion(userId)).await()
  }

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
    val groupsRaw = doc.get("groups") as? List<*> ?: emptyList()

    val groups: List<Group> =
        groupsRaw.mapNotNull { groupData ->
          if (groupData is Map<*, *>) {
            Group(
                gid = groupData["gid"] as? String ?: "",
                creatorId = groupData["creatorId"] as? String ?: "",
                name = groupData["name"] as? String ?: "",
                description = groupData["description"] as? String,
                memberIds =
                    (groupData["memberIds"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                adminIds =
                    (groupData["adminIds"] as? List<*>)?.filterIsInstance<String>() ?: emptyList())
          } else {
            null
          }
        }

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
            state,
            groups)

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
        "state" to event.state,
        "groups" to event.groups)
  }
}
