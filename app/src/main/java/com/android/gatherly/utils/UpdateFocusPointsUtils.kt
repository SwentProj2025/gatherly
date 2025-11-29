package com.android.gatherly.utils

import com.android.gatherly.model.points.Points
import com.android.gatherly.model.points.PointsRepository
import com.android.gatherly.model.profile.ProfileRepository

/**
 * Utility function to add focus points to a user. This ensures that focus points instances and the
 * number of points a user has are always in accordance
 *
 * @param pointsRepository The repository to call for [Points] instance update
 * @param profileRepository The repository to call for focus points update in the profile
 * @param points The [Points] instance to add
 */
suspend fun updateFocusPoints(
    pointsRepository: PointsRepository,
    profileRepository: ProfileRepository,
    points: Points
) {
  profileRepository.updateFocusPoints(points.userId, points.obtained)
  pointsRepository.addPoints(points)
}
