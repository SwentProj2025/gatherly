package com.android.gatherly.ui.map

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.gatherly.utils.FirestoreGatherlyTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Tests for the MapScreen composable. */
class MapScreenTest : FirestoreGatherlyTest() {

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  override fun setUp() {
    super.setUp()
    composeTestRule.setContent { MapScreen() }
  }

  /** Checks if google map is displayed */
  @Test
  fun google_map_is_displayed() {

    composeTestRule
        .onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN, useUnmergedTree = true)
        .assertExists()
  }
}
