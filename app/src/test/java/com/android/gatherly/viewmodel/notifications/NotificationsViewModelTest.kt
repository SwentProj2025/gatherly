package com.android.gatherly.viewmodel.notifications

import com.android.gatherly.model.notification.Notification
import com.android.gatherly.model.notification.NotificationType
import com.android.gatherly.model.notification.NotificationsLocalRepository
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.ui.notifications.NotificationViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

/**
 * Unit tests for [NotificationViewModel].
 *
 * These tests use:
 * - [NotificationsLocalRepository] as an in-memory notifications store
 * - [ProfileLocalRepository] as an in-memory profile store
 * - A mocked [FirebaseAuth] to control the current user UID
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NotificationViewModelTest {

  private val testDispatcher = StandardTestDispatcher()

  private lateinit var notificationsRepo: NotificationsLocalRepository
  private lateinit var profileRepo: ProfileLocalRepository
  private lateinit var mockAuth: FirebaseAuth
  private lateinit var mockUser: FirebaseUser

  private lateinit var viewModel: NotificationViewModel

  private val CURRENT_USER_ID = "currentUser"

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)

    notificationsRepo = NotificationsLocalRepository()
    profileRepo = ProfileLocalRepository()

    // --- Mock FirebaseAuth / FirebaseUser ---
    mockAuth = Mockito.mock(FirebaseAuth::class.java)
    mockUser = Mockito.mock(FirebaseUser::class.java)
    Mockito.`when`(mockUser.uid).thenReturn(CURRENT_USER_ID)
    Mockito.`when`(mockAuth.currentUser).thenReturn(mockUser)

    // Ensure current user has a profile in the local repo
    runTest {
      profileRepo.addProfile(
          Profile(
              uid = CURRENT_USER_ID,
              username = "current_user",
              name = "Current User",
              profilePicture = "pic.png"))
    }

    viewModel =
        NotificationViewModel(
            notificationsRepository = notificationsRepo,
            profileRepository = profileRepo,
            authProvider = { mockAuth })
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  // ------------------------------------------------------------------------
  // Helpers
  // ------------------------------------------------------------------------

  private fun newTimestamp(): Timestamp = Timestamp.now()

  private suspend fun createFriendRequestNotification(
      id: String,
      senderId: String,
      recipientId: String,
      wasRead: Boolean = false
  ): Notification {
    val notif =
        Notification(
            id = id,
            type = NotificationType.FRIEND_REQUEST,
            emissionTime = newTimestamp(),
            senderId = senderId,
            relatedEntityId = null,
            recipientId = recipientId,
            wasRead = wasRead)
    notificationsRepo.addNotification(notif)
    return notif
  }

  // ------------------------------------------------------------------------
  // loadNotifications
  // ------------------------------------------------------------------------

  @Test
  fun loadNotifications_success_updatesUiStateAndUnreadFlag() = runTest {
    // GIVEN some notifications in the repo for the current user
    val sender1 = "sender1"
    val sender2 = "sender2"

    // Unread friend request
    createFriendRequestNotification(
        id = "n1", senderId = sender1, recipientId = CURRENT_USER_ID, wasRead = false)

    // Read friend request
    val readNotif =
        createFriendRequestNotification(
            id = "n2", senderId = sender2, recipientId = CURRENT_USER_ID, wasRead = false)
    notificationsRepo.markAsRead(readNotif.id)

    // Non-friend type notification
    val other =
        Notification(
            id = "n3",
            type = NotificationType.TODO_REMINDER,
            emissionTime = newTimestamp(),
            senderId = null,
            relatedEntityId = null,
            recipientId = CURRENT_USER_ID,
            wasRead = false)
    notificationsRepo.addNotification(other)

    // WHEN
    viewModel.loadNotifications()
    advanceUntilIdle()

    // THEN
    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertNull(state.errorMessage)
    assertEquals(3, state.notifications.size)
    // At least one unread friend request
    assertTrue(state.hasUnreadFriendRequests)
  }

  @Test
  fun loadNotifications_failure_setsErrorMessage() = runTest {
    // GIVEN a repo that throws on getUserNotifications
    val failingRepo =
        object : NotificationsLocalRepository() {
          override suspend fun getUserNotifications(userId: String): List<Notification> {
            throw RuntimeException("Boom")
          }
        }

    val vm =
        NotificationViewModel(
            notificationsRepository = failingRepo,
            profileRepository = profileRepo,
            authProvider = { mockAuth })

    // WHEN
    vm.loadNotifications()
    advanceUntilIdle()

    // THEN
    val state = vm.uiState.value
    assertFalse(state.isLoading)
    assertEquals("Failed to load user's notifications", state.errorMessage)
    assertTrue(state.notifications.isEmpty())
  }

  // ------------------------------------------------------------------------
  // sendFriendRequest
  // ------------------------------------------------------------------------

  @Test
  fun sendFriendRequest_success_addsNotificationForRecipient() = runTest {
    val recipientId = "recipientUser"

    // WHEN
    viewModel.sendFriendRequest(recipientId)
    advanceUntilIdle()

    // THEN notification should be in repo for that recipient
    val userNotifications = notificationsRepo.getUserNotifications(recipientId)
    assertEquals(1, userNotifications.size)
    val notif = userNotifications.first()
    assertEquals(NotificationType.FRIEND_REQUEST, notif.type)
    assertEquals(CURRENT_USER_ID, notif.senderId)
    assertEquals(recipientId, notif.recipientId)
    assertFalse(notif.wasRead)
    assertNull(viewModel.uiState.value.errorMessage)
  }

  @Test
  fun sendFriendRequest_failure_setsErrorMessage() = runTest {
    // GIVEN repo that throws on addNotification
    val delegate = notificationsRepo
    val failingRepo =
        object : NotificationsLocalRepository() {
          override fun getNewId(): String = delegate.getNewId()

          override suspend fun getUserNotifications(userId: String): List<Notification> =
              delegate.getUserNotifications(userId)

          override suspend fun getNotification(notificationId: String): Notification =
              delegate.getNotification(notificationId)

          override suspend fun deleteNotification(notificationId: String) =
              delegate.deleteNotification(notificationId)

          override suspend fun addNotification(notification: Notification) {
            throw RuntimeException("Unable to add notification")
          }

          override suspend fun markAsRead(notificationId: String) =
              delegate.markAsRead(notificationId)
        }

    val vm =
        NotificationViewModel(
            notificationsRepository = failingRepo,
            profileRepository = profileRepo,
            authProvider = { mockAuth })

    // WHEN
    vm.sendFriendRequest("recipient")
    advanceUntilIdle()

    // THEN
    assertEquals("Failed to send friend request", vm.uiState.value.errorMessage)
  }

  // ------------------------------------------------------------------------
  // acceptFriendRequest
  // ------------------------------------------------------------------------

  @Test
  fun acceptFriendRequest_happyPath_addsFriend_andSendsAcceptedNotification_andDeletesOriginal() =
      runTest {
        val senderId = "senderUser"
        val senderUsername = "sender_username"

        // Sender profile (needed so ViewModel can resolve username -> friend)
        profileRepo.addProfile(
            Profile(
                uid = senderId,
                username = senderUsername,
                name = "Sender",
                profilePicture = "s.png"))

        // Friend request from sender -> current user
        val request =
            createFriendRequestNotification(
                id = "req1", senderId = senderId, recipientId = CURRENT_USER_ID)

        // WHEN
        viewModel.acceptFriendRequest(request.id)
        advanceUntilIdle()

        // THEN: current user should have sender as friend
        val currentProfile = profileRepo.getProfileByUid(CURRENT_USER_ID)
        assertNotNull(currentProfile)
        assertTrue(currentProfile!!.friendUids.contains(senderId))

        // Original request should be deleted
        try {
          notificationsRepo.getNotification(request.id)
          fail("Expected NoSuchElementException for deleted friend request")
        } catch (e: NoSuchElementException) {
          // ok
        }

        // Sender should have received a FRIEND_ACCEPTED notification
        val senderNotifications = notificationsRepo.getUserNotifications(senderId)
        assertEquals(1, senderNotifications.size)
        val accepted = senderNotifications.first()
        assertEquals(NotificationType.FRIEND_ACCEPTED, accepted.type)
        assertEquals(CURRENT_USER_ID, accepted.senderId)
        assertEquals(senderId, accepted.recipientId)
      }

  @Test
  fun acceptFriendRequest_onNonFriendRequest_doesNothing() = runTest {
    val notif =
        Notification(
            id = "n1",
            type = NotificationType.TODO_REMINDER,
            emissionTime = newTimestamp(),
            senderId = "someone",
            relatedEntityId = null,
            recipientId = CURRENT_USER_ID,
            wasRead = false)
    notificationsRepo.addNotification(notif)

    // WHEN
    viewModel.acceptFriendRequest(notif.id)
    advanceUntilIdle()

    // THEN: notification still exists, no friends added
    val stillThere = notificationsRepo.getNotification(notif.id)
    assertEquals(NotificationType.TODO_REMINDER, stillThere.type)

    val currentProfile = profileRepo.getProfileByUid(CURRENT_USER_ID)
    assertNotNull(currentProfile)
    assertTrue(currentProfile!!.friendUids.isEmpty())
  }

  @Test
  fun acceptFriendRequest_whenRecipientIsNotCurrentUser_setsErrorMessage() = runTest {
    val senderId = "sender"
    val otherRecipient = "someoneElse"

    // Sender profile exists
    profileRepo.addProfile(
        Profile(
            uid = senderId,
            username = "sender_username",
            name = "Sender",
            profilePicture = "pic.png"))

    // Friend request to someone else
    val request =
        createFriendRequestNotification(
            id = "req2", senderId = senderId, recipientId = otherRecipient)

    // WHEN
    viewModel.acceptFriendRequest(request.id)
    advanceUntilIdle()

    // THEN
    assertEquals("Failed to accept friend request", viewModel.uiState.value.errorMessage)
    // request should still exist
    val stillThere = notificationsRepo.getNotification(request.id)
    assertEquals(NotificationType.FRIEND_REQUEST, stillThere.type)
  }

  // ------------------------------------------------------------------------
  // rejectFriendRequest
  // ------------------------------------------------------------------------

  @Test
  fun rejectFriendRequest_happyPath_createsRejectedNotification_andDeletesOriginal() = runTest {
    val senderId = "senderUser"
    val otherUsername = "sender_username"

    // Sender profile exists (not required for reject, but realistic)
    profileRepo.addProfile(
        Profile(
            uid = senderId, username = otherUsername, name = "Sender", profilePicture = "s.png"))

    val request =
        createFriendRequestNotification(
            id = "reqReject", senderId = senderId, recipientId = CURRENT_USER_ID)

    // WHEN
    viewModel.rejectFriendRequest(request.id)
    advanceUntilIdle()

    // THEN: original request is gone
    try {
      notificationsRepo.getNotification(request.id)
      fail("Expected NoSuchElementException for deleted request")
    } catch (e: NoSuchElementException) {
      // ok
    }

    // Sender must receive a FRIEND_REJECTED notification
    val senderNotifications = notificationsRepo.getUserNotifications(senderId)
    assertEquals(1, senderNotifications.size)
    val rejected = senderNotifications.first()
    assertEquals(NotificationType.FRIEND_REJECTED, rejected.type)
    assertEquals(CURRENT_USER_ID, rejected.senderId)
    assertEquals(senderId, rejected.recipientId)
  }

  @Test
  fun rejectFriendRequest_onNonFriendRequest_doesNothing() = runTest {
    val notif =
        Notification(
            id = "nReject",
            type = NotificationType.EVENT_REMINDER,
            emissionTime = newTimestamp(),
            senderId = "someUser",
            relatedEntityId = null,
            recipientId = CURRENT_USER_ID,
            wasRead = false)
    notificationsRepo.addNotification(notif)

    // WHEN
    viewModel.rejectFriendRequest(notif.id)
    advanceUntilIdle()

    // THEN: no deletion
    val stillThere = notificationsRepo.getNotification(notif.id)
    assertEquals(NotificationType.EVENT_REMINDER, stillThere.type)

    // No new notifications created for sender
    val senderBox = notificationsRepo.getUserNotifications("someUser")
    assertTrue(senderBox.isEmpty()) // FIXED
  }

  // ------------------------------------------------------------------------
  // clearError
  // ------------------------------------------------------------------------

  @Test
  fun clearError_resetsErrorMessage() = runTest {
    // Force an error
    val failingRepo =
        object : NotificationsLocalRepository() {
          override suspend fun getUserNotifications(userId: String): List<Notification> {
            throw RuntimeException("Boom")
          }
        }

    val vm =
        NotificationViewModel(
            notificationsRepository = failingRepo,
            profileRepository = profileRepo,
            authProvider = { mockAuth })

    vm.loadNotifications()
    advanceUntilIdle()
    assertNotNull(vm.uiState.value.errorMessage)

    vm.clearError()
    assertNull(vm.uiState.value.errorMessage)
  }
}
