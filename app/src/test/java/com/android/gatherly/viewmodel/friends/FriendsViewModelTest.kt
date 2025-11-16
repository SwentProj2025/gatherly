package com.android.gatherly.viewmodel.friends

import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.profile.ProfileStatus
import com.android.gatherly.ui.friends.FriendsViewModel
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

private const val TIMEOUT = 30_000L
private const val DELAY = 200L

/**
 * Integration tests for [FriendsViewModel] using the local Emulators.
 *
 * These tests verify:
 * - Initial state of friends list
 * - When following a friend, the list is updated accordingly
 * - When unfollowing a friend, the list is updated accordingly
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FriendsViewModelTest {

  private lateinit var profileRepository: ProfileLocalRepository
  private lateinit var viewModel: FriendsViewModel
  private lateinit var mockitoUtils: MockitoUtils

  // initialize this so that tests control all coroutines and can wait on them
  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setUp() {
    // so that tests can wait on coroutines
    Dispatchers.setMain(testDispatcher)
    // initialize repos and viewModel
    profileRepository = ProfileLocalRepository()

    fill_repositories()

    // Mock Firebase Auth
    mockitoUtils = MockitoUtils()
    mockitoUtils.chooseCurrentUser("A")

    viewModel =
        FriendsViewModel(repository = profileRepository, authProvider = { mockitoUtils.mockAuth })
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  private val userA: Profile =
      Profile(
          uid = "A",
          username = "alice",
          name = "ProfileA",
          focusSessionIds = emptyList(),
          eventIds = emptyList(),
          groupIds = emptyList(),
          friendUids = listOf("B"),
          profilePicture = "profileA.png")

  private val userB: Profile =
      Profile(
          uid = "B",
          username = "bob",
          name = "ProfileB",
          focusSessionIds = emptyList(),
          eventIds = emptyList(),
          groupIds = emptyList(),
          friendUids = listOf("A"),
          profilePicture = "profileB.png")

  val userC: Profile =
      Profile(
          uid = "C",
          username = "charlie",
          name = "ProfileC",
          focusSessionIds = emptyList(),
          eventIds = emptyList(),
          groupIds = emptyList(),
          friendUids = emptyList(),
          profilePicture = "profileC.png")

  @Test
  fun testInitialStateShouldContainEachFriends() = runTest {
    val state = viewModel.uiState.value
    assertEquals(listOf("bob"), state.friends)
    assertTrue(state.listNoFriends.contains("charlie"))
  }

  @Test
  fun testFollowFriendCFromProfileA() = runTest {
    viewModel.followFriend("charlie", "A")

    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state.friends.contains("charlie"))
  }

  @Test
  fun testUnfollowFriendBFromProfileA() = runTest {
    viewModel.unfollowFriend("bob", "A")

    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.friends.contains("bob"))
  }

  // This function fills the profile repository with the created profiles
  fun fill_repositories() {
    runTest {
      profileRepository.addProfile(userA)
      profileRepository.addProfile(userB)
      profileRepository.addProfile(userC)
      advanceUntilIdle()
    }
  }

  /** Test: Verifies if the function refreshFriends catch errors */
  @Test
  fun testRefreshFriends_CatchesExceptionAndSetsErrorMsg() = runTest {
    val errorMessage = "Simulated Connection Failure"

    val throwingRepository =
        object : ProfileRepository {

          // Surcharge la méthode à tester pour lancer une exception
          override suspend fun getFriendsAndNonFriendsUsernames(
              currentUserId: String
          ): com.android.gatherly.model.friends.Friends {
            throw Exception(errorMessage)
          }

          override suspend fun getProfileByUid(uid: String): Profile? = null

          override suspend fun updateProfile(profile: Profile) {}

          override suspend fun deleteProfile(uid: String) {}

          override suspend fun isUidRegistered(uid: String): Boolean = false

          override suspend fun searchProfilesByNamePrefix(prefix: String): List<Profile> =
              emptyList()

          override suspend fun isUsernameAvailable(username: String): Boolean = false

          override suspend fun registerUsername(uid: String, username: String): Boolean = false

          override suspend fun updateUsername(
              uid: String,
              oldUsername: String?,
              newUsername: String
          ): Boolean = false

          override suspend fun updateProfilePic(uid: String, uri: android.net.Uri): String = ""

          override suspend fun getProfileByUsername(username: String): Profile? = null

          override suspend fun searchProfilesByUsernamePrefix(
              prefix: String,
              limit: Int
          ): List<Profile> = emptyList()

          override suspend fun initProfileIfMissing(uid: String, defaultPhotoUrl: String): Boolean =
              false

          override suspend fun addProfile(profile: Profile) {}

          override suspend fun getListNoFriends(currentUserId: String): List<String> = emptyList()

          override suspend fun addFriend(friend: String, currentUserId: String) {}
            override suspend fun updateBadges(userProfile: Profile) {
            }

            override suspend fun updateStatus(uid: String, status: ProfileStatus) {

          }

          override suspend fun deleteFriend(friend: String, currentUserId: String) {}

          override suspend fun deleteUserProfile(uid: String) {}
        }
    val errorViewModel =
        FriendsViewModel(repository = throwingRepository, authProvider = { mockitoUtils.mockAuth })
    errorViewModel.refreshFriends("A")

    advanceUntilIdle()
    val state = errorViewModel.uiState.value

    assertNotNull(state.errorMsg)

    assertTrue(state.errorMsg!!.contains("Failed to load friends: $errorMessage"))

    assertTrue(state.friends.isEmpty())
    assertTrue(state.listNoFriends.isEmpty())
  }
}
