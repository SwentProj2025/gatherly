package com.android.gatherly.model.points

interface PointsRepository {

  /** Gets all the user's [Points] history sorted by date, newest first */
  suspend fun getAllPoints(): List<Points>

  /** Adds a [Points] instance to the database */
  suspend fun addPoints(points: Points)
}
