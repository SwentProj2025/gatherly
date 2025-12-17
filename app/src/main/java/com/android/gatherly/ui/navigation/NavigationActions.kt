package com.android.gatherly.ui.navigation

import androidx.navigation.NavHostController

/**
 * Sealed class representing different screens in the application. Each screen has a route, a name,
 * and a flag indicating if it's a top-level destination.
 *
 * @param route The navigation route for the screen
 * @param name The display name of the screen
 * @param isTopLevelDestination Boolean indicating if the screen is a top-level destination
 */
sealed class Screen(
    val route: String,
    val name: String,
    val isTopLevelDestination: Boolean = false
) {

  /* Authentication Related Screens */
  object SignInScreen : Screen(route = "sign_in_screen", name = "Authentification")

  object InitProfileScreen : Screen(route = "init_profile_screen", name = "Complete your profile")

  /* Main Home page Screen */
  object HomePageScreen :
      Screen(route = "home_page_screen", name = "Home Page", isTopLevelDestination = true)

  /* Notifications Screen */
  object NotificationsScreen : Screen(route = "notifications_screen", name = "Notifications")

  /* Map Screen */
  object MapScreen : Screen(route = "map_screen", name = "Map")

  /* Focus Timer Screen */
  object FocusTimerScreen : Screen(route = "focus_timer_screen", name = "Focus Timer")

  /* To-Do Related Screens */
  object ToDoOverviewScreen : Screen(route = "todo_overview_screen", name = "To-Do Overview")

  object AddToDoScreen : Screen(route = "add_todo_screen", name = "Create a new To-Do")

  object EditTodoRootScreen : Screen(route = "edit_todo_screen", name = "Edit a To-Do")

  data class EditToDoScreen(val todoUid: String) :
      Screen(route = "edit_todo_screen/${todoUid}", name = "Edit a ToDo") {
    companion object {
      const val ROUTE = "edit_todo_screen/{uid}"
    }
  }

  /* Event Related Screens */
  object EventsOverviewScreen : Screen(route = "events_overview_screen", name = "Events Overview")

  data class EventsDetailsScreen(val eventUid: String) :
      Screen(route = "event_details/${eventUid}", name = "Event Details") {
    companion object {
      const val ROUTE = "event_details/{uid}"
    }
  }

  object AddEventScreen : Screen(route = "add_event_screen", name = "Create an event")

  object EditEventRootScreen : Screen(route = "edit_event_screen", name = "Edit an event")

  data class EditEvent(val eventUid: String) :
      Screen(route = "edit_event_screen/${eventUid}", name = "Edit an Event") {
    companion object {
      const val ROUTE = "edit_event_screen/{uid}"
    }
  }

  /* Profile Related Screens */
  object ProfileScreen : Screen(route = "profile_screen", name = "Your profile")

  object SettingsScreen : Screen(route = "settings_screen", name = "Settings")

  object UserProfileRootScreen : Screen(route = "user_profile_screen", name = "User Profile")

  data class UserProfileScreen(val uid: String) :
      Screen(route = "user_profile_screen/$uid", name = "User Profile") {
    companion object {
      const val ROUTE = "user_profile_screen/{uid}"
    }
  }

  /* Friends Related Screens */
  object FriendRequestsScreen : Screen(route = "friend_requests_screen", name = "Friend Requests")

  object FriendsScreen : Screen(route = "friends_screen", name = "Your friends")

  object FindFriendsScreen : Screen(route = "find_friends_screen", name = "Find new friends")

  /* Groups Related Screens */
  object OverviewGroupsScreen : Screen(route = "overview_groups_screen", name = "Groups Overview")

  object AddGroupScreen : Screen(route = "add_group_screen", name = "Add a New Group")

  object GroupsInfoRootScreen : Screen(route = "groups_info_screen", name = "Group information")

  data class GroupInfoScreen(val groupUid: String) :
      Screen(route = "group_info_screen/${groupUid}", name = "Group Information") {
    companion object {
      const val ROUTE = "group_info_screen/{uid}"
    }
  }

  object EditGroupRootScreen : Screen(route = "edit_group_screen", name = "Edit group")

  data class EditGroupScreen(val groupUid: String) :
      Screen(route = "edit_group_screen/${groupUid}", name = "Edit Group") {
    companion object {
      const val ROUTE = "edit_group_screen/{uid}"
    }
  }

  /* Badges Screen */
  object BadgeScreen : Screen(route = "badge_screen", name = "Badges")

  /* Focus History Screen */
  object FocusScreen : Screen(route = "focus_history_screen", name = "Focus History")
}

/**
 * Class that handles navigation actions using a NavHostController.
 *
 * @param navController The NavHostController used for navigation
 */
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
      if (screen !is Screen.SignInScreen) {
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
