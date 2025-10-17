package com.android.gatherly.ui.profile

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.utils.FirestoreGatherlyTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ProfileScreenTest : FirestoreGatherlyTest() {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  override fun setUp() {

    super.setUp()
  }

  private fun setContent() {
    val profile =
        Profile(
            name = "Default User",
            username = "defaultusername",
            school = "University",
            schoolYear = "Year",
            friendUids = emptyList())
    val profileRepository = ProfileLocalRepository()
    runBlocking { profileRepository.addProfile(profile) }
    val profileViewModel = ProfileViewModel(profileRepository)
    composeTestRule.setContent { ProfileScreen(profileViewModel = profileViewModel) }
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
}
