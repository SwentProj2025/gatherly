package com.android.gatherly.ui.navigation

import androidx.navigation.NavHostController

sealed class Screen(
    val route: String,
    val name: String,
    val isTopLevelDestination: Boolean = false
) {

  object SignIn : Screen(route = "sign_in", name = "Authentification")

  object HomePage : Screen(route = "home_page", name = "Home Page", isTopLevelDestination = true)

  object OverviewToDo : Screen(route = "overview", name = "Overview")

  object Map : Screen(route = "map", name = "Map")

  object AddToDo : Screen(route = "add_todo", name = "Create a new task")

  object Task : Screen(route = "edit_todo", name = "Edit a task")

  data class EditToDo(val todoUid: String) :
      Screen(route = "edit_todo/${todoUid}", name = "Edit ToDo") {
    companion object {
      const val route = "edit_todo/{uid}"
    }
  }

  object FocusTimerScreen : Screen(route = "focus_timer_screen", name = "Focus Timer")

  object EventsScreen : Screen(route = "events_screen", name = "Your events")

  data class EventsDetailsScreen(val eventUid: String) :
      Screen(route = "event_details/${eventUid}", name = "Event Details") {
    companion object {
      const val route = "event_details/{uid}"
    }
  }

  object AddEventScreen : Screen(route = "add_event_screen", name = "Create an event")

  object EditEventScreen : Screen(route = "edit_event", name = "Edit event")

  data class EditEvent(val eventUid: String) :
      Screen(route = "edit_event/${eventUid}", name = "Edit Event") {
    companion object {
      const val route = "edit_event/{uid}"
    }
  }

  object ProfileScreen : Screen(route = "profile_screen", name = "Your profile")

  object FriendsScreen : Screen(route = "friends_screen", name = "Your friends")

  object FindFriendsScreen : Screen(route = "find_friends_screen", name = "Find new friends")

  object SettingsScreen : Screen(route = "settings_screen", name = "Settings")

  object InitProfileScreen : Screen(route = "init_profile_screen", name = "Complete your profile")

  object AddGroupScreen : Screen(route = "add_group_screen", name = "Add a New Group")

  object UserProfileScreen : Screen(route = "user_profile", name = "User Profile")

  data class UserProfile(val uid: String) :
      Screen(route = "user_profile_screen/$uid", name = "User Profile") {
    companion object {
      const val route = "user_profile_screen/{uid}"
    }
  }
}

open class NavigationActions(
    private val navController: NavHostController,
) {
  /**
   * Navigate to the specified screen.
   *
   * @param screen The screen to navigate to
   */
  open fun navigateTo(screen: Screen) {
    if (screen.isTopLevelDestination && currentRoute() == screen.route) {
      // If the user is already on the top-level destination, do nothing
      return
    }
    navController.navigate(screen.route) {
      if (screen.isTopLevelDestination) {
        launchSingleTop = true
        popUpTo(screen.route) { inclusive = true }
      }
      if (screen !is Screen.SignIn) {
        // Restore state when reselecting a previously selected item
        restoreState = true
      }
    }
  }

  /** Navigate back to the previous screen. */
  open fun goBack() {
    navController.popBackStack()
  }

  /**
   * Get the current route of the navigation controller.
   *
   * @return The current route
   */
  open fun currentRoute(): String {
    return navController.currentDestination?.route ?: ""
  }
}
