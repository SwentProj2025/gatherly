package com.android.gatherly.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.R
import com.android.gatherly.ui.navigation.BottomNavigationMenu
import com.android.gatherly.ui.navigation.HandleSignedOutState
import com.android.gatherly.ui.navigation.NavigationActions
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu_Profile

/** Contains test tags used for UI testing on the Profile screen. */
object ProfileScreenTestTags {
  const val PROFILE_PICTURE = "profilePicture"
  const val PROFILE_NAME = "profileName"
  const val PROFILE_USERNAME = "profileUsername"
  const val PROFILE_SCHOOL = "profileSchool"
  const val PROFILE_FRIENDS_COUNT = "profileFriendsCount"
  const val PROFILE_FOCUS_POINTS_COUNT = "profileFocusPointsCount"
  const val PROFILE_FOCUS_SESSIONS = "profileFocusSessions"
  const val PROFILE_GROUPS = "profileGroups"
}

/**
 * Composable function that represents the Profile screen of the app.
 *
 * @param profileViewModel The ViewModel that provides the data for the Profile screen.
 * @param credentialManager The CredentialManager instance for handling authentication credentials.
 * @param onSignedOut Callback function to be invoked when the user signs out.
 * @param navigationActions Navigation actions for navigating between different screens.
 */
@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = viewModel(),
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current),
    onSignedOut: () -> Unit = {},
    navigationActions: NavigationActions? = null,
) {
  val uiState by profileViewModel.uiState.collectAsState()
  val profile = uiState.profile

  // Fetch profile when the screen is recomposed
  LaunchedEffect(Unit) { profileViewModel.loadProfile(profile?.username ?: "") }

  val paddingRegular = dimensionResource(id = R.dimen.padding_regular)
  val paddingMedium = dimensionResource(id = R.dimen.padding_medium)
  val fieldSpacingSmall = dimensionResource(id = R.dimen.spacing_between_fields)
  val fieldSpacingRegular = dimensionResource(id = R.dimen.spacing_between_fields_regular)
  val fieldSpacingMedium = dimensionResource(id = R.dimen.spacing_between_fields_medium)
  val fieldSpacingLarge = dimensionResource(id = R.dimen.spacing_between_fields_large)
  val profilePictureSize = dimensionResource(id = R.dimen.profile_pic_size)
  val profilePictureBorder = dimensionResource(id = R.dimen.profile_pic_border)

  HandleSignedOutState(uiState.signedOut, onSignedOut)
  Scaffold(
      topBar = {
        TopNavigationMenu_Profile(
            selectedTab = Tab.Profile,
            onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
            modifier = Modifier.testTag(NavigationTestTags.TOP_NAVIGATION_MENU),
            onSignedOut = { profileViewModel.signOut(credentialManager) })
      },
      bottomBar = {
        BottomNavigationMenu(
            selectedTab = Tab.Profile,
            onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
            modifier = Modifier.testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU))
      },
      content = { padding ->
        if (uiState.isLoading) {
          Box(
              modifier = Modifier.fillMaxSize().padding(padding),
              contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
              }
        } else {
          Column(
              modifier =
                  Modifier.fillMaxSize()
                      .verticalScroll(rememberScrollState())
                      .padding(padding)
                      .padding(horizontal = paddingRegular, vertical = paddingMedium),
              horizontalAlignment = Alignment.CenterHorizontally) {
                // Profile Picture
                Image(
                    painter = painterResource(id = R.drawable.default_profile_picture),
                    contentDescription = stringResource(R.string.profile_picture_description),
                    modifier =
                        Modifier.size(profilePictureSize)
                            .clip(CircleShape)
                            .border(
                                profilePictureBorder,
                                MaterialTheme.colorScheme.outline,
                                CircleShape)
                            .testTag(ProfileScreenTestTags.PROFILE_PICTURE),
                    contentScale = ContentScale.Crop)

                Spacer(modifier = Modifier.height(fieldSpacingRegular))

                // Name
                Text(
                    text = profile?.name ?: stringResource(R.string.profile_default_name),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag(ProfileScreenTestTags.PROFILE_NAME))

                Spacer(modifier = Modifier.height(fieldSpacingRegular))

                // Username
                Text(
                    text =
                        "@" +
                            (profile?.username
                                ?: stringResource(R.string.profile_default_username)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.testTag(ProfileScreenTestTags.PROFILE_USERNAME))

                Spacer(modifier = Modifier.height(fieldSpacingSmall))

                // School + School year
                Text(
                    text =
                        (profile?.school ?: stringResource(R.string.profile_default_school)) +
                            " " +
                            stringResource(R.string.profile_separator_school_school_year) +
                            " " +
                            (profile?.schoolYear
                                ?: stringResource(R.string.profile_default_school_year)),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.testTag(ProfileScreenTestTags.PROFILE_SCHOOL))

                Spacer(modifier = Modifier.height(fieldSpacingMedium))

                // Friends and Focus points
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly) {
                      Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = (profile?.friendUids?.size ?: 0).toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier =
                                Modifier.testTag(ProfileScreenTestTags.PROFILE_FRIENDS_COUNT))
                        Text(
                            stringResource(R.string.profile_friends_label),
                            style = MaterialTheme.typography.bodySmall)
                      }
                      Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.focusPoints.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier =
                                Modifier.testTag(ProfileScreenTestTags.PROFILE_FOCUS_POINTS_COUNT))
                        Text(
                            stringResource(R.string.profile_focus_points_label),
                            style = MaterialTheme.typography.bodySmall)
                      }
                    }

                Spacer(modifier = Modifier.height(fieldSpacingLarge))

                // Focus Sessions
                Text(
                    text = stringResource(R.string.profile_focus_sessions_section_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier.fillMaxWidth()
                            .testTag(ProfileScreenTestTags.PROFILE_FOCUS_SESSIONS))
                Spacer(modifier = Modifier.height(fieldSpacingSmall))
                Text(
                    text = stringResource(R.string.profile_empty_focus_sessions_message),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center)

                Spacer(modifier = Modifier.height(fieldSpacingLarge))

                // Groups
                Text(
                    text = stringResource(R.string.profile_groups_section_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier.fillMaxWidth().testTag(ProfileScreenTestTags.PROFILE_GROUPS))
                Spacer(modifier = Modifier.height(fieldSpacingSmall))
                Text(
                    text = stringResource(R.string.profile_empty_groups_message),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center)
              }
        }
      })
}
