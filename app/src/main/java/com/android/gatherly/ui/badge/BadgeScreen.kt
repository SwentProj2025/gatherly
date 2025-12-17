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
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.android.gatherly.R
import com.android.gatherly.model.badge.BadgeType
import com.android.gatherly.model.profile.ProfileRepositoryFirestore
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu_Goback
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

/**
 * Contains test tags used in [BadgeScreen] and its child composable for identifying UI elements
 * during Compose UI testing.
 */
object BadgeScreenTestTags {
  /** Test tag for the Badge Items */
  fun badgeTest(title: String): String {
    return "badge_$title"
  }

  const val TODO_TITLE = "todo_title"
  const val EVENT_TITLE = "event_title"
  const val FRIEND_TITLE = "friend_title"
  const val FOCUS_TITLE = "focus_title"
}

/**
 * The Badge screen displays a list all badges per type that the user obtained and if higher rank
 * are still obtainable, shows one blank badge to show the user that more can be done
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

  val verticalPadding = dimensionResource(R.dimen.badge_screen_vertical_padding)
  val horizontalPadding = dimensionResource(R.dimen.badge_screen_horizontal_padding)
  val spacerSmall = dimensionResource(R.dimen.badge_section_spacer_small)
  val spacerMedium = dimensionResource(R.dimen.badge_section_spacer_medium)
  val spacerLarge = dimensionResource(R.dimen.badge_section_spacer_large)

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
              contentPadding = PaddingValues(vertical = verticalPadding),
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(horizontal = horizontalPadding)
                      .padding(padding)) {

                // ---------------------- ToDos ----------------------
                item {
                  Text(
                      modifier = Modifier.testTag(BadgeScreenTestTags.TODO_TITLE),
                      text = stringResource(R.string.todos_badge_title),
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      textAlign = TextAlign.Left,
                      color = MaterialTheme.colorScheme.onBackground)
                  Spacer(modifier = Modifier.height(spacerSmall))
                  Spacer(modifier = Modifier.height(spacerMedium))
                }

                items(uiState.badgesByType[BadgeType.TODOS_CREATED].orEmpty()) { badgeUi ->
                  BadgeItem(badgeUi)
                }

                item { Spacer(modifier = Modifier.height(spacerLarge)) }

                items(uiState.badgesByType[BadgeType.TODOS_COMPLETED].orEmpty()) { badgeUi ->
                  BadgeItem(badgeUi)
                }

                // ---------------------- Events ----------------------
                item {
                  Spacer(modifier = Modifier.height(spacerLarge))
                  Text(
                      modifier = Modifier.testTag(BadgeScreenTestTags.EVENT_TITLE),
                      text = stringResource(R.string.events_badge_title),
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      textAlign = TextAlign.Left,
                      color = MaterialTheme.colorScheme.onBackground)
                  Spacer(modifier = Modifier.height(spacerSmall))
                }

                items(uiState.badgesByType[BadgeType.EVENTS_CREATED].orEmpty()) { badgeUi ->
                  BadgeItem(badgeUi)
                }

                item { Spacer(modifier = Modifier.height(spacerLarge)) }

                items(uiState.badgesByType[BadgeType.EVENTS_PARTICIPATED].orEmpty()) { badgeUi ->
                  BadgeItem(badgeUi)
                }

                // ---------------------- Friends ----------------------
                item {
                  Spacer(modifier = Modifier.height(spacerLarge))
                  Text(
                      modifier = Modifier.testTag(BadgeScreenTestTags.FRIEND_TITLE),
                      text = stringResource(R.string.friends_badge_title),
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      textAlign = TextAlign.Left,
                      color = MaterialTheme.colorScheme.onBackground)
                  Spacer(modifier = Modifier.height(spacerSmall))
                }

                items(uiState.badgesByType[BadgeType.FRIENDS_ADDED].orEmpty()) { badgeUi ->
                  BadgeItem(badgeUi)
                }

                // ---------------------- Focus Sessions ----------------------
                item {
                  Spacer(modifier = Modifier.height(spacerLarge))
                  Text(
                      modifier = Modifier.testTag(BadgeScreenTestTags.FOCUS_TITLE),
                      text = stringResource(R.string.focus_session_badge_title),
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      textAlign = TextAlign.Left,
                      color = MaterialTheme.colorScheme.onBackground)
                  Spacer(modifier = Modifier.height(spacerSmall))
                }

                items(uiState.badgesByType[BadgeType.FOCUS_SESSIONS_COMPLETED].orEmpty()) { badgeUi
                  ->
                  BadgeItem(badgeUi)
                }
              }
        }
      })
}

/**
 * Renders a single badge row inside a card.
 *
 * Uses surfaceVariant/onSurfaceVariant colors to match the design system and exposes a stable
 * testTag via [BadgeScreenTestTags.badgeTest] for UI testing.
 *
 * @param badgeUi UI model containing the badge title, description and icon resource.
 */
@Composable
fun BadgeItem(badgeUi: BadgeUI) {

  val cardCorner = dimensionResource(R.dimen.badge_card_corner_radius)
  val strokeWidth = dimensionResource(R.dimen.badge_card_stroke_width)
  val cardPadV = dimensionResource(R.dimen.badge_card_padding_vertical)
  val innerPad = dimensionResource(R.dimen.badge_card_inner_padding)
  val iconSize = dimensionResource(R.dimen.badge_icon_size)
  val rowSpacer = dimensionResource(R.dimen.badge_row_spacer)
  val textSpacer = dimensionResource(R.dimen.badge_text_spacer)

  Card(
      border = BorderStroke(strokeWidth, MaterialTheme.colorScheme.onSurfaceVariant),
      shape = RoundedCornerShape(cardCorner),
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant,
              contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
      modifier =
          Modifier.fillMaxWidth()
              .padding(vertical = cardPadV)
              .testTag(BadgeScreenTestTags.badgeTest(badgeUi.title))) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(rowSpacer),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Image(
              painter = painterResource(badgeUi.icon),
              contentDescription = badgeUi.title,
              modifier = Modifier.size(iconSize))

          Spacer(modifier = Modifier.size(innerPad))

          Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = badgeUi.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.size(textSpacer))
            Text(text = badgeUi.description, style = MaterialTheme.typography.bodyMedium)
          }
        }
      }
}
