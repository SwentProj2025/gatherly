package com.android.gatherly.model.map

class FakeNominatimLocationRepository : LocationRepository {
  private val fakeResults = mutableMapOf<String, List<Location>>()

  fun setSearchResults(query: String, results: List<Location>) {
    fakeResults[query] = results
  }

  fun clear() {
    fakeResults.clear()
  }

  override suspend fun search(query: String): List<Location> {
    return fakeResults[query] ?: emptyList()
  }
}
