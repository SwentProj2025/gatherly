package com.android.gatherly.ui.todo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.PopupProperties
import com.android.gatherly.R
import com.android.gatherly.model.map.Location
import kotlin.collections.filterNotNull
import kotlin.collections.isNotEmpty

/**
 * A reusable composable that displays a location text input field with an attached dropdown menu
 * showing location suggestions. This component is shared between the AddTodo and EditTodo screens
 * to avoid code duplication.
 *
 * @param location The current text value of the location field.
 * @param suggestions The list of suggested locations returned by the ViewModel.
 * @param onLocationChanged Callback triggered whenever the user edits the location text.
 * @param onSelectLocation Callback invoked when a suggestion is selected from the dropdown.
 * @param modifier The modifier to apply to the outer Box of this component.
 * @param testTagInput The test tag used for the location input field.
 * @param testTagDropdown The test tag used for the dropdown menu.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSuggestions(
    location: String,
    suggestions: List<Location>,
    onLocationChanged: (String) -> Unit,
    onSelectLocation: (Location) -> Unit,
    modifier: Modifier = Modifier,
    testTagInput: String,
    testTagDropdown: String,
) {
  val suggestionLimit = integerResource(R.integer.todo_location_number_of_suggestions)
  val suggestionTextLimit = integerResource(R.integer.todo_location_suggestion_length)
  val moreSuggestionsText = stringResource(R.string.todos_location_suggestions_more)
  val etc = stringResource(R.string.todos_location_text_etc)
  val label = stringResource(R.string.todos_location_field_label)
  val placeholder = stringResource(R.string.todos_location_field_placeholder)

  val textFieldColors =
      TextFieldDefaults.colors(
          focusedContainerColor = MaterialTheme.colorScheme.background,
          unfocusedContainerColor = MaterialTheme.colorScheme.background,
          unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
          focusedTextColor = MaterialTheme.colorScheme.onBackground,
          errorTextColor = MaterialTheme.colorScheme.onBackground,
      )

  var showLocationDropdown by remember { mutableStateOf(false) }

  Box(modifier = modifier.fillMaxWidth()) {
    OutlinedTextField(
        value = location,
        onValueChange = {
          onLocationChanged(it)
          showLocationDropdown = it.isNotBlank()
        },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        colors = textFieldColors,
        modifier = Modifier.fillMaxWidth().testTag(testTagInput))

    DropdownMenu(
        expanded = showLocationDropdown && suggestions.isNotEmpty(),
        onDismissRequest = { showLocationDropdown = false },
        properties = PopupProperties(focusable = false),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        modifier =
            Modifier.testTag(testTagDropdown)
                .fillMaxWidth()
                .heightIn(dimensionResource(R.dimen.todo_location_dropdown_height))) {
          suggestions.filterNotNull().take(suggestionLimit).forEach { loc ->
            DropdownMenuItem(
                text = {
                  val displayName =
                      loc.name.take(suggestionTextLimit) +
                          if (loc.name.length > suggestionTextLimit) etc else ""

                  Text(
                      text = displayName,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      maxLines = 1)
                },
                onClick = {
                  onSelectLocation(loc)
                  showLocationDropdown = false
                },
                modifier =
                    Modifier.padding(dimensionResource(R.dimen.todo_location_dropdown_padding)))
            Divider()
          }

          if (suggestions.size > suggestionLimit) {
            DropdownMenuItem(
                text = {
                  Text(moreSuggestionsText, color = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                onClick = {})
          }
        }
  }
}
