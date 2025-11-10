package com.android.gatherly.model.profile

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

/**
 * Provides a single instance of the repository in the app. `repository` is mutable for testing
 * purposes.
 */
object ProfileRepositoryProvider {

  private val _repository: ProfileRepository by lazy {
    ProfileRepositoryFirestore(Firebase.firestore, Firebase.storage)
  }

  var repository: ProfileRepository = _repository
}
