package com.android.gatherly.ui.events

import androidx.compose.runtime.Composable
import com.android.gatherly.ui.focusTimer.FocusTimerViewModel
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.ui.navigation.BottomNavigationMenu
import com.android.gatherly.ui.navigation.NavigationActions
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu

object EventsScreenTestTags{
}

@Composable
fun EventsScreen(
    eventsViewModel: EventsViewModel = viewModel(),
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current),
    onSignedOut: () -> Unit = {},
    navigationActions: NavigationActions? = null,
) {

    val context = LocalContext.current
    val uiState by eventsViewModel.uiState.collectAsState()

    LaunchedEffect(uiState.signedOut) {
        if (uiState.signedOut) {
            onSignedOut()
            Toast.makeText(context, "Logout successful", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {TopNavigationMenu(
            selectedTab = Tab.Events,
            onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
            modifier = Modifier.testTag(NavigationTestTags.TOP_NAVIGATION_MENU),
            onSignedOut =  {eventsViewModel.signOut(credentialManager)}

        )},

        bottomBar = {
            BottomNavigationMenu(
                selectedTab = Tab.Events,
                onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
                modifier = Modifier.testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU)
            )},
        content = { padding ->
            Text(
                text = "Events page",
                modifier = Modifier.padding(padding)
            )
        }
    )
}
