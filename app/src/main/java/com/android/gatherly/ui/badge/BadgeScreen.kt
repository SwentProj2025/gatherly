package com.android.gatherly.ui.badge

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.gatherly.model.profile.ProfileRepositoryFirestore
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu_Goback
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

/**
 * Contains test tags used in [BadgeScreen] and its child composables for identifying UI elements
 * during Compose UI testing.
 */
object BadgeScreenTestTags {
  /** Test tag for the Badge Items */
  fun badgeTest(title: String): String {
    return "badge_$title"
  }
}

/**
 * The Badge screen displays a list of highest ranked badges per type that the user obtained
 *
 * @param goBack called when the back arrow of the top bar is clicked to go back to Profile Screen
 * @param viewModel The ViewModel managing the state and logic for the Badge screen
 */
@Composable
fun BadgeScreen(
    viewModel: BadgeViewModel =
        BadgeViewModel(
            repository = ProfileRepositoryFirestore(Firebase.firestore, Firebase.storage)),
    goBack: () -> Unit = {},
) {

  val uiState by viewModel.uiState.collectAsState()

  LaunchedEffect(Unit) { viewModel.refresh() }

  Scaffold(
      topBar = {
        TopNavigationMenu_Goback(
            selectedTab = Tab.Badge,
            modifier = Modifier.testTag(NavigationTestTags.TOP_NAVIGATION_MENU),
            goBack = goBack)
      },
      content = { padding ->
        if (uiState.isLoading) {
          Box(
              modifier = Modifier.fillMaxSize().padding(padding),
              contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
              }
        } else {
          LazyColumn(
              contentPadding = PaddingValues(vertical = 8.dp),
              modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(padding)) {
                items(uiState.topBadges.values.toList()) { badgeUi -> BadgeItem(badgeUi) }
              }
        }
      })
}

/** Displays a single Badge item inside a [Card] */
@Composable
fun BadgeItem(badgeUi: BadgeUI) {

  Card(
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant),
      shape = RoundedCornerShape(8.dp),
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant,
              contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
      modifier =
          Modifier.fillMaxWidth()
              .padding(vertical = 4.dp)
              .testTag(BadgeScreenTestTags.badgeTest(badgeUi.title))) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Image(
              painter = painterResource(badgeUi.icon),
              contentDescription = badgeUi.title,
              modifier = Modifier.size(48.dp))

          Spacer(modifier = Modifier.size(12.dp))

          Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = badgeUi.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.size(4.dp))
            Text(text = badgeUi.description, style = MaterialTheme.typography.bodyMedium)
          }
        }
      }
}
