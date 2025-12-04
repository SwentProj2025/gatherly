package com.android.gatherly.viewmodel.badge

import com.android.gatherly.R
import com.android.gatherly.model.badge.BadgeType
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.ui.badge.BadgeUI
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
        listOf<BadgeUI>(
            BadgeUI(
                "Starting ToDo Created Badge",
                "You created 1 ToDo!",
                R.drawable.starting_todo_created),
            BadgeUI(
                "Bronze ToDo Created Badge",
                "You created 3 ToDos!",
                R.drawable.bronze_todo_created),
            BadgeUI(
                "Silver ToDo Created Badge",
                "You created 5 ToDos!",
                R.drawable.silver_todo_created),
            BadgeUI("???", "???", R.drawable.blank_todo_created)),
        uiState.value.badgesByType[BadgeType.TODOS_CREATED])
    assertEquals(
        listOf(
            BadgeUI(
                "Starting ToDo Completed Badge",
                "You completed 1 ToDo!",
                R.drawable.starting_todo_completed),
            BadgeUI(
                "Bronze ToDo Completed Badge",
                "You completed 3 ToDos!",
                R.drawable.bronze_todo_completed),
            BadgeUI(
                "Silver ToDo Completed Badge",
                "You completed 5 ToDos!",
                R.drawable.silver_todo_completed),
            BadgeUI(
                "Gold ToDo Completed Badge",
                "You completed 10 ToDos!",
                R.drawable.gold_todo_completed),
            BadgeUI(
                "Diamond ToDo Completed Badge",
                "You completed 20 ToDos!",
                R.drawable.diamond_todo_completed),
            BadgeUI("???", "???", R.drawable.blank_todo_completed)),
        uiState.value.badgesByType[BadgeType.TODOS_COMPLETED])
    assertEquals(
        listOf(
            BadgeUI(
                "Starting Event Created Badge",
                "You created 1 Event!",
                R.drawable.starting_event_created),
            BadgeUI(
                "Bronze Event Created Badge",
                "You created 3 Events!",
                R.drawable.bronze_event_created),
            BadgeUI("???", "???", R.drawable.blank_event_created)),
        uiState.value.badgesByType[BadgeType.EVENTS_CREATED])
    assertEquals(
        listOf(BadgeUI("???", "???", R.drawable.blank_event_participated)),
        uiState.value.badgesByType[BadgeType.EVENTS_PARTICIPATED])
    assertEquals(
        listOf(BadgeUI("???", "???", R.drawable.blank_friends)),
        uiState.value.badgesByType[BadgeType.FRIENDS_ADDED])
    assertEquals(
        listOf(BadgeUI("???", "???", R.drawable.blank_focus_session)),
        uiState.value.badgesByType[BadgeType.FOCUS_SESSIONS_COMPLETED])
  }
}
