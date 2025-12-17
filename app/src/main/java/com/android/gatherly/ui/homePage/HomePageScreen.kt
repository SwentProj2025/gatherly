package com.android.gatherly.ui.homePage

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.R
import com.android.gatherly.model.event.Event
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.ui.map.EventIcon
import com.android.gatherly.ui.map.ToDoIcon
import com.android.gatherly.ui.navigation.BottomNavigationMenu
import com.android.gatherly.ui.navigation.NavigationActions
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Screen
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu_HomePage
import com.android.gatherly.ui.profile.ProfilePictureWithStatus
import com.android.gatherly.utils.LoadingAnimation
import com.android.gatherly.utils.MapCoordinator
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
 * Root composable for the Home Page screen.
 *
 * Displays upcoming events, tasks, a mini map, friends section, and the focus timer section.
 *
 * @param homePageViewModel ViewModel providing UI state and updates.
 * @param navigationActions Optional navigation callbacks.
 * @param homePageScreenActions User interaction callbacks for this screen.
 * @param coordinator Coordinator used for map centering requests.
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
            SectionTitle(
                text = stringResource(id = R.string.homepage_upcoming_events_title),
                modifier =
                    Modifier.padding(horizontal = screenPadding)
                        .testTag(HomePageScreenTestTags.UPCOMING_EVENTS_TITLE)
                        .clickable { homePageScreenActions.onClickEventsTitle() })

            EventsAndFriendsSection(
                todos = uiState.displayableTodos,
                events = uiState.displayableEvents,
                onClickFriendsSection = homePageScreenActions.onClickFriendsSection,
                isAnon = uiState.isAnon,
                friends = uiState.friends,
                coordinator = coordinator,
                navigationActions = navigationActions)

            SectionTitle(
                text = stringResource(id = R.string.homepage_upcoming_tasks_title),
                modifier =
                    Modifier.padding(horizontal = screenPadding)
                        .testTag(HomePageScreenTestTags.UPCOMING_TASKS_TITLE)
                        .clickable { homePageScreenActions.onClickTodoTitle() })

            TaskList(
                modifier = Modifier.weight(1f),
                todos = uiState.todos,
                onClickTodoTitle = homePageScreenActions.onClickTodoTitle,
                onClickTodo = homePageScreenActions.onClickTodo)

            FocusSection(
                modifier = Modifier.padding(horizontal = screenPadding),
                onClick = homePageScreenActions.onClickFocusButton)

            Spacer(
                modifier =
                    Modifier.height(dimensionResource(id = R.dimen.spacing_between_fields_regular)))
          }
        }
      })
}

/**
 * Displays a section title with consistent typography and color.
 *
 * @param text Title text to display.
 * @param modifier Optional modifier for layout and interactions.
 */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {

  Text(
      text = text,
      color = MaterialTheme.colorScheme.onBackground,
      style = MaterialTheme.typography.titleLarge,
      modifier =
          modifier.padding(vertical = dimensionResource(id = R.dimen.homepage_section_spacing)))
}

/**
 * Displays the top section containing the mini map and friends section.
 *
 * @param todos List of todos displayed on the mini map.
 * @param events List of events displayed on the mini map.
 * @param onClickFriendsSection Callback when the friends section is clicked.
 * @param isAnon Whether the current user is anonymous.
 * @param friends List of user friends.
 * @param coordinator Coordinator used for map interactions.
 * @param navigationActions Optional navigation callbacks.
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
 * Displays a small Google Map with markers for todos or events.
 *
 * Defaults to a predefined location if no items have a location.
 *
 * @param todos Todos to display when in todo mode.
 * @param events Events to display when in event mode.
 * @param modifier Modifier controlling layout and size.
 * @param coordinator Coordinator used to request map centering.
 * @param navigationActions Optional navigation callbacks.
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
                        navigationActions?.navigateTo(Screen.MapScreen)
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
                        navigationActions?.navigateTo(Screen.MapScreen)
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
 * Displays a bordered, clickable friends section.
 *
 * Shows either an empty state or a list of friend avatars.
 *
 * @param modifier Optional modifier for layout and styling.
 * @param onClickFriendsSection Callback invoked when the section is clicked.
 * @param friends List of friends to display.
 */
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
 * Displays the list of upcoming tasks.
 *
 * Shows an empty-state message when no tasks are available.
 *
 * @param modifier Modifier controlling layout and available space.
 * @param todos List of todos to display.
 * @param onClickTodoTitle Callback invoked from the empty state.
 * @param onClickTodo Callback invoked when a task is clicked.
 */
@Composable
fun TaskList(
    modifier: Modifier = Modifier,
    todos: List<ToDo>,
    onClickTodoTitle: () -> Unit = {},
    onClickTodo: (ToDo) -> Unit = {}
) {
  Column(modifier = modifier.fillMaxWidth()) {
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
          modifier = Modifier.fillMaxSize().testTag(HomePageScreenTestTags.TASKS_LAZY_COLUMN)) {
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

/**
 * Displays a single task item with its description and navigation arrow.
 *
 * @param modifier Optional modifier for layout and testing.
 * @param text Task description text.
 * @param onClick Callback invoked when the item is clicked.
 */
@Composable
fun TaskItem(modifier: Modifier = Modifier, text: String, onClick: () -> Unit) {
  Surface(modifier = modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.background) {
    val paddingRegular = dimensionResource(id = R.dimen.padding_regular)
    Row(
        modifier =
            Modifier.clickable(onClick = onClick)
                .fillMaxWidth()
                .padding(
                    horizontal = paddingRegular,
                    vertical = dimensionResource(id = R.dimen.padding_small)),
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

/**
 * Displays the focus timer and action button.
 *
 * @param modifier Modifier applied to the timer text and button.
 * @param onClick Callback invoked when the focus button is pressed.
 */
@Composable
fun FocusSection(modifier: Modifier = Modifier, onClick: () -> Unit) {

  SectionTitle(
      text = stringResource(id = R.string.homepage_focus_section_title),
      modifier = modifier.testTag(HomePageScreenTestTags.FOCUS_TIMER_TEXT))

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
fun getFriendProfilePicTestTag(friendUid: String) =
    "${HomePageScreenTestTags.FRIEND_AVATAR_PREFIX}$friendUid"

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
        ProfilePictureWithStatus(
            profilePictureUrl = friend.profilePicture,
            statusTestTag = getFriendStatusTestTag(friend.uid),
            profilePictureTestTag = getFriendProfilePicTestTag(friend.uid),
            status = friend.status,
            size = dimensionResource(id = R.dimen.homepage_friend_profile_pic_size))
      }
    }
    Text(
        text = stringResource(R.string.homepage_friends_section_label),
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.bodySmall)
  }
}
