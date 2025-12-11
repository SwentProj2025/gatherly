package com.android.gatherly.utils

import com.android.gatherly.model.badge.Badge
import com.android.gatherly.model.badge.BadgeRank
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

    val startingPoints = 10.0
    val bronzePoints = 30.0
    val silverPoints = 50.0
    val goldPoints = 100.0
    val diamondPoints = 200.0
    val legendPoints = 300.0

    val badgeById = Badge.entries.associateBy { it.id }
    val badge = badgeById[badgeToAdd]

    badge?.let {
      when (it.rank) {
        BadgeRank.BLANK -> Unit
        BadgeRank.STARTING ->
            addToPoints(it, pointsRepository, profileRepository, uid, startingPoints)
        BadgeRank.BRONZE -> addToPoints(it, pointsRepository, profileRepository, uid, bronzePoints)
        BadgeRank.SILVER -> addToPoints(it, pointsRepository, profileRepository, uid, silverPoints)
        BadgeRank.GOLD -> addToPoints(it, pointsRepository, profileRepository, uid, goldPoints)
        BadgeRank.DIAMOND ->
            addToPoints(it, pointsRepository, profileRepository, uid, diamondPoints)
        BadgeRank.LEGEND -> addToPoints(it, pointsRepository, profileRepository, uid, legendPoints)
      }
    }
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
 * @param obtained the number of points obtained by the user
 */
suspend fun addToPoints(
    badge: Badge,
    pointsRepository: PointsRepository,
    profileRepository: ProfileRepository,
    uid: String,
    obtained: Double
) {
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
