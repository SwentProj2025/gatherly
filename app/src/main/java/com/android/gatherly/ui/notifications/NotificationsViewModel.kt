package com.android.gatherly.ui.notifications

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.gatherly.model.notification.Notification
import com.android.gatherly.model.notification.NotificationType
import com.android.gatherly.model.notification.NotificationsRepository
import com.android.gatherly.model.notification.NotificationsRepositoryFirestore
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.profile.ProfileRepositoryFirestore
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** UI state for the Notifications screen. */
data class NotificationUiState(
    val notifications: List<Notification> = emptyList(),
    val idToProfile: Map<String, Profile> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasUnreadFriendRequests: Boolean = false
)

/**
 * ViewModel responsible for:
 * - loading notifications for the current user
 * - sending friend requests
 * - accepting / rejecting friend requests
 *
 * It coordinates between [NotificationsRepository] for notifications and [ProfileRepository] for
 * updating the friends list
 */
class NotificationViewModel(
    private val notificationsRepository: NotificationsRepository =
        NotificationsRepositoryFirestore(Firebase.firestore),
    private val profileRepository: ProfileRepository =
        ProfileRepositoryFirestore(Firebase.firestore, Firebase.storage),
    private val authProvider: () -> FirebaseAuth = { Firebase.auth }
) : ViewModel() {

  private val _uiState = MutableStateFlow(NotificationUiState())
  val uiState: StateFlow<NotificationUiState> = _uiState

  private val currentUserId: String
    get() = authProvider().currentUser?.uid ?: throw IllegalStateException("No signed-in user")

  /** Loads all notifications for the currently signed-in user. */
  fun loadNotifications() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
      try {
        val notifications = notificationsRepository.getUserNotifications(currentUserId)
        val hasUnreadFriendRequests =
            notifications.any { it.type == NotificationType.FRIEND_REQUEST && !it.wasRead }

        val idToProfile = mutableMapOf<String, Profile>()
        for (notification in notifications) {
          val senderId = notification.senderId as String
          idToProfile[senderId] = profileRepository.getProfileByUid(senderId) as Profile
        }

        _uiState.value =
            _uiState.value.copy(
                notifications = notifications,
                idToProfile = idToProfile,
                isLoading = false,
                hasUnreadFriendRequests = hasUnreadFriendRequests)
      } catch (e: Exception) {
        _uiState.value =
            _uiState.value.copy(
                isLoading = false, errorMessage = "Failed to load user's notifications")
      }
    }
  }

  /** Accepts a friend request represented by [notificationId]. */
  fun acceptFriendRequest(notificationId: String) {
    viewModelScope.launch {
      try {
        val notification = notificationsRepository.getNotification(notificationId)
        if (notification.type != NotificationType.FRIEND_REQUEST) {
          Log.w(
              "NotificationViewModel",
              "acceptFriendRequest called on non-Friend_REQUEST notification")
          return@launch
        }
        val senderId =
            notification.senderId
                ?: throw IllegalArgumentException("Friend request notification missing senderId")
        val recipientId = notification.recipientId

        if (recipientId != currentUserId) {
          throw SecurityException("Current user is not the recipient of this friend request")
        }

        val senderProfile =
            profileRepository.getProfileByUid(senderId)
                ?: throw IllegalStateException("Sender profile not found")
        val senderUsername = senderProfile.username

        profileRepository.addFriend(friend = senderUsername, currentUserId = recipientId)

        val acceptedId = notificationsRepository.getNewId()
        val acceptedNotification =
            Notification(
                id = acceptedId,
                type = NotificationType.FRIEND_ACCEPTED,
                emissionTime = Timestamp.now(),
                senderId = currentUserId,
                relatedEntityId = null,
                recipientId = senderId,
                wasRead = false)
        notificationsRepository.addNotification(acceptedNotification)
        notificationsRepository.deleteNotification(notification.id)
        loadNotifications()
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(errorMessage = "Failed to accept friend request")
      }
    }
  }

  /** Rejects a friend request represented by [notificationId]. */
  fun rejectFriendRequest(notificationId: String) {
    viewModelScope.launch {
      try {
        val notification = notificationsRepository.getNotification(notificationId)
        if (notification.type != NotificationType.FRIEND_REQUEST) {
          Log.w(
              "NotificationViewModel",
              "rejectFriendRequest called on non-Friend_REQUEST notification")
          return@launch
        }
        val senderId =
            notification.senderId
                ?: throw IllegalArgumentException("Friend request notification missing senderId")
        val recipientId = notification.recipientId

        if (recipientId != currentUserId) {
          throw SecurityException("Current user is not the recipient of this friend request")
        }

        val rejectedId = notificationsRepository.getNewId()
        val rejectedNotification =
            Notification(
                id = rejectedId,
                type = NotificationType.FRIEND_REJECTED,
                emissionTime = Timestamp.now(),
                senderId = currentUserId,
                relatedEntityId = null,
                recipientId = senderId,
                wasRead = false)
        notificationsRepository.addNotification(rejectedNotification)
        notificationsRepository.deleteNotification(notification.id)
        loadNotifications()
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(errorMessage = "Failed to reject friend request")
      }
    }
  }

  /** Clears any error message currently in the UI state. */
  fun clearError() {
    _uiState.value = _uiState.value.copy(errorMessage = null)
  }
}
