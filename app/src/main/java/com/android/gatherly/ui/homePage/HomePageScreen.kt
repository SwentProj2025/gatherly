package com.android.gatherly.ui.homePage

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.R
import com.android.gatherly.model.event.Event
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.ui.navigation.BottomNavigationMenu
import com.android.gatherly.ui.navigation.HandleSignedOutState
import com.android.gatherly.ui.navigation.NavigationActions
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu_HomePage
import com.android.gatherly.ui.theme.GatherlyTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

// The documentation in this file was generated with the help of ChatGPT.

/**
 * Test tag constants for the Home Page screen. Used in Compose UI tests to locate and assert
 * specific components.
 */
object HomePageScreenTestTags {
  const val UPCOMING_EVENTS_TITLE = "upcomingEventsTitle"
  const val UPCOMING_TASKS_TITLE = "upcomingTasksTitle"
  const val FOCUS_TIMER_TEXT = "focusTimerText"
  const val FOCUS_BUTTON = "focusButton"
  const val TASK_ITEM_PREFIX = "taskItem_"
  const val FRIENDS_SECTION = "friendsSection"
  const val MINI_MAP_CARD = "miniMapCard"
}

/**
 * Main Home Page screen composable.
 *
 * Displays:
 * - Upcoming events and tasks
 * - A mini-map with markers
 * - Friends section
 * - Focus timer section
 *
 * Integrates data from [HomePageViewModel] and provides test tags for UI tests.
 */
@Composable
fun HomePageScreen(
    homePageViewModel: HomePageViewModel = viewModel(),
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current),
    onSignedOut: () -> Unit = {},
    navigationActions: NavigationActions? = null,
    onClickFocusButton: () -> Unit = {},
    onClickTodo: () -> Unit = {},
    onClickFriendsSection: () -> Unit = {},
) {
  val uiState by homePageViewModel.uiState.collectAsState()

  HandleSignedOutState(uiState.signedOut, onSignedOut)

  val screenPadding = dimensionResource(id = R.dimen.padding_screen)
  val verticalSpacing = dimensionResource(id = R.dimen.spacing_between_fields_medium)
  val sectionSpacing = dimensionResource(id = R.dimen.homepage_section_spacing)

  Scaffold(
      topBar = {
        TopNavigationMenu_HomePage(
            selectedTab = Tab.HomePage,
            onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
            onSignedOut = { homePageViewModel.signOut(credentialManager) })
      },
      bottomBar = {
        BottomNavigationMenu(
            selectedTab = Tab.HomePage,
            onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
            modifier = Modifier.testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU))
      },
      content = { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
          Spacer(modifier = Modifier.height(verticalSpacing))

          SectionTitle(
              text = stringResource(id = R.string.homepage_upcoming_events_title),
              modifier =
                  Modifier.padding(horizontal = screenPadding)
                      .testTag(HomePageScreenTestTags.UPCOMING_EVENTS_TITLE))

          Spacer(modifier = Modifier.height(sectionSpacing))

          EventsAndFriendsSection(
              todos = uiState.displayableTodos,
              events = uiState.displayableEvents,
              onClickFriendsSection = onClickFriendsSection)

          Spacer(modifier = Modifier.height(verticalSpacing))

          SectionTitle(
              text = stringResource(id = R.string.homepage_upcoming_tasks_title),
              modifier =
                  Modifier.padding(horizontal = screenPadding)
                      .testTag(HomePageScreenTestTags.UPCOMING_TASKS_TITLE))

          Spacer(modifier = Modifier.height(sectionSpacing))

          TaskList(todos = uiState.todos, onClickTodo)

          Spacer(modifier = Modifier.height(verticalSpacing))

          Spacer(modifier = Modifier.weight(1f))
          FocusSection(
              modifier = Modifier.padding(horizontal = screenPadding),
              timerString = uiState.timerString,
              onClick = onClickFocusButton)

          Spacer(
              modifier =
                  Modifier.height(dimensionResource(id = R.dimen.spacing_between_fields_regular)))
        }
      })
}

/** Simple reusable section title used throughout the Home Page. */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {

  Text(
      text = text,
      color = MaterialTheme.colorScheme.onBackground,
      fontSize = 20.sp,
      modifier = modifier)
}

/**
 * Displays the top half of the screen:
 * - Mini map of nearby todos/events
 * - Friends section
 */
@Composable
fun EventsAndFriendsSection(
    todos: List<ToDo>,
    events: List<Event>,
    onClickFriendsSection: () -> Unit
) {

  val spacingRegular = dimensionResource(id = R.dimen.spacing_between_fields_regular)
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .height(dimensionResource(id = R.dimen.homepage_events_section_height))) {
        Spacer(modifier = Modifier.width(spacingRegular))

        MiniMap(todos = todos, events = events)

        Spacer(modifier = Modifier.width(spacingRegular))

        FriendsSection(onClickFriendsSection = onClickFriendsSection)

        Spacer(modifier = Modifier.width(spacingRegular))
      }
}

/**
 * Displays a small, zoomable Google Map showing markers for todos and events. Defaults to the EPFL
 * campus if no locations are available.
 */
@Composable
fun MiniMap(todos: List<ToDo>, events: List<Event>) {
  val defaultLoc = LatLng(46.5191, 6.5668) // EPFL campus loc
  val firstTodoLoc =
      todos.firstOrNull()?.location?.let { LatLng(it.latitude, it.longitude) } ?: defaultLoc

  val cameraPositionState = rememberCameraPositionState {
    position = CameraPosition.fromLatLngZoom(firstTodoLoc, 15f)
  }

  Card(
      modifier =
          Modifier.width(dimensionResource(id = R.dimen.homepage_minimap_width))
              .fillMaxHeight()
              .testTag(HomePageScreenTestTags.MINI_MAP_CARD),
      shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_shape_medium)),
  ) {
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings =
            MapUiSettings(
                zoomControlsEnabled = false,
                scrollGesturesEnabled = true,
                zoomGesturesEnabled = true,
                tiltGesturesEnabled = false,
            ),
        properties = MapProperties(isMyLocationEnabled = false)) {
          todos.forEach { todo ->
            val loc = todo.location ?: return@forEach
            Marker(
                state = MarkerState(LatLng(loc.latitude, loc.longitude)),
                title = todo.name,
                snippet = todo.description)
          }
          events.forEach { event ->
            val loc = event.location ?: return@forEach
            Marker(
                state = MarkerState(LatLng(loc.latitude, loc.longitude)),
                title = event.title,
                snippet = event.description)
          }
        }
  }
}

/**
 * Circular avatar for a friend profile. Currently uses a default drawable until profile pictures
 * are supported.
 */
@Composable
fun FriendAvatar(
    modifier: Modifier = Modifier,
) {
  val size = dimensionResource(id = R.dimen.homepage_friend_profile_pic_size)
  Box(modifier = modifier.size(size)) {
    Image( // Currently a placeholder image, will be implemented when profile picture storage is
        // merged to main
        painter = painterResource(id = R.drawable.default_profile_picture),
        contentDescription = stringResource(id = R.string.homepage_profile_image_description),
        modifier = Modifier.fillMaxSize().clip(CircleShape),
        contentScale = ContentScale.Crop)
  }
}

/** Displays a bordered section with friend avatars and a label. The entire section is clickable. */
@Composable
fun FriendsSection(onClickFriendsSection: () -> Unit) {

  val friendCount = 3
  val roundedCornerPercentage = 50
  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier =
          Modifier.testTag(HomePageScreenTestTags.FRIENDS_SECTION)
              .border(
                  width = dimensionResource(id = R.dimen.homepage_friends_section_border_width),
                  color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                  shape = RoundedCornerShape(percent = roundedCornerPercentage))
              .clip(
                  RoundedCornerShape(
                      percent =
                          roundedCornerPercentage)) // Ensures children stay within border radius
              .clickable { onClickFriendsSection() }
              .padding(
                  vertical =
                      dimensionResource(
                          id = R.dimen.homepage_friends_section_vertical_border_padding),
                  horizontal = dimensionResource(id = R.dimen.padding_small))) {
        Column(
            verticalArrangement =
                Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_between_fields))) {
              repeat(friendCount) { FriendAvatar() }
            }

        Text(
            text = stringResource(R.string.homepage_friends_section_label),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodySmall)
      }
}

/**
 * Displays a vertical list of task items. Each [TaskItem] has a test tag for easier identification
 * in tests.
 */
@Composable
fun TaskList(todos: List<ToDo>, onClickTodo: () -> Unit = {}) {
  Column {
    todos.forEach { todo ->
      TaskItem(
          modifier = Modifier.testTag("${HomePageScreenTestTags.TASK_ITEM_PREFIX}${todo.uid}"),
          text = todo.description,
          onClick = onClickTodo)
    }
  }
}

/** A single clickable task item with a description and arrow icon. */
@Composable
fun TaskItem(modifier: Modifier = Modifier, text: String, onClick: () -> Unit) {
  Surface(modifier = modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.background) {
    val paddingRegular = dimensionResource(id = R.dimen.padding_regular)
    Row(
        modifier =
            Modifier.clickable(onClick = onClick)
                .fillMaxWidth()
                .padding(horizontal = paddingRegular, vertical = paddingRegular),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
          Text(
              text = text,
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.onBackground,
              modifier = Modifier.weight(1f))

          Icon(
              imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
              contentDescription = stringResource(id = R.string.homepage_arrow_icon_description),
              tint = MaterialTheme.colorScheme.onBackground,
              modifier = Modifier.size(dimensionResource(R.dimen.homepage_arrow_icon_size)))
        }
  }
}

/** Displays the focus timer text and button used for starting focus sessions. */
@Composable
fun FocusSection(modifier: Modifier = Modifier, timerString: String = "", onClick: () -> Unit) {
  Text(
      text = timerString,
      color = MaterialTheme.colorScheme.onBackground,
      style = MaterialTheme.typography.bodyLarge,
      modifier = modifier.testTag(HomePageScreenTestTags.FOCUS_TIMER_TEXT))

  Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.homepage_section_spacing)))

  Button(
      onClick = onClick,
      modifier =
          modifier
              .fillMaxWidth()
              .height(dimensionResource(R.dimen.homepage_focus_button_height))
              .testTag(HomePageScreenTestTags.FOCUS_BUTTON),
      shape =
          RoundedCornerShape(dimensionResource(id = R.dimen.homepage_save_button_corner_radius)),
      colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
        Text(
            text = stringResource(id = R.string.homepage_focus_button_text),
            color = MaterialTheme.colorScheme.onSecondary,
            style = MaterialTheme.typography.titleMedium)
      }
}

@Preview(showBackground = true)
@Composable
fun HomePageScreenPreview() {
  GatherlyTheme(darkTheme = true) { HomePageScreen() }
}
