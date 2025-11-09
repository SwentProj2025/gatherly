package com.android.gatherly.ui.friends

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.utils.FirestoreGatherlyProfileTest
import com.android.gatherly.utils.FirestoreGatherlyTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

private const val TIMEOUT = 30_000L

class FriendsScreenTest : FirestoreGatherlyTest() {

  @get:Rule val composeTestRule = createComposeRule()
  private lateinit var currentUserId: String
  private lateinit var friendsViewModel: FriendsViewModel
  private lateinit var profileRepository: ProfileRepository

  @Before
  override fun setUp() {
    super.setUp()
  }

  /**
   * Helper function: set the content of the composeTestRule with currentUserID Bob who have no
   * friend
   */
  private fun setContentwithBobUID() {
    runTest {
      profileRepository = ProfileLocalRepository()

      profileRepository.addProfile(bobProfile)

      currentUserId = bobProfile.uid

      friendsViewModel = FriendsViewModel(profileRepository, currentUserId)

      addProfiles()

      composeTestRule.setContent { FriendsScreen(friendsViewModel) }
    }
  }

  /**
   * Helper function: set the content of the composeTestRule with currentUserID Alice who have 3
   * friends
   */
  private fun setContentwithAliceUID() {
    runTest {
      profileRepository = ProfileLocalRepository()

      addProfiles()
      profileRepository.addProfile(aliceProfile)

      currentUserId = aliceProfile.uid

      friendsViewModel = FriendsViewModel(profileRepository, currentUserId)

      composeTestRule.setContent { FriendsScreen(friendsViewModel) }
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
    setContentwithBobUID()
    composeTestRule
        .onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
        .assertTextContains("Friends", substring = true, ignoreCase = true)
    composeTestRule.onNodeWithTag(FriendsScreenTestTags.EMPTY_LIST_MSG).assertIsDisplayed()
    composeTestRule.onNodeWithTag(FriendsScreenTestTags.SEARCH_FRIENDS_BAR).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(FriendsScreenTestTags.BUTTON_FIND_FRIENDS).assertIsDisplayed()
  }

  /**
   * Test: Verifies that when the user got no friend he can click on the button to navigate to
   * FindFriends screen.
   */
  @Test
  fun testButtonFindFriendClikable() {
    setContentwithBobUID()
    composeTestRule.onNodeWithTag(FriendsScreenTestTags.EMPTY_LIST_MSG).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(FriendsScreenTestTags.BUTTON_FIND_FRIENDS)
        .assertIsDisplayed()
        .performClick()
  }

  /** Test : Verifies that when the user got 3 friends, the friends items display correctly */
  @Test
  fun testDisplayCorrectlyFriends() {
    setContentwithAliceUID()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(FriendsScreenTestTags.BUTTON_FIND_FRIENDS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(FriendsScreenTestTags.SEARCH_FRIENDS_BAR).assertIsDisplayed()
    composeTestRule.onNodeWithTag(FriendsScreenTestTags.EMPTY_LIST_MSG).assertIsNotDisplayed()
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
        .onNodeWithTag(FriendsScreenTestTags.getTestTagForFriendUnfollowButton("francis"))
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(FriendsScreenTestTags.getTestTagForFriendItem("charlie"))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(FriendsScreenTestTags.getTestTagForFriendUsername("charlie"))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(FriendsScreenTestTags.getTestTagForFriendUnfollowButton("charlie"))
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(FriendsScreenTestTags.getTestTagForFriendItem("denis"))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(FriendsScreenTestTags.getTestTagForFriendUsername("denis"))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(FriendsScreenTestTags.getTestTagForFriendUnfollowButton("denis"))
        .assertIsDisplayed()
  }

  /** Test: Verifies that the user can click to the friend item to unfollow this friend */
  @Test
  fun testClickToUnfollow() {
    runTest {
      setContentwithAliceUID()
      composeTestRule.waitForIdle()

      composeTestRule
          .onNodeWithTag(FriendsScreenTestTags.getTestTagForFriendUnfollowButton("francis"))
          .assertIsDisplayed()
          .performClick()

      composeTestRule.waitUntil(TIMEOUT) { !aliceProfile.friendUids.contains("francis") }

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
    setContentwithAliceUID()
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
}
