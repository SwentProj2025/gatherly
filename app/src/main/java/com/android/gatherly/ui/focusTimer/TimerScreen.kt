package com.android.gatherly.ui.focusTimer

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.R
import com.android.gatherly.ui.navigation.BottomNavigationMenu
import com.android.gatherly.ui.navigation.HandleSignedOutState
import com.android.gatherly.ui.navigation.NavigationActions
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu
import com.android.gatherly.ui.theme.GatherlyTheme

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
}

@Composable
fun TimerScreen(
    timerViewModel: TimerViewModel = viewModel(),
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current),
    onSignedOut: () -> Unit = {},
    navigationActions: NavigationActions? = null,
) {

  val uiState by timerViewModel.uiState.collectAsState()

  // If the user signs out, call onSignedOut()
  HandleSignedOutState(uiState.signedOut, onSignedOut)

  // Scaffold to have top bar and bottom bar
  Scaffold(
      topBar = {
        TopNavigationMenu(
            selectedTab = Tab.Timer,
            onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
            modifier = Modifier.testTag(NavigationTestTags.TOP_NAVIGATION_MENU),
            onSignedOut = { timerViewModel.signOut(credentialManager) })
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

@Composable
fun TimerScreenContent(timerViewModel: TimerViewModel) {

  // Collect the uiState, and the context for Toasts
  val uiState by timerViewModel.uiState.collectAsState()
  val context = LocalContext.current

  // Launched effect to display a toast upon error
  LaunchedEffect(uiState.errorMsg) {
    if (uiState.errorMsg != null) {
      Toast.makeText(context, uiState.errorMsg, Toast.LENGTH_SHORT).show()
      timerViewModel.clearError()
    }
  }

  // Configuration is needed to get the screen width
  val configuration = LocalConfiguration.current
  val corner = 12.dp

  // If the timer didn't start, display the first view (editing timer time)
  if (!uiState.isStarted) {

    // Define weights of different components in the screen
    val timeWeight = 3f
    val buttonsWeight = 1f
    val todosWeight = 2f

    // Column of all 3 components
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
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

  // Second view, if timer is running or paused
  if (uiState.isStarted) {
    // Define the weights of the components
    val todoWeight = 1f
    val timerWeight = 2f
    val buttonsWeight = 1f

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
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
                      append(uiState.linkedTodo?.name ?: "")
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

  Column(modifier = Modifier.width((configuration.screenWidthDp * timeRatio).dp)) {
    // Text field displaying the chosen hours, minutes or seconds
    TextField(
        value = time,
        onValueChange = onValueChange,
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
