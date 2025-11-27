package com.android.gatherly.ui.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** UI test for UserProfileScreen. */
class UserProfileScreenTest {

  @get:Rule val composeRule = createComposeRule()

  private lateinit var repo: ProfileLocalRepository
  private lateinit var viewModel: UserProfileViewModel

  private val testProfile =
      Profile(
          uid = "userProfile_testUid",
          name = "Alice",
          username = "userProfile_alice",
          school = "EPFL",
          schoolYear = "2025",
          friendUids = emptyList())

  @Before
  fun setUp() {
    repo = ProfileLocalRepository()
    runBlocking { repo.addProfile(testProfile) }
    viewModel = UserProfileViewModel(repository = repo)
  }

  private fun setContent() {
    composeRule.setContent { UserProfileScreen(uid = testProfile.uid, viewModel = viewModel) }
  }

  /** Verifies all the components of the screen are displayed* */
  @Test
  fun profileComponents_areDisplayed() {
    setContent()

    composeRule.onNodeWithTag(UserProfileScreenTestTags.PROFILE_PICTURE).assertIsDisplayed()
    composeRule.onNodeWithTag(UserProfileScreenTestTags.NAME).assertIsDisplayed()
    composeRule.onNodeWithTag(UserProfileScreenTestTags.USERNAME).assertIsDisplayed()
    composeRule.onNodeWithTag(UserProfileScreenTestTags.SCHOOL_INFO).assertIsDisplayed()
    composeRule.onNodeWithTag(UserProfileScreenTestTags.USER_STATUS).assertIsDisplayed()
  }

  /** Verifies the snackBar is correctly displayed when an error occurs* */
  @Test
  fun failedProfileLoadingDisplaysSnackBar() {
    composeRule.setContent { UserProfileScreen(uid = "missing_user", viewModel = viewModel) }

    composeRule.onNodeWithTag(UserProfileScreenTestTags.ERROR_SNACKBAR).assertIsDisplayed()
    composeRule.onNodeWithTag(UserProfileScreenTestTags.EMPTY_STATE).assertIsDisplayed()
  }
}
