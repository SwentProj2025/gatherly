package com.android.gatherly.ui.navigation

//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

sealed class Tab(val name: String, val icon: ImageVector, val destination: Screen) {
    object Timer : Tab("Timer", Icons.Outlined.Schedule, Screen.FocusTimerInitScreen)

    object Overview : Tab("To-Do", Icons.Outlined.FormatListBulleted, Screen.OverviewToDo)

    object Events : Tab("Events", Icons.Outlined.Group, Screen.EventsScreen)
    object Map : Tab("Map", Icons.Outlined.Place, Screen.Map)

    object HomePage : Tab("Home", Icons.Outlined.Home, Screen.HomePage)

    object Profil : Tab("your profile", Icons.Outlined.Place, Screen.ProfileScreen)


}

private val tabs =
    listOf(
        Tab.Overview,
        Tab.Map,
    )

@Composable
fun BottomNavigationMenu(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier =
            modifier.fillMaxWidth().height(60.dp).testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU),
        containerColor  = MaterialTheme.colorScheme.surface,
        content = {
            tabs.forEach { tab ->
                NavigationBarItem(
                    icon = { Icon(tab.icon, contentDescription = null) },
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