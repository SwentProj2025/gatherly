package com.android.gatherly.model.todoCategory

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

// Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the
// SwEnt staff.

/**
 * Provides a single instance of the repository in the app. `repository` is mutable for testing
 * purposes.
 */
object ToDoCategoryRepositoryProvider {
  private val _repository: ToDoCategoryRepository by lazy {
    ToDoCategoryRepositoryFirestore(Firebase.firestore)
  }

  var repository: ToDoCategoryRepository = _repository
}
