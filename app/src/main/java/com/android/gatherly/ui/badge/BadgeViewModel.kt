package com.android.gatherly.ui.badge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.gatherly.R
import com.android.gatherly.model.badge.Badge
import com.android.gatherly.model.badge.BadgeType
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state for the Badge ViewModel
 *
 * @param badgeTodoCreated the title, description and icon of the highest ranked Badge of type Todo
 *   Created
 * @param badgeTodoCompleted the title, description and icon of the highest ranked Badge of type
 *   Todo Completed
 * @param badgeEventCreated the title, description and icon of the highest ranked Badge of type
 *   Event Created
 * @param badgeEventParticipated the title, description and icon of the highest ranked Badge of type
 *   Event Participated
 * @param badgeFriendAdded the title, description and icon of the highest ranked Badge of type
 *   Friend Added
 * @param badgeFocusSessionCompleted the title, description and icon of the highest ranked Badge of
 *   type Focus Session Completed
 */
data class UIState(
    val badgeTodoCreated: Triple<String, String, Int> =
        Triple(
            "Blank Todo Created Badge",
            "Create your first Todo to get a Badge!",
            R.drawable.blank_todo_created),
    val badgeTodoCompleted: Triple<String, String, Int> =
        Triple(
            "Blank Todo Completed Badge",
            "Complete your first Todo to get a Badge!",
            R.drawable.blank_todo_completed),
    val badgeEventCreated: Triple<String, String, Int> =
        Triple(
            "Blank Event Created Badge",
            "Create your first Event to get a Badge!",
            R.drawable.blank_event_created),
    val badgeEventParticipated: Triple<String, String, Int> =
        Triple(
            "Blank Event Participated Badge",
            "Participate to your first Todo to get a Badge!",
            R.drawable.blank_event_participated),
    val badgeFriendAdded: Triple<String, String, Int> =
        Triple(
            "Blank Friend Badge",
            "Add your first Friend to get a Badge!",
            R.drawable.blank_friends),
    val badgeFocusSessionCompleted: Triple<String, String, Int> =
        Triple(
            "Blank Focus Session Badge",
            "Complete your first Focus Session to get a Badge!",
            R.drawable.blank_friends),
)

/**
 * ViewModel for the Badge screen.
 *
 * @param repository the repository to fetch the list of badgeIds from the profile
 */
class BadgeViewModel(
    private val repository: ProfileRepository,
    private val authProvider: () -> FirebaseAuth = { Firebase.auth }
) : ViewModel() {

  private val _uiState: MutableStateFlow<UIState> = MutableStateFlow(UIState())

  /**
   * StateFlow exposing the current UI state, including all event lists categorized by user
   * relationship.
   */
  val uiState: StateFlow<UIState> = _uiState.asStateFlow()

  /** Initializes the ViewModel by loading the profile of the current user. */
  init {
    loadUserBadges()
  }

  /** Manual refresh for tests */
  fun refresh() {
    loadUserBadges()
  }

  /** Load the user's profile and it's badgeIds list to get the UI state */
  private fun loadUserBadges() {
    viewModelScope.launch {
      val uid = authProvider().currentUser?.uid ?: return@launch
      val profile = repository.getProfileByUid(uid) ?: return@launch

      _uiState.value = buildUiStateFromProfile(profile)
    }
  }

  /**
   * Links each badge Id from the user's badgeIds list to the actual Badge and filters the highest
   * ranked badge to send to the UI
   *
   * @param profile the user's profile
   */
  private fun buildUiStateFromProfile(profile: Profile): UIState {

    val userBadges: List<Badge> =
        profile.badgeIds.mapNotNull { badgeId -> Badge.entries.firstOrNull { it.id == badgeId } }

    fun highestBadgeOfType(type: BadgeType): Badge? =
        userBadges.filter { it.type == type }.maxByOrNull { it.rank.ordinal }

    fun Badge.toTriple(): Triple<String, String, Int> =
        Triple(this.title, this.description, this.iconRes)

    val default = UIState()

    return default.copy(
        badgeTodoCreated =
            highestBadgeOfType(BadgeType.TODOS_CREATED)?.toTriple() ?: default.badgeTodoCreated,
        badgeTodoCompleted =
            highestBadgeOfType(BadgeType.TODOS_COMPLETED)?.toTriple() ?: default.badgeTodoCompleted,
        badgeEventCreated =
            highestBadgeOfType(BadgeType.EVENTS_CREATED)?.toTriple() ?: default.badgeEventCreated,
        badgeEventParticipated =
            highestBadgeOfType(BadgeType.EVENTS_PARTICIPATED)?.toTriple()
                ?: default.badgeEventParticipated,
        badgeFriendAdded =
            highestBadgeOfType(BadgeType.FRIENDS_ADDED)?.toTriple() ?: default.badgeFriendAdded,
        badgeFocusSessionCompleted =
            highestBadgeOfType(BadgeType.FOCUS_SESSIONS_COMPLETED)?.toTriple()
                ?: default.badgeFocusSessionCompleted)
  }
}
