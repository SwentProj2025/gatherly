package com.android.gatherly.ui.authentication

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.gatherly.ui.navigation.NavigationActions
import com.android.gatherly.ui.settings.SettingsScreenTestTags
import com.android.gatherly.utils.FirestoreGatherlyTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** UI tests for the [InitProfileScreen]. */
@RunWith(AndroidJUnit4::class)
class InitProfileScreenTest : FirestoreGatherlyTest() {

  @get:Rule val composeRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions

  @Before
  override fun setUp() {
    super.setUp()
    // Pass a no-op NavigationActions, navigation won’t be tested here.
    composeRule.setContent { InitProfileScreen() }
  }

  /** Ensures all UI components are visible when screen loads. */
  @Test
  fun onboardingScreen_componentsAreDisplayed() {
    composeRule
        .onNodeWithTag(SettingsScreenTestTags.PROFILE_PICTURE)
        .assertExists()
        .assertIsDisplayed()
    composeRule.onNodeWithTag("onboarding_username_field").assertExists().assertIsDisplayed()
    composeRule.onNodeWithTag("onboarding_name_field").assertExists().assertIsDisplayed()
    composeRule.onNodeWithTag("onboarding_school_field").assertExists().assertIsDisplayed()
    composeRule.onNodeWithTag("onboarding_school_year_field").assertExists().assertIsDisplayed()
    composeRule.onNodeWithTag("onboarding_save_button").assertExists().assertIsDisplayed()
  }

  /** Verifies that username is mandatory and shows an error when cleared. */
  @Test
  fun onboardingScreen_showsError_whenUsernameEmpty() {
    val usernameField = composeRule.onNodeWithTag("onboarding_username_field")

    // Trigger recomposition by typing then clearing
    usernameField.performTextInput("tempuser")
    usernameField.performTextClearance()
    composeRule.waitForIdle()

    composeRule.onNodeWithTag("onboarding_username_field_error").assertIsDisplayed()
  }

  /** Verifies that name is mandatory and shows an error when cleared. */
  @Test
  fun onboardingScreen_showsError_whenNameEmpty() {
    val nameField = composeRule.onNodeWithTag("onboarding_name_field")

    // Trigger recomposition
    nameField.performTextInput("A")
    nameField.performTextClearance()
    composeRule.waitForIdle()

    composeRule.onNodeWithTag("onboarding_name_field_error").assertIsDisplayed()
  }

  /** Ensures that both username and name must be valid for the save button to be enabled. */
  @Test
  fun onboardingScreen_saveButton_enabledOnlyWhenValid() {
    // Invalid (both empty)
    composeRule.onNodeWithTag("onboarding_save_button").assertIsNotEnabled()

    // Type only username → still invalid (name missing)
    composeRule.onNodeWithTag("onboarding_username_field").performTextInput("alice_ok")
    composeRule.waitForIdle()
    composeRule.onNodeWithTag("onboarding_save_button").assertIsNotEnabled()

    // Add name → becomes valid
    composeRule.onNodeWithTag("onboarding_name_field").performTextInput("Alice")
    composeRule.waitForIdle()
    composeRule.onNodeWithTag("onboarding_save_button").assertIsEnabled()
  }

  /** Ensures that an invalid username format triggers the appropriate error message. */
  @Test
  fun onboardingScreen_showsError_whenUsernameInvalid() {
    composeRule.onNodeWithTag("onboarding_username_field").performTextInput("!!bad!!")
    composeRule.waitForIdle()

    composeRule.onNodeWithTag("onboarding_username_field_error").assertIsDisplayed()
  }

  /** Ensures that valid inputs hide all error messages. */
  @Test
  fun onboardingScreen_noError_whenFieldsValid() {
    composeRule.onNodeWithTag("onboarding_username_field").performTextInput("validuser")
    composeRule.onNodeWithTag("onboarding_name_field").performTextInput("Alice")
    composeRule.waitForIdle()

    composeRule.onNodeWithTag("onboarding_username_field_error").assertDoesNotExist()
    composeRule.onNodeWithTag("onboarding_name_field_error").assertDoesNotExist()
  }

  /** Verifies that the username field retains its entered text after input. */
  @Test
  fun onboardingScreen_usernameField_retainsValue() {
    val username = "persistentuser"
    composeRule.onNodeWithTag("onboarding_username_field").performTextInput(username)
    composeRule.waitForIdle()
    composeRule.onNodeWithTag("onboarding_username_field").assertTextEquals(username)
  }

  /** Verifies that the name field retains its entered text after input. */
  @Test
  fun onboardingScreen_nameField_retainsValue() {
    val name = "TestUser"
    composeRule.onNodeWithTag("onboarding_name_field").performTextInput(name)
    composeRule.waitForIdle()
    composeRule.onNodeWithTag("onboarding_name_field").assertTextEquals(name)
  }

  /** Simulates user filling valid inputs and clicking the save button. */
  @Test
  fun onboardingScreen_clickSaveButton_isEnabledAndClickable() {
    composeRule.onNodeWithTag("onboarding_username_field").performTextInput("user123")
    composeRule.onNodeWithTag("onboarding_name_field").performTextInput("User 123")
    composeRule.waitForIdle()

    val saveButton = composeRule.onNodeWithTag("onboarding_save_button")
    saveButton.assertIsEnabled()
    saveButton.performClick()
    saveButton.assertExists()
  }

  /** Ensures school and school year inputs don’t affect save button availability. */
  @Test
  fun onboardingScreen_optionalFields_doNotBlockSave() {
    composeRule.onNodeWithTag("onboarding_username_field").performTextInput("alice_ok")
    composeRule.onNodeWithTag("onboarding_name_field").performTextInput("Alice")

    composeRule.onNodeWithTag("onboarding_school_field").performTextInput("EPFL")
    composeRule.onNodeWithTag("onboarding_school_year_field").performTextInput("BA5")

    composeRule.waitForIdle()
    composeRule.onNodeWithTag("onboarding_save_button").assertIsEnabled()
  }

  /** Ensures valid username displays success message text. */
  @Test
  fun onboardingScreen_showsValidUsernameConfirmationText() {
    composeRule.onNodeWithTag("onboarding_username_field").performTextInput("uniqueuser")
    composeRule.waitForIdle()

    composeRule.onNodeWithText("This username is available!").assertExists()
  }
}
