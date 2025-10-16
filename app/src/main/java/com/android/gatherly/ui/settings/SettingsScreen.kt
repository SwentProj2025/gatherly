package com.android.gatherly.ui.settings

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.R
import com.android.gatherly.ui.navigation.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

object SettingsScreenTestTags {
  const val PROFILE_PICTURE = "settings_profile_picture"
  const val USERNAME = "settings_username"
  const val EDIT_PHOTO_BUTTON = "settings_edit_photo_button"
  const val NAME_FIELD = "settings_name_field"
  const val BIRTHDAY_FIELD = "settings_birthday_field"
  const val SCHOOL_FIELD = "settings_school_field"
  const val SCHOOL_YEAR_FIELD = "settings_school_year_field"
  const val SAVE_BUTTON = "settings_save_button"
}

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = viewModel(),
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current),
    onSignedOut: () -> Unit = {},
    navigationActions: NavigationActions? = null,
) {

  val currentUser = Firebase.auth.currentUser

  // Load the profile only once when the screen appears
  LaunchedEffect(currentUser?.uid) {
    currentUser?.uid?.let { uid -> settingsViewModel.loadProfile(uid) }
  }

  val uiState by settingsViewModel.uiState.collectAsState()
  val context = LocalContext.current

  val errorMsg = uiState.errorMsg
  LaunchedEffect(errorMsg) {
    if (errorMsg != null) {
      Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
      settingsViewModel.clearErrorMsg()
    }
  }

  HandleSignedOutState(uiState.signedOut, onSignedOut)

  Scaffold(
      topBar = {
        TopNavigationMenuSettings(
            selectedTab = Tab.Settings,
            onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
            modifier = Modifier.testTag(NavigationTestTags.TOP_NAVIGATION_MENU),
            onSignedOut = { settingsViewModel.signOut(credentialManager) })
      },
      containerColor = Color(0xFF1A1D1F),
      content = { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
              // Profile Picture
              Image(
                  painter =
                      painterResource(
                          id = R.drawable.ic_launcher_foreground), // currently a placeholder image
                  contentDescription =
                      stringResource(R.string.settings_profile_picture_description),
                  modifier =
                      Modifier.size(120.dp)
                          .clip(CircleShape)
                          .testTag(SettingsScreenTestTags.PROFILE_PICTURE),
                  contentScale = ContentScale.Crop)

              Spacer(modifier = Modifier.height(16.dp))

              // Username
              Text(
                  text = stringResource(R.string.settings_default_username),
                  color = Color.White,
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Medium,
                  modifier = Modifier.testTag(SettingsScreenTestTags.USERNAME))

              Spacer(modifier = Modifier.height(16.dp))

              // Edit Photo Button currently non-functional, will be implemented in next sprint
              Button(
                  onClick = { /* Handle edit photo */},
                  modifier =
                      Modifier.fillMaxWidth()
                          .height(48.dp)
                          .testTag(SettingsScreenTestTags.EDIT_PHOTO_BUTTON),
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D3135)),
                  shape = RoundedCornerShape(12.dp)) {
                    Text(text = "Edit Photo", color = Color.White, fontSize = 16.sp)
                  }

              Spacer(modifier = Modifier.height(32.dp))

              // Settings Fields
              SettingsField(
                  label = stringResource(R.string.settings_label_name),
                  value = uiState.name,
                  onValueChange = { settingsViewModel.editName(it) },
                  modifier = Modifier.testTag(SettingsScreenTestTags.NAME_FIELD))
              Spacer(modifier = Modifier.height(16.dp))
              SettingsField(
                  label = stringResource(R.string.settings_label_birthday),
                  value = uiState.birthday,
                  onValueChange = { settingsViewModel.editBirthday(it) },
                  modifier = Modifier.testTag(SettingsScreenTestTags.BIRTHDAY_FIELD))
              Spacer(modifier = Modifier.height(16.dp))
              SettingsField(
                  label = stringResource(R.string.settings_label_school),
                  value = uiState.school,
                  onValueChange = { settingsViewModel.editSchool(it) },
                  modifier = Modifier.testTag(SettingsScreenTestTags.SCHOOL_FIELD))
              Spacer(modifier = Modifier.height(16.dp))
              SettingsField(
                  label = stringResource(R.string.settings_label_school_year),
                  value = uiState.schoolYear,
                  onValueChange = { settingsViewModel.editSchoolYear(it) },
                  modifier = Modifier.testTag(SettingsScreenTestTags.SCHOOL_YEAR_FIELD))

              Spacer(modifier = Modifier.height(32.dp))

              // Save Button
              Button(
                  onClick = {
                    currentUser?.uid?.let { uid -> settingsViewModel.updateProfile(uid) }
                  },
                  modifier =
                      Modifier.fillMaxWidth(0.8f) // 80% width, you can tweak this (0.7f, 0.9f…)
                          .height(48.dp)
                          .padding(bottom = 16.dp)
                          .testTag(SettingsScreenTestTags.SAVE_BUTTON),
                  colors =
                      ButtonDefaults.buttonColors(
                          containerColor = Color.White,
                          contentColor = MaterialTheme.colorScheme.primary),
                  enabled = uiState.isValid) {
                    Text(
                        text = stringResource(R.string.settings_save),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium)
                  }
              Spacer(modifier = Modifier.weight(1f))
            }
      })
}

@Composable
fun SettingsField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Text(
        text = label,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.padding(bottom = 8.dp))

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF2D3135),
                unfocusedContainerColor = Color(0xFF2D3135),
                disabledContainerColor = Color(0xFF2D3135),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color.White,
                focusedBorderColor = Color(0xFF4A4D52),
                unfocusedBorderColor = Color.Transparent),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp))
  }
}

@Composable
fun SettingsScreenPreviewContent() {
  // Fake UI state for preview
  val fakeUiState =
      object {
        val name = "Gersende Kerjan"
        val birthday = "28/07/2005"
        val school = "EPFL"
        val schoolYear = "IN BA5"
        val isValid = true
        val errorMsg: String? = null
        val signedOut = false
      }

  Scaffold(
      topBar = {
        TopNavigationMenuSettings(
            selectedTab = Tab.Settings,
            onTabSelected = {},
            modifier = Modifier.testTag(NavigationTestTags.TOP_NAVIGATION_MENU),
            onSignedOut = {})
      },
      containerColor = Color(0xFF1A1D1F),
      content = { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
              Image(
                  painter = painterResource(id = R.drawable.ic_launcher_foreground),
                  contentDescription =
                      stringResource(R.string.settings_profile_picture_description),
                  modifier = Modifier.size(120.dp).clip(CircleShape),
                  contentScale = ContentScale.Crop)

              Spacer(modifier = Modifier.height(16.dp))

              Text(
                  text = stringResource(R.string.settings_default_username),
                  color = Color.White,
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Medium)

              Spacer(modifier = Modifier.height(16.dp))

              Button(
                  onClick = {},
                  modifier = Modifier.fillMaxWidth().height(48.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D3135)),
                  shape = RoundedCornerShape(12.dp)) {
                    Text(
                        text = stringResource(R.string.settings_edit_photo),
                        color = Color.White,
                        fontSize = 16.sp)
                  }

              Spacer(modifier = Modifier.height(32.dp))

              SettingsField(
                  label = stringResource(R.string.settings_label_name),
                  value = fakeUiState.name,
                  onValueChange = {})
              Spacer(modifier = Modifier.height(16.dp))
              SettingsField(
                  label = stringResource(R.string.settings_label_birthday),
                  value = fakeUiState.birthday,
                  onValueChange = {})
              Spacer(modifier = Modifier.height(16.dp))
              SettingsField(
                  label = stringResource(R.string.settings_label_school),
                  value = fakeUiState.school,
                  onValueChange = {})
              Spacer(modifier = Modifier.height(16.dp))
              SettingsField(
                  label = stringResource(R.string.settings_label_school_year),
                  value = fakeUiState.schoolYear,
                  onValueChange = {})

              Spacer(modifier = Modifier.height(32.dp))

              Button(
                  onClick = {},
                  modifier = Modifier.fillMaxWidth(0.8f).height(48.dp).padding(bottom = 16.dp),
                  colors =
                      ButtonDefaults.buttonColors(
                          containerColor = Color.White,
                          contentColor = MaterialTheme.colorScheme.primary),
                  enabled = fakeUiState.isValid) {
                    Text(
                        text = stringResource(R.string.settings_save),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium)
                  }

              Spacer(modifier = Modifier.weight(1f))
            }
      })
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
  SettingsScreenPreviewContent()
}
