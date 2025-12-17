package com.android.gatherly.ui.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.ui.profile.ProfileScreenTestData.profile2
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * UI test for UserProfileScreen.
 *
 * Verifies that all components are displayed correctly, and that error handling works as expected.
 */
class UserProfileScreenTest {

  @get:Rule val composeRule = createComposeRule()
  private lateinit var repo: ProfileLocalRepository
  private lateinit var viewModel: UserProfileViewModel

  @Before
  fun setUp() {
    repo = ProfileLocalRepository()
    runBlocking { repo.addProfile(profile2) }
    viewModel = UserProfileViewModel(repository = repo)
  }

  /** Sets the content of the compose rule to the UserProfileScreen with the test profile's UID. */
  private fun setContent() {
    composeRule.setContent { UserProfileScreen(uid = profile2.uid, viewModel = viewModel) }
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
    composeRule.onNodeWithTag(UserProfileScreenTestTags.USER_BIO).assertIsDisplayed()
  }

  /** Verifies the snackBar is correctly displayed when an error occurs* */
  @Test
  fun failedProfileLoadingDisplaysSnackBar() {
    composeRule.setContent { UserProfileScreen(uid = "missing_user", viewModel = viewModel) }

    composeRule.onNodeWithTag(UserProfileScreenTestTags.ERROR_SNACKBAR).assertIsDisplayed()
    composeRule.onNodeWithTag(UserProfileScreenTestTags.EMPTY_STATE).assertIsDisplayed()
  }
}
