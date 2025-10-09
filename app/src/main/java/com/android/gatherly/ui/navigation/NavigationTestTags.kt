package com.android.gatherly.ui.navigation

object NavigationTestTags {
    const val BOTTOM_NAVIGATION_MENU = "BottomNavigationMenu"
    const val TOP_NAVIGATION_MENU = "TopNavigationMenu"
    const val GO_BACK_BUTTON = "GoBackButton"

    const val OVERVIEW_TAB = "OverviewTab"
    const val MAP_TAB = "MapTab"
    const val EVENTS_TAB = "EventsTab"
    const val HOMEPAGE_TAB = "HomePageTab"
    const val PROFILE_TAB = "ProfileTab"
    const val TIMER_TAB = "TimerTab"
    const val FRIENDS_TAB = "FriendsTab"

    const val SETTINGS_TAB = "SettingsButton"

    const val LOGOUT_TAB = "LogoutButton"

    const val DROPMENU = "DropMenu"


    fun getTabTestTag(tab: Tab): String =
        when (tab) {
            is Tab.Overview -> OVERVIEW_TAB
            is Tab.Map -> MAP_TAB
            is Tab.Events -> EVENTS_TAB
            is Tab.HomePage -> HOMEPAGE_TAB
            is Tab.Profile -> PROFILE_TAB
            is Tab.Timer -> TIMER_TAB
            is Tab.Settings -> SETTINGS_TAB
            is Tab.SignOut -> LOGOUT_TAB
            is Tab.Friends -> FRIENDS_TAB
        }
}