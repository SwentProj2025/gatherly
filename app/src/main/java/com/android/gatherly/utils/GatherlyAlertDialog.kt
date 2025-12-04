package com.android.gatherly.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun GatherlyAlertDialog(
    titleText: String,
    bodyText: String,
    dismissText: String,
    confirmText: String,
    creatorText: String? = null,
    dateText: String? = null,
    startTimeText: String? = null,
    endTimeText: String? = null,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isImportantWarning: Boolean = false,
    confirmEnabled: Boolean = true,
    neutralText: String? = null,
    onNeutral: (() -> Unit)? = null,
    neutralEnabled: Boolean = true,
    onOpenAttendeesList: (() -> Unit)? = null,
    numberAttendees: Int? = null,
) {
  AlertDialog(
      containerColor = MaterialTheme.colorScheme.surfaceVariant,
      titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
      textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.testTag(AlertDialogTestTags.ALERT),
      title = {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = titleText,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f).testTag(AlertDialogTestTags.TITLE))

            // Button to view attendees
            if (numberAttendees != null) {
              Spacer(modifier = Modifier.height(12.dp))

              Button(
                  colors =
                      buttonColors(
                          containerColor = MaterialTheme.colorScheme.secondaryContainer,
                          contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                  contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                  onClick = { onOpenAttendeesList?.invoke() },
                  modifier =
                      Modifier.testTag(AlertDialogTestTags.ATTENDEES_BTN)
                          .height(32.dp)
                          .widthIn(min = 32.dp)) {
                    BoxNumberAttendees(numberAttendees)
                  }
            }
          }

          GatherlyDialogTitleContent(
              creatorText = creatorText,
              dateText = dateText,
              startTimeText = startTimeText,
              endTimeText = endTimeText)
        }
      },
      text = {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
              text = bodyText,
              textAlign = TextAlign.Center,
              modifier = Modifier.testTag(AlertDialogTestTags.BODY))

          neutralText?.let { text ->
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                colors =
                    buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                onClick = { onNeutral?.invoke() },
                enabled = neutralEnabled,
                modifier = Modifier.fillMaxWidth().testTag(AlertDialogTestTags.NEUTRAL_BTN)) {
                  Text(text = text)
                }
          }
        }
      },

      // Entire button logic here, to get spacing
      confirmButton = {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              // Dismiss Button
              Button(
                  colors =
                      buttonColors(
                          containerColor = MaterialTheme.colorScheme.primaryContainer,
                          contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                  onClick = onDismiss,
                  modifier =
                      Modifier.weight(1f) // Takes up 50%
                          .testTag(AlertDialogTestTags.DISMISS_BTN)) {
                    Text(
                        text = dismissText,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center,
                        maxLines = 1)
                  }

              // Confirm Button
              Button(
                  colors =
                      buttonColors(
                          containerColor =
                              if (isImportantWarning) {
                                MaterialTheme.colorScheme.surfaceVariant
                              } else {
                                MaterialTheme.colorScheme.primaryContainer
                              },
                          contentColor =
                              if (isImportantWarning) {
                                MaterialTheme.colorScheme.error
                              } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                              }),
                  enabled = confirmEnabled,
                  onClick = onConfirm,
                  modifier = Modifier.weight(1f).testTag(AlertDialogTestTags.CONFIRM_BTN)) {
                    Text(
                        text = confirmText,
                        color =
                            if (isImportantWarning) {
                              MaterialTheme.colorScheme.error
                            } else {
                              MaterialTheme.colorScheme.onPrimaryContainer
                            },
                        textAlign = TextAlign.Center,
                        maxLines = 1)
                  }
            }
      },
      onDismissRequest = onDismiss,
  )
}

/**
 * Helper function: Display all the information displayed only for the event alert dialog.
 *
 * @param creatorText the name of the creator of the event
 * @param dateText the date of the event
 * @param startTimeText the starting time of the event
 * @param endTimeText the ending time of the event
 */
@Composable
private fun GatherlyDialogTitleContent(
    creatorText: String?,
    dateText: String?,
    startTimeText: String?,
    endTimeText: String?
) {

  if (dateText == null || startTimeText == null || endTimeText == null) return

  if (creatorText != null) {
    Text(
        text = "By $creatorText",
        textAlign = TextAlign.End,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.fillMaxWidth().testTag(AlertDialogTestTags.CREATOR_TEXT))
  }

  Text(
      text = "On $dateText from $startTimeText to $endTimeText",
      textAlign = TextAlign.End,
      style = MaterialTheme.typography.bodyMedium,
      modifier = Modifier.fillMaxWidth().testTag(AlertDialogTestTags.DATE_TEXT))
}

object AlertDialogTestTags {
  const val ALERT = "alert"
  const val TITLE = "title"
  const val BODY = "body"
  const val DISMISS_BTN = "dismissButton"
  const val CONFIRM_BTN = "confirmButton"
  const val NEUTRAL_BTN = "neutralButton"

  const val CREATOR_TEXT = "creatorText"

  const val DATE_TEXT = "dateText"

  const val ATTENDEES_BTN = "attendeesButton"
}
