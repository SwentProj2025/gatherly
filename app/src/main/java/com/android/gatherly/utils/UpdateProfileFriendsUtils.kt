package com.android.gatherly.utils

import com.android.gatherly.model.notification.NotificationType
import com.android.gatherly.model.notification.NotificationsRepository
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileRepository

/**
 * Synchronises the user's friends list with all pending friend-related notifications.
 *
 * For every FRIEND_ACCEPTED or FRIEND_REJECTED notification addressed to [userId], this:
 * - updates the friends list (for FRIEND_ACCEPTED)
 * - deletes the processed notification
 *
 * Returns the fresh up-to-date profile for the user.
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
          }
        }
        notificationsRepository.deleteNotification(notification.id)
      }
      NotificationType.FRIEND_REJECTED -> {
        notificationsRepository.deleteNotification(notificationId = notification.id)
      }
      else -> {}
    }
  }

  return profileRepository.getProfileByUid(userId)
}
