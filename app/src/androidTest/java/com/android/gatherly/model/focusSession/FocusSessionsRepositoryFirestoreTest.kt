package com.android.gatherly.model.focusSession

import com.android.gatherly.utils.FirestoreFocusSessionsGatherlyTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.fail
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotEquals
import org.junit.Test

// This class contains code adapted from the groups repository tests.

/**
 * Test suite for [FocusSessionsRepositoryFirestore].
 *
 * Tests cover CRUD operations, permission checks (creator-only actions), and edge cases such as
 * null linkedTodoId or duration defaults.
 */
class FocusSessionsRepositoryFirestoreTest : FirestoreFocusSessionsGatherlyTest() {

  /**
   * Verifies that multiple focus sessions can be added and retrieved with getAllFocusSessions().
   */
  @Test
  fun add_and_getAll_works() = runTest {
    repository.addFocusSession(session1)
    repository.addFocusSession(session2)

    val sessions = repository.getAllFocusSessions()
    assertEquals(2, sessions.size)
    assertTrue(sessions.any { it.focusSessionId == "1" })
    assertTrue(sessions.any { it.focusSessionId == "2" })
  }

  /** Verifies that getUserFocusSessions() only returns sessions belonging to the current user. */
  @Test
  fun getUserFocusSessions_returns_only_current_user_sessions() = runTest {
    // user1 adds a session
    repository.addFocusSession(session1)

    // user2 adds another session
    signInWithToken(user2Token)
    repository.addFocusSession(session2)
    val user2Sessions = repository.getUserFocusSessions()

    assertEquals(1, user2Sessions.size)
    assertEquals(session2.focusSessionId, user2Sessions.first().focusSessionId)
  }

  /** Verifies that getFocusSession() returns the correct session. */
  @Test
  fun getFocusSession_returns_correct_session() = runTest {
    repository.addFocusSession(session1)

    val retrieved = repository.getFocusSession(session1.focusSessionId)
    assertEquals(session1.focusSessionId, retrieved.focusSessionId)
    assertEquals(user1Id, retrieved.creatorId)
    assertEquals(session1.duration, retrieved.duration)
    assertEquals(session1.linkedTodoId, retrieved.linkedTodoId)
    assertEquals(session1.focusSessionId, retrieved.focusSessionId)
    assertEquals(session1.startedAt, retrieved.startedAt)
    assertEquals(session1.endedAt, retrieved.endedAt)
  }

  /** Verifies that getFocusSession() throws a NoSuchElementException when session not found. */
  @Test
  fun getFocusSession_throws_when_not_found() = runTest {
    try {
      repository.getFocusSession("non_existing_id")
      fail("Expected NoSuchElementException")
    } catch (_: NoSuchElementException) {
      // Expected
    }
  }

  /** Verifies that addFocusSession correctly stores and assigns creatorId. */
  @Test
  fun addFocusSession_stores_creator_correctly() = runTest {
    val testSession = session1.copy(creatorId = "someone_else")
    repository.addFocusSession(testSession)

    val retrieved = repository.getFocusSession(testSession.focusSessionId)
    assertEquals(user1Id, retrieved.creatorId)
    assertEquals(testSession.duration, retrieved.duration)
    assertEquals(testSession.linkedTodoId, retrieved.linkedTodoId)
    assertEquals(testSession.focusSessionId, retrieved.focusSessionId)
    assertEquals(testSession.startedAt, retrieved.startedAt)
    assertEquals(testSession.endedAt, retrieved.endedAt)
  }

  /** Verifies that a session can be edited by its creator. */
  @Test
  fun updateFocusSession_updates_existing_session() = runTest {
    repository.addFocusSession(session1)

    val updatedFocusSession = session1.copy(linkedTodoId = "updated_todo")
    repository.updateFocusSession(session1.focusSessionId, updatedFocusSession)
    val retrieved = repository.getFocusSession(session1.focusSessionId)

    assertEquals(updatedFocusSession.focusSessionId, retrieved.focusSessionId)
    assertEquals(user1Id, retrieved.creatorId)
    assertEquals(updatedFocusSession.linkedTodoId, retrieved.linkedTodoId)
    assertEquals(updatedFocusSession.duration, retrieved.duration)
    assertEquals(updatedFocusSession.focusSessionId, retrieved.focusSessionId)
    assertEquals(updatedFocusSession.startedAt, retrieved.startedAt)
    assertEquals(updatedFocusSession.endedAt, retrieved.endedAt)
  }

  /** Verifies that only the creator can edit their focus session. */
  @Test
  fun editFocusSessionLinkedTodo_throws_security_exception_when_not_creator() = runTest {
    repository.addFocusSession(session1)

    val updatedFocusSession = session1.copy(linkedTodoId = "updated_todo")
    signInWithToken(user2Token)
    try {
      repository.updateFocusSession(session1.focusSessionId, updatedFocusSession)
      fail("Expected SecurityException")
    } catch (_: SecurityException) {
      // Expected
    }
  }

  /** Verifies that a session can be deleted by its creator. */
  @Test
  fun deleteFocusSession_removes_session() = runTest {
    repository.addFocusSession(session3)
    repository.addFocusSession(session2)
    assertEquals(2, repository.getAllFocusSessions().size)

    repository.deleteFocusSession(session3.focusSessionId)
    val remaining = repository.getAllFocusSessions()

    assertEquals(1, remaining.size)
    assertEquals(session2.focusSessionId, remaining.first().focusSessionId)
  }

  /** Verifies that only the creator can delete their focus session. */
  @Test
  fun deleteFocusSession_throws_security_exception_when_not_creator() = runTest {
    repository.addFocusSession(session1)

    signInWithToken(user2Token)
    try {
      repository.deleteFocusSession(session1.focusSessionId)
      fail("Expected SecurityException")
    } catch (_: SecurityException) {
      // Expected
    }
  }

  /** Verifies that sessions with null linkedTodoId are stored correctly. */
  @Test
  fun addFocusSession_with_null_linkedTodoId_stores_correctly() = runTest {
    val testSession = session3.copy(linkedTodoId = null)
    repository.addFocusSession(testSession)
    val retrieved = repository.getFocusSession(testSession.focusSessionId)

    assertNull(retrieved.linkedTodoId)
    assertEquals(user1Id, retrieved.creatorId)
  }

  /** Verifies that getNewId() returns unique, non-empty IDs. */
  @Test
  fun getNewId_returns_unique_values() {
    val id1 = repository.getNewId()
    val id2 = repository.getNewId()
    assertNotEquals(id2, id1)
    assertTrue(id2.isNotEmpty())
    assertTrue(id1.isNotEmpty())
  }
}
