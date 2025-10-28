package com.android.gatherly.ui.friends

import android.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FriendsUIState(
  val errorMsg: String? = null,
  val friends: List<String> = emptyList(),
  val listNoFriends: List<String> = emptyList()
)

class FriendsViewModel(private val repository: ProfileRepository, val currentUserId: String) :
  ViewModel() {

  /** StateFlow that emits the current UI state for the Friends screen. */
  private val _uiState = MutableStateFlow(FriendsUIState())
  val uiState: StateFlow<FriendsUIState> = _uiState.asStateFlow()

  /**
   * Initializes the ViewModel by loading all the friends' profile from the repository and filtering
   * them to display only drawable todos.
   */
  init {
    viewModelScope.launch { refreshFriends(currentUserId) }
  }

  /**
   * Refreshes the friends lists based on the current user ID.
   *
   * @param currentUserId the ID of the current user
   */
   suspend fun refreshFriends(currentUserId: String) {
    val profile = repository.getProfileByUid(currentUserId)
    _uiState.value =
      _uiState.value.copy(
        friends = profile?.friendUids ?: throw Exception("FriendsVM: Profile not found"),
        listNoFriends = repository.getListNoFriends(currentUserId))
  }

  /**
   * Handles user unfollowing a friend.
   *
   * @param friend the username of the friend to unfollow
   * @param currentUserId the ID of the current user
   */
  fun unfollowFriend(friend: String, currentUserId: String) {
    viewModelScope.launch {
      repository.deleteFriend(friend, currentUserId)
      refreshFriends(currentUserId)
    }
  }

  /**
   * Handles user following a new friend.
   *
   * @param friend the username of the friend to follow
   * @param currentUserId the ID of the current user
   */
  fun followFriend(friend: String, currentUserId: String) {
    viewModelScope.launch {
      repository.addFriend(friend, currentUserId)
      refreshFriends(currentUserId)
    }
  }

  /**
   * Helper function to get the profilePicture of a friend by their username.
   *
   * @param friend the username of the friend
   * @return the profile picture URL of the friend, or a default if not found
   */
  fun getFriendProfilePicture(friend: String): String {
    var profilePicture = R.drawable.ic_menu_report_image.toString()
    viewModelScope.launch {
      val profile: Profile? = repository.getProfileByUsername(friend)
      if (profile != null) {
        profilePicture = profile.profilePicture
      }
    }
    return profilePicture
  }
}
