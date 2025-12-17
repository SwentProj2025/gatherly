package com.android.gatherly.ui.profile

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.android.gatherly.model.group.GroupsLocalRepository
import com.android.gatherly.model.group.GroupsRepository
import com.android.gatherly.model.notification.NotificationsLocalRepository
import com.android.gatherly.model.notification.NotificationsRepository
import com.android.gatherly.model.points.PointsLocalRepository
import com.android.gatherly.model.points.PointsRepository
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.ui.badge.BadgeScreenTestTags
import com.android.gatherly.ui.profile.ProfileScreenTestData.group1
import com.android.gatherly.ui.profile.ProfileScreenTestData.group2
import com.android.gatherly.ui.profile.ProfileScreenTestData.profile1
import com.android.gatherly.utils.MockitoUtils
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for the [ProfileScreen].
 *
 * Tests include verifying that profile information is displayed correctly, groups overview is shown
 * based on user's group membership, badges are displayed, and anonymous users see the appropriate
 * upgrade prompt.
 */
class ProfileScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()
  private lateinit var profileRepository: ProfileRepository
  private lateinit var groupsRepository: GroupsRepository
  private lateinit var notificationsRepository: NotificationsRepository
  private lateinit var pointsRepository: PointsRepository
  private lateinit var profileViewModel: ProfileViewModel
  private lateinit var mockitoUtils: MockitoUtils

  /**
   * Sets up the content for testing the ProfileScreen.
   *
   * @param isAnon Boolean indicating if the user is anonymous.
   * @param hasGroups Boolean indicating if the user has groups.
   */
  private fun setContent(isAnon: Boolean = false, hasGroups: Boolean = true) {
    profileRepository = ProfileLocalRepository()
    groupsRepository = GroupsLocalRepository()
    fill_profile_repository()
    if (hasGroups) {
      fill_groups_repository()
    }
    notificationsRepository = NotificationsLocalRepository()
    pointsRepository = PointsLocalRepository()

    // Mock Firebase Auth
    mockitoUtils = MockitoUtils()
    mockitoUtils.chooseCurrentUser("", isAnon)

    profileViewModel =
        ProfileViewModel(
            profileRepository = profileRepository,
            groupsRepository = groupsRepository,
            notificationsRepository = notificationsRepository,
            pointsRepository = pointsRepository,
            authProvider = { mockitoUtils.mockAuth })
    composeTestRule.setContent { ProfileScreen(profileViewModel = profileViewModel) }
  }

  /** Fills the groups repository with test groups. */
  fun fill_groups_repository() = runTest {
    groupsRepository.addGroup(group1)
    groupsRepository.addGroup(group2)
  }

  /** Fills the profile repository with the test profile. */
  fun fill_profile_repository() = runTest { profileRepository.addProfile(profile1) }

  /** Test to verify that the profile picture is displayed on the ProfileScreen. */
  @Test
  fun profilePicture_IsDisplayed() {
    setContent()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.PROFILE_PICTURE).assertExists()
  }

  /**
   * Test to verify that the user's name and username are displayed correctly on the ProfileScreen.
   */
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

  /** Test to verify that the user's bio is displayed on the ProfileScreen. */
  @Test
  fun userBio_IsDisplayed() {
    setContent()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.USER_BIO).assertIsDisplayed()
  }

  /**
   * Test to verify that the user's school information is displayed correctly on the ProfileScreen.
   */
  @Test
  fun schoolInfo_IsDisplayedCorrectly() {
    setContent()
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.PROFILE_SCHOOL)
        .assertExists()
        .assertTextEquals("University - Year")
  }

  /**
   * Test to verify that the friends count and focus points are displayed correctly on the
   * ProfileScreen.
   */
  @Test
  fun friendsAndFocusPoints_AreDisplayedCorrectly() {
    setContent()
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.PROFILE_FRIENDS_COUNT)
        .assertExists()
        .assertTextEquals("0")
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.PROFILE_FOCUS_POINTS_COUNT, useUnmergedTree = true)
        .assertExists()
        .assertTextEquals("0.0")
  }

  /** Test to verify that the section titles are displayed correctly on the ProfileScreen. */
  @Test
  fun sectionsTitles_AreDisplayedCorrectly() {
    setContent()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.PROFILE_FOCUS_SESSIONS).assertExists()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.PROFILE_GROUPS).assertExists()
  }

  /** Test to verify that the groups overview is displayed correctly when the user has groups. */
  @Test
  fun groupsOverview_Displayed_WhenUserHasGroups() = runTest {
    setContent()

    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.GROUPS_OVERVIEW_CONTAINER, useUnmergedTree = true)
        .assertExists()

    // Group 1
    composeTestRule
        .onNodeWithTag("${ProfileScreenTestTags.GROUP_ROW_NAME}_0", useUnmergedTree = true)
        .assertTextContains("Group One")
    composeTestRule
        .onNodeWithTag("${ProfileScreenTestTags.GROUP_ROW_MEMBER_COUNT}_0", useUnmergedTree = true)
        .assertTextContains("3 members")

    // Group 2
    composeTestRule
        .onNodeWithTag("${ProfileScreenTestTags.GROUP_ROW_NAME}_1", useUnmergedTree = true)
        .assertTextContains("Group Two")
    composeTestRule
        .onNodeWithTag("${ProfileScreenTestTags.GROUP_ROW_MEMBER_COUNT}_1", useUnmergedTree = true)
        .assertTextContains("1 member")
  }

  /** Test to verify that the appropriate message is displayed when the user has no groups. */
  @Test
  fun groupsOverview_Displayed_WhenUserHasNoGroups() = runTest {
    setContent(isAnon = false, hasGroups = false)

    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.GROUPS_OVERVIEW_CONTAINER)
        .assertDoesNotExist()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.NO_GROUPS_TEXT).assertExists()
  }

  /** Test to verify that badge information is displayed correctly on the ProfileScreen. */
  @Test
  fun badgeInfo_AreDisplayedCorrectly() {
    setContent()

    val expectedTitles =
        listOf(
            "Blank Todo Created Badge",
            "Blank Todo Completed Badge",
            "Blank Event Created Badge",
            "Blank Event Participated Badge",
            "Blank Friend Badge",
            "Blank Focus Session Badge")

    expectedTitles.forEach { title ->
      composeTestRule
          .onNodeWithTag(BadgeScreenTestTags.badgeTest(title), useUnmergedTree = true)
          .assertExists()
    }

    composeTestRule.onNodeWithTag(ProfileScreenTestTags.PROFILE_BADGES).assertExists()
  }

  /** Check that the anonymous user sees the "upgrade with google" button */
  @Test
  fun anonUserSeesGoogleButton() {
    setContent(true)
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.GOOGLE_BUTTON).assertIsDisplayed()
  }
}
