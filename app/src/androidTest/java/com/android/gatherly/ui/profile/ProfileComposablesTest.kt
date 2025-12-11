package com.android.gatherly.ui.profile

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.gatherly.model.profile.ProfileStatus
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for the profile-related composables:
 * - [ProfilePicture]
 * - [StatusIndicator]
 * - [ProfilePictureWithStatus]
 *
 * These tests ensure that each composable renders correctly and exposes the expected test tags. All
 * tests run in an Android environment because resources, UI semantics, and Compose rendering cannot
 * be evaluated in JVM-only unit tests.
 */
@RunWith(AndroidJUnit4::class)
class ProfileComposablesTest {

  /** Provides a Compose test environment attached to an Android Activity. */
  @get:Rule val composeRule = createComposeRule()

  /** Verifies that the [ProfilePicture] composable renders and exposes its test tag. */
  @Test
  fun profilePicture_rendersSuccessfully() {
    composeRule.setContent {
      ProfilePicture(pictureUrl = "https://example.com/example.png", modifier = Modifier)
    }

    composeRule.onNodeWithTag(ProfileComposablesTestTags.PROFILE_PICTURE).assertIsDisplayed()
  }

  /** Verifies that the [StatusIndicator] composable renders and exposes its test tag. */
  @Test
  fun statusIndicator_rendersSuccessfully() {

    composeRule.setContent {
      StatusIndicator(
          status = ProfileStatus.FOCUSED,
          size = 10.dp,
          testTag = ProfileComposablesTestTags.USER_STATUS,
          modifier = Modifier)
    }
    composeRule.onNodeWithTag(ProfileComposablesTestTags.USER_STATUS).assertIsDisplayed()
  }

  /**
   * Verifies that the [ProfilePictureWithStatus] composable renders both the profile picture and
   * the status indicator, each with its respective test tag.
   */
  @Test
  fun profilePictureWithStatus_rendersPictureAndStatus() {

    composeRule.setContent {
      ProfilePictureWithStatus(
          profilePictureUrl = "https://example.com/example.png",
          status = ProfileStatus.OFFLINE,
          size = 40.dp,
          modifier = Modifier)
    }
    composeRule.onNodeWithTag(ProfileComposablesTestTags.USER_STATUS).assertIsDisplayed()
    composeRule.onNodeWithTag(ProfileComposablesTestTags.PROFILE_PICTURE).assertIsDisplayed()
  }
}
