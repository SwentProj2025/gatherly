package com.android.gatherly.ui.groups

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import com.android.gatherly.model.group.Group
import com.android.gatherly.model.group.GroupsLocalRepository
import com.android.gatherly.model.notification.NotificationsLocalRepository
import com.android.gatherly.model.notification.NotificationsRepository
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.utils.MockitoUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

// This class was inspired by AddGroupScreenTest and FriendsScreenTest.

class EditGroupScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var currentUserId: String
  private lateinit var mockitoUtils: MockitoUtils
  private lateinit var profileRepository: ProfileLocalRepository
  private lateinit var notificationsRepository: NotificationsRepository
  private lateinit var groupsRepository: GroupsLocalRepository
  private lateinit var editGroupViewModel: EditGroupViewModel

  private val testGroupId = "group1"

  /*----------------------------------------Profiles--------------------------------------------*/

  private val ownerProfile: Profile =
      Profile(
          uid = "OwnerID",
          name = "Owner",
          username = "owner",
          groupIds = listOf(testGroupId),
          friendUids = listOf("GersID", "ClaireID", "AlessandroID", "ClaudiaID"))

  private val gersProfile: Profile =
      Profile(
          uid = "GersID",
          name = "Gers",
          username = "gers",
          groupIds = emptyList(),
          friendUids = listOf("OwnerID"))

  private val claireProfile: Profile =
      Profile(
          uid = "ClaireID",
          name = "Claire",
          username = "claire",
          groupIds = emptyList(),
          friendUids = listOf("OwnerID"))

  private val alessandroProfile: Profile =
      Profile(
          uid = "AlessandroID",
          name = "Alessandro",
          username = "alessandro",
          groupIds = emptyList(),
          friendUids = listOf("OwnerID"))

  private val claudiaProfile: Profile =
      Profile(
          uid = "ClaudiaID",
          name = "Claudia",
          username = "claudia",
          groupIds = emptyList(),
          friendUids = listOf("OwnerID"))

  /** Helper: sets up repositories, creates a group, and composes EditGroupScreen as Owner. */
  @OptIn(ExperimentalCoroutinesApi::class)
  private fun setContentAsOwner() {
    runTest {
      profileRepository = ProfileLocalRepository()
      groupsRepository = GroupsLocalRepository()
      notificationsRepository = NotificationsLocalRepository()

      // Add profiles
      profileRepository.addProfile(ownerProfile)
      profileRepository.addProfile(gersProfile)
      profileRepository.addProfile(claireProfile)
      profileRepository.addProfile(alessandroProfile)
      profileRepository.addProfile(claudiaProfile)
      advanceUntilIdle()

      // Create initial group in local repository
      val initialGroup =
          Group(
              gid = testGroupId,
              creatorId = ownerProfile.uid,
              name = "Study Group",
              description = "Initial description",
              memberIds = listOf(ownerProfile.uid, gersProfile.uid, claireProfile.uid),
              adminIds = listOf(ownerProfile.uid))

      groupsRepository.addGroup(initialGroup)
      advanceUntilIdle()

      currentUserId = ownerProfile.uid

      mockitoUtils = MockitoUtils()
      mockitoUtils.chooseCurrentUser(currentUserId)

      editGroupViewModel =
          EditGroupViewModel(
              groupsRepository = groupsRepository,
              profileRepository = profileRepository,
              notificationsRepository = notificationsRepository,
              authProvider = { mockitoUtils.mockAuth })

      composeTestRule.setContent {
        EditGroupScreen(
            groupId = testGroupId,
            editGroupViewModel = editGroupViewModel,
        )
      }
    }
  }

  /*----------------------------------------Tests--------------------------------------------*/

  /** Verifies that all non-owner member items are displayed (owner is hidden from the list). */
  @Test
  fun showsAllMemberItemsForOwner() {
    setContentAsOwner()

    // Owner is not shown in members list
    composeTestRule
        .onNodeWithTag(EditGroupScreenTestTags.getTestTagForMemberItem("owner"))
        .assertDoesNotExist()

    // Gers and Claire are displayed
    composeTestRule
        .onNodeWithTag(EditGroupScreenTestTags.getTestTagForMemberItem("gers"))
        .assertExists()
    composeTestRule
        .onNodeWithTag(EditGroupScreenTestTags.getTestTagForMemberItem("claire"))
        .assertExists()
  }

  /** Verifies that searching for a friend filters the AVAILABLE friends list accordingly. */
  @Test
  fun friendSearchFiltersAvailableFriendsList() {
    setContentAsOwner()

    // Alessandro and Claudia available friends
    composeTestRule
        .onNodeWithTag(EditGroupScreenTestTags.getTestTagForAvailableFriendCheckbox("alessandro"))
        .assertExists()
    composeTestRule
        .onNodeWithTag(EditGroupScreenTestTags.getTestTagForAvailableFriendCheckbox("claudia"))
        .assertExists()

    // Search for "ale"
    composeTestRule
        .onNodeWithTag(EditGroupScreenTestTags.SEARCH_FRIENDS_BAR)
        .assertExists()
        .performTextInput("ale")

    // Only Alessandro should be visible now
    composeTestRule
        .onNodeWithTag(EditGroupScreenTestTags.getTestTagForAvailableFriendCheckbox("alessandro"))
        .assertExists()
    composeTestRule
        .onNodeWithTag(EditGroupScreenTestTags.getTestTagForAvailableFriendCheckbox("claudia"))
        .assertDoesNotExist()
  }

  /** Verifies that all main UI components are displayed for the owner. */
  @Test
  fun allComponentsDisplayedForOwner() {
    setContentAsOwner()

    // Group name field
    composeTestRule
        .onNodeWithTag(EditGroupScreenTestTags.GROUP_NAME_FIELD)
        .assertExists()
        .assertIsDisplayed()

    // Group description field
    composeTestRule
        .onNodeWithTag(EditGroupScreenTestTags.GROUP_DESCRIPTION_FIELD)
        .assertExists()
        .assertIsDisplayed()

    // Search bar for available friends
    composeTestRule
        .onNodeWithTag(EditGroupScreenTestTags.SEARCH_FRIENDS_BAR)
        .assertExists()
        .assertIsDisplayed()

    // At least one member item (gers)
    composeTestRule
        .onNodeWithTag(EditGroupScreenTestTags.getTestTagForMemberItem("gers"))
        .assertExists()
        .assertIsDisplayed()

    // At least one available friend checkbox (alessandro)
    composeTestRule
        .onNodeWithTag(EditGroupScreenTestTags.getTestTagForAvailableFriendCheckbox("alessandro"))
        .assertExists()
        .assertIsDisplayed()

    // At least one participant profile picture (gers)
    composeTestRule
        .onNodeWithTag(EditGroupScreenTestTags.getTestTagForSelectedFriendProfilePicture("gers"))
        .assertExists()
        .assertIsDisplayed()

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(EditGroupScreenTestTags.LAZY_COLUMN_GROUP)
        .performScrollToNode(hasTestTag(EditGroupScreenTestTags.BUTTON_SAVE_GROUP))

    composeTestRule
        .onNodeWithTag(EditGroupScreenTestTags.BUTTON_SAVE_GROUP)
        .assertExists()
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(EditGroupScreenTestTags.LAZY_COLUMN_GROUP)
        .performScrollToNode(hasTestTag(EditGroupScreenTestTags.BUTTON_DELETE_GROUP))

    composeTestRule
        .onNodeWithTag(EditGroupScreenTestTags.BUTTON_DELETE_GROUP)
        .assertExists()
        .assertIsDisplayed()
  }
}
