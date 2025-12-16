package com.android.gatherly.ui.groups

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.gatherly.model.group.Group
import com.android.gatherly.model.group.GroupsLocalRepository
import com.android.gatherly.model.group.GroupsRepository
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
/** Tests the Group Overview display */
class GroupsOverviewScreenTest {
  @get:Rule val composeTestRule = createComposeRule()
  private lateinit var groupsRepository: GroupsRepository
  private lateinit var profileRepository: ProfileRepository
  private lateinit var groupsOverviewViewModel: GroupsOverviewViewModel

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

  val userGroup2 =
      Group(
          gid = "group2",
          creatorId = otherUserId,
          name = "Running Club",
          description = "Weekly running meetups",
          memberIds = listOf(testUserId, otherUserId, friendUserId),
          adminIds = listOf(otherUserId, testUserId))

  val testProfilePic = "testProfilePic"
  val otherProfilePic = "otherProfilePic"
  val friendProfilePic = "friendProfilePic"

  val testUser = Profile(uid = testUserId, profilePicture = testProfilePic)

  val otherUser = Profile(uid = otherUserId, profilePicture = otherProfilePic)

  val friendUser = Profile(uid = friendUserId, profilePicture = friendProfilePic)

  /** Sets repositories to local implementations */
  @Before
  fun setUp() {
    groupsRepository = GroupsLocalRepository()
    profileRepository = ProfileLocalRepository()
  }

  /** Sets the content of the repositories by adding groups */
  fun setContentGroups() = runBlocking {
    profileRepository.addProfile(testUser)
    profileRepository.addProfile(friendUser)
    profileRepository.addProfile(otherUser)
    groupsRepository.addGroup(userGroup1)
    groupsRepository.addGroup(userGroup2)

    groupsOverviewViewModel =
        GroupsOverviewViewModel(
            groupsRepository = groupsRepository, profileRepository = profileRepository)

    composeTestRule.setContent {
      GroupsOverviewScreen(groupsOverviewViewModel = groupsOverviewViewModel)
    }
  }

  /** Sets the content of the repositories without adding any groups */
  fun setContentNoGroups() = runBlocking {
    profileRepository.addProfile(testUser)
    profileRepository.addProfile(friendUser)
    profileRepository.addProfile(otherUser)

    groupsOverviewViewModel =
        GroupsOverviewViewModel(
            groupsRepository = groupsRepository, profileRepository = profileRepository)

    composeTestRule.setContent {
      GroupsOverviewScreen(groupsOverviewViewModel = groupsOverviewViewModel)
    }
  }

  /** Checks that multiple groups are correctly displayed when they exist */
  @Test
  fun checkGroupsAreDisplayed() {
    setContentGroups()

    composeTestRule.onNodeWithTag(GroupsOverviewScreenTestTags.LIST).assertIsDisplayed()
    composeTestRule.onNodeWithTag(GroupsOverviewScreenTestTags.CREATE_BUTTON).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(GroupsOverviewScreenTestTags.getTestTagForGroupItem(userGroup1.gid))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(GroupsOverviewScreenTestTags.getTestTagForGroupItem(userGroup2.gid))
        .assertIsDisplayed()
  }

  /** Checks that when the user isn't part of any groups, an empty message is displayed */
  @Test
  fun checkNoGroupsDisplaysMessage() {
    setContentNoGroups()

    composeTestRule.onNodeWithTag(GroupsOverviewScreenTestTags.CREATE_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(GroupsOverviewScreenTestTags.NO_GROUPS).assertIsDisplayed()
  }
}
