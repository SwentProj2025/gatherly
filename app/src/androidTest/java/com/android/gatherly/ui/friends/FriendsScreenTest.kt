package com.android.gatherly.ui.friends

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import com.android.gatherly.ui.navigation.NavigationTestTags
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

private const val TIMEOUT = 30_000L

class FriendsScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var helper: FriendsScreensTestHelper
  private lateinit var environment: FriendsScreensTestHelper.TestEnvironment

  @Before
  fun setup() {
    helper = FriendsScreensTestHelper(composeTestRule)
  }

  /** No friends: empty state UI is shown */
  @Test
  fun testTagsCorrectlySetWhenListAreEmpty() {
    environment = helper.setupWithBobUID(FriendsScreensTestHelper.ScreenType.FRIENDS)

    composeTestRule
        .onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
        .assertTextContains("Friends", substring = true, ignoreCase = true)

    composeTestRule.onNodeWithTag(FriendsScreenTestTags.EMPTY_LIST_MSG).assertIsDisplayed()
    composeTestRule.onNodeWithTag(FriendsScreenTestTags.SEARCH_FRIENDS_BAR).assertIsDisplayed()
    composeTestRule.onNodeWithTag(FriendsScreenTestTags.BUTTON_FIND_FRIENDS).assertIsDisplayed()
  }

  /** Empty state button is clickable */
  @Test
  fun testButtonFindFriendClickable() {
    environment = helper.setupWithBobUID(FriendsScreensTestHelper.ScreenType.FRIENDS)

    composeTestRule
        .onNodeWithTag(FriendsScreenTestTags.BUTTON_FIND_FRIENDS)
        .assertIsDisplayed()
        .performClick()
  }

  /** Friends list displays correctly */
  @Test
  fun testDisplayCorrectlyFriends() {
    environment = helper.setupWithAliceUID(FriendsScreensTestHelper.ScreenType.FRIENDS)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(FriendsScreenTestTags.EMPTY_LIST_MSG).assertDoesNotExist()
    composeTestRule.onNodeWithTag(FriendsScreenTestTags.FRIENDS_SECTION_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(FriendsScreenTestTags.PENDING_SECTION_TITLE).assertDoesNotExist()

    listOf("francis", "charlie", "denis").forEach { username ->
      composeTestRule
          .onNodeWithTag(FriendsScreenTestTags.getTestTagForFriendItem(username))
          .assertIsDisplayed()

      composeTestRule
          .onNodeWithTag(FriendsScreenTestTags.getTestTagForFriendUsername(username))
          .assertIsDisplayed()

      composeTestRule
          .onNodeWithTag(FriendsScreenTestTags.getTestTagForFriendUnfriendButton(username))
          .assertIsDisplayed()
    }
  }

  /** Unfriend removes item from UI */
  @Test
  fun testClickToUnfriend() = runTest {
    environment = helper.setupWithAliceUID(FriendsScreensTestHelper.ScreenType.FRIENDS)
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(FriendsScreenTestTags.getTestTagForFriendUnfriendButton("francis"))
        .performClick()

    composeTestRule.waitUntil(TIMEOUT) {
      !environment.friendsViewModel.uiState.value.friends.contains("francis")
    }

    composeTestRule
        .onNodeWithTag(FriendsScreenTestTags.getTestTagForFriendItem("francis"))
        .assertDoesNotExist()
  }

  /** Search filters friends */
  @Test
  fun testFriendSearchBar() {
    environment = helper.setupWithAliceUID(FriendsScreensTestHelper.ScreenType.FRIENDS)
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(FriendsScreenTestTags.SEARCH_FRIENDS_BAR)
        .performTextInput("denis")

    composeTestRule
        .onNodeWithTag(FriendsScreenTestTags.getTestTagForFriendItem("denis"))
        .assertIsDisplayed()

    listOf("francis", "charlie").forEach {
      composeTestRule
          .onNodeWithTag(FriendsScreenTestTags.getTestTagForFriendItem(it))
          .assertDoesNotExist()
    }
  }

  /** Heart-breaking animation appears and disappears */
  @Test
  fun testHeartBreakingAnimation() = runTest {
    environment = helper.setupWithAliceUID(FriendsScreensTestHelper.ScreenType.FRIENDS)
    composeTestRule.waitForIdle()

    composeTestRule.mainClock.autoAdvance = false

    val friend = "francis"
    val unfollowButton = FriendsScreenTestTags.getTestTagForFriendUnfriendButton(friend)

    composeTestRule.onNodeWithTag(unfollowButton).performClick()
    composeTestRule.mainClock.advanceTimeBy(100)

    composeTestRule
        .onNodeWithTag(FriendsScreenTestTags.UNFRIENDING_TEXT_ANIMATION)
        .assertIsDisplayed()

    composeTestRule.onNodeWithTag(FriendsScreenTestTags.HEART_BREAK_ANIMATION).assertIsDisplayed()

    composeTestRule.mainClock.advanceTimeBy(2_000)

    composeTestRule
        .onNodeWithTag(FriendsScreenTestTags.UNFRIENDING_TEXT_ANIMATION)
        .assertDoesNotExist()

    composeTestRule.onNodeWithTag(FriendsScreenTestTags.HEART_BREAK_ANIMATION).assertDoesNotExist()

    composeTestRule
        .onNodeWithTag(FriendsScreenTestTags.getTestTagForFriendItem(friend))
        .assertDoesNotExist()

    composeTestRule.mainClock.autoAdvance = true
  }

  /** Pending-only user */
  @Test
  fun testPendingRequestsDisplayCorrectly() {
    environment = helper.setupWithPendingProfile(FriendsScreensTestHelper.ScreenType.FRIENDS)
    composeTestRule.waitForIdle()

    // Wait for VM state to be populated
    composeTestRule.waitUntil(TIMEOUT) {
      environment.friendsViewModel.uiState.value.pendingSentUsernames.size == 3
    }

    // Friends section must NOT appear
    composeTestRule.onNodeWithTag(FriendsScreenTestTags.FRIENDS_SECTION_TITLE).assertDoesNotExist()

    // Pending title must appear (scroll to it in case it is below the fold)
    composeTestRule
        .onNodeWithTag(FriendsScreenTestTags.PENDING_SECTION_TITLE)
        .performScrollTo()
        .assertIsDisplayed()

    val expectedUsernames = listOf("francis", "charlie", "denis")
    expectedUsernames.forEach { username ->
      val itemTag = FriendsScreenTestTags.getTestTagForPendingFriendItem(username)
      val usernameTag = FriendsScreenTestTags.getTestTagForPendingFriendUsername(username)
      val picTag = FriendsScreenTestTags.getTestTagForPendingFriendProfilePicture(username)
      val cancelTag = FriendsScreenTestTags.getTestTagForPendingFriendCancelRequestButton(username)

      composeTestRule.onNodeWithTag(itemTag).performScrollTo().assertIsDisplayed()
      composeTestRule.onNodeWithTag(usernameTag).performScrollTo().assertIsDisplayed()

      // Profile picture might be merged, keep assertExists (not displayed requirement)
      composeTestRule.onNodeWithTag(picTag, useUnmergedTree = true).assertExists()

      composeTestRule.onNodeWithTag(cancelTag).performScrollTo().assertIsDisplayed()
    }
  }

  /** Cancel pending request removes it */
  @Test
  fun testCancelPendingFriendRequest() = runTest {
    environment = helper.setupWithPendingProfile(FriendsScreensTestHelper.ScreenType.FRIENDS)
    composeTestRule.waitForIdle()

    val target = "francis"
    composeTestRule
        .onNodeWithTag(FriendsScreenTestTags.getTestTagForPendingFriendCancelRequestButton(target))
        .performClick()

    composeTestRule.waitUntil {
      !environment.friendsViewModel.uiState.value.pendingSentUsernames.contains(target)
    }

    composeTestRule
        .onNodeWithTag(FriendsScreenTestTags.getTestTagForPendingFriendItem(target))
        .assertDoesNotExist()
  }

  /** Friends + pending together */
  @Test
  fun testFriendsAndPendingRequestsBothDisplayedCorrectly() {
    environment = helper.setupWithTotalProfile(FriendsScreensTestHelper.ScreenType.FRIENDS)
    composeTestRule.waitForIdle()

    composeTestRule.waitUntil(TIMEOUT) {
      environment.friendsViewModel.uiState.value.pendingSentUsernames.size == 1 &&
          environment.friendsViewModel.uiState.value.friends.size == 2
    }

    val listNode = composeTestRule.onNodeWithTag(FriendsScreenTestTags.FRIENDS_LIST)

    // Scroll to pending section title (more robust than performScrollTo on the title itself)
    listNode.performScrollToNode(hasTestTag(FriendsScreenTestTags.PENDING_SECTION_TITLE))
    composeTestRule.onNodeWithTag(FriendsScreenTestTags.PENDING_SECTION_TITLE).assertIsDisplayed()

    // Scroll to pending item
    listNode.performScrollToNode(
        hasTestTag(FriendsScreenTestTags.getTestTagForPendingFriendItem("charlie")))
    composeTestRule
        .onNodeWithTag(FriendsScreenTestTags.getTestTagForPendingFriendItem("charlie"))
        .assertIsDisplayed()

    // Friends section should exist too
    listNode.performScrollToNode(hasTestTag(FriendsScreenTestTags.FRIENDS_SECTION_TITLE))
    composeTestRule.onNodeWithTag(FriendsScreenTestTags.FRIENDS_SECTION_TITLE).assertIsDisplayed()
  }
}
