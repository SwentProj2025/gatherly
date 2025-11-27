package com.android.gatherly.viewmodel.profile

import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.ui.profile.UserProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserProfileViewModelTest {

  private lateinit var viewModel: UserProfileViewModel
  private lateinit var repository: ProfileRepository
  private val testDispatcher = StandardTestDispatcher()

  private val testProfile =
      Profile(
          uid = "userProfileVMTest_user123",
          name = "Test User",
          username = "userProfileVM_testUser",
          school = "EPFL",
          schoolYear = "2025",
          profilePicture = "profile.png")

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    repository = ProfileLocalRepository()
    runBlocking { repository.addProfile(testProfile) }
    viewModel = UserProfileViewModel(repository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }
  /** Verifies a Profile loads correctly * */
  @Test
  fun loadUserProfile_successfullyLoadsProfile() = runTest {
    viewModel.loadUserProfile(testProfile.uid)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNotNull(state.profile)
    assertEquals(testProfile.name, state.profile!!.name)
    assertEquals(testProfile.school, state.profile!!.school)
    assertEquals(testProfile.schoolYear, state.profile!!.schoolYear)
    assertNull(state.errorMessage)
  }
  /** Verifies loading a non-existing profile sets and errorMessage * */
  @Test
  fun loadUserProfile_returnsErrorForMissingProfile() = runTest {
    viewModel.loadUserProfile("missingUser")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNull(state.profile)
    assertEquals("Profile not found", state.errorMessage)
  }
  /** Tests the clear Error Message Function* */
  @Test
  fun clearErrorMsg_clearsErrorMessage() = runTest {
    viewModel.loadUserProfile("missingUser")
    advanceUntilIdle()
    assertNotNull(viewModel.uiState.value.errorMessage)

    viewModel.clearErrorMsg()
    val state = viewModel.uiState.value
    assertNull(state.errorMessage)
  }
}
