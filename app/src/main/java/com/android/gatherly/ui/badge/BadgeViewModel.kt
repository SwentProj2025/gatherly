package com.android.gatherly.ui.badge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.gatherly.R
import com.android.gatherly.model.badge.Badge
import com.android.gatherly.model.badge.BadgeRank
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

/** UI state for the Badge ViewModel */
data class UIState(
    val badgesByType: Map<BadgeType, List<BadgeUI>> = emptyMap(),
    val isLoading: Boolean = true,
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
  val lockedBadgeText = "???"

  /** StateFlow exposing the current badge UI state */
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
      val uid =
          authProvider().currentUser?.uid
              ?: run {
                _uiState.value = _uiState.value.copy(isLoading = true, badgesByType = emptyMap())
                return@launch
              }
      val profile = repository.getProfileByUid(uid)
      buildUiStateFromProfile(profile)
    }
  }

  /**
   * Links each badge Id from the user's badgeIds list to the actual Badge and creates a BadgeUI to
   * send to the UI. This function gets all badges obtained by the user to be displayed in the badge
   * screen and also creates blank badgeUIs when more badges are still unobtained by the user.
   *
   * @param profile the user's profile
   */
  private fun buildUiStateFromProfile(profile: Profile?) {
    _uiState.value = _uiState.value.copy(isLoading = true)

    if (profile == null) {
      _uiState.value = _uiState.value.copy(isLoading = false, badgesByType = emptyMap())
    } else {
      val userBadges: List<Badge> =
          profile.badgeIds.mapNotNull { badgeId -> Badge.entries.firstOrNull { it.id == badgeId } }

      val highestRankByType: Map<BadgeType, BadgeRank?> =
          BadgeType.entries.associateWith { type ->
            userBadges.filter { it.type == type }.maxByOrNull { it.rank.ordinal }?.rank
          }

      val badgesByType: Map<BadgeType, List<BadgeUI>> =
          BadgeType.entries.associateWith { type ->
            val highestRank = highestRankByType[type]
            val blankIcon = type.blankIconRes()

            val allBadgesOfType =
                Badge.entries.filter { it.type == type }.sortedBy { it.rank.ordinal }

            val obtainedBadges: List<Badge> =
                if (highestRank == null) {
                  emptyList()
                } else {
                  allBadgesOfType.filter { it.rank.ordinal <= highestRank.ordinal }
                }

            val nextLockedBadge: Badge? =
                if (highestRank == null) {
                  allBadgesOfType.firstOrNull()
                } else {
                  allBadgesOfType.firstOrNull { it.rank.ordinal > highestRank.ordinal }
                }

            val obtainedUi: List<BadgeUI> =
                obtainedBadges.map { badge ->
                  BadgeUI(
                      title = badge.title, description = badge.description, icon = badge.iconRes)
                }

            val lockedUi: BadgeUI? =
                nextLockedBadge?.let {
                  BadgeUI(title = lockedBadgeText, description = lockedBadgeText, icon = blankIcon)
                }

            if (lockedUi != null) obtainedUi + lockedUi else obtainedUi
          }
      _uiState.value = _uiState.value.copy(isLoading = false, badgesByType = badgesByType)
    }
  }

  /**
   * Returns the drawable resource id of the "locked / not yet obtained" badge icon corresponding to
   * this [BadgeType].
   *
   * This is used to display a placeholder badge in the UI when a higher rank badge exists for a
   * type but has not been unlocked by the user yet.
   *
   * @return A drawable resource id for the placeholder icon associated with this badge type.
   * @receiver The [BadgeType] for which we want the placeholder icon.
   */
  private fun BadgeType.blankIconRes(): Int =
      when (this) {
        BadgeType.TODOS_CREATED -> R.drawable.blank_todo_created
        BadgeType.TODOS_COMPLETED -> R.drawable.blank_todo_completed
        BadgeType.EVENTS_CREATED -> R.drawable.blank_event_created
        BadgeType.EVENTS_PARTICIPATED -> R.drawable.blank_event_participated
        BadgeType.FRIENDS_ADDED -> R.drawable.blank_friends
        BadgeType.FOCUS_SESSIONS_COMPLETED -> R.drawable.blank_focus_session
      }
}
