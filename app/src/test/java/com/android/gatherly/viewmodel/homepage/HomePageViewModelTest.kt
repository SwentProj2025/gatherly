package com.android.gatherly.viewmodel.homepage

import com.android.gatherly.model.event.EventsLocalRepository
import com.android.gatherly.model.event.EventsRepository
import com.android.gatherly.model.notification.NotificationsLocalRepository
import com.android.gatherly.model.notification.NotificationsRepository
import com.android.gatherly.model.points.PointsLocalRepository
import com.android.gatherly.model.points.PointsRepository
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDosLocalRepository
import com.android.gatherly.model.todo.ToDosRepository
import com.android.gatherly.ui.homePage.HomePageViewModel
import com.android.gatherly.utilstest.MockitoUtils
import com.android.gatherly.viewmodel.homepage.HomePageViewModelTestData.currentProfile
import com.android.gatherly.viewmodel.homepage.HomePageViewModelTestData.displayableEvents
import com.android.gatherly.viewmodel.homepage.HomePageViewModelTestData.displayableTodos
import com.android.gatherly.viewmodel.homepage.HomePageViewModelTestData.event1
import com.android.gatherly.viewmodel.homepage.HomePageViewModelTestData.event2
import com.android.gatherly.viewmodel.homepage.HomePageViewModelTestData.friend1Profile
import com.android.gatherly.viewmodel.homepage.HomePageViewModelTestData.friend2Profile
import com.android.gatherly.viewmodel.homepage.HomePageViewModelTestData.friend3Profile
import com.android.gatherly.viewmodel.homepage.HomePageViewModelTestData.friend4Profile
import com.android.gatherly.viewmodel.homepage.HomePageViewModelTestData.friendlessProfile
import com.android.gatherly.viewmodel.homepage.HomePageViewModelTestData.friendsList
import com.android.gatherly.viewmodel.homepage.HomePageViewModelTestData.todo1
import com.android.gatherly.viewmodel.homepage.HomePageViewModelTestData.todo2
import com.android.gatherly.viewmodel.homepage.HomePageViewModelTestData.todo3
import com.android.gatherly.viewmodel.homepage.HomePageViewModelTestData.todo4
import com.android.gatherly.viewmodel.homepage.HomePageViewModelTestData.upcomingTodos
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomePageViewModelTest {
  // declare viewModel and repositories
  private lateinit var homePageViewModel: HomePageViewModel
  private lateinit var eventsRepository: EventsRepository
  private lateinit var toDosRepository: ToDosRepository
  private lateinit var profileRepository: ProfileRepository
  private lateinit var notificationsRepository: NotificationsRepository
  private lateinit var pointsRepository: PointsRepository
  private lateinit var mockitoUtils: MockitoUtils

  // initialize this so that tests control all coroutines and can wait on them
  private val testDispatcher = UnconfinedTestDispatcher()
  val testTimeout = 120.seconds

  @Before
  fun setUp() {
    // so that tests can wait on coroutines
    Dispatchers.setMain(testDispatcher)

    // initialize repos and viewModel
    profileRepository = ProfileLocalRepository()
    eventsRepository = EventsLocalRepository()
    toDosRepository = ToDosLocalRepository()
    notificationsRepository = NotificationsLocalRepository()
    pointsRepository = PointsLocalRepository()

    // fill the profile and events repositories with profiles and event
    fillRepositories()

    // Mock Firebase Auth
    mockitoUtils = MockitoUtils()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  /*-------------------------------------Tests--------------------------------------------------*/

  /**
   * Verifies that the ViewModel correctly initializes UI state for a user with friends and todos.
   *
   * Tests that displayable todos (with locations), displayable events (with locations), friends
   * list, and all upcoming todos are properly loaded and set in the UI state.
   */
  @Test
  fun correctSetValuesForNormalUser() =
      runTest(testDispatcher, testTimeout) {
        addFriendsCurrentUser()
        addTodos()
        advanceUntilIdle()

        mockitoUtils.chooseCurrentUser(currentProfile.uid)
        advanceUntilIdle()

        homePageViewModel =
            HomePageViewModel(
                eventsRepository = eventsRepository,
                toDosRepository = toDosRepository,
                profileRepository = profileRepository,
                notificationsRepository = notificationsRepository,
                pointsRepository = pointsRepository,
                authProvider = { mockitoUtils.mockAuth })

        advanceUntilIdle()

        assert(homePageViewModel.uiState.value.displayableTodos == displayableTodos)
        assert(homePageViewModel.uiState.value.displayableEvents == displayableEvents)
        assert(homePageViewModel.uiState.value.friends == friendsList) {
          "Actual: ${homePageViewModel.uiState.value.friends}"
        }
        assert(homePageViewModel.uiState.value.todos == upcomingTodos)
      }

  /**
   * Verifies that the ViewModel correctly initializes UI state for a user with no friends but with
   * todos.
   *
   * Tests that todos and events are properly loaded while the friends list remains empty.
   */
  @Test
  fun correctSetValuesForNoFriendsUser() =
      runTest(testDispatcher, testTimeout) {
        addFriendlessCurrentUser()
        addTodos()
        advanceUntilIdle()

        mockitoUtils.chooseCurrentUser(currentProfile.uid)
        advanceUntilIdle()

        homePageViewModel =
            HomePageViewModel(
                eventsRepository = eventsRepository,
                toDosRepository = toDosRepository,
                profileRepository = profileRepository,
                notificationsRepository = notificationsRepository,
                pointsRepository = pointsRepository,
                authProvider = { mockitoUtils.mockAuth })

        advanceUntilIdle()

        assert(homePageViewModel.uiState.value.displayableTodos == displayableTodos) {
          "Actual : ${homePageViewModel.uiState.value.displayableTodos}"
        }
        assert(homePageViewModel.uiState.value.displayableEvents == displayableEvents)
        assert(homePageViewModel.uiState.value.friends == emptyList<Profile>())
        assert(homePageViewModel.uiState.value.todos == upcomingTodos)
      }

  /**
   * Verifies that the ViewModel correctly initializes UI state for a user with no todos.
   *
   * Tests that displayable todos and upcoming todos remain empty while events are still properly
   * loaded.
   */
  @Test
  fun correctSetValuesForNoTodosUser() =
      runTest(testDispatcher, testTimeout) {
        addFriendlessCurrentUser()
        advanceUntilIdle()

        mockitoUtils.chooseCurrentUser(currentProfile.uid)
        advanceUntilIdle()

        homePageViewModel =
            HomePageViewModel(
                eventsRepository = eventsRepository,
                toDosRepository = toDosRepository,
                profileRepository = profileRepository,
                notificationsRepository = notificationsRepository,
                pointsRepository = pointsRepository,
                authProvider = { mockitoUtils.mockAuth })

        advanceUntilIdle()

        assert(homePageViewModel.uiState.value.displayableTodos.isEmpty()) {
          "Actual : ${homePageViewModel.uiState.value.displayableTodos}"
        }
        assert(homePageViewModel.uiState.value.displayableEvents == displayableEvents) {
          "Actual : ${homePageViewModel.uiState.value.displayableEvents} : ${homePageViewModel.uiState.value}"
        }
        assert(homePageViewModel.uiState.value.friends == emptyList<Profile>())
        assert(homePageViewModel.uiState.value.todos == emptyList<ToDo>())
      }

  /*-------------------------------------Helper functions---------------------------------------*/

  /**
   * Populates the profile and events repositories with test data.
   *
   * Adds friend profiles and events to their respective repositories for use in test scenarios.
   */
  private fun fillRepositories() =
      runTest(testDispatcher, testTimeout) {
        profileRepository.addProfile(friend1Profile)
        profileRepository.addProfile(friend2Profile)
        profileRepository.addProfile(friend3Profile)
        profileRepository.addProfile(friend4Profile)
        eventsRepository.addEvent(event1)
        eventsRepository.addEvent(event2)
        advanceUntilIdle()
      }

  /** Adds a user profile with the current Firebase ID and four friends to the repository. */
  private suspend fun addFriendsCurrentUser() {
    profileRepository.addProfile(currentProfile)
  }

  /** Adds a user profile with the current Firebase ID and no friends to the repository. */
  private suspend fun addFriendlessCurrentUser() {
    profileRepository.addProfile(friendlessProfile)
  }

  /** Adds all test todos to the todos repository. */
  private suspend fun addTodos() {
    toDosRepository.addTodo(todo1)
    toDosRepository.addTodo(todo2)
    toDosRepository.addTodo(todo3)
    toDosRepository.addTodo(todo4)
  }
}
