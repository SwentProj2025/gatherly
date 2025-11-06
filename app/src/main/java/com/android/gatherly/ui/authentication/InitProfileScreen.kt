package com.android.gatherly.ui.authentication

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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

          SettingsField(
              label = stringResource(R.string.settings_label_birthday),
              value = uiState.birthday,
              onValueChange = { settingsViewModel.editBirthday(it) },
              testTag = InitProfileScreenTestTags.BIRTHDAY_FIELD,
              errorMessage = uiState.invalidBirthdayMsg)

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
