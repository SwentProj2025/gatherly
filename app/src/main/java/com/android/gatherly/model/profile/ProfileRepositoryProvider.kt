package com.android.gatherly.model.profile
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProfileRepositoryProvider {

    private val _repository: ProfileRepository by lazy {
        ProfileRepositoryFirestore(Firebase.firestore)
    }

    var repository: ProfileRepository = _repository
}