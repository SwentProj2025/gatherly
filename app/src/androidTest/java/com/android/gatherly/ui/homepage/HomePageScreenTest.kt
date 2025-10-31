package com.android.gatherly.ui.homepage

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.gatherly.model.event.EventsLocalRepository
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todo.ToDosRepositoryLocalMapTest
import com.android.gatherly.ui.homePage.HomePageScreen
import com.android.gatherly.ui.homePage.HomePageScreenTestTags
import com.android.gatherly.ui.homePage.HomePageViewModel
import com.android.gatherly.utils.FirebaseEmulator
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HomePageScreenTest {

  /*----------------------------------------Profiles--------------------------------------------*/
  private val friend1Profile: Profile =
      Profile(
          uid = "1",
          name = "Friend1",
          focusSessionIds = emptyList(),
          eventIds = emptyList(),
          groupIds = emptyList(),
          friendUids = emptyList())

  private val friend2Profile: Profile =
      Profile(
          uid = "2",
          name = "Friend2",
          focusSessionIds = emptyList(),
          eventIds = emptyList(),
          groupIds = emptyList(),
          friendUids = emptyList())

  private val friend3Profile: Profile =
      Profile(
          uid = "3",
          name = "Friend3",
          focusSessionIds = emptyList(),
          eventIds = emptyList(),
          groupIds = emptyList(),
          friendUids = emptyList())

  private val friend4Profile: Profile =
      Profile(
          uid = "4",
          name = "Friend4",
          focusSessionIds = emptyList(),
          eventIds = emptyList(),
          groupIds = emptyList(),
          friendUids = emptyList())

  private var currentProfile: Profile =
      Profile(
          uid = "0+",
          name = "Current",
          focusSessionIds = emptyList(),
          eventIds = emptyList(),
          groupIds = emptyList(),
          friendUids =
              listOf(
                  friend1Profile.uid, friend2Profile.uid, friend3Profile.uid, friend4Profile.uid))

  private var friendlessProfile: Profile =
      Profile(
          uid = "0-",
          name = "Current",
          focusSessionIds = emptyList(),
          eventIds = emptyList(),
          groupIds = emptyList(),
          friendUids = emptyList())

  private val friendsList = listOf(friend1Profile, friend2Profile, friend3Profile)

  @get:Rule val composeRule = createComposeRule()

  private lateinit var fakeViewModel: HomePageViewModel
  private lateinit var todosLocalRepo: ToDosRepositoryLocalMapTest
  private lateinit var eventsLocalRepo: EventsLocalRepository
  private lateinit var profileLocalRepo: ProfileLocalRepository

  @Before
  fun setUp() {
    runTest {
      FirebaseEmulator.auth.signInAnonymously().await()
      currentProfile = currentProfile.copy(uid = Firebase.auth.currentUser?.uid!!)
      todosLocalRepo = ToDosRepositoryLocalMapTest()
      eventsLocalRepo = EventsLocalRepository()
      profileLocalRepo = ProfileLocalRepository()
      populateRepositories()
      fakeViewModel =
          HomePageViewModel(
              toDosRepository = todosLocalRepo,
              eventsRepository = eventsLocalRepo,
              profileRepository = profileLocalRepo)
      composeRule.setContent { HomePageScreen(homePageViewModel = fakeViewModel) }
    }
  }

  @After
  fun tearDown() {
    FirebaseEmulator.clearAuthEmulator()
    FirebaseEmulator.clearFirestoreEmulator()
  }

  /** Helper to fill local repositories with fake test data */
  private fun populateRepositories() = runBlocking {
    todosLocalRepo.addTodo(
        ToDo(
            uid = "3",
            name = "Plan party",
            description = "Buy decorations and invite friends",
            assigneeName = "Eve",
            dueDate = Timestamp.now(),
            dueTime = null,
            location = null,
            status = ToDoStatus.ONGOING,
            ownerId = "user1"))

    profileLocalRepo.addProfile(currentProfile)
  }

  @Test
  fun componentsAreDisplayed() {

    composeRule.onNodeWithTag(HomePageScreenTestTags.UPCOMING_EVENTS_TITLE).assertIsDisplayed()
    composeRule.onNodeWithTag(HomePageScreenTestTags.UPCOMING_TASKS_TITLE).assertIsDisplayed()

    composeRule.onNodeWithTag(HomePageScreenTestTags.FOCUS_TIMER_TEXT).assertIsDisplayed()
    composeRule.onNodeWithTag(HomePageScreenTestTags.FOCUS_BUTTON).assertIsDisplayed()

    composeRule.onNodeWithTag(HomePageScreenTestTags.FOCUS_BUTTON).performClick()
  }

  @Test
  fun focusButton_isClickable() {
    composeRule
        .onNodeWithTag(HomePageScreenTestTags.FOCUS_BUTTON)
        .assertIsDisplayed()
        .performClick()
        .assertExists()
  }

  @Test
  fun taskItemsAreDisplayed() {

    print("Starting to check task items...")
    fakeViewModel.uiState.value.todos.forEach { todo ->
      print("Checking for todo item with uid: ${todo.uid}")
      composeRule
          .onNodeWithTag("${HomePageScreenTestTags.TASK_ITEM_PREFIX}${todo.uid}")
          .assertIsDisplayed()
    }
    print("Finished checking task items.")
  }

  @Test
  fun taskItemsTextMatchesUiState() {
    fakeViewModel.uiState.value.todos.forEach { todo ->
      composeRule
          .onNodeWithTag("${HomePageScreenTestTags.TASK_ITEM_PREFIX}${todo.uid}")
          .assertTextContains(todo.description)
    }
  }

  @Test
  fun taskItem_isClickable() {
    fakeViewModel.uiState.value.todos.forEach { todo ->
      composeRule
          .onNodeWithTag("${HomePageScreenTestTags.TASK_ITEM_PREFIX}${todo.uid}")
          .assertIsDisplayed()
          .performClick()
          .assertExists()
    }
  }
}
