package com.android.gatherly.viewmodel.groups.info

import com.android.gatherly.model.group.GroupsLocalRepository
import com.android.gatherly.model.group.GroupsRepository
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.ui.groups.GroupInformationViewModel
import com.android.gatherly.utilstest.MockitoUtils
import com.android.gatherly.viewmodel.groups.overview.GroupsOverviewViewModelTestData.FRIEND_USER_ID
import com.android.gatherly.viewmodel.groups.overview.GroupsOverviewViewModelTestData.OTHER_USER_ID
import com.android.gatherly.viewmodel.groups.overview.GroupsOverviewViewModelTestData.TEST_USER_ID
import com.android.gatherly.viewmodel.groups.overview.GroupsOverviewViewModelTestData.friendUser
import com.android.gatherly.viewmodel.groups.overview.GroupsOverviewViewModelTestData.testUser
import com.android.gatherly.viewmodel.groups.overview.GroupsOverviewViewModelTestData.userGroup1
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

/** Unit tests for the [GroupInformationViewModel] class */
@OptIn(ExperimentalCoroutinesApi::class)
class GroupInformationViewModelTest {
  private lateinit var groupsInformationViewModel: GroupInformationViewModel
  private lateinit var groupsRepository: GroupsRepository
  private lateinit var profileRepository: ProfileRepository
  private lateinit var mockitoUtils: MockitoUtils
  @OptIn(ExperimentalCoroutinesApi::class) private val testDispatcher = UnconfinedTestDispatcher()

  private val testTimeout = 120.seconds

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    groupsRepository = GroupsLocalRepository()
    profileRepository = ProfileLocalRepository()

    runBlocking {
      groupsRepository.addGroup(userGroup1)
      profileRepository.addProfile(testUser)
      profileRepository.addProfile(friendUser)
    }

    mockitoUtils = MockitoUtils()

    groupsInformationViewModel =
        GroupInformationViewModel(
            groupsRepository = groupsRepository,
            profileRepository = profileRepository,
            authProvider = { mockitoUtils.mockAuth })
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  /** Utility functions to choose the current user for testing */
  fun chooseAdmin() {
    mockitoUtils.chooseCurrentUser(TEST_USER_ID)
  }

  fun chooseNonAdmin() {
    mockitoUtils.chooseCurrentUser(FRIEND_USER_ID)
  }

  fun chooseNonExistentUser() {
    mockitoUtils.chooseCurrentUser(OTHER_USER_ID)
  }

  /** Check that a group with the current user being an admin loads the correct ui state */
  @Test
  fun checkGroupAdminLoadsCorrectly() =
      runTest(testDispatcher, testTimeout) {
        chooseAdmin()

        groupsInformationViewModel.loadUIState(userGroup1.gid)

        advanceUntilIdle()

        val uiState = groupsInformationViewModel.uiState

        assertNull(uiState.value.errorMessage)
        assertEquals(userGroup1, uiState.value.group)
        assertEquals(listOf(testUser, friendUser), uiState.value.memberProfiles)
        assertFalse(uiState.value.isLoading)
        assertTrue(uiState.value.isAdmin)
      }

  /** Check that a group with the current user NOT being an admin loads the correct ui state */
  @Test
  fun checkGroupMemberLoadsCorrectly() =
      runTest(testDispatcher, testTimeout) {
        chooseNonAdmin()

        groupsInformationViewModel.loadUIState(userGroup1.gid)

        advanceUntilIdle()

        val uiState = groupsInformationViewModel.uiState

        assertNull(uiState.value.errorMessage)
        assertEquals(userGroup1, uiState.value.group)
        assertEquals(listOf(testUser, friendUser), uiState.value.memberProfiles)
        assertFalse(uiState.value.isLoading)
        assertFalse(uiState.value.isAdmin)
      }

  /**
   * Check that choosing a group that doesn't exist causes an error and that clearing the error
   * works
   */
  @Test
  fun checkErrorMessageIsSetAndCleared() =
      runTest(testDispatcher, testTimeout) {
        chooseNonExistentUser()

        groupsInformationViewModel.loadUIState("GROUP_DOES_NOT_EXIST")

        advanceUntilIdle()

        val uiState = groupsInformationViewModel.uiState

        assertNotNull(uiState.value.errorMessage)

        groupsInformationViewModel.clearErrorMessage()

        assertNull(uiState.value.errorMessage)
      }
}
