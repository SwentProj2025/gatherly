package com.android.gatherly.viewmodel.badge

import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.ui.badge.BadgeViewModel
import com.android.gatherly.utilstest.MockitoUtils
import com.android.gatherly.viewmodel.event.EventsViewModelTestsData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [BadgeViewModel] using a fake in-memory repository.
 *
 * This class verifies that the badgeViewModel correctly receives and sends the badges' information
 * to the BadgeScreen
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BadgeViewModelTest {

  private val testDispatcher = StandardTestDispatcher()

  private lateinit var profileRepo: ProfileLocalRepository
  private lateinit var vm: BadgeViewModel
  private lateinit var mockitoUtils: MockitoUtils

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    profileRepo = ProfileLocalRepository()
    fillRepository()

    // Mock Firebase Auth
    mockitoUtils = MockitoUtils()
    mockitoUtils.chooseCurrentUser(EventsViewModelTestsData.TEST_USER_ID)

    vm = BadgeViewModel(repository = profileRepo, authProvider = { mockitoUtils.mockAuth })
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  private fun fillRepository() = runTest {
    profileRepo.addProfile(
        Profile(
            uid = "testUser123",
            name = "Test User",
            profilePicture = "",
            badgeIds =
                listOf(
                    "bronze_TodosCreated",
                    "silver_TodosCreated",
                    "starting_TodosCreated",
                    "bronze_EventsCreated",
                    "starting_EventsCreated",
                    "bronze_TodosCompleted",
                    "starting_TodosCompleted",
                    "silver_TodosCompleted",
                    "gold_TodosCompleted",
                    "diamond_TodosCompleted")))
    advanceUntilIdle()
  }

  /**
   * Checks that the highest ranked badge is sent to the UI and that non obtained badge are
   * correctly set to blank
   */
  @Test
  fun uiStateIsCorrectlyInstantiated() = runTest {
    vm.refresh()

    val uiState = vm.uiState

    assertEquals(
        Triple(
            "Silver ToDo Created Badge",
            "You created 5 ToDos!",
            "app/src/main/res/drawable/badges/todos/Silver Todo Created.png"),
        uiState.value.badgeTodoCreated)
    assertEquals(
        Triple(
            "Diamond ToDo Completed Badge",
            "You completed 20 ToDos!",
            "app/src/main/res/drawable/badges/todos/Diamond Todo Completed.png"),
        uiState.value.badgeTodoCompleted)
    assertEquals(
        Triple(
            "Bronze Event Created Badge",
            "You created 3 Events!",
            "app/src/main/res/drawable/badges/events/Bronze Event Created.png"),
        uiState.value.badgeEventCreated)
    assertEquals(
        Triple(
            "Blank Event Participated Badge",
            "Participate to your first Todo to get a Badge!",
            "app/src/main/res/drawable/badges/events/Blank Events.png"),
        uiState.value.badgeEventParticipated)
    assertEquals(
        Triple(
            "Blank Friend Badge",
            "Add your first Friend to get a Badge!",
            "app/src/main/res/drawable/badges/friends/Blank Friends.png"),
        uiState.value.badgeFriendAdded)
    assertEquals(
        Triple(
            "Blank Focus Session Badge",
            "Complete your first Focus Session to get a Badge!",
            "app/src/main/res/drawable/badges/focusSessions/Blank FocusSession.png"),
        uiState.value.badgeFocusSessionCompleted)
  }
}
