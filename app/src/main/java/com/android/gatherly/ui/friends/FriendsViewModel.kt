package com.android.gatherly.ui.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.profile.ProfileRepositoryFirestore
import com.android.gatherly.utils.GenericViewModelFactory
import com.android.gatherly.utils.addFriend
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FriendsUIState(
    val errorMsg: String? = null,
    val friends: List<String> = emptyList(),
    val listNoFriends: List<String> = emptyList(),
    val currentUserId: String = "",
    val isLoading: Boolean = true,
    val profiles: Map<String, Profile> = emptyMap()
)

class FriendsViewModel(
    private val repository: ProfileRepository,
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
      val friendsData = repository.getFriendsAndNonFriendsUsernames(currentUserId)

      val allUsernames = friendsData.friendUsernames + friendsData.nonFriendUsernames
      val profiles =
          allUsernames
              .mapNotNull { username -> repository.getProfileByUsername(username) }
              .associateBy { it.username }

      _uiState.value =
          _uiState.value.copy(
              friends = friendsData.friendUsernames,
              listNoFriends = friendsData.nonFriendUsernames,
              profiles = profiles,
              errorMsg = null,
              currentUserId = currentUserId,
              isLoading = false)
    } catch (e: Exception) {
      _uiState.value = _uiState.value.copy(errorMsg = "Failed to load friends: ${e.message}")
    }
  }

  /**
   * Handles user unfollowing a friend.
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
   * Handles user following a new friend.
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
        addFriend(repository, friend, currentUserId)
        refreshFriends(currentUserId)
      } catch (e: Exception) {
        _uiState.value =
            _uiState.value.copy(friends = currentFriends, errorMsg = "Error failed to follow.")
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
        currentUserId: String = Firebase.auth.currentUser?.uid ?: ""
    ): ViewModelProvider.Factory {
      return GenericViewModelFactory { FriendsViewModel(profileRepository) }
    }
  }
}
