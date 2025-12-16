package com.android.gatherly.ui.groups

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.gatherly.model.group.GroupsLocalRepository
import com.android.gatherly.model.notification.NotificationsLocalRepository
import com.android.gatherly.model.notification.NotificationsRepository
import com.android.gatherly.model.points.PointsLocalRepository
import com.android.gatherly.model.points.PointsRepository
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.utils.MockitoUtils
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

// This class was inspired by the FriendsScreenTest class.

/** Tests the Add Group Display */
class AddGroupScreenTest {

  @get:Rule val composeTestRule = createComposeRule()
  private lateinit var currentUserId: String
  private lateinit var mockitoUtils: MockitoUtils
  private lateinit var profileRepository: ProfileLocalRepository
  private lateinit var notificationsRepository: NotificationsRepository
  private lateinit var groupsRepository: GroupsLocalRepository
  private lateinit var pointsRepository: PointsRepository
  private lateinit var addGroupViewModel: AddGroupViewModel

  /**
   * Helper function: set the content of the composeTestRule with the current user as Alice who has
   * 3 friends
   */
  @OptIn(ExperimentalCoroutinesApi::class)
  private fun setContent() {
    runTest {
      profileRepository = ProfileLocalRepository()
      groupsRepository = GroupsLocalRepository()
      notificationsRepository = NotificationsLocalRepository()
      pointsRepository = PointsLocalRepository()

      profileRepository.addProfile(francisProfile)
      advanceUntilIdle()
      profileRepository.addProfile(charlieProfile)
      advanceUntilIdle()
      profileRepository.addProfile(denisProfile)
      advanceUntilIdle()
      profileRepository.addProfile(aliceProfile)
      advanceUntilIdle()

      currentUserId = aliceProfile.uid

      // Mock Firebase Auth
      mockitoUtils = MockitoUtils()
      mockitoUtils.chooseCurrentUser(currentUserId)

      addGroupViewModel =
          AddGroupViewModel(
              groupsRepository = groupsRepository,
              profileRepository = profileRepository,
              notificationsRepository = notificationsRepository,
              pointsRepository = pointsRepository,
              authProvider = { mockitoUtils.mockAuth })

      composeTestRule.setContent { AddGroupScreen(addGroupViewModel) }
    }
  }

  /*----------------------------------------Profiles--------------------------------------------*/
  val aliceProfile: Profile =
      Profile(
          uid = "AliceID",
          name = "Alice",
          username = "alice",
          groupIds = emptyList(),
          friendUids = listOf("FrancisID", "CharlieID", "DenisID"))
  val francisProfile: Profile =
      Profile(
          uid = "FrancisID",
          name = "Francis",
          username = "francis",
          groupIds = emptyList(),
          friendUids = listOf("AliceID"))

  val charlieProfile: Profile =
      Profile(
          uid = "CharlieID",
          name = "Charlie",
          username = "charlie",
          groupIds = emptyList(),
          friendUids = listOf("AliceID"))

  val denisProfile: Profile =
      Profile(
          uid = "DenisID",
          name = "Denis",
          username = "denis",
          groupIds = emptyList(),
          friendUids = listOf("AliceID"))

  /** Verifies that all friend items are displayed initially */
  @Test
  fun showsAllFriendItems() {
    setContent()

    composeTestRule
        .onNodeWithTag(AddGroupScreenTestTags.getTestTagForFriendCheckbox("charlie"))
        .assertExists()
    composeTestRule
        .onNodeWithTag(AddGroupScreenTestTags.getTestTagForFriendCheckbox("denis"))
        .assertExists()
    composeTestRule
        .onNodeWithTag(AddGroupScreenTestTags.getTestTagForFriendCheckbox("francis"))
        .assertExists()
  }

  /** Verifies that searching for a friend filters the friends list accordingly */
  @Test
  fun friendSearchFiltersList() {
    setContent()

    composeTestRule.onNodeWithTag(AddGroupScreenTestTags.SEARCH_FRIENDS_BAR).performTextInput("cha")

    composeTestRule
        .onNodeWithTag(AddGroupScreenTestTags.getTestTagForFriendCheckbox("charlie"))
        .assertExists()
    composeTestRule
        .onNodeWithTag(AddGroupScreenTestTags.getTestTagForFriendCheckbox("denis"))
        .assertDoesNotExist()
    composeTestRule
        .onNodeWithTag(AddGroupScreenTestTags.getTestTagForFriendCheckbox("francis"))
        .assertDoesNotExist()
  }

  /** Verifies that when trying to create a group with empty name, an error message is shown */
  @Test
  fun emptyGroupNameShowsError() {
    setContent()

    // Create group with empty name
    composeTestRule.onNodeWithTag(AddGroupScreenTestTags.BUTTON_CREATE_GROUP).performClick()

    composeTestRule
        .onNodeWithTag(AddGroupScreenTestTags.NAME_ERROR_MESSAGE, useUnmergedTree = true)
        .assertExists()
  }

  /** Verifies that only the selected friends are saved in the created group */
  @Test
  fun creatingGroupSavesOnlySelectedFriends() = runBlocking {
    setContent()

    // Select Denis
    composeTestRule
        .onNodeWithTag(AddGroupScreenTestTags.getTestTagForFriendCheckbox("denis"))
        .performClick()

    // Select Charlie
    composeTestRule
        .onNodeWithTag(AddGroupScreenTestTags.getTestTagForFriendCheckbox("charlie"))
        .performClick()

    // Toggling Francis Twice (to test unselecting)
    composeTestRule
        .onNodeWithTag(AddGroupScreenTestTags.getTestTagForFriendCheckbox("francis"))
        .performClick()
        .performClick()

    // Check that Denis's profile picture appears in the selected friends section
    composeTestRule
        .onNodeWithTag(AddGroupScreenTestTags.getTestTagForSelectedFriendProfilePicture("denis"))
        .assertExists()

    // Check that Charlie's profile picture appears in the selected friends section
    composeTestRule
        .onNodeWithTag(AddGroupScreenTestTags.getTestTagForSelectedFriendProfilePicture("charlie"))
        .assertExists()

    // Check that Francis's profile picture doesn't appear in the selected friends section
    composeTestRule
        .onNodeWithTag(AddGroupScreenTestTags.getTestTagForSelectedFriendProfilePicture("francis"))
        .assertDoesNotExist()

    // Type group name
    composeTestRule
        .onNodeWithTag(AddGroupScreenTestTags.GROUP_NAME_FIELD)
        .performTextInput("Chess Club")

    // Type group name
    composeTestRule
        .onNodeWithTag(AddGroupScreenTestTags.GROUP_DESCRIPTION_FIELD)
        .assertIsDisplayed()
        .performTextInput("Best chess players")

    composeTestRule.onNodeWithTag(AddGroupScreenTestTags.BUTTON_CREATE_GROUP).performClick()

    // Retrieve groups from repository
    val groups = groupsRepository.getAllGroups()
    assert(groups.size == 1)
    val created = groups.first()

    assertEquals(created.name, "Chess Club")
    assert(created.memberIds.contains("DenisID"))
    assert(created.memberIds.contains("CharlieID"))
    assert(!created.memberIds.contains("FrancisID"))
  }

  /** Verifies that searching, selecting, naming and creating a group works as expected */
  @Test
  fun searchThenSelectThenCreateGroup() = runBlocking {
    setContent()

    // Search for "den"
    composeTestRule
        .onNodeWithTag(AddGroupScreenTestTags.SEARCH_FRIENDS_BAR)
        .assertExists()
        .performTextInput("den")

    // Select Denis
    composeTestRule
        .onNodeWithTag(AddGroupScreenTestTags.getTestTagForFriendCheckbox("denis"))
        .assertExists()
        .performClick()

    // Check that Denis's profile picture appears in the selected friends section
    composeTestRule
        .onNodeWithTag(AddGroupScreenTestTags.getTestTagForSelectedFriendProfilePicture("denis"))
        .assertExists()

    // Check that Charlie's profile picture doesn't appear in the selected friends section
    composeTestRule
        .onNodeWithTag(AddGroupScreenTestTags.getTestTagForSelectedFriendProfilePicture("charlie"))
        .assertDoesNotExist()

    // Check that Francis's profile picture doesn't appear in the selected friends section
    composeTestRule
        .onNodeWithTag(AddGroupScreenTestTags.getTestTagForSelectedFriendProfilePicture("francis"))
        .assertDoesNotExist()

    // Name group
    composeTestRule
        .onNodeWithTag(AddGroupScreenTestTags.GROUP_NAME_FIELD)
        .assertExists()
        .performTextInput("Denis Fanclub")

    composeTestRule
        .onNodeWithTag(AddGroupScreenTestTags.BUTTON_CREATE_GROUP)
        .assertExists()
        .performClick()

    val groups = groupsRepository.getAllGroups()
    assert(groups.size == 1)
    val created = groups.first()

    assertEquals(created.name, "Denis Fanclub")
    assert(created.memberIds.contains("DenisID"))
    assert(!created.memberIds.contains("FrancisID"))
    assert(!created.memberIds.contains("CharlieID"))
  }
}
