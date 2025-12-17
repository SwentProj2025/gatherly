package com.android.gatherly.ui.notifications

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.android.gatherly.ui.navigation.NavigationActions
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu_Goback
import com.android.gatherly.utils.LoadingAnimation
import com.android.gatherly.utils.profilePicturePainter

const val MAX_MEMBERS_DISPLAYED = 3

/** Test tags for Notifications Screen composables. */
object NotificationsScreenTestTags {
  const val EMPTY_LIST_MSG = "messageEmptyList"
  const val FRIEND_REQUEST_SECTION = "friendRequestSection"
  const val FRIEND_REQUEST_SECTION_TEXT = "friendRequestSectionText"

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
   * @return A string uniquely identifying the sender's name Text item in the UI.
   */
  fun getTestTagForSenderName(username: String): String = "friendUsername${username}"

  /**
   * Returns a unique test tag for the card or container representing a given
   * [Profile.profilePicture] item.
   *
   * @param username The username of the friend whose profile picture test tag will be generated.
   * @return A string uniquely identifying the sender's profile picture item in the UI.
   */
  fun getTestTagForSenderProfilePictureInNotification(username: String): String =
      "senderProfilePictureInNotification${username}"

  /**
   * Returns a unique test tag for the card or container representing a given
   * [Profile.profilePicture] item.
   *
   * @param username The username of the friend whose profile picture test tag will be generated.
   * @return A string uniquely identifying the sender's profile picture item in the UI.
   */
  fun getTestTagForSenderProfilePictureInFriendRequestSection(username: String): String =
      "senderProfilePictureInFriendRequestSection${username}"

  /**
   * Returns a unique test tag for the card or container representing a given [Profile.username]
   * item.
   *
   * @param username The [Button] item for following button whose test tag will be generated.
   * @return A string uniquely identifying the sender's username item in the UI.
   */
  fun getTestTagForVisitProfileButton(username: String): String = "visitProfileButton${username}"
}

/**
 * Composable function representing the Notifications Screen.
 *
 * @param notificationsViewModel The ViewModel managing the notifications data and state.
 * @param navigationActions Optional [NavigationActions] for handling navigation events.
 * @param onVisitProfile Callback triggered when the "Visit Profile" button is clicked.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotificationsScreen(
    notificationsViewModel: NotificationViewModel = viewModel(),
    navigationActions: NavigationActions? = null,
    onVisitProfile: (Profile) -> Unit = { _ -> }
) {

  // Retrieve the necessary values for the implementation from the ViewModel
  val uiState by notificationsViewModel.uiState.collectAsState()
  val notifications = uiState.notifications
  val friendRequests = uiState.notifications.filter { it.type == NotificationType.FRIEND_REQUEST }
  val isLoading = uiState.isLoading

  // Fetch notifications when the screen is recomposed
  LaunchedEffect(Unit) { notificationsViewModel.loadNotifications() }

  // --- PART SCAFFOLD COMPOSE ---
  Scaffold(
      topBar = {
        TopNavigationMenu_Goback(
            selectedTab = Tab.Notifications,
            modifier = Modifier.testTag(NavigationTestTags.TOP_NAVIGATION_MENU),
            goBack = { navigationActions?.goBack() })
      },
      content = { padding ->
        Box(modifier = Modifier.fillMaxSize()) {

          // --- LOADING NOTIFICATIONS ANIMATION ---
          if (isLoading) {
            LoadingAnimation(stringResource(R.string.loading_notifications_message), padding)
          } else {
            LazyColumn(
                contentPadding = PaddingValues(vertical = dimensionResource(R.dimen.padding_small)),
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(horizontal = dimensionResource(R.dimen.padding_screen))
                        .padding(padding)) {

                  // --- NO NOTIFICATIONS ---
                  if (notifications.isEmpty()) {
                    item {
                      Text(
                          text = stringResource(R.string.notifications_empty_list_message),
                          modifier =
                              Modifier.padding(dimensionResource(R.dimen.padding_screen))
                                  .testTag(NotificationsScreenTestTags.EMPTY_LIST_MSG))
                    }
                  } else {
                    item {
                      FriendRequestsNavigation(
                          idToProfile = uiState.idToProfile,
                          sendersProfiles =
                              friendRequests.mapNotNull { uiState.idToProfile[it.senderId] },
                          friendRequests = friendRequests,
                          onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) })
                    }

                    // --- SHOWING USERS' NOTIFICATION ITEMS ---
                    notificationsList(
                        notifications = notifications,
                        onVisitProfile = { profile -> onVisitProfile(profile) },
                        idToProfile = uiState.idToProfile)
                  }
                }
          }
        }
      })
}

/**
 * Helper function : Composable helper that displays a single notification item
 *
 * @param senderProfile The profile of the user who sent the notification
 * @param notificationType The type of the notification that was sent
 * @param visitProfile Callback triggered when the "Visit Profile" button is clicked, if the
 *   notification type is a friend request
 * @param modifier Optional [Modifier] for layout customization.
 */
@Composable
private fun NotificationsItem(
    senderProfile: Profile,
    notificationType: NotificationType,
    modifier: Modifier = Modifier,
    visitProfile: () -> Unit = {}
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
              .testTag(
                  NotificationsScreenTestTags.getTestTagForFriendRequestItem(
                      senderProfile.username))
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
              painter = profilePicturePainter(senderProfile.profilePicture),
              contentDescription = "Profile picture of ${senderProfile.username}",
              contentScale = ContentScale.Crop,
              modifier =
                  Modifier.size(dimensionResource(R.dimen.friend_request_item_profile_picture_size))
                      .clip(CircleShape)
                      .testTag(
                          NotificationsScreenTestTags
                              .getTestTagForSenderProfilePictureInNotification(
                                  senderProfile.username)))

          // -- SPACER
          Spacer(
              modifier =
                  Modifier.width(dimensionResource(R.dimen.spacing_between_fields_smaller_regular)))

          val senderName =
              when (notificationType) {
                NotificationType.EVENT_REMINDER,
                NotificationType.TODO_REMINDER ->
                    stringResource(R.string.notification_reminder_user)
                else -> senderProfile.name
              }

          val text =
              when (notificationType) {
                NotificationType.FRIEND_REQUEST ->
                    stringResource(R.string.friend_requests_notification_text)
                NotificationType.EVENT_REMINDER ->
                    stringResource(R.string.notification_event_reminder)
                NotificationType.TODO_REMINDER ->
                    stringResource(R.string.notification_todo_reminder)
                NotificationType.GROUP_ADDED -> stringResource(R.string.notification_group_added)
                NotificationType.EVENT_PARTICIPATION ->
                    stringResource(R.string.notification_event_added)
                else -> ""
              }

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
                            NotificationsScreenTestTags.getTestTagForSenderName(
                                senderProfile.username)))
                // -- Friend Request Text --
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Light)
              }

          if (notificationType == NotificationType.FRIEND_REQUEST) {
            // -- SPACER
            Spacer(
                modifier =
                    Modifier.width(dimensionResource(R.dimen.spacing_between_fields_regular)))

            // -- Visit Profile Button --
            Button(
                onClick = visitProfile,
                colors =
                    buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary),
                modifier =
                    Modifier.wrapContentWidth()
                        .testTag(
                            NotificationsScreenTestTags.getTestTagForVisitProfileButton(
                                senderProfile.username))) {
                  Text(stringResource(R.string.friend_requests_notification_button_title))
                }
          }
        }
      }
}

/**
 * Composable that displays the list of notifications in the notifications screen.
 *
 * @param notifications List of [Notification] objects to be displayed.
 * @param onVisitProfile Callback triggered when the "Visit Profile" button is clicked.
 * @param idToProfile Map of profile IDs to Profile objects for sender information.
 */
@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.notificationsList(
    notifications: List<Notification>,
    onVisitProfile: (Profile) -> Unit,
    idToProfile: Map<String, Profile>
) {
  // --- NOTIFICATIONS ITEMS ---
  items(items = notifications) { notification ->
    val senderId = notification.senderId
    val senderProfile = idToProfile[senderId]
    if (senderProfile == null || senderId == null) {
      // Profile/ID not found, skip rendering this item
      return@items
    }

    when (notification.type) {
      NotificationType.FRIEND_REQUEST -> {
        NotificationsItem(
            senderProfile = senderProfile,
            notificationType = notification.type,
            visitProfile = { onVisitProfile(senderProfile) })
      }
      NotificationType.EVENT_REMINDER,
      NotificationType.TODO_REMINDER,
      NotificationType.GROUP_ADDED,
      NotificationType.EVENT_PARTICIPATION -> {
        NotificationsItem(senderProfile = senderProfile, notificationType = notification.type)
      }
      else -> {
        // Do nothing for other notification types
      }
    }
  }
}

/**
 * Composable that displays the friend requests navigation section in the notifications screen.
 *
 * @param idToProfile Map of profile IDs to Profile objects for sender information.
 * @param sendersProfiles List of Profile objects representing the senders of friend requests.
 * @param friendRequests List of Notification objects representing the friend requests.
 * @param modifier Optional [Modifier] for layout customization.
 * @param onTabSelected Callback triggered when the friend requests tab is selected.
 */
@Composable
fun FriendRequestsNavigation(
    idToProfile: Map<String, Profile>,
    sendersProfiles: List<Profile>,
    friendRequests: List<Notification>,
    modifier: Modifier = Modifier,
    onTabSelected: (Tab) -> Unit
) {
  val profilePictureSize = dimensionResource(id = R.dimen.profile_pic_size_small)
  val smallSpacing = dimensionResource(id = R.dimen.spacing_between_fields_smaller_regular)
  val regularSpacing = dimensionResource(id = R.dimen.spacing_between_fields_regular)
  val avatarSpacing = dimensionResource(id = R.dimen.notification_avatar_spacing)
  val horizontalPadding = dimensionResource(id = R.dimen.notification_row_horizontal_padding)
  val borderWidth = dimensionResource(id = R.dimen.notification_avatar_border_width)
  val pictureContentDescription = stringResource(R.string.profile_picture_description)
  val zeroRequestsSupportingText = stringResource(R.string.friend_requests_button_text_zero)
  val twoRequestsSupportingText = stringResource(R.string.friend_requests_button_text_two)
  val multipleRequestsSupportingText = stringResource(R.string.friend_requests_button_text_multiple)
  val nbFriendRequests = friendRequests.size
  val firstSenderUsername: String =
      if (friendRequests.isEmpty()) "" else idToProfile[friendRequests[0].senderId]?.username ?: ""
  val requestsText =
      when {
        friendRequests.isEmpty() -> zeroRequestsSupportingText
        nbFriendRequests == 1 -> firstSenderUsername
        nbFriendRequests == 2 ->
            "$firstSenderUsername + ${nbFriendRequests - 1} $twoRequestsSupportingText"
        else -> "$firstSenderUsername + ${nbFriendRequests - 1} $multipleRequestsSupportingText"
      }

  // Width needed for 3 avatars + spacing between them
  val maxAvatarWidth =
      profilePictureSize * MAX_MEMBERS_DISPLAYED +
          avatarSpacing * (MAX_MEMBERS_DISPLAYED - 1) // two gaps between 3 images

  Column(modifier = modifier.testTag(NotificationsScreenTestTags.FRIEND_REQUEST_SECTION)) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .padding(horizontal = horizontalPadding, vertical = smallSpacing),
        verticalAlignment = Alignment.CenterVertically) {
          Row(
              modifier = Modifier.width(maxAvatarWidth),
              verticalAlignment = Alignment.CenterVertically) {
                sendersProfiles.take(MAX_MEMBERS_DISPLAYED).forEach { sender ->
                  Image(
                      painter = profilePicturePainter(sender.profilePicture),
                      contentDescription = pictureContentDescription,
                      modifier =
                          Modifier.size(profilePictureSize)
                              .clip(CircleShape)
                              .border(borderWidth, MaterialTheme.colorScheme.outline, CircleShape)
                              .testTag(
                                  NotificationsScreenTestTags
                                      .getTestTagForSenderProfilePictureInFriendRequestSection(
                                          sender.username)))
                  Spacer(modifier = Modifier.width(avatarSpacing))
                }
              }

          Spacer(modifier = Modifier.width(regularSpacing))

          Column(
              modifier =
                  Modifier.weight(
                      integerResource(R.integer.friend_request_column_weight).toFloat())) {
                Text(
                    text = stringResource(R.string.friend_requests_label),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold)
                Text(
                    text = requestsText,
                    modifier =
                        Modifier.testTag(NotificationsScreenTestTags.FRIEND_REQUEST_SECTION_TEXT),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
              }

          IconButton(
              content = {
                Icon(
                    imageVector = Tab.FriendRequests.icon,
                    contentDescription = Tab.FriendRequests.name,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
              },
              onClick = { onTabSelected(Tab.FriendRequests) },
              modifier = Modifier.testTag(NavigationTestTags.FRIEND_REQUESTS_TAB))
        }
  }
}
