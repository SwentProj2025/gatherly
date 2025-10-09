package com.android.gatherly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.gatherly.ui.theme.GatherlyAppTheme
import com.google.firebase.auth.FirebaseAuth
import okhttp3.OkHttpClient
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.android.gatherly.ui.overview.OverviewScreen
import com.android.gatherly.ui.SignIn.SignInScreen
import com.android.gatherly.ui.events.EventsScreen
import com.android.gatherly.ui.focusTimer.FocusTimerInitScreen
import com.android.gatherly.ui.focusTimer.FocusTimerScreen
import com.android.gatherly.ui.friends.FriendsScreen
import com.android.gatherly.ui.homePage.HomePageScreen
import com.android.gatherly.ui.map.MapScreen

import com.android.gatherly.ui.navigation.NavigationActions
import com.android.gatherly.ui.navigation.Screen
import com.android.gatherly.ui.profile.ProfileScreen
import com.android.gatherly.ui.settings.SettingsScreen

/**
 * *B3 only*:
 *
 * Provide an OkHttpClient client for network requests.
 *
 * Property `client` is mutable for testing purposes.
 */
object HttpClientProvider {
    var client: OkHttpClient = OkHttpClient()
}

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    //private lateinit var authRepository: AuthRepository    todo : change the name with signIn repository

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
        GatherlyAppTheme { Surface(modifier = Modifier.fillMaxSize()) { GatherlyApp() } }

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
        if (FirebaseAuth.getInstance().currentUser == null) Screen.Login.name
        else Screen.HomePage.route

    NavHost(navController = navController, startDestination = startDestination) {


        // SIGNIN COMPOSABLE  ------------------------------
        navigation(
            startDestination = Screen.Login.route,
            route = Screen.Login.name,
        ) {
            composable(Screen.Login.route) {
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
                    onSignedOut = { navigationActions.navigateTo(Screen.Login) }
                )
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
                    onSignedOut = { navigationActions.navigateTo(Screen.Login) }
                )
            }

            /*composable(Screen.OverviewToDo.route) {
                OverviewScreen(
                    onSelectTodo = { navigationActions.navigateTo(Screen.EditToDo(it.uid)) },
                    onAddTodo = { navigationActions.navigateTo(Screen.AddToDo) },
                    onSignedOut = { navigationActions.navigateTo(Screen.Login) },
                    navigationActions = navigationActions,
                    credentialManager = credentialManager)
            }
            composable(Screen.AddToDo.route) {
                AddTodoScreen(
                    onDone = { navigationActions.navigateTo(Screen.OverviewToDo) },
                    onGoBack = { navigationActions.goBack() })
            }
            composable(Screen.EditToDo.route) { navBackStackEntry ->
                // Get the Todo UID from the arguments
                val uid = navBackStackEntry.arguments?.getString("uid")

                // Create the EditToDoScreen with the Todo UID
                uid?.let {
                    EditToDoScreen(
                        onDone = { navigationActions.navigateTo(Screen.OverviewToDo) },
                        todoUid = it,
                        onGoBack = { navigationActions.goBack() })
                }
                    ?: run {
                        Log.e("EditToDoScreen", "ToDo UID is null")
                        Toast.makeText(context, "ToDo UID is null", Toast.LENGTH_SHORT).show()
                    }
            }

             */
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
                        onSignedOut = { navigationActions.navigateTo(Screen.Login) }
                )
            }
        }

        // TIMER COMPOSABLE  ------------------------------
        navigation(
            startDestination = Screen.FocusTimerInitScreen.route,
            route = Screen.FocusTimerInitScreen.name,
        ) {
            composable(Screen.FocusTimerInitScreen.route) {
                FocusTimerInitScreen(
                    navigationActions = navigationActions,
                    credentialManager = credentialManager,
                    onSignedOut = { navigationActions.navigateTo(Screen.Login) })
            }
            composable(Screen.FocusTimerScreen.route) {
                FocusTimerScreen(
                    navigationActions = navigationActions,
                    credentialManager = credentialManager,
                    onSignedOut = { navigationActions.navigateTo(Screen.Login) })
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
                    onSignedOut = { navigationActions.navigateTo(Screen.Login) })
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
                    onSignedOut = { navigationActions.navigateTo(Screen.Login) })
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
                    onSignedOut = { navigationActions.navigateTo(Screen.Login) })
            }
        }

        // FRIENDS COMPOSABLE  ------------------------------
        navigation(
            startDestination = Screen.FriendsScreen.route,
            route = Screen.FriendsScreen.name,
        ) {
            composable(Screen.FriendsScreen.route) {
                FriendsScreen(
                    navigationActions = navigationActions,
                    credentialManager = credentialManager,
                    onSignedOut = { navigationActions.navigateTo(Screen.Login) })
            }
        }
    }
}