package com.android.gatherly.ui.events

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import com.android.gatherly.R
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * A reusable composable that displays a date input field. When clicked, it opens a Material 3 Date
 * Picker Dialog.
 *
 * @param onDateChanged Callback triggered whenever the user selects a new date. The date is
 *   returned as a formatted string "dd/MM/yyyy".
 * @param dueDateError Boolean indicating if an error should be displayed.
 * @param textFieldColors The colors to be used for the text field.
 * @param testTagInput The test tag used for the date input field.
 * @param testTagErrorMessage The test tag used for the error message text.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateInputField(
    initialDate: String = "",
    onDateChanged: (String) -> Unit = {},
    dueDateError: Boolean,
    textFieldColors: TextFieldColors,
    testTagInput: String,
    testTagErrorMessage: String,
    onClick: () -> Unit
) {
  val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

  val dateFieldValue = remember(initialDate) { TextFieldValue(initialDate) }
  OutlinedTextField(
      value = dateFieldValue,
      onValueChange = {},
      readOnly = true,
      label = { Text(stringResource(R.string.events_date_field_label)) },
      placeholder = { Text("dd/MM/yyyy") },
      isError = dueDateError,
      supportingText = {
        if (dueDateError) {
          Text("Invalid format or past date", modifier = Modifier.testTag(testTagErrorMessage))
        }
      },
      colors = textFieldColors,
      modifier = Modifier.fillMaxWidth().testTag(testTagInput).clickable { onClick() },
  )
}
