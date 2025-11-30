package com.android.gatherly.utilstest

import com.android.gatherly.model.notification.Notification
import com.android.gatherly.model.notification.NotificationType
import com.android.gatherly.model.notification.NotificationsLocalRepository
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.utils.getProfileWithSyncedFriendNotifications
import com.google.firebase.Timestamp
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class UpdateProfileFriendsUtilsTest {

  private fun ts() = Timestamp.now()

  @Test
  fun friendAccepted_addsFriend_andDeletesNotification() = runTest {
    val profiles = ProfileLocalRepository()
    val notifs = NotificationsLocalRepository()

    val current = Profile("u1", "alice", "Alice")
    val sender = Profile("u2", "bob", "Bob")

    profiles.addProfile(current)
    profiles.addProfile(sender)

    val notif =
        Notification(
            id = "n1",
            type = NotificationType.FRIEND_ACCEPTED,
            emissionTime = ts(),
            senderId = "u2",
            relatedEntityId = null,
            recipientId = "u1",
            wasRead = false)
    notifs.addNotification(notif)

    val updated = getProfileWithSyncedFriendNotifications(profiles, notifs, "u1")

    assertNotNull(updated)
    assertTrue(updated!!.friendUids.contains("u2"))

    try {
      notifs.getNotification("n1")
      fail("Expected NoSuchElementException")
    } catch (e: NoSuchElementException) {
      // success
    }
  }

  @Test
  fun friendRejected_deletesNotification_only() = runTest {
    val profiles = ProfileLocalRepository()
    val notifs = NotificationsLocalRepository()

    val current = Profile("u1", "alice", "Alice")
    profiles.addProfile(current)

    val notif =
        Notification(
            id = "n1",
            type = NotificationType.FRIEND_REJECTED,
            emissionTime = ts(),
            senderId = "u99",
            relatedEntityId = null,
            recipientId = "u1",
            wasRead = false)
    notifs.addNotification(notif)

    val updated = getProfileWithSyncedFriendNotifications(profiles, notifs, "u1")

    assertNotNull(updated)
    assertTrue(updated!!.friendUids.isEmpty())

    try {
      notifs.getNotification("n1")
      fail("Expected NoSuchElementException")
    } catch (e: NoSuchElementException) {
      // success
    }
  }

  @Test
  fun irrelevantNotifications_doNothing() = runTest {
    val profiles = ProfileLocalRepository()
    val notifs = NotificationsLocalRepository()

    val current = Profile("u1", "alice", "Alice")
    profiles.addProfile(current)

    val notif =
        Notification(
            id = "n1",
            type = NotificationType.EVENT_REMINDER,
            emissionTime = ts(),
            senderId = null,
            relatedEntityId = null,
            recipientId = "u1",
            wasRead = false)
    notifs.addNotification(notif)

    val updated = getProfileWithSyncedFriendNotifications(profiles, notifs, "u1")

    assertNotNull(updated)
    assertTrue(updated!!.friendUids.isEmpty())

    // Irrelevant notifications remain
    assertNotNull(notifs.getNotification("n1"))
  }
}
