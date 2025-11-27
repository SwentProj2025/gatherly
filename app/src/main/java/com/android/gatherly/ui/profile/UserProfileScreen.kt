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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.R
import com.android.gatherly.ui.navigation.BottomNavigationMenu
import com.android.gatherly.ui.navigation.NavigationActions
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu_Profile
import com.android.gatherly.utils.profilePicturePainter

object UserProfileScreenTestTags {
  const val LOADING_INDICATOR = "userProfile_loading"
  const val PROFILE_PICTURE = "userProfile_profilePicture"
  const val USERNAME = "userProfile_username"
  const val NAME = "userProfile_name"
  const val SCHOOL_INFO = "userProfile_schoolInfo"
  const val ERROR_SNACKBAR = "userProfile_errorSnackbar"
  const val EMPTY_STATE = "userProfile_emptyState"
}

@Composable
fun UserProfileScreen(
    uid: String,
    navigationActions: NavigationActions? = null,
    viewModel: UserProfileViewModel = viewModel()
) {

  val snackbarHostState = remember { SnackbarHostState() }
  val uiState by viewModel.uiState.collectAsState()
  val profile = uiState.profile

  LaunchedEffect(uid) { viewModel.loadUserProfile(uid) }

  val paddingRegular = dimensionResource(id = R.dimen.padding_regular)
  val paddingMedium = dimensionResource(id = R.dimen.padding_medium)
  val spacingRegular = dimensionResource(id = R.dimen.spacing_between_fields_regular)
  val spacingSmall = dimensionResource(id = R.dimen.spacing_between_fields)
  val spacingLarge = dimensionResource(id = R.dimen.spacing_between_fields_large)
  val profilePictureSize = dimensionResource(id = R.dimen.profile_pic_size)
  val profilePictureBorder = dimensionResource(id = R.dimen.profile_pic_border)

  Scaffold(
      snackbarHost = {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.testTag(UserProfileScreenTestTags.ERROR_SNACKBAR))
      },
      topBar = {
        TopNavigationMenu_Profile(
            selectedTab = Tab.Profile,
            onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
            modifier = Modifier.testTag(NavigationTestTags.TOP_NAVIGATION_MENU),
            onSignedOut = {})
      },
      bottomBar = {
        BottomNavigationMenu(
            selectedTab = Tab.Profile,
            onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
            modifier = Modifier.testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU))
      },
      content = { padding ->
        uiState.errorMessage?.let { error ->
          LaunchedEffect(error) {
            snackbarHostState.showSnackbar(error)
            viewModel.clearErrorMsg()
          }
        }
        when {
          uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center) {
                  CircularProgressIndicator(
                      Modifier.testTag(UserProfileScreenTestTags.LOADING_INDICATOR))
                }
          }
          profile == null -> {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center) {
                  Text(
                      stringResource(id = R.string.user_profile_no_user),
                      Modifier.testTag(UserProfileScreenTestTags.EMPTY_STATE))
                }
          }
          else -> {
            Column(
                modifier =
                    Modifier.fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(padding)
                        .padding(horizontal = paddingRegular, vertical = paddingMedium),
                horizontalAlignment = Alignment.CenterHorizontally) {
                  Image(
                      painter = profilePicturePainter(profile.profilePicture),
                      contentDescription =
                          stringResource(id = R.string.user_profile_picture_description),
                      modifier =
                          Modifier.testTag(UserProfileScreenTestTags.PROFILE_PICTURE)
                              .size(profilePictureSize)
                              .border(
                                  profilePictureBorder,
                                  MaterialTheme.colorScheme.outline,
                                  CircleShape)
                              .clip(CircleShape),
                      contentScale = ContentScale.Crop)

                  Spacer(Modifier.height(spacingRegular))

                  Text(
                      text = profile.name,
                      style = MaterialTheme.typography.titleLarge,
                      modifier = Modifier.testTag(UserProfileScreenTestTags.NAME))

                  Spacer(Modifier.height(spacingRegular))

                  Text(
                      text = "@${profile.username}",
                      style = MaterialTheme.typography.titleMedium,
                      modifier = Modifier.testTag(UserProfileScreenTestTags.USERNAME))

                  Spacer(Modifier.height(spacingSmall))

                  val schoolInfo =
                      listOfNotNull(
                              profile.school.takeIf { it.isNotBlank() },
                              profile.schoolYear.takeIf { it.isNotBlank() })
                          .joinToString(" • ")

                  if (schoolInfo.isNotEmpty()) {
                    Text(
                        text = schoolInfo,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.testTag(UserProfileScreenTestTags.SCHOOL_INFO))
                  }

                  Spacer(Modifier.height(spacingLarge))

                  Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceEvenly) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                          Text(
                              text = profile.friendUids.size.toString(),
                              style = MaterialTheme.typography.titleMedium)
                          Text(
                              stringResource(id = R.string.user_profile_friends),
                              style = MaterialTheme.typography.bodySmall)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                          // Focus Points count is currently a placeholder, will be changed on next
                          // sprint once focus points are implemented
                          Text(text = "0", style = MaterialTheme.typography.titleMedium)
                          Text(
                              stringResource(id = R.string.user_profile_focus_points),
                              style = MaterialTheme.typography.bodySmall)
                        }
                      }
                }
          }
        }
      })
}
