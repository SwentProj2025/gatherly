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
import com.android.gatherly.model.badge.Badge
import com.android.gatherly.model.badge.BadgeType
import com.android.gatherly.model.group.Group
import com.android.gatherly.model.group.GroupsRepository
import com.android.gatherly.model.group.GroupsRepositoryProvider
import com.android.gatherly.model.notification.NotificationsRepository
import com.android.gatherly.model.notification.NotificationsRepositoryProvider
import com.android.gatherly.model.points.PointsRepository
import com.android.gatherly.model.points.PointsRepositoryProvider
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.profile.ProfileRepositoryProvider
import com.android.gatherly.model.profile.ProfileStatus
import com.android.gatherly.model.profile.UserStatusManager
import com.android.gatherly.ui.badge.BadgeUI
import com.android.gatherly.utils.getProfileWithSyncedNotifications
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
    val isLoading: Boolean = true,
    val profile: Profile? = null,
    val groupsToMembers: Map<Group, List<Profile>> = emptyMap(),
    val focusPoints: Double = 0.0,
    val errorMessage: String? = null,
    val signedOut: Boolean = false,
    val navigateToInit: Boolean = false,
    val isAnon: Boolean = true,
    val topBadges: Map<BadgeType, BadgeUI> =
        mapOf(
            BadgeType.TODOS_CREATED to
                BadgeUI(
                    "Blank Todo Created Badge",
                    "Create your first Todo to get a Badge!",
                    R.drawable.blank_todo_created),
            BadgeType.TODOS_COMPLETED to
                BadgeUI(
                    "Blank Todo Completed Badge",
                    "Complete your first Todo to get a Badge!",
                    R.drawable.blank_todo_completed),
            BadgeType.EVENTS_CREATED to
                BadgeUI(
                    "Blank Event Created Badge",
                    "Create your first Event to get a Badge!",
                    R.drawable.blank_event_created),
            BadgeType.EVENTS_PARTICIPATED to
                BadgeUI(
                    "Blank Event Participated Badge",
                    "Participate to your first Todo to get a Badge!",
                    R.drawable.blank_event_participated),
            BadgeType.FRIENDS_ADDED to
                BadgeUI(
                    "Blank Friend Badge",
                    "Add your first Friend to get a Badge!",
                    R.drawable.blank_friends),
            BadgeType.FOCUS_SESSIONS_COMPLETED to
                BadgeUI(
                    "Blank Focus Session Badge",
                    "Complete your first Focus Session to get a Badge!",
                    R.drawable.blank_focus_session))
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
 * @param pointsRepository The [PointsRepository] used to fetch user points.
 * @param notificationsRepository The [NotificationsRepository] used to sync friend notifications.
 * @param authProvider A lambda that provides the current [FirebaseAuth] instance.
 * @param userStatusManager The [UserStatusManager] used to update user status on sign-out.
 */
class ProfileViewModel(
    private val profileRepository: ProfileRepository = ProfileRepositoryProvider.repository,
    private val groupsRepository: GroupsRepository = GroupsRepositoryProvider.repository,
    private val pointsRepository: PointsRepository = PointsRepositoryProvider.repository,
    private val notificationsRepository: NotificationsRepository =
        NotificationsRepositoryProvider.repository,
    private val authProvider: () -> FirebaseAuth = { Firebase.auth },
    private val userStatusManager: UserStatusManager =
        UserStatusManager(authProvider(), profileRepository)
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
            getProfileWithSyncedNotifications(
                profileRepository,
                notificationsRepository,
                pointsRepository,
                authProvider().currentUser?.uid!!)
        if (profile == null) {
          _uiState.value =
              _uiState.value.copy(isLoading = false, errorMessage = "Profile not found")
        } else {
          _uiState.value =
              _uiState.value.copy(
                  isLoading = false,
                  profile = profile,
                  focusPoints = profile.focusPoints,
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
   * data.
   *
   * @param context The context used to access resources.
   * @param credentialManager The CredentialManager used to retrieve Google credentials.
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

  /**
   * Initiates sign-out.
   *
   * @param credentialManager The CredentialManager used to clear credentials.
   */
  fun signOut(credentialManager: CredentialManager) {
    viewModelScope.launch {
      userStatusManager.setStatus(ProfileStatus.OFFLINE)
      _uiState.value = _uiState.value.copy(signedOut = true)
      authProvider().signOut()
      credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
  }

  /**
   * Builds the top badges map for the profile screen:
   * - starts from the default blank badges
   * - replaces each entry with the highest ranked badge of that type, if the user has one.
   *
   * @param profile The user's profile containing badge IDs.
   * @return A map of [BadgeType] to [BadgeUI] representing the user's top badges.
   */
  private fun buildUiStateFromProfile(profile: Profile): Map<BadgeType, BadgeUI> {
    val userBadges: List<Badge> =
        profile.badgeIds.mapNotNull { badgeId -> Badge.entries.firstOrNull { it.id == badgeId } }

    fun highestBadgeOfType(type: BadgeType): Badge? =
        userBadges.filter { it.type == type }.maxByOrNull { it.rank.ordinal }

    fun Badge.toBadgeUI(): BadgeUI =
        BadgeUI(title = this.title, description = this.description, icon = this.iconRes)

    val defaultTopBadges = ProfileState().topBadges

    return defaultTopBadges.mapValues { (badgeType, defaultUi) ->
      highestBadgeOfType(badgeType)?.toBadgeUI() ?: defaultUi
    }
  }
}
