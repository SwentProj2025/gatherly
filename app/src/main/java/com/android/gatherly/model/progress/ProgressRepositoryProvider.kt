package com.android.gatherly.model.progress

import com.android.gatherly.model.profile.ProfileRepositoryFirestore
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage

object ProgressRepositoryProvider {

  private val _repository: ProgressRepository by lazy {
    ProgressRepositoryFirestore(
        Firebase.firestore,
        profileRepository = ProfileRepositoryFirestore(Firebase.firestore, Firebase.storage))
  }

  var repository: ProgressRepository = _repository
}
