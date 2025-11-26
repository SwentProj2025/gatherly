package com.android.gatherly.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isImportantWarning: Boolean = false,
    confirmEnabled: Boolean = true,
    neutralText: String? = null,
    onNeutral: (() -> Unit)? = null,
    neutralEnabled: Boolean = true
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
      dismissButton = null,

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

object AlertDialogTestTags {
  const val ALERT = "alert"
  const val TITLE = "title"
  const val BODY = "body"
  const val DISMISS_BTN = "dismissButton"
  const val CONFIRM_BTN = "confirmButton"
  const val NEUTRAL_BTN = "neutralButton"
}
