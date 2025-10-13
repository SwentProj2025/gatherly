package com.android.gatherly.ui.homePage

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
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu_HomePage

object HomePageScreenTestTags {
  const val HOMETEXT = "Homepage"
}

@Composable
fun HomePageScreen(
    homePageViewModel: HomePageViewModel = viewModel(),
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current),
    onSignedOut: () -> Unit = {},
    navigationActions: NavigationActions? = null,
) {
  val context = LocalContext.current
  val uiState by homePageViewModel.uiState.collectAsState()

  LaunchedEffect(uiState.signedOut) {
    if (uiState.signedOut) {
      onSignedOut()
      Toast.makeText(context, "Logout successful", Toast.LENGTH_SHORT).show()
    }
  }

  Scaffold(
      topBar = {
        TopNavigationMenu_HomePage(
            selectedTab = Tab.HomePage,
            onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
            onSignedOut = { homePageViewModel.signOut(credentialManager) })
      },

      /*bottomBar = {
         BottomNavigationMenu(
             selectedTab = Tab.HomePage,
             onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
             modifier = Modifier.testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU)
         )},

      */
      content = { padding ->
        Text(
            text = "Home page",
            modifier = Modifier.padding(padding).testTag(HomePageScreenTestTags.HOMETEXT))
      })
}
