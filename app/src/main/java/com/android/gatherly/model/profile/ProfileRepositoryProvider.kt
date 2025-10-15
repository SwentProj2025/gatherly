package com.android.gatherly.model.profile

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * Provides a single instance of the repository in the app. `repository` is mutable for testing
 * purposes.
 */
object ProfileRepositoryProvider {

  private val _repository: ProfileRepository by lazy {
    ProfileRepositoryFirestore(Firebase.firestore)
  }

  var repository: ProfileRepository = _repository
}
