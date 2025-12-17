package com.android.gatherly.model.todoCategory

import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [ToDoCategoryLocalRepository].
 *
 * Uses an [UnconfinedTestDispatcher] with a timeout to ensure coroutines complete safely.
 */
class ToDoCategoryLocalRepositoryTest {

  private lateinit var repository: ToDoCategoryLocalRepository

  // Test dispatcher for coroutines
  @OptIn(ExperimentalCoroutinesApi::class) private val testDispatcher = UnconfinedTestDispatcher()

  // Maximum allowed time for a test coroutine
  private val testTimeout = 120.seconds

  @Before
  fun setUp() {
    repository = ToDoCategoryLocalRepository()
  }

  /** Verifies that getNewId generates unique incremental IDs. */
  @Test
  fun getNewId_generatesIncrementalIds() {
    val id1 = repository.getNewId()
    val id2 = repository.getNewId()

    assertEquals("0", id1)
    assertEquals("1", id2)
  }

  /** Verifies that default categories are initialized correctly. */
  @Test
  fun initializeDefaultCategories_addsAllDefaults() =
      runTest(testDispatcher, testTimeout) {
        repository.initializeDefaultCategories()

        val categories = repository.getAllCategories()
        assertEquals(DEFAULT_CATEGORIES.size, categories.size)
        assertTrue(categories.containsAll(DEFAULT_CATEGORIES))
      }

  /** Verifies that a category can be added and retrieved. */
  @Test
  fun addToDoCategory_addsCategory() =
      runTest(testDispatcher, testTimeout) {
        repository.addToDoCategory(ToDoCategoryLocalRepositoryTestData.customCategory)

        val categories = repository.getAllCategories()
        assertEquals(1, categories.size)
        assertEquals(ToDoCategoryLocalRepositoryTestData.customCategory, categories.first())
      }

  /** Verifies that an existing category can be deleted by ID. */
  @Test
  fun deleteToDoCategory_removesCategory() =
      runTest(testDispatcher, testTimeout) {
        repository.addToDoCategory(ToDoCategoryLocalRepositoryTestData.customCategory)

        repository.deleteToDoCategory(ToDoCategoryLocalRepositoryTestData.customCategory.id)

        val categories = repository.getAllCategories()
        assertTrue(categories.isEmpty())
      }

  /** Verifies that deleting a non-existent category throws an exception. */
  @Test(expected = Exception::class)
  fun deleteToDoCategory_throwsWhenCategoryNotFound() =
      runTest(testDispatcher, testTimeout) { repository.deleteToDoCategory("non_existing_id") }
}
