package com.android.gatherly.ui.friends

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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

  /**
   * Test: Verifies that when the user got no friend, all relevant UI components are displayed
   * correctly.
   */
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

  /**
   * Test: Verifies that when the user got no friend he can click on the button to navigate to
   * FindFriends screen.
   */
  @Test
  fun testButtonFindFriendClikable() {
    environment = helper.setupWithBobUID(FriendsScreensTestHelper.ScreenType.FRIENDS)
    composeTestRule.onNodeWithTag(FriendsScreenTestTags.EMPTY_LIST_MSG).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(FriendsScreenTestTags.BUTTON_FIND_FRIENDS)
        .assertIsDisplayed()
        .performClick()
  }

  /** Test : Verifies that when the user got 3 friends, the friends items display correctly */
  @Test
  fun testDisplayCorrectlyFriends() {
    environment = helper.setupWithAliceUID(FriendsScreensTestHelper.ScreenType.FRIENDS)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(FriendsScreenTestTags.BUTTON_FIND_FRIENDS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(FriendsScreenTestTags.SEARCH_FRIENDS_BAR).assertIsDisplayed()
    composeTestRule.onNodeWithTag(FriendsScreenTestTags.EMPTY_LIST_MSG).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(FriendsScreenTestTags.FRIENDS_SECTION_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(FriendsScreenTestTags.PENDING_SECTION_TITLE).assertDoesNotExist()

    composeTestRule
        .onNodeWithTag(FriendsScreenTestTags.getTestTagForFriendItem("francis"))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(FriendsScreenTestTags.getTestTagForFriendUsername("francis"))
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(
            FriendsScreenTestTags.getTestTagForFriendProfilePicture("francis"),
            useUnmergedTree = true)
        .assertExists()
    composeTestRule
        .onNodeWithTag(FriendsScreenTestTags.getTestTagForFriendUnfriendButton("francis"))
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(FriendsScreenTestTags.getTestTagForFriendItem("charlie"))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(FriendsScreenTestTags.getTestTagForFriendUsername("charlie"))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(FriendsScreenTestTags.getTestTagForFriendUnfriendButton("charlie"))
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(FriendsScreenTestTags.getTestTagForFriendItem("denis"))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(FriendsScreenTestTags.getTestTagForFriendUsername("denis"))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(FriendsScreenTestTags.getTestTagForFriendUnfriendButton("denis"))
        .assertIsDisplayed()
  }

  /** Test: Verifies that the user can click to the friend item to unfollow this friend */
  @Test
  fun testClickToUnfriend() {
    runTest {
      environment = helper.setupWithAliceUID(FriendsScreensTestHelper.ScreenType.FRIENDS)
      composeTestRule.waitForIdle()

      composeTestRule
          .onNodeWithTag(FriendsScreenTestTags.getTestTagForFriendUnfriendButton("francis"))
          .assertIsDisplayed()
          .performClick()

      composeTestRule.waitUntil(TIMEOUT) { !helper.aliceProfile.friendUids.contains("francis") }

      composeTestRule
          .onNodeWithTag(FriendsScreenTestTags.getTestTagForFriendItem("francis"))
          .assertIsNotDisplayed()
    }
  }

  /**
   * Test: Verifies that the user can search a friend username and the screen display only the
   * correct profiles item
   */
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

    composeTestRule
        .onNodeWithTag(FriendsScreenTestTags.getTestTagForFriendItem("francis"))
        .assertIsNotDisplayed()

    composeTestRule
        .onNodeWithTag(FriendsScreenTestTags.getTestTagForFriendItem("charlie"))
        .assertIsNotDisplayed()
  }

  /**
   * Test: Verifies when the screen is currently loading all the profiles item, a special animation
   * is displayed.
   */
  @Test
  fun testLoadingAnimation() {
    runTest {
      environment = helper.setupWithAliceUID(FriendsScreensTestHelper.ScreenType.FRIENDS)

      composeTestRule.waitForIdle()

      if (environment.friendsViewModel.uiState.value.isLoading) {
        composeTestRule.onNodeWithTag(FriendsScreenTestTags.LOADING_ANIMATION).assertIsDisplayed()
      }
    }
  }

  /**
   * Test: Verifies that when the current user wants to unfollow a friend, a special animation is
   * displayed.
   */
  @Test
  fun testHeartBreakingAnimation() {
    runTest {
      environment = helper.setupWithAliceUID(FriendsScreensTestHelper.ScreenType.FRIENDS)
      composeTestRule.waitForIdle()

      composeTestRule.mainClock.autoAdvance = false
      val animationDelay = 2000L

      val friendToUnfollow = "francis"
      val unfollowButtonTag =
          FriendsScreenTestTags.getTestTagForFriendUnfriendButton(friendToUnfollow)
      val unfollowMessage = FriendsScreenTestTags.UNFRIENDING_TEXT_ANIMATION
      val heartBreakAnimation = FriendsScreenTestTags.HEART_BREAK_ANIMATION

      composeTestRule.onNodeWithTag(unfollowButtonTag).performClick()
      composeTestRule.mainClock.advanceTimeBy(100)
      composeTestRule.onNodeWithTag(unfollowMessage).assertIsDisplayed()
      composeTestRule.onNodeWithTag(heartBreakAnimation).assertIsDisplayed()

      composeTestRule.mainClock.advanceTimeBy(animationDelay)
      composeTestRule.onNodeWithText(unfollowMessage, ignoreCase = true).assertIsNotDisplayed()
      composeTestRule.onNodeWithText(heartBreakAnimation, ignoreCase = true).assertIsNotDisplayed()

      composeTestRule
          .onNodeWithTag(FriendsScreenTestTags.getTestTagForFriendItem(friendToUnfollow))
          .assertIsNotDisplayed()
      composeTestRule.mainClock.autoAdvance = true
    }
  }

  /** Test: Friends and Pending section titles appear for a user with both. */
  @Test
  fun testSectionTitlesDisplayedForUserWithFriendsAndPendings() {
    environment = helper.setupWithTotalProfile(FriendsScreensTestHelper.ScreenType.FRIENDS)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(FriendsScreenTestTags.FRIENDS_SECTION_TITLE).assertIsDisplayed()

    composeTestRule.onNodeWithTag(FriendsScreenTestTags.PENDING_SECTION_TITLE).assertIsDisplayed()
  }

  /**
   * Test: A user with ONLY pending requests sees:
   * - the pending section title
   * - all pending items (with profile pic, username, and cancel button)
   * - NO friends section
   */
  @Test
  fun testPendingRequestsDisplayCorrectly() {
    environment = helper.setupWithPendingProfile(FriendsScreensTestHelper.ScreenType.FRIENDS)
    composeTestRule.waitForIdle()

    composeTestRule.waitUntil {
      environment.friendsViewModel.uiState.value.pendingSentUsernames.size == 3
    }

    val expectedUsernames = listOf("francis", "charlie", "denis")
    // Friends section must NOT appear
    composeTestRule.onNodeWithTag(FriendsScreenTestTags.FRIENDS_SECTION_TITLE).assertDoesNotExist()

    // Pending title must appear
    composeTestRule.onNodeWithTag(FriendsScreenTestTags.PENDING_SECTION_TITLE).assertIsDisplayed()

    // Each pending item must be visible
    expectedUsernames.forEach { username ->
      composeTestRule
          .onNodeWithTag(FriendsScreenTestTags.getTestTagForPendingFriendItem(username))
          .assertIsDisplayed()

      composeTestRule
          .onNodeWithTag(FriendsScreenTestTags.getTestTagForPendingFriendUsername(username))
          .assertIsDisplayed()

      composeTestRule
          .onNodeWithTag(FriendsScreenTestTags.getTestTagForPendingFriendProfilePicture(username))
          .assertExists()

      composeTestRule
          .onNodeWithTag(
              FriendsScreenTestTags.getTestTagForPendingFriendCancelRequestButton(username))
          .assertIsDisplayed()
    }
  }

  /** Test: Cancel a pending request removes it from UI. */
  @Test
  fun testCancelPendingFriendRequest() = runTest {
    environment = helper.setupWithPendingProfile(FriendsScreensTestHelper.ScreenType.FRIENDS)
    composeTestRule.waitForIdle()

    composeTestRule.waitUntil {
      environment.friendsViewModel.uiState.value.pendingSentUsernames.size == 3
    }
    val target = "francis"
    val cancelTag = FriendsScreenTestTags.getTestTagForPendingFriendCancelRequestButton(target)

    // Click cancel request
    composeTestRule.onNodeWithTag(cancelTag).performClick()

    // Wait until removed from ViewModel state
    composeTestRule.waitUntil {
      !environment.friendsViewModel.uiState.value.pendingSentUsernames.contains(target)
    }

    // Item should disappear
    composeTestRule
        .onNodeWithTag(FriendsScreenTestTags.getTestTagForPendingFriendItem(target))
        .assertDoesNotExist()
  }

  /** Test: Search filters BOTH friends and pending requests lists. */
  @Test
  fun testSearchFiltersFriendsAndPendingRequests() {
    environment = helper.setupWithTotalProfile(FriendsScreensTestHelper.ScreenType.FRIENDS)
    composeTestRule.waitForIdle()

    composeTestRule.waitUntil {
      environment.friendsViewModel.uiState.value.pendingSentUsernames.size == 1 &&
          environment.friendsViewModel.uiState.value.friends.size == 2
    }

    // Search for "charlie" (a pending request)
    composeTestRule.onNodeWithTag(FriendsScreenTestTags.SEARCH_FRIENDS_BAR).performTextInput("char")

    // charlie must appear (pending)
    composeTestRule
        .onNodeWithTag(FriendsScreenTestTags.getTestTagForPendingFriendItem("charlie"))
        .assertIsDisplayed()

    // friends "francis" and "denis" must NOT appear
    listOf("francis", "denis").forEach { friend ->
      composeTestRule
          .onNodeWithTag(FriendsScreenTestTags.getTestTagForFriendItem(friend))
          .assertIsNotDisplayed()
    }
  }

  /** Test: User with friends AND pending requests shows correct items in both sections. */
  @Test
  fun testFriendsAndPendingRequestsBothDisplayedCorrectly() {
    environment = helper.setupWithTotalProfile(FriendsScreensTestHelper.ScreenType.FRIENDS)
    composeTestRule.waitForIdle()

    // FRIENDS: francis, denis
    listOf("francis", "denis").forEach { friend ->
      composeTestRule
          .onNodeWithTag(FriendsScreenTestTags.getTestTagForFriendItem(friend))
          .assertIsDisplayed()

      composeTestRule
          .onNodeWithTag(FriendsScreenTestTags.getTestTagForFriendUsername(friend))
          .assertIsDisplayed()

      composeTestRule
          .onNodeWithTag(FriendsScreenTestTags.getTestTagForFriendUnfriendButton(friend))
          .assertIsDisplayed()
    }

    // PENDING: charlie
    composeTestRule
        .onNodeWithTag(FriendsScreenTestTags.getTestTagForPendingFriendItem("charlie"))
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(
            FriendsScreenTestTags.getTestTagForPendingFriendCancelRequestButton("charlie"))
        .assertIsDisplayed()
  }
}
