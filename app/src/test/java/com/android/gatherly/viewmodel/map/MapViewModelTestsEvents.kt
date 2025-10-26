package com.android.gatherly.viewmodel.map

import com.android.gatherly.model.event.Event
import com.android.gatherly.model.event.EventStatus
import com.android.gatherly.model.map.Location
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat

object MapViewModelTestsEvents {
  val testLocation1 = Location(latitude = 46.5190, longitude = 6.5668, name = "BC Building")

  /** Test location at Rolex Learning Center on EPFL campus. */
  val testLocation2 =
      Location(latitude = 46.5186, longitude = 6.5661, name = "Rolex Learning Center")

  val christmas2025 = Timestamp(SimpleDateFormat("dd/MM/yyyy").parse("25/12/2025")!!)
  val newYears2023 = Timestamp(SimpleDateFormat("dd/MM/yyyy").parse("01/01/2023")!!)
  val mexicoGP = Timestamp(SimpleDateFormat("dd/MM/yyyy").parse("26/10/2023")!!)
  val startTime = Timestamp(SimpleDateFormat("HH:mm").parse("10:00")!!)
  val endTime = Timestamp(SimpleDateFormat("HH:mm").parse("23:00")!!)

  /** Upcoming event with a valid location. This event should be drawable on the map. */
  val upcomingEventWithLocation1 =
      Event(
          id = "upcoming_location",
          title = "Celebrate Christmas 2025",
          description = "Come celebrate christmas with us in a few months :)",
          creatorName = "Gatherly team",
          location = testLocation1,
          date = christmas2025,
          startTime = startTime,
          endTime = endTime,
          creatorId = "gersende",
          participants =
              listOf("gersende", "colombe", "claire", "gab", "alessandro", "clau", "mohamed"),
          status = EventStatus.UPCOMING)

  /** Another upcoming event with a valid location. This event should be drawable on the map. */
  val upcomingEventWithLocation2 =
      Event(
          id = "upcoming_location_2",
          title = "Christmas anti-party",
          description = ">:(",
          creatorName = "Anti-Gatherly team",
          location = testLocation2,
          date = christmas2025,
          startTime = startTime,
          endTime = endTime,
          creatorId = "anti-gersende",
          participants = listOf("anti-gersende"),
          status = EventStatus.UPCOMING)

  /** Past event with a valid location. This event should NOT be drawable on the map. */
  val pastEventWithLocation =
      Event(
          id = "past_location",
          title = "Celebrate New Years 2023",
          description = "Come celebrate new years with us",
          creatorName = "Gatherly team",
          location = testLocation1,
          date = newYears2023,
          startTime = startTime,
          endTime = endTime,
          creatorId = "gersende",
          participants =
              listOf("gersende", "colombe", "claire", "gab", "alessandro", "clau", "mohamed"),
          status = EventStatus.PAST)

  /** Upcoming event without a location. This event should NOT be drawable on the map. */
  val upcomingEventWithoutLocation =
      Event(
          id = "upcoming_without_location",
          title = "Watch Mexico GP",
          description = "Watch F1 online together",
          creatorName = "Gersende",
          location = null,
          date = mexicoGP,
          startTime = startTime,
          endTime = endTime,
          creatorId = "gersende",
          participants = listOf("gersende"),
          status = EventStatus.ONGOING)

  /** Past event without a location. This event should NOT be drawable on the map. */
  val pastEventWithoutLocation =
      Event(
          id = "past_without_location",
          title = "NY23",
          description = "PAAARRTYYYYYY idk where",
          creatorName = "Gatherly team",
          location = null,
          date = newYears2023,
          startTime = startTime,
          endTime = endTime,
          creatorId = "gersende",
          participants = listOf("gersende"),
          status = EventStatus.PAST)

  /**
   * Comprehensive list of all test events covering all combinations of status and location. Used to
   * verify that the map filtering logic correctly identifies only the drawable events.
   */
  val testEvents: List<Event> =
      listOf(
          upcomingEventWithLocation1,
          upcomingEventWithLocation2,
          upcomingEventWithoutLocation,
          pastEventWithLocation,
          pastEventWithoutLocation)
}
