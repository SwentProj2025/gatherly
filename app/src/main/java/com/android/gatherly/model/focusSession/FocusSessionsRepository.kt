package com.android.gatherly.model.focusSession

/** Repository interface for managing focus sessions. */
interface FocusSessionsRepository {

  /**
   * Returns a new unique ID for a focus session.
   *
   * @return A unique identifier that can be used when creating a new [FocusSession].
   */
  fun getNewId(): String

  /**
   * Returns a list of all focus sessions in the system.
   *
   * Note: This returns all focus sessions regardless of the user who created them. Use
   * [getUserFocusSessions] to get the focus sessions of a particular user.
   *
   * @return A list of all [FocusSession]s.
   */
  suspend fun getAllFocusSessions(): List<FocusSession>

  /**
   * Returns a list of focus sessions that the current user has created.
   *
   * @return A list of [FocusSession]s created by the user with the current user ID.
   */
  suspend fun getUserFocusSessions(): List<FocusSession>

  /**
   * Returns the [FocusSession] with the given ID.
   *
   * @param focusSessionId The unique identifier of the Focus Session.
   * @return The focus session with the specified ID.
   * @throws NoSuchElementException if no focus session with the given ID exists.
   */
  suspend fun getFocusSession(focusSessionId: String): FocusSession

  /**
   * Adds a new [FocusSession] to the repository.
   *
   * The current user is automatically set as the creator of the focus session
   *
   * @param focusSession The focus session to add.
   */
  suspend fun addFocusSession(focusSession: FocusSession)

  /**
   * Edits the linked todo of a [FocusSession] with the given ID.
   *
   * @param focusSessionId The unique identifier of the focus session to edit.
   * @param newLinkedTodoId The if of the new linked todo.
   * @throws SecurityException if the current user is not the creator of the focus session.
   */
  suspend fun editFocusSessionLinkedTodo(focusSessionId: String, newLinkedTodoId: String)

  /**
   * Deletes the [FocusSession] with the given ID.
   *
   * @param focusSessionId The unique identifier of the focus session to delete.
   * @throws SecurityException if the current user is not the creator of the focus session.
   */
  suspend fun deleteFocusSession(focusSessionId: String)
}
