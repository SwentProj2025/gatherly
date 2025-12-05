package com.android.gatherly.model.profile

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

/**
 * Manages the online/offline status of the currently signed-in user.
 *
 * @property auth FirebaseAuth instance used to get the current user. Defaults to [Firebase.auth].
 * @property repo ProfileRepository used to update the user's status in the database.
 */
class UserStatusManager(
    private val auth: FirebaseAuth = Firebase.auth,
    private val repo: ProfileRepository = ProfileRepositoryProvider.repository
) {

  /**
   * Updates the current user's status in the repository.
   *
   * Does nothing if no user is currently signed in.
   *
   * @param status The [ProfileStatus] to set for the user.
   */
  suspend fun setStatus(status: ProfileStatus) {
    val uid = auth.currentUser?.uid ?: return
    repo.updateStatus(uid, status)
  }
}
