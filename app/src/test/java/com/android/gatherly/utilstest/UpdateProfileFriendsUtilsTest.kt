package com.android.gatherly.utilstest

import com.android.gatherly.model.notification.Notification
import com.android.gatherly.model.notification.NotificationType
import com.android.gatherly.model.notification.NotificationsLocalRepository
import com.android.gatherly.model.points.PointsLocalRepository
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.utils.getProfileWithSyncedFriendNotifications
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UpdateProfileFriendsUtilsTest {

  private fun ts() = Timestamp.now()

  private val pointsRepository = PointsLocalRepository()

  private val testDispatcher = UnconfinedTestDispatcher()

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

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

    val updated = getProfileWithSyncedFriendNotifications(profiles, notifs, pointsRepository, "u1")

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

    val updated = getProfileWithSyncedFriendNotifications(profiles, notifs, pointsRepository, "u1")

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

    val updated = getProfileWithSyncedFriendNotifications(profiles, notifs, pointsRepository, "u1")

    assertNotNull(updated)
    assertTrue(updated!!.friendUids.isEmpty())

    // Irrelevant notifications remain
    assertNotNull(notifs.getNotification("n1"))
  }

  @Test
  fun friendAccepted_removesPendingSentUid() = runTest {
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

    // Should now be friends
    assertTrue(updated.friendUids.contains("u2"))

    // Pending should be removed
    assertFalse(updated.pendingSentFriendsUids.contains("u2"))
  }

  @Test
  fun friendRejected_removesPendingSentUid() = runTest {
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

    // Should NOT become friends
    assertFalse(updated.friendUids.contains("u2"))

    // Pending should be removed
    assertFalse(updated.pendingSentFriendsUids.contains("u2"))
  }

  @Test
  fun removeFriend_notificationRemovesFriend_andDeletesNotification() = runTest {
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

    // Friend must be removed
    assertFalse(updated.friendUids.contains("u2"))

    // Notification must be deleted
    try {
      notifs.getNotification("n1")
      fail("Expected deletion of remove-friend notification")
    } catch (_: NoSuchElementException) {}
  }

  @Test
  fun friendRequestCancelled_deletesOriginalRequest() = runTest {
    val profiles = ProfileLocalRepository()
    val notifs = NotificationsLocalRepository()

    // u1 (sender)
    val u1 =
        Profile(
            uid = "u1",
            username = "alice",
            name = "Alice",
            pendingSentFriendsUids = listOf("u2") // u1 has sent a request to u2
            )
    profiles.addProfile(u1)

    // u2 (recipient)
    val u2 = Profile(uid = "u2", username = "bob", name = "Bob")
    profiles.addProfile(u2)

    // Original friend request (belongs in u2’s inbox)
    val request =
        Notification(
            id = "req1",
            type = NotificationType.FRIEND_REQUEST,
            emissionTime = ts(),
            senderId = "u1",
            recipientId = "u2",
            relatedEntityId = null,
            wasRead = false)

    // Cancellation (also belongs in u2’s inbox)
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

    // Sync u2’s profile with its notifications
    val updatedU2 =
        getProfileWithSyncedFriendNotifications(profiles, notifs, pointsRepository, "u2")!!

    // --- Assertions ---

    // u2's notifications should be deleted
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

    // u2 should not have u1 as friend(unchanged)
    assertFalse("u2 should not have u1 as friend", updatedU2.friendUids.contains("u1"))
  }
}
