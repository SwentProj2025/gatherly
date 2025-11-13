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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
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

object FriendsScreenTestTags {
  const val BUTTON_FIND_FRIENDS = "buttonFindFriends"
  const val SEARCH_FRIENDS_BAR = "searchBarFriends"
  const val EMPTY_LIST_MSG = "messageEmptyList"
  const val LOADING_ANIMATION = "loadingAnimation"
  const val HEART_BREAK_ANIMATION = "heartBreakAnimation"

  const val UNFOLLOWING_TEXT_ANIMATION = "unfollowingTextAnimation"

  /**
   * Returns a unique test tag for the card or container representing a given [Profile.username]
   * item.
   *
   * @param friend The [Profile.username] item of a chosen friend whose test tag will be generated.
   * @return A string uniquely identifying the Friend username item in the UI.
   */
  fun getTestTagForFriendItem(friend: String): String = "friendItem${friend}"

  /**
   * Returns a unique test tag for the card container representing a given [Profile.username] item.
   *
   * @param friend The [Profile.username] TExt item of a chosen friend whose test tag will be
   *   generated.
   * @return A string uniquely identifying the Friend username Text item in the UI.
   */
  fun getTestTagForFriendUsername(friend: String): String = "friendUsername${friend}"

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
   * Returns a unique test tag for the card or container representing a given [Profile.username]
   * item.
   *
   * @param friend The [Button] item for unfollowing button whose test tag will be generated.
   * @return A string uniquely identifying the Friend username item in the UI.
   */
  fun getTestTagForFriendUnfollowButton(friend: String): String = "friendUnfollowingButton${friend}"
}

// Private values with the json animation files
private val ANIMATION_BROKEN_HEART = R.raw.broken_heart
private val ANIMATION_LOADING = R.raw.loading_profiles

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FriendsScreen(
    friendsViewModel: FriendsViewModel = viewModel(factory = FriendsViewModel.provideFactory()),
    goBack: () -> Unit = {},
    onFindFriends: () -> Unit = {},
) {

  // Retrieve the necessary values for the implementation from the ViewModel
  val uiState by friendsViewModel.uiState.collectAsState()
  val friendsList = uiState.friends
  val currentUserIdFromVM = uiState.currentUserId

  // Holds the current text entered by the friend username in the search bar
  var searchQuery by remember { mutableStateOf("") }

  // Update the list depending on whether the current user types something in the search bar
  val filteredFriends =
      if (searchQuery.isBlank()) {
        friendsList
      } else {
        friendsList.filter { friend -> friend.contains(searchQuery, ignoreCase = true) }
      }

  // Refresh friends profile when there is an update in the current user profile
  LaunchedEffect(currentUserIdFromVM) {
    if (currentUserIdFromVM.isNotBlank()) {
      friendsViewModel.refreshFriends(currentUserIdFromVM)
    }
  }

  // Holds the boolean that determines when to trigger the animation
  // after the current user unfollows a profile
  var showUnfollowMessage by remember { mutableStateOf(false) }

  // Holds the text displayed during the unfollow animation
  val messageText = stringResource(R.string.friends_unfollow_message)

  // Triggers the temporary message box when needed
  LaunchedEffect(showUnfollowMessage) {
    if (showUnfollowMessage) {
      kotlinx.coroutines.delay(2000)
      showUnfollowMessage = false
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
        // Value used to determine when the profile loading animation should appear
        val isLoading = uiState.isLoading && !showUnfollowMessage

        Box(modifier = Modifier.fillMaxSize()) {

          // --- LOADING PROFILE ANIMATION ---
          if (isLoading) {
            val loadingComposition by
                rememberLottieComposition(LottieCompositionSpec.RawRes(ANIMATION_LOADING))
            LottieAnimation(
                composition = loadingComposition,
                iterations = LottieConstants.IterateForever,
                modifier =
                    Modifier.size(dimensionResource(R.dimen.lottie_icon_size_extra_large))
                        .align(Alignment.Center)
                        .testTag(FriendsScreenTestTags.LOADING_ANIMATION))
          } else {

            // --- SHOWING FRIENDS PROFILES ITEMS ---
            LazyColumn(
                contentPadding = PaddingValues(vertical = dimensionResource(R.dimen.padding_small)),
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(horizontal = dimensionResource(R.dimen.padding_screen))
                        .padding(padding)) {

                  // --- NO PROFILE ITEM NEED TO BE DISPLAYED ---
                  if (filteredFriends.isEmpty()) {
                    item {
                      Text(
                          text = stringResource(R.string.friends_empty_list_msg),
                          modifier =
                              Modifier.padding(dimensionResource(R.dimen.padding_screen))
                                  .testTag(FriendsScreenTestTags.EMPTY_LIST_MSG))
                    }
                  } else {

                    // --- SEARCH BAR ---
                    item {
                      OutlinedTextField(
                          value = searchQuery,
                          onValueChange = { searchQuery = it },
                          modifier =
                              Modifier.fillMaxWidth()
                                  .padding(vertical = dimensionResource(R.dimen.padding_small))
                                  .testTag(FriendsScreenTestTags.SEARCH_FRIENDS_BAR),
                          placeholder = {
                            Text(
                                text = stringResource(R.string.friends_search_bar_label),
                                modifier =
                                    Modifier.padding(
                                        dimensionResource(R.dimen.friends_search_bar_width)))
                          },
                          singleLine = true,
                          shape =
                              RoundedCornerShape(
                                  dimensionResource(R.dimen.friends_item_rounded_corner_shape)))
                    }

                    // --- FRIENDS' ITEM ---
                    items(items = filteredFriends, key = { it }) { friend ->
                      FriendItem(
                          friend = friend,
                          unfollow = {
                            friendsViewModel.unfollowFriend(
                                currentUserId = currentUserIdFromVM, friend = friend)

                            showUnfollowMessage = true
                          },
                          // -- Animation slide up when an item disappear
                          modifier =
                              Modifier.animateItemPlacement(
                                  animationSpec =
                                      tween(durationMillis = 3000, easing = LinearOutSlowInEasing)))
                    }
                  }

                  // -- BUTTON NAVIGATE TO FIND FRIENDS SCREEN
                  item {
                    Button(
                        onClick = { onFindFriends() },
                        modifier =
                            Modifier.fillMaxWidth()
                                // -- Animation slide up when an item disappear
                                .animateItemPlacement(
                                    animationSpec =
                                        tween(
                                            durationMillis = 3000, easing = LinearOutSlowInEasing))
                                .height(dimensionResource(R.dimen.friends_find_button_height))
                                .padding(
                                    vertical =
                                        dimensionResource(R.dimen.friends_find_button_vertical))
                                .testTag(FriendsScreenTestTags.BUTTON_FIND_FRIENDS),
                        shape =
                            RoundedCornerShape(
                                dimensionResource(R.dimen.friends_item_rounded_corner_shape)),
                        colors =
                            buttonColors(
                                containerColor = MaterialTheme.colorScheme.inversePrimary)) {
                          Text(
                              text = stringResource(R.string.find_friends_button_label),
                              fontSize = 16.sp,
                              fontWeight = FontWeight.Medium,
                              color = MaterialTheme.colorScheme.onPrimary)
                        }
                  }
                }
          }
          // --- UNFOLLOW A FRIEND ANIMATION ---
          if (showUnfollowMessage) {
            FloatingMessage(text = messageText, modifier = Modifier.fillMaxSize().padding(padding))
          }
        }
      })
}

/**
 * Helper function : Composable helper that displays a single user profile item in the friends list.
 *
 * @param friend Username of the friend to display.
 * @param unfollow Callback triggered when the "Follow" button is clicked.
 * @param modifier Optional [Modifier] for layout customization.
 */
@Composable
private fun FriendItem(friend: String, unfollow: () -> Unit, modifier: Modifier = Modifier) {
  Card(
      border =
          BorderStroke(
              dimensionResource(R.dimen.friends_item_card_border_width),
              MaterialTheme.colorScheme.primary),
      shape = RoundedCornerShape(dimensionResource(R.dimen.friends_item_card_rounded_corner_shape)),
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.secondaryContainer,
              contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
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

          // -- Placeholder Profile Picture --
          Image(
              painter =
                  painterResource(
                      id = R.drawable.ic_launcher_foreground), // currently a placeholder image
              contentDescription = "Profile picture of ${friend}",
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
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                modifier =
                    Modifier.testTag(FriendsScreenTestTags.getTestTagForFriendUsername(friend)))
          }
          // -- SPACER
          Spacer(
              modifier = Modifier.width(dimensionResource(R.dimen.spacing_between_fields_regular)))

          // -- Unfollow button --
          Button(
              onClick = unfollow,
              modifier =
                  Modifier.wrapContentWidth()
                      .testTag(FriendsScreenTestTags.getTestTagForFriendUnfollowButton(friend))) {
                Text(stringResource(R.string.friends_unfollow_button_title))
              }
        }
      }
}

// This function contains code generated by an AI (DeepSeek).

/**
 * Helper function: Composable helper that displays a floating message animation when the user
 * follows a profile.
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
                            .testTag(FriendsScreenTestTags.UNFOLLOWING_TEXT_ANIMATION)) {
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
