package com.android.gatherly.viewmodel.profile

import com.android.gatherly.model.group.GroupsLocalRepository
import com.android.gatherly.model.group.GroupsRepository
import com.android.gatherly.model.notification.NotificationsLocalRepository
import com.android.gatherly.model.notification.NotificationsRepository
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.ui.profile.ProfileViewModel
import com.android.gatherly.utilstest.MockitoUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.not

/**
 * Integration tests for [com.android.gatherly.ui.profile.ProfileViewModel] using the Firebase
 * Emulators.
 *
 * These tests verify:
 * - ProfileViewModel correctly loads an existing profile from Firestore.
 * - Proper error states are emitted for missing or unauthenticated users.
 *
 * Firestore and Auth emulators must be running locally before executing: firebase emulators:start
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelIntegrationTest {

  private lateinit var profileViewModel: ProfileViewModel
  private lateinit var profileRepository: ProfileRepository
  private lateinit var groupsRepository: GroupsRepository
  private lateinit var notificationsRepository: NotificationsRepository
  private lateinit var mockitoUtils: MockitoUtils

  // initialize this so that tests control all coroutines and can wait on them
  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setUp() {
    // so that tests can wait on coroutines
    Dispatchers.setMain(testDispatcher)

    // Mock Firebase Auth
    mockitoUtils = MockitoUtils()

    // initialize repos and profileViewModel
    profileRepository = ProfileLocalRepository()
    groupsRepository = GroupsLocalRepository()
    notificationsRepository = NotificationsLocalRepository()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun loadUserProfile_successfullyLoadsExistingProfile() = runTest {
    val uid = "currentUser"
    profileRepository.initProfileIfMissing(uid, "pic.png")

    val profile = Profile(uid = uid, name = "Alice", school = "EPFL", profilePicture = "alice.png")
    profileRepository.updateProfile(profile)

    mockitoUtils.chooseCurrentUser(uid)

    profileViewModel =
        ProfileViewModel(
            profileRepository = profileRepository,
            groupsRepository = groupsRepository,
            notificationsRepository = notificationsRepository,
            authProvider = { mockitoUtils.mockAuth })
    profileViewModel.loadUserProfile()

    // Wait until loading completes and profile is available
    advanceUntilIdle()

    val state = profileViewModel.uiState.value
    assertNotNull(state.profile)
    assertEquals("Alice", state.profile!!.name)
    assertEquals("EPFL", state.profile!!.school)
    assertNull(state.errorMessage)
  }

  @Test
  fun loadUserProfile_returnsErrorIfProfileMissing() = runTest {
    val uid = "currentUser"

    mockitoUtils.chooseCurrentUser(uid)

    profileViewModel =
        ProfileViewModel(
            profileRepository = profileRepository,
            groupsRepository = groupsRepository,
            notificationsRepository = notificationsRepository,
            authProvider = { mockitoUtils.mockAuth })
    profileViewModel.loadUserProfile()

    // Wait until loading completes and an error appears
    advanceUntilIdle()

    val state = profileViewModel.uiState.value
    assertNull(state.profile)
    assertEquals("Profile not found", state.errorMessage)
  }

  @Test
  fun loadUserProfile_returnsErrorIfUserNotAuthenticated() = runTest {
    mockitoUtils.unauthenticatedCurrentUser()

    profileViewModel =
        ProfileViewModel(
            profileRepository = profileRepository,
            groupsRepository = groupsRepository,
            notificationsRepository = notificationsRepository,
            authProvider = { mockitoUtils.mockAuth })
    profileViewModel.loadUserProfile()

    // Wait until loading completes and an error appears
    advanceUntilIdle()

    val state = profileViewModel.uiState.value
    assertNull(state.profile)
    assertEquals("User not authenticated", state.errorMessage)
  }
}
