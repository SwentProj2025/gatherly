package com.android.gatherly.ui.profile

import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileRepositoryFirestore
import com.android.gatherly.utils.FirebaseEmulator
import com.android.gatherly.utils.FirestoreGatherlyProfileTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test

private const val TIMEOUT = 30_000L
private const val DELAY = 200L

/**
 * Integration tests for [ProfileViewModel] using the Firebase Emulators.
 *
 * These tests verify:
 * - ProfileViewModel correctly loads an existing profile from Firestore.
 * - Proper error states are emitted for missing or unauthenticated users.
 *
 * Firestore and Auth emulators must be running locally before executing: firebase emulators:start
 */
@OptIn(ExperimentalCoroutinesApi::class)
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

    // Wait until loading completes and profile is available
    withContext(Dispatchers.Default.limitedParallelism(1)) {
      withTimeout(TIMEOUT) {
        while (viewModel.uiState.value.isLoading || viewModel.uiState.value.profile == null) {
          delay(DELAY)
        }
      }
    }

    val state = viewModel.uiState.value
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

    // Wait until loading completes and an error appears
    withContext(Dispatchers.Default.limitedParallelism(1)) {
      withTimeout(TIMEOUT) {
        while (viewModel.uiState.value.isLoading && viewModel.uiState.value.errorMessage == null) {
          delay(DELAY)
        }
      }
    }

    val state = viewModel.uiState.value
    assertNull(state.profile)
    assertEquals("Profile not found", state.errorMessage)
  }

  @Test
  fun loadUserProfile_returnsErrorIfUserNotAuthenticated() = runTest {
    FirebaseEmulator.auth.signOut()

    viewModel = ProfileViewModel(repository)
    viewModel.loadUserProfile()

    // Wait until loading completes and an error appears
    withContext(Dispatchers.Default.limitedParallelism(1)) {
      withTimeout(TIMEOUT) {
        while (viewModel.uiState.value.isLoading || viewModel.uiState.value.errorMessage == null) {
          delay(DELAY)
        }
      }
    }

    val state = viewModel.uiState.value
    assertNull(state.profile)
    assertEquals("User not authenticated", state.errorMessage)
  }
}
