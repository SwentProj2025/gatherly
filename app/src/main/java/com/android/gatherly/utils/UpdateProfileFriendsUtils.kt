package com.android.gatherly.utils

import com.android.gatherly.model.badge.BadgeType
import com.android.gatherly.model.notification.Notification
import com.android.gatherly.model.notification.NotificationType
import com.android.gatherly.model.notification.NotificationsRepository
import com.android.gatherly.model.points.PointsRepository
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileRepository

/**
 * Synchronises the user's friends list with all pending friend-related notifications and events
 * notifications.
 *
 * For every FRIEND_ACCEPTED, FRIEND_REJECTED, REMOVE_FRIEND, FRIEND_REQUEST_CANCELLED notification
 * addressed to [userId], this:
 * - updates the friends list if needed
 * - updates pending states of current user's requests if needed
 * - deletes the processed notification Returns the fresh up-to-date profile for the user.
 *
 * For each EVENT_PARTICIPATION notification addressed to [userId], this awards one "events
 * participated" increment for badges and deletes the notification. This function does not display
 * any UI. It only performs background synchronisation.
 *
 * @param profileRepository The repository to fetch and update profiles
 * @param notificationsRepository The repository to fetch a user's notifications
 * @param pointsRepository The repository to update a user's points history
 * @param userId The user whose profile to update
 */
suspend fun getProfileWithSyncedNotifications(
    profileRepository: ProfileRepository,
    notificationsRepository: NotificationsRepository,
    pointsRepository: PointsRepository,
    userId: String
): Profile? {

  val notifications = notificationsRepository.getUserNotifications(userId)

  for (notification in notifications) {
    when (notification.type) {
      NotificationType.FRIEND_ACCEPTED -> {
        handleFriendAccepted(
            profileRepository, notificationsRepository, pointsRepository, notification, userId)
      }
      NotificationType.FRIEND_REJECTED -> {
        handleFriendRejected(profileRepository, notificationsRepository, notification, userId)
      }
      NotificationType.REMOVE_FRIEND -> {
        handleRemoveFriend(profileRepository, notificationsRepository, notification, userId)
      }
      NotificationType.FRIEND_REQUEST_CANCELLED -> {
        handleFriendRequestCancelled(notificationsRepository, notification, notifications)
      }
      NotificationType.EVENT_PARTICIPATION -> {
        handleEventParticipation(
            profileRepository = profileRepository,
            notificationsRepository = notificationsRepository,
            pointsRepository = pointsRepository,
            notification = notification,
            userId = userId,
        )
      }
      else -> {}
    }
  }

  return profileRepository.getProfileByUid(userId)
}

/**
 * Handles a friend request being accepted by the other party and updates the user profile
 *
 * @param profileRepository The repository to update profiles
 * @param notificationsRepository The repository to fetch a user's notifications
 * @param pointsRepository The repository to update a user's points history
 * @param userId The user whose profile to update
 */
private suspend fun handleFriendAccepted(
    profileRepository: ProfileRepository,
    notificationsRepository: NotificationsRepository,
    pointsRepository: PointsRepository,
    notification: Notification,
    userId: String
) {
  val senderId = notification.senderId
  if (senderId != null) {
    val senderProfile = profileRepository.getProfileByUid(senderId)
    if (senderProfile != null) {
      addFriendWithPointsCheck(profileRepository, pointsRepository, senderProfile.username, userId)
      profileRepository.removePendingSentFriendUid(userId, senderId)
    }
  }
  notificationsRepository.deleteNotification(notification.id)
}

/**
 * Handles a friend request being rejected by the other party and updates the user profile
 *
 * @param profileRepository The repository to update profiles
 * @param notificationsRepository The repository to fetch a user's notifications
 * @param notification The notification to delete
 * @param userId The user whose profile to update
 */
private suspend fun handleFriendRejected(
    profileRepository: ProfileRepository,
    notificationsRepository: NotificationsRepository,
    notification: Notification,
    userId: String
) {
  val senderId = notification.senderId
  if (senderId != null) {
    profileRepository.removePendingSentFriendUid(userId, senderId)
  }
  notificationsRepository.deleteNotification(notificationId = notification.id)
}

/**
 * Handles a friend removal and updates the user profile
 *
 * @param profileRepository The repository to update profiles
 * @param notificationsRepository The repository to fetch a user's notifications
 * @param notification The notification to send
 * @param userId The user whose profile to update
 */
private suspend fun handleRemoveFriend(
    profileRepository: ProfileRepository,
    notificationsRepository: NotificationsRepository,
    notification: Notification,
    userId: String
) {
  val senderId = notification.senderId
  if (senderId != null) {
    val senderProfile = profileRepository.getProfileByUid(senderId)
    if (senderProfile != null) {
      profileRepository.deleteFriend(senderProfile.username, userId)
    }
  }
  notificationsRepository.deleteNotification(notification.id)
}

/**
 * Handles a friend request being cancelled and updates the user profile
 *
 * @param notificationsRepository The repository to fetch a user's notifications
 * @param notification The notification to delete
 * @param notifications The existing notifications
 */
private suspend fun handleFriendRequestCancelled(
    notificationsRepository: NotificationsRepository,
    notification: Notification,
    notifications: List<Notification>,
) {
  val senderId = notification.senderId
  if (senderId != null) {
    val originalNotifications =
        notifications.filter {
          it.type == NotificationType.FRIEND_REQUEST && it.senderId == senderId
        }
    for (notification in originalNotifications) {
      notificationsRepository.deleteNotification(notification.id)
    }
  }
  notificationsRepository.deleteNotification(notification.id)
}

/**
 * Handles an EVENT_PARTICIPATION notification:
 * - awards the "events participated" badge counter
 * - deletes the notification once processed
 */
private suspend fun handleEventParticipation(
    profileRepository: ProfileRepository,
    notificationsRepository: NotificationsRepository,
    pointsRepository: PointsRepository,
    notification: Notification,
    userId: String,
) {

  if (notification.relatedEntityId.isNullOrBlank()) {
    notificationsRepository.deleteNotification(notification.id)
    return
  }

  incrementBadgeCheckPoints(
      profileRepository, pointsRepository, userId, BadgeType.EVENTS_PARTICIPATED)

  notificationsRepository.deleteNotification(notification.id)
}
