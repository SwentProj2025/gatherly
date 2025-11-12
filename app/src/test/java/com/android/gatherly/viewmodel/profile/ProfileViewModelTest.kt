package com.android.gatherly.viewmodel.profile

import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.ui.profile.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

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
  private lateinit var mockAuth: FirebaseAuth
  private lateinit var mockUser: FirebaseUser

  // initialize this so that tests control all coroutines and can wait on them
  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setUp() {
    // so that tests can wait on coroutines
    Dispatchers.setMain(testDispatcher)

    // Mock Firebase Auth
    mockAuth = mock(FirebaseAuth::class.java)
    mockUser = mock(FirebaseUser::class.java)

    // initialize repos and profileViewModel
    profileRepository = ProfileLocalRepository()
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

    `when`(mockAuth.currentUser).thenReturn(mockUser)
    `when`(mockUser.uid).thenReturn(uid)
    `when`(mockUser.isAnonymous).thenReturn(false)

    profileViewModel = ProfileViewModel(repository = profileRepository, authProvider = { mockAuth })
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

    `when`(mockAuth.currentUser).thenReturn(mockUser)
    `when`(mockUser.uid).thenReturn(uid)
    `when`(mockUser.isAnonymous).thenReturn(false)

    profileViewModel = ProfileViewModel(repository = profileRepository, authProvider = { mockAuth })
    profileViewModel.loadUserProfile()

    // Wait until loading completes and an error appears
    advanceUntilIdle()

    val state = profileViewModel.uiState.value
    assertNull(state.profile)
    assertEquals("Profile not found", state.errorMessage)
  }

  @Test
  fun loadUserProfile_returnsErrorIfUserNotAuthenticated() = runTest {
    `when`(mockAuth.currentUser).thenReturn(mockUser)
    `when`(mockUser.uid).thenReturn(null)
    `when`(mockUser.isAnonymous).thenReturn(false)

    profileViewModel = ProfileViewModel(repository = profileRepository, authProvider = { mockAuth })
    profileViewModel.loadUserProfile()

    // Wait until loading completes and an error appears
    advanceUntilIdle()

    val state = profileViewModel.uiState.value
    assertNull(state.profile)
    assertEquals("User not authenticated", state.errorMessage)
  }
}
