package com.android.gatherly.utilstest

import com.android.gatherly.utils.MapCoordinator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MapCoordinatorTest {

    @Test
    fun requestCenterOnEvent_updatesStateCorrectly() {
        val coordinator = MapCoordinator()
        val eventId = "event_123"

        coordinator.requestCenterOnEvent(eventId)

        assertEquals("Coordinator should hold the ID", eventId, coordinator.getUnconsumedEventId())
    }

    @Test
    fun markConsumed_clearsPendingRequest() {
        val coordinator = MapCoordinator()
        val eventId = "event_123"

        coordinator.requestCenterOnEvent(eventId)
        coordinator.markConsumed()

        assertNull("Coordinator should return null after consumption", coordinator.getUnconsumedEventId())
    }

    @Test
    fun getUnconsumedEventId_returnsNullInitially() {
        val coordinator = MapCoordinator()
        assertNull(coordinator.getUnconsumedEventId())
    }

    @Test
    fun requestCenterOnEvent_overwritesPreviousUnconsumedRequest() {
        val coordinator = MapCoordinator()
        coordinator.requestCenterOnEvent("event_1")
        coordinator.requestCenterOnEvent("event_2")

        assertEquals("Coordinator should hold the latest ID", "event_2", coordinator.getUnconsumedEventId())
    }
}