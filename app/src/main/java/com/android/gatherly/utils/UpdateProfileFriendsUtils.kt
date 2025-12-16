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
 */
suspend fun getProfileWithSyncedFriendNotifications(
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
 * Handles a FRIEND_ACCEPTED notification:
 * - adds the sender as a friend for [userId] (and awards points if applicable)
 * - removes the pending request state
 * - deletes the notification once processed
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
 * Handles a FRIEND_REJECTED notification:
 * - removes the pending request state from [userId] to the sender
 * - deletes the notification once processed
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
 * Handles a REMOVE_FRIEND notification:
 * - deletes the sender from [userId]'s friend list (if possible)
 * - deletes the notification once processed
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
 * Handles a FRIEND_REQUEST_CANCELLED notification:
 * - finds the original FRIEND_REQUEST notification from the same sender and deletes it
 * - deletes the cancellation notification once processed
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
