package com.android.gatherly.utilstest

import com.android.gatherly.utils.MapCoordinator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for the MapCoordinator class, verifying its behavior in handling event centering
 * requests.
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
}
