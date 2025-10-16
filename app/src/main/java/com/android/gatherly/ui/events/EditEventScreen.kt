package com.android.gatherly.ui.events

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu

object EditEventScreenTestTags {
  const val EDITEVENTTEXT = "Event"
}

@Composable
fun EditEventScreen() {
  Scaffold(
      topBar = {
        TopNavigationMenu(
            selectedTab = Tab.EditEvent,
            onTabSelected = { /* Handle tab selection */},
            modifier = Modifier.testTag(NavigationTestTags.TOP_NAVIGATION_MENU),
            onSignedOut = { /* Handle sign out */})
      },
      content = { padding ->
        Text(
            text = "Edit Event Screen",
            modifier = Modifier.padding(padding).testTag(EditEventScreenTestTags.EDITEVENTTEXT))
      })
}
