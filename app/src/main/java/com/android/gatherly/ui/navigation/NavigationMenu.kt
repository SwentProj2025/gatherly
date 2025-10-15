package com.android.gatherly.ui.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Diversity1
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.gatherly.R

sealed class Tab(val name: String, val icon: ImageVector, val destination: Screen) {
  object Timer : Tab("Timer", Icons.Outlined.Schedule, Screen.FocusTimerScreen)

  object Overview : Tab("To-Do", Icons.Outlined.FormatListBulleted, Screen.OverviewToDo)

  object Events : Tab("Events", Icons.Outlined.Group, Screen.EventsScreen)

  object Map : Tab("Map", Icons.Outlined.Place, Screen.Map)

  object HomePage : Tab("Home", Icons.Outlined.Home, Screen.HomePage)

  object Profile : Tab("Your profile", Icons.Outlined.AccountCircle, Screen.ProfileScreen)

  object Settings : Tab("Settings", Icons.Outlined.Settings, Screen.SettingsScreen)

  object SignOut : Tab("Sign Out", Icons.Outlined.Person, Screen.SignIn)

  object Friends : Tab("Friends", Icons.Outlined.Diversity1, Screen.FriendsScreen)

  object AddTodo : Tab("Add To-Do", Icons.Outlined.Add, Screen.AddToDo)

  object EditTodo : Tab("Edit To-Do", Icons.Outlined.Edit, Screen.EditTodo)
}

private val bottomtabs =
    listOf(
        Tab.Timer,
        Tab.Overview,
        Tab.Events,
        Tab.Map,
    )

// PART 1: Navigation Bar
/**
 * A bottom navigation menu with tabs for Timer, Overview, Events, and Map.
 *
 * @param selectedTab The currently selected tab.
 * @param onTabSelected A callback function that is invoked when a tab is selected. It takes a [Tab]
 *   as a parameter.
 * @param modifier A [Modifier] for this component. Default is [Modifier]. *
 */
@Composable
fun BottomNavigationMenu(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
    modifier: Modifier = Modifier,
) {
  NavigationBar(
      modifier =
          modifier.fillMaxWidth().height(60.dp).testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU),
      containerColor = MaterialTheme.colorScheme.onSurface,
      content = {
        bottomtabs.forEach { tab ->
          val isSelected = tab == selectedTab
          NavigationBarItem(
              icon = {
                Icon(
                    tab.icon,
                    contentDescription = null,
                    tint =
                        if (isSelected) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.outline)
              },
              label = { Text(tab.name) },
              selected = tab == selectedTab,
              onClick = { onTabSelected(tab) },
              modifier =
                  Modifier.clip(RoundedCornerShape(50.dp))
                      .testTag(NavigationTestTags.getTabTestTag(tab)))
        }
      },
  )
}

// PART2 : Top App Bar functions

/**
 * A top navigation menu with a centered title, a home button on the left, and a dropdown menu on
 * the right.
 *
 * @param selectedTab The currently selected tab.
 * @param onTabSelected A callback function that is invoked when a tab is selected. It takes a [Tab]
 *   as a parameter.
 * @param modifier A [Modifier] for this component. Default is [Modifier].
 * @param onSignedOut A callback function that is invoked when the user chooses to sign out. Default
 *   is an empty function.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigationMenu(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
    modifier: Modifier = Modifier,
    onSignedOut: () -> Unit = {}
) {
  TopAppBar(
      title = {
        Box(modifier = Modifier.fillMaxWidth()) {
          Text(
              text = selectedTab.name,
              modifier = Modifier.align(Alignment.Center).testTag(NavigationTestTags.TOP_BAR_TITLE),
              textAlign = TextAlign.Center)
        }
      },
      navigationIcon = {
        IconButton(
            onClick = { onTabSelected(Tab.HomePage) },
            modifier = Modifier.testTag(NavigationTestTags.HOMEPAGE_TAB)) {
              Icon(imageVector = Tab.HomePage.icon, contentDescription = Tab.HomePage.name)
            }
      },
      actions = { TopDropdownMenu(onTabSelected = onTabSelected, onSignedOut = onSignedOut) },
      modifier =
          modifier.fillMaxWidth().height(60.dp).testTag(NavigationTestTags.TOP_NAVIGATION_MENU),
      colors = topAppColor())
}

/**
 * A top navigation menu with a centered title, a home button on the left, and a specific dropdown
 * menu on the right, used only for the settings screen.
 *
 * @param selectedTab The currently selected tab.
 * @param onTabSelected A callback function that is invoked when a tab is selected. It takes a [Tab]
 *   as a parameter.
 * @param modifier A [Modifier] for this component. Default is [Modifier].
 * @param onSignedOut A callback function that is invoked when the user chooses to sign out. Default
 *   is an empty function.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigationMenuSettings(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
    modifier: Modifier = Modifier,
    onSignedOut: () -> Unit = {}
) {
  TopAppBar(
      title = {
        Box(modifier = Modifier.fillMaxWidth()) {
          Text(
              text = selectedTab.name,
              modifier = Modifier.align(Alignment.Center).testTag(NavigationTestTags.TOP_BAR_TITLE),
              textAlign = TextAlign.Center)
        }
      },
      navigationIcon = {
        IconButton(
            onClick = { onTabSelected(Tab.HomePage) },
            modifier = Modifier.testTag(NavigationTestTags.HOMEPAGE_TAB)) {
              Icon(imageVector = Tab.HomePage.icon, contentDescription = Tab.HomePage.name)
            }
      },
      actions = {
        TopDropdownMenuForSettings(onTabSelected = onTabSelected, onSignedOut = onSignedOut)
      },
      modifier =
          modifier.fillMaxWidth().height(60.dp).testTag(NavigationTestTags.TOP_NAVIGATION_MENU),
      colors = topAppColor())
}

/**
 * A top navigation menu with a title, and a specific dropdown menu on the right, used only for the
 * home page screen.
 *
 * @param selectedTab The currently selected tab.
 * @param onTabSelected A callback function that is invoked when a tab is selected. It takes a [Tab]
 *   as a parameter.
 * @param modifier A [Modifier] for this component. Default is [Modifier].
 * @param onSignedOut A callback function that is invoked when the user chooses to sign out. Default
 *   is an empty function.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigationMenu_HomePage(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
    modifier: Modifier = Modifier,
    onSignedOut: () -> Unit = {}
) {
  TopAppBar(
      title = {
        Box(modifier = Modifier.fillMaxWidth()) {
          Text(
              text = selectedTab.name,
              modifier = Modifier.align(Alignment.Center).testTag(NavigationTestTags.TOP_BAR_TITLE),
              textAlign = TextAlign.Center)
        }
      },
      actions = { TopDropdownMenu(onTabSelected = onTabSelected, onSignedOut = onSignedOut) },
      modifier =
          modifier.fillMaxWidth().height(60.dp).testTag(NavigationTestTags.TOP_NAVIGATION_MENU),
      colors = topAppColor())
}

/**
 * A top navigation menu with the title and a specific dropdown menu on the right, used only for the
 * profile screen.
 *
 * @param selectedTab The currently selected tab.
 * @param onTabSelected A callback function that is invoked when a tab is selected. It takes a [Tab]
 *   as a parameter.
 * @param modifier A [Modifier] for this component. Default is [Modifier].
 * @param onSignedOut A callback function that is invoked when the user chooses to sign out. Default
 *   is an empty function.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigationMenu_Profile(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
    modifier: Modifier = Modifier,
    onSignedOut: () -> Unit = {}
) {
  TopAppBar(
      title = {
        Box(modifier = Modifier.fillMaxWidth()) {
          Text(
              text = selectedTab.name,
              modifier = Modifier.align(Alignment.Center).testTag(NavigationTestTags.TOP_BAR_TITLE),
              textAlign = TextAlign.Center)
        }
      },
      actions = {
        TopDropdownMenuForProfile(onTabSelected = onTabSelected, onSignedOut = onSignedOut)
      },
      modifier =
          modifier.fillMaxWidth().height(60.dp).testTag(NavigationTestTags.TOP_NAVIGATION_MENU),
      colors = topAppColor())
}

/**
 * A top navigation menu with the title and a go back button
 *
 * @param selectedTab The currently selected tab.
 * @param onTabSelected A callback function that is invoked when a tab is selected. It takes a [Tab]
 *   as a parameter.
 * @param modifier A [Modifier] for this component. Default is [Modifier].
 * @param onSignedOut A callback function that is invoked when the user chooses to sign out. Default
 *   is an empty function.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigationMenu_Goback(
    selectedTab: Tab,
    modifier: Modifier = Modifier,
    goBack: () -> Unit = {}
) {
  TopAppBar(
      title = {
        Text(
            text = selectedTab.name,
            modifier = Modifier.fillMaxWidth().testTag(NavigationTestTags.TOP_BAR_TITLE),
            textAlign = TextAlign.Center)
      },
      navigationIcon = {
        IconButton(
            onClick = { goBack() },
            modifier = Modifier.testTag(NavigationTestTags.GO_BACK_BUTTON)) {
              Icon(
                  imageVector = Icons.Outlined.ArrowBack,
                  contentDescription = "Go back to previous screen")
            }
      },
      modifier =
          modifier.fillMaxWidth().height(60.dp).testTag(NavigationTestTags.TOP_NAVIGATION_MENU),
      colors = topAppColor())
}

////// PART3 :  Dropdown Menu functions

/**
 * A top dropdown menu with options for Profile, Settings, and Logout.
 *
 * @param onTabSelected A callback function that is invoked when a tab is selected. It takes a [Tab]
 *   as a parameter.
 * @param onSignedOut A callback function that is invoked when the user chooses to sign out. Default
 *   is an empty function.
 */
@Composable
fun TopDropdownMenu(onTabSelected: (Tab) -> Unit, onSignedOut: () -> Unit = {}) {
  var expanded by remember { mutableStateOf(false) }

  Box {
    IconButton(
        onClick = { expanded = !expanded },
        modifier = Modifier.testTag(NavigationTestTags.DROPMENU)) {
          Icon(Icons.Outlined.MoreVert, contentDescription = "Options")
        }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      // Profile section
      DropdownMenuItem(
          text = { Text(Tab.Profile.name) },
          leadingIcon = {
            Icon(imageVector = Tab.Profile.icon, contentDescription = Tab.Profile.name)
          },
          onClick = { onTabSelected(Tab.Profile) },
          modifier = Modifier.testTag(NavigationTestTags.PROFILE_TAB))

      // Settings section
      DropdownMenuItem(
          text = { Text(Tab.Settings.name) },
          leadingIcon = {
            Icon(imageVector = Tab.Settings.icon, contentDescription = Tab.Settings.name)
          },
          onClick = { onTabSelected(Tab.Settings) },
          modifier = Modifier.testTag(NavigationTestTags.SETTINGS_TAB))

      // Logout section
      DropdownMenuItem(
          text = { Text(Tab.SignOut.name) },
          leadingIcon = { Icon(Icons.Outlined.Logout, contentDescription = null) },
          onClick = { onSignedOut() },
          modifier = Modifier.testTag(NavigationTestTags.LOGOUT_TAB))
    }
  }
}

/**
 * A top dropdown menu with options for Profile, and Logout. Used only for the settings screen
 *
 * @param onTabSelected A callback function that is invoked when a tab is selected. It takes a [Tab]
 *   as a parameter.
 * @param onSignedOut A callback function that is invoked when the user chooses to sign out. Default
 *   is an empty function.
 */
@Composable
fun TopDropdownMenuForSettings(onTabSelected: (Tab) -> Unit, onSignedOut: () -> Unit = {}) {
  var expanded by remember { mutableStateOf(false) }

  Box {
    IconButton(
        onClick = { expanded = !expanded },
        modifier = Modifier.testTag(NavigationTestTags.DROPMENU)) {
          Icon(Icons.Outlined.Person, contentDescription = "Options")
        }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      // Profile section
      DropdownMenuItem(
          text = { Text(Tab.Profile.name) },
          leadingIcon = {
            Icon(imageVector = Tab.Profile.icon, contentDescription = Tab.Profile.name)
          },
          onClick = { onTabSelected(Tab.Profile) },
          modifier = Modifier.testTag(NavigationTestTags.PROFILE_TAB))

      // Logout section
      DropdownMenuItem(
          text = { Text(Tab.SignOut.name) },
          leadingIcon = { Icon(Icons.Outlined.Logout, contentDescription = null) },
          onClick = { onSignedOut() },
          modifier = Modifier.testTag(NavigationTestTags.LOGOUT_TAB))
    }
  }
}

/**
 * A top dropdown menu with options for Settings, and Logout. Used only for the profile screen
 *
 * @param onTabSelected A callback function that is invoked when a tab is selected. It takes a [Tab]
 *   as a parameter.
 * @param onSignedOut A callback function that is invoked when the user chooses to sign out. Default
 *   is an empty function.
 */
@Composable
fun TopDropdownMenuForProfile(onTabSelected: (Tab) -> Unit, onSignedOut: () -> Unit = {}) {
  var expanded by remember { mutableStateOf(false) }

  Box {
    IconButton(
        onClick = { expanded = !expanded },
        modifier = Modifier.testTag(NavigationTestTags.DROPMENU)) {
          Icon(Icons.Outlined.Settings, contentDescription = "Options")
        }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      // Settings section
      DropdownMenuItem(
          text = { Text(Tab.Settings.name) },
          leadingIcon = {
            Icon(imageVector = Tab.Settings.icon, contentDescription = Tab.Settings.name)
          },
          onClick = { onTabSelected(Tab.Settings) },
          modifier = Modifier.testTag(NavigationTestTags.SETTINGS_TAB))

      // Logout section
      DropdownMenuItem(
          text = { Text(Tab.SignOut.name) },
          leadingIcon = { Icon(Icons.Outlined.Logout, contentDescription = null) },
          onClick = { onSignedOut() },
          modifier = Modifier.testTag(NavigationTestTags.LOGOUT_TAB))
    }
  }
}

/** Helper function : Initiates sign-out */
@Composable
fun HandleSignedOutState(signedOut: Boolean, onSignedOut: () -> Unit) {
  val context = LocalContext.current

  LaunchedEffect(signedOut) {
    if (signedOut) {
      onSignedOut()
      Toast.makeText(context, context.getString(R.string.logOut_message), Toast.LENGTH_SHORT).show()
    }
  }
}

/** Helper function : To simplificate the calling to the theme color of the top app bar */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun topAppColor(): TopAppBarColors {
  return TopAppBarColors(
      containerColor = MaterialTheme.colorScheme.onSurface,
      scrolledContainerColor = MaterialTheme.colorScheme.background,
      navigationIconContentColor = MaterialTheme.colorScheme.outline,
      titleContentColor = MaterialTheme.colorScheme.outlineVariant,
      actionIconContentColor = MaterialTheme.colorScheme.outline,
  )
}
