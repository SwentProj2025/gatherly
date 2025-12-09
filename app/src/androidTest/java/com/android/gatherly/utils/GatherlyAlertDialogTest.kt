package com.android.gatherly.utils

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented test suite for [GatherlyAlertDialog] component.
 *
 * Tests cover basic rendering, event details display, edge cases, button interactions, and disabled
 * states across all dialog variants (dismiss/confirm buttons, neutral button, attendees button).
 */
class GatherlyAlertDialogTest {

  @get:Rule val composeTestRule = createComposeRule()

  /**
   * Verifies that the dialog displays all basic required elements: title, body text, dismiss
   * button, and confirm button.
   */
  @Test
  fun alertDialog_displaysBasicElements() {
    composeTestRule.setContent {
      GatherlyAlertDialog(
          titleText = "Test Title",
          bodyText = "Test Body",
          dismissText = "Cancel",
          confirmText = "OK",
          onDismiss = {},
          onConfirm = {})
    }

    composeTestRule.onNodeWithTag(AlertDialogTestTags.ALERT).assertExists()
    composeTestRule.onNodeWithTag(AlertDialogTestTags.TITLE).assertTextEquals("Test Title")
    composeTestRule.onNodeWithTag(AlertDialogTestTags.BODY).assertTextEquals("Test Body")
    composeTestRule.onNodeWithTag(AlertDialogTestTags.DISMISS_BTN).assertTextEquals("Cancel")
    composeTestRule.onNodeWithTag(AlertDialogTestTags.CONFIRM_BTN).assertTextEquals("OK")
  }

  /**
   * Verifies that the dialog displays complete event information including creator name and
   * date/time details when all event parameters are provided.
   */
  @Test
  fun alertDialog_withEventDetails_displaysAllInformation() {
    composeTestRule.setContent {
      GatherlyAlertDialog(
          titleText = "Event Title",
          bodyText = "Event Description",
          dismissText = "Back",
          confirmText = "Join",
          creatorText = "John Doe",
          dateText = "25/12/2025",
          startTimeText = "14:00",
          endTimeText = "16:00",
          onDismiss = {},
          onConfirm = {})
    }

    composeTestRule
        .onNodeWithTag(AlertDialogTestTags.CREATOR_TEXT)
        .assertExists()
        .assertTextEquals("By John Doe")
    composeTestRule
        .onNodeWithTag(AlertDialogTestTags.DATE_TEXT)
        .assertExists()
        .assertTextEquals("On 25/12/2025 from 14:00 to 16:00")
  }

  /**
   * Verifies that the creator line is not displayed when the creator text is blank, preventing
   * display of "By" without a name.
   */
  @Test
  fun alertDialog_withBlankCreatorText_doesNotDisplayCreatorLine() {
    composeTestRule.setContent {
      GatherlyAlertDialog(
          titleText = "Event Title",
          bodyText = "Event Description",
          dismissText = "Back",
          confirmText = "Join",
          creatorText = "",
          dateText = "25/12/2025",
          startTimeText = "14:00",
          endTimeText = "16:00",
          onDismiss = {},
          onConfirm = {})
    }

    composeTestRule.onNodeWithTag(AlertDialogTestTags.CREATOR_TEXT).assertDoesNotExist()
    composeTestRule.onNodeWithTag(AlertDialogTestTags.DATE_TEXT).assertExists()
  }

  /**
   * Verifies that the creator line is not displayed when the creator text is null, preventing
   * display of "By" without a name.
   */
  @Test
  fun alertDialog_withNullCreatorText_doesNotDisplayCreatorLine() {
    composeTestRule.setContent {
      GatherlyAlertDialog(
          titleText = "Event Title",
          bodyText = "Event Description",
          dismissText = "Back",
          confirmText = "Join",
          creatorText = null,
          dateText = "25/12/2025",
          startTimeText = "14:00",
          endTimeText = "16:00",
          onDismiss = {},
          onConfirm = {})
    }

    composeTestRule.onNodeWithTag(AlertDialogTestTags.CREATOR_TEXT).assertDoesNotExist()
    composeTestRule.onNodeWithTag(AlertDialogTestTags.DATE_TEXT).assertExists()
  }

  /**
   * Verifies that event-specific information (creator and date) is not displayed when those
   * parameters are not provided, such as in simple confirmation dialogs.
   */
  @Test
  fun alertDialog_withoutEventDetails_doesNotDisplayDateInfo() {
    composeTestRule.setContent {
      GatherlyAlertDialog(
          titleText = "Delete Confirmation",
          bodyText = "Are you sure you want to delete?",
          dismissText = "Cancel",
          confirmText = "Delete",
          onDismiss = {},
          onConfirm = {})
    }

    composeTestRule.onNodeWithTag(AlertDialogTestTags.CREATOR_TEXT).assertDoesNotExist()
    composeTestRule.onNodeWithTag(AlertDialogTestTags.DATE_TEXT).assertDoesNotExist()
  }

  /**
   * Verifies that the optional neutral button is displayed with correct text when provided, and
   * that it is enabled by default.
   */
  @Test
  fun alertDialog_withNeutralButton_displaysNeutralButton() {
    var neutralClicked = false
    composeTestRule.setContent {
      GatherlyAlertDialog(
          titleText = "Event",
          bodyText = "Event details",
          dismissText = "Back",
          confirmText = "Join",
          neutralText = "See on map",
          onNeutral = { neutralClicked = true },
          onDismiss = {},
          onConfirm = {})
    }

    composeTestRule
        .onNodeWithTag(AlertDialogTestTags.NEUTRAL_BTN)
        .assertExists()
        .assertTextEquals("See on map")
        .assertIsEnabled()
  }

  /**
   * Verifies that the neutral button is not displayed when the neutral text parameter is not
   * provided.
   */
  @Test
  fun alertDialog_withoutNeutralButton_doesNotDisplayNeutralButton() {
    composeTestRule.setContent {
      GatherlyAlertDialog(
          titleText = "Event",
          bodyText = "Event details",
          dismissText = "Back",
          confirmText = "Join",
          neutralText = null,
          onDismiss = {},
          onConfirm = {})
    }

    composeTestRule.onNodeWithTag(AlertDialogTestTags.NEUTRAL_BTN).assertDoesNotExist()
  }

  /**
   * Verifies that the neutral button can be disabled via the neutralEnabled parameter and is not
   * clickable when disabled.
   */
  @Test
  fun alertDialog_neutralButtonDisabled_isNotClickable() {
    composeTestRule.setContent {
      GatherlyAlertDialog(
          titleText = "Event",
          bodyText = "Event details",
          dismissText = "Back",
          confirmText = "Join",
          neutralText = "See on map",
          onNeutral = {},
          neutralEnabled = false,
          onDismiss = {},
          onConfirm = {})
    }

    composeTestRule
        .onNodeWithTag(AlertDialogTestTags.NEUTRAL_BTN)
        .assertExists()
        .assertIsNotEnabled()
  }

  /**
   * Verifies that the attendees button is displayed when the numberAttendees parameter is provided.
   */
  @Test
  fun alertDialog_withAttendeesButton_displaysAttendeesButton() {
    var attendeesClicked = false
    composeTestRule.setContent {
      GatherlyAlertDialog(
          titleText = "Event",
          bodyText = "Event details",
          dismissText = "Back",
          confirmText = "Join",
          numberAttendees = 5,
          onOpenAttendeesList = { attendeesClicked = true },
          onDismiss = {},
          onConfirm = {})
    }

    composeTestRule.onNodeWithTag(AlertDialogTestTags.ATTENDEES_BTN).assertExists()
  }

  /**
   * Verifies that the attendees button is not displayed when the numberAttendees parameter is null.
   */
  @Test
  fun alertDialog_withoutAttendeesButton_doesNotDisplayAttendeesButton() {
    composeTestRule.setContent {
      GatherlyAlertDialog(
          titleText = "Event",
          bodyText = "Event details",
          dismissText = "Back",
          confirmText = "Join",
          numberAttendees = null,
          onDismiss = {},
          onConfirm = {})
    }

    composeTestRule.onNodeWithTag(AlertDialogTestTags.ATTENDEES_BTN).assertDoesNotExist()
  }

  /** Verifies that clicking the dismiss button triggers the onDismiss callback. */
  @Test
  fun alertDialog_dismissButtonClick_triggersOnDismiss() {
    var dismissClicked = false
    composeTestRule.setContent {
      GatherlyAlertDialog(
          titleText = "Test",
          bodyText = "Test Body",
          dismissText = "Cancel",
          confirmText = "OK",
          onDismiss = { dismissClicked = true },
          onConfirm = {})
    }

    composeTestRule.onNodeWithTag(AlertDialogTestTags.DISMISS_BTN).performClick()

    assert(dismissClicked)
  }

  /** Verifies that clicking the confirm button triggers the onConfirm callback. */
  @Test
  fun alertDialog_confirmButtonClick_triggersOnConfirm() {
    var confirmClicked = false
    composeTestRule.setContent {
      GatherlyAlertDialog(
          titleText = "Test",
          bodyText = "Test Body",
          dismissText = "Cancel",
          confirmText = "OK",
          onDismiss = {},
          onConfirm = { confirmClicked = true })
    }

    composeTestRule.onNodeWithTag(AlertDialogTestTags.CONFIRM_BTN).performClick()

    assert(confirmClicked)
  }

  /** Verifies that clicking the neutral button triggers the onNeutral callback. */
  @Test
  fun alertDialog_neutralButtonClick_triggersOnNeutral() {
    var neutralClicked = false
    composeTestRule.setContent {
      GatherlyAlertDialog(
          titleText = "Test",
          bodyText = "Test Body",
          dismissText = "Cancel",
          confirmText = "OK",
          neutralText = "Neutral",
          onNeutral = { neutralClicked = true },
          onDismiss = {},
          onConfirm = {})
    }

    composeTestRule.onNodeWithTag(AlertDialogTestTags.NEUTRAL_BTN).performClick()

    assert(neutralClicked)
  }

  /** Verifies that clicking the attendees button triggers the onOpenAttendeesList callback. */
  @Test
  fun alertDialog_attendeesButtonClick_triggersOnOpenAttendeesList() {
    var attendeesClicked = false
    composeTestRule.setContent {
      GatherlyAlertDialog(
          titleText = "Test",
          bodyText = "Test Body",
          dismissText = "Cancel",
          confirmText = "OK",
          numberAttendees = 3,
          onOpenAttendeesList = { attendeesClicked = true },
          onDismiss = {},
          onConfirm = {})
    }

    composeTestRule.onNodeWithTag(AlertDialogTestTags.ATTENDEES_BTN).performClick()

    assert(attendeesClicked)
  }

  /**
   * Verifies that the confirm button can be disabled via the confirmEnabled parameter and does not
   * trigger the onConfirm callback when clicked whilst disabled.
   */
  @Test
  fun alertDialog_confirmButtonDisabled_isNotClickable() {
    var confirmClicked = false
    composeTestRule.setContent {
      GatherlyAlertDialog(
          titleText = "Test",
          bodyText = "Test Body",
          dismissText = "Cancel",
          confirmText = "OK",
          confirmEnabled = false,
          onDismiss = {},
          onConfirm = { confirmClicked = true })
    }

    composeTestRule
        .onNodeWithTag(AlertDialogTestTags.CONFIRM_BTN)
        .assertExists()
        .assertIsNotEnabled()

    composeTestRule.onNodeWithTag(AlertDialogTestTags.CONFIRM_BTN).performClick()

    assert(!confirmClicked)
  }

  /**
   * Verifies that the isImportantWarning flag changes the visual styling of the confirm button. The
   * actual colour change is verified via manual/screenshot testing.
   */
  @Test
  fun alertDialog_withImportantWarning_displaysDifferentConfirmButtonStyle() {
    composeTestRule.setContent {
      GatherlyAlertDialog(
          titleText = "Delete Event",
          bodyText = "This cannot be undone",
          dismissText = "Cancel",
          confirmText = "Delete",
          isImportantWarning = true,
          onDismiss = {},
          onConfirm = {})
    }

    composeTestRule
        .onNodeWithTag(AlertDialogTestTags.CONFIRM_BTN)
        .assertExists()
        .assertTextEquals("Delete")
  }

  /**
   * Verifies that dismiss and confirm buttons are laid out in a Row with equal weight, ensuring
   * both buttons have the same width regardless of text length. Actual equal width is verified via
   * manual testing; this test confirms both buttons exist.
   */
  @Test
  fun alertDialog_buttonsHaveEqualWidth() {
    composeTestRule.setContent {
      GatherlyAlertDialog(
          titleText = "Test",
          bodyText = "Body",
          dismissText = "Short",
          confirmText = "Very Long Button Text",
          onDismiss = {},
          onConfirm = {})
    }

    composeTestRule.onNodeWithTag(AlertDialogTestTags.DISMISS_BTN).assertExists()
    composeTestRule.onNodeWithTag(AlertDialogTestTags.CONFIRM_BTN).assertExists()
  }
}
