package com.android.gatherly.ui.settings

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileRepositoryFirestore
import com.android.gatherly.utils.FirebaseEmulator
import com.android.gatherly.utils.FirestoreGatherlyTest
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Integration tests for [SettingsViewModel] using the real Firestore repository (emulator). */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class SettingsViewModelInstrumentedTest : FirestoreGatherlyTest() {

  private lateinit var repo: ProfileRepositoryFirestore
  private lateinit var viewModel: SettingsViewModel
  private lateinit var testUid: String

  @Before
  override fun setUp() {
    super.setUp()
    repo = ProfileRepositoryFirestore(FirebaseEmulator.firestore, FirebaseEmulator.storage)
    viewModel = SettingsViewModel(repo)

    // Create a test user (anonymous is fine)
    runBlocking {
      val auth = Firebase.auth
      val user = auth.signInAnonymously().result?.user
      testUid = user?.uid ?: throw IllegalStateException("Auth failed")
      repo.initProfileIfMissing(testUid, "")
    }
  }

  /** Ensures loading an existing profile from Firestore populates the UI state correctly. */
  @Test
  fun loadProfile_populatesUiStateFromFirestore() = runBlocking {
    val profile =
        Profile(
            uid = testUid,
            name = "Alice",
            username = "alice_test",
            school = "EPFL",
            schoolYear = "BA3")
    repo.updateProfile(profile)

    viewModel.loadProfile(testUid)
    withContext(Dispatchers.Default.limitedParallelism(1)) {
      withTimeout(5000L) {
        while (viewModel.uiState.first().name.isEmpty()) {
          delay(50)
        }
      }
    }

    val state = viewModel.uiState.first()
    assertEquals("Alice", state.name)
    assertEquals("alice_test", state.username)
    assertEquals("EPFL", state.school)
  }

  /** Ensures that editing a username updates UI state and validates format correctly. */
  @Test
  fun editUsername_setsValidationMessagesCorrectly() = runBlocking {
    viewModel.editUsername("Bad!!Name")
    var state = viewModel.uiState.first()
    assertNotNull(state.invalidUsernameMsg)
    assertNull(state.isUsernameAvailable)

    viewModel.editUsername("good_name")
    withContext(Dispatchers.Default.limitedParallelism(1)) {
      withTimeout(5000L) {
        while (viewModel.uiState.first().isUsernameAvailable == null) {
          delay(50)
        }
      }
    }
    state = viewModel.uiState.first()
    assertNull(state.invalidUsernameMsg)
    assertNotNull(state.isUsernameAvailable)
  }

  /** Ensures that an invalid name triggers an error in the ViewModel. */
  @Test
  fun editName_invalid_setsErrorMessage() = runBlocking {
    viewModel.editName("")
    val state = viewModel.uiState.first()
    assertEquals("Name cannot be empty", state.invalidNameMsg)
    assertFalse(state.isValid)
  }

  /** Ensures that a valid updateProfile() correctly persists to Firestore. */
  @Test
  fun updateProfile_persistsToFirestore() = runBlocking {
    // First load and set valid values
    viewModel.loadProfile(testUid)
    withContext(Dispatchers.Default.limitedParallelism(1)) {
      withTimeout(5000L) {
        while (viewModel.uiState.value.isLoadingProfile) {
          delay(50)
        }
      }
    }

    viewModel.editName("Updated Name")
    viewModel.editUsername("updated_user")
    withContext(Dispatchers.Default.limitedParallelism(1)) {
      withTimeout(5000L) {
        while (viewModel.uiState.first().isUsernameAvailable == null) {
          delay(50)
        }
      }
    }
    viewModel.editSchool("EPFL")
    viewModel.editSchoolYear("MA1")

    viewModel.updateProfile(testUid, isFirstTime = true)
    withContext(Dispatchers.Default.limitedParallelism(1)) {
      withTimeout(5000L) {
        while (repo.getProfileByUid(testUid)?.name != "Updated Name") {
          delay(50)
        }
      }
    }
    // Verify persistence
    val stored = repo.getProfileByUid(testUid)
    assertEquals("Updated Name", stored?.name)
    assertEquals("updated_user", stored?.username)
    assertEquals("EPFL", stored?.school)
    assertEquals("MA1", stored?.schoolYear)
  }

  /** Ensures invalid birthday prevents profile from being valid. */
  @Test
  fun editBirthday_invalidDate_setsError() = runBlocking {
    viewModel.editBirthday("31-31-2025")
    val state = viewModel.uiState.first()
    assertEquals("Date is not valid (format: dd/mm/yyyy)", state.invalidBirthdayMsg)
    assertFalse(state.isValid)
  }

  /** Ensures valid birthday passes validation. */
  @Test
  fun editBirthday_validDate_clearsError() = runBlocking {
    viewModel.editBirthday("17/07/2000")
    val state = viewModel.uiState.first()
    assertNull(state.invalidBirthdayMsg)
  }

  /** Ensures an invalid username prevents updateProfile() from running. */
  @Test
  fun updateProfile_invalidUsername_blocksSave() = runBlocking {
    viewModel.editName("Test")
    viewModel.editUsername("Bad!!Name")

    viewModel.updateProfile(testUid, isFirstTime = true)
    withContext(Dispatchers.Default.limitedParallelism(1)) {
      withTimeout(5000L) {
        while (viewModel.uiState.first().errorMsg == null) {
          delay(50)
        }
      }
    }
    val state = viewModel.uiState.first()
    assertEquals("At least one field is not valid.", state.errorMsg)
  }
}
