package com.android.gatherly.ui.navigation

/**
 * Object containing test tags for navigation components in the application. These tags are used for
 * UI testing to identify various navigation elements.
 */
object NavigationTestTags {
  const val BOTTOM_NAVIGATION_MENU = "BottomNavigationMenu"
  const val TOP_NAVIGATION_MENU = "TopNavigationMenu"
  const val GO_BACK_BUTTON = "GoBackButton"
  const val OVERVIEW_TAB = "OverviewTab"
  const val ADD_TODO_TAB = "AddToDoTab"
  const val EDIT_TODO_TAB = "EditToDoTab"
  const val MAP_TAB = "MapTab"
  const val EVENTS_TAB = "EventsTab"
  const val HOMEPAGE_TAB = "HomePageTab"
  const val NOTIFICATIONS_TAB = "NotificationsTab"
  const val FRIEND_REQUESTS_TAB = "FriendRequestsScreenTab"
  const val PROFILE_TAB = "ProfileTab"
  const val TIMER_TAB = "TimerTab"
  const val FRIENDS_TAB = "FriendsTab"
  const val SETTINGS_TAB = "SettingsButton"
  const val LOGOUT_TAB = "LogoutButton"
  const val DROP_MENU = "DropMenu"
  const val TOP_BAR_TITLE = "TopBarTitle"
  const val ADD_EVENT_TAB = "AddEventTab"
  const val ADD_GROUP_TAB = "AddGroupTab"
  const val EDIT_EVENT_TAB = "EditEventTab"
  const val FIND_FRIENDS_TAB = "FindFriendsTab"
  const val USER_PROFILE_TAB = "UserProfileTab"
  const val BADGE_TAB = "BadgeTab"
  const val FOCUS_TAB = "FocusTab"
  const val GROUP_OVERVIEW_TAB = "GroupOverviewTab"
  const val GROUP_INFO_TAB = "GroupInfoTab"
  const val EDIT_GROUP_TAB = "EditGroupTab"

  /**
   * Returns the test tag associated with the given [tab].
   *
   * @param tab The tab for which to get the test tag.
   * @return The test tag as a [String].
   */
  fun getTabTestTag(tab: Tab): String =
      when (tab) {
        is Tab.TodoOverview -> OVERVIEW_TAB
        is Tab.Map -> MAP_TAB
        is Tab.EventsOverview -> EVENTS_TAB
        is Tab.HomePage -> HOMEPAGE_TAB
        is Tab.Notifications -> NOTIFICATIONS_TAB
        is Tab.FriendRequests -> FRIEND_REQUESTS_TAB
        is Tab.Profile -> PROFILE_TAB
        is Tab.Timer -> TIMER_TAB
        is Tab.Settings -> SETTINGS_TAB
        is Tab.SignOut -> LOGOUT_TAB
        is Tab.Friends -> FRIENDS_TAB
        is Tab.AddTodo -> ADD_TODO_TAB
        is Tab.EditTodo -> EDIT_TODO_TAB
        is Tab.AddEvent -> ADD_EVENT_TAB
        is Tab.EditEvent -> EDIT_EVENT_TAB
        is Tab.FindFriends -> FIND_FRIENDS_TAB
        is Tab.AddGroup -> ADD_GROUP_TAB
        is Tab.Badge -> BADGE_TAB
        is Tab.UserProfile -> USER_PROFILE_TAB
        is Tab.FocusPoints -> FOCUS_TAB
        is Tab.GroupsOverview -> GROUP_OVERVIEW_TAB
        is Tab.GroupInfo -> GROUP_INFO_TAB
        is Tab.EditGroup -> EDIT_GROUP_TAB
      }
}
