package com.android.gatherly.ui.friends

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu_Goback

object FriendsScreenTestTags {
  const val FriendsText = "friends"
}

@Composable
fun FriendsScreen(
    friendsViewModel: FriendsViewModel = viewModel(),
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current),
    goBack: () -> Unit,
) {

  Scaffold(
      topBar = {
        TopNavigationMenu_Goback(
            selectedTab = Tab.Friends,
            modifier = Modifier.testTag(NavigationTestTags.TOP_NAVIGATION_MENU),
            goBack = goBack)
      },
      content = { padding ->
        Text(
            text = "Friends accounts page",
            modifier = Modifier.padding(padding).testTag(FriendsScreenTestTags.FriendsText))
      })
}
