package com.android.gatherly.model.points

class PointsLocalRepository : PointsRepository {

  private val events: MutableList<Points> = mutableListOf()

  override suspend fun getAllPoints(): List<Points> {
    return events.toList().sortedByDescending { it.dateObtained }
  }

  override suspend fun addPoints(points: Points) {
    events.add(points)
  }
}
