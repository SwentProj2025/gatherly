package com.android.gatherly.model.focusSession

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

// Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the
// SwEnt staff.

/**
 * Provides a single instance of [FocusSessionsRepository] for the app.
 *
 * This object lazily initializes the repository with [FocusSessionsRepositoryFirestore] using
 * Firebase Firestore. The [repository] property is mutable to allow swapping implementations for
 * testing purposes.
 */
object FocusSessionsRepositoryProvider {
  private val _repository: FocusSessionsRepository by lazy {
    FocusSessionsRepositoryFirestore(Firebase.firestore)
  }

  /** The current instance of [FocusSessionsRepository]. Mutable for testing. */
  var repository: FocusSessionsRepository = _repository
}
