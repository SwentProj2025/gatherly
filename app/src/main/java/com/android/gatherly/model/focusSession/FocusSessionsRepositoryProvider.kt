package com.android.gatherly.model.focusSession

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

// Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the
// SwEnt staff.

/**
 * Provides a single instance of focus sessions the repository in the app. `repository` is mutable
 * for testing purposes.
 */
object FocusSessionsRepositoryProvider {
  private val _repository: FocusSessionsRepository by lazy {
    FocusSessionsRepositoryFirestore(Firebase.firestore)
  }

  var repository: FocusSessionsRepository = _repository
}
