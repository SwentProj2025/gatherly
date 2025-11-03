package com.android.gatherly.ui.homepage

import com.android.gatherly.model.event.Event
import com.android.gatherly.model.event.EventStatus
import com.android.gatherly.model.event.EventsLocalRepository
import com.android.gatherly.model.event.EventsRepository
import com.android.gatherly.model.map.Location
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todo.ToDosRepository
import com.android.gatherly.model.todo.ToDosRepositoryLocalMapTest
import com.android.gatherly.ui.homePage.HomePageViewModel
import com.android.gatherly.utils.FirebaseEmulator
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import java.text.SimpleDateFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
            listOf(friend1Profile.uid, friend2Profile.uid, friend3Profile.uid, friend4Profile.uid))

private var friendlessProfile: Profile =
    Profile(
        uid = "0-",
        name = "Current",
        focusSessionIds = emptyList(),
        eventIds = emptyList(),
        groupIds = emptyList(),
        friendUids = emptyList())

private val friendsList = listOf(friend1Profile, friend2Profile, friend3Profile)

/*----------------------------------------ToDos-----------------------------------------------*/

private var todo1: ToDo =
    ToDo(
        uid = "todo1",
        name = "Todo1",
        description = "Desc",
        assigneeName = "Assignee",
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
        assigneeName = "Assignee",
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
        assigneeName = "Assignee",
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
        assigneeName = "Assignee",
        dueDate = Timestamp(1728000000, 0),
        dueTime = null,
        location = null,
        status = ToDoStatus.ONGOING,
        ownerId = "")

/*----------------------------------------Events----------------------------------------------*/

private val mexicoGP = Timestamp(SimpleDateFormat("dd/MM/yyyy").parse("26/10/2023")!!)
private val startTime = Timestamp(SimpleDateFormat("HH:mm").parse("10:00")!!)
private val endTime = Timestamp(SimpleDateFormat("HH:mm").parse("23:00")!!)

private val event1 =
    Event(
        id = "1",
        title = "Celebrate Christmas 2025",
        description = "Come celebrate christmas with us in a few months :)",
        creatorName = "Gatherly team",
        location = Location(latitude = 46.5190, longitude = 6.5668, name = "BC Building"),
        date = mexicoGP,
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
        date = mexicoGP,
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
  private lateinit var displayableTodos: List<ToDo>
  private lateinit var upcomingTodos: List<ToDo>

  // initialize this so that tests control all coroutines and can wait on them
  private val testDispatcher = UnconfinedTestDispatcher()

  @Before
  fun setUp() {
    // so that tests can wait on coroutines
    Dispatchers.setMain(testDispatcher)

    if (!FirebaseEmulator.isRunning) {
      error("Firebase emulator must be running! Use: firebase emulators:start")
    }

    runTest {
      FirebaseEmulator.auth.signInAnonymously().await()

      todo1 = todo1.copy(ownerId = Firebase.auth.currentUser?.uid!!)
      todo2 = todo2.copy(ownerId = Firebase.auth.currentUser?.uid!!)
      todo3 = todo3.copy(ownerId = Firebase.auth.currentUser?.uid!!)
      todo4 = todo4.copy(ownerId = Firebase.auth.currentUser?.uid!!)
      currentProfile = currentProfile.copy(uid = Firebase.auth.currentUser?.uid!!)
      friendlessProfile = friendlessProfile.copy(uid = Firebase.auth.currentUser?.uid!!)
      displayableTodos = listOf(todo1, todo2)
      upcomingTodos = listOf(todo1, todo2, todo3)

      // initialize repos and viewModel
      profileRepository = ProfileLocalRepository()
      eventsRepository = EventsLocalRepository()
      toDosRepository = ToDosRepositoryLocalMapTest()

      // fill the profile and events repositories with profiles and event
      fill_repositories()
      advanceUntilIdle()
    }
  }

  @After
  fun tearDown() {
    FirebaseEmulator.clearAuthEmulator()
    FirebaseEmulator.clearFirestoreEmulator()
    Dispatchers.resetMain()
  }

  /*-------------------------------------Tests--------------------------------------------------*/

  @Test
  fun correctSetValuesForNormalUser() = runTest {
    addFriendsCurrentUser()
    addTodos()

    homePageViewModel =
        HomePageViewModel(
            eventsRepository = eventsRepository,
            toDosRepository = toDosRepository,
            profileRepository = profileRepository)

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

    homePageViewModel =
        HomePageViewModel(
            eventsRepository = eventsRepository,
            toDosRepository = toDosRepository,
            profileRepository = profileRepository)

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
    homePageViewModel =
        HomePageViewModel(
            eventsRepository = eventsRepository,
            toDosRepository = toDosRepository,
            profileRepository = profileRepository)

    advanceUntilIdle()

    assert(homePageViewModel.uiState.value.displayableTodos == emptyList<ToDo>()) {
      "Actual : ${homePageViewModel.uiState.value.displayableTodos}"
    }
    assert(homePageViewModel.uiState.value.displayableEvents == displayableEvents)
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
  fun addFriendsCurrentUser() {
    runTest {
      profileRepository.addProfile(currentProfile)
      advanceUntilIdle()
    }
  }

  // Adds a user with the current firebase id and no friends
  fun addFriendlessCurrentUser() {
    runTest {
      profileRepository.addProfile(friendlessProfile)
      advanceUntilIdle()
    }
  }

  fun addTodos() {
    runTest {
      toDosRepository.addTodo(todo1)
      toDosRepository.addTodo(todo2)
      toDosRepository.addTodo(todo3)
      toDosRepository.addTodo(todo4)
      advanceUntilIdle()
    }
  }
}
