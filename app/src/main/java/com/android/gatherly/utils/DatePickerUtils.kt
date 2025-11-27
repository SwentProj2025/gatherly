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

@Composable
fun DatePickerInputField(
    value: String,
    label: String,
    placeholder: String = "dd/MM/yyyy",
    isError: Boolean,
    errorMessage: String = "",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTagInput: String,
    testTagError: String,
    colors: TextFieldColors
) {
  Box(modifier = modifier.fillMaxWidth()) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        isError = isError,
        supportingText = {
          if (isError && errorMessage.isNotBlank()) {
            Text(errorMessage, modifier = Modifier.testTag(testTagError))
          }
        },
        colors = colors,
        modifier = Modifier.fillMaxWidth().testTag(testTagInput))

    Box(modifier = Modifier.matchParentSize().clickable { onClick() })
  }
}
