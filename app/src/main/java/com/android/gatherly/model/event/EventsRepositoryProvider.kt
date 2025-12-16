package com.android.gatherly.model.event

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

// Portions of the code in this file are inspired by the Bootcamp solution provided by the
// SwEnt staff.

/**
 * Provides a single instance of the [EventsRepository] in the app. `repository` is mutable for
 * testing purposes.
 */
object EventsRepositoryProvider {
  private val _repository: EventsRepository by lazy {
    EventsRepositoryFirestore(Firebase.firestore)
  }

  var repository: EventsRepository = _repository
}
