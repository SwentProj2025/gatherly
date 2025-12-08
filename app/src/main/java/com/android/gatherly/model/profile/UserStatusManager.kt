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
   * Automatic updates (like focus timer or lifecycle events) do not override manual updates (from
   * settings).
   *
   * @param status The [ProfileStatus] to set for the user.
   * @param source Whether the change is manual or automatic. Defaults to AUTOMATIC.
   * @param resetToAuto Optional flag to force the source to AUTOMATIC even if the update is manual.
   *   Useful for "reset to auto" scenarios, (e.g when the user selects ONLINE manually)
   */
  suspend fun setStatus(
      status: ProfileStatus,
      source: UserStatusSource = UserStatusSource.AUTOMATIC,
      resetToAuto: Boolean = false
  ) {
    val uid = auth.currentUser?.uid ?: return
    val profile = repo.getProfileByUid(uid) ?: return
    // Prevent automatic updates from overriding manual status
    val autoOverridesManual =
        (profile.userStatusSource == UserStatusSource.MANUAL &&
            source == UserStatusSource.AUTOMATIC)
    if (autoOverridesManual && !resetToAuto) return

    val newSource = if (resetToAuto) UserStatusSource.AUTOMATIC else source
    repo.updateStatus(uid, status, newSource)
  }
}
