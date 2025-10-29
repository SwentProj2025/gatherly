package com.android.gatherly.ui.friends

import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
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

  private lateinit var repository: ProfileLocalRepository
  private lateinit var viewModel: FriendsViewModel

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

  @Before
  fun setup() = runTest {
    repository = ProfileLocalRepository()
    repository.addProfile(userA)
    repository.addProfile(userB)
    repository.addProfile(userC)

    viewModel = FriendsViewModel(repository, "A")
  }

  @Test
  fun testInitialStateShouldContainEachFriends() = runTest {
    val state = viewModel.uiState.value
    assertEquals(listOf("B"), state.friends)
    assertTrue(state.listNoFriends.contains("charlie"))
  }

  @Test
  fun testFollowFriendCFromProfileA() = runTest {
    viewModel.followFriend("C", "A")

    // Wait until the friends list is updated
    withContext(Dispatchers.Default.limitedParallelism(1)) {
      withTimeout(TIMEOUT) {
        while (viewModel.uiState.value.friends != listOf("B", "C")) {
          delay(DELAY)
        }
      }
    }

    val state = viewModel.uiState.value
    assertTrue(state.friends.contains("C"))
  }

  @Test
  fun testUnfollowFriendBFromProfileA() = runTest {
    viewModel.unfollowFriend("B", "A")

    // Wait until the friends list is updated
    withContext(Dispatchers.Default.limitedParallelism(1)) {
      withTimeout(TIMEOUT) {
        while (viewModel.uiState.value.friends == listOf("C")) {
          delay(DELAY)
        }
      }
    }

    val state = viewModel.uiState.value
    assertFalse(state.friends.contains("B"))
  }

  @Test
  fun testGetProfilePictureOfFriend() = runTest {
    val state = viewModel.uiState.value
    state.friends.forEach { friend ->
      val profilePicture = viewModel.getFriendProfilePicture(friend)

      withContext(Dispatchers.Default.limitedParallelism(1)) {
        withTimeout(TIMEOUT) {
          var profile: String? = viewModel.getFriendProfilePicture(friend)
          while (profile != "profileB.png") {
            profile = viewModel.getFriendProfilePicture(friend)
            delay(DELAY)
          }
        }
      }

      if (friend == "B") {
        assertEquals("profileB.png", profilePicture)
      } else {
        fail("Unexpected friend username: $friend")
      }
    }
  }
}
