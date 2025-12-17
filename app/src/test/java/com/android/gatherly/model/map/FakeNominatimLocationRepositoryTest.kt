package com.android.gatherly.model.map

import com.android.gatherly.runUnconfinedTest
import kotlin.time.Duration.Companion.seconds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Unit tests for [FakeNominatimLocationRepository]. */
class FakeNominatimLocationRepositoryTest {

  private lateinit var repository: FakeNominatimLocationRepository
  private val testTimeout = 120.seconds

  @Before
  fun setUp() {
    repository = FakeNominatimLocationRepository()
  }

  /** Ensures that by default, searching returns an empty list. */
  @Test
  fun searchReturnsEmptyListByDefault() =
      runUnconfinedTest(testTimeout) {
        val results = repository.search("anything")
        assertTrue(results.isEmpty())
      }

  /** Ensures that configured search results are returned correctly. */
  @Test
  fun searchReturnsConfiguredResults() =
      runUnconfinedTest(testTimeout) {
        val expectedLocations =
            listOf(
                Location(48.8566, 2.3522, "Paris, France"),
                Location(51.5074, -0.1278, "London, UK"))

        repository.setSearchResults("test query", expectedLocations)

        val results = repository.search("test query")
        assertEquals(expectedLocations, results)
      }
  /** Ensures that different queries return their respective configured results. */
  @Test
  fun searchReturnsDifferentResultsForDifferentQueries() =
      runUnconfinedTest(testTimeout) {
        val parisResults = listOf(Location(48.8566, 2.3522, "Paris, France"))
        val londonResults = listOf(Location(51.5074, -0.1278, "London, UK"))

        repository.setSearchResults("Paris", parisResults)
        repository.setSearchResults("London", londonResults)

        assertEquals(parisResults, repository.search("Paris"))
        assertEquals(londonResults, repository.search("London"))
      }

  /** Ensures that clearing the repository removes all configured results. */
  @Test
  fun clearRemovesAllConfiguredResults() =
      runUnconfinedTest(testTimeout) {
        repository.setSearchResults("Paris", listOf(Location(48.8566, 2.3522, "Paris, France")))

        repository.clear()

        val results = repository.search("Paris")
        assertTrue(results.isEmpty())
      }
}
