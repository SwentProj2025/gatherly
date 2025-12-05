package com.android.gatherly.model.profile

import com.android.gatherly.utils.FirebaseEmulator
import com.android.gatherly.utils.FirestoreGatherlyProfileTest
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class UserStatusManagerTest : FirestoreGatherlyProfileTest() {

  @Test
  fun setStatus_updatesUserStatusInFirestore() =
      runTest(timeout = 120.seconds) {
        val auth = FirebaseEmulator.auth
        val uid = auth.currentUser!!.uid

        // Given a profile exists
        repository.initProfileIfMissing(uid, defaultPhotoUrl = "default.png")

        val manager = UserStatusManager(auth, repository)

        // When setting status to ONLINE
        manager.setStatus(ProfileStatus.ONLINE)
        var profile = repository.getProfileByUid(uid)
        assertEquals(ProfileStatus.ONLINE, profile!!.status)

        // When setting status to OFFLINE
        manager.setStatus(ProfileStatus.OFFLINE)
        profile = repository.getProfileByUid(uid)
        assertEquals(ProfileStatus.OFFLINE, profile!!.status)
      }
}
