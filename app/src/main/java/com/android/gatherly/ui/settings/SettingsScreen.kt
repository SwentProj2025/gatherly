package com.android.gatherly.ui.settings

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
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
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.android.gatherly.R
import com.android.gatherly.ui.navigation.*
import java.io.File

// Technical constants
private const val MIME_TYPE_IMAGE = "image/*"

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
  const val PHOTO_PICKER_CAMERA_BUTTON = "settings_photo_picker_camera_button"
  const val PHOTO_PICKER_GALLERY_BUTTON = "settings_photo_picker_gallery_button"
  const val PHOTO_PICKER_CANCEL_BUTTON = "settings_photo_picker_cancel_button"
  const val PROFILE_PICTURE_URL_NOT_EMPTY = "settings_profile_picture_url_not_empty"
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

  var showPhotoPickerDialog by remember { mutableStateOf(false) }

  val imageFile = remember { File(context.filesDir, SettingsViewModel.PROFILE_PIC_FILENAME) }

  val imageUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile)

  // launcher to pick from gallery
  val pickImageLauncher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri?
        ->
        uri?.let { settingsViewModel.onGalleryImagePicked(context, it, imageFile) }
      }

  // launcher to take a photo with the camera
  val takePhotoLauncher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) { success
        ->
        if (success) {
          settingsViewModel.editPhoto("${imageFile.toURI()}?t=${System.currentTimeMillis()}")
        }
      }

  val errorMsg = uiState.errorMsg
  LaunchedEffect(errorMsg) {
    if (errorMsg != null) {
      Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
      settingsViewModel.clearErrorMsg()
    }
  }

  val saveSuccess = uiState.saveSuccess
  LaunchedEffect(saveSuccess) {
    if (saveSuccess) {
      Toast.makeText(context, "Profile saved successfully!", Toast.LENGTH_SHORT).show()
      settingsViewModel.clearSaveSuccess()
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
      containerColor = MaterialTheme.colorScheme.background,
      content = { paddingValues ->
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
                    ProfilePictureImage(uiState.profilePictureUrl)

                    Spacer(modifier = Modifier.height(fieldSpacingRegular))

                    // Username Field
                    SettingsField(
                        label = stringResource(R.string.settings_label_username),
                        value = uiState.username,
                        onValueChange = { settingsViewModel.editUsername(it) },
                        testTag = SettingsScreenTestTags.USERNAME,
                        errorMessage = uiState.invalidUsernameMsg)

                    if (uiState.isUsernameAvailable == true && uiState.invalidUsernameMsg == null) {
                      Text(
                          text = stringResource(R.string.settings_valid_username),
                          color = MaterialTheme.colorScheme.primary,
                          fontSize = 14.sp,
                          modifier =
                              Modifier.padding(
                                  top = dimensionResource(id = R.dimen.padding_extra_small)))
                    }

                    Spacer(modifier = Modifier.height(fieldSpacingRegular))

                    // Edit Photo Button
                    Button(
                        onClick = { showPhotoPickerDialog = true },
                        modifier =
                            Modifier.fillMaxWidth()
                                .height(dimensionResource(id = R.dimen.settings_text_field_height))
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

                    if (showPhotoPickerDialog) {
                      PhotoPickerDialog(
                          imageUri = imageUri,
                          takePhotoLauncher = takePhotoLauncher,
                          pickImageLauncher = pickImageLauncher) {
                            showPhotoPickerDialog = false
                          }
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
                  enabled = uiState.isValid) {
                    Text(
                        text = stringResource(R.string.settings_save),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium)
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

/**
 * Displays a circular profile picture.
 * - If [pictureUrl] is non-empty, loads the image from the URL using Coil.
 * - If [pictureUrl] is empty, shows a default placeholder image.
 * - Adds a test tag for UI testing:
 *     - [SettingsScreenTestTags.PROFILE_PICTURE_URL_NOT_EMPTY] if URL is provided
 *     - [SettingsScreenTestTags.PROFILE_PICTURE] otherwise
 *
 * @param pictureUrl The URL of the profile picture. Can be empty to show a placeholder.
 */
@Composable
fun ProfilePictureImage(pictureUrl: String) {
  val painter =
      if (pictureUrl.isNotEmpty()) {
        rememberAsyncImagePainter(pictureUrl)
      } else {
        painterResource(R.drawable.ic_launcher_foreground)
      }

  Image(
      painter = painter,
      contentDescription = stringResource(R.string.settings_profile_picture_description),
      modifier =
          Modifier.size(dimensionResource(id = R.dimen.profile_pic_size))
              .clip(CircleShape)
              .testTag(
                  if (pictureUrl.isNotEmpty()) SettingsScreenTestTags.PROFILE_PICTURE_URL_NOT_EMPTY
                  else SettingsScreenTestTags.PROFILE_PICTURE),
      contentScale = ContentScale.Crop)
}

/**
 * Internal content of the [PhotoPickerDialog] showing the camera and gallery buttons.
 *
 * @param imageUri The Uri where a new photo will be saved when using the camera option.
 * @param takePhotoLauncher Launcher for taking a new photo with the camera.
 * @param pickImageLauncher Launcher for picking an image from the gallery.
 * @param onDismiss Lambda called when a button is clicked or the dialog should close.
 */
@Composable
private fun PhotoPickerDialogContent(
    imageUri: Uri,
    takePhotoLauncher: ActivityResultLauncher<Uri>,
    pickImageLauncher: ActivityResultLauncher<String>,
    onDismiss: () -> Unit
) {
  Column {
    TextButton(
        onClick = {
          takePhotoLauncher.launch(imageUri)
          onDismiss()
        },
        modifier = Modifier.testTag(SettingsScreenTestTags.PHOTO_PICKER_CAMERA_BUTTON)) {
          Text(
              color = MaterialTheme.colorScheme.primary,
              text = stringResource(id = R.string.settings_alert_dialog_option_camera))
        }

    TextButton(
        onClick = {
          pickImageLauncher.launch(MIME_TYPE_IMAGE)
          onDismiss()
        },
        modifier = Modifier.testTag(SettingsScreenTestTags.PHOTO_PICKER_GALLERY_BUTTON)) {
          Text(
              color = MaterialTheme.colorScheme.primary,
              text = stringResource(id = R.string.settings_alert_dialog_option_gallery))
        }
  }
}

/**
 * Displays a photo picker AlertDialog
 *
 * The dialog allows the user to either take a new photo or pick an existing image from the gallery.
 * It also provides a cancel button to dismiss the dialog.
 *
 * @param imageUri The Uri where a new photo will be saved when using the camera option.
 * @param takePhotoLauncher Launcher for taking a new photo with the camera.
 * @param pickImageLauncher Launcher for picking an image from the gallery.
 * @param onDismiss Lambda called when the dialog is dismissed.
 */
@Composable
fun PhotoPickerDialog(
    imageUri: Uri,
    takePhotoLauncher: ActivityResultLauncher<Uri>,
    pickImageLauncher: ActivityResultLauncher<String>,
    onDismiss: () -> Unit
) {

  AlertDialog(
      onDismissRequest = onDismiss,
      title = {
        Text(
            color = MaterialTheme.colorScheme.primary,
            text = stringResource(id = R.string.settings_alert_dialog_title))
      },
      text = {
        Text(
            color = MaterialTheme.colorScheme.primary,
            text = stringResource(id = R.string.settings_alert_dialog_text))
      },
      confirmButton = {
        PhotoPickerDialogContent(imageUri, takePhotoLauncher, pickImageLauncher, onDismiss)
      },
      dismissButton = {
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.testTag(SettingsScreenTestTags.PHOTO_PICKER_CANCEL_BUTTON)) {
              Text(stringResource(id = R.string.settings_alert_dialog_option_cancel))
            }
      })
}
