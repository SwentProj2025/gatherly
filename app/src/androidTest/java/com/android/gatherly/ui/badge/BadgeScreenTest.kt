package com.android.gatherly.ui.badge

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.utils.MockitoUtils
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
    composeTestRule.setContent {
      val viewModel =
          BadgeViewModel(repository = profileRepository, authProvider = { mockitoUtils.mockAuth })

      BadgeScreen(viewModel = viewModel, goBack = {})
    }

    val expectedTitles =
        listOf(
            "Blank Todo Created Badge",
            "Blank Todo Completed Badge",
            "Blank Event Created Badge",
            "Blank Event Participated Badge",
            "Blank Friend Badge",
            "Blank Focus Session Badge")

    expectedTitles.forEach { title ->
      composeTestRule
          .onNodeWithTag(BadgeScreenTestTags.badgeTest(title), useUnmergedTree = true)
          .assertExists()
    }
  }
}
