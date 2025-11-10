package com.android.gatherly.utils

import com.android.gatherly.model.focusSession.FocusSession
import com.android.gatherly.model.focusSession.FocusSessionsRepository
import com.android.gatherly.model.focusSession.FocusSessionsRepositoryFirestore
import com.google.firebase.Timestamp
import com.google.firebase.auth.GoogleAuthProvider
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before

// This class contains code adapted from the groups repository tests.

/**
 * Base class for Firestore-based Focus Session tests using Firebase Emulator Suite.
 *
 * Start emulators with: firebase emulators:start
 */
open class FirestoreFocusSessionsGatherlyTest {

  /**
   * The FocusSessionsRepository instance backed by the Firestore emulator. Initialized during
   * setUp() and used for testing repository operations.
   */
  protected lateinit var repository: FocusSessionsRepository

  // Create fake tokens for different users
  protected val user1Token = FakeJwtGenerator.createFakeGoogleIdToken("User1", "user1@test.com")
  protected val user2Token = FakeJwtGenerator.createFakeGoogleIdToken("User2", "user2@test.com")

  /**
   * The unique identifiers for the test users in the Firebase Auth emulator. Set dynamically during
   * setUp() after authentication.
   */
  protected lateinit var user1Id: String

  protected val session1 =
      FocusSession(
          focusSessionId = "1",
          creatorId = "", // Will be set dynamically in tests
          linkedTodoId = "todo1",
          duration = 25.minutes,
          startedAt = Timestamp.now(),
          endedAt = Timestamp.now())

  protected val session2 =
      session1.copy(focusSessionId = "2", linkedTodoId = "todo2", duration = 50.minutes)

  protected val session3 =
      session1.copy(focusSessionId = "3", linkedTodoId = null, duration = 10.minutes)

  /**
   * Sets up the test environment before each test.
   *
   * This method performs the following steps:
   * 1. Verifies the Firebase emulator is running
   * 2. Clears all existing authentication and Firestore data
   * 3. Seeds test users in the Auth emulator
   * 4. Signs in as user1 by default and captures other user IDs
   * 5. Initializes the FocusSessionsRepository with the emulator Firestore instance
   * 6. Clears any existing focus sessions from Firestore
   *
   * @throws IllegalStateException if the Firebase emulator is not running
   */
  @Before
  open fun setUp() = runTest {
    if (!FirebaseEmulator.isRunning) {
      error("Firebase emulator must be running! Use: firebase emulators:start")
    }

    // Seed users in emulator
    FirebaseEmulator.createGoogleUser(user1Token)
    FirebaseEmulator.createGoogleUser(user2Token)

    // Sign in as user1 by default
    signInWithToken(user1Token)
    user1Id = FirebaseEmulator.auth.currentUser!!.uid

    // Switch back to user1
    signInWithToken(user1Token)

    repository = FocusSessionsRepositoryFirestore(FirebaseEmulator.firestore)
  }

  /**
   * Cleans up the test environment after each test.
   *
   * This method:
   * 1. Removes all focus sessions from Firestore
   * 2. Clears all users from the Auth emulator
   * 3. Clears all data from the Firestore emulator
   *
   * Logs are included for debugging purposes to track user cleanup.
   */
  @After
  open fun tearDown() = runTest {
    FirebaseEmulator.clearAuthEmulator()
    FirebaseEmulator.clearFirestoreEmulator()
  }

  /**
   * Sign in with the given fake JWT token.
   *
   * @param token The fake JWT token created with FakeJwtGenerator
   */
  protected suspend fun signInWithToken(token: String) {
    val credential = GoogleAuthProvider.getCredential(token, null)
    FirebaseEmulator.auth.signInWithCredential(credential).await()
  }
}
