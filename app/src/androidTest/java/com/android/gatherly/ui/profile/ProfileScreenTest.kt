package com.android.gatherly.ui.profile

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class ProfileScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private val profile =
      Profile(
          name = "Default User",
          username = "defaultusername",
          school = "University",
          schoolYear = "Year",
          friendUids = emptyList())

  private lateinit var profileRepository: ProfileRepository
  private lateinit var profileViewModel: ProfileViewModel

  private lateinit var mockAuth: FirebaseAuth
  private lateinit var mockUser: FirebaseUser

  private fun setContent() {
    profileRepository = ProfileLocalRepository()
    fill_repository()

    // Mock Firebase Auth
    mockAuth = mock(FirebaseAuth::class.java)
    mockUser = mock(FirebaseUser::class.java)
    `when`(mockAuth.currentUser).thenReturn(mockUser)
    `when`(mockUser.uid).thenReturn("")
    `when`(mockUser.isAnonymous).thenReturn(false)

    profileViewModel = ProfileViewModel(repository = profileRepository, authProvider = { mockAuth })
    composeTestRule.setContent { ProfileScreen(profileViewModel = profileViewModel) }
  }

  fun fill_repository() = runTest { profileRepository.addProfile(profile) }

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
