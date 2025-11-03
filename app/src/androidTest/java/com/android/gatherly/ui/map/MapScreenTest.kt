package com.android.gatherly.ui.map

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.gatherly.model.event.EventsLocalRepository
import com.android.gatherly.model.event.EventsRepository
import com.android.gatherly.model.todo.ToDosRepository
import com.android.gatherly.model.todo.ToDosRepositoryLocalMapTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Tests for the MapScreen composable. */
class MapScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var toDosRepository: ToDosRepository
  private lateinit var eventsRepository: EventsRepository
  private lateinit var mapViewModel: MapViewModel

  @Before
  fun setUp() {
    toDosRepository = ToDosRepositoryLocalMapTest()
    eventsRepository = EventsLocalRepository()
    mapViewModel =
        MapViewModel(todosRepository = toDosRepository, eventsRepository = eventsRepository)

    composeTestRule.setContent { MapScreen(mapViewModel) }
  }

  /** Checks if google map is displayed */
  @Test
  fun google_map_is_displayed() {

    composeTestRule
        .onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN, useUnmergedTree = true)
        .assertExists()
  }
}
