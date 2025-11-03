package com.android.gatherly.ui.authentication

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.R
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.ui.theme.GatherlyTheme

object SignInScreenTestTags {
  const val WELCOME_TITLE = "welcomeTitle"
  const val WELCOME_SUBTITLE = "welcomeSubtitle"
  const val GOOGLE_BUTTON = "googleButton"
  const val ANONYMOUS_BUTTON = "anonymousButton"
}

/**
 * Main sign-in screen providing various authentication options (Google, anonymous...).
 *
 * @param authViewModel ViewModel managing authentication state and logic.
 * @param credentialManager Used for Google Credential authentication.
 * @param onSignedIn Callback invoked after a successful sign-in.
 */
@Composable
fun SignInScreen(
    authViewModel: SignInViewModel = viewModel(),
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current),
    onSignedIn: () -> Unit = {},
) {

  val context = LocalContext.current
  val isSignedIn by authViewModel.uiState.collectAsState()

  // Navigate to home page screen on successful login
  LaunchedEffect(isSignedIn) {
    if (isSignedIn) {
      onSignedIn()
    }
  }

  Scaffold(
      containerColor = MaterialTheme.colorScheme.background,
      modifier = Modifier.fillMaxSize(),
      content = { innerPadding ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_screen)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween) {
              Spacer(
                  modifier = Modifier.height(dimensionResource(id = R.dimen.sign_in_top_spacing)))

              Column(
                  modifier = Modifier.weight(1f),
                  horizontalAlignment = Alignment.CenterHorizontally) {
                    WelcomeSection()
                    Spacer(
                        modifier =
                            Modifier.height(
                                dimensionResource(id = R.dimen.sign_in_top_buttons_spacing)))

                    // Sign In Buttons Section
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement =
                            Arrangement.spacedBy(
                                dimensionResource(id = R.dimen.spacing_between_fields_regular))) {
                          SignInButton(
                              text = stringResource(id = R.string.sign_in_google_button_label),
                              onSignInClick = {
                                authViewModel.signInWithGoogle(context, credentialManager)
                              },
                              iconResId = R.drawable.google_logo,
                              modifier = Modifier.testTag(SignInScreenTestTags.GOOGLE_BUTTON))
                          SignInButton(
                              text = stringResource(id = R.string.sign_in_anonymous_button_label),
                              onSignInClick = { authViewModel.signInAnonymously() },
                              modifier = Modifier.testTag(SignInScreenTestTags.ANONYMOUS_BUTTON))
                        }
                  }
            }
      })
}

/**
 * Displays the welcome section of the sign-in screen.
 *
 * This section shows a main title and a subtitle that greet the user when they open the app.
 */
@Composable
fun WelcomeSection() {

  Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
    Text(
        text = stringResource(id = R.string.sign_in_welcome_section_text0),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth().testTag(SignInScreenTestTags.WELCOME_TITLE))

    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.sign_in_welcome_text_spacing)))

    Text(
        text = stringResource(id = R.string.sign_in_welcome_section_text1),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.fillMaxWidth().testTag(SignInScreenTestTags.WELCOME_SUBTITLE))
  }
}

/**
 * A reusable button used for sign-in actions such as Google or anonymous authentication.
 *
 * The button supports an optional leading icon, customizable text, and an [onSignInClick] action
 * callback. It uses the app's color scheme and rounded corners for consistent styling.
 *
 * @param text The label displayed on the button.
 * @param onSignInClick Callback triggered when the button is clicked.
 * @param modifier Optional [Modifier] for layout and styling customization.
 * @param iconResId Optional drawable resource ID for an icon displayed to the left of the text.
 */
@Composable
fun SignInButton(
    text: String,
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconResId: Int? = null
) {
  Button(
      onClick = onSignInClick,
      modifier =
          modifier.fillMaxWidth().height(dimensionResource(id = R.dimen.sign_in_button_height)),
      colors =
          ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.onSurface,
              contentColor = MaterialTheme.colorScheme.primary),
      shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_shape_medium))) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()) {
              iconResId?.let { id ->
                Image(
                    painter = painterResource(id = id),
                    contentDescription = null, // Action still clear with button text description
                    modifier =
                        Modifier.size(dimensionResource(id = R.dimen.sign_in_button_icon_size))
                            .padding(end = dimensionResource(R.dimen.padding_small)))
              }
              Text(
                  text = text,
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.Medium)
            }
      }
}

@Preview(showBackground = true)
@Composable
fun SignInScreenPreview() {
  GatherlyTheme(darkTheme = true) {
    val fakeViewMod = SignInViewModel(ProfileLocalRepository())
    SignInScreen(fakeViewMod)
  }
}
