package com.android.gatherly.ui.homePage

import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
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
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

object HomePageScreenTestTags {
  const val UPCOMING_EVENTS_TITLE = "upcomingEventsTitle"
  const val UPCOMING_TASKS_TITLE = "upcomingTasksTitle"
  const val FOCUS_TIMER_TEXT = "focusTimerText"
  const val FOCUS_BUTTON = "focusButton"
  const val TASK_ITEM_PREFIX = "taskItem_"
}

@Composable
fun HomePageScreen(
    homePageViewModel: HomePageViewModel = viewModel(),
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current),
    onSignedOut: () -> Unit = {},
    navigationActions: NavigationActions? = null,
    onClickFocusButton: () -> Unit = {},
    onClickMap: () -> Unit = {},
    onClickTodo: () -> Unit = {},
) {
  val uiState by homePageViewModel.uiState.collectAsState()

  HandleSignedOutState(uiState.signedOut, onSignedOut)

  val screenPadding = dimensionResource(id = R.dimen.padding_screen)
  val verticalSpacing = dimensionResource(id = R.dimen.spacing_between_fields_medium)
  val sectionSpacing = dimensionResource(id = R.dimen.homepage_section_spacing)

  Scaffold( //  TODO BALA Screen Padding for all instead of in each children, spacers inside Titles
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
              onClickMap = onClickMap)

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

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {

  Text(
      text = text, color = MaterialTheme.colorScheme.primary, fontSize = 20.sp, modifier = modifier)
}

@Composable
fun EventsAndFriendsSection(todos: List<ToDo>, events: List<Event>, onClickMap: () -> Unit) {

  val spacingRegular = dimensionResource(id = R.dimen.spacing_between_fields_regular)
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .height(dimensionResource(id = R.dimen.homepage_events_section_height))) {
        Spacer(modifier = Modifier.width(spacingRegular))

        MiniMap(todos = todos, events = events, onClickMap = { onClickMap })

        Spacer(modifier = Modifier.width(spacingRegular))

        FriendsSection()

        Spacer(modifier = Modifier.width(spacingRegular))
      }
}

@Composable
fun MiniMap(todos: List<ToDo>, events: List<Event>, onClickMap: () -> Unit) {
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
              .clickable { onClickMap() },
      shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_shape_medium)),
  ) {
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings =
            MapUiSettings(
                zoomControlsEnabled = true,
                scrollGesturesEnabled = true,
                zoomGesturesEnabled = true,
                tiltGesturesEnabled = false,
            ),
        properties = MapProperties(isMyLocationEnabled = false)) {
          todos.forEach { todo ->
            val loc = todo.location ?: return@forEach
            Marker(
                state = MarkerState(LatLng(loc.latitude, loc.longitude)),
                icon = todoIcon(todo.name))
          }
        }
  }
}

/**
 * Creates a small rounded marker icon displaying a ToDo title.
 *
 * @param title the title of the toDo to render inside the marker icon.
 * @return A [BitmapDescriptor] representing the ToDo marker icon.
 */
@Composable
private fun todoIcon(title: String): BitmapDescriptor {
  val density = LocalDensity.current
  val primary = MaterialTheme.colorScheme.primary
  val onPrimary = MaterialTheme.colorScheme.onPrimary
  return remember(title) {
    // Text Style
    val textPaint =
        Paint().apply {
          color = onPrimary.toArgb()
          textSize = with(density) { 20.sp.toPx() }
        }

    // measures
    val bounds = Rect().also { textPaint.getTextBounds(title, 0, title.length, it) }
    val hPad = with(density) { 8f * density.density }
    val vPad = with(density) { 4f * density.density }
    val w = (bounds.width() + 2 * hPad).toInt().coerceAtLeast(1)
    val h = (bounds.height() + 2 * vPad).toInt().coerceAtLeast(1)

    // creates the bitmap and canvas to draw on
    val bmp = createBitmap(w, h)
    val c = Canvas(bmp)

    // Box
    val bg = Paint().apply { color = primary.toArgb() }
    c.drawRoundRect(RectF(0f, 0f, w.toFloat(), h.toFloat()), 12f, 12f, bg)

    // Text
    val baselineY = h / 2f + bounds.height() / 2f - bounds.bottom
    c.drawText(title, hPad, baselineY, textPaint)

    // creates the icon
    BitmapDescriptorFactory.fromBitmap(bmp)
  }
}

@Composable
fun FriendAvatar(
    // imageUrl: String,
    modifier: Modifier = Modifier,
) {
  val size = dimensionResource(id = R.dimen.homepage_friend_profile_pic_size)
  Box(modifier = modifier.size(size)) {
    // Avatar Image
    // AsyncImage( todo
    //  model = imageUrl,
    //  contentDescription = "Friend avatar",
    //  modifier = Modifier
    //    .fillMaxSize()
    //    .clip(CircleShape),
    //  contentScale = ContentScale.Crop
    // )
    Image(
        painter = painterResource(id = R.drawable.default_profile_picture),
        contentDescription = stringResource(id = R.string.homepage_profile_image_description),
        modifier = Modifier.fillMaxSize().clip(CircleShape),
        contentScale = ContentScale.Crop)
  }
}

@Composable
fun FriendsSection() {

  val friendCount = 3
  val roundedCornerPercentage = 50
  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier =
          Modifier.border(
                  width = dimensionResource(id = R.dimen.homepage_friends_section_border_width),
                  color = MaterialTheme.colorScheme.primary,
                  shape = RoundedCornerShape(percent = roundedCornerPercentage))
              .clip(
                  RoundedCornerShape(
                      percent =
                          roundedCornerPercentage)) // Ensures children stay within border radius
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
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodySmall)
      }
}

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
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.weight(1f))

          Icon(
              imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
              contentDescription = stringResource(id = R.string.homepage_arrow_icon_description),
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(dimensionResource(R.dimen.homepage_arrow_icon_size)))
        }
  }
}

@Composable
fun FocusSection(modifier: Modifier = Modifier, timerString: String = "", onClick: () -> Unit) {
  Text(
      text = timerString,
      color = MaterialTheme.colorScheme.primary,
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
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium)
      }
}

// @Preview(name = "Light Mode", showBackground = true)
// @Composable
// fun HomePageScreenLightPreview() {
//  GatherlyTheme(darkTheme = false) {
//    HomePageScreen()
//  }
// }

@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomePageScreenDarkPreview() {
  GatherlyTheme(darkTheme = true) { HomePageScreen() }
}
