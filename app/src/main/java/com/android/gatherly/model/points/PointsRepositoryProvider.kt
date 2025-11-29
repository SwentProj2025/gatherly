package com.android.gatherly.model.points

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * Provides a single instance of the repository in the app. `repository` is mutable for testing
 * purposes.
 */
object PointsRepositoryProvider {

  private val _repository: PointsRepository by lazy {
    PointsRepositoryFirestore(Firebase.firestore)
  }

  var repository: PointsRepository = _repository
}
