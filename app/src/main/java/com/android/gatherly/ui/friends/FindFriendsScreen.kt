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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import kotlinx.coroutines.delay

object FindFriendsScreenTestTags {
  const val SEARCH_FRIENDS_BAR = "searchBarFriends"
  const val EMPTY_LIST_MSG = "messageEmptyList"

  const val LOADING_ANIMATION = "loadingAnimation"

  const val HEART_ANIMATION = "heartAnimation"

  const val REQUESTING_TEXT_ANIMATION = "requestingTextAnimation"

  /**
   * Returns a unique test tag for the card or container representing a given [Profile.username]
   * item.
   *
   * @param username The [Profile.username] item of a chosen friend whose test tag will be
   *   generated.
   * @return A string uniquely identifying the Friend username item in the UI.
   */
  fun getTestTagForFriendItem(username: String): String = "friendItem${username}"

  /**
   * Returns a unique test tag for the card container representing a given [Profile.username] item.
   *
   * @param username The [Profile.username] Text item of a chosen friend whose test tag will be
   *   generated.
   * @return A string uniquely identifying the Friend username Text item in the UI.
   */
  fun getTestTagForFriendUsername(username: String): String = "friendUsername${username}"

  /**
   * Returns a unique test tag for the card or container representing a given
   * [Profile.profilePicture] item.
   *
   * @param username The username of the friend whose profile picture test tag will be generated.
   * @return A string uniquely identifying the friend's profile picture item in the UI.
   */
  fun getTestTagForFriendProfilePicture(username: String): String =
      "friendProfilePicture${username}"

  /**
   * Returns a unique test tag for the card or container representing a given [Profile.username]
   * item.
   *
   * @param username The [Button] item for requesting button whose test tag will be generated.
   * @return A string uniquely identifying the Friend username item in the UI.
   */
  fun getTestTagForFriendRequestButton(username: String): String =
      "friendRequestingButton${username}"
}

// Private values with the json animation files
private val ANIMATION_HEART = R.raw.heart
private val ANIMATION_LOADING = R.raw.loading_profiles
private const val ANIMATION_TIME = 3000
private const val ANIMATION_LOADING_DELAY: Long = 2000

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FindFriendsScreen(
    friendsViewModel: FriendsViewModel = viewModel(factory = FriendsViewModel.provideFactory()),
    goBack: () -> Unit = {},
) {

  // Retrieve the necessary values for the implementation from the ViewModel

  val uiState by friendsViewModel.uiState.collectAsState()
  val notFriendsList =
      uiState.listNoFriends.filter { username -> !uiState.pendingSentUsernames.contains(username) }
  val currentUserIdFromVM = uiState.currentUserId

  // Holds the current text entered by the user username in the search bar
  var searchQuery by remember { mutableStateOf("") }

  // Holds the boolean that determines when to trigger the animation
  // after the current user requests being friend with a profile
  var showRequestMessage by remember { mutableStateOf(false) }

  // Holds the text displayed during the request animation
  val messageText = stringResource(R.string.friends_request_message)

  // Value used to determine when the profile loading animation should appear
  val isLoading = uiState.isLoading && !showRequestMessage

  // Update the list depending on whether the current user types something in the search bar
  val filteredNotFriends =
      if (searchQuery.isBlank()) {
        notFriendsList
      } else {
        notFriendsList.filter { friend -> friend.contains(searchQuery, ignoreCase = true) }
      }

  // Refresh users profile when there is an update in the current user profile
  LaunchedEffect(currentUserIdFromVM) {
    if (currentUserIdFromVM.isNotBlank()) {
      friendsViewModel.refreshFriends(currentUserIdFromVM)
    }
  }

  // Triggers the temporary message box when needed
  LaunchedEffect(showRequestMessage) {
    if (showRequestMessage) {
      delay(ANIMATION_LOADING_DELAY)
      showRequestMessage = false
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
            // --- SHOWING USERS' PROFILES ITEMS ---
            FriendsListContent(
                padding = padding,
                filteredNotFriends = filteredNotFriends,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onRequestFriend = { username ->
                  val targetUid = uiState.profiles[username]?.uid ?: return@FriendsListContent
                  friendsViewModel.sendFriendRequest(
                      friendUserId = targetUid, currentUserId = currentUserIdFromVM)
                  showRequestMessage = true
                },
                profiles = uiState.profiles)
          }

          // --- REQUEST A FRIEND ANIMATION ---
          if (showRequestMessage) {
            FloatingMessage(text = messageText, modifier = Modifier.fillMaxSize().padding(padding))
          }
        }
      })
}

/**
 * Helper function : Composable helper that displays a single user profile item in the friends list.
 *
 * @param friend Username of the friend to display.
 * @param request Callback triggered when the "Request" button is clicked.
 * @param modifier Optional [Modifier] for layout customization.
 */
@Composable
private fun FriendItem(
    friend: String,
    request: () -> Unit,
    modifier: Modifier = Modifier,
    profilePicUrl: String? = null
) {
  Card(
      border =
          BorderStroke(
              dimensionResource(R.dimen.find_friends_item_card_border_width),
              MaterialTheme.colorScheme.primary),
      shape =
          RoundedCornerShape(
              dimensionResource(R.dimen.find_friends_item_card_rounded_corner_shape)),
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.secondaryContainer,
              contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
      modifier =
          modifier
              .testTag(FindFriendsScreenTestTags.getTestTagForFriendItem(friend))
              .fillMaxWidth()
              .padding(vertical = 4.dp)) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(dimensionResource(R.dimen.find_friends_item_card_padding)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Image(
              painter = profilePicturePainter(profilePicUrl),
              contentDescription = "Profile picture of ${friend}",
              contentScale = ContentScale.Crop,
              modifier =
                  Modifier.size(dimensionResource(R.dimen.find_friends_item_profile_picture_size))
                      .clip(CircleShape)
                      .testTag(FindFriendsScreenTestTags.getTestTagForFriendProfilePicture(friend)))

          // -- SPACER
          Spacer(
              modifier =
                  Modifier.width(dimensionResource(R.dimen.spacing_between_fields_smaller_regular)))

          Column(modifier = Modifier.weight(1f)) {

            // -- Username Text --
            Text(
                text = friend,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                modifier =
                    Modifier.testTag(FindFriendsScreenTestTags.getTestTagForFriendUsername(friend)))
          }

          // -- SPACER
          Spacer(
              modifier = Modifier.width(dimensionResource(R.dimen.spacing_between_fields_regular)))

          // -- Request button --
          Button(
              onClick = request,
              modifier =
                  Modifier.wrapContentWidth()
                      .testTag(
                          FindFriendsScreenTestTags.getTestTagForFriendRequestButton(friend))) {
                Text(stringResource(R.string.friends_request_button_title))
              }
        }
      }
}

// This function contains code generated by an AI (DeepSeek).

/**
 * Helper function: Composable helper that displays a floating message animation when the user
 * requests to be friend with a profile.
 *
 * The animation consists of a Lottie heart animation followed by a message box.
 *
 * @param text Message to display inside the floating box.
 * @param modifier Optional [Modifier] for layout customization.
 */
@Composable
private fun FloatingMessage(text: String, modifier: Modifier = Modifier) {
  val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(ANIMATION_HEART))

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

                // Heart animation (played once)
                LottieAnimation(
                    composition = composition,
                    iterations = 1,
                    speed = 1f,
                    modifier =
                        Modifier.testTag(FindFriendsScreenTestTags.HEART_ANIMATION)
                            .size(dimensionResource(R.dimen.lottie_icon_size_large)))

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
                        Modifier.padding(bottom = dimensionResource(R.dimen.padding_large))) {
                      Text(
                          text = text,
                          color = MaterialTheme.colorScheme.onSecondary,
                          fontSize = 25.sp,
                          fontWeight = FontWeight.Bold,
                          modifier =
                              Modifier.padding(dimensionResource(R.dimen.padding_regular))
                                  .testTag(FindFriendsScreenTestTags.REQUESTING_TEXT_ANIMATION))
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
                .testTag(FindFriendsScreenTestTags.LOADING_ANIMATION))
  }
}

/**
 * Helper function: Composable helper that display the different items when there exist user
 * profiles to display, the search bar. If there is currently no user available this display a
 * specific message instead.
 *
 * @param padding : PaddingValues from the LazyColumn
 * @param filteredNotFriends : List of the profile username that can be friend with the current user
 * @param searchQuery : value written in the search bar
 * @param onSearchQueryChange : function will remember the value written in the search bar
 * @param onRequestFriend : function to save the chosen profile as new friend to the current user
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FriendsListContent(
    padding: PaddingValues,
    filteredNotFriends: List<String>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onRequestFriend: (String) -> Unit,
    profiles: Map<String, Profile>
) {
  LazyColumn(
      contentPadding = PaddingValues(vertical = dimensionResource(R.dimen.padding_small)),
      modifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = dimensionResource(R.dimen.padding_screen))
              .padding(padding)) {

        // --- NO PROFILE ITEM NEED TO BE DISPLAYED ---
        if (filteredNotFriends.isEmpty()) {
          item {
            Text(
                text = stringResource(R.string.find_friends_empty_list_message),
                modifier =
                    Modifier.padding(dimensionResource(R.dimen.padding_screen))
                        .testTag(FindFriendsScreenTestTags.EMPTY_LIST_MSG))
          }
        } else {

          // --- SEARCH BAR ---
          item { SearchBarContent(searchQuery, onSearchQueryChange) }

          // --- USER' ITEM ---

          items(items = filteredNotFriends, key = { it }) { friend ->
            FriendItem(
                friend = friend,
                request = { onRequestFriend(friend) },

                // -- Animation slide up when an item disappear
                modifier =
                    Modifier.animateItemPlacement(
                        animationSpec =
                            tween(durationMillis = ANIMATION_TIME, easing = LinearOutSlowInEasing)),
                profilePicUrl = profiles[friend]?.profilePicture)
          }
        }
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
              .testTag(FindFriendsScreenTestTags.SEARCH_FRIENDS_BAR),
      placeholder = {
        Text(
            text = stringResource(R.string.find_friends_search_bar_label),
            modifier = Modifier.padding(dimensionResource(R.dimen.find_friends_search_bar_width)))
      },
      singleLine = true,
      shape = RoundedCornerShape(dimensionResource(R.dimen.find_friends_item_rounded_corner_shape)))
}
