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

class FindFriendsScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var helper: FriendsScreensTestHelper
  private lateinit var environment: FriendsScreensTestHelper.TestEnvironment

  @Before
  fun setup() {
    helper = FriendsScreensTestHelper(composeTestRule)
  }

  /**
   * Test: Verifies that when the user has no friends, all relevant UI components are displayed
   * correctly.
   */
  @Test
  fun testTagsCorrectlySetWhenListAreEmpty() {
    environment = helper.setupWithAliceUID(FriendsScreensTestHelper.ScreenType.FIND_FRIENDS)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
        .assertTextContains("Friends", substring = true, ignoreCase = true)
    composeTestRule.onNodeWithTag(FindFriendsScreenTestTags.EMPTY_LIST_MSG).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(FindFriendsScreenTestTags.SEARCH_FRIENDS_BAR)
        .assertIsNotDisplayed()
  }

  /**
   * Test : Verifies that when the user got multiple possibilities of new friends, the items display
   * correctly
   */
  @Test
  fun testDisplayCorrectlyUsers() {
    environment = helper.setupWithBobUID(FriendsScreensTestHelper.ScreenType.FIND_FRIENDS)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(FindFriendsScreenTestTags.SEARCH_FRIENDS_BAR).assertIsDisplayed()
    composeTestRule.onNodeWithTag(FindFriendsScreenTestTags.EMPTY_LIST_MSG).assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag(FindFriendsScreenTestTags.getTestTagForFriendItem("francis"))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(FindFriendsScreenTestTags.getTestTagForFriendUsername("francis"))
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(
            FindFriendsScreenTestTags.getTestTagForFriendProfilePicture("francis"),
            useUnmergedTree = true)
        .assertExists()
    composeTestRule
        .onNodeWithTag(FindFriendsScreenTestTags.getTestTagForFriendRequestButton("francis"))
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(FindFriendsScreenTestTags.getTestTagForFriendItem("charlie"))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(FindFriendsScreenTestTags.getTestTagForFriendUsername("charlie"))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(FindFriendsScreenTestTags.getTestTagForFriendRequestButton("charlie"))
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(FindFriendsScreenTestTags.getTestTagForFriendItem("denis"))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(FindFriendsScreenTestTags.getTestTagForFriendUsername("denis"))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(FindFriendsScreenTestTags.getTestTagForFriendRequestButton("denis"))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(FindFriendsScreenTestTags.getTestTagForFriendRequestButton("denis"))
        .assertIsDisplayed()
  }

  /** Test: Verifies that the user can click to the friend item to friend request this user */
  @Test
  fun testClickToRequest() {
    runTest {
      environment = helper.setupWithBobUID(FriendsScreensTestHelper.ScreenType.FIND_FRIENDS)
      composeTestRule.waitForIdle()

      composeTestRule
          .onNodeWithTag(FindFriendsScreenTestTags.getTestTagForFriendRequestButton("francis"))
          .assertIsDisplayed()
          .performClick()

      composeTestRule.waitUntil(TIMEOUT) {
        try {
          composeTestRule
              .onNodeWithTag(FindFriendsScreenTestTags.getTestTagForFriendItem("francis"))
              .assertIsNotDisplayed()
          true
        } catch (_: AssertionError) {
          false
        }
      }
    }
  }

  /**
   * Test: Verifies that the user can search an user username and the screen display only the
   * correct profiles item
   */
  @Test
  fun testUserSearchBar() {
    environment = helper.setupWithBobUID(FriendsScreensTestHelper.ScreenType.FIND_FRIENDS)
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(FindFriendsScreenTestTags.SEARCH_FRIENDS_BAR)
        .performTextInput("denis")

    composeTestRule
        .onNodeWithTag(FindFriendsScreenTestTags.getTestTagForFriendItem("denis"))
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(FindFriendsScreenTestTags.getTestTagForFriendItem("francis"))
        .assertIsNotDisplayed()

    composeTestRule
        .onNodeWithTag(FindFriendsScreenTestTags.getTestTagForFriendItem("charlie"))
        .assertIsNotDisplayed()
  }

  /**
   * Test: Verifies when the screen is currently loading all the profiles item, a special animation
   * is displayed.
   */
  @Test
  fun testLoadingAnimation() {
    runTest {
      environment = helper.setupWithAliceUID(FriendsScreensTestHelper.ScreenType.FIND_FRIENDS)

      composeTestRule.waitForIdle()

      if (environment.friendsViewModel.uiState.value.isLoading) {
        composeTestRule
            .onNodeWithTag(FindFriendsScreenTestTags.LOADING_ANIMATION)
            .assertIsDisplayed()
      }
    }
  }

  /**
   * Test: Verifies that when the current user wants to follow a friend, a special animation is
   * displayed.
   */
  @Test
  fun testHeartAnimation() {
    runTest {
      environment = helper.setupWithBobUID(FriendsScreensTestHelper.ScreenType.FIND_FRIENDS)
      composeTestRule.waitForIdle()

      composeTestRule.mainClock.autoAdvance = false
      val animationDelay = 2000L

      val friendToFollow = "francis"
      val unfollowButtonTag =
          FindFriendsScreenTestTags.getTestTagForFriendRequestButton(friendToFollow)
      val followMessage = FindFriendsScreenTestTags.REQUESTING_TEXT_ANIMATION
      val heartAnimation = FindFriendsScreenTestTags.HEART_ANIMATION

      composeTestRule.onNodeWithTag(unfollowButtonTag).performClick()
      composeTestRule.mainClock.advanceTimeBy(100)
      composeTestRule.onNodeWithTag(followMessage).assertIsDisplayed()
      composeTestRule.onNodeWithTag(heartAnimation).assertIsDisplayed()

      composeTestRule.mainClock.advanceTimeBy(animationDelay)
      composeTestRule.onNodeWithText(followMessage, ignoreCase = true).assertIsNotDisplayed()
      composeTestRule.onNodeWithText(heartAnimation, ignoreCase = true).assertIsNotDisplayed()

      composeTestRule
          .onNodeWithTag(FindFriendsScreenTestTags.getTestTagForFriendItem(friendToFollow))
          .assertIsNotDisplayed()
      composeTestRule.mainClock.autoAdvance = true
    }
  }

  @Test
  fun testPendingUsersAreRemovedFromFindList() {
    environment =
        helper.setupWithTotalProfile(
            FriendsScreensTestHelper.ScreenType
                .FIND_FRIENDS) // userTotal: friends=[1,3], pending=[2]

    composeTestRule.waitForIdle()

    // pending user "charlie" (uid=2) must NOT appear
    composeTestRule
        .onNodeWithTag(FindFriendsScreenTestTags.getTestTagForFriendItem("charlie"))
        .assertDoesNotExist()

    // friends should not appear either in FindFriends
    composeTestRule
        .onNodeWithTag(FindFriendsScreenTestTags.getTestTagForFriendItem("francis"))
        .assertDoesNotExist()

    composeTestRule
        .onNodeWithTag(FindFriendsScreenTestTags.getTestTagForFriendItem("denis"))
        .assertDoesNotExist()

    // should show empty list since all are either friends or pending
    composeTestRule.onNodeWithTag(FindFriendsScreenTestTags.EMPTY_LIST_MSG).assertIsDisplayed()
  }
}
