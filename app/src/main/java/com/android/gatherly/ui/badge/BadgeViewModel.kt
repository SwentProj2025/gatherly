package com.android.gatherly.ui.badge

import androidx.lifecycle.ViewModel
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.ui.events.UIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UIState(
    val badgeTodoCreated : Triple<String, String, String> = Triple("", "", ""),
    val badgeTodoCompleted: Triple<String, String, String> = Triple("", "", ""),
    val badgeEventCreated : Triple<String, String, String> = Triple("", "", ""),
    val badgeEventCompleted: Triple<String, String, String> = Triple("", "", ""),
    val badgeFriendAdded : Triple<String, String, String> = Triple("", "", ""),
    val badgeFocusSessionCompleted : Triple<String, String, String> = Triple("", "", ""),
)

class BadgeViewModel(
    private val repository: ProfileRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<UIState> = MutableStateFlow(UIState())

    /**
     * StateFlow exposing the current UI state, including all event lists categorized by user
     * relationship.
     */
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()



}