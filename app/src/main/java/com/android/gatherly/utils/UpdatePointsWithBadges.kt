package com.android.gatherly.utils

import com.android.gatherly.model.badge.Badge
import com.android.gatherly.model.badge.BadgeType
import com.android.gatherly.model.points.Points
import com.android.gatherly.model.points.PointsRepository
import com.android.gatherly.model.points.PointsSource
import com.android.gatherly.model.profile.ProfileRepository

/**
 * This function increments the users badge counts and if a badge is obtained, depending on the
 * rank, adds points to the user
 *
 * @param profileRepository the profile repository to call
 * @param pointsRepository the points repository to call
 * @param uid the user's id
 * @param type the type of the badge
 */
suspend fun incrementBadgeCheckPoints(
    profileRepository: ProfileRepository,
    pointsRepository: PointsRepository,
    uid: String,
    type: BadgeType
) {
  // check if there is a badge to be added
  val badgeToAdd = profileRepository.incrementBadge(uid = uid, type = type)

  if (badgeToAdd != null) {
    // add the badge
    profileRepository.addBadge(uid, badgeToAdd)

    val badgeById = Badge.entries.associateBy { it.id }
    val badge = badgeById[badgeToAdd]

    badge?.let { addToPoints(it, pointsRepository, profileRepository, uid) }
  }
}

/**
 * Given a badge type, add the appropriate amount of points to the user ([Points] instance and add
 * to the profile)
 *
 * @param badge the badge for which points were obtained
 * @param pointsRepository the points repository to call
 * @param profileRepository the profile repository to call
 * @param uid the user's id
 */
suspend fun addToPoints(
    badge: Badge,
    pointsRepository: PointsRepository,
    profileRepository: ProfileRepository,
    uid: String
) {
  val obtained = badge.rank.pointsEarned
  pointsRepository.addPoints(
      Points(userId = uid, obtained = obtained, reason = PointsSource.Badge(badge.title)))

  profileRepository.updateFocusPoints(uid, obtained)
}

suspend fun addFriendWithPointsCheck(
    profileRepository: ProfileRepository,
    pointsRepository: PointsRepository,
    friend: String,
    currentUserId: String
) {
  profileRepository.addFriend(friend, currentUserId)

  incrementBadgeCheckPoints(
      profileRepository, pointsRepository, currentUserId, BadgeType.FRIENDS_ADDED)
}
