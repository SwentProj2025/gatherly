package com.android.gatherly.model.map

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeNominatimLocationRepositoryTest {

  private lateinit var repository: FakeNominatimLocationRepository

  @Before
  fun setUp() {
    repository = FakeNominatimLocationRepository()
  }

  @Test
  fun searchReturnsEmptyListByDefault() = runTest {
    val results = repository.search("anything")
    assertTrue(results.isEmpty())
  }

  @Test
  fun searchReturnsConfiguredResults() = runTest {
    val expectedLocations =
        listOf(Location(48.8566, 2.3522, "Paris, France"), Location(51.5074, -0.1278, "London, UK"))

    repository.setSearchResults("test query", expectedLocations)

    val results = repository.search("test query")
    assertEquals(expectedLocations, results)
  }

  @Test
  fun searchReturnsDifferentResultsForDifferentQueries() = runTest {
    val parisResults = listOf(Location(48.8566, 2.3522, "Paris, France"))
    val londonResults = listOf(Location(51.5074, -0.1278, "London, UK"))

    repository.setSearchResults("Paris", parisResults)
    repository.setSearchResults("London", londonResults)

    assertEquals(parisResults, repository.search("Paris"))
    assertEquals(londonResults, repository.search("London"))
  }

  @Test
  fun clearRemovesAllConfiguredResults() = runTest {
    repository.setSearchResults("Paris", listOf(Location(48.8566, 2.3522, "Paris, France")))

    repository.clear()

    val results = repository.search("Paris")
    assertTrue(results.isEmpty())
  }
}
