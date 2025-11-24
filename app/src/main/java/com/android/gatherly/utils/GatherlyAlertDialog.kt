package com.android.gatherly.utils

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign

@Composable
fun GatherlyAlertDialog(
    titleText: String,
    bodyText: String,
    dismissText: String,
    confirmText: String,
    creatorText: String?,
    dateText: String?,
    startTimeText: String?,
    endTimeText: String?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isImportantWarning: Boolean = false,
    confirmEnabled: Boolean = true
) {
  AlertDialog(
      containerColor = MaterialTheme.colorScheme.surfaceVariant,
      titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
      textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.testTag(AlertDialogTestTags.ALERT),
      title = {
        Column {
          Text(
              text = titleText,
              textAlign = TextAlign.Center,
              style = MaterialTheme.typography.bodyLarge,
              modifier = Modifier.testTag(AlertDialogTestTags.TITLE))

          if (dateText == null || startTimeText == null || endTimeText == null) return@Column

          if (creatorText != null) {
            Text(
                text = "By $creatorText",
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth().testTag(AlertDialogTestTags.CREATOR_TEXT))
          }

          Text(
              text = "On $dateText From $startTimeText to $endTimeText",
              textAlign = TextAlign.End,
              style = MaterialTheme.typography.bodyMedium,
              modifier = Modifier.fillMaxWidth().testTag(AlertDialogTestTags.DATE_TEXT))
        }
      },
      text = {
        Text(
            text = bodyText,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag(AlertDialogTestTags.BODY))
      },
      dismissButton = {
        Button(
            colors =
                buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
            onClick = onDismiss,
            modifier = Modifier.testTag(AlertDialogTestTags.DISMISS_BTN)) {
              Text(text = dismissText, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
      },
      onDismissRequest = onDismiss,
      confirmButton = {
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
            modifier = Modifier.testTag(AlertDialogTestTags.CONFIRM_BTN)) {
              Text(
                  text = confirmText,
                  color =
                      if (isImportantWarning) {
                        MaterialTheme.colorScheme.error
                      } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                      })
            }
      },
  )
}

object AlertDialogTestTags {
  const val ALERT = "alert"
  const val TITLE = "title"
  const val BODY = "body"
  const val DISMISS_BTN = "dismissButton"
  const val CONFIRM_BTN = "confirmButton"

  const val CREATOR_TEXT = "creatorText"

  const val DATE_TEXT = "dateText"
}
