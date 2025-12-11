package com.android.gatherly.ui.focusTimer

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.R
import com.android.gatherly.model.focusSession.FocusSession
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.ui.navigation.BottomNavigationMenu
import com.android.gatherly.ui.navigation.NavigationActions
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu
import com.android.gatherly.ui.theme.GatherlyTheme
import com.android.gatherly.ui.theme.theme_leaderboard_bronze
import com.android.gatherly.ui.theme.theme_leaderboard_gold
import com.android.gatherly.ui.theme.theme_leaderboard_silver
import com.android.gatherly.utils.profilePicturePainter
import java.util.Locale

object FocusTimerScreenTestTags {
  const val TIMERTEXT = "TIMER"
  const val HOURS_TEXT = "HOURS_TEXT"
  const val MINUTES_TEXT = "MINUTES_TEXT"
  const val SECONDS_TEXT = "SECONDS_TEXT"
  const val START_BUTTON = "START_BUTTON"
  const val RESET_BUTTON = "RESET_BUTTON"
  const val PAUSE_BUTTON = "PAUSE_BUTTON"
  const val RESUME_BUTTON = "RESUME_BUTTON"
  const val STOP_BUTTON = "STOP_BUTTON"
  const val TODO_TO_CHOOSE = "TODO_TO_CHOOSE"
  const val LINKED_TODO = "LINKED_TODO"
  const val TIMER_TIME = "TIMER_TIME"
  const val TIMER_CIRCLE = "TIMER_CIRCLE"
  const val TIMER_SELECT = "TIMER_SELECT"
  const val LEADERBOARD_SELECT = "LEADERBOARD_SELECT"
  const val LEADERBOARD_LIST = "LEADERBOARD_LIST"
  const val HISTORY_SELECT = "HISTORY_SELECT"
  const val HISTORY_LIST = "HISTORY_LIST"
  const val EMPTY_HISTORY_LIST_MSG = "EMPTY_HISTORY_LIST_MSG"

  /**
   * Returns a unique test tag for the duration associated with a given [FocusSession] item.
   *
   * @param focusSessionId The id of the focus session whose test tag will be generated.
   * @return A string uniquely identifying the duration text of the given [FocusSession].
   */
  fun getDurationTagForFocusSessionItem(focusSessionId: String): String = "duration$focusSessionId"

  /**
   * Returns a unique test tag for the timestamp associated with a given [FocusSession] item.
   *
   * @param focusSessionId The id of the focus session whose test tag will be generated.
   * @return A string uniquely identifying the timestamp text of the given [FocusSession].
   */
  fun getTimestampTagForFocusSessionItem(focusSessionId: String): String =
      "timestamp$focusSessionId"

  /**
   * Returns a unique test tag for the todo associated with a given [FocusSession] item.
   *
   * @param focusSessionId The id of the focus session whose test tag will be generated.
   * @return A string uniquely identifying the linked todo of the given [FocusSession].
   */
  fun getTodoTagForFocusSessionItem(focusSessionId: String): String = "todo$focusSessionId"

  /**
   * Returns a unique test tag for the card or container representing a given [FocusSession] item.
   *
   * @param focusSessionId The id of the focus session whose test tag will be generated.
   * @return A string uniquely identifying the FocusSession item in the UI.
   */
  fun getTestTagForFocusSessionItem(focusSessionId: String): String =
      "focusSessionItem$focusSessionId"
}

@Composable
fun TimerScreen(
    timerViewModel: TimerViewModel = viewModel(),
    navigationActions: NavigationActions? = null,
) {

  // Scaffold to have top bar and bottom bar
  Scaffold(
      topBar = {
        TopNavigationMenu(
            selectedTab = Tab.Timer,
            onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
            modifier = Modifier.testTag(NavigationTestTags.TOP_NAVIGATION_MENU))
      },
      bottomBar = {
        BottomNavigationMenu(
            selectedTab = Tab.Timer,
            onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
            modifier = Modifier.testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU))
      },
      content = { padding ->
        Box(modifier = Modifier.padding(padding)) { TimerScreenContent(timerViewModel) }
      })
}

/** Represents the different tabs in the timer screen */
enum class TimerScreenTab {
  TIMER,
  LEADERBOARD,
  HISTORY
}

// Organises the display of different views within the screen
@Composable
fun TimerScreenContent(timerViewModel: TimerViewModel) {

  // Collect the uiState, and the context for Toasts
  val uiState by timerViewModel.uiState.collectAsState()
  val context = LocalContext.current
  val selectedTab = remember { mutableStateOf(TimerScreenTab.TIMER) }

  // Launched effect to display a toast upon error
  LaunchedEffect(uiState.errorMsg) {
    if (uiState.errorMsg != null) {
      Toast.makeText(context, uiState.errorMsg, Toast.LENGTH_SHORT).show()
      timerViewModel.clearError()
    }
  }

  val corner = 12.dp

  // Column to contain averything
  Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
    // Top bar to select either timer or leaderboard view
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly) {
          // Timer selected
          Button(
              onClick = {
                selectedTab.value = TimerScreenTab.TIMER
                timerViewModel.loadUI()
              },
              colors =
                  buttonColors(
                      containerColor =
                          if (selectedTab.value == TimerScreenTab.TIMER)
                              MaterialTheme.colorScheme.primary
                          else MaterialTheme.colorScheme.surfaceVariant,
                      contentColor =
                          if (selectedTab.value == TimerScreenTab.TIMER)
                              MaterialTheme.colorScheme.onPrimary
                          else MaterialTheme.colorScheme.background),
              shape = RoundedCornerShape(dimensionResource(R.dimen.rounded_corner_shape_large)),
              modifier =
                  Modifier.height(dimensionResource(R.dimen.events_filter_button_height))
                      .testTag(FocusTimerScreenTestTags.TIMER_SELECT)) {
                Text(text = stringResource(R.string.timer_select))
              }

          // History selected
          Button(
              onClick = {
                selectedTab.value = TimerScreenTab.HISTORY
                timerViewModel.loadUI()
              },
              colors =
                  buttonColors(
                      containerColor =
                          if (selectedTab.value == TimerScreenTab.HISTORY)
                              MaterialTheme.colorScheme.primary
                          else MaterialTheme.colorScheme.surfaceVariant,
                      contentColor =
                          if (selectedTab.value == TimerScreenTab.HISTORY)
                              MaterialTheme.colorScheme.onPrimary
                          else MaterialTheme.colorScheme.background),
              shape = RoundedCornerShape(dimensionResource(R.dimen.rounded_corner_shape_large)),
              modifier =
                  Modifier.height(dimensionResource(R.dimen.focus_session_history_button_height))
                      .testTag(FocusTimerScreenTestTags.HISTORY_SELECT)) {
                Text(text = stringResource(R.string.history_select))
              }

          // Leaderboard selected
          Button(
              onClick = {
                selectedTab.value = TimerScreenTab.LEADERBOARD
                timerViewModel.loadUI()
              },
              colors =
                  buttonColors(
                      containerColor =
                          if (selectedTab.value == TimerScreenTab.LEADERBOARD)
                              MaterialTheme.colorScheme.primary
                          else MaterialTheme.colorScheme.surfaceVariant,
                      contentColor =
                          if (selectedTab.value == TimerScreenTab.LEADERBOARD)
                              MaterialTheme.colorScheme.onPrimary
                          else MaterialTheme.colorScheme.background),
              shape = RoundedCornerShape(dimensionResource(R.dimen.rounded_corner_shape_large)),
              modifier =
                  Modifier.height(dimensionResource(R.dimen.events_filter_button_height))
                      .testTag(FocusTimerScreenTestTags.LEADERBOARD_SELECT)) {
                Text(text = stringResource(R.string.leaderboard_select))
              }
        }

    // If the timer is selected, show that
    if (selectedTab.value == TimerScreenTab.TIMER) {

      // If the timer didn't start, display the first view (editing timer time)
      if (!uiState.isStarted) {
        TimerNotStarted(uiState = uiState, timerViewModel = timerViewModel, corner = corner)
      }

      // Second view, if timer is running or paused
      if (uiState.isStarted) {
        TimerStarted(uiState = uiState, timerViewModel = timerViewModel, corner = corner)
      }

      // If the leaderboard is selected, show that
    } else if (selectedTab.value == TimerScreenTab.LEADERBOARD) {
      Leaderboard(uiState = uiState, timerViewModel = timerViewModel)
    } else if (selectedTab.value == TimerScreenTab.HISTORY) {
      FocusSessionsHistory(uiState = uiState)
    } else {
      // Default case, should not happen
    }
  }
}

/**
 * A composable to show the friends leaderboard
 *
 * @param uiState The state exposed to the UI by the VM
 * @param timerViewModel The viewModel instance
 */
@Composable
fun Leaderboard(uiState: TimerState, timerViewModel: TimerViewModel) {
  // Lazy column to scroll through leaderboard
  LazyColumn(
      modifier = Modifier.fillMaxWidth().testTag(FocusTimerScreenTestTags.LEADERBOARD_LIST),
      horizontalAlignment = Alignment.CenterHorizontally) {
        var ranking = 0
        // the leaderboard is a sorted map from points to a list of profiles. all the profiles in
        // the list have the same rank
        for ((_, friends) in uiState.leaderboard) {
          for (friend in friends) {
            // computes the ranking of this group
            val rank = ranking + 1
            item {
              Card(
                  colors =
                      CardDefaults.cardColors(
                          containerColor =
                              if (timerViewModel.isCurrentUser(friend.uid))
                                  MaterialTheme.colorScheme.surfaceVariant
                              else MaterialTheme.colorScheme.background),
                  modifier =
                      Modifier.fillMaxWidth()
                          .padding(
                              vertical =
                                  dimensionResource(R.dimen.friends_item_card_padding_vertical))) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(
                                    horizontal =
                                        dimensionResource(R.dimen.friends_item_card_padding),
                                    vertical =
                                        dimensionResource(
                                            R.dimen.friends_item_card_padding_vertical))) {

                          // Show the rank with particular colors for top 3
                          Text(
                              text = (rank).toString(),
                              color = rankColor(rank),
                              style = MaterialTheme.typography.headlineLarge,
                              fontWeight = FontWeight.Bold)

                          // Profile pic
                          Image(
                              painter = profilePicturePainter(friend.profilePicture),
                              contentDescription = "Profile picture",
                              contentScale = ContentScale.Crop,
                              modifier =
                                  Modifier.padding(
                                          horizontal =
                                              dimensionResource(R.dimen.friends_item_card_padding))
                                      .size(
                                          dimensionResource(
                                              R.dimen.find_friends_item_profile_picture_size))
                                      .clip(CircleShape))

                          // Name and username
                          Column(
                              modifier = Modifier.fillMaxHeight().weight(1f),
                              horizontalAlignment = Alignment.Start,
                              verticalArrangement = Arrangement.SpaceEvenly) {
                                Text(
                                    text = friend.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold)

                                Text(
                                    text = friend.username,
                                    style = MaterialTheme.typography.bodyMedium)
                              }

                          // Number of points
                          Text(
                              text =
                                  stringResource(R.string.leaderboard_points, friend.weeklyPoints),
                              style = MaterialTheme.typography.headlineLarge,
                              fontWeight = FontWeight.Bold)
                        }
                  }
            }
          }

          ranking++
        }
      }
}

/**
 * Composable to choose a color given a rank (Gold, Silver, Bronze or normal)
 *
 * @param rank the rank to assign the color for
 */
@Composable
fun rankColor(rank: Int): Color {
  return when (rank) {
    1 -> theme_leaderboard_gold
    2 -> theme_leaderboard_silver
    3 -> theme_leaderboard_bronze
    else -> MaterialTheme.colorScheme.onBackground
  }
}

/**
 * The composable to show the view when the timer is started
 *
 * @param uiState The state exposed to the UI by the VM
 * @param timerViewModel The viewModel instance
 * @param corner The value to use for rounded corners components
 */
@Composable
fun TimerStarted(uiState: TimerState, timerViewModel: TimerViewModel, corner: Dp) {

  // Configuration is needed to get the screen width
  val configuration = LocalConfiguration.current

  Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
    // Define the weights of the components
    val todoWeight = 0.5f
    val timerWeight = 2f
    val textWeight = 0.5f
    val buttonsWeight = 1f
    Box(modifier = Modifier.weight(todoWeight), contentAlignment = Alignment.BottomCenter) {
      val todoRatio = 2.0 / 3.0

      // If a todo is linked, display it
      if (uiState.linkedTodo != null) {
        Text(
            text =
                // Build a string with only the title of the todo to in bold
                buildAnnotatedString {
                  append(stringResource(R.string.timer_linked_todo))
                  withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(uiState.linkedTodo.name)
                  }
                },
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier =
                Modifier.width((configuration.screenWidthDp * todoRatio).dp)
                    .testTag(FocusTimerScreenTestTags.LINKED_TODO))
      }
    }

    // Timer circle and test
    Box(modifier = Modifier.weight(timerWeight), contentAlignment = Alignment.Center) {
      val progressRatio = 6.0 / 7.0
      val progressWidth = 15.dp
      val progressGap = 5.dp

      Box(
          modifier =
              Modifier.height((configuration.screenWidthDp * progressRatio).dp)
                  .width((configuration.screenWidthDp * progressRatio).dp)) {
            // Circular time left
            CircularProgressIndicator(
                progress = { (uiState.remainingTime / uiState.plannedDuration).toFloat() },
                strokeWidth = progressWidth,
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                gapSize = progressGap,
                modifier =
                    Modifier.width((configuration.screenWidthDp * progressRatio).dp)
                        .testTag(FocusTimerScreenTestTags.TIMER_CIRCLE))

            // Time left
            Text(
                text = uiState.hours + ":" + uiState.minutes + ":" + uiState.seconds,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                modifier =
                    Modifier.align(Alignment.Center).testTag(FocusTimerScreenTestTags.TIMER_TIME))
          }
    }

    // Points text
    Box(modifier = Modifier.weight(textWeight).fillMaxSize(), contentAlignment = Alignment.Center) {
      Text(
          text = "You have gained ${uiState.pointsGained} points!",
          style = MaterialTheme.typography.headlineMedium,
          modifier = Modifier.align(Alignment.BottomCenter))
    }

    // Control buttons
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth().weight(buttonsWeight)) {
          if (!uiState.isPaused) {

            // Pause button
            TimerButton(
                { timerViewModel.pauseTimer() },
                MaterialTheme.colorScheme.secondary,
                MaterialTheme.colorScheme.onSecondary,
                stringResource(R.string.timer_pause),
                corner,
                FocusTimerScreenTestTags.PAUSE_BUTTON)
          } else {

            // Resume button
            TimerButton(
                { timerViewModel.startTimer() },
                MaterialTheme.colorScheme.secondary,
                MaterialTheme.colorScheme.onSecondary,
                stringResource(R.string.timer_resume),
                corner,
                FocusTimerScreenTestTags.RESUME_BUTTON)
          }

          // Stop timer button
          TimerButton(
              { timerViewModel.endTimer() },
              MaterialTheme.colorScheme.surfaceVariant,
              MaterialTheme.colorScheme.onSurfaceVariant,
              stringResource(R.string.timer_stop),
              corner,
              FocusTimerScreenTestTags.STOP_BUTTON)
        }
  }
}

/**
 * The composable to show the view when the timer is not started yet
 *
 * @param uiState The state exposed to the UI by the VM
 * @param timerViewModel The viewModel instance
 * @param corner The value to use for rounded corners components
 */
@Composable
fun TimerNotStarted(uiState: TimerState, timerViewModel: TimerViewModel, corner: Dp) {
  Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

    // Define weights of different components in the screen
    val timeWeight = 3f
    val buttonsWeight = 1f
    val todosWeight = 2f

    // Column of all 3 components

    // The three time textFields and texts (hours, minutes, seconds)
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth().weight(timeWeight),
        verticalAlignment = Alignment.Bottom) {
          // The hours time textField and text
          TimerTime(
              uiState.hours,
              { timerViewModel.setHours(it) },
              stringResource(R.string.timer_hours),
              corner,
              FocusTimerScreenTestTags.HOURS_TEXT)

          // The minutes time textField and text
          TimerTime(
              uiState.minutes,
              { timerViewModel.setMinutes(it) },
              stringResource(R.string.timer_minutes),
              corner,
              FocusTimerScreenTestTags.MINUTES_TEXT)

          // The seconds time textField and text
          TimerTime(
              uiState.seconds,
              { timerViewModel.setSeconds(it) },
              stringResource(R.string.timer_seconds),
              corner,
              FocusTimerScreenTestTags.SECONDS_TEXT)
        }

    // The start and reset timer buttons
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth().weight(buttonsWeight)) {

          // Start button
          TimerButton(
              { timerViewModel.startTimer() },
              MaterialTheme.colorScheme.secondary,
              MaterialTheme.colorScheme.onSecondary,
              stringResource(R.string.timer_start),
              corner,
              FocusTimerScreenTestTags.START_BUTTON)

          // Reset button
          TimerButton(
              { timerViewModel.resetTimer() },
              MaterialTheme.colorScheme.surfaceVariant,
              MaterialTheme.colorScheme.onSurfaceVariant,
              stringResource(R.string.timer_reset),
              corner,
              FocusTimerScreenTestTags.RESET_BUTTON)
        }

    // Todos to link, in a lazy column to enable scrolling
    LazyColumn(modifier = Modifier.fillMaxSize().weight(todosWeight)) {
      val padding = 6.dp
      val todoHeight = 60.dp
      val horizontalThickness = 0.5.dp

      // Title text for linking todos
      item {
        Text(
            text = stringResource(R.string.timer_todos_linking),
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(padding))
      }

      // Todos lazily displayed
      for (todo in uiState.allTodos) {
        item {
          // Divider for aesthetics
          HorizontalDivider(
              thickness = horizontalThickness, color = MaterialTheme.colorScheme.onSurfaceVariant)

          // The card for each todo
          Card(
              colors =
                  if (todo == uiState.linkedTodo) {
                    // Highlight the linked todo with different colors
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary)
                  } else {
                    // Normal colors for unlinked todos
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.onBackground)
                  },
              shape = RectangleShape,
              modifier =
                  Modifier.fillMaxWidth()
                      .height(todoHeight)
                      .clickable(onClick = { timerViewModel.linkToDo(todo) })
                      .testTag(FocusTimerScreenTestTags.TODO_TO_CHOOSE)) {
                Box(modifier = Modifier.fillMaxSize().padding(horizontal = padding)) {
                  Text(
                      text = todo.name,
                      modifier = Modifier.padding(padding).align(Alignment.CenterStart))
                }
              }
        }
      }
    }
  }
}

/**
 * Helper function to display buttons on the screen
 *
 * @param onClick to call when button is clicked
 * @param containerColor color of the button
 * @param contentColor color of the button text
 * @param text text to display
 * @param corner the corner rounding of the buttons
 * @param testTag test tag to attach to the button
 */
@Composable
fun TimerButton(
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    text: String,
    corner: Dp,
    testTag: String
) {

  // Configuration needed to get the width, and ratio to use later
  val configuration = LocalConfiguration.current
  val buttonRatio = 3.0 / 7.0

  Button(
      onClick = onClick,
      shape = RoundedCornerShape(corner),
      colors =
          ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
      modifier = Modifier.width((configuration.screenWidthDp * buttonRatio).dp).testTag(testTag)) {
        Text(text = text, fontWeight = FontWeight.Bold)
      }
}

/**
 * Helper function for the editing time text fields
 *
 * @param time the text to display
 * @param onValueChange to call when the value of the text field changes
 * @param text the text to display under the text field
 * @param corner the corner rounding of the text field
 * @param testTag the test tag to attach to the text field
 */
@Composable
fun TimerTime(
    time: String,
    onValueChange: (String) -> Unit,
    text: String,
    corner: Dp,
    testTag: String
) {

  // Configuration needed to get the width, and ratio to use later and font size
  val configuration = LocalConfiguration.current
  val timeRatio = 1.0 / 4.0
  val timeFontSize = 25.sp

  val placeholderAlpha = LocalContext.current.resources.getFloat(R.dimen.alpha_placeholder)

  Column(modifier = Modifier.width((configuration.screenWidthDp * timeRatio).dp)) {
    // Text field displaying the chosen hours, minutes or seconds
    TextField(
        value = time,
        onValueChange = onValueChange,
        placeholder = {
          Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.timer_initial_time_placeholder),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = timeFontSize,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = placeholderAlpha))
          }
        },
        shape = RoundedCornerShape(corner),
        textStyle =
            LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = timeFontSize),
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent),
        maxLines = 1,
        keyboardOptions =
            KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        modifier = Modifier.fillMaxWidth().testTag(testTag))
    // Text displayed under the text field
    Text(
        text = text,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.fillMaxWidth())
  }
}

// Helper function to preview the timer screen
@Preview
@Composable
fun TimerScreenPreview() {
  GatherlyTheme { TimerScreen() }
}

/**
 * A composable to show the user's focus sessions history
 *
 * @param uiState The state exposed to the UI by the VM
 */
@Composable
fun FocusSessionsHistory(uiState: TimerState) {
  val usersFocusSessions =
      uiState.usersFocusSessions.toSortedSet(compareByDescending { it.startedAt })
  if (usersFocusSessions.isEmpty()) {
    Text(
        modifier =
            Modifier.padding(dimensionResource(R.dimen.padding_screen))
                .testTag(FocusTimerScreenTestTags.EMPTY_HISTORY_LIST_MSG),
        text = stringResource(R.string.no_focus_sessions_history_text))
  } else {
    LazyColumn(
        contentPadding =
            PaddingValues(
                vertical = dimensionResource(id = R.dimen.focus_session_history_vertical_padding)),
        modifier =
            Modifier.fillMaxWidth()
                .padding(
                    horizontal =
                        dimensionResource(id = R.dimen.focus_session_history_horizontal_padding))
                .testTag(FocusTimerScreenTestTags.HISTORY_LIST)) {
          items(usersFocusSessions.size) { index ->
            val focusSession = usersFocusSessions.elementAt(index)
            if (focusSession.endedAt != null) {
              FocusSessionItem(focusSession = focusSession, allTodos = uiState.allTodos)
            }
          }
        }
  }
}

/**
 * Displays a single focus session item inside a [Card] with basic details.
 *
 * @param focusSession The [FocusSession] item to display.
 * @param allTodos The list of all [ToDo] items to find linked todos.
 */
@SuppressLint("DefaultLocale")
@Composable
fun FocusSessionItem(
    focusSession: FocusSession,
    allTodos: List<ToDo>,
) {
  val missingDetail = stringResource(R.string.focus_session_missing_detail)
  val linkedTodo = focusSession.linkedTodoId?.let { todoId -> allTodos.find { it.uid == todoId } }

  val startedAt = focusSession.startedAt
  val startDate =
      startedAt?.let {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(focusSession.startedAt.toDate())
      } ?: missingDetail
  val startTime =
      startedAt?.let {
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(focusSession.startedAt.toDate())
      } ?: missingDetail

  val startedAtText = "$startDate - $startTime"
  val durationText =
      if (focusSession.endedAt != null && startedAt != null) {
        val durationInSeconds = (focusSession.endedAt.seconds - startedAt.seconds).coerceAtLeast(0)
        val hours = durationInSeconds / 3600
        val minutes = (durationInSeconds % 3600) / 60
        val seconds = durationInSeconds % 60
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
      } else {
        missingDetail
      }

  Card(
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant,
              contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
      modifier =
          Modifier.testTag(
                  FocusTimerScreenTestTags.getTestTagForFocusSessionItem(
                      focusSession.focusSessionId))
              .fillMaxWidth()
              .padding(
                  vertical = dimensionResource(id = R.dimen.focus_session_item_vertical_padding))) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(dimensionResource(id = R.dimen.focus_session_item_row_padding)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(
              modifier =
                  Modifier.weight(
                      integerResource(id = R.integer.focus_session_item_column_weight).toFloat())) {
                // Start timestamp text
                Text(
                    text = startedAtText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier =
                        Modifier.testTag(
                            FocusTimerScreenTestTags.getTimestampTagForFocusSessionItem(
                                focusSession.focusSessionId)))
                if (linkedTodo != null) {
                  // Linked todo text
                  Text(
                      text = linkedTodo.name,
                      style = MaterialTheme.typography.bodyMedium,
                      color = MaterialTheme.colorScheme.primary,
                      modifier =
                          Modifier.testTag(
                              FocusTimerScreenTestTags.getTodoTagForFocusSessionItem(
                                  focusSession.focusSessionId)))
                } else {
                  // No linked todo text
                  Text(
                      text = stringResource(R.string.focus_session_no_linked_todo),
                      style = MaterialTheme.typography.bodyMedium,
                      color = MaterialTheme.colorScheme.outline,
                      modifier =
                          Modifier.testTag(
                              FocusTimerScreenTestTags.getTodoTagForFocusSessionItem(
                                  focusSession.focusSessionId)))
                }
              }

          // Duration text
          Text(
              text = durationText,
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontWeight = FontWeight.Medium,
              modifier =
                  Modifier.testTag(
                      FocusTimerScreenTestTags.getDurationTagForFocusSessionItem(
                          focusSession.focusSessionId)))
        }
      }
}
