package com.android.gatherly.ui.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Diversity1
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import com.android.gatherly.R

/**
 * Sealed class representing different tabs in the navigation menu. Each tab has a name, an icon,
 * and a destination screen.
 *
 * @param name The display name of the tab
 * @param icon The icon associated with the tab
 * @param destination The destination screen associated with the tab
 */
sealed class Tab(val name: String, val icon: ImageVector, val destination: Screen) {

  object SignOut : Tab("Sign Out", Icons.Outlined.Person, Screen.SignInScreen)

  /* To-Do related Tabs */
  object TodoOverview : Tab("To-Do", Icons.Outlined.FormatListBulleted, Screen.ToDoOverviewScreen)

  object AddTodo : Tab("Add To-Do", Icons.Outlined.Add, Screen.AddToDoScreen)

  object EditTodo : Tab("Edit To-Do", Icons.Outlined.Edit, Screen.EditTodoRootScreen)

  /* Event related Tabs */
  object EventsOverview : Tab("Events", Icons.Outlined.Group, Screen.EventsOverviewScreen)

  object AddEvent : Tab("Add Event", Icons.Outlined.Add, Screen.AddEventScreen)

  object EditEvent : Tab("Edit Event", Icons.Outlined.Edit, Screen.EditEventRootScreen)

  /* Timer Tab */
  object Timer : Tab("Timer", Icons.Outlined.Schedule, Screen.FocusTimerScreen)

  /* Map Tab */
  object Map : Tab("Map", Icons.Outlined.Place, Screen.MapScreen)

  /* Home page Tab */
  object HomePage : Tab("Home", Icons.Outlined.Home, Screen.HomePageScreen)

  /* Notifications Tab */
  object Notifications :
      Tab("Notifications", Icons.Outlined.Notifications, Screen.NotificationsScreen)

  /* Friend related Tab */
  object FriendRequests :
      Tab("Friend Requests", Icons.Default.ChevronRight, Screen.FriendRequestsScreen)

  object Friends : Tab("Friends", Icons.Outlined.Diversity1, Screen.FriendsScreen)

  object FindFriends : Tab("Find Friends", Icons.Outlined.GroupAdd, Screen.FindFriendsScreen)

  /* Profile related Tabs */
  object Profile : Tab("Your profile", Icons.Outlined.AccountCircle, Screen.ProfileScreen)

  object Settings : Tab("Settings", Icons.Outlined.Settings, Screen.SettingsScreen)

  object UserProfile :
      Tab("User Profile", Icons.Outlined.AccountCircle, Screen.UserProfileRootScreen)

  /* Group related Tabs */
  object GroupsOverview : Tab("Groups Overview", Icons.Outlined.Group, Screen.OverviewGroupsScreen)

  object GroupInfo : Tab("Group Information", Icons.Outlined.Info, Screen.GroupsInfoRootScreen)

  object EditGroup : Tab("Edit Group", Icons.Outlined.Edit, Screen.EditGroupRootScreen)

  object AddGroup : Tab("Add Group", Icons.Outlined.GroupAdd, Screen.AddGroupScreen)

  /* Badge Tab */
  object Badge : Tab("Badges", Icons.Outlined.Badge, Screen.BadgeScreen)

  /* Focus Points Tab */
  object FocusPoints : Tab("Focus History", Icons.Outlined.History, Screen.FocusScreen)
}

/** List of bottom bar navigation tabs */
private val bottomBarTabs =
    listOf(
        Tab.Timer,
        Tab.TodoOverview,
        Tab.EventsOverview,
        Tab.Map,
    )

/* ------------------------------ Bottom Navigation Bar functions ------------------------------- */
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
          modifier
              .fillMaxWidth()
              .height(dimensionResource(R.dimen.bar_menu_height))
              .testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU),
      containerColor = MaterialTheme.colorScheme.surfaceVariant,
      content = {
        bottomBarTabs.forEach { tab ->
          val isSelected = tab == selectedTab
          NavigationBarItem(
              icon = {
                Icon(
                    tab.icon,
                    contentDescription = null,
                    tint =
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant)
              },
              label = { Text(tab.name) },
              colors =
                  NavigationBarItemDefaults.colors(
                      selectedIconColor = MaterialTheme.colorScheme.primary,
                      unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                      selectedTextColor = MaterialTheme.colorScheme.primary,
                      unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                      indicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
              selected = tab == selectedTab,
              onClick = { onTabSelected(tab) },
              modifier =
                  Modifier.clip(
                          RoundedCornerShape(
                              dimensionResource(R.dimen.rounded_corner_shape_clip_large)))
                      .testTag(NavigationTestTags.getTabTestTag(tab)))
        }
      },
  )
}

/* -------------------------------- Top Navigation Bar functions ---------------------------- */

/**
 * A top navigation menu with a centered title, a home button on the left, and a dropdown menu on
 * the right.
 *
 * @param selectedTab The currently selected tab.
 * @param onTabSelected A callback function that is invoked when a tab is selected. It takes a [Tab]
 *   as a parameter.
 * @param modifier A [Modifier] for this component. Default is [Modifier].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigationMenu(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
    modifier: Modifier = Modifier
) {
  CenterAlignedTopAppBar(
      title = {
        Text(text = selectedTab.name, modifier = Modifier.testTag(NavigationTestTags.TOP_BAR_TITLE))
      },
      navigationIcon = {
        IconButton(
            onClick = { onTabSelected(Tab.HomePage) },
            modifier = Modifier.testTag(NavigationTestTags.HOMEPAGE_TAB)) {
              Icon(imageVector = Tab.HomePage.icon, contentDescription = Tab.HomePage.name)
            }
      },
      actions = { TopDropdownMenu(onTabSelected = onTabSelected) },
      modifier =
          modifier
              .fillMaxWidth()
              .height(dimensionResource(R.dimen.bar_menu_height))
              .testTag(NavigationTestTags.TOP_NAVIGATION_MENU),
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
  CenterAlignedTopAppBar(
      title = {
        Text(text = selectedTab.name, modifier = Modifier.testTag(NavigationTestTags.TOP_BAR_TITLE))
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
          modifier
              .fillMaxWidth()
              .height(dimensionResource(R.dimen.bar_menu_height))
              .testTag(NavigationTestTags.TOP_NAVIGATION_MENU),
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
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigationMenu_HomePage(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
    modifier: Modifier = Modifier
) {
  CenterAlignedTopAppBar(
      title = {
        Text(text = selectedTab.name, modifier = Modifier.testTag(NavigationTestTags.TOP_BAR_TITLE))
      },
      actions = { TopDropdownMenu(onTabSelected = onTabSelected) },
      modifier =
          modifier
              .fillMaxWidth()
              .height(dimensionResource(R.dimen.bar_menu_height))
              .testTag(NavigationTestTags.TOP_NAVIGATION_MENU),
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
  CenterAlignedTopAppBar(
      title = {
        Text(text = selectedTab.name, modifier = Modifier.testTag(NavigationTestTags.TOP_BAR_TITLE))
      },
      navigationIcon = {
        IconButton(
            onClick = { onTabSelected(Tab.HomePage) },
            modifier = Modifier.testTag(NavigationTestTags.HOMEPAGE_TAB)) {
              Icon(imageVector = Tab.HomePage.icon, contentDescription = Tab.HomePage.name)
            }
      },
      actions = {
        TopDropdownMenuForProfile(onTabSelected = onTabSelected, onSignedOut = onSignedOut)
      },
      modifier =
          modifier
              .fillMaxWidth()
              .height(dimensionResource(R.dimen.bar_menu_height))
              .testTag(NavigationTestTags.TOP_NAVIGATION_MENU),
      colors = topAppColor())
}

/**
 * A top navigation menu with the title and a go back button
 *
 * @param selectedTab The currently selected tab.
 * @param modifier A [Modifier] for this component. Default is [Modifier].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigationMenu_Goback(
    selectedTab: Tab,
    modifier: Modifier = Modifier,
    goBack: () -> Unit = {}
) {
  CenterAlignedTopAppBar(
      title = {
        Text(text = selectedTab.name, modifier = Modifier.testTag(NavigationTestTags.TOP_BAR_TITLE))
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
          modifier
              .fillMaxWidth()
              .height(dimensionResource(R.dimen.bar_menu_height))
              .testTag(NavigationTestTags.TOP_NAVIGATION_MENU),
      colors = topAppColor())
}

/* -------------------------------- Top Bar Dropdown Menu functions ---------------------------- */

/**
 * A top dropdown menu with options for Profile, Settings, and Logout.
 *
 * @param onTabSelected A callback function that is invoked when a tab is selected. It takes a [Tab]
 *   as a parameter.
 */
@Composable
fun TopDropdownMenu(onTabSelected: (Tab) -> Unit) {
  var expanded by remember { mutableStateOf(false) }

  Box {
    IconButton(
        onClick = { expanded = !expanded },
        modifier = Modifier.testTag(NavigationTestTags.DROP_MENU)) {
          Icon(Icons.Outlined.MoreVert, contentDescription = "Options")
        }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        containerColor = MaterialTheme.colorScheme.surfaceVariant) {
          // Profile section
          DropdownMenuItem(
              text = { Text(Tab.Profile.name, color = MaterialTheme.colorScheme.onSurfaceVariant) },
              leadingIcon = {
                Icon(
                    imageVector = Tab.Profile.icon,
                    contentDescription = Tab.Profile.name,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
              },
              onClick = { onTabSelected(Tab.Profile) },
              modifier = Modifier.testTag(NavigationTestTags.PROFILE_TAB))

          // Notifications section
          NotificationsDropdownItem(onTabSelected = onTabSelected)

          // Add Group section
          AddGroupDropdownItem(onTabSelected = onTabSelected)

          // Settings section
          DropdownMenuItem(
              text = {
                Text(Tab.Settings.name, color = MaterialTheme.colorScheme.onSurfaceVariant)
              },
              leadingIcon = {
                Icon(
                    imageVector = Tab.Settings.icon,
                    contentDescription = Tab.Settings.name,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
              },
              onClick = { onTabSelected(Tab.Settings) },
              modifier = Modifier.testTag(NavigationTestTags.SETTINGS_TAB))
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
        modifier = Modifier.testTag(NavigationTestTags.DROP_MENU)) {
          Icon(Icons.Outlined.Person, contentDescription = "Options")
        }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        containerColor = MaterialTheme.colorScheme.surfaceVariant) {
          // Profile section
          DropdownMenuItem(
              text = { Text(Tab.Profile.name, color = MaterialTheme.colorScheme.onSurfaceVariant) },
              leadingIcon = {
                Icon(
                    imageVector = Tab.Profile.icon,
                    contentDescription = Tab.Profile.name,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
              },
              onClick = { onTabSelected(Tab.Profile) },
              modifier = Modifier.testTag(NavigationTestTags.PROFILE_TAB))

          // Notifications section
          NotificationsDropdownItem(onTabSelected = onTabSelected)

          // Add Group section
          AddGroupDropdownItem(onTabSelected = onTabSelected)

          // Logout section
          DropdownMenuItem(
              text = { Text(Tab.SignOut.name, color = MaterialTheme.colorScheme.onSurfaceVariant) },
              leadingIcon = {
                Icon(
                    Icons.Outlined.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
              },
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
        modifier = Modifier.testTag(NavigationTestTags.DROP_MENU)) {
          Icon(Icons.Outlined.Settings, contentDescription = "Options")
        }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        containerColor = MaterialTheme.colorScheme.surfaceVariant) {
          // Notifications section
          NotificationsDropdownItem(onTabSelected = onTabSelected)

          // Add Group section
          AddGroupDropdownItem(onTabSelected = onTabSelected)

          // Settings section
          DropdownMenuItem(
              text = {
                Text(Tab.Settings.name, color = MaterialTheme.colorScheme.onSurfaceVariant)
              },
              leadingIcon = {
                Icon(
                    imageVector = Tab.Settings.icon,
                    contentDescription = Tab.Settings.name,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
              },
              onClick = { onTabSelected(Tab.Settings) },
              modifier = Modifier.testTag(NavigationTestTags.SETTINGS_TAB))

          // Logout section
          DropdownMenuItem(
              text = { Text(Tab.SignOut.name, color = MaterialTheme.colorScheme.onSurfaceVariant) },
              leadingIcon = {
                Icon(
                    Icons.Outlined.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
              },
              onClick = { onSignedOut() },
              modifier = Modifier.testTag(NavigationTestTags.LOGOUT_TAB))
        }
  }
}

/* -------------------------------- Helper functions ---------------------------------------- */

/**
 * Helper function : Initiates sign-out
 *
 * @param signedOut A boolean indicating whether the user is signed out
 * @param onSignedOut A callback function that is invoked when the user is signed out
 */
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

/** Helper private function : Customizes the top app bar colors */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun topAppColor(): TopAppBarColors {
  return TopAppBarColors(
      containerColor = MaterialTheme.colorScheme.background,
      scrolledContainerColor = MaterialTheme.colorScheme.background,
      navigationIconContentColor = MaterialTheme.colorScheme.primary,
      titleContentColor = MaterialTheme.colorScheme.primary,
      actionIconContentColor = MaterialTheme.colorScheme.primary,
  )
}

/**
 * Helper private function: Add Group section in the dropdown menu.
 *
 * @param onTabSelected A callback function that is invoked when a tab is selected. It takes a [Tab]
 *   as a parameter.
 */
@Composable
private fun AddGroupDropdownItem(onTabSelected: (Tab) -> Unit) {
  DropdownMenuItem(
      text = { Text(Tab.AddGroup.name, color = MaterialTheme.colorScheme.onSurfaceVariant) },
      leadingIcon = {
        Icon(
            imageVector = Tab.AddGroup.icon,
            contentDescription = Tab.AddGroup.name,
            tint = MaterialTheme.colorScheme.onSurfaceVariant)
      },
      onClick = { onTabSelected(Tab.AddGroup) },
      modifier = Modifier.testTag(NavigationTestTags.ADD_GROUP_TAB))
}

/**
 * Helper private function: A Notifications section in the dropdown menu.
 *
 * @param onTabSelected A callback function that is invoked when a tab is selected. It takes a [Tab]
 *   as a parameter.
 */
@Composable
private fun NotificationsDropdownItem(onTabSelected: (Tab) -> Unit) {
  DropdownMenuItem(
      text = { Text(Tab.Notifications.name, color = MaterialTheme.colorScheme.onSurfaceVariant) },
      leadingIcon = {
        Icon(
            imageVector = Tab.Notifications.icon,
            contentDescription = Tab.Notifications.name,
            tint = MaterialTheme.colorScheme.onSurfaceVariant)
      },
      onClick = { onTabSelected(Tab.Notifications) },
      modifier = Modifier.testTag(NavigationTestTags.NOTIFICATIONS_TAB))
}
