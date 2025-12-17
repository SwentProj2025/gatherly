package com.android.gatherly.ui.homePage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.R
import com.android.gatherly.model.event.Event
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileStatus
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.ui.map.EventIcon
import com.android.gatherly.ui.map.ToDoIcon
import com.android.gatherly.ui.navigation.BottomNavigationMenu
import com.android.gatherly.ui.navigation.NavigationActions
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Screen
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu_HomePage
import com.android.gatherly.utils.LoadingAnimation
import com.android.gatherly.utils.MapCoordinator
import com.android.gatherly.utils.profilePicturePainter
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

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
  const val FRIEND_AVATAR_PREFIX = "friendAvatar_"
  const val FRIEND_STATUS_PREFIX = "friendStatus_"
  const val EMPTY_TASK_LIST_TEXT_BUTTON = "emptyTaskListTextButton"
  const val ADD_FRIENDS_ICON = "addFriendsIcon"
  const val ADD_FRIENDS_TEXT = "addFriendsText"
  const val FRIENDS_LAZY_COLUMN = "friendsLazyColumn"
  const val TASKS_LAZY_COLUMN = "tasksLazyColumn"
  const val MINIMAP_BUTTON = "miniMapButton"
}

/**
 * Generates a unique test tag for a taskItem
 *
 * @param todoUid The unique identifier of the todo
 * @return The generated test tag for the taskItem
 */
fun getTaskItemTestTag(todoUid: String) = "${HomePageScreenTestTags.TASK_ITEM_PREFIX}$todoUid"

/**
 * Generates a unique test tag for a friend's status
 *
 * @param friendUid The unique identifier of the friend.
 * @return The generated test tag for the friend's status.
 */
fun getFriendStatusTestTag(friendUid: String) =
    "${HomePageScreenTestTags.FRIEND_STATUS_PREFIX}$friendUid"

/**
 * Actions that can be performed on the HomePage Screen.
 *
 * @param onClickFocusButton Callback invoked when the focus button is clicked to start/stop a focus
 *   session.
 * @param onClickTodoTitle Callback invoked when the "Upcoming Tasks" title is clicked.
 * @param onClickFriendsSection Callback invoked when the friends section is clicked.
 * @param onClickTodo Callback invoked when a specific todo item is clicked. Takes a [ToDo] as a
 *   parameter.
 * @param onClickEventsTitle Callback invoked when the "Upcoming Events" title is clicked.
 */
data class HomePageScreenActions(
    val onClickFocusButton: () -> Unit = {},
    val onClickTodoTitle: () -> Unit = {},
    val onClickFriendsSection: () -> Unit = {},
    val onClickTodo: (ToDo) -> Unit = {},
    val onClickEventsTitle: () -> Unit = {},
)

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
    navigationActions: NavigationActions? = null,
    homePageScreenActions: HomePageScreenActions,
    coordinator: MapCoordinator,
) {

  val lifecycle = LocalLifecycleOwner.current.lifecycle

  LaunchedEffect(lifecycle) {
    lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) { homePageViewModel.updateUI() }
  }
  val uiState by homePageViewModel.uiState.collectAsState()

  val screenPadding = dimensionResource(id = R.dimen.padding_screen)
  val verticalSpacing = dimensionResource(id = R.dimen.spacing_between_fields_medium)
  val sectionSpacing = dimensionResource(id = R.dimen.homepage_section_spacing)

  Scaffold(
      topBar = {
        TopNavigationMenu_HomePage(
            selectedTab = Tab.HomePage,
            onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) })
      },
      bottomBar = {
        BottomNavigationMenu(
            selectedTab = Tab.HomePage,
            onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
            modifier = Modifier.testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU))
      },
      content = { paddingValues ->
        if (uiState.isLoading) {
          LoadingAnimation(stringResource(R.string.loading_homepage_message), paddingValues)
        } else {
          Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Spacer(modifier = Modifier.height(verticalSpacing))

            SectionTitle(
                text = stringResource(id = R.string.homepage_upcoming_events_title),
                modifier =
                    Modifier.padding(horizontal = screenPadding)
                        .testTag(HomePageScreenTestTags.UPCOMING_EVENTS_TITLE)
                        .clickable { homePageScreenActions.onClickEventsTitle() })

            Spacer(modifier = Modifier.height(sectionSpacing))

            EventsAndFriendsSection(
                todos = uiState.displayableTodos,
                events = uiState.displayableEvents,
                onClickFriendsSection = homePageScreenActions.onClickFriendsSection,
                isAnon = uiState.isAnon,
                friends = uiState.friends,
                coordinator = coordinator,
                navigationActions = navigationActions)

            Spacer(modifier = Modifier.height(verticalSpacing))

            SectionTitle(
                text = stringResource(id = R.string.homepage_upcoming_tasks_title),
                modifier =
                    Modifier.padding(horizontal = screenPadding)
                        .testTag(HomePageScreenTestTags.UPCOMING_TASKS_TITLE)
                        .clickable { homePageScreenActions.onClickTodoTitle() })

            Spacer(modifier = Modifier.height(sectionSpacing))

            TaskList(
                todos = uiState.todos,
                homePageScreenActions.onClickTodoTitle,
                homePageScreenActions.onClickTodo)

            Spacer(modifier = Modifier.height(verticalSpacing))

            Spacer(modifier = Modifier.weight(1f))
            FocusSection(
                modifier = Modifier.padding(horizontal = screenPadding),
                timerString = uiState.timerString,
                onClick = homePageScreenActions.onClickFocusButton)

            Spacer(
                modifier =
                    Modifier.height(dimensionResource(id = R.dimen.spacing_between_fields_regular)))
          }
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
    onClickFriendsSection: () -> Unit,
    isAnon: Boolean,
    friends: List<Profile>,
    coordinator: MapCoordinator,
    navigationActions: NavigationActions? = null
) {

  val spacingRegular = dimensionResource(id = R.dimen.spacing_between_fields_regular)
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .height(dimensionResource(id = R.dimen.homepage_events_section_height))) {
        Spacer(modifier = Modifier.width(spacingRegular))

        MiniMap(
            todos = todos,
            events = events,
            modifier = Modifier.weight(0.8f),
            coordinator = coordinator,
            navigationActions = navigationActions)

        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_between_fields)))

        if (!isAnon) {
          FriendsSection(
              onClickFriendsSection = onClickFriendsSection,
              friends = friends,
              modifier = Modifier.weight(0.2f))
          Spacer(modifier = Modifier.width(spacingRegular))
        }
      }
}

/**
 * Displays a small, zoomable Google Map showing markers for todos and events. Defaults to the EPFL
 * campus if no locations are available.
 */
@Composable
fun MiniMap(
    todos: List<ToDo>,
    events: List<Event>,
    modifier: Modifier = Modifier,
    coordinator: MapCoordinator,
    navigationActions: NavigationActions? = null
) {
  val defaultLoc = LatLng(46.5191, 6.5668) // EPFL campus loc
  val firstEventLoc = events.firstOrNull()?.location?.let { LatLng(it.latitude, it.longitude) }

  val firstTodoLoc = todos.firstOrNull()?.location?.let { LatLng(it.latitude, it.longitude) }

  val startLoc =
      when {
        firstEventLoc != null -> firstEventLoc
        firstTodoLoc != null -> firstTodoLoc
        else -> defaultLoc
      }

  val cameraState = rememberCameraPositionState {
    position = CameraPosition.fromLatLngZoom(startLoc, 15f)
  }
  var showEvents by remember { mutableStateOf(false) }

  Box(
      modifier =
          modifier
              .fillMaxSize()
              .clip(RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_shape_medium)))
              .testTag(HomePageScreenTestTags.MINI_MAP_CARD)) {
        GoogleMap(
            cameraPositionState = cameraState,
            modifier = Modifier.fillMaxSize(),
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings =
                MapUiSettings(myLocationButtonEnabled = false, zoomControlsEnabled = false)) {

              // ---------- TODOS ----------
              if (showEvents) {
                todos.forEach { todo ->
                  val loc = todo.location ?: return@forEach

                  MarkerComposable(
                      state = rememberMarkerState(position = LatLng(loc.latitude, loc.longitude)),
                      onClick = {
                        coordinator.requestCenterOnTodo(todo.uid)
                        navigationActions?.navigateTo(Screen.Map)
                        true
                      }) {
                        ToDoIcon(todo)
                      }
                }
              }

              // ---------- EVENTS ----------
              if (!showEvents) {
                events.forEach { event ->
                  val loc = event.location ?: return@forEach

                  MarkerComposable(
                      state = rememberMarkerState(position = LatLng(loc.latitude, loc.longitude)),
                      onClick = {
                        coordinator.requestCenterOnEvent(event.id)
                        navigationActions?.navigateTo(Screen.Map)
                        true
                      }) {
                        EventIcon(event)
                      }
                }
              }
            }

        // ---------- Bottom-left Toggle Button ----------
        Box(
            modifier =
                Modifier.padding(dimensionResource(id = R.dimen.padding_small_regular))
                    .align(Alignment.BottomStart)) {
              FilterIconButton(isTodo = showEvents, onClick = { showEvents = !showEvents })
            }
      }
}

/**
 * A circular toggle button used to switch between two filter modes (e.g. todos vs. events).
 *
 * @param isTodo Drives which icon is shown and what color should the button be.
 * @param onClick Callback invoked when the user taps the button.
 */
@Composable
fun FilterIconButton(isTodo: Boolean, onClick: () -> Unit) {
  val backgroundColor =
      if (isTodo) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
  Card(
      modifier =
          Modifier.size(dimensionResource(id = R.dimen.homepage_filter_icon_button_size))
              .clickable { onClick() }
              .testTag(HomePageScreenTestTags.MINIMAP_BUTTON),
      shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_shape_medium)),
      elevation =
          CardDefaults.cardElevation(
              dimensionResource(id = R.dimen.homepage_filter_card_elevation)),
      colors =
          CardDefaults.cardColors(
              containerColor = backgroundColor,
              contentColor = MaterialTheme.colorScheme.onPrimaryContainer)) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Icon(
              imageVector = if (isTodo) Icons.Default.Event else Icons.AutoMirrored.Filled.List,
              contentDescription =
                  stringResource(id = R.string.homepage_map_button_icon_description))
        }
      }
}

/**
 * Circular avatar for a friend profile.
 *
 * @param profilePicUrl url to the picture data
 */
@Composable
fun FriendAvatar(
    modifier: Modifier = Modifier,
    profilePicUrl: String? = null,
    status: ProfileStatus,
    statusTag: String
) {
  val size = dimensionResource(id = R.dimen.homepage_friend_profile_pic_size)
  Box(modifier = modifier.size(size)) {
    Image(
        painter = profilePicturePainter(profilePicUrl),
        contentDescription = stringResource(id = R.string.homepage_profile_image_description),
        modifier = Modifier.fillMaxSize().clip(CircleShape),
        contentScale = ContentScale.Crop)

    StatusIndicator(
        status = status,
        modifier = Modifier.align(Alignment.BottomEnd).testTag(statusTag),
        size = size * 0.25f)
  }
}

/** Displays a bordered section with friend avatars and a label. The entire section is clickable. */
@Composable
fun FriendsSection(
    modifier: Modifier = Modifier,
    onClickFriendsSection: () -> Unit,
    friends: List<Profile>
) {

  val roundedCornerPercentage = 50
  val arrangement = if (friends.isEmpty()) Arrangement.Bottom else Arrangement.SpaceBetween
  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = arrangement,
      modifier =
          modifier
              .testTag(HomePageScreenTestTags.FRIENDS_SECTION)
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
                  horizontal = dimensionResource(id = R.dimen.padding_small))
              .fillMaxHeight()) {
        if (friends.isEmpty()) {
          EmptyFriendsView()
        } else {
          PopulatedFriendsView(friends)
        }
      }
}

/**
 * Displays a vertical list of task items. Each [TaskItem] has a test tag for easier identification
 * in tests.
 */
@Composable
fun TaskList(
    todos: List<ToDo>,
    onClickTodoTitle: () -> Unit = {},
    onClickTodo: (ToDo) -> Unit = {}
) {
  Column(
      modifier =
          Modifier.height(dimensionResource(id = R.dimen.homepage_task_section_height))
              .fillMaxWidth()) {
        if (todos.isEmpty()) {
          TextButton(
              onClick = onClickTodoTitle,
              modifier =
                  Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_small))
                      .testTag(HomePageScreenTestTags.EMPTY_TASK_LIST_TEXT_BUTTON)) {
                Text(
                    text = stringResource(id = R.string.homepage_empty_task_list_message),
                    color = MaterialTheme.colorScheme.onBackground)
              }
        } else {

          LazyColumn(
              modifier = Modifier.weight(1f).testTag(HomePageScreenTestTags.TASKS_LAZY_COLUMN)) {
                items(todos.size) { index ->
                  val todo = todos[index]
                  TaskItem(
                      modifier = Modifier.testTag(getTaskItemTestTag(todo.uid)),
                      text = todo.description,
                      onClick = { onClickTodo(todo) })
                }
              }
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

/**
 * Generates a unique test tag for a friend's avatar.
 *
 * @param friendUid The unique identifier of the friend.
 * @return The generated test tag for the friend's avatar.
 */
fun getFriendAvatarTestTag(friendUid: String) =
    "${HomePageScreenTestTags.FRIEND_AVATAR_PREFIX}$friendUid"

/**
 * Small colored status dot used to represent a user's presence state. (Green = Online, Red =
 * Offline, Blue = Focused)
 *
 * @param status The current [ProfileStatus] to display.
 * @param modifier Optional modifier for positioning.
 * @param size The diameter of the indicator.
 */
@Composable
fun StatusIndicator(status: ProfileStatus, modifier: Modifier = Modifier, size: Dp) {
  val color =
      when (status) {
        ProfileStatus.ONLINE -> Color.Green
        ProfileStatus.FOCUSED -> Color.Blue
        ProfileStatus.OFFLINE -> Color.Red
      }

  Box(modifier = modifier.size(size).clip(CircleShape).background(color))
}

/**
 * Displays the empty state of the friends section.
 *
 * Shows a "add friend" icon and a message prompting the user to add friends.
 */
@Composable
private fun EmptyFriendsView() {
  Icon(
      imageVector = Icons.Default.PersonAdd,
      contentDescription = stringResource(id = R.string.homepage_add_friend_icon_description),
      tint = MaterialTheme.colorScheme.onBackground,
      modifier =
          Modifier.size(dimensionResource(id = R.dimen.homepage_add_friends_icon_size))
              .testTag(HomePageScreenTestTags.ADD_FRIENDS_ICON))
  Text(
      text = stringResource(id = R.string.homepage_no_friends_message),
      style = MaterialTheme.typography.bodySmall,
      textAlign = TextAlign.Center,
      modifier = Modifier.testTag(HomePageScreenTestTags.ADD_FRIENDS_TEXT))
}

/**
 * Displays the populated state of the friends section.
 *
 * Shows a vertical column of friend avatars and a label at the bottom.
 *
 * @param friends List of [Profile] representing the user's friends.
 */
@Composable
private fun PopulatedFriendsView(friends: List<Profile>) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    LazyColumn(
        modifier = Modifier.weight(1f).testTag(HomePageScreenTestTags.FRIENDS_LAZY_COLUMN),
        verticalArrangement =
            Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_between_fields)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      items(friends.size) { index ->
        val friend = friends[index]
        FriendAvatar(
            profilePicUrl = friend.profilePicture,
            modifier = Modifier.testTag(getFriendAvatarTestTag(friend.uid)),
            status = friend.status,
            statusTag = getFriendStatusTestTag(friend.uid))
      }
    }
    Text(
        text = stringResource(R.string.homepage_friends_section_label),
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.bodySmall)
  }
}
