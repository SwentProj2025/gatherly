package com.android.gatherly.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.R
import com.android.gatherly.ui.navigation.*
import com.android.gatherly.ui.theme.GatherlyTheme

object SettingsScreenTestTags {
  const val PROFILE_PICTURE = "settings_profile_picture"
  const val USERNAME = "settings_username"
  const val EDIT_PHOTO_BUTTON = "settings_edit_photo_button"
  const val NAME_FIELD = "settings_name_field"
  const val BIRTHDAY_FIELD = "settings_birthday_field"
  const val SCHOOL_FIELD = "settings_school_field"
  const val SCHOOL_YEAR_FIELD = "settings_school_year_field"
  const val SAVE_BUTTON = "settings_save_button"
  const val USERNAME_ERROR = "settings_username_error"
  const val NAME_FIELD_ERROR = "settings_name_field_error"
  const val BIRTHDAY_FIELD_ERROR = "settings_birthday_field_error"
  const val GOOGLE_BUTTON = "google_button"
  const val LOADING = "loading"
  const val DELETE_BTN = "deleteButton"
  const val DELETE_POP_UP = "deletePopUp"
  const val SNACKBAR = "snackbar"
}

/**
 * Settings screen composable, where users can view and edit their profile information.
 *
 * This screen allows editing of basic user details (e.g name, school).
 *
 * @param credentialManager Used for managing sign-out actions.
 * @param onSignedOut Callback triggered when the user signs out.
 * @param navigationActions Handles navigation between different app sections.
 */
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = viewModel(),
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current),
    onSignedOut: () -> Unit = {},
    navigationActions: NavigationActions? = null,
) {
  val paddingRegular = dimensionResource(id = R.dimen.padding_regular)
  val fieldSpacingRegular = dimensionResource(id = R.dimen.spacing_between_fields_regular)
  val paddingMedium = dimensionResource(id = R.dimen.padding_medium)
  val fieldSpacingMedium = dimensionResource(id = R.dimen.spacing_between_fields_medium)

  val uiState by settingsViewModel.uiState.collectAsState()
  val context = LocalContext.current
  val shouldShowDialog = remember { mutableStateOf(false) }
  val snackBarHostState = remember { SnackbarHostState() }

  val errorMsg = uiState.errorMsg
  LaunchedEffect(errorMsg) {
    if (errorMsg != null) {
      snackBarHostState.showSnackbar(message = errorMsg, withDismissAction = true)
      settingsViewModel.clearErrorMsg()
    }
  }

  // If the anonymous user decides to upgrade their account to a signed in one, navigate to the init
  // profile screen
  val navigateToInit = uiState.navigateToInit
  LaunchedEffect(navigateToInit) {
    if (navigateToInit) {
      navigationActions?.navigateTo(Screen.InitProfileScreen)
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
      snackbarHost = {
        SnackbarHost(
            hostState = snackBarHostState,
            modifier = Modifier.testTag(SettingsScreenTestTags.SNACKBAR))
      },
      containerColor = MaterialTheme.colorScheme.background,
      content = { paddingValues ->
        if (uiState.isLoadingProfile) {
          Box(
              modifier = Modifier.fillMaxSize().padding(paddingValues),
              contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.testTag(SettingsScreenTestTags.LOADING))
              }
        } else if (uiState.isAnon) {

          // If the user is anonymous, they do not have a profile

          Box(
              modifier =
                  Modifier.fillMaxSize()
                      .padding(paddingValues)
                      .padding(horizontal = fieldSpacingMedium),
              contentAlignment = Alignment.Center) {
                Column {
                  // Inform the user that they are signed in anonymously
                  Text(
                      text = stringResource(R.string.profile_anon_message),
                      color = MaterialTheme.colorScheme.onBackground,
                      textAlign = TextAlign.Center)

                  Spacer(Modifier.height(fieldSpacingMedium))

                  // Sign in with google button
                  Button(
                      onClick = { settingsViewModel.upgradeWithGoogle(context, credentialManager) },
                      modifier =
                          Modifier.fillMaxWidth()
                              .height(dimensionResource(id = R.dimen.sign_in_button_height))
                              .testTag(SettingsScreenTestTags.GOOGLE_BUTTON),
                      colors =
                          ButtonDefaults.buttonColors(
                              containerColor = MaterialTheme.colorScheme.surfaceVariant,
                              contentColor = MaterialTheme.colorScheme.primary),
                      shape =
                          RoundedCornerShape(
                              dimensionResource(id = R.dimen.rounded_corner_shape_medium))) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()) {
                              // Google image
                              Image(
                                  painter = painterResource(id = R.drawable.google_logo),
                                  contentDescription =
                                      null, // Action still clear with button text description
                                  modifier =
                                      Modifier.size(
                                              dimensionResource(
                                                  id = R.dimen.sign_in_button_icon_size))
                                          .padding(end = dimensionResource(R.dimen.padding_small)))

                              // Upgrade with google text
                              Text(
                                  text = stringResource(R.string.upgrade_to_google_button_label),
                                  style = MaterialTheme.typography.bodyLarge,
                                  fontWeight = FontWeight.Medium)
                            }
                      }
                }
              }
        } else {
          Column(
              modifier =
                  Modifier.fillMaxSize()
                      .padding(paddingValues)
                      .padding(horizontal = paddingRegular, vertical = paddingMedium),
              horizontalAlignment = Alignment.CenterHorizontally) {
                Column(
                    modifier =
                        Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                      // Profile Picture
                      Image(
                          painter =
                              painterResource(
                                  id =
                                      R.drawable
                                          .ic_launcher_foreground), // currently a placeholder image
                          contentDescription =
                              stringResource(R.string.settings_profile_picture_description),
                          modifier =
                              Modifier.size(dimensionResource(id = R.dimen.profile_pic_size))
                                  .clip(CircleShape)
                                  .testTag(SettingsScreenTestTags.PROFILE_PICTURE),
                          contentScale = ContentScale.Crop)

                      Spacer(modifier = Modifier.height(fieldSpacingRegular))

                      // Username Field
                      SettingsField(
                          label = stringResource(R.string.settings_label_username),
                          value = uiState.username,
                          onValueChange = { settingsViewModel.editUsername(it) },
                          testTag = SettingsScreenTestTags.USERNAME,
                          errorMessage = uiState.invalidUsernameMsg)

                      if (uiState.isUsernameAvailable == true &&
                          uiState.invalidUsernameMsg == null) {
                        Text(
                            text = stringResource(R.string.settings_valid_username),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp,
                            modifier =
                                Modifier.padding(
                                    top = dimensionResource(id = R.dimen.padding_extra_small)))
                      }

                      Spacer(modifier = Modifier.height(fieldSpacingRegular))

                      // Edit Photo Button currently non-functional, will be implemented in next
                      // sprint
                      Button(
                          onClick = { /* Handle edit photo will be handled in next sprint*/},
                          modifier =
                              Modifier.fillMaxWidth()
                                  .height(
                                      dimensionResource(id = R.dimen.settings_text_field_height))
                                  .testTag(SettingsScreenTestTags.EDIT_PHOTO_BUTTON),
                          colors =
                              ButtonDefaults.buttonColors(
                                  containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                  contentColor = MaterialTheme.colorScheme.onSurface),
                          shape =
                              RoundedCornerShape(
                                  dimensionResource(id = R.dimen.rounded_corner_shape_medium))) {
                            Text(
                                text = stringResource(id = R.string.settings_edit_photo),
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 16.sp)
                          }

                      Spacer(modifier = Modifier.height(fieldSpacingMedium))

                      // Settings Fields
                      SettingsField(
                          label = stringResource(R.string.settings_label_name),
                          value = uiState.name,
                          onValueChange = { settingsViewModel.editName(it) },
                          testTag = SettingsScreenTestTags.NAME_FIELD,
                          errorMessage = uiState.invalidNameMsg)
                      Spacer(modifier = Modifier.height(fieldSpacingRegular))
                      SettingsField(
                          label = stringResource(R.string.settings_label_birthday),
                          value = uiState.birthday,
                          onValueChange = { settingsViewModel.editBirthday(it) },
                          testTag = SettingsScreenTestTags.BIRTHDAY_FIELD,
                          errorMessage = uiState.invalidBirthdayMsg)
                      Spacer(modifier = Modifier.height(fieldSpacingRegular))
                      SettingsField(
                          label = stringResource(R.string.settings_label_school),
                          value = uiState.school,
                          onValueChange = { settingsViewModel.editSchool(it) },
                          testTag = SettingsScreenTestTags.SCHOOL_FIELD)
                      Spacer(modifier = Modifier.height(fieldSpacingRegular))
                      SettingsField(
                          label = stringResource(R.string.settings_label_school_year),
                          value = uiState.schoolYear,
                          onValueChange = { settingsViewModel.editSchoolYear(it) },
                          testTag = SettingsScreenTestTags.SCHOOL_YEAR_FIELD)
                    }

                Spacer(modifier = Modifier.height(fieldSpacingMedium))

                // Save Button
                Button(
                    onClick = { settingsViewModel.updateProfile(isFirstTime = false) },
                    modifier =
                        Modifier.fillMaxWidth(0.8f)
                            .height(dimensionResource(id = R.dimen.settings_save_button_height))
                            .padding(bottom = paddingRegular)
                            .testTag(SettingsScreenTestTags.SAVE_BUTTON),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary),
                    enabled = uiState.isValid && !uiState.isSaving) {
                      Text(
                          text =
                              if (uiState.isSaving) {
                                stringResource(R.string.saving)
                              } else {
                                stringResource(R.string.settings_save)
                              },
                          fontSize = 16.sp,
                          fontWeight = FontWeight.Medium)
                    }

                // Delete account
                TextButton(
                    onClick = { shouldShowDialog.value = true },
                    modifier = Modifier.fillMaxWidth().testTag(SettingsScreenTestTags.DELETE_BTN),
                    colors =
                        ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error)) {
                      Icon(
                          imageVector = Icons.Filled.DeleteForever,
                          contentDescription = "",
                          tint = MaterialTheme.colorScheme.error)
                      Text(
                          stringResource(R.string.todos_delete_button_text),
                          color = MaterialTheme.colorScheme.error)
                    }
              }
          if (shouldShowDialog.value) {
            DeletePopUp(viewModel = settingsViewModel, shouldShowDialog = shouldShowDialog)
          }
        }
      })
}

/**
 * A reusable input field composable used within the Settings screen for editing profile details.
 *
 * @param label The text label shown above the input field.
 * @param value The current text value of the field.
 * @param onValueChange Callback triggered when the field’s value changes.
 * @param modifier Modifier for styling or layout customization.
 * @param testTag Tag used for UI testing.
 * @param errorMessage Optional error message displayed below the field.
 */
@Composable
fun SettingsField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "",
    errorMessage: String? = null,
) {

  val fieldsTextColor = MaterialTheme.colorScheme.onBackground
  Column(modifier = modifier.fillMaxWidth()) {
    Text(
        text = label,
        color = fieldsTextColor,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_small)))

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().testTag(testTag),
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedTextColor = fieldsTextColor,
                unfocusedTextColor = fieldsTextColor,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Transparent),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_shape_medium)),
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp))

    // Show error message if not null
    if (!errorMessage.isNullOrEmpty()) {
      Text(
          text = errorMessage,
          color = MaterialTheme.colorScheme.error,
          fontSize = 14.sp,
          modifier =
              Modifier.padding(top = dimensionResource(id = R.dimen.padding_extra_small))
                  .testTag("${testTag}_error"))
    }
  }
}

@Composable
fun DeletePopUp(viewModel: SettingsViewModel, shouldShowDialog: MutableState<Boolean>) {
  AlertDialog(
      containerColor = MaterialTheme.colorScheme.surfaceVariant,
      titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
      textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.testTag(SettingsScreenTestTags.DELETE_POP_UP),
      title = {
        Text(text = stringResource(R.string.settings_delete_warning), textAlign = TextAlign.Center)
      },
      text = {
        Text(
            text = stringResource(R.string.settings_delete_warning_text),
            textAlign = TextAlign.Center,
        )
      },
      dismissButton = {
        Button(
            colors =
                buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
            onClick = { shouldShowDialog.value = false }) {
              Text(
                  text = stringResource(R.string.cancel),
                  color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
      },
      onDismissRequest = { shouldShowDialog.value = false },
      confirmButton = {
        Button(
            colors =
                buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.error),
            onClick = {
              viewModel.deleteProfile()
              shouldShowDialog.value = false
            }) {
              Text(text = stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
            }
      },
  )
}

// Helper function to preview the timer screen
@Preview
@Composable
fun SettingsScreenPreview() {
  GatherlyTheme(darkTheme = true) { SettingsScreen() }
}
