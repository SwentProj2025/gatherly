package com.android.gatherly

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.android.gatherly.model.profile.ProfileStatus
import com.android.gatherly.model.profile.UserStatusManager
import com.android.gatherly.ui.authentication.InitProfileScreen
import com.android.gatherly.ui.authentication.SignInScreen
import com.android.gatherly.ui.badge.BadgeScreen
import com.android.gatherly.ui.events.AddEventScreen
import com.android.gatherly.ui.events.EditEventsScreen
import com.android.gatherly.ui.events.EventsOverviewScreen
import com.android.gatherly.ui.events.EventsScreenActions
import com.android.gatherly.ui.focusTimer.TimerScreen
import com.android.gatherly.ui.friends.FindFriendsScreen
import com.android.gatherly.ui.friends.FriendsScreen
import com.android.gatherly.ui.groups.AddGroupScreen
import com.android.gatherly.ui.groups.EditGroupScreen
import com.android.gatherly.ui.groups.GroupInformationScreen
import com.android.gatherly.ui.groups.GroupsOverviewScreen
import com.android.gatherly.ui.homePage.HomePageScreen
import com.android.gatherly.ui.homePage.HomePageScreenActions
import com.android.gatherly.ui.map.MapScreen
import com.android.gatherly.ui.navigation.NavigationActions
import com.android.gatherly.ui.navigation.Screen
import com.android.gatherly.ui.notifications.FriendRequestsScreen
import com.android.gatherly.ui.notifications.NotificationsScreen
import com.android.gatherly.ui.points.FocusPointsScreen
import com.android.gatherly.ui.profile.ProfileScreen
import com.android.gatherly.ui.profile.UserProfileScreen
import com.android.gatherly.ui.settings.SettingsScreen
import com.android.gatherly.ui.theme.GatherlyTheme
import com.android.gatherly.ui.todo.AddToDoScreen
import com.android.gatherly.ui.todo.EditToDoScreen
import com.android.gatherly.ui.todo.OverviewScreen
import com.android.gatherly.utils.MapCoordinator
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * `MainActivity` is the entry point of the Gatherly application. It sets up the main UI and manages
 * user status based on the activity lifecycle.
 */
class MainActivity : ComponentActivity() {

  /**
   * Called when the activity is first created. It sets the content view to the main composable
   * function `GatherlyApp`, wrapped in the app's theme.
   */
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent { GatherlyTheme { Surface(modifier = Modifier.fillMaxSize()) { GatherlyApp() } } }
  }

  // UserStatusManager instance to manage user online/offline status
  private val userStatusManager = UserStatusManager()

  /** Called when the activity becomes visible to the user. It sets the user's status to ONLINE. */
  override fun onStart() {
    super.onStart()
    lifecycleScope.launch { userStatusManager.setStatus(ProfileStatus.ONLINE) }
  }

  /**
   * Called when the activity is no longer visible to the user. It sets the user's status to
   * OFFLINE.
   */
  override fun onStop() {
    super.onStop()
    lifecycleScope.launch { userStatusManager.setStatus(ProfileStatus.OFFLINE) }
  }
}

/**
 * `GatherlyApp` is the main composable function that sets up the whole app UI. It initializes the
 * navigation controller and defines the navigation graph. You can add your app implementation
 * inside this function.
 *
 * @param context The context of the application, used for accessing resources and services.
 * @param credentialManager The CredentialManager instance for handling authentication credentials.
 */
@Composable
fun GatherlyApp(
    context: Context = LocalContext.current,
    credentialManager: CredentialManager = CredentialManager.create(context),
) {
  // Remember the NavController for navigation between screens
  val navController = rememberNavController()
  // Initialize navigation actions with the NavController
  val navigationActions = NavigationActions(navController)
  // Remember the MapCoordinator for managing map-related interactions
  val mapCoordinator = remember { MapCoordinator() }
  // Determine the start destination based on user authentication status
  val startDestination =
      if (FirebaseAuth.getInstance().currentUser == null) Screen.SignInScreen.name
      else Screen.HomePageScreen.name

  // Set up the NavHost with the navigation graph
  NavHost(navController = navController, startDestination = startDestination) {

    // ---------------------------- SIGN IN COMPOSABLE  ------------------------------
    navigation(
        startDestination = Screen.SignInScreen.route,
        route = Screen.SignInScreen.name,
    ) {
      composable(Screen.SignInScreen.route) {
        SignInScreen(credentialManager = credentialManager, navigationActions = navigationActions)
      }
    }
    // ----------------------------- HOMEPAGE COMPOSABLE  ------------------------------
    navigation(
        startDestination = Screen.HomePageScreen.route,
        route = Screen.HomePageScreen.name,
    ) {
      composable(Screen.HomePageScreen.route) {
        HomePageScreen(
            navigationActions = navigationActions,
            homePageScreenActions =
                HomePageScreenActions(
                    onClickFocusButton = { navigationActions.navigateTo(Screen.FocusTimerScreen) },
                    onClickTodoTitle = { navigationActions.navigateTo(Screen.ToDoOverviewScreen) },
                    onClickFriendsSection = { navigationActions.navigateTo(Screen.FriendsScreen) },
                    onClickTodo = { navigationActions.navigateTo(Screen.EditToDoScreen(it.uid)) },
                    onClickEventsTitle = {
                      navigationActions.navigateTo(Screen.EventsOverviewScreen)
                    },
                ),
            coordinator = mapCoordinator)
      }
    }

    // ------------------------------- TO-DO COMPOSABLE  ------------------------------
    navigation(
        startDestination = Screen.ToDoOverviewScreen.route,
        route = Screen.ToDoOverviewScreen.name,
    ) {
      composable(Screen.ToDoOverviewScreen.route) {
        OverviewScreen(
            navigationActions = navigationActions,
            onAddTodo = { navigationActions.navigateTo(Screen.AddToDoScreen) },
            onSelectTodo = { navigationActions.navigateTo(Screen.EditToDoScreen(it.uid)) })
      }
      composable(Screen.AddToDoScreen.route) {
        AddToDoScreen(
            onAdd = { navigationActions.navigateTo(Screen.ToDoOverviewScreen) },
            goBack = { navigationActions.goBack() })
      }

      composable(Screen.EditToDoScreen.ROUTE) { navBackStackEntry ->
        // Get the To-Do UID from the arguments
        val uid = navBackStackEntry.arguments?.getString("uid")

        // Create the EditToDoScreen with the To-Do UID
        uid?.let {
          EditToDoScreen(
              onSave = { navigationActions.navigateTo(Screen.ToDoOverviewScreen) },
              todoUid = it,
              goBack = { navigationActions.goBack() },
              onDelete = { navigationActions.navigateTo(Screen.ToDoOverviewScreen) })
        }
            ?: run {
              Log.e("EditToDoScreen", "ToDo UID is null")
              Toast.makeText(context, "ToDo UID is null", Toast.LENGTH_SHORT).show()
            }
      }
    }

    // ---------------------------------- MAP COMPOSABLE  -------------------------------------
    navigation(
        startDestination = Screen.MapScreen.route,
        route = Screen.MapScreen.name,
    ) {
      composable(Screen.MapScreen.route) {
        MapScreen(
            navigationActions = navigationActions,
            goToEvent = { event ->
              navigationActions.navigateTo(Screen.EventsDetailsScreen(event))
            },
            goToToDo = { navigationActions.navigateTo(Screen.ToDoOverviewScreen) },
            coordinator = mapCoordinator)
      }
    }

    // ----------------------------------- TIMER COMPOSABLE ------------------------------------
    navigation(
        startDestination = Screen.FocusTimerScreen.route,
        route = Screen.FocusTimerScreen.name,
    ) {
      composable(Screen.FocusTimerScreen.route) {
        TimerScreen(navigationActions = navigationActions)
      }
    }

    // ----------------------------------- EVENTS COMPOSABLE  --------------------------------------
    navigation(
        startDestination = Screen.EventsOverviewScreen.route,
        route = Screen.EventsOverviewScreen.name,
    ) {
      composable(Screen.EventsOverviewScreen.route) {
        EventsOverviewScreen(
            navigationActions = navigationActions,
            actions =
                EventsScreenActions(
                    onSignedOut = { navigationActions.navigateTo(Screen.SignInScreen) },
                    onAddEvent = { navigationActions.navigateTo(Screen.AddEventScreen) },
                    navigateToEditEvent = { event ->
                      navigationActions.navigateTo(Screen.EditEvent(event.id))
                    }),
            coordinator = mapCoordinator)
      }

      composable(Screen.EventsDetailsScreen.ROUTE) { navBackStackEntry ->
        val uid = navBackStackEntry.arguments?.getString("uid")
        uid?.let {
          EventsOverviewScreen(
              navigationActions = navigationActions,
              actions =
                  EventsScreenActions(
                      onSignedOut = { navigationActions.navigateTo(Screen.SignInScreen) },
                      onAddEvent = { navigationActions.navigateTo(Screen.AddEventScreen) },
                      navigateToEditEvent = { event ->
                        navigationActions.navigateTo(Screen.EditEvent(event.id))
                      }),
              eventId = it,
              coordinator = mapCoordinator)
        }
      }

      composable(Screen.AddEventScreen.route) {
        AddEventScreen(
            goBack = { navigationActions.goBack() },
            onSave = { navigationActions.navigateTo(Screen.EventsOverviewScreen) })
      }

      composable(Screen.EditEvent.ROUTE) { navBackStackEntry ->
        val uid = navBackStackEntry.arguments?.getString("uid")
        uid?.let {
          EditEventsScreen(
              eventId = it,
              goBack = { navigationActions.goBack() },
              onSave = { navigationActions.navigateTo(Screen.EventsOverviewScreen) })
        }
            ?: run {
              Log.e("EditEventsScreen", "Event UID is null")
              Toast.makeText(context, "Event UID is null", Toast.LENGTH_SHORT).show()
            }
      }
    }

    // ---------------------------------  PROFILE COMPOSABLE  ------------------------------------
    navigation(
        startDestination = Screen.ProfileScreen.route,
        route = Screen.ProfileScreen.name,
    ) {
      composable(Screen.ProfileScreen.route) {
        ProfileScreen(
            navigationActions = navigationActions,
            credentialManager = credentialManager,
            onBadgeClicked = { navigationActions.navigateTo(Screen.BadgeScreen) },
            onSignedOut = { navigationActions.navigateTo(Screen.SignInScreen) })
      }
    }

    // ----------------------------------- BADGE COMPOSABLE  --------------------------------------
    navigation(
        startDestination = Screen.BadgeScreen.route,
        route = Screen.BadgeScreen.name,
    ) {
      composable(Screen.BadgeScreen.route) {
        BadgeScreen(goBack = { navigationActions.navigateTo(Screen.ProfileScreen) })
      }
    }

    // --------------------------------- FOCUS HISTORY COMPOSABLE  --------------------------------
    navigation(
        startDestination = Screen.FocusScreen.route,
        route = Screen.FocusScreen.name,
    ) {
      composable(Screen.FocusScreen.route) {
        FocusPointsScreen(goBack = { navigationActions.navigateTo(Screen.ProfileScreen) })
      }
    }

    // ----------------------------------  SETTINGS COMPOSABLE  -----------------------------------
    navigation(
        startDestination = Screen.SettingsScreen.route,
        route = Screen.SettingsScreen.name,
    ) {
      composable(Screen.SettingsScreen.route) {
        SettingsScreen(
            navigationActions = navigationActions,
            credentialManager = credentialManager,
            onSignedOut = { navigationActions.navigateTo(Screen.SignInScreen) })
      }
    }

    // ----------------------------------- FRIENDS COMPOSABLE  ----------------------------------
    navigation(
        startDestination = Screen.FriendsScreen.route,
        route = Screen.FriendsScreen.name,
    ) {
      composable(Screen.FriendsScreen.route) {
        FriendsScreen(
            onFindFriends = { navigationActions.navigateTo(Screen.FindFriendsScreen) },
            goBack = { navigationActions.goBack() },
            onClickFriend = { profile ->
              navigationActions.navigateTo(Screen.UserProfileScreen(profile.uid))
            })
      }

      composable(Screen.FindFriendsScreen.route) {
        FindFriendsScreen(
            goBack = { navigationActions.goBack() },
            onClickFriend = { profile ->
              navigationActions.navigateTo(Screen.UserProfileScreen(profile.uid))
            })
      }

      composable(Screen.UserProfileScreen.ROUTE) { entry ->
        val uid = entry.arguments?.getString("uid")

        if (uid != null) {
          UserProfileScreen(uid = uid, navigationActions = navigationActions)
        } else {
          run {
            Log.e("UserProfileScreen", "User uid is null")
            Toast.makeText(context, "User UID is null", Toast.LENGTH_SHORT).show()
          }
        }
      }
    }

    // ---------------------------------- INIT PROFILE COMPOSABLE  ------------------------------
    navigation(
        startDestination = Screen.InitProfileScreen.route,
        route = Screen.InitProfileScreen.name,
    ) {
      composable(Screen.InitProfileScreen.route) {
        InitProfileScreen(navigationActions = navigationActions)
      }
    }

    // ---------------------------------- NOTIFICATIONS COMPOSABLE  ------------------------------
    navigation(
        startDestination = Screen.NotificationsScreen.route,
        route = Screen.NotificationsScreen.name,
    ) {
      composable(Screen.NotificationsScreen.route) {
        NotificationsScreen(
            navigationActions = navigationActions,
            onVisitProfile = { profile ->
              navigationActions.navigateTo(Screen.UserProfileScreen(profile.uid))
            })
      }
    }

    // -------------------------------- FRIEND REQUESTS COMPOSABLE  --------------------------------
    navigation(
        startDestination = Screen.FriendRequestsScreen.route,
        route = Screen.FriendRequestsScreen.name,
    ) {
      composable(Screen.FriendRequestsScreen.route) {
        FriendRequestsScreen(goBack = { navigationActions.goBack() })
      }
    }

    // ------------------------------------ GROUPS COMPOSABLE  -------------------------------------
    navigation(
        startDestination = Screen.AddGroupScreen.route,
        route = Screen.AddGroupScreen.name,
    ) {

      // --------------------------------- ADD GROUP COMPOSABLE  ----------------------------------
      composable(Screen.AddGroupScreen.route) {
        AddGroupScreen(
            goBack = { navigationActions.goBack() },
            onCreate = { navigationActions.navigateTo(Screen.OverviewGroupsScreen) })
      }

      val nullUIDMessage = "Group UID is null"

      // ---------------------------------- GROUP INFO COMPOSABLE  ---------------------------------
      composable(Screen.GroupInfoScreen.ROUTE) { navBackStackEntry ->
        val uid = navBackStackEntry.arguments?.getString("uid")
        uid?.let { GroupInformationScreen(navigationActions = navigationActions, groupId = it) }
            ?: run {
              Log.e("GroupInformationScreen", nullUIDMessage)
              Toast.makeText(context, "Navigating to an invalid group", Toast.LENGTH_SHORT).show()
            }
      }

      // --------------------------------- EDIT GROUP COMPOSABLE  ----------------------------------
      composable(Screen.EditGroupScreen.ROUTE) { navBackStackEntry ->
        val uid = navBackStackEntry.arguments?.getString("uid")
        uid?.let { groupId ->
          EditGroupScreen(
              groupId = groupId,
              goBack = { navigationActions.goBack() },
              onSaved = { navigationActions.navigateTo(Screen.GroupInfoScreen(groupId)) },
              onDelete = { navigationActions.navigateTo(Screen.OverviewGroupsScreen) })
        }
            ?: run {
              Log.e("EditGroupScreen", nullUIDMessage)
              Toast.makeText(context, "Trying to edit an invalid group", Toast.LENGTH_SHORT).show()
            }
      }
    }

    // ---------------------------------- GROUP OVERVIEW COMPOSABLE  ------------------------------
    navigation(
        startDestination = Screen.OverviewGroupsScreen.route,
        route = Screen.OverviewGroupsScreen.name,
    ) {
      composable(Screen.OverviewGroupsScreen.route) {
        GroupsOverviewScreen(navigationActions = navigationActions)
      }
    }
  }
}
