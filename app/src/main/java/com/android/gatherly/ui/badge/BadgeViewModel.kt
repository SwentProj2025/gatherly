package com.android.gatherly.ui.badge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

data class UIState(
    val badgeTodoCreated: Triple<String, String, String> =
        Triple(
            "Blank Todo Created Badge",
            "Create your first Todo to get a Badge!",
            "app/src/main/res/drawable/badges/todos/Blank Todo Created.png"),
    val badgeTodoCompleted: Triple<String, String, String> =
        Triple(
            "Blank Todo Completed Badge",
            "Complete your first Todo to get a Badge!",
            "app/src/main/res/drawable/badges/todos/Blank Todo Completed.png"),
    val badgeEventCreated: Triple<String, String, String> =
        Triple(
            "Blank Event Created Badge",
            "Create your first Event to get a Badge!",
            "app/src/main/res/drawable/badges/events/Blank Event Created.png"),
    val badgeEventParticipated: Triple<String, String, String> =
        Triple(
            "Blank Event Participated Badge",
            "Participate to your first Todo to get a Badge!",
            "app/src/main/res/drawable/badges/events/Blank Events.png"),
    val badgeFriendAdded: Triple<String, String, String> =
        Triple(
            "Blank Friend Badge",
            "Add your first Friend to get a Badge!",
            "app/src/main/res/drawable/badges/friends/Blank Friends.png"),
    val badgeFocusSessionCompleted: Triple<String, String, String> =
        Triple(
            "Blank Focus Session Badge",
            "Complete your first Focus Session to get a Badge!",
            "app/src/main/res/drawable/badges/focusSessions/Blank FocusSession.png"),
)

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

  init {
    loadUserBadges()
  }

  fun refresh() {
    loadUserBadges()
  }

  private fun loadUserBadges() {
    viewModelScope.launch {
      val uid = authProvider().currentUser?.uid ?: return@launch
      val profile = repository.getProfileByUid(uid) ?: return@launch

      _uiState.value = buildUiStateFromProfile(profile)
    }
  }

  private fun buildUiStateFromProfile(profile: Profile): UIState {

    val userBadges: List<Badge> =
        profile.badgeIds.mapNotNull { badgeId -> Badge.entries.firstOrNull { it.id == badgeId } }

    fun highestBadgeOfType(type: BadgeType): Badge? =
        userBadges.filter { it.type == type }.maxByOrNull { it.rank.ordinal }

    fun Badge.toTriple(): Triple<String, String, String> =
        Triple(this.title, this.description, this.icon)

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
