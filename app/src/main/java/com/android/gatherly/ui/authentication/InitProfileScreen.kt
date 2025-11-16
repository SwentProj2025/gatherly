package com.android.gatherly.ui.authentication

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.R
import com.android.gatherly.ui.navigation.NavigationActions
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.settings.SettingsField
import com.android.gatherly.ui.settings.SettingsViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

object InitProfileScreenTestTags {
  const val PROFILE_PICTURE = "initProfile_profile_picture"
  const val USERNAME = "initProfile_username"
  const val NAME_FIELD = "initProfile_name_field"
  const val BIRTHDAY_FIELD = "initProfile_birthday_field"
  const val SCHOOL_FIELD = "initProfile_school_field"
  const val SCHOOL_YEAR_FIELD = "initProfile_school_year_field"
  const val SAVE_BUTTON = "initProfile_save_button"
  const val USERNAME_ERROR = "initProfile_username_error"
  const val NAME_FIELD_ERROR = "initProfile_name_field_error"
  const val BIRTHDAY_FIELD_ERROR = "initProfile_birthday_field_error"
}
/**
 * This screen is shown right after first login, to force the user to fill in a profile. Same fields
 * and ViewModel as SettingsScreen.
 *
 * Differences:
 * - No top or bottom navigation
 * - User can’t leave until they save
 * - On save, navigates to Home
 */
@Composable
fun InitProfileScreen(
    navigationActions: NavigationActions? = null,
    settingsViewModel: SettingsViewModel = viewModel()
) {
  val uiState by settingsViewModel.uiState.collectAsState()
  val currentUser = Firebase.auth.currentUser

  val birthdayFieldColors =
      OutlinedTextFieldDefaults.colors(
          focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
          unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
          disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
          focusedTextColor = MaterialTheme.colorScheme.onBackground,
          unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
          cursorColor = MaterialTheme.colorScheme.primary,
          focusedBorderColor = MaterialTheme.colorScheme.primary,
          unfocusedBorderColor = Color.Transparent)

  LaunchedEffect(currentUser?.uid) { currentUser?.uid?.let { settingsViewModel.loadProfile(it) } }

  Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
    Column(
        modifier =
            Modifier.fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = dimensionResource(id = R.dimen.padding_regular)),
        horizontalAlignment = Alignment.CenterHorizontally) {
          Spacer(
              modifier =
                  Modifier.height(dimensionResource(id = R.dimen.spacing_between_fields_medium)))

          Image(
              painter =
                  painterResource(
                      id = R.drawable.ic_launcher_foreground), // currently a placeholder image
              contentDescription = stringResource(R.string.settings_profile_picture_description),
              modifier =
                  Modifier.size(dimensionResource(id = R.dimen.profile_pic_size))
                      .clip(CircleShape)
                      .testTag(InitProfileScreenTestTags.PROFILE_PICTURE),
              contentScale = ContentScale.Crop)

          Spacer(
              modifier =
                  Modifier.height(dimensionResource(id = R.dimen.spacing_between_fields_regular)))

          SettingsField(
              label = stringResource(R.string.settings_label_username),
              value = uiState.username,
              onValueChange = { settingsViewModel.editUsername(it) },
              testTag = InitProfileScreenTestTags.USERNAME,
              errorMessage = uiState.invalidUsernameMsg)

          if (uiState.isUsernameAvailable == true && uiState.invalidUsernameMsg == null) {
            Text(
                text = stringResource(R.string.settings_valid_username),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                modifier =
                    Modifier.padding(top = dimensionResource(id = R.dimen.padding_extra_small)))
          }

          Spacer(
              modifier =
                  Modifier.height(dimensionResource(id = R.dimen.spacing_between_fields_regular)))

          SettingsField(
              label = stringResource(R.string.settings_label_name),
              value = uiState.name,
              onValueChange = { settingsViewModel.editName(it) },
              testTag = InitProfileScreenTestTags.NAME_FIELD,
              errorMessage = uiState.invalidNameMsg)

          Spacer(
              modifier =
                  Modifier.height(dimensionResource(id = R.dimen.spacing_between_fields_regular)))

          // Birthday Field
          Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.settings_label_birthday),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_small)))
            var dateFieldValue by remember { mutableStateOf(TextFieldValue("")) }
            dateFieldValue = dateFieldValue.copy(text = uiState.birthday)
            OutlinedTextField(
                value = dateFieldValue,
                onValueChange = { newValue ->
                  updateBirthdayField(
                      newValue = newValue,
                      currentValue = dateFieldValue,
                      onFormattedChange = { formatted ->
                        dateFieldValue = formatted
                        settingsViewModel.editBirthday(formatted.text)
                      })
                },
                modifier =
                    Modifier.fillMaxWidth().testTag(InitProfileScreenTestTags.BIRTHDAY_FIELD),
                colors = birthdayFieldColors,
                shape =
                    RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_shape_medium)),
                singleLine = true,
                keyboardOptions =
                    KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                textStyle =
                    LocalTextStyle.current.copy(
                        fontSize = dimensionResource(id = R.dimen.font_size_medium).value.sp))

            // Show error message if not null
            if (!uiState.invalidBirthdayMsg.isNullOrEmpty()) {
              Text(
                  text = uiState.invalidBirthdayMsg!!,
                  color = MaterialTheme.colorScheme.error,
                  fontSize = dimensionResource(id = R.dimen.font_size_regular).value.sp,
                  modifier =
                      Modifier.padding(top = dimensionResource(id = R.dimen.padding_extra_small))
                          .testTag("${InitProfileScreenTestTags.BIRTHDAY_FIELD}_error"))
            }
          }

          Spacer(
              modifier =
                  Modifier.height(dimensionResource(id = R.dimen.spacing_between_fields_regular)))

          SettingsField(
              label = stringResource(R.string.settings_label_school),
              value = uiState.school,
              onValueChange = { settingsViewModel.editSchool(it) },
              testTag = InitProfileScreenTestTags.SCHOOL_FIELD)

          Spacer(
              modifier =
                  Modifier.height(dimensionResource(id = R.dimen.spacing_between_fields_regular)))

          SettingsField(
              label = stringResource(R.string.settings_label_school_year),
              value = uiState.schoolYear,
              onValueChange = { settingsViewModel.editSchoolYear(it) },
              testTag = InitProfileScreenTestTags.SCHOOL_YEAR_FIELD)

          Spacer(
              modifier =
                  Modifier.height(dimensionResource(id = R.dimen.spacing_between_fields_medium)))

          Button(
              onClick = {
                currentUser?.uid?.let { uid ->
                  settingsViewModel.updateProfile(uid, isFirstTime = true)
                  if (uiState.isValid) {
                    navigationActions?.navigateTo(Tab.HomePage.destination)
                  }
                }
              },
              enabled = uiState.isValid,
              modifier =
                  Modifier.fillMaxWidth(0.8f)
                      .height(dimensionResource(id = R.dimen.settings_save_button_height))
                      .testTag(InitProfileScreenTestTags.SAVE_BUTTON)) {
                Text(
                    text = stringResource(R.string.settings_save),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium)
              }

          Spacer(modifier = Modifier.weight(1f))
        }
  }
}

/**
 * Formats the input string into "dd/MM/yyyy" format as the user types. Non-digit characters are
 * removed, and slashes are inserted at appropriate positions.
 */
private fun formatDateInput(input: String): String {
  // Remove any non-digit characters
  val digits = input.filter { it.isDigit() }.take(8) // Limit to ddMMyyyy
  return buildString {
    for (i in digits.indices) {
      append(digits[i])
      if (i == 1 || i == 3) append('/')
    }
  }
}

/** Updates the birthday field with formatted text and correct cursor position. */
private fun updateBirthdayField(
    newValue: TextFieldValue,
    currentValue: TextFieldValue,
    onFormattedChange: (TextFieldValue) -> Unit
) {
  val oldText = currentValue.text
  val newText = newValue.text

  // Detect if user is deleting (backspace)
  val isDeleting = newText.length < oldText.length

  val formatted = formatDateInput(newText)

  // Calculate new cursor position
  val newCursorPos =
      when {
        isDeleting -> newValue.selection.start.coerceAtMost(formatted.length)
        else -> formatted.length // keep cursor at end when typing
      }

  onFormattedChange(newValue.copy(text = formatted, selection = TextRange(newCursorPos)))
}
