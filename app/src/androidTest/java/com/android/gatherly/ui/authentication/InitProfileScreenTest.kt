package com.android.gatherly.ui.authentication

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.gatherly.utils.FirestoreGatherlyTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** UI tests for the [InitProfileScreen]. */
@RunWith(AndroidJUnit4::class)
class InitProfileScreenTest : FirestoreGatherlyTest() {

  @get:Rule val composeRule = createComposeRule()

  @Before
  override fun setUp() {
    super.setUp()
    composeRule.setContent { InitProfileScreen() }
  }

  /** Ensures all UI components are visible when screen loads. */
  @Test
  fun initProfileScreen_componentsAreDisplayed() {
    composeRule
        .onNodeWithTag(InitProfileScreenTestTags.PROFILE_PICTURE)
        .assertExists()
        .assertIsDisplayed()
    composeRule.onNodeWithTag(InitProfileScreenTestTags.USERNAME).assertExists().assertIsDisplayed()
    composeRule
        .onNodeWithTag(InitProfileScreenTestTags.NAME_FIELD)
        .assertExists()
        .assertIsDisplayed()
    composeRule
        .onNodeWithTag(InitProfileScreenTestTags.BIRTHDAY_FIELD)
        .assertExists()
        .assertIsDisplayed()
    composeRule
        .onNodeWithTag(InitProfileScreenTestTags.SCHOOL_FIELD)
        .assertExists()
        .assertIsDisplayed()
    composeRule
        .onNodeWithTag(InitProfileScreenTestTags.SCHOOL_YEAR_FIELD)
        .assertExists()
        .assertIsDisplayed()
    composeRule
        .onNodeWithTag(InitProfileScreenTestTags.SAVE_BUTTON)
        .assertExists()
        .assertIsDisplayed()
  }

  /** Verifies that username is mandatory and shows an error when cleared. */
  @Test
  fun initProfileScreen_showsError_whenUsernameEmpty() {
    val usernameField = composeRule.onNodeWithTag(InitProfileScreenTestTags.USERNAME)

    // Trigger recomposition by typing then clearing
    usernameField.performTextInput("tempuser")
    usernameField.performTextClearance()
    composeRule.waitForIdle()

    composeRule.onNodeWithTag(InitProfileScreenTestTags.USERNAME_ERROR).assertIsDisplayed()
  }

  /** Verifies that name is mandatory and shows an error when cleared. */
  @Test
  fun initProfileScreen_showsError_whenNameEmpty() {
    val nameField = composeRule.onNodeWithTag(InitProfileScreenTestTags.NAME_FIELD)

    // Trigger recomposition
    nameField.performTextInput("A")
    nameField.performTextClearance()
    composeRule.waitForIdle()

    composeRule.onNodeWithTag(InitProfileScreenTestTags.NAME_FIELD_ERROR).assertIsDisplayed()
  }

  /** Ensures that both username and name must be valid for the save button to be enabled. */
  @Test
  fun initProfileScreen_saveButton_enabledOnlyWhenValid() {
    // Invalid (both empty)
    composeRule.onNodeWithTag(InitProfileScreenTestTags.SAVE_BUTTON).assertIsNotEnabled()

    // Type only username → still invalid (name missing)
    composeRule.onNodeWithTag(InitProfileScreenTestTags.USERNAME).performTextInput("alice_ok")
    composeRule.waitForIdle()
    composeRule.onNodeWithTag(InitProfileScreenTestTags.SAVE_BUTTON).assertIsNotEnabled()

    // Add name → becomes valid
    composeRule.onNodeWithTag(InitProfileScreenTestTags.NAME_FIELD).performTextInput("Alice")
    composeRule.waitForIdle()
    composeRule.onNodeWithTag(InitProfileScreenTestTags.SAVE_BUTTON).assertIsEnabled()
  }

  /** Ensures that an invalid username format triggers the appropriate error message. */
  @Test
  fun onboardingScreen_showsError_whenUsernameInvalid() {
    composeRule.onNodeWithTag(InitProfileScreenTestTags.USERNAME).performTextInput("!!bad!!")
    composeRule.waitForIdle()

    composeRule.onNodeWithTag(InitProfileScreenTestTags.USERNAME_ERROR).assertIsDisplayed()
  }

  @Test
  fun initProfileScreen_showsError_whenBirthdayInvalid() {
    /**
     * Simulates entering an invalid birthday and verifies that the appropriate error message is
     * displayed using a test tag.
     */

    // Enter an invalid birthday
    composeRule
        .onNodeWithTag(InitProfileScreenTestTags.BIRTHDAY_FIELD)
        .performTextInput("31-31-2025")

    // Error message should appear below the birthday field
    composeRule.onNodeWithTag(InitProfileScreenTestTags.BIRTHDAY_FIELD_ERROR).assertIsDisplayed()
  }

  /** Ensures that valid inputs hide all error messages. */
  @Test
  fun initProfileScreen_noError_whenFieldsValid() {
    composeRule.onNodeWithTag(InitProfileScreenTestTags.USERNAME).performTextInput("validuser")
    composeRule.onNodeWithTag(InitProfileScreenTestTags.NAME_FIELD).performTextInput("Alice")
    composeRule.waitForIdle()

    composeRule.onNodeWithTag(InitProfileScreenTestTags.USERNAME_ERROR).assertDoesNotExist()
    composeRule.onNodeWithTag(InitProfileScreenTestTags.NAME_FIELD_ERROR).assertDoesNotExist()
  }

  /** Verifies that the username field retains its entered text after input. */
  @Test
  fun initProfileScreen_usernameField_retainsValue() {
    val username = "persistentuser"
    composeRule.onNodeWithTag(InitProfileScreenTestTags.USERNAME).performTextInput(username)
    composeRule.waitForIdle()
    composeRule.onNodeWithTag(InitProfileScreenTestTags.USERNAME).assertTextEquals(username)
  }

  /** Verifies that the name field retains its entered text after input. */
  @Test
  fun initProfileScreen_nameField_retainsValue() {
    val name = "TestUser"
    composeRule.onNodeWithTag(InitProfileScreenTestTags.NAME_FIELD).performTextInput(name)
    composeRule.waitForIdle()
    composeRule.onNodeWithTag(InitProfileScreenTestTags.NAME_FIELD).assertTextEquals(name)
  }

  /** Simulates user filling valid inputs and clicking the save button. */
  @Test
  fun initProfileScreen_clickSaveButton_isEnabledAndClickable() {
    composeRule.onNodeWithTag(InitProfileScreenTestTags.USERNAME).performTextInput("user123")
    composeRule.onNodeWithTag(InitProfileScreenTestTags.NAME_FIELD).performTextInput("User 123")
    composeRule.waitForIdle()

    val saveButton = composeRule.onNodeWithTag(InitProfileScreenTestTags.SAVE_BUTTON)
    saveButton.assertIsEnabled()
    saveButton.performClick()
    saveButton.assertExists()
  }

  /** Ensures school, school year and birthday inputs don’t affect save button availability. */
  @Test
  fun initProfileScreen_optionalFields_doNotBlockSave() {
    composeRule.onNodeWithTag(InitProfileScreenTestTags.USERNAME).performTextInput("alice_ok")
    composeRule.onNodeWithTag(InitProfileScreenTestTags.NAME_FIELD).performTextInput("Alice")

    composeRule
        .onNodeWithTag(InitProfileScreenTestTags.BIRTHDAY_FIELD)
        .performTextInput("02/10/2000")
    composeRule.onNodeWithTag(InitProfileScreenTestTags.SCHOOL_FIELD).performTextInput("EPFL")
    composeRule.onNodeWithTag(InitProfileScreenTestTags.SCHOOL_YEAR_FIELD).performTextInput("BA5")

    composeRule.waitForIdle()
    composeRule.onNodeWithTag(InitProfileScreenTestTags.SAVE_BUTTON).assertIsEnabled()
  }

  /** Ensures valid username displays success message text. */
  @Test
  fun initProfileScreen_showsValidUsernameConfirmationText() {
    composeRule.onNodeWithTag(InitProfileScreenTestTags.USERNAME).performTextInput("uniqueuser")
    composeRule.waitForIdle()

    composeRule.onNodeWithText("This username is available!").assertExists()
  }
}
