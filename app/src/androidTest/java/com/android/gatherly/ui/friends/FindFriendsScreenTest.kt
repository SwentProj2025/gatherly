package com.android.gatherly.ui.friends

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.gatherly.model.notification.NotificationsLocalRepository
import com.android.gatherly.model.notification.NotificationsRepository
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.utils.MockitoUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

private const val TIMEOUT = 30_000L

class FindFriendsScreenTest {

  @get:Rule val composeTestRule = createComposeRule()
  private lateinit var currentUserId: String
  private lateinit var friendsViewModel: FriendsViewModel
  private lateinit var profileRepository: ProfileRepository
  private lateinit var notificationsRepository: NotificationsRepository
  private lateinit var mockitoUtils: MockitoUtils

  /**
   * Helper function: set the content of the composeTestRule with currentUserID Bob who have no
   * friend
   */
  @OptIn(ExperimentalCoroutinesApi::class)
  private fun setContentwithBobUID() {
    runTest {
      profileRepository = ProfileLocalRepository()
      notificationsRepository = NotificationsLocalRepository()

      addProfiles()
      profileRepository.addProfile(bobProfile)
      advanceUntilIdle()

      currentUserId = bobProfile.uid

      // Mock Firebase Auth
      mockitoUtils = MockitoUtils()
      mockitoUtils.chooseCurrentUser(currentUserId)

      friendsViewModel =
          FriendsViewModel(
              profileRepository,
              notificationsRepository = notificationsRepository,
              authProvider = { mockitoUtils.mockAuth })

      addProfiles()

      composeTestRule.setContent { FindFriendsScreen(friendsViewModel) }
    }
  }

  /**
   * Helper function: set the content of the composeTestRule with currentUserID Alice who have 3
   * friends
   */
  @OptIn(ExperimentalCoroutinesApi::class)
  private fun setContentwithAliceUID() {
    runTest {
      profileRepository = ProfileLocalRepository()
      notificationsRepository = NotificationsLocalRepository()
      addProfiles()
      profileRepository.addProfile(aliceProfile)
      advanceUntilIdle()

      currentUserId = aliceProfile.uid

      // Mock Firebase Auth
      mockitoUtils = MockitoUtils()
      mockitoUtils.chooseCurrentUser(currentUserId)

      friendsViewModel =
          FriendsViewModel(
              profileRepository,
              notificationsRepository = notificationsRepository,
              authProvider = { mockitoUtils.mockAuth })

      composeTestRule.setContent { FindFriendsScreen(friendsViewModel) }
    }
  }

  /*----------------------------------------Profiles--------------------------------------------*/
  val bobProfile: Profile =
      Profile(
          uid = "bobID",
          name = "bobby",
          username = "bob",
          groupIds = emptyList(),
          friendUids = emptyList())

  val aliceProfile: Profile =
      Profile(
          uid = "AliceID",
          name = "alicia",
          username = "alice",
          groupIds = emptyList(),
          friendUids = listOf("1", "2", "3"))
  val profile1: Profile =
      Profile(
          uid = "1",
          name = "Profile1",
          username = "francis",
          groupIds = emptyList(),
          friendUids = emptyList())

  val profile2: Profile =
      Profile(
          uid = "2",
          name = "Profile2",
          username = "charlie",
          groupIds = emptyList(),
          friendUids = emptyList())

  val profile3: Profile =
      Profile(
          uid = "3",
          name = "Profile3",
          username = "denis",
          groupIds = emptyList(),
          friendUids = emptyList())

  /** Helper function : fills the profile repository with created profiles */
  @OptIn(ExperimentalCoroutinesApi::class)
  fun addProfiles() {
    runTest {
      profileRepository.addProfile(profile1)
      advanceUntilIdle()
      profileRepository.addProfile(profile2)
      advanceUntilIdle()
      profileRepository.addProfile(profile3)
      advanceUntilIdle()
    }
  }

  /**
   * Test: Verifies that when the user got no friend, all relevant UI components are displayed
   * correctly.
   */
  @Test
  fun testTagsCorrectlySetWhenListAreEmpty() {
    setContentwithAliceUID()
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
    setContentwithBobUID()
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
        .onNodeWithTag(FindFriendsScreenTestTags.getTestTagForFriendFollowButton("francis"))
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(FindFriendsScreenTestTags.getTestTagForFriendItem("charlie"))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(FindFriendsScreenTestTags.getTestTagForFriendUsername("charlie"))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(FindFriendsScreenTestTags.getTestTagForFriendFollowButton("charlie"))
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(FindFriendsScreenTestTags.getTestTagForFriendItem("denis"))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(FindFriendsScreenTestTags.getTestTagForFriendUsername("denis"))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(FindFriendsScreenTestTags.getTestTagForFriendFollowButton("denis"))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(FindFriendsScreenTestTags.getTestTagForFriendFollowButton("denis"))
        .assertIsDisplayed()
  }

  /** Test: Verifies that the user can click to the friend item to follow this user */
  @Test
  fun testClickToFollow() {
    runTest {
      setContentwithBobUID()
      composeTestRule.waitForIdle()

      composeTestRule
          .onNodeWithTag(FindFriendsScreenTestTags.getTestTagForFriendFollowButton("francis"))
          .assertIsDisplayed()
          .performClick()

      composeTestRule
          .onNodeWithTag(FindFriendsScreenTestTags.getTestTagForFriendItem("francis"))
          .assertIsNotDisplayed()
    }
  }

  /**
   * Test: Verifies that the user can search an user username and the screen display only the
   * correct profiles item
   */
  @Test
  fun testUserSearchBar() {
    setContentwithBobUID()
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
      profileRepository = ProfileLocalRepository()
      notificationsRepository = NotificationsLocalRepository()
      addProfiles()
      profileRepository.addProfile(aliceProfile)

      currentUserId = aliceProfile.uid

      // Mock Firebase Auth
      mockitoUtils = MockitoUtils()
      mockitoUtils.chooseCurrentUser(currentUserId)

      friendsViewModel =
          FriendsViewModel(profileRepository, notificationsRepository, { mockitoUtils.mockAuth })

      composeTestRule.setContent { FindFriendsScreen(friendsViewModel) }

      composeTestRule.waitForIdle()

      if (friendsViewModel.uiState.value.isLoading) {
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
      setContentwithBobUID()
      composeTestRule.waitForIdle()

      composeTestRule.mainClock.autoAdvance = false
      val animationDelay = 2000L

      val friendToFollow = "francis"
      val unfollowButtonTag =
          FindFriendsScreenTestTags.getTestTagForFriendFollowButton(friendToFollow)
      val followMessage = FindFriendsScreenTestTags.FOLLOWING_TEXT_ANIMATION
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
}
