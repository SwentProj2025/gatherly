package com.android.gatherly.model.focusSession

// This class contains code adapted from the CS-311 bootcamp.

/**
 * Simplified in-memory local implementation of [FocusSessionsRepository].
 *
 * Used for local testing or offline mode. Only implements the methods actually needed in the
 * current app logic.
 */
class FocusSessionsLocalRepository : FocusSessionsRepository {

  /** Holds the focus sessions in memory. */
  private val focusSessions: MutableList<FocusSession> = mutableListOf()

  /** Counter to generate unique IDs for new focus sessions. */
  private var counter = 0

  override fun getNewId(): String {
    return (counter++).toString()
  }

  override suspend fun getAllFocusSessions(): List<FocusSession> {
    return focusSessions
  }

  override suspend fun getUserFocusSessions(): List<FocusSession> {
    return focusSessions
  }

  override suspend fun getFocusSession(focusSessionId: String): FocusSession {
    return focusSessions.find { it.focusSessionId == focusSessionId }
        ?: throw NoSuchElementException("FocusSessionsLocalRepository: Focus Session not found")
  }

  override suspend fun addFocusSession(focusSession: FocusSession) {
    focusSessions.add(focusSession)
  }

  override suspend fun updateFocusSession(
      focusSessionId: String,
      updatedFocusSession: FocusSession
  ) {
    val index = focusSessions.indexOfFirst { it.focusSessionId == focusSessionId }
    if (index != -1) {
      focusSessions[index] = updatedFocusSession
    } else {
      throw NoSuchElementException("FocusSessionsLocalRepository: Focus Session not found")
    }
  }

  override suspend fun deleteFocusSession(focusSessionId: String) {
    val index = focusSessions.indexOfFirst { it.focusSessionId == focusSessionId }
    if (index != -1) {
      focusSessions.removeAt(index)
    } else {
      throw NoSuchElementException("FocusSessionsLocalRepository: Focus Session not found")
    }
  }
}
