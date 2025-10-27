package com.android.gatherly.ui.homePage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.ui.navigation.BottomNavigationMenu
import com.android.gatherly.ui.navigation.HandleSignedOutState
import com.android.gatherly.ui.navigation.NavigationActions
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu_HomePage

object HomePageScreenTestTags {}

@Composable
fun HomePageScreen(
    homePageViewModel: HomePageViewModel = viewModel(),
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current),
    onSignedOut: () -> Unit = {},
    navigationActions: NavigationActions? = null,
) {
  val uiState by homePageViewModel.uiState.collectAsState()

  HandleSignedOutState(uiState.signedOut, onSignedOut)

  // Dimension values will be moved to dimens.xml
  val horizontalPadding = 16.dp
  val verticalSpacing = 24.dp
  val sectionTitleSize = 20.sp
  val carouselHeight = 200.dp
  val friendAvatarSize = 56.dp
  val friendAvatarSpacing = 8.dp
  val taskItemHeight = 56.dp
  val taskItemSpacing = 12.dp
  val iconSize = 24.dp
  val mapImageHeight = 160.dp
  val buttonHeight = 56.dp
  val buttonCornerRadius = 28.dp
  val smallIconSize = 20.dp
  val chevronIconSize = 20.dp
  val profileIconSize = 28.dp
  val dividerThickness = 1.dp
  val bottomSpacing = 16.dp
  val focusMessagePadding = 16.dp
  val sectionSpacing = 20.dp
  val taskIconSize = 24.dp
  val friendsLabelTopMargin = 8.dp
  val carouselItemWidth = 280.dp
  val carouselImageRadius = 12.dp

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
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .background(Color(0xFF1A1F25))) {
              Spacer(modifier = Modifier.height(verticalSpacing))

              SectionTitle(
                  text = "Upcoming events",
                  modifier = Modifier.padding(horizontal = horizontalPadding),
                  fontSize = sectionTitleSize)

              Spacer(modifier = Modifier.height(sectionSpacing))

              EventsAndFriendsSection(
                  carouselHeight = carouselHeight,
                  carouselItemWidth = carouselItemWidth,
                  cardCornerRadius = carouselImageRadius,
                  mapImageHeight = mapImageHeight,
                  friendAvatarSize = friendAvatarSize,
                  friendAvatarSpacing = friendAvatarSpacing,
                  friendsLabelTopMargin = friendsLabelTopMargin)

              Spacer(modifier = Modifier.height(verticalSpacing))

              SectionTitle(
                  text = "My upcoming tasks",
                  modifier = Modifier.padding(horizontal = horizontalPadding),
                  fontSize = sectionTitleSize)

              Spacer(modifier = Modifier.height(sectionSpacing))

              TaskList(
                  tasks = listOf("Working with Gersende", "Lunch with Clic", "Workout with Claire"),
                  horizontalPadding = horizontalPadding,
                  taskItemHeight = taskItemHeight,
                  taskItemSpacing = taskItemSpacing,
                  taskIconSize = taskIconSize,
                  chevronIconSize = chevronIconSize)

              Spacer(modifier = Modifier.height(verticalSpacing))

              FocusMessage(
                  message = "You focused 5h yesterday!",
                  modifier = Modifier.padding(horizontal = horizontalPadding),
                  padding = focusMessagePadding)

              Spacer(modifier = Modifier.height(sectionSpacing))

              FocusTimerButton(
                  modifier = Modifier.padding(horizontal = horizontalPadding),
                  buttonHeight = buttonHeight,
                  buttonCornerRadius = buttonCornerRadius)

              Spacer(modifier = Modifier.height(bottomSpacing))
            }
      })
}

@Composable
fun HomeTopBar(height: Dp, horizontalPadding: Dp, iconSize: Dp) {
  Surface(modifier = Modifier.fillMaxWidth().height(height), color = Color(0xFF1A1F25)) {
    Row(
        modifier = Modifier.fillMaxSize().padding(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
          Text(
              text = "Home Page",
              color = Color.White,
              style = MaterialTheme.typography.headlineSmall)

          Icon(
              imageVector = Icons.Default.AccountCircle,
              contentDescription = "Profile",
              tint = Color.White,
              modifier = Modifier.size(iconSize))
        }
  }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier, fontSize: TextUnit) {
  Text(text = text, color = Color.White, fontSize = fontSize, modifier = modifier)
}

@Composable
fun EventsAndFriendsSection(
    carouselHeight: Dp,
    carouselItemWidth: Dp,
    cardCornerRadius: Dp,
    mapImageHeight: Dp,
    friendAvatarSize: Dp,
    friendAvatarSpacing: Dp,
    friendsLabelTopMargin: Dp
) {
  Row(modifier = Modifier.fillMaxWidth().height(carouselHeight)) {
    Spacer(modifier = Modifier.width(16.dp))

    // Map
    Card(
        modifier = Modifier.width(carouselItemWidth).fillMaxHeight(),
        shape = RoundedCornerShape(cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A3139))) {
          Box(modifier = Modifier.fillMaxSize()) {
            Text("MAP", color = Color.White, modifier = Modifier.align(Alignment.Center))
          }
        }

    Spacer(modifier = Modifier.width(16.dp))

    FriendsSection()

    Spacer(modifier = Modifier.width(16.dp))
  }
}

@Composable
fun FriendAvatar(size: Dp, color: Color = Color(0xFF3A4149)) {
  Box(modifier = Modifier.size(size).clip(CircleShape).background(color))
}

@Composable
fun FriendsSection() {

  val friendCount = 3
  val friendAvatarSize = 48.dp
  val friendAvatarSpacing = 8.dp
  val borderWidth = 2.dp
  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier =
          Modifier.border(
                  width = borderWidth,
                  color = Color.White,
                  shape = RoundedCornerShape(percent = 50))
              .clip(RoundedCornerShape(percent = 50)) // Ensures children stay within border radius
              .padding(vertical = 12.dp, horizontal = 8.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(friendAvatarSpacing)) {
          repeat(friendCount) { FriendAvatar(size = friendAvatarSize) }
        }

        Text(text = "Friends", color = Color.White, style = MaterialTheme.typography.bodySmall)
      }
}

@Composable
fun TaskList(
    tasks: List<String>,
    horizontalPadding: Dp,
    taskItemHeight: Dp,
    taskItemSpacing: Dp,
    taskIconSize: Dp,
    chevronIconSize: Dp
) {
  Column(
      modifier = Modifier.padding(horizontal = horizontalPadding),
      verticalArrangement = Arrangement.spacedBy(taskItemSpacing)) {
        tasks.forEach { task ->
          TaskItem(
              text = task,
              height = taskItemHeight,
              iconSize = taskIconSize,
              chevronIconSize = chevronIconSize)
        }
      }
}

@Composable
fun TaskItem(text: String, height: Dp, iconSize: Dp, chevronIconSize: Dp) {
  Surface(
      modifier = Modifier.fillMaxWidth().height(height),
      color = Color(0xFF2A3139),
      shape = RoundedCornerShape(8.dp),
      onClick = { /* Handle click */}) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
              Text(text = text, color = Color.White, style = MaterialTheme.typography.bodyLarge)

              Icon(
                  imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                  contentDescription = "Navigate",
                  tint = Color.White,
                  modifier = Modifier.size(chevronIconSize))
            }
      }
}

@Composable
fun FocusMessage(message: String, modifier: Modifier = Modifier, padding: Dp) {
  Text(
      text = message,
      color = Color.White,
      style = MaterialTheme.typography.bodyLarge,
      modifier = modifier)
}

@Composable
fun FocusTimerButton(modifier: Modifier = Modifier, buttonHeight: Dp, buttonCornerRadius: Dp) {
  Button(
      onClick = { /* Navigate to Focus Timer */},
      modifier = modifier.fillMaxWidth().height(buttonHeight),
      shape = RoundedCornerShape(buttonCornerRadius),
      colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A8E8))) {
        Text(
            text = "Go to Focus Timer",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium)
      }
}

@Preview(showBackground = true)
@Composable
fun HomePageScreenPreview() {
  HomePageScreen()
}
