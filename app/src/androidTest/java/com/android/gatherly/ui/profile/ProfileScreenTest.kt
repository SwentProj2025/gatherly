package com.android.gatherly.ui.profile

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.android.gatherly.model.group.Group
import com.android.gatherly.model.group.GroupsLocalRepository
import com.android.gatherly.model.group.GroupsRepository
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.utils.MockitoUtils
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ProfileScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private val profile =
      Profile(
          name = "Default User",
          username = "defaultusername",
          school = "University",
          schoolYear = "Year",
          friendUids = emptyList(),
          groupIds = listOf("g1", "g2"))

  private val group1 =
      Group(
          gid = "g1",
          creatorId = "u1",
          name = "Group One",
          memberIds = listOf("a", "b", "c"),
          adminIds = listOf())
  private val group2 =
      Group(
          gid = "g2",
          creatorId = "u1",
          name = "Group Two",
          memberIds = listOf("a"),
          adminIds = listOf())

  private lateinit var profileRepository: ProfileRepository
  private lateinit var groupsRepository: GroupsRepository
  private lateinit var profileViewModel: ProfileViewModel

  private lateinit var mockitoUtils: MockitoUtils

  private fun setContent(isAnon: Boolean = false) {
    profileRepository = ProfileLocalRepository()
    groupsRepository = GroupsLocalRepository()
    fill_repository()

    // Mock Firebase Auth
    mockitoUtils = MockitoUtils()
    mockitoUtils.chooseCurrentUser("", isAnon)

    profileViewModel =
        ProfileViewModel(
            profileRepository = profileRepository,
            groupsRepository = groupsRepository,
            authProvider = { mockitoUtils.mockAuth })
    composeTestRule.setContent { ProfileScreen(profileViewModel = profileViewModel) }
  }

  fun fill_repository() = runTest {
    groupsRepository.addGroup(group1)
    groupsRepository.addGroup(group2)
    profileRepository.addProfile(profile)
  }

  @Test
  fun profilePicture_IsDisplayed() {
    setContent()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.PROFILE_PICTURE).assertExists()
  }

  @Test
  fun nameAndUsername_AreDisplayedCorrectly() {
    setContent()
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.PROFILE_NAME)
        .assertExists()
        .assertTextContains("Default User")
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.PROFILE_USERNAME)
        .assertExists()
        .assertTextContains("@defaultusername")
  }

  @Test
  fun schoolInfo_IsDisplayedCorrectly() {
    setContent()
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.PROFILE_SCHOOL)
        .assertExists()
        .assertTextEquals("University - Year")
  }

  @Test
  fun friendsAndFocusPoints_AreDisplayedCorrectly() {
    setContent()
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.PROFILE_FRIENDS_COUNT)
        .assertExists()
        .assertTextEquals("0")
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.PROFILE_FOCUS_POINTS_COUNT)
        .assertExists()
        .assertTextEquals("0")
  }

  @Test
  fun sectionsTitles_AreDisplayedCorrectly() {
    setContent()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.PROFILE_FOCUS_SESSIONS).assertExists()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.PROFILE_GROUPS).assertExists()
  }

  @Test
  fun groupsOverview_Displayed_WhenUserHasGroups() = runTest {
    setContent()

    composeTestRule.onNodeWithTag(ProfileScreenTestTags.GROUPS_OVERVIEW_CONTAINER).assertExists()

    // Group 1
    composeTestRule
        .onNodeWithTag("${ProfileScreenTestTags.GROUP_ROW_NAME}_0")
        .assertTextContains("Group One")
    composeTestRule
        .onNodeWithTag("${ProfileScreenTestTags.GROUP_ROW_MEMBER_COUNT}_0")
        .assertTextContains("3 members")

    // Group 2
    composeTestRule
        .onNodeWithTag("${ProfileScreenTestTags.GROUP_ROW_NAME}_1")
        .assertTextContains("Group Two")
    composeTestRule
        .onNodeWithTag("${ProfileScreenTestTags.GROUP_ROW_MEMBER_COUNT}_1")
        .assertTextContains("1 member")
  }

  /** Check that the anonymous user sees the "upgrade with google" button */
  @Test
  fun anonUserSeesGoogleButton() {
    setContent(true)
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.GOOGLE_BUTTON).assertIsDisplayed()
  }
}
