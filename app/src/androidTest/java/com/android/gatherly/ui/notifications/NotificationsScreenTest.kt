package com.android.gatherly.ui.notifications

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NotificationsScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var helper: NotificationsTestHelper
  private lateinit var environment: NotificationsTestHelper.TestEnvironment

  @Before
  fun setup() {
    helper = NotificationsTestHelper(composeTestRule)
  }

  /**
   * Test: Verifies that no friend request notifications are displayed when the user has no friend
   * requests.
   */
  @Test
  fun testTagsCorrectlySetWhenListAreEmpty() {
    environment = helper.setupWithAliceUID(NotificationsTestHelper.ScreenType.NOTIFICATIONS_SCREEN)
    composeTestRule.onNodeWithTag(NotificationsScreenTestTags.EMPTY_LIST_MSG).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(NotificationsScreenTestTags.FRIEND_REQUEST_SECTION)
        .assertIsNotDisplayed()
    assertFriendRequestItemsNotDisplayed("bob")
    assertFriendRequestItemsNotDisplayed("alice")
    assertFriendRequestItemsNotDisplayed("francis")
    assertFriendRequestItemsNotDisplayed("charlie")
  }

  /**
   * Test : Verifies that friend request items are displayed correctly when the user has multiple
   * friend request notifications.
   */
  @Test
  fun testDisplayCorrectlyFriendRequests() {
    environment = helper.setupWithBobUID(NotificationsTestHelper.ScreenType.NOTIFICATIONS_SCREEN)
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(NotificationsScreenTestTags.FRIEND_REQUEST_SECTION)
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag(NotificationsScreenTestTags.EMPTY_LIST_MSG).assertIsNotDisplayed()

    assertFriendRequestItemsDisplayed("francis")
    assertFriendRequestItemsDisplayed("alice")
    assertFriendRequestItemsDisplayed("charlie")
    assertFriendRequestItemsNotDisplayed("bob")
  }

  /** Helper: Asserts that a friend request item is displayed */
  fun assertFriendRequestItemsDisplayed(username: String) {
    composeTestRule
        .onNodeWithTag(NotificationsScreenTestTags.getTestTagForFriendRequestItem(username))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(NotificationsScreenTestTags.getTestTagForSenderName(username))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(
            NotificationsScreenTestTags.getTestTagForSenderProfilePictureInNotification(username),
            useUnmergedTree = true)
        .assertExists()
    composeTestRule
        .onNodeWithTag(NotificationsScreenTestTags.getTestTagForVisitProfileButton(username))
        .assertIsDisplayed()
  }

  /** Helper: Asserts that a friend request item is not displayed */
  fun assertFriendRequestItemsNotDisplayed(username: String) {
    composeTestRule
        .onNodeWithTag(NotificationsScreenTestTags.getTestTagForFriendRequestItem(username))
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag(NotificationsScreenTestTags.getTestTagForSenderName(username))
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag(
            NotificationsScreenTestTags.getTestTagForSenderProfilePictureInNotification(username),
            useUnmergedTree = true)
        .assertDoesNotExist()
    composeTestRule
        .onNodeWithTag(NotificationsScreenTestTags.getTestTagForVisitProfileButton(username))
        .assertIsNotDisplayed()
  }

  /**
   * Test: Verifies that the "Friend Requests" section is displayed correctly with the expected
   * profile pictures and text.
   */
  @Test
  fun testUserFriendRequestsSection() {
    environment = helper.setupWithBobUID(NotificationsTestHelper.ScreenType.NOTIFICATIONS_SCREEN)
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(NotificationsScreenTestTags.FRIEND_REQUEST_SECTION)
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(NotificationsScreenTestTags.FRIEND_REQUEST_SECTION_TEXT)
        .assertTextContains("francis + 2 others", substring = true, ignoreCase = true)

    composeTestRule
        .onNodeWithTag(
            NotificationsScreenTestTags.getTestTagForSenderProfilePictureInFriendRequestSection(
                "francis"))
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(
            NotificationsScreenTestTags.getTestTagForSenderProfilePictureInFriendRequestSection(
                "charlie"))
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(
            NotificationsScreenTestTags.getTestTagForSenderProfilePictureInFriendRequestSection(
                "alice"))
        .assertIsDisplayed()
  }

  /** Test: Verifies when the screen is loading all friend requests, an animation is displayed. */
  @Test
  fun testLoadingCircle() {
    runTest {
      environment =
          helper.setupWithAliceUID(NotificationsTestHelper.ScreenType.NOTIFICATIONS_SCREEN)

      composeTestRule.waitForIdle()

      if (environment.notificationsViewModel.uiState.value.isLoading) {
        composeTestRule
            .onNodeWithTag(NotificationsScreenTestTags.LOADING_CIRCLE)
            .assertIsDisplayed()
      }
    }
  }
}
