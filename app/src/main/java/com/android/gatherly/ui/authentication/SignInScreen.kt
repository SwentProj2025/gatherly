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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.R

object SignInScreenTestTags {
  const val LOGIN_TITLE = "loginTitle"
  const val GOOGLE_BUTTON = "googleButton"
  const val ANONYMOUS_BUTTON = "anonymousButton"
  const val SIGN_UP_BUTTON = "signUpButton"
}

// placeholder Color definitions TODO replace by values in res / define a theme
private val DarkBackground = Color(0xFF1A1D23)
private val ButtonBackground = Color(0xFF2D3139)
private val TextWhite = Color(0xFFFFFFFF)

@Composable
fun SignInScreen(
    authViewModel: SignInViewModel = viewModel(),
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current),
    onSignedIn: () -> Unit = {}
) {
  val context = LocalContext.current
  val isSignedIn by authViewModel.uiState.collectAsState()

  // Navigate to overview screen on successful login
  LaunchedEffect(isSignedIn) {
    if (isSignedIn) {
      onSignedIn()
    }
  }

  Scaffold(
      containerColor = DarkBackground,
      modifier = Modifier.fillMaxSize(),
      content = { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween) {
              Spacer(modifier = Modifier.height(80.dp))

              Column(
                  modifier = Modifier.weight(1f),
                  horizontalAlignment = Alignment.CenterHorizontally) {
                    WelcomeSection()
                    Spacer(modifier = Modifier.height(60.dp))

                    // Sign In Buttons Section TODO have a section for that ? what's cleaner ?
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)) {
                          SignInButton(
                              text = "Continue with Google",
                              onSignInClick = {
                                authViewModel.signInWithGoogle(context, credentialManager)
                              },
                              iconResId = R.drawable.google_logo,
                              modifier = Modifier.testTag(SignInScreenTestTags.GOOGLE_BUTTON))
                          SignInButton(
                              text = "Continue without account",
                              onSignInClick = { authViewModel.signInAnonymously() },
                              modifier = Modifier.testTag(SignInScreenTestTags.ANONYMOUS_BUTTON))
                        }
                  }

              // SignUpSection()
              Spacer(modifier = Modifier.height(32.dp))
            }
      })
}

@Composable
fun WelcomeSection() {

  Column(
      horizontalAlignment = Alignment.Start,
      modifier = Modifier.fillMaxWidth().testTag(SignInScreenTestTags.LOGIN_TITLE)) {
        Text(
            text = "Welcome to Gatherly,",
            color = TextWhite,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Your workflow and social events app!",
            color = TextWhite,
            fontSize = 20.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.fillMaxWidth())
      }
}

@Composable
fun SignInButton(
    text: String,
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconResId: Int? = null
) {
  Button(
      onClick = onSignInClick,
      modifier = modifier.fillMaxWidth().height(56.dp),
      colors =
          ButtonDefaults.buttonColors(containerColor = ButtonBackground, contentColor = TextWhite),
      shape = RoundedCornerShape(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()) {
              // Load the Google logo from resources
              iconResId?.let { id ->
                Image(
                    painter = painterResource(id = id),
                    contentDescription = null, // action still clear with button text description
                    modifier = Modifier.size(30.dp).padding(end = 8.dp))
              }
              // Text for the button
              Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
      }
}

@Composable
fun SignUpSection() {
  TextButton(
      onClick = { /* TODO sign up ?*/},
      modifier = Modifier.fillMaxWidth().testTag(SignInScreenTestTags.SIGN_UP_BUTTON)) {
        Text(
            text = "Don't have an account? Sign up",
            color = TextWhite,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium)
      }
}

@Preview(showBackground = true)
@Composable
fun SignInScreenPreview() {
  SignInScreen()
}
