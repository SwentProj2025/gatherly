package com.android.gatherly.ui.badge

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.utils.MockitoUtils
import com.android.gatherly.utils.UI_WAIT_TIMEOUT
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Class to test if all UI elements of BadgeScreen are displayed */
class BadgeScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var profileRepository: ProfileRepository
  private lateinit var mockitoUtils: MockitoUtils
  private lateinit var currentUserId: String

  @Before
  fun setUp() {
    profileRepository = ProfileLocalRepository()
    currentUserId = ""
    mockitoUtils = MockitoUtils()
  }

  /** Test to check that all badgeItems are displayed */
  @Test
  fun badgesTestTagsAreDisplayed() {
    mockitoUtils.chooseCurrentUser("bobId")
    currentUserId = "bobId"
    val viewModel =
        BadgeViewModel(repository = profileRepository, authProvider = { mockitoUtils.mockAuth })

    composeTestRule.setContent { BadgeScreen(viewModel = viewModel, goBack = {}) }

    composeTestRule.waitForIdle()

    composeTestRule.waitUntil(UI_WAIT_TIMEOUT) {
      composeTestRule
          .onNodeWithTag(BadgeScreenTestTags.TODO_TITLE, useUnmergedTree = true)
          .isDisplayed()
    }

    composeTestRule
        .onNodeWithTag(BadgeScreenTestTags.EVENT_TITLE, useUnmergedTree = true)
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(BadgeScreenTestTags.FOCUS_TITLE, useUnmergedTree = true)
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(BadgeScreenTestTags.FRIEND_TITLE, useUnmergedTree = true)
        .assertIsDisplayed()
  }
}
