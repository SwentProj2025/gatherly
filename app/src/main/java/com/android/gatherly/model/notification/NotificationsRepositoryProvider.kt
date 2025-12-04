package com.android.gatherly.model.notification

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object NotificationsRepositoryProvider {
  private val _repository: NotificationsRepository by lazy {
    NotificationsRepositoryFirestore(Firebase.firestore)
  }

  var repository: NotificationsRepository = _repository
}
