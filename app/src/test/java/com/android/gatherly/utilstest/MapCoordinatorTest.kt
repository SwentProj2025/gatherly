package com.android.gatherly.utilstest

import com.android.gatherly.utils.MapCoordinator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for the MapCoordinator class, verifying its behavior in handling event and todo
 * centering requests.
 */
class MapCoordinatorTest {

  /** Test that requesting to center on an event updates the internal state correctly. */
  @Test
  fun requestCenterOnEvent_updatesStateCorrectly() {
    val coordinator = MapCoordinator()
    val eventId = "event_123"

    coordinator.requestCenterOnEvent(eventId)

    assertEquals("Coordinator should hold the ID", eventId, coordinator.getUnconsumedEventId())
  }

  /** Test that marking an event as consumed clears the pending request. */
  @Test
  fun markConsumed_clearsPendingRequest() {
    val coordinator = MapCoordinator()
    val eventId = "event_123"

    coordinator.requestCenterOnEvent(eventId)
    coordinator.markConsumed()

    assertNull(
        "Coordinator should return null after consumption", coordinator.getUnconsumedEventId())
  }

  /** Test that initially there is no unconsumed event ID. */
  @Test
  fun getUnconsumedEventId_returnsNullInitially() {
    val coordinator = MapCoordinator()
    assertNull(coordinator.getUnconsumedEventId())
  }

  /** Test that a new request overwrites any previous unconsumed request. */
  @Test
  fun requestCenterOnEvent_overwritesPreviousUnconsumedRequest() {
    val coordinator = MapCoordinator()
    coordinator.requestCenterOnEvent("event_1")
    coordinator.requestCenterOnEvent("event_2")

    assertEquals(
        "Coordinator should hold the latest ID", "event_2", coordinator.getUnconsumedEventId())
  }

  /** Test that requesting to center on a todo updates the internal state correctly. */
  @Test
  fun requestCenterOnTodo_updatesStateCorrectly() {
    val coordinator = MapCoordinator()
    val todoId = "todo_123"

    coordinator.requestCenterOnTodo(todoId)

    assertEquals("Coordinator should hold the todo ID", todoId, coordinator.getUnconsumedTodoId())
  }

  /** Test that marking a todo request as consumed clears the pending request. */
  @Test
  fun markConsumed_clearsPendingTodoRequest() {
    val coordinator = MapCoordinator()
    val todoId = "todo_123"

    coordinator.requestCenterOnTodo(todoId)
    coordinator.markConsumed()

    assertNull(
        "Coordinator should return null after todo consumption", coordinator.getUnconsumedTodoId())
  }

  /** Test that initially there is no unconsumed todo ID. */
  @Test
  fun getUnconsumedTodoId_returnsNullInitially() {
    val coordinator = MapCoordinator()
    assertNull(coordinator.getUnconsumedTodoId())
  }

  /** Test that a new todo request overwrites any previous unconsumed todo request. */
  @Test
  fun requestCenterOnTodo_overwritesPreviousUnconsumedTodoRequest() {
    val coordinator = MapCoordinator()

    coordinator.requestCenterOnTodo("todo_1")
    coordinator.requestCenterOnTodo("todo_2")

    assertEquals(
        "Coordinator should hold the latest todo ID", "todo_2", coordinator.getUnconsumedTodoId())
  }

  /** Test that requesting an event clears any pending todo request. */
  @Test
  fun requestCenterOnEvent_clearsPendingTodoRequest() {
    val coordinator = MapCoordinator()

    coordinator.requestCenterOnTodo("todo_123")
    coordinator.requestCenterOnEvent("event_123")

    assertNull("Todo request should be cleared", coordinator.getUnconsumedTodoId())
    assertEquals("event_123", coordinator.getUnconsumedEventId())
  }

  /** Test that requesting a todo clears any pending event request. */
  @Test
  fun requestCenterOnTodo_clearsPendingEventRequest() {
    val coordinator = MapCoordinator()

    coordinator.requestCenterOnEvent("event_123")
    coordinator.requestCenterOnTodo("todo_123")

    assertNull("Event request should be cleared", coordinator.getUnconsumedEventId())
    assertEquals("todo_123", coordinator.getUnconsumedTodoId())
  }
}
