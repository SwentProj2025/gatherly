package com.android.gatherly.viewmodel.homepage

import com.android.gatherly.model.event.Event
import com.android.gatherly.model.event.EventStatus
import com.android.gatherly.model.event.EventsLocalRepository
import com.android.gatherly.model.event.EventsRepository
import com.android.gatherly.model.map.Location
import com.android.gatherly.model.notification.NotificationsLocalRepository
import com.android.gatherly.model.notification.NotificationsRepository
import com.android.gatherly.model.points.PointsLocalRepository
import com.android.gatherly.model.points.PointsRepository
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todo.ToDosLocalRepository
import com.android.gatherly.model.todo.ToDosRepository
import com.android.gatherly.ui.homePage.HomePageViewModel
import com.android.gatherly.utilstest.MockitoUtils
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

/*----------------------------------------Profiles--------------------------------------------*/
private val friend1Profile: Profile =
    Profile(
        uid = "1",
        name = "Friend1",
        focusSessionIds = emptyList(),
        participatingEventIds = emptyList(),
        groupIds = emptyList(),
        friendUids = emptyList())

private val friend2Profile: Profile =
    Profile(
        uid = "2",
        name = "Friend2",
        focusSessionIds = emptyList(),
        participatingEventIds = emptyList(),
        groupIds = emptyList(),
        friendUids = emptyList())

private val friend3Profile: Profile =
    Profile(
        uid = "3",
        name = "Friend3",
        focusSessionIds = emptyList(),
        participatingEventIds = emptyList(),
        groupIds = emptyList(),
        friendUids = emptyList())

private val friend4Profile: Profile =
    Profile(
        uid = "4",
        name = "Friend4",
        focusSessionIds = emptyList(),
        participatingEventIds = emptyList(),
        groupIds = emptyList(),
        friendUids = emptyList())

private var currentProfile: Profile =
    Profile(
        uid = "",
        name = "Current",
        focusSessionIds = emptyList(),
        participatingEventIds = emptyList(),
        groupIds = emptyList(),
        friendUids =
            listOf(friend1Profile.uid, friend2Profile.uid, friend3Profile.uid, friend4Profile.uid))

private var friendlessProfile: Profile =
    Profile(
        uid = "",
        name = "Current",
        focusSessionIds = emptyList(),
        participatingEventIds = emptyList(),
        groupIds = emptyList(),
        friendUids = emptyList())

private val friendsList = listOf(friend1Profile, friend2Profile, friend3Profile, friend4Profile)

/*----------------------------------------ToDos-----------------------------------------------*/

private var todo1: ToDo =
    ToDo(
        uid = "todo1",
        name = "Todo1",
        description = "Desc",
        dueDate = Timestamp(1728000000, 0),
        dueTime = null,
        location = Location(latitude = 46.5186, longitude = 6.5661, name = "Rolex Learning Center"),
        status = ToDoStatus.ONGOING,
        ownerId = "")

private var todo2: ToDo =
    ToDo(
        uid = "todo2",
        name = "Todo2",
        description = "Desc",
        dueDate = Timestamp(1728000000, 0),
        dueTime = null,
        location = Location(latitude = 46.5190, longitude = 6.5668, name = "BC Building"),
        status = ToDoStatus.ONGOING,
        ownerId = "")

private var todo3: ToDo =
    ToDo(
        uid = "todo3",
        name = "Todo3",
        description = "Desc",
        dueDate = Timestamp(1728000000, 0),
        dueTime = null,
        location = null,
        status = ToDoStatus.ONGOING,
        ownerId = "")

private var todo4: ToDo =
    ToDo(
        uid = "todo4",
        name = "Todo4",
        description = "Desc",
        dueDate = Timestamp(1728000000, 0),
        dueTime = null,
        location = null,
        status = ToDoStatus.ONGOING,
        ownerId = "")

/*----------------------------------------Events----------------------------------------------*/

private val tomorrowTimestamp = Timestamp(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)))

private val startTime = Timestamp(SimpleDateFormat("HH:mm").parse("10:00")!!)
private val endTime = Timestamp(SimpleDateFormat("HH:mm").parse("23:00")!!)

private val event1 =
    Event(
        id = "1",
        title = "Celebrate Christmas 2025",
        description = "Come celebrate christmas with us in a few months :)",
        creatorName = "Gatherly team",
        location = Location(latitude = 46.5190, longitude = 6.5668, name = "BC Building"),
        date = tomorrowTimestamp,
        startTime = startTime,
        endTime = endTime,
        creatorId = "gersende",
        participants =
            listOf("gersende", "colombe", "claire", "gab", "alessandro", "clau", "mohamed"),
        status = EventStatus.UPCOMING)

private val event2 =
    Event(
        id = "2",
        title = "Celebrate Christmas 2025",
        description = "Come celebrate christmas with us in a few months :)",
        creatorName = "Gatherly team",
        location = null,
        date = tomorrowTimestamp,
        startTime = startTime,
        endTime = endTime,
        creatorId = "gersende",
        participants =
            listOf("gersende", "colombe", "claire", "gab", "alessandro", "clau", "mohamed"),
        status = EventStatus.UPCOMING)

private val displayableEvents = listOf(event1)

@OptIn(ExperimentalCoroutinesApi::class)
class HomePageViewModelTest {
  // declare viewModel and repositories
  private lateinit var homePageViewModel: HomePageViewModel
  private lateinit var eventsRepository: EventsRepository
  private lateinit var toDosRepository: ToDosRepository
  private lateinit var profileRepository: ProfileRepository
  private lateinit var notificationsRepository: NotificationsRepository
  private lateinit var pointsRepository: PointsRepository
  private lateinit var displayableTodos: List<ToDo>
  private lateinit var upcomingTodos: List<ToDo>
  private lateinit var mockitoUtils: MockitoUtils

  // initialize this so that tests control all coroutines and can wait on them
  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setUp() {
    // so that tests can wait on coroutines
    Dispatchers.setMain(testDispatcher)
    displayableTodos = listOf(todo1, todo2)
    upcomingTodos = listOf(todo1, todo2, todo3, todo4)

    // initialize repos and viewModel
    profileRepository = ProfileLocalRepository()
    eventsRepository = EventsLocalRepository()
    toDosRepository = ToDosLocalRepository()
    notificationsRepository = NotificationsLocalRepository()
    pointsRepository = PointsLocalRepository()

    // fill the profile and events repositories with profiles and event
    fill_repositories()

    // Mock Firebase Auth
    mockitoUtils = MockitoUtils()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  /*-------------------------------------Tests--------------------------------------------------*/

  @Test
  fun correctSetValuesForNormalUser() = runTest {
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

  @Test
  fun correctSetValuesForNoFriendsUser() = runTest {
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

  @Test
  fun correctSetValuesForNoTodosUser() = runTest {
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
  // This function fills the profile repository with the created profiles
  fun fill_repositories() {
    runTest {
      profileRepository.addProfile(friend1Profile)
      profileRepository.addProfile(friend2Profile)
      profileRepository.addProfile(friend3Profile)
      profileRepository.addProfile(friend4Profile)
      eventsRepository.addEvent(event1)
      eventsRepository.addEvent(event2)
      advanceUntilIdle()
    }
  }

  // Adds a user with the current firebase id and 4 friends
  suspend fun addFriendsCurrentUser() {
    profileRepository.addProfile(currentProfile)
  }

  // Adds a user with the current firebase id and no friends
  suspend fun addFriendlessCurrentUser() {
    profileRepository.addProfile(friendlessProfile)
  }

  suspend fun addTodos() {
    toDosRepository.addTodo(todo1)
    toDosRepository.addTodo(todo2)
    toDosRepository.addTodo(todo3)
    toDosRepository.addTodo(todo4)
  }
}
