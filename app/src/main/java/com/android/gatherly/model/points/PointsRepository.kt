package com.android.gatherly.model.points

interface PointsRepository {

  /** Gets all the users [Points] history in chronological order */
  suspend fun getAllPoints(): List<Points>

  /** Adds a [Points] instance to the database */
  suspend fun addPoints(points: Points)
}
