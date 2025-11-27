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
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [BadgeViewModel] using a fake in-memory repository.
 *
 * These tests verify event list categorization, user participation flows, and event editing
 * functionality.
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

  @Test fun uiStateIsCorrectlyInstantiated() = runTest {}
}
