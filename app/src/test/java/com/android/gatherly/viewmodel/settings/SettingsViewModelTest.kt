package com.android.gatherly.viewmodel.settings

import android.net.Uri
import androidx.credentials.CredentialManager
import com.android.gatherly.model.event.EventsLocalRepository
import com.android.gatherly.model.focusSession.FocusSessionsLocalRepository
import com.android.gatherly.model.group.GroupsLocalRepository
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.profile.ProfileStatus
import com.android.gatherly.model.profile.UserStatusManager
import com.android.gatherly.model.profile.UserStatusSource
import com.android.gatherly.model.todo.ToDosLocalRepository
import com.android.gatherly.ui.settings.SettingsViewModel
import com.android.gatherly.utilstest.MockitoUtils
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.mock

/** Unit tests for [SettingsViewModel]. */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

  private val testDispatcher = UnconfinedTestDispatcher()
  private lateinit var profileLocalRepository: ProfileLocalRepository
  private lateinit var groupsLocalRepository: GroupsLocalRepository
  private lateinit var eventsLocalRepository: EventsLocalRepository
  private lateinit var focusSessionsLocalRepository: FocusSessionsLocalRepository
  private lateinit var toDosLocalRepository: ToDosLocalRepository
  private lateinit var viewModel: SettingsViewModel
  private lateinit var mockitoUtils: MockitoUtils
  private lateinit var statusManagerMock: UserStatusManager
  private lateinit var credentialManager: CredentialManager
  private val testTimeout = 120.seconds

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    profileLocalRepository = ProfileLocalRepository()
    groupsLocalRepository = GroupsLocalRepository()
    eventsLocalRepository = EventsLocalRepository()
    focusSessionsLocalRepository = FocusSessionsLocalRepository()
    toDosLocalRepository = ToDosLocalRepository()
    statusManagerMock = mock()
    fill_repository()
    credentialManager = mock()
    mockitoUtils = MockitoUtils()
    mockitoUtils.chooseCurrentUser("currentUser")

    viewModel =
        SettingsViewModel(
            profileRepository = profileLocalRepository,
            userStatusManager = statusManagerMock,
            authProvider = { mockitoUtils.mockAuth },
            groupsRepository = groupsLocalRepository,
            eventsRepository = eventsLocalRepository,
            focusSessionsRepository = focusSessionsLocalRepository,
            todosRepository = toDosLocalRepository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  fun fill_repository() =
      runTest(testDispatcher, testTimeout) {
        profileLocalRepository.initProfileIfMissing("currentUser", "")
        advanceUntilIdle()
      }

  // ------------------------------------------------------------------------
  // PROFILE LOADING
  // ------------------------------------------------------------------------

  /** Tests that loading an existing profile correctly populates the UI state. */
  @Test
  fun loadProfile_LoadsExistingProfileIntoUiState() =
      runTest(testDispatcher, testTimeout) {
        val profile =
            Profile(
                uid = "u1",
                name = "Alice",
                username = "alice_ok",
                school = "Harvard",
                schoolYear = "3",
                bio = "settingsTestBio")
        profileLocalRepository.addProfile(profile)

        viewModel.loadProfile("u1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Alice", state.name)
        assertEquals("alice_ok", state.username)
        assertEquals("Harvard", state.school)
        assertEquals("3", state.schoolYear)
        assertEquals(profile.bio, state.bio)
        assertNull(state.errorMsg)
      }

  /** Tests that loading a missing profile creates a default profile with empty fields. */
  @Test
  fun loadProfile_WhenMissing_CreatesDefaultProfile() =
      runTest(testDispatcher, testTimeout) {
        viewModel.loadProfile("new_user")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("", state.name)
        assertEquals("", state.username)
        assertFalse(state.isValid)
        assertNull(state.errorMsg)
      }

  // ------------------------------------------------------------------------
  // FIELD VALIDATION
  // ------------------------------------------------------------------------

  /** Tests that editing the name to a blank value sets an invalid name message. */
  @Test
  fun editName_WithBlankName_SetsInvalidMessage() =
      runTest(testDispatcher, testTimeout) {
        viewModel.editName("")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Name cannot be empty", state.invalidNameMsg)
        assertFalse(state.isValid)
      }

  /** Tests that an invalid date format sets an appropriate error message. */
  @Test
  fun editBirthday_WithInvalidDate_SetsErrorMessage() =
      runTest(testDispatcher, testTimeout) {
        viewModel.editBirthday("99/99/9999")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Date is not valid (format: dd/mm/yyyy)", state.invalidBirthdayMsg)
        assertFalse(state.isValid)
      }

  /** Tests that a valid date format clears the error message. */
  @Test
  fun editBirthday_WithValidDate_ClearsErrorMessage() =
      runTest(testDispatcher, testTimeout) {
        viewModel.editName("Alice")
        viewModel.editUsername("alice_ok")
        viewModel.editBirthday("10/12/2024")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.invalidBirthdayMsg)
        assertTrue(state.isValid)
      }

  // ------------------------------------------------------------------------
  // USERNAME VALIDATION & AVAILABILITY
  // ------------------------------------------------------------------------

  /** Tests that an invalid username format sets an appropriate error message. */
  @Test
  fun editUsername_WithInvalidFormat_SetsErrorMessage() =
      runTest(testDispatcher, testTimeout) {
        viewModel.editUsername("Bad!!Name")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(
            "Invalid username format (3–20 chars, lowercase, ., -, _ allowed)",
            state.invalidUsernameMsg)
        assertNull(state.isUsernameAvailable)
        assertFalse(state.isValid)
      }

  /** Tests that a valid username format triggers an availability check. */
  @Test
  fun editUsername_WithValidName_ChecksAvailability() =
      runTest(testDispatcher, testTimeout) {
        viewModel.editUsername("valid_name")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.invalidUsernameMsg)
        assertNotNull(state.isUsernameAvailable)
      }

  // ------------------------------------------------------------------------
  // PROFILE UPDATES
  // ------------------------------------------------------------------------

  /** Tests that updating a profile with valid data correctly updates the repository. */
  @Test
  fun updateProfile_WithValidData_UpdatesRepositoryAndClearsError() =
      runTest(testDispatcher, testTimeout) {
        val profile =
            Profile(uid = "u1", name = "Alice", username = "old_name", bio = "settingsTestBio")
        profileLocalRepository.addProfile(profile)

        viewModel.loadProfile("u1")
        advanceUntilIdle()

        viewModel.editName("Bob")
        viewModel.editUsername("new_user")
        viewModel.editBio("new_bio")
        advanceUntilIdle()

        viewModel.updateProfile("u1", isFirstTime = true)
        advanceUntilIdle()

        val updated = profileLocalRepository.getProfileByUid("u1")
        assertEquals("Bob", updated?.name)
        assertEquals("new_user", updated?.username)
        assertEquals("new_bio", updated?.bio)
        assertNull(viewModel.uiState.value.errorMsg)
      }

  /** Tests that attempting to update with an invalid name sets an error and doesn't update. */
  @Test
  fun updateProfile_WhenInvalidName_SetsErrorMsgAndDoesNotUpdate() =
      runTest(testDispatcher, testTimeout) {
        val profile = Profile(uid = "u1", name = "Alice", username = "ok")
        profileLocalRepository.addProfile(profile)

        viewModel.loadProfile("u1")
        advanceUntilIdle()

        viewModel.editName("")
        viewModel.editUsername("ok")

        viewModel.updateProfile("u1", isFirstTime = true)
        advanceUntilIdle()

        val updated = profileLocalRepository.getProfileByUid("u1")
        assertEquals("Alice", updated?.name)
        assertEquals("ok", updated?.username)
        assertEquals("At least one field is not valid.", viewModel.uiState.value.errorMsg)
      }

  /**
   * Tests that attempting to update with a username already taken by another user sets an error.
   */
  @Test
  fun updateProfile_WhenUsernameAlreadyTaken_SetsErrorMsg() =
      runTest(testDispatcher, testTimeout) {
        val existing = Profile(uid = "u2", name = "Bob", username = "taken_name")
        profileLocalRepository.addProfile(existing)

        val newProfile = Profile(uid = "u1", name = "Alice", username = "old_name")
        profileLocalRepository.addProfile(newProfile)

        viewModel.loadProfile("u1")
        advanceUntilIdle()

        viewModel.editName("Alice Updated")
        viewModel.editUsername("taken_name")
        advanceUntilIdle()

        viewModel.updateProfile("u1", isFirstTime = true)
        advanceUntilIdle()

        assertEquals("At least one field is not valid.", viewModel.uiState.value.errorMsg)
      }

  // ------------------------------------------------------------------------
  // SAVE SUCCESS & USERNAME CHANGE BEHAVIOR
  // ------------------------------------------------------------------------

  /** Tests that updating profile without changing username doesn't trigger an error. */
  @Test
  fun updateProfile_WhenUsernameUnchanged_DoesNotTriggerError() =
      runTest(testDispatcher, testTimeout) {
        val existing = Profile(uid = "u1", name = "Alice", username = "same_user")
        profileLocalRepository.addProfile(existing)

        viewModel.loadProfile("u1")
        advanceUntilIdle()

        viewModel.editName("Alice Updated")
        advanceUntilIdle()

        viewModel.updateProfile("u1", isFirstTime = false)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.errorMsg)
        assertTrue("Expected saveSuccess to be true", state.saveSuccess)
      }

  /** Tests that a valid profile update shows the save success flag. */
  @Test
  fun updateProfile_WhenValid_ShowsSaveSuccessFlag() =
      runTest(testDispatcher, testTimeout) {
        val profile = Profile(uid = "u1", name = "Alice", username = "user_ok")
        profileLocalRepository.addProfile(profile)

        viewModel.loadProfile("u1")
        advanceUntilIdle()

        viewModel.editName("Alice Updated")
        viewModel.editUsername("user_ok_new")
        advanceUntilIdle()

        viewModel.updateProfile("u1", isFirstTime = false)
        advanceUntilIdle()

        val updated = profileLocalRepository.getProfileByUid("u1")
        assertEquals("Alice Updated", updated?.name)
        assertEquals("user_ok_new", updated?.username)
        assertTrue(viewModel.uiState.value.saveSuccess)
      }

  /** Tests that clearing the save success flag resets it to false. */
  @Test
  fun clearSaveSuccess_ResetsFlag() =
      runTest(testDispatcher, testTimeout) {
        viewModel.editName("Test")
        viewModel.editUsername("valid_name")
        advanceUntilIdle()
        viewModel.updateProfile("id1", isFirstTime = true)
        advanceUntilIdle()

        viewModel.clearSaveSuccess()
        assertFalse(viewModel.uiState.value.saveSuccess)
      }

  /**
   * Tests that when the repository fails to register a username, the ViewModel sets the appropriate
   * error.
   */
  @Test
  fun updateProfile_WhenRepositoryReturnsFalse_SetsUsernameTakenError() =
      runTest(testDispatcher, testTimeout) {
        val existing = Profile(uid = "u1", name = "Alice", username = "old_name")
        profileLocalRepository.addProfile(existing)

        profileLocalRepository.shouldFailRegisterUsername = true

        viewModel.loadProfile("u1")
        advanceUntilIdle()

        viewModel.editName("Alice Updated")
        viewModel.editUsername("new_user")
        advanceUntilIdle()

        viewModel.updateProfile("u1", isFirstTime = true)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Username is invalid or already taken.", state.errorMsg)
        assertFalse(state.saveSuccess)
        profileLocalRepository.shouldFailRegisterUsername = false
      }

  /** Tests that updating profile with unchanged URL doesn't modify the repository unnecessarily. */
  @Test
  fun updateProfilePicture_WhenUrlUnchanged_DoesNotChangeRepository() =
      runTest(testDispatcher, testTimeout) {
        val uid = "u1"
        val repo = ProfileLocalRepository()
        mockitoUtils.chooseCurrentUser(uid)
        val viewModel =
            SettingsViewModel(
                profileRepository = repo,
                authProvider = { mockitoUtils.mockAuth },
                userStatusManager = statusManagerMock,
                groupsRepository = groupsLocalRepository,
                eventsRepository = eventsLocalRepository,
                focusSessionsRepository = focusSessionsLocalRepository,
                todosRepository = toDosLocalRepository)

        val originalProfile =
            Profile(uid = uid, name = "Alice", username = "alice_ok", profilePicture = "same_url")
        repo.addProfile(originalProfile)

        viewModel.loadProfile(uid)
        advanceUntilIdle()

        viewModel.editProfilePictureUrl("same_url")
        viewModel.updateProfile(uid, isFirstTime = false)
        advanceUntilIdle()

        val updated = repo.getProfileByUid(uid)
        assertEquals("same_url", updated?.profilePicture)
        assertTrue(viewModel.uiState.value.saveSuccess)
      }

  /**
   * Tests that updating profile with a changed URL calls the repository and updates the profile.
   * Uses Mockito to bypass Android runtime for Uri.parse().
   */
  @Test
  fun updateProfilePicture_WhenUrlChanged_CallsRepoAndUpdatesProfile() =
      runTest(testDispatcher, testTimeout) {
        val uid = "u1"
        val repo = ProfileLocalRepository()

        mockitoUtils.chooseCurrentUser(uid)
        val viewModel =
            SettingsViewModel(
                profileRepository = repo,
                authProvider = { mockitoUtils.mockAuth },
                userStatusManager = statusManagerMock,
                groupsRepository = groupsLocalRepository,
                eventsRepository = eventsLocalRepository,
                focusSessionsRepository = focusSessionsLocalRepository,
                todosRepository = toDosLocalRepository)

        val fakeUri = Mockito.mock(Uri::class.java)
        Mockito.mockStatic(Uri::class.java).use { mockedStatic ->
          mockedStatic.`when`<Uri> { Uri.parse(any()) }.thenReturn(fakeUri)

          val originalProfile =
              Profile(uid = uid, name = "Alice", username = "alice_ok", profilePicture = "old_url")
          repo.addProfile(originalProfile)

          viewModel.loadProfile(uid)
          advanceUntilIdle()
          viewModel.editProfilePictureUrl("https://example.com/new.png")
          viewModel.editName("Alice")
          viewModel.editUsername("alice_ok")
          advanceUntilIdle()

          viewModel.updateProfile(uid, isFirstTime = false)
          advanceUntilIdle()

          val updated = repo.getProfileByUid(uid)
          assertEquals("https://local.test.storage/$uid.jpg", updated?.profilePicture)
          assertTrue(viewModel.uiState.value.saveSuccess)
          assertNull(viewModel.uiState.value.errorMsg)
        }
      }

  /** Tests that updating user status updates the UI state and calls the status manager. */
  @Test
  fun updateUserStatus_UpdatesStateAndCallsUserStatusManager() =
      runTest(testDispatcher, testTimeout) {
        val status = ProfileStatus.ONLINE

        viewModel.updateUserStatus(status)
        advanceUntilIdle()

        assertEquals(status, viewModel.uiState.value.currentUserStatus)

        Mockito.verify(statusManagerMock)
            .setStatus(status = status, source = UserStatusSource.MANUAL, resetToAuto = true)
      }

  /** Tests that updating user status to OFFLINE does not reset to auto. */
  @Test
  fun updateUserStatus_OfflineDoesNotResetToAuto() =
      runTest(testDispatcher, testTimeout) {
        val status = ProfileStatus.OFFLINE

        viewModel.updateUserStatus(status)
        advanceUntilIdle()
        assertEquals(status, viewModel.uiState.value.currentUserStatus)

        Mockito.verify(statusManagerMock)
            .setStatus(status = status, source = UserStatusSource.MANUAL, resetToAuto = false)
      }

  /** Tests that signing out calls setStatus with OFFLINE. */
  @Test
  fun signOutCallsSetStatus() =
      runTest(testDispatcher, testTimeout) {
        viewModel.signOut(credentialManager)
        advanceUntilIdle()
        Mockito.verify(statusManagerMock).setStatus(status = ProfileStatus.OFFLINE)
      }
}
