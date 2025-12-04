package com.android.gatherly.ui.profile

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.gatherly.R
import com.android.gatherly.model.group.Group
import com.android.gatherly.model.group.GroupsRepository
import com.android.gatherly.model.group.GroupsRepositoryProvider
import com.android.gatherly.model.badge.Badge
import com.android.gatherly.model.badge.BadgeType
import com.android.gatherly.model.notification.NotificationsRepository
import com.android.gatherly.model.notification.NotificationsRepositoryProvider
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.profile.ProfileRepositoryProvider
import com.android.gatherly.ui.badge.BadgeUI
import com.android.gatherly.ui.badge.UIState
import com.android.gatherly.utils.getProfileWithSyncedFriendNotifications
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/** Represents the UI state of the Profile screen. */
data class ProfileState(
    val isLoading: Boolean = false,
    val profile: Profile? = null,
    val focusPoints: Int = 0,
    val groupsToMembers: Map<Group, List<Profile>> = emptyMap(),
    val focusPoints: Double = 0.0,
    val errorMessage: String? = null,
    val signedOut: Boolean = false,
    val navigateToInit: Boolean = false,
    val isAnon: Boolean = true,
    val topBadges: Map<BadgeType, BadgeUI> = UIState().topBadges
)

/**
 * ViewModel responsible for fetching the authenticated user's [Profile].
 *
 * This ViewModel interacts with [ProfileRepository] to:
 * - Load the authenticated user's profile from Firestore.
 *
 * Handle user sign-out action.
 *
 * The UI observes [uiState] to react to updates in [Profile] data, loading, or errors.
 *
 * @param profileRepository The [ProfileRepository] used to interact with Firestore.
 * @param groupsRepository The [GroupsRepository] used to fetch user groups.
 * @param authProvider A lambda that provides the current [FirebaseAuth] instance.
 */
class ProfileViewModel(
    private val profileRepository: ProfileRepository = ProfileRepositoryProvider.repository,
    private val groupsRepository: GroupsRepository = GroupsRepositoryProvider.repository,
    private val repository: ProfileRepository = ProfileRepositoryProvider.repository,
    private val notificationsRepository: NotificationsRepository =
        NotificationsRepositoryProvider.repository,
    private val authProvider: () -> FirebaseAuth = { Firebase.auth }
) : ViewModel() {

  private val _uiState = MutableStateFlow(ProfileState())

  /** Exposes the immutable UI state of the Profile screen. */
  val uiState: StateFlow<ProfileState> = _uiState

  /**
   * Loads the currently authenticated user's [Profile] from Firestore.
   *
   * Assumes that a [Profile] already exists (it should be created at sign-in). If the [Profile] is
   * missing, sets an appropriate error state.
   */
  fun loadUserProfile() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

      val user = authProvider().currentUser
      if (user == null) {
        _uiState.value =
            _uiState.value.copy(isLoading = false, errorMessage = "User not authenticated")
        return@launch
      }

      _uiState.value = _uiState.value.copy(isAnon = authProvider().currentUser?.isAnonymous ?: true)

      try {
        val profile =
            getProfileWithSyncedFriendNotifications(
                repository, notificationsRepository, authProvider().currentUser?.uid!!)
        if (profile == null) {
          _uiState.value =
              _uiState.value.copy(isLoading = false, errorMessage = "Profile not found")
        } else {
          _uiState.value =
              _uiState.value.copy(
                  isLoading = false,
                  profile = profile,
                  topBadges = buildUiStateFromProfile(profile))
        }
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
      }
    }
  }

  /** Loads the groups the user is a member of along with their members' profiles. */
  fun loadUserGroups() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
      try {
        // Fetch all user groups
        val groups = groupsRepository.getUserGroups()
        val updatedMap = mutableMapOf<Group, List<Profile>>()
        groups.forEach { group ->
          // Fetch member profiles for each group
          val groupsMembersProfile = mutableListOf<Profile>()
          group.memberIds.forEach { memberId ->
            // Fetch profile for each member
            val memberProfile = profileRepository.getProfileByUid(memberId)
            if (memberProfile != null) {
              groupsMembersProfile.add(memberProfile)
            }
          }
          updatedMap.put(group, groupsMembersProfile)
        }
        _uiState.value = _uiState.value.copy(groupsToMembers = updatedMap, isLoading = false)
      } catch (_: Exception) {
        _uiState.value =
            _uiState.value.copy(
                groupsToMembers = emptyMap(),
                isLoading = false,
                errorMessage = "Failed to load groups")
      }
    }
  }

  /**
   * Upgrades the user's current account to a Google account. The user keeps all of their current
   * data
   */
  fun upgradeWithGoogle(context: Context, credentialManager: CredentialManager) {
    viewModelScope.launch {
      try {
        val signInWithGoogleOption =
            GetSignInWithGoogleOption.Builder(
                    serverClientId = context.getString(R.string.web_client_id))
                .build()

        val request =
            GetCredentialRequest.Builder().addCredentialOption(signInWithGoogleOption).build()

        val result = credentialManager.getCredential(request = request, context = context)

        if (result.credential is CustomCredential &&
            result.credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
          try {
            // Create idToken
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
            val idToken = googleIdTokenCredential.idToken

            // Authenticate to Firebase
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            Firebase.auth.currentUser!!.linkWithCredential(firebaseCredential).await()
            val uid = Firebase.auth.currentUser?.uid ?: return@launch

            // Initialize profile in profileRepository
            profileRepository.initProfileIfMissing(uid, "")

            // Navigate to init profile
            _uiState.value = _uiState.value.copy(navigateToInit = true)
          } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(errorMessage = "Google sign-in failed")
            Log.e("SignInViewModel", "Google sign-in failed", e)
          }
        } else {
          _uiState.value =
              _uiState.value.copy(errorMessage = "Failed to recognize Google credentials")
          Log.e("Google credentials", "Failed to recognize Google credentials")
        }
      } catch (e: NoCredentialException) {
        _uiState.value = _uiState.value.copy(errorMessage = "No Google credentials")
        Log.e("Google authentication", e.message.orEmpty())
      } catch (e: GetCredentialException) {
        _uiState.value = _uiState.value.copy(errorMessage = "Failed to get Google credentials")
        Log.e("Google authentication", e.message.orEmpty())
      }
    }
  }

  /** Initiates sign-out */
  fun signOut(credentialManager: CredentialManager): Unit {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(signedOut = true)
      Firebase.auth.signOut()
      credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
  }

  /**
   * Builds the top badges map for the profile screen:
   * - starts from the default blank badges (from UIState)
   * - replaces each entry with the highest ranked badge of that type, if the user has one
   */
  private fun buildUiStateFromProfile(profile: Profile): Map<BadgeType, BadgeUI> {
    val userBadges: List<Badge> =
        profile.badgeIds.mapNotNull { badgeId -> Badge.entries.firstOrNull { it.id == badgeId } }

    fun highestBadgeOfType(type: BadgeType): Badge? =
        userBadges.filter { it.type == type }.maxByOrNull { it.rank.ordinal }

    fun Badge.toBadgeUI(): BadgeUI =
        BadgeUI(title = this.title, description = this.description, icon = this.iconRes)

    val defaultTopBadges = UIState().topBadges

    return defaultTopBadges.mapValues { (badgeType, defaultUi) ->
      highestBadgeOfType(badgeType)?.toBadgeUI() ?: defaultUi
    }
  }
}
