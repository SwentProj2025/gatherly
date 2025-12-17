package com.android.gatherly.ui.friends

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.android.gatherly.R
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu_Goback
import com.android.gatherly.utils.profilePicturePainter

object FriendsScreenTestTags {
  const val BUTTON_FIND_FRIENDS = "buttonFindFriends"
  const val SEARCH_FRIENDS_BAR = "searchBarFriends"
  const val EMPTY_LIST_MSG = "messageEmptyList"
  const val LOADING_ANIMATION = "loadingAnimation"
  const val HEART_BREAK_ANIMATION = "heartBreakAnimation"
  const val UNFRIENDING_TEXT_ANIMATION = "unfriendingTextAnimation"
  const val FRIENDS_SECTION_TITLE = "friendsSectionTitle"
  const val PENDING_SECTION_TITLE = "pendingSectionTitle"

  /**
   * Returns a unique test tag for the card or container representing a given [Profile.username]
   * item.
   *
   * @param friend The [Profile.username] item of a chosen friend whose test tag will be generated.
   * @return A string uniquely identifying the Friend username item in the UI.
   */
  fun getTestTagForFriendItem(friend: String): String = "friendItem${friend}"

  /**
   * Returns a unique test tag for the card or container representing a given [Profile.username]
   * item.
   *
   * @param pendingFriend The [Profile.username] item of a chosen pending friend whose test tag will
   *   be generated.
   * @return A string uniquely identifying the pending friend username item in the UI.
   */
  fun getTestTagForPendingFriendItem(pendingFriend: String): String =
      "pendingFriendItem${pendingFriend}"

  /**
   * Returns a unique test tag for the card container representing a given [Profile.username] item.
   *
   * @param friend The [Profile.username] TExt item of a chosen friend whose test tag will be
   *   generated.
   * @return A string uniquely identifying the Friend username Text item in the UI.
   */
  fun getTestTagForFriendUsername(friend: String): String = "friendUsername${friend}"

  /**
   * Returns a unique test tag for the text in the card container representing a given
   * [Profile.username] item.
   *
   * @param pendingFriend The [Profile.username] Text item of a chosen pending friend whose test tag
   *   will be generated.
   * @return A string uniquely identifying the pending friend username Text item in the UI.
   */
  fun getTestTagForPendingFriendUsername(pendingFriend: String): String =
      "pendingFriendUsername${pendingFriend}"

  /**
   * Returns a unique test tag for the card or container representing a given
   * [Profile.profilePicture] item.
   *
   * @param friend The [Profile.profilePicture] item of a chosen friend [Profile.username] whose
   *   test tag will be generated.
   * @return A string uniquely identifying the Friend username item in the UI.
   */
  fun getTestTagForFriendProfilePicture(friend: String): String = "friendProfilePicture${friend}"

  /**
   * Returns a unique test tag for the card or container representing a given
   * [Profile.profilePicture] item.
   *
   * @param pendingFriend The [Profile.profilePicture] item of a chosen pending friend
   *   [Profile.username] whose test tag will be generated.
   * @return A string uniquely identifying the pending friend username item in the UI.
   */
  fun getTestTagForPendingFriendProfilePicture(pendingFriend: String): String =
      "pendingFriendProfilePicture${pendingFriend}"

  /**
   * Returns a unique test tag for the card or container representing a given [Profile.username]
   * item.
   *
   * @param friend The [Button] item for unfriending button whose test tag will be generated.
   * @return A string uniquely identifying the Friend username item in the UI.
   */
  fun getTestTagForFriendUnfriendButton(friend: String): String = "friendUnfriendingButton${friend}"

  /**
   * Returns a unique test tag for the [Button] used to cancel a friend request. item.
   *
   * @param friend The [Button] item for canceling a friend request whose test tag will be
   *   generated.
   * @return A string uniquely identifying the pending friend username item in the UI.
   */
  fun getTestTagForPendingFriendCancelRequestButton(friend: String): String =
      "pendingFriendCancelRequestButton${friend}"
}

// Private values with the json animation files
private val ANIMATION_BROKEN_HEART = R.raw.broken_heart
private val ANIMATION_LOADING = R.raw.loading_profiles
private const val ANIMATION_TIME = 3000
private const val ANIMATION_LOADING_DELAY: Long = 2000

/**
 * Friends screen showing the current user's friends and pending outgoing friend requests.
 *
 * Features:
 * - Search bar to filter friends and pending requests.
 * - Two sections: friends and pending requests (only shown when non-empty).
 * - Actions to unfriend an existing friend or cancel a pending request.
 * - Navigation to the "Find Friends" screen.
 * - A temporary Lottie animation message shown after an unfriend action.
 *
 * UI testing:
 * - Uses stable test tags from [FriendsScreenTestTags] for key UI elements.
 *
 * @param friendsViewModel ViewModel providing friends data and actions. Defaults to the standard
 *   factory instance.
 * @param goBack Callback invoked when the top bar back arrow is pressed.
 * @param onFindFriends Callback invoked when the user presses the "Find friends" button.
 * @param onClickFriend Callback invoked when a friend row is tapped (opens that profile).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FriendsScreen(
    friendsViewModel: FriendsViewModel = viewModel(factory = FriendsViewModel.provideFactory()),
    goBack: () -> Unit = {},
    onFindFriends: () -> Unit = {},
    onClickFriend: (Profile) -> Unit = {}
) {

  // Retrieve the necessary values for the implementation from the ViewModel
  val uiState by friendsViewModel.uiState.collectAsState()

  val currentUserIdFromVM = uiState.currentUserId

  // Holds the current text entered by the friend username in the search bar
  var searchQuery by rememberSaveable { mutableStateOf("") }

  // Holds the boolean that determines when to trigger the animation
  // after the current user unfriends a profile
  var showUnfriendMessage by rememberSaveable { mutableStateOf(false) }

  // Holds the text displayed during the unfriend animation
  val messageText = stringResource(R.string.friends_unfriend_message)

  // Value used to determine when the profile loading animation should appear
  val isLoading = uiState.isLoading && !showUnfriendMessage

  // Update the list depending on whether the current user types something in the search bar
  LaunchedEffect(searchQuery) { friendsViewModel.searchFriends(searchQuery) }

  val filteredFriends = uiState.friends
  val filteredPendingRequests = uiState.pendingSentUsernames

  // Refresh friends profile when there is an update in the current user profile
  LaunchedEffect(currentUserIdFromVM) {
    if (currentUserIdFromVM.isNotBlank()) {
      friendsViewModel.refreshFriends(currentUserIdFromVM)
    }
  }

  // Triggers the temporary message box when needed
  LaunchedEffect(showUnfriendMessage) {
    if (showUnfriendMessage) {
      kotlinx.coroutines.delay(ANIMATION_LOADING_DELAY)
      showUnfriendMessage = false
    }
  }

  // --- PART SCAFFOLD COMPOSE ---
  Scaffold(
      topBar = {
        TopNavigationMenu_Goback(
            selectedTab = Tab.Friends,
            modifier = Modifier.testTag(NavigationTestTags.TOP_NAVIGATION_MENU),
            goBack = goBack)
      },
      content = { padding ->
        Box(modifier = Modifier.fillMaxSize()) {

          // --- LOADING PROFILE ANIMATION ---
          if (isLoading) {
            LoadingAnimationContent(padding)
          } else {

            // --- SHOWING FRIENDS PROFILES ITEMS ---
            FriendsListContent(
                padding = padding,
                data =
                    FriendsListData(
                        filteredFriends = filteredFriends,
                        filteredPendingRequests = filteredPendingRequests,
                        searchQuery = searchQuery,
                        profiles = uiState.profiles),
                actions =
                    FriendsListActions(
                        onSearchQueryChange = { searchQuery = it },
                        onUnfriend = { username ->
                          val friendUid =
                              uiState.profiles[username]?.uid ?: return@FriendsListActions
                          friendsViewModel.removeFriend(
                              friendUserId = friendUid, currentUserId = currentUserIdFromVM)
                          showUnfriendMessage = true
                        },
                        onCancel = { username ->
                          val recipientUid =
                              uiState.profiles[username]?.uid ?: return@FriendsListActions
                          friendsViewModel.cancelPendingFriendRequest(
                              recipientId = recipientUid, currentUserId = currentUserIdFromVM)
                        },
                        onFindFriends = onFindFriends),
                onClickFriend = onClickFriend)
          }
          // --- UNFRIEND A FRIEND ANIMATION ---
          if (showUnfriendMessage) {
            FloatingMessage(text = messageText, modifier = Modifier.fillMaxSize().padding(padding))
          }
        }
      })
}

/**
 * Helper function : Composable helper that displays a single user profile item in the friends list.
 *
 * @param friend Username of the friend to display.
 * @param unfriend Callback triggered when the "Unfriend" button is clicked.
 * @param modifier Optional [Modifier] for layout customization.
 */
@Composable
private fun FriendItem(
    friend: String,
    unfriend: () -> Unit,
    modifier: Modifier = Modifier,
    profilePicUrl: String? = null,
    onClickFriend: () -> Unit
) {
  Card(
      border =
          BorderStroke(
              dimensionResource(R.dimen.friends_item_card_border_width),
              MaterialTheme.colorScheme.onSurfaceVariant),
      shape = RoundedCornerShape(dimensionResource(R.dimen.friends_item_card_rounded_corner_shape)),
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant,
              contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
      modifier =
          modifier
              .testTag(FriendsScreenTestTags.getTestTagForFriendItem(friend))
              .fillMaxWidth()
              .padding(vertical = dimensionResource(R.dimen.friends_item_card_padding_vertical))) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(dimensionResource(R.dimen.friends_item_card_padding)),
            verticalAlignment = Alignment.CenterVertically,
        ) {

          // -- Profile Picture --
          Image(
              painter = profilePicturePainter(profilePicUrl),
              contentDescription = "Profile picture of $friend",
              contentScale = ContentScale.Crop,
              modifier =
                  Modifier.size(dimensionResource(R.dimen.friends_item_profile_picture_size))
                      .clip(CircleShape)
                      .testTag(FriendsScreenTestTags.getTestTagForFriendProfilePicture(friend)))

          // -- SPACER
          Spacer(
              modifier =
                  Modifier.width(dimensionResource(R.dimen.spacing_between_fields_smaller_regular)))

          Column(modifier = Modifier.weight(1f)) {
            // -- Username Text --
            Text(
                text = friend,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                modifier =
                    Modifier.testTag(FriendsScreenTestTags.getTestTagForFriendUsername(friend))
                        .clickable { onClickFriend() })
          }
          // -- SPACER
          Spacer(
              modifier = Modifier.width(dimensionResource(R.dimen.spacing_between_fields_regular)))

          // -- Unfriend button --
          Button(
              onClick = unfriend,
              modifier =
                  Modifier.wrapContentWidth()
                      .testTag(FriendsScreenTestTags.getTestTagForFriendUnfriendButton(friend))) {
                Text(stringResource(R.string.friends_unfriend_button_title))
              }
        }
      }
}

/**
 * Displays a single pending friend request row.
 *
 * Shows the recipient's profile picture and username, with a button to cancel the pending request.
 * The row uses only design-system colors (surfaceVariant/onSurfaceVariant) and exposes stable test
 * tags for UI tests via [FriendsScreenTestTags].
 *
 * @param friendUsername Username of the user to whom the current user has sent a friend request.
 * @param onCancel Callback invoked when the "Cancel request" button is pressed.
 * @param modifier Optional modifier applied to the outer card container.
 * @param profilePicUrl Optional URL of the pending user's profile picture.
 */
@Composable
private fun PendingRequestItem(
    friendUsername: String,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    profilePicUrl: String?
) {
  Card(
      border =
          BorderStroke(
              dimensionResource(R.dimen.friends_item_card_border_width),
              MaterialTheme.colorScheme.onSurfaceVariant),
      shape = RoundedCornerShape(dimensionResource(R.dimen.friends_item_card_rounded_corner_shape)),
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant,
              contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
      modifier =
          modifier
              .testTag(FriendsScreenTestTags.getTestTagForFriendItem(friendUsername))
              .fillMaxWidth()
              .padding(vertical = dimensionResource(R.dimen.friends_item_card_padding_vertical))) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(dimensionResource(R.dimen.friends_item_card_padding)),
            verticalAlignment = Alignment.CenterVertically,
        ) {

          // -- Placeholder Profile Picture --
          Image(
              painter = profilePicturePainter(profilePicUrl),
              contentDescription = "Profile picture of $friendUsername",
              contentScale = ContentScale.Crop,
              modifier =
                  Modifier.size(dimensionResource(R.dimen.friends_item_profile_picture_size))
                      .clip(CircleShape)
                      .testTag(
                          FriendsScreenTestTags.getTestTagForPendingFriendProfilePicture(
                              friendUsername)))

          // -- SPACER
          Spacer(
              modifier =
                  Modifier.width(dimensionResource(R.dimen.spacing_between_fields_smaller_regular)))

          Column(modifier = Modifier.weight(1f)) {
            // -- Username Text --
            Text(
                text = friendUsername,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                modifier =
                    Modifier.testTag(
                        FriendsScreenTestTags.getTestTagForPendingFriendUsername(friendUsername)))
          }
          // -- SPACER
          Spacer(
              modifier = Modifier.width(dimensionResource(R.dimen.spacing_between_fields_regular)))

          // -- Cancel request button --
          Button(
              onClick = onCancel,
              modifier =
                  Modifier.wrapContentWidth()
                      .testTag(
                          FriendsScreenTestTags.getTestTagForPendingFriendCancelRequestButton(
                              friendUsername))) {
                Text(stringResource(R.string.friends_cancel_request_button_title))
              }
        }
      }
}

// This function contains code generated by an AI (DeepSeek).

/**
 * Helper function: Composable helper that displays a floating message animation when the user
 * unfriend a profile.
 *
 * The animation consists of a Lottie heart breaking animation followed by a message box.
 *
 * @param text Message to display inside the floating box.
 * @param modifier Optional [Modifier] for layout customization.
 */
@Composable
private fun FloatingMessage(text: String, modifier: Modifier = Modifier) {
  val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(ANIMATION_BROKEN_HEART))

  // Show animated message with fade and slide transitions
  AnimatedVisibility(
      visible = true,
      enter = slideInVertically(initialOffsetY = { -it / 2 }) + fadeIn(),
      exit = slideOutVertically(targetOffsetY = { -it / 2 }) + fadeOut(),
      modifier = modifier.zIndex(1f)) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center) {

                // Heart breaking animation (played once)
                LottieAnimation(
                    composition = composition,
                    iterations = 1,
                    speed = 1f,
                    modifier =
                        Modifier.size(dimensionResource(R.dimen.lottie_icon_size_medium))
                            .testTag(FriendsScreenTestTags.HEART_BREAK_ANIMATION))

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))

                // Text message box displayed below the animation
                Card(
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.padding_small)),
                    elevation =
                        CardDefaults.cardElevation(
                            defaultElevation = dimensionResource(R.dimen.padding_extra_small)),
                    modifier =
                        Modifier.padding(bottom = dimensionResource(R.dimen.padding_large))
                            .testTag(FriendsScreenTestTags.UNFRIENDING_TEXT_ANIMATION)) {
                      Text(
                          text = text,
                          color = MaterialTheme.colorScheme.onSecondary,
                          fontSize = 25.sp,
                          fontWeight = FontWeight.Bold,
                          modifier = Modifier.padding(dimensionResource(R.dimen.padding_regular)))
                    }
              }
        }
      }
}

/**
 * Helper function: Composable helper that displays the animation when loading the needed profiles
 * item
 *
 * The animation consists of a Lottie searching waiting screen.
 *
 * @param padding
 */
@Composable
private fun LoadingAnimationContent(padding: PaddingValues) {
  val loadingComposition by
      rememberLottieComposition(LottieCompositionSpec.RawRes(ANIMATION_LOADING))
  Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
    LottieAnimation(
        composition = loadingComposition,
        iterations = LottieConstants.IterateForever,
        modifier =
            Modifier.size(dimensionResource(R.dimen.lottie_icon_size_extra_large))
                .testTag(FriendsScreenTestTags.LOADING_ANIMATION))
  }
}

/** Bundles all UI data needed to render the friends list section. */
data class FriendsListData(
    val filteredFriends: List<String>,
    val filteredPendingRequests: List<String>,
    val searchQuery: String,
    val profiles: Map<String, Profile>
)

/** Bundles all callbacks used by FriendsListContent. */
data class FriendsListActions(
    val onSearchQueryChange: (String) -> Unit,
    val onUnfriend: (String) -> Unit,
    val onCancel: (String) -> Unit,
    val onFindFriends: () -> Unit
)

/**
 * Helper function: Composable helper that display the different items when there exist user
 * profiles to display, the search bar. If there is currently no user available this display a
 * specific message instead.
 *
 * @param padding : PaddingValues from the LazyColumn
 * @param data : all UI data needed to render the friends list section
 * @param actions : all callbacks used by FriendsListContent
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FriendsListContent(
    padding: PaddingValues,
    data: FriendsListData,
    actions: FriendsListActions,
    onClickFriend: (Profile) -> Unit,
) {
  LazyColumn(
      contentPadding = PaddingValues(vertical = dimensionResource(R.dimen.padding_small)),
      modifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = dimensionResource(R.dimen.padding_screen))
              .padding(padding)) {
        item { SearchBarContent(data.searchQuery, actions.onSearchQueryChange) }

        if (data.filteredFriends.isNotEmpty()) {
          item {
            Text(
                text = stringResource(R.string.friends_list_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier =
                    Modifier.padding(vertical = dimensionResource(R.dimen.padding_small))
                        .testTag(FriendsScreenTestTags.FRIENDS_SECTION_TITLE))
          }

          items(items = data.filteredFriends, key = { it }) { friend ->
            val friendProfile = data.profiles[friend] ?: return@items
            FriendItem(
                friend = friend,
                unfriend = { actions.onUnfriend(friend) },

                // -- Animation slide up when an item disappear
                modifier =
                    Modifier.animateItem(
                        fadeInSpec = null,
                        fadeOutSpec = null,
                        placementSpec =
                            tween(durationMillis = ANIMATION_TIME, easing = LinearOutSlowInEasing)),
                profilePicUrl = friendProfile.profilePicture,
                onClickFriend = { onClickFriend(friendProfile) },
            )
          }
        }

        if (data.filteredPendingRequests.isNotEmpty()) {
          item {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
            Text(
                text = stringResource(R.string.friends_pending_list_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier =
                    Modifier.padding(vertical = dimensionResource(R.dimen.padding_small))
                        .testTag(FriendsScreenTestTags.PENDING_SECTION_TITLE))
          }

          items(items = data.filteredPendingRequests, key = { it }) { pendingUsername ->
            PendingRequestItem(
                friendUsername = pendingUsername,
                onCancel = { actions.onCancel(pendingUsername) },

                // -- Animation slide up when an item disappear
                modifier =
                    Modifier.animateItem(
                        fadeInSpec = null,
                        fadeOutSpec = null,
                        placementSpec =
                            tween(durationMillis = ANIMATION_TIME, easing = LinearOutSlowInEasing)),
                profilePicUrl = data.profiles[pendingUsername]?.profilePicture)
          }
        }

        if (data.filteredFriends.isEmpty() && data.filteredPendingRequests.isEmpty()) {
          item {
            Text(
                text = stringResource(R.string.friends_empty_list_msg),
                modifier =
                    Modifier.padding(dimensionResource(R.dimen.padding_screen))
                        .testTag(FriendsScreenTestTags.EMPTY_LIST_MSG))
          }
        }
        item { FindFriendButton(actions.onFindFriends) }
      }
}

/**
 * Helper function: Composable helps to display the search bar
 *
 * @param searchQuery : value written in the OutlinedTextField value
 * @param onSearchQueryChange : remember the changement made in the value
 */
@Composable
private fun SearchBarContent(searchQuery: String, onSearchQueryChange: (String) -> Unit) {
  OutlinedTextField(
      value = searchQuery,
      onValueChange = onSearchQueryChange,
      modifier =
          Modifier.fillMaxWidth()
              .padding(vertical = dimensionResource(R.dimen.padding_small))
              .testTag(FriendsScreenTestTags.SEARCH_FRIENDS_BAR),
      placeholder = {
        Text(
            text = stringResource(R.string.friends_search_bar_label),
            modifier = Modifier.padding(dimensionResource(R.dimen.friends_search_bar_width)))
      },
      singleLine = true,
      shape = RoundedCornerShape(dimensionResource(R.dimen.friends_item_rounded_corner_shape)))
}

/**
 * Helper function: Composable helps to display the button to go to the FindFriends screen
 *
 * @param onFindFriends : function to navigate to the FindFriendsScreen
 */
@Composable
private fun FindFriendButton(onFindFriends: () -> Unit) {
  Button(
      onClick = onFindFriends,
      modifier =
          Modifier.fillMaxWidth()
              .height(dimensionResource(R.dimen.friends_find_button_height))
              .padding(vertical = dimensionResource(R.dimen.friends_find_button_vertical))
              .testTag(FriendsScreenTestTags.BUTTON_FIND_FRIENDS),
      shape = RoundedCornerShape(dimensionResource(R.dimen.friends_item_rounded_corner_shape)),
      colors =
          buttonColors(
              containerColor = MaterialTheme.colorScheme.secondary,
              contentColor = MaterialTheme.colorScheme.onSecondary)) {
        Text(
            text = stringResource(R.string.find_friends_button_label),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium)
      }
}
