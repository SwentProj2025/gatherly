package com.android.gatherly.model.todoCategory

import com.android.gatherly.utils.FirestoreGatherlyTodoCategoryTest
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Integration tests for [ToDoCategoryRepositoryFirestore] using the Firebase Emulator Suite.
 *
 * These tests assume:
 * - The Firestore and Auth emulators are running locally.
 */
class ToDoCategoryFirestoreTest : FirestoreGatherlyTodoCategoryTest() {

  @Test
  fun add_and_getAll_works() =
      runTest(timeout = 120.seconds) {
        repository.initializeDefaultCategories()
        repository.addToDoCategory(tag1)

        val todoCategories = repository.getAllCategories()
        assertEquals(5, todoCategories.size)
        assertTrue(todoCategories.any { it.name == "Sport" })
        assertTrue(todoCategories.any { it.name == TAG_HOMEWORK.name })
        assertTrue(todoCategories.any { it.name == TAG_PROJECT.name })
        assertTrue(todoCategories.any { it.name == TAG_COURSES.name })
        assertTrue(todoCategories.any { it.name == TAG_PERSONAL.name })
      }

  @Test
  fun init_default_tags_works() =
      runTest(timeout = 120.seconds) {
        repository.initializeDefaultCategories()

        val todoCategories = repository.getAllCategories()
        assertEquals(4, todoCategories.size)
        assertTrue(todoCategories.any { it.name == TAG_HOMEWORK.name })
        assertTrue(todoCategories.any { it.name == TAG_PROJECT.name })
        assertTrue(todoCategories.any { it.name == TAG_COURSES.name })
        assertTrue(todoCategories.any { it.name == TAG_PERSONAL.name })
      }

  @Test
  fun delete_default_tag_works() =
      runTest(timeout = 120.seconds) {
        repository.initializeDefaultCategories()
        val todoCategories = repository.getAllCategories()
        assertEquals(4, todoCategories.size)
        repository.deleteToDoCategory(TAG_HOMEWORK.id)
        val todoCategories2 = repository.getAllCategories()
        assertEquals(3, todoCategories2.size)
        assertFalse(todoCategories2.any { it.name == TAG_HOMEWORK.name })
        assertTrue(todoCategories2.any { it.name == TAG_PROJECT.name })
        assertTrue(todoCategories2.any { it.name == TAG_COURSES.name })
        assertTrue(todoCategories2.any { it.name == TAG_PERSONAL.name })
      }

  @Test
  fun delete_created_tag_works() =
      runTest(timeout = 120.seconds) {
        repository.addToDoCategory(tag1)
        val todoCategories = repository.getAllCategories()
        assertEquals(1, todoCategories.size)
        repository.deleteToDoCategory(todoCategories.first().id)

        val todoCategories2 = repository.getAllCategories()
        assertEquals(0, todoCategories2.size)

        assertFalse(todoCategories2.any { it.name == tag1.name })
      }
}
