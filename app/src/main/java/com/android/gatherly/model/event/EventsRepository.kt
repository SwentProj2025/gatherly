package com.android.gatherly.model.event

// This file was inspired from the CS-311 bootcamp, and was adapted by an LLM (GitHub Copilot).

/**
 * Repository interface that defines all operations for managing [Event] items.
 *
 * This abstraction allows different data sources (e.g., Firestore, local DB, or fake data)
 */
interface EventsRepository {

  /** Generates and returns a new unique identifier for an [Event]. */
  fun getNewId(): String

  /**
   * Retrieves all [Event]s from the repository.
   *
   * @return A list of all [Event]s.
   */
  suspend fun getAllEvents(): List<Event>

  /**
   * Retrieves a specific [Event] by its unique identifier.
   *
   * @param eventId The unique identifier of the [Event] to retrieve.
   * @return The [Event] with the specified identifier.
   */
  suspend fun getEvent(eventId: String): Event

  /**
   * Adds a new [Event] to the repository.
   *
   * @param event The [Event] to add.
   */
  suspend fun addEvent(event: Event)

  /**
   * Edits an existing [Event] in the repository.
   *
   * @param eventId The unique identifier of the [Event] to edit.
   * @param newValue The new value for the [Event].
   */
  suspend fun editEvent(eventId: String, newValue: Event)

  /**
   * Deletes an [Event] from the repository.
   *
   * @param eventId The unique identifier of the [Event] to delete.
   */
  suspend fun deleteEvent(eventId: String)

  /**
   * Adds a participant to an [Event].
   *
   * @param eventId The identifier of the [Event].
   * @param userId The identifier of the user to add.
   */
  suspend fun addParticipant(eventId: String, userId: String)

  /**
   * Removes a participant from an [Event].
   *
   * @param eventId The identifier of the [Event].
   * @param userId The identifier of the user to remove.
   */
  suspend fun removeParticipant(eventId: String, userId: String)
}
