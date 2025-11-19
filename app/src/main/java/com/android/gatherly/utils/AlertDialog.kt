package com.android.gatherly.utils

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
        Text(
            text = titleText,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag(AlertDialogTestTags.TITLE))
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
}
