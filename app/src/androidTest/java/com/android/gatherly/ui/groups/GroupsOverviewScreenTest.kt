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
class GroupsOverviewScreenTest {
  @get:Rule val composeTestRule = createComposeRule()
  private lateinit var groupsRepository: GroupsRepository
  private lateinit var profileRepository: ProfileRepository
  private lateinit var groupsOverviewViewModel: GroupsOverviewViewModel

  val TEST_USER_ID = "testUser"
  val OTHER_USER_ID = "otherUser"
  val FRIEND_USER_ID = "friendUser"

  val userGroup1 =
      Group(
          gid = "group1",
          creatorId = TEST_USER_ID,
          name = "Study Group",
          description = "CS students study group",
          memberIds = listOf(TEST_USER_ID, FRIEND_USER_ID),
          adminIds = listOf(TEST_USER_ID))

  val userGroup2 =
      Group(
          gid = "group2",
          creatorId = OTHER_USER_ID,
          name = "Running Club",
          description = "Weekly running meetups",
          memberIds = listOf(TEST_USER_ID, OTHER_USER_ID, FRIEND_USER_ID),
          adminIds = listOf(OTHER_USER_ID, TEST_USER_ID))

  val testProfilePic = "testProfilePic"
  val otherProfilePic = "otherProfilePic"
  val friendProfilePic = "friendProfilePic"

  val testUser = Profile(uid = TEST_USER_ID, profilePicture = testProfilePic)

  val otherUser = Profile(uid = OTHER_USER_ID, profilePicture = otherProfilePic)

  val friendUser = Profile(uid = FRIEND_USER_ID, profilePicture = friendProfilePic)

  @Before
  fun setUp() {
    groupsRepository = GroupsLocalRepository()
    profileRepository = ProfileLocalRepository()
  }

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

  @Test
  fun checkNoGroupsDisplaysMessage() {
    setContentNoGroups()

    composeTestRule.onNodeWithTag(GroupsOverviewScreenTestTags.CREATE_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(GroupsOverviewScreenTestTags.NO_GROUPS).assertIsDisplayed()
  }
}
