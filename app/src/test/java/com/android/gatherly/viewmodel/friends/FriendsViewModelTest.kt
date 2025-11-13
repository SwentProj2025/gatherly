package com.android.gatherly.viewmodel.friends

import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.ui.friends.FriendsViewModel
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

  // initialize this so that tests control all coroutines and can wait on them
  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setUp() {
    // so that tests can wait on coroutines
    Dispatchers.setMain(testDispatcher)
    // initialize repos and viewModel
    profileRepository = ProfileLocalRepository()

    fill_repositories()

    viewModel = FriendsViewModel(repository = profileRepository, currentUserId = "A")
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
          participatingEventIds = emptyList(),
          groupIds = emptyList(),
          friendUids = listOf("B"),
          profilePicture = "profileA.png")

  private val userB: Profile =
      Profile(
          uid = "B",
          username = "bob",
          name = "ProfileB",
          focusSessionIds = emptyList(),
          participatingEventIds = emptyList(),
          groupIds = emptyList(),
          friendUids = listOf("A"),
          profilePicture = "profileB.png")

  val userC: Profile =
      Profile(
          uid = "C",
          username = "charlie",
          name = "ProfileC",
          focusSessionIds = emptyList(),
          participatingEventIds = emptyList(),
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
}
