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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
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
import com.android.gatherly.model.profile.ProfileRepositoryFirestore
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu_Goback
import com.android.gatherly.utils.GenericViewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage

object FindFriendsScreenTestTags {
  const val SEARCH_FRIENDS_BAR = "searchBarFriends"
  const val EMPTY_LIST_MSG = "messageEmptyList"

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
   * @param username The [Button] item for following button whose test tag will be generated.
   * @return A string uniquely identifying the Friend username item in the UI.
   */
  fun getTestTagForFriendFollowButton(username: String): String = "friendFollowingButton${username}"
}

private val LOTTIE_ANIMATION_RESOURCE = R.raw.heart
private val ANIMATION_LOADING = R.raw.loading_profiles

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FindFriendsScreen(
    friendsViewModel: FriendsViewModel =
        viewModel(
            factory =
                GenericViewModelFactory<FriendsViewModel> {
                  FriendsViewModel(
                      repository = ProfileRepositoryFirestore(Firebase.firestore, Firebase.storage),
                      currentUserId = Firebase.auth.currentUser?.uid ?: "")
                }),
    goBack: () -> Unit = {},
) {

  val currentUserIdFromVM = friendsViewModel.currentUserId
  val uiState by friendsViewModel.uiState.collectAsState()
  val notFriendsList = uiState.listNoFriends

  var searchQuery by remember { mutableStateOf("") }

  val filteredNotFriends =
      if (searchQuery.isBlank()) {
        notFriendsList
      } else {
        notFriendsList.filter { friend -> friend.contains(searchQuery, ignoreCase = true) }
      }

  LaunchedEffect(currentUserIdFromVM) {
    if (currentUserIdFromVM.isNotBlank()) {
      friendsViewModel.refreshFriends(currentUserIdFromVM)
    }
  }

  var showFollowMessage by remember { mutableStateOf(false) }

  val messageText = stringResource(R.string.friends_follow_message)

  LaunchedEffect(showFollowMessage) {
    if (showFollowMessage) {
      kotlinx.coroutines.delay(2000)
      showFollowMessage = false
    }
  }

  Scaffold(
      topBar = {
        TopNavigationMenu_Goback(
            selectedTab = Tab.Friends,
            modifier = Modifier.testTag(NavigationTestTags.TOP_NAVIGATION_MENU),
            goBack = goBack)
      },
      content = { padding ->
          val showHeartAnimation = showFollowMessage
        val isLoading = uiState.isLoading && !showHeartAnimation

        Box(modifier = Modifier.fillMaxSize()) {
          if (isLoading ) {
            val loadingComposition by
                rememberLottieComposition(LottieCompositionSpec.RawRes(ANIMATION_LOADING))
            LottieAnimation(
                composition = loadingComposition,
                iterations = LottieConstants.IterateForever,
                modifier =
                    Modifier.size(dimensionResource(R.dimen.lottie_icon_size_extra_large))
                        .align(Alignment.Center))
          } else {

            LazyColumn(
                contentPadding = PaddingValues(vertical = dimensionResource(R.dimen.padding_small)),
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(horizontal = dimensionResource(R.dimen.padding_screen))
                        .padding(padding)) {
                  if (filteredNotFriends.isEmpty()) {
                    item {
                      Text(
                          text = stringResource(R.string.find_friends_empty_list_message),
                          modifier =
                              Modifier.padding(dimensionResource(R.dimen.padding_screen))
                                  .testTag(FindFriendsScreenTestTags.EMPTY_LIST_MSG))
                    }
                  } else {
                    item {
                      OutlinedTextField(
                          value = searchQuery,
                          onValueChange = { searchQuery = it },
                          modifier =
                              Modifier.fillMaxWidth()
                                  .padding(vertical = dimensionResource(R.dimen.padding_small))
                                  .testTag(FindFriendsScreenTestTags.SEARCH_FRIENDS_BAR),
                          placeholder = {
                            Text(
                                text = stringResource(R.string.find_friends_search_bar_label),
                                modifier =
                                    Modifier.padding(
                                        dimensionResource(R.dimen.find_friends_search_bar_width)))
                          },
                          singleLine = true,
                          shape =
                              RoundedCornerShape(
                                  dimensionResource(
                                      R.dimen.find_friends_item_rounded_corner_shape)))
                    }

                    items(items = filteredNotFriends, key = { it }) { friend ->
                      FriendItem(
                          friend = friend,
                          follow = {
                            friendsViewModel.followFriend(
                                currentUserId = currentUserIdFromVM, friend = friend)
                            showFollowMessage = true
                          },
                          modifier =
                              Modifier.animateItemPlacement(
                                  animationSpec =
                                      tween(durationMillis = 3000, easing = LinearOutSlowInEasing)))
                    }
                  }
                }
            if (showFollowMessage) {
              FloatingMessage(
                  text = messageText, modifier = Modifier.fillMaxSize().padding(padding))
            }
          }
        }
      })
}

@Composable
private fun FriendItem(friend: String, follow: () -> Unit, modifier: Modifier = Modifier) {
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
              containerColor = MaterialTheme.colorScheme.onSurface,
              contentColor = MaterialTheme.colorScheme.primary),
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
              painter =
                  painterResource(
                      id = R.drawable.ic_launcher_foreground), // currently a placeholder image
              contentDescription = "Profile picture of ${friend}",
              modifier =
                  Modifier.size(dimensionResource(R.dimen.find_friends_item_profile_picture_size))
                      .clip(CircleShape)
                      .testTag(FindFriendsScreenTestTags.getTestTagForFriendProfilePicture(friend)))

          Spacer(
              modifier =
                  Modifier.width(dimensionResource(R.dimen.spacing_between_fields_smaller_regular)))

          Column(modifier = Modifier.weight(1f)) {
            Text(
                text = friend,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                modifier =
                    Modifier.testTag(FindFriendsScreenTestTags.getTestTagForFriendUsername(friend)))
          }
          Spacer(
              modifier = Modifier.width(dimensionResource(R.dimen.spacing_between_fields_regular)))

          Button(
              onClick = follow,
              modifier =
                  Modifier.wrapContentWidth()
                      .testTag(FindFriendsScreenTestTags.getTestTagForFriendFollowButton(friend))) {
                Text(stringResource(R.string.friends_follow_button_title))
              }
        }
      }
}


// This function contains code generated by an AI (DeepSeek).

/**
 * Helper function :
 */
@Composable
private fun FloatingMessage(text: String, modifier: Modifier = Modifier) {
  val composition by
      rememberLottieComposition(LottieCompositionSpec.RawRes(LOTTIE_ANIMATION_RESOURCE))
  AnimatedVisibility(
      visible = true,
      enter = slideInVertically(initialOffsetY = { -it / 2 }) + fadeIn(),
      exit = slideOutVertically(targetOffsetY = { -it / 2 }) + fadeOut(),
      modifier = modifier.zIndex(1f)) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center) {
                LottieAnimation(
                    composition = composition,
                    iterations = 1,
                    speed = 1f,
                    modifier = Modifier.size(dimensionResource(R.dimen.lottie_icon_size_large)))

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))

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
                          modifier = Modifier.padding(dimensionResource(R.dimen.padding_regular)))
                    }
              }
        }
      }
}
