package com.android.gatherly.model.map

interface LocationRepository {
  suspend fun search(query: String): List<Location>
}
