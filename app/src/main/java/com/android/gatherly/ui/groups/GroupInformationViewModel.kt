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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State for the groups information screen
 *
 * @param group the group to display
 * @param memberProfiles the list of member profiles
 * @param isAdmin true if the current user is an admin of the group
 * @param isLoading true if the screen is loading
 * @param errorMessage an error message, null if there is none
 */
data class GroupInformationUIState(
    val group: Group = Group(),
    val memberProfiles: List<Profile> = emptyList(),
    val isAdmin: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val navigateToOverview: Boolean = false
)

/**
 * Viewmodel for the groups information screen
 *
 * @param groupsRepository to access the group to display
 * @param profileRepository to access members profile information
 * @param authProvider to access the current user
 */
class GroupInformationViewModel(
    private val groupsRepository: GroupsRepository = GroupsRepositoryFirestore(Firebase.firestore),
    private val profileRepository: ProfileRepository =
        ProfileRepositoryFirestore(Firebase.firestore, Firebase.storage),
    private val authProvider: () -> FirebaseAuth = { Firebase.auth }
) : ViewModel() {

  private val _uiState = MutableStateFlow(GroupInformationUIState())
  val uiState: StateFlow<GroupInformationUIState> = _uiState.asStateFlow()

  /**
   * Load a group to the UI
   *
   * @param groupId the is of the group to load
   */
  fun loadUIState(groupId: String) {
    _uiState.value = _uiState.value.copy(isLoading = true)
    viewModelScope.launch {
      try {
        val group = groupsRepository.getGroup(groupId)

        val membersProfile = mutableListOf<Profile>()
        for (member in group.memberIds) {
          val profile = profileRepository.getProfileByUid(member)!!
          membersProfile.add(profile)
        }

        val isAdmin = group.adminIds.contains(authProvider().currentUser?.uid!!)

        _uiState.value =
            _uiState.value.copy(
                group = group,
                memberProfiles = membersProfile,
                isAdmin = isAdmin,
                isLoading = false)
      } catch (e: Exception) {
        _uiState.value =
            _uiState.value.copy(
                isLoading = false, errorMessage = "Error loading the UI: ${e.message}")
      }
    }
  }

  /**
   * If the current user wants to leave the group, remove them, and navigate back to the overview
   * screen
   */
  fun onLeaveGroup() {
    viewModelScope.launch {
      groupsRepository.removeMember(uiState.value.group.gid, authProvider().currentUser?.uid!!)
      println("just before true")
      _uiState.value = _uiState.value.copy(navigateToOverview = true)
    }
  }

  /** Clears the UI's error message after displaying it */
  fun clearErrorMessage() {
    _uiState.value = _uiState.value.copy(errorMessage = null)
  }
}
