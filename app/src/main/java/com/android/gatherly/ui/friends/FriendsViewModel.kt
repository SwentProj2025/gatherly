package com.android.gatherly.ui.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.gatherly.model.notification.Notification
import com.android.gatherly.model.notification.NotificationType
import com.android.gatherly.model.notification.NotificationsRepository
import com.android.gatherly.model.notification.NotificationsRepositoryProvider
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.profile.ProfileRepositoryFirestore
import com.android.gatherly.utils.GenericViewModelFactory
import com.android.gatherly.utils.getProfileWithSyncedFriendNotifications
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI model for the Friends screen.
 *
 * @property errorMsg optional message to display on network or logic errors
 * @property friends list of usernames that are confirmed friends
 * @property listNoFriends list of usernames that are not friends
 * @property pendingSentUsernames usernames for which the current user has sent a pending friend
 *   request
 * @property currentUserId the uid of the logged-in user
 * @property isLoading controls the loading indicator
 * @property profiles cached map of usernames to Profile for quick UI access
 */
data class FriendsUIState(
    val errorMsg: String? = null,
    val friends: List<String> = emptyList(),
    val listNoFriends: List<String> = emptyList(),
    val pendingSentUsernames: List<String> = emptyList(),
    val currentUserId: String = "",
    val isLoading: Boolean = true,
    val profiles: Map<String, Profile> = emptyMap()
)

/**
 * ViewModel responsible for all friend-related operations:
 * - Refresh friends
 * - Sending friend requests
 * - Cancelling friend requests
 * - Removing existing friends
 */
class FriendsViewModel(
    private val repository: ProfileRepository,
    private val notificationsRepository: NotificationsRepository =
        NotificationsRepositoryProvider.repository,
    private val authProvider: () -> FirebaseAuth = { Firebase.auth }
) : ViewModel() {

  /** StateFlow that emits the current UI state for the Friends screen. */
  private val _uiState = MutableStateFlow(FriendsUIState())
  val uiState: StateFlow<FriendsUIState> = _uiState.asStateFlow()

  /**
   * Initializes the ViewModel by loading all the friends' profile from the repository and filtering
   * them to display only drawable todos.
   */
  init {
    viewModelScope.launch { refreshFriends(authProvider().currentUser?.uid ?: "") }
  }

  /**
   * Refreshes the friends lists based on the current user ID.
   *
   * @param currentUserId the ID of the current user
   */
  suspend fun refreshFriends(currentUserId: String) {
    _uiState.value = _uiState.value.copy(isLoading = true)
    try {
      getProfileWithSyncedFriendNotifications(
          profileRepository = repository,
          notificationsRepository = notificationsRepository,
          userId = currentUserId)
      val friendsData = repository.getFriendsAndNonFriendsUsernames(currentUserId)
      val allUsernames = friendsData.friendUsernames + friendsData.nonFriendUsernames
      val profiles =
          allUsernames
              .mapNotNull { username -> repository.getProfileByUsername(username) }
              .associateBy { it.username }

      val currentUserProfile = repository.getProfileByUid(currentUserId)!!
      val pendingSentUsernames =
          currentUserProfile.pendingSentFriendsUids.mapNotNull { uid ->
            repository.getProfileByUid(uid)?.username
          }

      _uiState.value =
          _uiState.value.copy(
              friends = friendsData.friendUsernames,
              listNoFriends = friendsData.nonFriendUsernames,
              pendingSentUsernames = pendingSentUsernames,
              profiles = profiles,
              errorMsg = null,
              currentUserId = currentUserId,
              isLoading = false)
    } catch (e: Exception) {
      _uiState.value = _uiState.value.copy(errorMsg = "Failed to load friends: ${e.message}")
    }
  }

  /**
   * Handles user unfollowing a friend. Should be deleted when ui is updated.
   *
   * @param friend the username of the friend to unfollow
   * @param currentUserId the ID of the current user
   */
  fun unfollowFriend(friend: String, currentUserId: String) {
    val currentFriends = _uiState.value.friends
    val updatedFriends = currentFriends.filter { it != friend }
    _uiState.value = _uiState.value.copy(friends = updatedFriends)
    viewModelScope.launch {
      try {
        repository.deleteFriend(friend, currentUserId)
        refreshFriends(currentUserId)
      } catch (e: Exception) {
        _uiState.value =
            _uiState.value.copy(friends = currentFriends, errorMsg = "Error failed to unfollow.")
      }
    }
  }

  /**
   * Handles user following a new friend. Should be deleted when ui is updated.
   *
   * @param friend the username of the friend to follow
   * @param currentUserId the ID of the current user
   */
  fun followFriend(friend: String, currentUserId: String) {
    val currentFriends = _uiState.value.friends
    val updatedFriends =
        if (currentFriends.contains(friend)) {
          currentFriends
        } else {
          currentFriends + friend
        }
    _uiState.value = _uiState.value.copy(friends = updatedFriends)
    viewModelScope.launch {
      try {
        repository.addFriend(friend, currentUserId)
        refreshFriends(currentUserId)
      } catch (e: Exception) {
        _uiState.value =
            _uiState.value.copy(friends = currentFriends, errorMsg = "Error failed to follow.")
      }
    }
  }

  /**
   * Sends a friend request notification to another user.
   *
   * @param friendUserId the UID of the user receiving the request
   * @param currentUserId the UID of the sender
   */
  fun sendFriendRequest(friendUserId: String, currentUserId: String) {
    viewModelScope.launch {
      try {
        if (friendUserId == currentUserId) {
          _uiState.value = _uiState.value.copy(errorMsg = "Cannot friend yourself")
          return@launch
        }
        val friendProfile = repository.getProfileByUid(friendUserId)
        if (friendProfile == null) {
          _uiState.value = _uiState.value.copy(errorMsg = "Friend profile not found")
          return@launch
        }

        if (_uiState.value.friends.contains(friendProfile.username)) return@launch
        if (_uiState.value.pendingSentUsernames.contains(friendProfile.username)) return@launch

        repository.addPendingSentFriendUid(currentUserId, friendUserId)

        val newId = notificationsRepository.getNewId()
        notificationsRepository.addNotification(
            Notification(
                id = newId,
                type = NotificationType.FRIEND_REQUEST,
                emissionTime = Timestamp.now(),
                senderId = currentUserId,
                relatedEntityId = null,
                recipientId = friendUserId,
                wasRead = false))
        refreshFriends(currentUserId)
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(errorMsg = "Failed to send friend request")
      }
    }
  }

  /**
   * Removes an existing friend.
   *
   * @param friendUserId UID of the friend to remove
   * @param currentUserId UID of the user performing the removal
   */
  fun removeFriend(friendUserId: String, currentUserId: String) {
    viewModelScope.launch {
      try {
        val friendProfile = repository.getProfileByUid(friendUserId)
        if (friendProfile == null) {
          _uiState.value = _uiState.value.copy(errorMsg = "Friend profile not found")
          return@launch
        }
        if (!_uiState.value.friends.contains(friendProfile.username)) {
          _uiState.value = _uiState.value.copy(errorMsg = "Cannot remove non-friend")
          return@launch
        }

        repository.deleteFriend(friendProfile.username, currentUserId)

        val newId = notificationsRepository.getNewId()
        notificationsRepository.addNotification(
            Notification(
                id = newId,
                type = NotificationType.REMOVE_FRIEND,
                emissionTime = Timestamp.now(),
                senderId = currentUserId,
                recipientId = friendUserId,
                relatedEntityId = null,
                wasRead = false))

        refreshFriends(currentUserId)
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(errorMsg = "Failed to remove friend")
      }
    }
  }

  /**
   * Cancels a previously sent friend request by sending a cancel notification.
   *
   * @param recipientId UID of the user who originally received the request
   * @param currentUserId UID of the sender who is cancelling
   */
  fun cancelPendingFriendRequest(recipientId: String, currentUserId: String) {
    viewModelScope.launch {
      try {
        val friendProfile = repository.getProfileByUid(recipientId)
        if (friendProfile == null) {
          _uiState.value = _uiState.value.copy(errorMsg = "Profile not found")
          return@launch
        }
        repository.removePendingSentFriendUid(currentUserId, recipientId)
        val newId = notificationsRepository.getNewId()
        notificationsRepository.addNotification(
            Notification(
                id = newId,
                type = NotificationType.FRIEND_REQUEST_CANCELLED,
                emissionTime = Timestamp.now(),
                senderId = currentUserId,
                recipientId = recipientId,
                relatedEntityId = null,
                wasRead = false))
        refreshFriends(currentUserId)
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(errorMsg = "Failed to cancel friend request")
      }
    }
  }

  /**
   * Companion Object used to encapsulate a static method to retrieve a ViewModelProvider.Factory
   * and its default dependencies.
   */
  companion object {
    fun provideFactory(
        profileRepository: ProfileRepository =
            ProfileRepositoryFirestore(Firebase.firestore, Firebase.storage),
        notificationsRepository: NotificationsRepository =
            NotificationsRepositoryProvider.repository,
        currentUserId: String = Firebase.auth.currentUser?.uid ?: ""
    ): ViewModelProvider.Factory {
      return GenericViewModelFactory {
        FriendsViewModel(profileRepository, notificationsRepository)
      }
    }
  }
}
