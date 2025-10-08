package com.android.gatherly.model.todo

import com.android.gatherly.model.map.Location
import com.android.gatherly.utils.FirestoreGatherlyTest
import com.google.firebase.Timestamp
import java.util.NoSuchElementException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * Integration tests for [ToDosRepositoryFirestore] using the Firebase Emulator Suite.
 *
 * These tests assume:
 * - The Firestore and Auth emulators are running locally.
 */
class ToDosRepositoryFirestoreTest : FirestoreGatherlyTest() {

  @Test
  fun add_and_getAll_works() = runTest {
    repository.addTodo(todo1)
    repository.addTodo(todo2)

    val todos = repository.getAllTodos()
    assertEquals(2, todos.size)
    assertTrue(todos.any { it.name == "Buy groceries" })
    assertTrue(todos.any { it.name == "Walk the dog" })
  }

  @Test
  fun getTodo_returns_exact_todo() = runTest {
    repository.addTodo(todo1)

    val retrieved = repository.getTodo(todo1.uid)
    assertEquals(todo1.uid, retrieved.uid)
    assertEquals(todo1.name, retrieved.name)
    assertEquals(todo1.description, retrieved.description)
    assertEquals(todo1.status, retrieved.status)
  }

  @Test
  fun getTodo_throws_when_not_found() = runTest {
    try {
      repository.getTodo("non_existing_id")
      fail("Expected NoSuchElementException to be thrown")
    } catch (e: NoSuchElementException) {
      // Expected
    }
  }

  @Test
  fun addTodo_with_location_and_dueTime_stores_correctly() = runTest {
    val todoWithExtras =
        todo1.copy(uid = "x1", location = Location(46.52, 6.57, "EPFL"), dueTime = Timestamp.now())

    repository.addTodo(todoWithExtras)
    val fetched = repository.getTodo("x1")

    assertEquals("EPFL", fetched.location?.name)
    assertNotNull(fetched.dueTime)
    assertEquals(46.52, fetched.location?.latitude ?: 0.0, 0.0001)
  }

  @Test
  fun editTodo_updates_existing_todo() = runTest {
    repository.addTodo(todo1)

    val updated =
        todo1.copy(
            name = "Buy groceries (updated)", description = "Add coffee", status = ToDoStatus.ENDED)
    repository.editTodo(todo1.uid, updated)

    val fetched = repository.getTodo(todo1.uid)
    assertEquals("Buy groceries (updated)", fetched.name)
    assertEquals("Add coffee", fetched.description)
    assertEquals(ToDoStatus.ENDED, fetched.status)
  }

  @Test
  fun deleteTodo_removes_it() = runTest {
    repository.addTodo(todo1)
    repository.addTodo(todo2)
    assertEquals(2, getTodosCount())

    repository.deleteTodo(todo1.uid)
    val todos = repository.getAllTodos()
    assertEquals(1, todos.size)
    assertEquals(todo2.uid, todos.first().uid)
  }

  @Test
  fun deleteTodo_throws_if_not_found() = runTest {
    try {
      repository.deleteTodo("invalid_id")
      fail("Expected NoSuchElementException to be thrown")
    } catch (e: NoSuchElementException) {
      // Expected
    }
  }

  @Test
  fun toggleStatus_flips_status_correctly() = runTest {
    repository.addTodo(todo1)
    val original = repository.getTodo(todo1.uid)
    assertEquals(ToDoStatus.ONGOING, original.status)

    repository.toggleStatus(todo1.uid)
    val toggled = repository.getTodo(todo1.uid)
    assertEquals(ToDoStatus.ENDED, toggled.status)

    repository.toggleStatus(todo1.uid)
    val backToOngoing = repository.getTodo(todo1.uid)
    assertEquals(ToDoStatus.ONGOING, backToOngoing.status)
  }

  @Test
  fun getAllEndedTodos_returns_only_completed() = runTest {
    val ended = todo1.copy(uid = "a", status = ToDoStatus.ENDED)
    val ongoing = todo2.copy(uid = "b", status = ToDoStatus.ONGOING)
    val ended2 = todo3.copy(uid = "c", status = ToDoStatus.ENDED)

    repository.addTodo(ended)
    repository.addTodo(ongoing)
    repository.addTodo(ended2)

    val endedTodos = repository.getAllEndedTodos()
    assertEquals(2, endedTodos.size)
    assertTrue(endedTodos.all { it.status == ToDoStatus.ENDED })
  }

  @Test
  fun getNewUid_returns_unique_values() {
    val id1 = repository.getNewUid()
    val id2 = repository.getNewUid()
    assertNotEquals(id1, id2)
    assertTrue(id1.isNotEmpty())
  }
}
