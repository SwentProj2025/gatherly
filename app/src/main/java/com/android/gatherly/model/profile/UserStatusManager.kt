package com.android.gatherly.model.profile

import com.google.firebase.auth.FirebaseAuth

class UserStatusManager(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val repo: ProfileRepository = ProfileRepositoryProvider.repository
) {
  suspend fun setStatus(status: ProfileStatus) {
    val uid = auth.currentUser?.uid ?: return
    repo.updateStatus(uid, status)
  }
}
