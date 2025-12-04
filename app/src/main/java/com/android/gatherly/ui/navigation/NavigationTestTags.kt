package com.android.gatherly.ui.navigation

object NavigationTestTags {
  const val BOTTOM_NAVIGATION_MENU = "BottomNavigationMenu"
  const val TOP_NAVIGATION_MENU = "TopNavigationMenu"
  const val GO_BACK_BUTTON = "GoBackButton"

  const val OVERVIEW_TAB = "OverviewTab"

  const val ADDTODO_TAB = "AddToDoTab"
  const val EDITTODO_TAB = "EditToDoTab"
  const val MAP_TAB = "MapTab"
  const val EVENTS_TAB = "EventsTab"
  const val HOMEPAGE_TAB = "HomePageTab"
  const val NOTIFICATIONS_TAB = "NotificationsTab"
  const val PROFILE_TAB = "ProfileTab"
  const val TIMER_TAB = "TimerTab"
  const val FRIENDS_TAB = "FriendsTab"

  const val SETTINGS_TAB = "SettingsButton"

  const val LOGOUT_TAB = "LogoutButton"

  const val DROPMENU = "DropMenu"

  const val TOP_BAR_TITLE = "TopBarTitle"

  const val ADDEVENT_TAB = "AddEventTab"
  const val ADDGROUP_TAB = "AddGroupTab"
  const val EDITEVENT_TAB = "EditEventTab"
  const val FINDFRIENDS_TAB = "FindFriendsTab"

  const val BADGE_TAB = "BadgeTab"
  const val FOCUS_TAB = "FocusTab"

  fun getTabTestTag(tab: Tab): String =
      when (tab) {
        is Tab.Overview -> OVERVIEW_TAB
        is Tab.Map -> MAP_TAB
        is Tab.Events -> EVENTS_TAB
        is Tab.HomePage -> HOMEPAGE_TAB
        is Tab.Notifications -> NOTIFICATIONS_TAB
        is Tab.Profile -> PROFILE_TAB
        is Tab.Timer -> TIMER_TAB
        is Tab.Settings -> SETTINGS_TAB
        is Tab.SignOut -> LOGOUT_TAB
        is Tab.Friends -> FRIENDS_TAB
        is Tab.AddTodo -> ADDTODO_TAB
        is Tab.EditTodo -> EDITTODO_TAB
        is Tab.AddEvent -> ADDEVENT_TAB
        is Tab.EditEvent -> EDITEVENT_TAB
        is Tab.FindFriends -> FINDFRIENDS_TAB
        is Tab.AddGroup -> ADDGROUP_TAB
        is Tab.Badge -> BADGE_TAB
        is Tab.FocusPoints -> FOCUS_TAB
      }
}
