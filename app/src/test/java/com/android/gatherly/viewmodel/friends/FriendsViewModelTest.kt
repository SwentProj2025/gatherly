package com.android.gatherly.viewmodel.friends

import com.android.gatherly.model.badge.BadgeType
import com.android.gatherly.model.notification.NotificationType
import com.android.gatherly.model.notification.NotificationsLocalRepository
import com.android.gatherly.model.notification.NotificationsRepository
import com.android.gatherly.model.points.PointsLocalRepository
import com.android.gatherly.model.points.PointsRepository
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.profile.ProfileStatus
import com.android.gatherly.model.profile.UserStatusSource
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
  private lateinit var notificationsRepository: NotificationsRepository
  private lateinit var pointsRepository: PointsRepository
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
    notificationsRepository = NotificationsLocalRepository()
    pointsRepository = PointsLocalRepository()

    fill_repositories()

    // Mock Firebase Auth
    mockitoUtils = MockitoUtils()
    mockitoUtils.chooseCurrentUser("A")

    viewModel =
        FriendsViewModel(
            repository = profileRepository,
            notificationsRepository = notificationsRepository,
            pointsRepository = pointsRepository,
            authProvider = { mockitoUtils.mockAuth })
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

  val anon: Profile =
      Profile(
          uid = "",
          username = "",
          name = "",
          focusSessionIds = emptyList(),
          participatingEventIds = emptyList(),
          groupIds = emptyList(),
          friendUids = emptyList(),
          profilePicture = "anon.png")

  @Test
  fun testInitialStateShouldContainEachFriends() = runTest {
    val state = viewModel.uiState.value
    assertEquals(listOf("bob"), state.friends)
    assertTrue(state.listNoFriends.contains("charlie"))
    assertFalse(state.listNoFriends.contains(""))
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
      profileRepository.addProfile(anon)
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

          override suspend fun addPendingSentFriendUid(currentUserId: String, targetUid: String) {}

          override suspend fun removePendingSentFriendUid(
              currentUserId: String,
              targetUid: String
          ) {}

          override suspend fun updateStatus(
              uid: String,
              status: ProfileStatus,
              source: UserStatusSource
          ) {}

          override suspend fun createEvent(eventId: String, currentUserId: String) {}

          override suspend fun deleteEvent(eventId: String, currentUserId: String) {}

          override suspend fun participateEvent(eventId: String, currentUserId: String) {}

          override suspend fun deleteFriend(friend: String, currentUserId: String) {}

          override suspend fun allParticipateEvent(eventId: String, participants: List<String>) {}

          override suspend fun unregisterEvent(eventId: String, currentUserId: String) {}

          override suspend fun allUnregisterEvent(eventId: String, participants: List<String>) {}

          override suspend fun addBadge(uid: String, badgeId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun incrementBadge(uid: String, type: BadgeType): String? {
            TODO("Not yet implemented")
          }

          override suspend fun deleteUserProfile(uid: String) {}

          override suspend fun updateFocusPoints(
              uid: String,
              points: Double,
              addToLeaderboard: Boolean
          ) {}
        }
    val errorViewModel =
        FriendsViewModel(
            repository = throwingRepository,
            notificationsRepository = notificationsRepository,
            pointsRepository = pointsRepository,
            authProvider = { mockitoUtils.mockAuth })
    errorViewModel.refreshFriends("A")

    advanceUntilIdle()
    val state = errorViewModel.uiState.value

    assertNotNull(state.errorMsg)

    assertTrue(state.errorMsg!!.contains("Failed to load friends: $errorMessage"))

    assertTrue(state.friends.isEmpty())
    assertTrue(state.listNoFriends.isEmpty())
  }

  @Test
  fun testSendFriendRequest_addsPendingAndNotification() = runTest {
    // userA sending request to userC
    viewModel.sendFriendRequest("C", "A")
    advanceUntilIdle()

    val state = viewModel.uiState.value

    // userC username = "charlie"
    assertTrue(state.pendingSentUsernames.contains("charlie"))

    val userA = profileRepository.getProfileByUid("A")!!
    assertTrue(userA.pendingSentFriendsUids.contains("C"))

    val notifications = notificationsRepository.getUserNotifications("C")
    assertEquals(1, notifications.size)
    assertEquals(NotificationType.FRIEND_REQUEST, notifications.first().type)
    assertEquals("A", notifications.first().senderId)
  }

  @Test
  fun testSendFriendRequest_doesNotDuplicatePending() = runTest {
    // first request
    viewModel.sendFriendRequest("C", "A")
    advanceUntilIdle()

    // second should be ignored
    viewModel.sendFriendRequest("C", "A")
    advanceUntilIdle()

    val userA = profileRepository.getProfileByUid("A")!!
    assertEquals(1, userA.pendingSentFriendsUids.size)

    val notifications = notificationsRepository.getUserNotifications("C")
    assertEquals(1, notifications.size)
  }

  @Test
  fun testSendFriendRequest_failsIfProfileMissing() = runTest {
    viewModel.sendFriendRequest("UNKNOWN", "A")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals("Friend profile not found", state.errorMsg)
  }

  @Test
  fun testRemoveFriend_removesAndSendsNotification() = runTest {
    // ensure B is friend of A
    assertTrue(viewModel.uiState.value.friends.contains("bob"))

    viewModel.removeFriend("B", "A")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.friends.contains("bob"))

    val notifications = notificationsRepository.getUserNotifications("B")
    assertEquals(1, notifications.size)
    assertEquals(NotificationType.REMOVE_FRIEND, notifications.first().type)
  }

  @Test
  fun testRemoveFriend_errorsIfNotFriend() = runTest {
    // C is not friend of A
    viewModel.removeFriend("C", "A")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals("Cannot remove non-friend", state.errorMsg)
  }

  @Test
  fun testCancelPendingFriendRequest_cancelsCorrectly() = runTest {
    // A sends request to C
    viewModel.sendFriendRequest("C", "A")
    advanceUntilIdle()

    // Now cancel
    viewModel.cancelPendingFriendRequest("C", "A")
    advanceUntilIdle()

    val userA = profileRepository.getProfileByUid("A")!!
    // pending array should NOT contain C anymore
    assertFalse(userA.pendingSentFriendsUids.contains("C"))

    val notifications = notificationsRepository.getUserNotifications("C")

    // C should have both: original request + cancellation
    assertEquals(2, notifications.size)
    assertTrue(
        notifications.any {
          it.type == NotificationType.FRIEND_REQUEST && it.senderId == "A" && it.recipientId == "C"
        })
    assertTrue(
        notifications.any {
          it.type == NotificationType.FRIEND_REQUEST_CANCELLED &&
              it.senderId == "A" &&
              it.recipientId == "C"
        })
  }

  @Test
  fun testRefreshFriends_populatesStateCorrectly() = runTest {
    viewModel.refreshFriends("A")
    advanceUntilIdle()

    val state = viewModel.uiState.value

    assertEquals("A", state.currentUserId)
    assertTrue(state.friends.contains("bob")) // B is friend
    assertTrue(state.listNoFriends.contains("charlie")) // C is not friend

    // Profiles map: should have B and C, but not A
    assertNotNull(state.profiles["bob"])
    assertNotNull(state.profiles["charlie"])
    assertFalse(state.profiles.containsKey("alice"))

    assertFalse(state.isLoading)
  }
}
