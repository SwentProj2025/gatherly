package com.android.gatherly.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DatePickerTestTags {
  const val DATE_PICKER_DIALOG = "datePickerDialog"
  const val DATE_PICKER_SAVE = "datePickerSaveButton"
}

private const val placeholder: String = "dd/MM/yyyy"

/**
 * Helper function who displays a Material3 Date Picker dialog allowing the user to select a date.
 *
 * The dialog is shown only when [show] is true. It initializes with the provided [initialDate] if
 * valid and formats the selected date using the specified [dateFormat].
 *
 * Once a date is selected and confirmed, the formatted date is returned via [onDateSelected], and
 * the dialog is dismissed via [onDismiss].
 *
 * @param show Controls whether the Date Picker dialog is visible.
 * @param initialDate Initial date displayed in the picker, formatted as defined by [dateFormat].
 * @param onDateSelected Callback triggered when a date is selected and confirmed.
 * @param onDismiss Callback triggered when the dialog is dismissed.
 * @param dateFormat Format used to parse and display the date (default: "dd/MM/yyyy").
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GatherlyDatePicker(
    show: Boolean,
    initialDate: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    dateFormat: String = "dd/MM/yyyy"
) {
  if (!show) return

  val formatter = remember { SimpleDateFormat(dateFormat, Locale.getDefault()) }

  val initialDateMillis =
      remember(initialDate) {
        try {
          if (initialDate.isNotBlank()) {
            formatter.parse(initialDate)?.time
          } else null
        } catch (e: Exception) {
          null
        }
      }

  val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)

  DatePickerDialog(
      modifier = Modifier.testTag(DatePickerTestTags.DATE_PICKER_DIALOG),
      onDismissRequest = onDismiss,
      confirmButton = {
        TextButton(
            modifier = Modifier.testTag(DatePickerTestTags.DATE_PICKER_SAVE),
            onClick = {
              datePickerState.selectedDateMillis?.let { onDateSelected(formatter.format(Date(it))) }
              onDismiss()
            }) {
              Text("Save")
            }
      },
      dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }) {
        DatePicker(state = datePickerState)
      }
}

/**
 * Helper function : Read-only input date field that displays a selected date and triggers the Date
 * Picker when clicked.
 *
 * This composable shows an outlined text field styled for date input, with optional error handling
 *
 * @param value Current value displayed in the field.
 * @param label Label displayed above the input field.
 * @param placeholder Placeholder text when the field is empty.
 * @param isErrorMessage Indicates whether the field is in an error state with a specific message.
 * @param onClick Callback invoked when the field is clicked.
 * @param modifier Modifier applied to the root container.
 * @param testTag will represent (testTagInput: String,testTagError: String)
 * @param colors Color configuration for the text field.
 */
@Composable
fun DatePickerInputField(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    isErrorMessage: String?,
    onClick: () -> Unit,
    testTag: Pair<String, String>,
    colors: TextFieldColors
) {
  Box(modifier = modifier.fillMaxWidth().testTag(testTag.first)) { // Tag OUTER box
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        isError = isErrorMessage != null,
        supportingText = {
          if (isErrorMessage != null) {
            Text(isErrorMessage, modifier = Modifier.testTag(testTag.second))
          }
        },
        colors = colors,
        modifier = Modifier.fillMaxWidth()) // No tag here

    Box(modifier = Modifier.matchParentSize().clickable { onClick() }) // No tag here either
  }
}
