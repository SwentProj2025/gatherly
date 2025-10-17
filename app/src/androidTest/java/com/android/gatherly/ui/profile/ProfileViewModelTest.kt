package com.android.gatherly.ui.profile

import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileRepositoryFirestore
import com.android.gatherly.utils.FirebaseEmulator
import com.android.gatherly.utils.FirestoreGatherlyProfileTest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * Integration tests for [ProfileViewModel] using the Firebase Emulator Suite.
 *
 * These tests verify:
 * - ProfileViewModel correctly loads an existing profile from Firestore.
 * - Proper error states are emitted for missing or unauthenticated users.
 *
 * Firestore and Auth emulators must be running locally before executing: firebase emulators:start
 */
class ProfileViewModelIntegrationTest : FirestoreGatherlyProfileTest() {

  private lateinit var viewModel: ProfileViewModel

  @Test
  fun loadUserProfile_successfullyLoadsExistingProfile() = runTest {
    val uid = FirebaseEmulator.auth.currentUser!!.uid
    repository.initProfileIfMissing(uid, "pic.png")

    val profile = Profile(uid = uid, name = "Alice", school = "EPFL", profilePicture = "alice.png")
    repository.updateProfile(profile)

    viewModel = ProfileViewModel(repository)
    viewModel.loadUserProfile()

    val state = viewModel.uiState.first { !it.isLoading }
    assertNotNull(state.profile)
    assertEquals("Alice", state.profile!!.name)
    assertEquals("EPFL", state.profile!!.school)
    assertNull(state.errorMessage)
  }

  @Test
  fun loadUserProfile_returnsErrorIfProfileMissing() = runTest {
    FirebaseEmulator.clearFirestoreEmulator()
    FirebaseEmulator.auth.signInAnonymously().await()

    viewModel = ProfileViewModel(ProfileRepositoryFirestore(FirebaseEmulator.firestore))
    viewModel.loadUserProfile()

    val state = viewModel.uiState.first { !it.isLoading }
    assertNull(state.profile)
    assertEquals("Profile not found", state.errorMessage)
  }

  @Test
  fun loadUserProfile_returnsErrorIfUserNotAuthenticated() = runTest {
    FirebaseEmulator.auth.signOut()

    viewModel = ProfileViewModel(repository)
    viewModel.loadUserProfile()

    delay(500)

    val state = viewModel.uiState.first { !it.isLoading }
    assertNull(state.profile)
    assertEquals("User not authenticated", state.errorMessage)
  }
}
