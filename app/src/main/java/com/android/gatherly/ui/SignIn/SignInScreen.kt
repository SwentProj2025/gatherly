package com.android.gatherly.ui.SignIn

import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.ui.navigation.NavigationActions

object SignInScreenTestTags {
  const val SignInText = "SignIN"
}

@Composable
fun SignInScreenxx(
    signInViewModel: SignInViewModel = viewModel(),
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current),
    onSignedIn: () -> Unit = {},
    navigationActions: NavigationActions? = null,
) {

  val context = LocalContext.current
  val uiState by signInViewModel.uiState.collectAsState()

  LaunchedEffect(uiState.signedIn) {
    if (uiState.signedIn) {
      onSignedIn()
      Toast.makeText(context, "LogIn successful", Toast.LENGTH_SHORT).show()
    }
  }

  Scaffold(
      content = { padding ->
        Text(
            text = "SignIN page",
            modifier = Modifier.padding(padding).testTag(SignInScreenTestTags.SignInText))
      })
}
