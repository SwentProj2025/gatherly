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
import androidx.compose.ui.platform.LocalContext
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
import com.android.gatherly.ui.settings.SettingsScreenTestTags
import com.android.gatherly.ui.settings.SettingsViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

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
  val context = LocalContext.current
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
                      .testTag(SettingsScreenTestTags.PROFILE_PICTURE),
              contentScale = ContentScale.Crop)

          Spacer(
              modifier =
                  Modifier.height(dimensionResource(id = R.dimen.spacing_between_fields_regular)))

          SettingsField(
              label = stringResource(R.string.settings_label_username),
              value = uiState.username,
              onValueChange = { settingsViewModel.editUsername(it) },
              testTag = "onboarding_username_field",
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
              testTag = "onboarding_name_field",
              errorMessage = uiState.invalidNameMsg)

          Spacer(
              modifier =
                  Modifier.height(dimensionResource(id = R.dimen.spacing_between_fields_regular)))

          SettingsField(
              label = stringResource(R.string.settings_label_school),
              value = uiState.school,
              onValueChange = { settingsViewModel.editSchool(it) },
              testTag = "onboarding_school_field")

          Spacer(
              modifier =
                  Modifier.height(dimensionResource(id = R.dimen.spacing_between_fields_regular)))

          SettingsField(
              label = stringResource(R.string.settings_label_school_year),
              value = uiState.schoolYear,
              onValueChange = { settingsViewModel.editSchoolYear(it) },
              testTag = "onboarding_school_year_field")

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
                      .testTag("onboarding_save_button")) {
                Text(
                    text = stringResource(R.string.settings_save),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium)
              }

          Spacer(modifier = Modifier.weight(1f))
        }
  }
}
