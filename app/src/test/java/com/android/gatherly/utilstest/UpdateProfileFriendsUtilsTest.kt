package com.android.gatherly.utilstest

import com.android.gatherly.model.notification.Notification
import com.android.gatherly.model.notification.NotificationType
import com.android.gatherly.model.notification.NotificationsLocalRepository
import com.android.gatherly.model.points.PointsLocalRepository
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.runUnconfinedTest
import com.android.gatherly.utils.getProfileWithSyncedFriendNotifications
import com.google.firebase.Timestamp
import kotlin.time.Duration.Companion.seconds
import org.junit.Assert.*
import org.junit.Test

/** Unit tests for updating friends based on notifications. */
class UpdateProfileFriendsUtilsTest {

  private val testTimeout = 120.seconds

  private fun ts() = Timestamp.now()

  private val pointsRepository = PointsLocalRepository()

  /**
   * Verify that a FRIEND_ACCEPTED notification adds the sender as a friend and deletes the
   * notification.
   */
  @Test
  fun friendAccepted_addsFriend_andDeletesNotification() =
      runUnconfinedTest(testTimeout) {
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

        val updated =
            getProfileWithSyncedFriendNotifications(profiles, notifs, pointsRepository, "u1")

        assertNotNull(updated)
        assertTrue(updated!!.friendUids.contains("u2"))

        try {
          notifs.getNotification("n1")
          fail("Expected NoSuchElementException")
        } catch (e: NoSuchElementException) {
          // success
        }
      }

  /**
   * Verify that a FRIEND_REJECTED notification deletes the notification without adding a friend.
   */
  @Test
  fun friendRejected_deletesNotification_only() =
      runUnconfinedTest(testTimeout) {
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

        val updated =
            getProfileWithSyncedFriendNotifications(profiles, notifs, pointsRepository, "u1")

        assertNotNull(updated)
        assertTrue(updated!!.friendUids.isEmpty())

        try {
          notifs.getNotification("n1")
          fail("Expected NoSuchElementException")
        } catch (e: NoSuchElementException) {
          // success
        }
      }

  /** Verify that notifications irrelevant to friend management are not processed or deleted. */
  @Test
  fun irrelevantNotifications_doNothing() =
      runUnconfinedTest(testTimeout) {
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

        val updated =
            getProfileWithSyncedFriendNotifications(profiles, notifs, pointsRepository, "u1")

        assertNotNull(updated)
        assertTrue(updated!!.friendUids.isEmpty())

        assertNotNull(notifs.getNotification("n1"))
      }

  /** Verify that FRIEND_ACCEPTED removes the sender from pendingSentFriendsUids. */
  @Test
  fun friendAccepted_removesPendingSentUid() =
      runUnconfinedTest(testTimeout) {
        val profiles = ProfileLocalRepository()
        val notifs = NotificationsLocalRepository()

        val current = Profile("u1", "alice", "Alice", pendingSentFriendsUids = listOf("u2"))
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

        val updated =
            getProfileWithSyncedFriendNotifications(profiles, notifs, pointsRepository, "u1")!!

        assertTrue(updated.friendUids.contains("u2"))
        assertFalse(updated.pendingSentFriendsUids.contains("u2"))
      }

  /**
   * Verify that FRIEND_REJECTED removes the sender from pendingSentFriendsUids without adding as
   * friend.
   */
  @Test
  fun friendRejected_removesPendingSentUid() =
      runUnconfinedTest(testTimeout) {
        val profiles = ProfileLocalRepository()
        val notifs = NotificationsLocalRepository()

        val current = Profile("u1", "alice", "Alice", pendingSentFriendsUids = listOf("u2"))
        profiles.addProfile(current)

        val notif =
            Notification(
                id = "n1",
                type = NotificationType.FRIEND_REJECTED,
                emissionTime = ts(),
                senderId = "u2",
                relatedEntityId = null,
                recipientId = "u1",
                wasRead = false)
        notifs.addNotification(notif)

        val updated =
            getProfileWithSyncedFriendNotifications(profiles, notifs, pointsRepository, "u1")!!

        assertFalse(updated.friendUids.contains("u2"))
        assertFalse(updated.pendingSentFriendsUids.contains("u2"))
      }

  /**
   * Verify that REMOVE_FRIEND notification removes the sender from friendUids and deletes the
   * notification.
   */
  @Test
  fun removeFriend_notificationRemovesFriend_andDeletesNotification() =
      runUnconfinedTest(testTimeout) {
        val profiles = ProfileLocalRepository()
        val notifs = NotificationsLocalRepository()

        val current = Profile("u1", "alice", "Alice", friendUids = listOf("u2"))
        val sender = Profile("u2", "bob", "Bob")

        profiles.addProfile(current)
        profiles.addProfile(sender)

        val notif =
            Notification(
                id = "n1",
                type = NotificationType.REMOVE_FRIEND,
                emissionTime = ts(),
                senderId = "u2",
                relatedEntityId = null,
                recipientId = "u1",
                wasRead = false)
        notifs.addNotification(notif)

        val updated =
            getProfileWithSyncedFriendNotifications(profiles, notifs, pointsRepository, "u1")!!

        assertFalse(updated.friendUids.contains("u2"))

        try {
          notifs.getNotification("n1")
          fail("Expected deletion of remove-friend notification")
        } catch (_: NoSuchElementException) {}
      }

  /**
   * Verify that FRIEND_REQUEST_CANCELLED deletes both the cancellation and original request
   * notifications.
   */
  @Test
  fun friendRequestCancelled_deletesOriginalRequest() =
      runUnconfinedTest(testTimeout) {
        val profiles = ProfileLocalRepository()
        val notifs = NotificationsLocalRepository()

        val u1 =
            Profile(
                uid = "u1",
                username = "alice",
                name = "Alice",
                pendingSentFriendsUids = listOf("u2"))
        profiles.addProfile(u1)

        val u2 = Profile(uid = "u2", username = "bob", name = "Bob")
        profiles.addProfile(u2)

        val request =
            Notification(
                id = "req1",
                type = NotificationType.FRIEND_REQUEST,
                emissionTime = ts(),
                senderId = "u1",
                recipientId = "u2",
                relatedEntityId = null,
                wasRead = false)

        val cancel =
            Notification(
                id = "req2",
                type = NotificationType.FRIEND_REQUEST_CANCELLED,
                emissionTime = ts(),
                senderId = "u1",
                recipientId = "u2",
                relatedEntityId = null,
                wasRead = false)

        notifs.addNotification(request)
        notifs.addNotification(cancel)

        val updatedU2 =
            getProfileWithSyncedFriendNotifications(profiles, notifs, pointsRepository, "u2")!!

        try {
          notifs.getNotification("req1")
          fail("Expected NoSuchElementException")
        } catch (e: NoSuchElementException) {
          // success
        }
        try {
          notifs.getNotification("req2")
          fail("Expected NoSuchElementException")
        } catch (e: NoSuchElementException) {
          // success
        }

        assertFalse("u2 should not have u1 as friend", updatedU2.friendUids.contains("u1"))
      }
}
