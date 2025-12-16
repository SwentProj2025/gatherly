package com.android.gatherly.model.points

/**
 * Local in-memory implementation of [PointsRepository].
 *
 * This repository stores points in a mutable list and is intended for testing or temporary storage.
 * Data is not persisted and will be lost when the instance is destroyed.
 */
class PointsLocalRepository : PointsRepository {

  private val pointList: MutableList<Points> = mutableListOf()

  override suspend fun getAllPoints(): List<Points> {
    return pointList.toList().sortedByDescending { it.dateObtained }
  }

  override suspend fun addPoints(points: Points) {
    pointList.add(points)
  }
}
