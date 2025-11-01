package com.android.gatherly.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.gatherly.model.group.Group
import com.android.gatherly.model.group.GroupsRepository
import com.android.gatherly.model.group.GroupsRepositoryFirestore
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.profile.ProfileRepositoryFirestore
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Represents the UI state of the Add Group screen.
 *
 * Holds user-entered data, validation errors, and progress flags used by [AddGroupViewModel] to
 * manage the process of creating a new Group.
 */
data class AddGroupUiState(
    val name: String = "",
    val description: String = "",
    val nameError: String? = null,
    val friendsList: List<Profile> = emptyList(),
    val selectedFriendIds: List<String> = emptyList(),
    val isFriendsLoading: Boolean = false,
    val friendsError: String? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
)

/**
 * ViewModel responsible for managing the "Add Group" screen.
 *
 * Handles user input updates, field validation, friend selection, and saving Group items to the
 * Firestore repository through [GroupsRepository].
 *
 * @param groupsRepository The repository responsible for persisting Group items.
 * @param profileRepository The repository responsible for fetching user profiles and friends.
 */
class AddGroupViewModel(
    private val groupsRepository: GroupsRepository = GroupsRepositoryFirestore(Firebase.firestore),
    private val profileRepository: ProfileRepository =
        ProfileRepositoryFirestore(Firebase.firestore)
) : ViewModel() {
  private val _uiState = MutableStateFlow(AddGroupUiState())

  /** Public immutable access to the Add Group UI state. */
  val uiState: StateFlow<AddGroupUiState> = _uiState.asStateFlow()

  init {
    loadFriends()
  }

  /** Clears the error message in the UI state. */
  fun clearErrorMsg() {
    _uiState.value = _uiState.value.copy(saveError = null)
  }

  /** Clears the save success flag in the UI state. */
  fun clearSaveSuccess() {
    _uiState.value = _uiState.value.copy(saveSuccess = false)
  }

  /**
   * Updates the name field and validates that it is not blank.
   *
   * @param newValue The new name entered by the user. If blank, a validation error is set.
   */
  fun onNameChanged(newValue: String) {
    _uiState.value =
        _uiState.value.copy(
            name = newValue, nameError = if (newValue.isBlank()) "Name cannot be empty" else null)
  }

  /**
   * Updates the description field.
   *
   * @param newValue The new description entered by the user.
   */
  fun onDescriptionChanged(newValue: String) {
    _uiState.value = _uiState.value.copy(description = newValue)
  }

  /**
   * Loads the current user's friends list from the repository.
   *
   * Fetches the user's profile and then retrieves each friend's profile to populate the friends
   * list in the UI state.
   */
  private fun loadFriends() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isFriendsLoading = true, friendsError = null)
      try {
        val currentUserId =
            Firebase.auth.currentUser?.uid ?: throw IllegalStateException("No signed in user")
        val currentProfile =
            profileRepository.getProfileByUid(currentUserId)
                ?: throw NoSuchElementException("Current user profile not found")

        val friendProfiles =
            currentProfile.friendUids.mapNotNull { friendId ->
              try {
                profileRepository.getProfileByUid(friendId)
              } catch (e: Exception) {
                null // Skip friends that can't be fetched
              }
            }
        _uiState.value = _uiState.value.copy(friendsList = friendProfiles, isFriendsLoading = false)
      } catch (e: Exception) {
        _uiState.value =
            _uiState.value.copy(
                friendsList = emptyList(), friendsError = e.message, isFriendsLoading = false)
      }
    }
  }

  /**
   * Toggles the selection of a friend for the group.
   *
   * @param friendId The ID of the friend to toggle selection for.
   */
  fun onFriendToggled(friendId: String) {
    val currentSelected = _uiState.value.selectedFriendIds
    val newSelected =
        if (friendId in currentSelected) {
          currentSelected - friendId
        } else {
          currentSelected + friendId
        }
    _uiState.value = _uiState.value.copy(selectedFriendIds = newSelected)
  }

  /**
   * Attempts to create and save a new [Group] entry to the repository.
   *
   * Performs field validation before saving, and updates the UI state to reflect loading, success,
   * and error states. Selected friends are added as initial members of the group.
   */
  fun saveGroup() {
    val validated =
        _uiState.value.copy(
            nameError = if (_uiState.value.name.isBlank()) "Name cannot be empty" else null)
    _uiState.value = validated

    // Abort if validation failed
    if (_uiState.value.nameError != null) {
      return
    }

    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isSaving = true, saveError = null)
      try {
        val gid = groupsRepository.getNewId()

        val group =
            Group(
                gid = gid,
                creatorId = "", // will be filled by Firestore repo
                name = validated.name,
                description = if (validated.description.isBlank()) null else validated.description,
                memberIds = validated.selectedFriendIds, // selected friends as initial members
                adminIds = emptyList() // creator will be added by Firestore repo
                )

        groupsRepository.addGroup(group)
        _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(isSaving = false, saveError = e.message)
      }
    }
  }
}
