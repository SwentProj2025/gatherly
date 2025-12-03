package com.android.gatherly.utils

import com.android.gatherly.model.profile.ProfileRepositoryFirestore
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before

/**
 * Base class for Firestore-based integration tests using the Firebase Emulator Suite, specifically
 * for the [ProfileRepositoryFirestore].
 *
 * Usage:
 * - Ensures Firebase emulators are running.
 * - Signs in anonymously (required for Firestore security rules).
 * - Clears the current user's profile and related username before/after tests.
 *
 * Start emulators before running: firebase emulators:start
 */
open class FirestoreGatherlyProfileTest {

  protected lateinit var repository: ProfileRepositoryFirestore

  @Before
  open fun setUp() {
    if (!FirebaseEmulator.isRunning) {
      error("Firebase emulator must be running! Use: firebase emulators:start")
    }

    // Sign in anonymously like in ToDo tests
    runTest(timeout = 120.seconds) { FirebaseEmulator.auth.signInAnonymously().await() }

    // Create the repository
    repository = ProfileRepositoryFirestore(FirebaseEmulator.firestore, FirebaseEmulator.storage)

    // Clean up existing test data
    runTest(timeout = 120.seconds) { clearCurrentUserData() }
  }

  @After
  open fun tearDown() {
    runTest(timeout = 120.seconds) {
      clearCurrentUserData()
      clearStorageData()
    }
    FirebaseEmulator.clearFirestoreEmulator()
  }

  /** Deletes the current user's profile and username documents in the emulator Firestore. */
  protected suspend fun clearCurrentUserData() {
    val user = FirebaseEmulator.auth.currentUser ?: return
    val firestore = FirebaseEmulator.firestore

    // Delete the user's profile doc
    firestore.collection("profiles").document(user.uid).delete().await()

    // Delete any username docs linked to this UID
    val usernames = firestore.collection("usernames").whereEqualTo("uid", user.uid).get().await()
    val batch = firestore.batch()
    usernames.documents.forEach { batch.delete(it.reference) }
    batch.commit().await()
  }

  /** Deletes all uploaded test files in the Firebase Storage emulator under profile_pictures/. */
  protected suspend fun clearStorageData() {
    val storage = Firebase.storage
    try {
      val list = storage.reference.child("profile_pictures").listAll().await()
      for (item in list.items) {
        item.delete().await()
      }
    } catch (_: Exception) {
      // Ignore if folder doesn't exist (e.g., no uploads happened)
    }
  }
}
