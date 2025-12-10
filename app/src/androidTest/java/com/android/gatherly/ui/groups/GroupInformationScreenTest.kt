package com.android.gatherly.ui.groups

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.gatherly.model.group.Group
import com.android.gatherly.model.group.GroupsLocalRepository
import com.android.gatherly.model.group.GroupsRepository
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.utils.MockitoUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GroupInformationScreenTest {
  @get:Rule val composeTestRule = createComposeRule()
  private lateinit var groupsRepository: GroupsRepository
  private lateinit var profileRepository: ProfileRepository
  private lateinit var mockitoUtils: MockitoUtils
  private lateinit var groupInformationViewModel: GroupInformationViewModel

  val testUserId = "testUser"
  val otherUserId = "otherUser"
  val friendUserId = "friendUser"

  val userGroup1 =
      Group(
          gid = "group1",
          creatorId = testUserId,
          name = "Study Group",
          description = "CS students study group",
          memberIds = listOf(testUserId, friendUserId),
          adminIds = listOf(testUserId))

  val testProfilePic = "testProfilePic"
  val friendProfilePic = "friendProfilePic"

  val testUser = Profile(uid = testUserId, profilePicture = testProfilePic)

  val friendUser = Profile(uid = friendUserId, profilePicture = friendProfilePic)

  @Before
  fun setUp() {
    groupsRepository = GroupsLocalRepository()
    profileRepository = ProfileLocalRepository()
    mockitoUtils = MockitoUtils()

    runBlocking {
      profileRepository.addProfile(testUser)
      profileRepository.addProfile(friendUser)
      groupsRepository.addGroup(userGroup1)

      groupInformationViewModel =
          GroupInformationViewModel(
              groupsRepository = groupsRepository,
              profileRepository = profileRepository,
              authProvider = { mockitoUtils.mockAuth })
    }
  }

  /** Chooses the current user to be an admin of the group */
  fun setAdminUser() {
    mockitoUtils.chooseCurrentUser(testUserId)

    composeTestRule.setContent {
      GroupInformationScreen(
          groupInformationViewModel = groupInformationViewModel, groupId = userGroup1.gid)
    }
  }

  /** Chooses the current user to not be an admin of the group */
  fun setNonAdminUser() {
    mockitoUtils.chooseCurrentUser(friendUserId)

    composeTestRule.setContent {
      GroupInformationScreen(
          groupInformationViewModel = groupInformationViewModel, groupId = userGroup1.gid)
    }
  }

  /** Checks that everything is displayed correctly for an admin user */
  @Test
  fun adminUserDisplaysButton() {
    setAdminUser()

    composeTestRule
        .onNodeWithTag(GroupInformationScreenTestTags.GROUP_NAME)
        .assertIsDisplayed()
        .assertTextContains(userGroup1.name)
    composeTestRule
        .onNodeWithTag(GroupInformationScreenTestTags.GROUP_DESCRIPTION)
        .assertIsDisplayed()
        .assertTextContains(userGroup1.description!!)
    composeTestRule.onNodeWithTag(GroupInformationScreenTestTags.MEMBERS_LIST).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(GroupInformationScreenTestTags.getTestTagForAdminItem(testUserId))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(GroupInformationScreenTestTags.getTestTagForAdminItem(friendUserId))
        .assertDoesNotExist()
    composeTestRule.onNodeWithTag(GroupInformationScreenTestTags.EDIT_BUTTON).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(GroupInformationScreenTestTags.getTestTagForMemberItem(testUserId))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(GroupInformationScreenTestTags.getTestTagForMemberItem(friendUserId))
        .assertIsDisplayed()
  }

  /** Checks that everything is displayed correctly for a member, non-admin user */
  @Test
  fun memberUserDoesNotDisplayButton() {
    setNonAdminUser()

    composeTestRule
        .onNodeWithTag(GroupInformationScreenTestTags.GROUP_NAME)
        .assertIsDisplayed()
        .assertTextContains(userGroup1.name)
    composeTestRule
        .onNodeWithTag(GroupInformationScreenTestTags.GROUP_DESCRIPTION)
        .assertIsDisplayed()
        .assertTextContains(userGroup1.description!!)
    composeTestRule.onNodeWithTag(GroupInformationScreenTestTags.MEMBERS_LIST).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(GroupInformationScreenTestTags.getTestTagForAdminItem(testUserId))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(GroupInformationScreenTestTags.getTestTagForAdminItem(friendUserId))
        .assertDoesNotExist()
    composeTestRule.onNodeWithTag(GroupInformationScreenTestTags.EDIT_BUTTON).assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag(GroupInformationScreenTestTags.getTestTagForMemberItem(testUserId))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(GroupInformationScreenTestTags.getTestTagForMemberItem(friendUserId))
        .assertIsDisplayed()
  }
}
