package com.android.gatherly.utils

import com.android.gatherly.model.notification.NotificationType
import com.android.gatherly.model.notification.NotificationsRepository
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileRepository

/**
 * Synchronises the user's friends list with all pending friend-related notifications.
 *
 * For every FRIEND_ACCEPTED, FRIEND_REJECTED, REMOVE_FRIEND, FRIEND_REQUEST_CANCELLED notification
 * addressed to [userId], this:
 * - updates the friends list if needed
 * - updates pending states of current user's requests if needed
 * - deletes the processed notification Returns the fresh up-to-date profile for the user.
 */
suspend fun getProfileWithSyncedFriendNotifications(
    profileRepository: ProfileRepository,
    notificationsRepository: NotificationsRepository,
    userId: String
): Profile? {
  val notifications = notificationsRepository.getUserNotifications(userId)

  for (notification in notifications) {
    when (notification.type) {
      NotificationType.FRIEND_ACCEPTED -> {
        val senderId = notification.senderId
        if (senderId != null) {
          val senderProfile = profileRepository.getProfileByUid(senderId)
          if (senderProfile != null) {
            profileRepository.addFriend(senderProfile.username, userId)
            profileRepository.removePendingSentFriendUid(userId, senderId)
          }
        }
        notificationsRepository.deleteNotification(notification.id)
      }
      NotificationType.FRIEND_REJECTED -> {
        val senderId = notification.senderId
        if (senderId != null) {
          profileRepository.removePendingSentFriendUid(userId, senderId)
        }
        notificationsRepository.deleteNotification(notificationId = notification.id)
      }
      NotificationType.REMOVE_FRIEND -> {
        val senderId = notification.senderId
        if (senderId != null) {
          val senderProfile = profileRepository.getProfileByUid(senderId)
          if (senderProfile != null) {
            profileRepository.deleteFriend(senderProfile.username, userId)
          }
        }
        notificationsRepository.deleteNotification(notification.id)
      }
      NotificationType.FRIEND_REQUEST_CANCELLED -> {
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
      else -> {}
    }
  }

  return profileRepository.getProfileByUid(userId)
}
