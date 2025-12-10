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

    val startingPoints = 10.0
    val bronzePoints = 30.0
    val silverPoints = 50.0
    val goldPoints = 100.0
    val diamondPoints = 200.0
    val legendPoints = 300.0

    when (badgeToAdd) {
      Badge.STARTING_TODOS_CREATED_BADGE.id ->
          addToPoints(
              Badge.STARTING_TODOS_CREATED_BADGE,
              pointsRepository,
              profileRepository,
              uid,
              startingPoints)
      Badge.BRONZE_TODOS_CREATED_BADGE.id ->
          addToPoints(
              Badge.BRONZE_TODOS_CREATED_BADGE,
              pointsRepository,
              profileRepository,
              uid,
              bronzePoints)
      Badge.SILVER_TODOS_CREATED_BADGE.id ->
          addToPoints(
              Badge.SILVER_TODOS_CREATED_BADGE,
              pointsRepository,
              profileRepository,
              uid,
              silverPoints)
      Badge.GOLD_TODOS_CREATED_BADGE.id ->
          addToPoints(
              Badge.GOLD_TODOS_CREATED_BADGE, pointsRepository, profileRepository, uid, goldPoints)
      Badge.DIAMOND_TODOS_CREATED_BADGE.id ->
          addToPoints(
              Badge.DIAMOND_TODOS_CREATED_BADGE,
              pointsRepository,
              profileRepository,
              uid,
              diamondPoints)
      Badge.LEGEND_TODOS_CREATED_BADGE.id ->
          addToPoints(
              Badge.LEGEND_TODOS_CREATED_BADGE,
              pointsRepository,
              profileRepository,
              uid,
              legendPoints)
      Badge.STARTING_TODOS_COMPLETED_BADGE.id ->
          addToPoints(
              Badge.STARTING_TODOS_COMPLETED_BADGE,
              pointsRepository,
              profileRepository,
              uid,
              startingPoints)
      Badge.BRONZE_TODOS_COMPLETED_BADGE.id ->
          addToPoints(
              Badge.BRONZE_TODOS_COMPLETED_BADGE,
              pointsRepository,
              profileRepository,
              uid,
              bronzePoints)
      Badge.SILVER_TODOS_COMPLETED_BADGE.id ->
          addToPoints(
              Badge.SILVER_TODOS_COMPLETED_BADGE,
              pointsRepository,
              profileRepository,
              uid,
              silverPoints)
      Badge.GOLD_TODOS_COMPLETED_BADGE.id ->
          addToPoints(
              Badge.GOLD_TODOS_COMPLETED_BADGE,
              pointsRepository,
              profileRepository,
              uid,
              goldPoints)
      Badge.DIAMOND_TODOS_COMPLETED_BADGE.id ->
          addToPoints(
              Badge.DIAMOND_TODOS_COMPLETED_BADGE,
              pointsRepository,
              profileRepository,
              uid,
              diamondPoints)
      Badge.LEGEND_TODOS_COMPLETED_BADGE.id ->
          addToPoints(
              Badge.LEGEND_TODOS_COMPLETED_BADGE,
              pointsRepository,
              profileRepository,
              uid,
              legendPoints)
      Badge.STARTING_EVENTS_CREATED_BADGE.id ->
          addToPoints(
              Badge.STARTING_EVENTS_CREATED_BADGE,
              pointsRepository,
              profileRepository,
              uid,
              startingPoints)
      Badge.BRONZE_EVENTS_CREATED_BADGE.id ->
          addToPoints(
              Badge.BRONZE_EVENTS_CREATED_BADGE,
              pointsRepository,
              profileRepository,
              uid,
              bronzePoints)
      Badge.SILVER_EVENTS_CREATED_BADGE.id ->
          addToPoints(
              Badge.SILVER_EVENTS_CREATED_BADGE,
              pointsRepository,
              profileRepository,
              uid,
              silverPoints)
      Badge.GOLD_EVENTS_CREATED_BADGE.id ->
          addToPoints(
              Badge.GOLD_EVENTS_CREATED_BADGE, pointsRepository, profileRepository, uid, goldPoints)
      Badge.DIAMOND_EVENTS_CREATED_BADGE.id ->
          addToPoints(
              Badge.DIAMOND_EVENTS_CREATED_BADGE,
              pointsRepository,
              profileRepository,
              uid,
              diamondPoints)
      Badge.LEGEND_EVENTS_CREATED_BADGE.id ->
          addToPoints(
              Badge.LEGEND_EVENTS_CREATED_BADGE,
              pointsRepository,
              profileRepository,
              uid,
              legendPoints)
      Badge.STARTING_EVENTS_PARTICIPATED_BADGE.id ->
          addToPoints(
              Badge.STARTING_EVENTS_PARTICIPATED_BADGE,
              pointsRepository,
              profileRepository,
              uid,
              startingPoints)
      Badge.BRONZE_EVENTS_PARTICIPATED_BADGE.id ->
          addToPoints(
              Badge.BRONZE_EVENTS_PARTICIPATED_BADGE,
              pointsRepository,
              profileRepository,
              uid,
              bronzePoints)
      Badge.SILVER_EVENTS_PARTICIPATED_BADGE.id ->
          addToPoints(
              Badge.SILVER_EVENTS_PARTICIPATED_BADGE,
              pointsRepository,
              profileRepository,
              uid,
              silverPoints)
      Badge.GOLD_EVENTS_PARTICIPATED_BADGE.id ->
          addToPoints(
              Badge.GOLD_EVENTS_PARTICIPATED_BADGE,
              pointsRepository,
              profileRepository,
              uid,
              goldPoints)
      Badge.DIAMOND_EVENTS_PARTICIPATED_BADGE.id ->
          addToPoints(
              Badge.DIAMOND_EVENTS_PARTICIPATED_BADGE,
              pointsRepository,
              profileRepository,
              uid,
              diamondPoints)
      Badge.LEGEND_EVENTS_PARTICIPATED_BADGE.id ->
          addToPoints(
              Badge.LEGEND_EVENTS_PARTICIPATED_BADGE,
              pointsRepository,
              profileRepository,
              uid,
              legendPoints)
      Badge.STARTING_FRIENDS_BADGE.id ->
          addToPoints(
              Badge.STARTING_FRIENDS_BADGE,
              pointsRepository,
              profileRepository,
              uid,
              startingPoints)
      Badge.BRONZE_FRIENDS_BADGE.id ->
          addToPoints(
              Badge.BRONZE_FRIENDS_BADGE, pointsRepository, profileRepository, uid, bronzePoints)
      Badge.SILVER_FRIENDS_BADGE.id ->
          addToPoints(
              Badge.SILVER_FRIENDS_BADGE, pointsRepository, profileRepository, uid, silverPoints)
      Badge.GOLD_FRIENDS_BADGE.id ->
          addToPoints(
              Badge.GOLD_FRIENDS_BADGE, pointsRepository, profileRepository, uid, goldPoints)
      Badge.DIAMOND_FRIENDS_BADGE.id ->
          addToPoints(
              Badge.DIAMOND_FRIENDS_BADGE, pointsRepository, profileRepository, uid, diamondPoints)
      Badge.LEGEND_FRIENDS_BADGE.id ->
          addToPoints(
              Badge.LEGEND_FRIENDS_BADGE, pointsRepository, profileRepository, uid, legendPoints)
      Badge.STARTING_FOCUS_SESSION_BADGE.id ->
          addToPoints(
              Badge.STARTING_FOCUS_SESSION_BADGE,
              pointsRepository,
              profileRepository,
              uid,
              startingPoints)
      Badge.BRONZE_FOCUS_SESSION_BADGE.id ->
          addToPoints(
              Badge.BRONZE_FOCUS_SESSION_BADGE,
              pointsRepository,
              profileRepository,
              uid,
              bronzePoints)
      Badge.SILVER_FOCUS_SESSION_BADGE.id ->
          addToPoints(
              Badge.SILVER_FOCUS_SESSION_BADGE,
              pointsRepository,
              profileRepository,
              uid,
              silverPoints)
      Badge.GOLD_FOCUS_SESSION_BADGE.id ->
          addToPoints(
              Badge.GOLD_FOCUS_SESSION_BADGE, pointsRepository, profileRepository, uid, goldPoints)
      Badge.DIAMOND_FOCUS_SESSION_BADGE.id ->
          addToPoints(
              Badge.DIAMOND_FOCUS_SESSION_BADGE,
              pointsRepository,
              profileRepository,
              uid,
              diamondPoints)
      Badge.LEGEND_FOCUS_SESSION_BADGE.id ->
          addToPoints(
              Badge.LEGEND_FOCUS_SESSION_BADGE,
              pointsRepository,
              profileRepository,
              uid,
              legendPoints)
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

suspend fun participateEventWithPointsCheck(
    profileRepository: ProfileRepository,
    pointsRepository: PointsRepository,
    eventId: String,
    currentUserId: String
) {
  profileRepository.participateEvent(eventId, currentUserId)

  incrementBadgeCheckPoints(
      profileRepository, pointsRepository, currentUserId, BadgeType.EVENTS_PARTICIPATED)
}
