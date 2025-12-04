package com.android.gatherly.model.profile

import com.android.gatherly.utils.FirebaseEmulator
import com.android.gatherly.utils.FirestoreGatherlyProfileTest
import com.google.firebase.auth.FirebaseAuth
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UserStatusManagerTest : FirestoreGatherlyProfileTest() {

  private lateinit var auth: FirebaseAuth
  private lateinit var uid: String
  private lateinit var manager: UserStatusManager

  @Before
  fun setup() = runTest {
    auth = FirebaseEmulator.auth
    uid = auth.currentUser!!.uid

    repository.initProfileIfMissing(uid, defaultPhotoUrl = "default.png")

    manager = UserStatusManager(auth, repository)
  }

  @Test
  fun setStatus_updatesUserStatusInFirestore() =
      runTest(timeout = 120.seconds) {
        manager.setStatus(ProfileStatus.ONLINE)
        var profile = repository.getProfileByUid(uid)
        assertEquals(ProfileStatus.ONLINE, profile!!.status)
        assertEquals(UserStatusSource.AUTOMATIC, profile.userStatusSource)

        manager.setStatus(ProfileStatus.OFFLINE)
        profile = repository.getProfileByUid(uid)
        assertEquals(ProfileStatus.OFFLINE, profile!!.status)
        assertEquals(UserStatusSource.AUTOMATIC, profile.userStatusSource)
      }

  @Test
  fun setStatus_manualUpdate_updatesUserStatusSource() =
      runTest(timeout = 120.seconds) {
        manager.setStatus(ProfileStatus.FOCUSED, UserStatusSource.MANUAL)
        val profile = repository.getProfileByUid(uid)
        assertEquals(ProfileStatus.FOCUSED, profile!!.status)
        assertEquals(UserStatusSource.MANUAL, profile.userStatusSource)
      }

  @Test
  fun automaticUpdate_doesNotOverrideManualStatus() =
      runTest(timeout = 120.seconds) {
        // User manually sets status
        manager.setStatus(ProfileStatus.FOCUSED, UserStatusSource.MANUAL)

        // Automatic update occurs
        manager.setStatus(ProfileStatus.ONLINE) // automatic

        val profile = repository.getProfileByUid(uid)
        assertEquals(ProfileStatus.FOCUSED, profile!!.status)
        assertEquals(UserStatusSource.MANUAL, profile.userStatusSource)
      }

  @Test
  fun manualOnline_resetsSourceToAutomatic() =
      runTest(timeout = 120.seconds) {
        manager.setStatus(ProfileStatus.ONLINE, UserStatusSource.MANUAL, resetToAuto = true)
        val profile = repository.getProfileByUid(uid)
        assertEquals(ProfileStatus.ONLINE, profile!!.status)
        assertEquals(UserStatusSource.AUTOMATIC, profile.userStatusSource)
      }

  @Test
  fun automaticUpdate_overridesAutomaticStatus() =
      runTest(timeout = 120.seconds) {
        manager.setStatus(ProfileStatus.FOCUSED)
        manager.setStatus(ProfileStatus.ONLINE)

        val profile = repository.getProfileByUid(uid)
        assertEquals(ProfileStatus.ONLINE, profile!!.status)
        assertEquals(UserStatusSource.AUTOMATIC, profile.userStatusSource)
      }
}
