package com.android.gatherly.ui.notifications

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented tests for the Friend Requests Screen UI components and their behavior.
 *
 * These tests ensure that the Friend Requests Screen renders correctly, displays friend request
 * items as expected, and handles loading states appropriately.
 */
class FriendRequestsScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var helper: NotificationsTestHelper
  private lateinit var environment: NotificationsTestHelper.TestEnvironment

  @Before
  fun setup() {
    helper = NotificationsTestHelper(composeTestRule)
  }

  /**
   * Test: Verifies that no friend request items are displayed when the user has no friend requests.
   */
  @Test
  fun testTagsCorrectlySetWhenListAreEmpty() {
    environment =
        helper.setupWithAliceUID(NotificationsTestHelper.ScreenType.FRIEND_REQUESTS_SCREEN)
    composeTestRule.onNodeWithTag(FriendRequestsScreenTestTags.EMPTY_LIST_MSG).assertIsDisplayed()
    assertFriendRequestItemNotDisplayed("bob")
    assertFriendRequestItemNotDisplayed("alice")
    assertFriendRequestItemNotDisplayed("francis")
    assertFriendRequestItemNotDisplayed("charlie")
  }

  /**
   * Test : Verifies that friend request items are displayed correctly when the user has multiple
   * friend requests.
   */
  @Test
  fun testDisplayCorrectlyFriendRequests() {
    environment = helper.setupWithBobUID(NotificationsTestHelper.ScreenType.FRIEND_REQUESTS_SCREEN)
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(FriendRequestsScreenTestTags.EMPTY_LIST_MSG)
        .assertIsNotDisplayed()

    assertFriendRequestItemDisplayed("francis")
    assertFriendRequestItemDisplayed("alice")
    assertFriendRequestItemDisplayed("charlie")
    assertFriendRequestItemNotDisplayed("bob")
  }

  /** Helper: Asserts that a friend request item is displayed correctly. */
  fun assertFriendRequestItemDisplayed(username: String) {
    composeTestRule
        .onNodeWithTag(FriendRequestsScreenTestTags.getTestTagForFriendRequestItem(username))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(FriendRequestsScreenTestTags.getTestTagForSenderUsername(username))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(FriendRequestsScreenTestTags.getTestTagForSenderName(username))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(
            FriendRequestsScreenTestTags.getTestTagForSenderProfilePicture(username),
            useUnmergedTree = true)
        .assertExists()
    composeTestRule
        .onNodeWithTag(FriendRequestsScreenTestTags.getTestTagForAcceptButton(username))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(FriendRequestsScreenTestTags.getTestTagForRejectButton(username))
        .assertIsDisplayed()
  }

  /** Helper: Asserts that a friend request item is not displayed */
  fun assertFriendRequestItemNotDisplayed(username: String) {
    composeTestRule
        .onNodeWithTag(FriendRequestsScreenTestTags.getTestTagForFriendRequestItem(username))
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag(FriendRequestsScreenTestTags.getTestTagForSenderUsername(username))
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag(FriendRequestsScreenTestTags.getTestTagForSenderName(username))
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag(
            FriendRequestsScreenTestTags.getTestTagForSenderProfilePicture(username),
            useUnmergedTree = true)
        .assertDoesNotExist()
    composeTestRule
        .onNodeWithTag(FriendRequestsScreenTestTags.getTestTagForAcceptButton(username))
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag(FriendRequestsScreenTestTags.getTestTagForRejectButton(username))
        .assertIsNotDisplayed()
  }

  /** Test: Verifies when the screen is loading all friend requests, an animation is displayed. */
  @Test
  fun testLoadingCircle() {
    runTest {
      environment =
          helper.setupWithAliceUID(NotificationsTestHelper.ScreenType.FRIEND_REQUESTS_SCREEN)

      composeTestRule.waitForIdle()

      if (environment.notificationsViewModel.uiState.value.isLoading) {
        composeTestRule
            .onNodeWithTag(FriendRequestsScreenTestTags.LOADING_CIRCLE)
            .assertIsDisplayed()
      }
    }
  }
}
