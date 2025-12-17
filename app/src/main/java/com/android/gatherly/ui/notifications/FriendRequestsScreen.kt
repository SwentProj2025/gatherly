package com.android.gatherly.ui.notifications

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.R
import com.android.gatherly.model.notification.Notification
import com.android.gatherly.model.notification.NotificationType
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu_Goback
import com.android.gatherly.utils.profilePicturePainter

object FriendRequestsScreenTestTags {
  const val EMPTY_LIST_MSG = "messageEmptyList"
  const val LOADING_CIRCLE = "loadingCircle"

  /**
   * Returns a unique test tag for the card or container representing a given [Profile.username]
   * item.
   *
   * @param username The [Profile.username] item of a chosen friend whose test tag will be
   *   generated.
   * @return A string uniquely identifying the sender's username item in the UI.
   */
  fun getTestTagForFriendRequestItem(username: String): String = "friendItem${username}"

  /**
   * Returns a unique test tag for the card container representing a given [Profile.username] item.
   *
   * @param username The [Profile.username] Text item of a chosen friend whose test tag will be
   *   generated.
   * @return A string uniquely identifying the sender's username Text item in the UI.
   */
  fun getTestTagForSenderUsername(username: String): String = "friendUsername${username}"

  /**
   * Returns a unique test tag for the card container representing a given [Profile.name] item.
   *
   * @param username The [Profile.username] Text item of a chosen friend whose test tag will be
   *   generated.
   * @return A string uniquely identifying the sender's name Text item in the UI.
   */
  fun getTestTagForSenderName(username: String): String = "friendName${username}"

  /**
   * Returns a unique test tag for the card or container representing a given
   * [Profile.profilePicture] item.
   *
   * @param username The username of the friend whose profile picture test tag will be generated.
   * @return A string uniquely identifying the sender's profile picture item in the UI.
   */
  fun getTestTagForSenderProfilePicture(username: String): String =
      "friendProfilePicture${username}"

  /**
   * Returns a unique test tag for the card or container representing an accept button [Button].
   *
   * @param username The [Button] item for the accept button whose test tag will be generated.
   * @return A string uniquely identifying the sender's username item in the UI.
   */
  fun getTestTagForAcceptButton(username: String): String = "acceptButton${username}"

  /**
   * Returns a unique test tag for the card or container representing a reject button [Button].
   *
   * @param username The [Button] item for the reject button whose test tag will be generated.
   * @return A string uniquely identifying the sender's username item in the UI.
   */
  fun getTestTagForRejectButton(username: String): String = "rejectButton${username}"
}

/**
 * Composable function that represents the Friend Requests screen.
 *
 * This screen displays a list of incoming friend requests, allowing the user to accept or reject
 * each request. It retrieves the necessary data from the [NotificationViewModel] and handles user
 * interactions.
 *
 * @param notificationsViewModel The ViewModel responsible for managing notifications data.
 * @param goBack A callback function to navigate back to the previous screen.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FriendRequestsScreen(
    notificationsViewModel: NotificationViewModel = viewModel(),
    goBack: () -> Unit = {},
) {

  // Retrieve the necessary values for the implementation from the ViewModel
  val uiState by notificationsViewModel.uiState.collectAsState()
  val friendRequests = uiState.notifications.filter { it.type == NotificationType.FRIEND_REQUEST }
  val isLoading = uiState.isLoading

  // Fetch notifications when the screen is recomposed
  LaunchedEffect(Unit) { notificationsViewModel.loadNotifications() }

  // --- PART SCAFFOLD COMPOSE ---
  Scaffold(
      topBar = {
        TopNavigationMenu_Goback(
            selectedTab = Tab.FriendRequests,
            modifier = Modifier.testTag(NavigationTestTags.TOP_NAVIGATION_MENU),
            goBack = goBack)
      },
      content = { padding ->
        Box(modifier = Modifier.fillMaxSize()) {

          // --- LOADING FRIEND REQUEST ANIMATION ---
          if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center) {
                  CircularProgressIndicator()
                }
          } else {
            LazyColumn(
                contentPadding = PaddingValues(vertical = dimensionResource(R.dimen.padding_small)),
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(horizontal = dimensionResource(R.dimen.padding_screen))
                        .padding(padding)) {
                  // --- NO FRIEND REQUESTS ---
                  if (friendRequests.isEmpty()) {
                    item {
                      Text(
                          text = stringResource(R.string.friend_requests_empty_list_message),
                          modifier =
                              Modifier.padding(dimensionResource(R.dimen.padding_screen))
                                  .testTag(FriendRequestsScreenTestTags.EMPTY_LIST_MSG))
                    }
                  } else {
                    // --- FRIEND REQUEST ITEMS ---
                    // --- SHOWING USERS' FRIEND REQUEST ITEMS ---
                    friendRequestsList(
                        friendRequests = friendRequests,
                        onAcceptFriendRequest = { notificationId ->
                          notificationsViewModel.acceptFriendRequest(notificationId)
                        },
                        onRejectFriendRequest = { notificationId ->
                          notificationsViewModel.rejectFriendRequest(notificationId)
                        },
                        idToProfile = uiState.idToProfile)
                  }
                }
          }
        }
      })
}

/**
 * Helper function : Composable helper that displays a single friend request item in the
 * notifications list.
 *
 * @param senderUsername Username of the friend that sent the request.
 * @param senderName Name of the friend that sent the request.
 * @param acceptFriendRequest Callback triggered when the "Accept" button is clicked.
 * @param rejectFriendRequest Callback triggered when the "Reject" button is clicked.
 * @param modifier Optional [Modifier] for layout customization.
 * @param profilePicUrl Optional URL of the friend's profile picture.
 */
@Composable
private fun FriendRequestItem(
    senderUsername: String,
    senderName: String,
    acceptFriendRequest: () -> Unit,
    rejectFriendRequest: () -> Unit,
    modifier: Modifier = Modifier,
    profilePicUrl: String? = null
) {
  Card(
      shape =
          RoundedCornerShape(
              dimensionResource(R.dimen.friend_request_item_card_rounded_corner_shape)),
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant,
              contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
      modifier =
          modifier
              .testTag(FriendRequestsScreenTestTags.getTestTagForFriendRequestItem(senderUsername))
              .fillMaxWidth()
              .padding(
                  vertical =
                      dimensionResource(R.dimen.friend_request_item_card_padding_vertical))) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(dimensionResource(R.dimen.friend_request_item_card_padding)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Image(
              painter = profilePicturePainter(profilePicUrl),
              contentDescription = "Profile picture of $senderUsername",
              contentScale = ContentScale.Crop,
              modifier =
                  Modifier.size(dimensionResource(R.dimen.friend_request_item_profile_picture_size))
                      .clip(CircleShape)
                      .testTag(
                          FriendRequestsScreenTestTags.getTestTagForSenderProfilePicture(
                              senderUsername)))

          // -- SPACER
          Spacer(
              modifier =
                  Modifier.width(dimensionResource(R.dimen.spacing_between_fields_smaller_regular)))

          Column(
              modifier =
                  Modifier.weight(
                      integerResource(R.integer.friend_request_column_weight).toFloat())) {

                // -- Name Text --
                Text(
                    text = senderName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    modifier =
                        Modifier.testTag(
                            FriendRequestsScreenTestTags.getTestTagForSenderName(senderUsername)))

                // -- Username Text --
                Text(
                    text = senderUsername,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Light,
                    modifier =
                        Modifier.testTag(
                            FriendRequestsScreenTestTags.getTestTagForSenderUsername(
                                senderUsername)))
              }

          // -- SPACER
          Spacer(
              modifier = Modifier.width(dimensionResource(R.dimen.spacing_between_fields_regular)))

          // -- Accept Button --
          Button(
              onClick = acceptFriendRequest,
              colors =
                  buttonColors(
                      containerColor = MaterialTheme.colorScheme.secondary,
                      contentColor = MaterialTheme.colorScheme.onSecondary),
              modifier =
                  Modifier.wrapContentWidth()
                      .testTag(
                          FriendRequestsScreenTestTags.getTestTagForAcceptButton(senderUsername))) {
                Text(stringResource(R.string.friend_requests_accept_button_text))
              }

          // -- SPACER
          Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_between_fields)))

          // -- Reject Button --
          Button(
              onClick = rejectFriendRequest,
              colors =
                  buttonColors(
                      containerColor = MaterialTheme.colorScheme.background,
                      contentColor = MaterialTheme.colorScheme.onBackground),
              modifier =
                  Modifier.wrapContentWidth()
                      .testTag(
                          FriendRequestsScreenTestTags.getTestTagForRejectButton(senderUsername))) {
                Text(stringResource(R.string.friend_requests_reject_button_text))
              }
        }
      }
}

/**
 * Composable that displays a list of friend requests.
 *
 * @param friendRequests List of friend request notifications to display.
 * @param onAcceptFriendRequest Callback triggered when a friend request is accepted.
 * @param onRejectFriendRequest Callback triggered when a friend request is rejected.
 * @param idToProfile Map of user IDs to their corresponding Profile objects.
 */
@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.friendRequestsList(
    friendRequests: List<Notification>,
    onAcceptFriendRequest: (String) -> Unit,
    onRejectFriendRequest: (String) -> Unit,
    idToProfile: Map<String, Profile>
) {
  items(items = friendRequests) { friendRequest ->
    when (friendRequest.type) {
      NotificationType.FRIEND_REQUEST -> {
        val senderId = friendRequest.senderId
        val senderProfile = idToProfile[senderId]
        if (senderProfile == null || senderId == null) {
          // Profile/ID not found, skip rendering this item
          return@items
        }
        val senderName = senderProfile.name
        FriendRequestItem(
            senderName = senderName,
            senderUsername = senderProfile.username,
            acceptFriendRequest = { onAcceptFriendRequest(friendRequest.id) },
            rejectFriendRequest = { onRejectFriendRequest(friendRequest.id) },
            profilePicUrl = senderProfile.profilePicture)
      }
      else -> {
        // Do nothing for other notification types
      }
    }
  }
}
