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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.android.gatherly.ui.authentication.SignInScreen
import com.android.gatherly.ui.events.AddEventScreen
import com.android.gatherly.ui.events.EditEventsScreen
import com.android.gatherly.ui.events.EventsScreen
import com.android.gatherly.ui.focusTimer.TimerScreen
import com.android.gatherly.ui.friends.FriendsScreen
import com.android.gatherly.ui.homePage.HomePageScreen
import com.android.gatherly.ui.map.MapScreen
import com.android.gatherly.ui.navigation.NavigationActions
import com.android.gatherly.ui.navigation.Screen
import com.android.gatherly.ui.profile.ProfileScreen
import com.android.gatherly.ui.settings.SettingsScreen
import com.android.gatherly.ui.theme.GatherlyTheme
import com.android.gatherly.ui.todo.AddToDoScreen
import com.android.gatherly.ui.todo.EditToDoScreen
import com.android.gatherly.ui.todo.OverviewScreen
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      GatherlyTheme(darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) { GatherlyApp() }
      }
    }
  }
}

/**
 * `GatherlyApp` is the main composable function that sets up the whole app UI. It initializes the
 * navigation controller and defines the navigation graph. You can add your app implementation
 * inside this function.
 *
 * @param navHostController The navigation controller used for navigating between screens.
 * @param context The context of the application, used for accessing resources and services.
 * @param credentialManager The CredentialManager instance for handling authentication credentials.
 */
@Composable
fun GatherlyApp(
    context: Context = LocalContext.current,
    credentialManager: CredentialManager = CredentialManager.create(context),
) {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)
  val startDestination =
      if (FirebaseAuth.getInstance().currentUser == null) Screen.SignIn.name
      else Screen.HomePage.name

  NavHost(navController = navController, startDestination = startDestination) {

    // SIGNIN COMPOSABLE  ------------------------------
    navigation(
        startDestination = Screen.SignIn.route,
        route = Screen.SignIn.name,
    ) {
      composable(Screen.SignIn.route) {
        SignInScreen(
            credentialManager = credentialManager,
            onSignedIn = { navigationActions.navigateTo(Screen.HomePage) })
      }
    }
    // HOMEPAGE COMPOSABLE  ------------------------------
    navigation(
        startDestination = Screen.HomePage.route,
        route = Screen.HomePage.name,
    ) {
      composable(Screen.HomePage.route) {
        HomePageScreen(
            credentialManager = credentialManager,
            navigationActions = navigationActions,
            onSignedOut = { navigationActions.navigateTo(Screen.SignIn) })
      }
    }

    // TODO COMPOSABLE  ------------------------------
    navigation(
        startDestination = Screen.OverviewToDo.route,
        route = Screen.OverviewToDo.name,
    ) {
      composable(Screen.OverviewToDo.route) {
        OverviewScreen(
            credentialManager = credentialManager,
            navigationActions = navigationActions,
            onSignedOut = { navigationActions.navigateTo(Screen.SignIn) },
            onAddTodo = { navigationActions.navigateTo(Screen.AddToDo) },
            onSelectTodo = { navigationActions.navigateTo(Screen.EditToDo(it.uid)) })
      }
      composable(Screen.AddToDo.route) {
        AddToDoScreen(
            onAdd = { navigationActions.navigateTo(Screen.OverviewToDo) },
            goBack = { navigationActions.goBack() })
      }

      composable(Screen.EditToDo.route) { navBackStackEntry ->
        // Get the Todo UID from the arguments
        val uid = navBackStackEntry.arguments?.getString("uid")

        // Create the EditToDoScreen with the Todo UID
        uid?.let {
          EditToDoScreen(
              onSave = { navigationActions.navigateTo(Screen.OverviewToDo) },
              todoUid = it,
              goBack = { navigationActions.goBack() },
              onDelete = { navigationActions.navigateTo(Screen.OverviewToDo) })
        }
            ?: run {
              Log.e("EditToDoScreen", "ToDo UID is null")
              Toast.makeText(context, "ToDo UID is null", Toast.LENGTH_SHORT).show()
            }
      }
    }

    // MAP COMPOSABLE  ------------------------------
    navigation(
        startDestination = Screen.Map.route,
        route = Screen.Map.name,
    ) {
      composable(Screen.Map.route) {
        MapScreen(
            navigationActions = navigationActions,
            credentialManager = credentialManager,
            onSignedOut = { navigationActions.navigateTo(Screen.SignIn) })
      }
    }

    // TIMER COMPOSABLE  ------------------------------
    navigation(
        startDestination = Screen.FocusTimerScreen.route,
        route = Screen.FocusTimerScreen.name,
    ) {
      composable(Screen.FocusTimerScreen.route) {
        TimerScreen(
            navigationActions = navigationActions,
            credentialManager = credentialManager,
            onSignedOut = { navigationActions.navigateTo(Screen.SignIn) })
      }
    }

    // EVENTS COMPOSABLE  ------------------------------
    navigation(
        startDestination = Screen.EventsScreen.route,
        route = Screen.EventsScreen.name,
    ) {
      composable(Screen.EventsScreen.route) {
        EventsScreen(
            navigationActions = navigationActions,
            credentialManager = credentialManager,
            onSignedOut = { navigationActions.navigateTo(Screen.SignIn) },
            onAddEvent = { navigationActions.navigateTo(Screen.AddEventScreen) },
            navigateToEditEvent = { event ->
              navigationActions.navigateTo(Screen.EditEvent(event.id))
            })
      }
      composable(Screen.AddEventScreen.route) {
        AddEventScreen(
            goBack = { navigationActions.goBack() },
            onSave = { navigationActions.navigateTo(Screen.EventsScreen) })
      }

      composable(Screen.EditEvent.route) { navBackStackEntry ->
        val uid = navBackStackEntry.arguments?.getString("uid")
        uid?.let {
          EditEventsScreen(
              eventId = it,
              goBack = { navigationActions.goBack() },
              onSave = { navigationActions.navigateTo(Screen.EventsScreen) })
        }
            ?: run {
              Log.e("EditEventsScreen", "Event UID is null")
              Toast.makeText(context, "Event UID is null", Toast.LENGTH_SHORT).show()
            }
      }
    }

    // PROFILE COMPOSABLE  ------------------------------
    navigation(
        startDestination = Screen.ProfileScreen.route,
        route = Screen.ProfileScreen.name,
    ) {
      composable(Screen.ProfileScreen.route) {
        ProfileScreen(
            navigationActions = navigationActions,
            credentialManager = credentialManager,
            onSignedOut = { navigationActions.navigateTo(Screen.SignIn) })
      }
    }

    // SETTINGS COMPOSABLE  ------------------------------
    navigation(
        startDestination = Screen.SettingsScreen.route,
        route = Screen.SettingsScreen.name,
    ) {
      composable(Screen.SettingsScreen.route) {
        SettingsScreen(
            navigationActions = navigationActions,
            credentialManager = credentialManager,
            onSignedOut = { navigationActions.navigateTo(Screen.SignIn) })
      }
    }

    // FRIENDS COMPOSABLE  ------------------------------
    navigation(
        startDestination = Screen.FriendsScreen.route,
        route = Screen.FriendsScreen.name,
    ) {
      composable(Screen.FriendsScreen.route) {
        FriendsScreen(
            credentialManager = credentialManager, goBack = { navigationActions.goBack() })
      }
    }
  }
}
