package com.android.gatherly.model.todo

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

// Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the
// SwEnt staff.

/**
 * Provides a single instance of the repository in the app. `repository` is mutable for testing
 * purposes.
 */
object ToDosRepositoryProvider {
  private val _repository: ToDosRepository by lazy { ToDosRepositoryFirestore(Firebase.firestore) }

  var repository: ToDosRepository = _repository
}
