package com.android.gatherly.ui.friends

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import com.android.gatherly.model.notification.NotificationsLocalRepository
import com.android.gatherly.model.notification.NotificationsRepository
import com.android.gatherly.model.points.PointsLocalRepository
import com.android.gatherly.model.points.PointsRepository
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.utils.MockitoUtils
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

/**
 * Helper class used by UI tests for `FriendsScreen` and `FindFriendsScreen`.
 *
 * @property composeTestRule The Compose test rule used to set the screen content.
 */
class FriendsScreensTestHelper(private val composeTestRule: ComposeContentTestRule) {

  /**
   * Enum specifying which screen should be rendered during test setup.
   * - [FRIENDS] renders FriendsScreen
   * - [FIND_FRIENDS] renders FindFriendsScreen
   */
  enum class ScreenType {
    FRIENDS,
    FIND_FRIENDS
  }

  /**
   * Container object returned by setup functions to expose the initialized test environment.
   *
   * @property friendsViewModel The view model controlling the rendered screen.
   * @property profileRepository The repository storing mock test profiles.
   * @property notificationsRepository The repository storing test notifications.
   * @property mockito The [MockitoUtils] instance providing a mocked FirebaseAuth user.
   * @property userId The ID of the user currently "logged in" for this test.
   */
  data class TestEnvironment(
      val friendsViewModel: FriendsViewModel,
      val profileRepository: ProfileRepository,
      val notificationsRepository: NotificationsRepository,
      val mockito: MockitoUtils,
      val userId: String
  )

  /** A user with no friends. */
  val bobProfile: Profile =
      Profile(
          uid = "bobID",
          name = "bobby",
          username = "bob",
          groupIds = emptyList(),
          friendUids = emptyList())

  /** A user with 3 friends (profile1, profile2, profile3). */
  val aliceProfile: Profile =
      Profile(
          uid = "AliceID",
          name = "alicia",
          username = "alice",
          groupIds = emptyList(),
          friendUids = listOf("1", "2", "3"))

  /** Basic sample profile used as friend or pending user. */
  val profile1: Profile =
      Profile(
          uid = "1",
          name = "Profile1",
          username = "francis",
          groupIds = emptyList(),
          friendUids = emptyList())

  /** Basic sample profile used as friend or pending user. */
  val profile2: Profile =
      Profile(
          uid = "2",
          name = "Profile2",
          username = "charlie",
          groupIds = emptyList(),
          friendUids = emptyList())

  /** Basic sample profile used as friend or pending user. */
  val profile3: Profile =
      Profile(
          uid = "3",
          name = "Profile3",
          username = "denis",
          groupIds = emptyList(),
          friendUids = emptyList())

  /** A user with only pending outgoing friend requests. */
  val profileWithOnlyPending =
      Profile(
          uid = "userPending",
          name = "UserPend",
          username = "usernamePending",
          friendUids = emptyList(),
          pendingSentFriendsUids = listOf("1", "2", "3"))

  /** A user with both friends and pending friend requests. */
  val profileWithPendingAndFriends =
      Profile(
          uid = "userTotal",
          name = "userTot",
          username = "usernameTotal",
          friendUids = listOf("1", "3"),
          pendingSentFriendsUids = listOf("2"))

  /**
   * Adds the sample profiles (profile1, profile2, profile3) into the provided repository.
   *
   * @param repo The profile repository into which the sample profiles should be inserted.
   */
  @OptIn(ExperimentalCoroutinesApi::class)
  private fun addProfiles(repo: ProfileRepository) {
    runTest {
      repo.addProfile(profile1)
      advanceUntilIdle()
      repo.addProfile(profile2)
      advanceUntilIdle()
      repo.addProfile(profile3)
      advanceUntilIdle()
    }
  }

  /**
   * Internal helper that sets up the environment for a given user and screen type.
   *
   * @param user The profile that should act as the currently authenticated user.
   * @param screen The screen to render during setup.
   * @return A [TestEnvironment] object exposing all initialized components.
   */
  @OptIn(ExperimentalCoroutinesApi::class)
  private fun setupUser(user: Profile, screen: ScreenType): TestEnvironment {
    lateinit var environment: TestEnvironment

    runTest(timeout = 120.seconds) {
      val profileRepository: ProfileRepository = ProfileLocalRepository()
      val notificationsRepository: NotificationsRepository = NotificationsLocalRepository()
      val pointsRepository: PointsRepository = PointsLocalRepository()

      profileRepository.addProfile(user)
      addProfiles(profileRepository)
      advanceUntilIdle()

      val userId = user.uid
      val mockito = MockitoUtils()
      mockito.chooseCurrentUser(userId)

      val viewModel =
          FriendsViewModel(
              repository = profileRepository,
              notificationsRepository = notificationsRepository,
              pointsRepository = pointsRepository,
              authProvider = { mockito.mockAuth })

      composeTestRule.setContent {
        when (screen) {
          ScreenType.FRIENDS -> FriendsScreen(viewModel)
          ScreenType.FIND_FRIENDS -> FindFriendsScreen(viewModel)
        }
      }

      environment =
          TestEnvironment(
              friendsViewModel = viewModel,
              profileRepository = profileRepository,
              notificationsRepository = notificationsRepository,
              mockito = mockito,
              userId = userId)
    }
    return environment
  }

  /** Sets up the test environment for user Bob on the requested [screen]. */
  fun setupWithBobUID(screen: ScreenType) = setupUser(bobProfile, screen)

  /** Sets up the test environment for user Alice on the requested [screen]. */
  fun setupWithAliceUID(screen: ScreenType) = setupUser(aliceProfile, screen)

  /** Sets up the test environment for a user with pending outgoing friend requests only. */
  fun setupWithPendingProfile(screen: ScreenType) = setupUser(profileWithOnlyPending, screen)

  /** Sets up the test environment for a user with both friends and outgoing pending requests. */
  fun setupWithTotalProfile(screen: ScreenType) = setupUser(profileWithPendingAndFriends, screen)
}
