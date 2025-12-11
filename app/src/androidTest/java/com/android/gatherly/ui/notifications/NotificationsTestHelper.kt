package com.android.gatherly.ui.notifications

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import com.android.gatherly.model.notification.Notification
import com.android.gatherly.model.notification.NotificationsLocalRepository
import com.android.gatherly.model.notification.NotificationsRepository
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.utils.MockitoUtils
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

// This class was inspired by FriendsScreensTestHelper.kt

/**
 * Helper class used by UI tests for `NotificationsScreen` and `FriendRequestsScreen`.
 *
 * @property composeTestRule The Compose test rule used to set the screen content.
 */
class NotificationsTestHelper(private val composeTestRule: ComposeContentTestRule) {

  /**
   * Enum specifying which screen should be rendered during test setup.
   * - [NOTIFICATIONS_SCREEN] renders NotificationsScreen
   * - [FRIEND_REQUESTS_SCREEN] renders FriendRequestsScreen
   */
  enum class ScreenType {
    NOTIFICATIONS_SCREEN,
    FRIEND_REQUESTS_SCREEN
  }

  /**
   * Container object returned by setup functions to expose the initialized test environment.
   *
   * @property notificationsViewModel The view model controlling the rendered screen.
   * @property profileRepository The repository storing mock test profiles.
   * @property notificationsRepository The repository storing test notifications.
   * @property mockito The [MockitoUtils] instance providing a mocked FirebaseAuth user.
   * @property userId The ID of the user currently "logged in" for this test.
   */
  data class TestEnvironment(
      val notificationsViewModel: NotificationViewModel,
      val profileRepository: ProfileRepository,
      val notificationsRepository: NotificationsRepository,
      val mockito: MockitoUtils,
      val userId: String
  )

  val bobProfile: Profile = Profile(uid = "bobID", name = "Bob", username = "bob")
  val aliceProfile: Profile = Profile(uid = "AliceID", name = "Alice", username = "alice")
  val francisProfile: Profile = Profile(uid = "FrancisID", name = "Francis", username = "francis")
  val charlieProfile: Profile = Profile(uid = "CharlieID", name = "Charlie", username = "charlie")

  val friendRequestFrancisToBob: Notification =
      Notification(
          id = "FrancisToBobRequestID",
          senderId = francisProfile.uid,
          recipientId = bobProfile.uid,
          type = com.android.gatherly.model.notification.NotificationType.FRIEND_REQUEST,
          emissionTime = com.google.firebase.Timestamp.now(),
          relatedEntityId = null,
          wasRead = false)
  val friendRequestAliceToBob: Notification =
      Notification(
          id = "AliceToBobRequestID",
          senderId = aliceProfile.uid,
          recipientId = bobProfile.uid,
          type = com.android.gatherly.model.notification.NotificationType.FRIEND_REQUEST,
          emissionTime = com.google.firebase.Timestamp.now(),
          relatedEntityId = null,
          wasRead = false)
  val friendRequestCharlieToBob: Notification =
      Notification(
          id = "CharlieToBobRequestID",
          senderId = charlieProfile.uid,
          recipientId = bobProfile.uid,
          type = com.android.gatherly.model.notification.NotificationType.FRIEND_REQUEST,
          emissionTime = com.google.firebase.Timestamp.now(),
          relatedEntityId = null,
          wasRead = false)

  /**
   * Adds the sample profiles into the provided repository.
   *
   * @param repo The profile repository into which the sample profiles should be inserted.
   */
  @OptIn(ExperimentalCoroutinesApi::class)
  private fun addProfiles(repo: ProfileRepository) {
    runTest {
      repo.addProfile(bobProfile)
      advanceUntilIdle()
      repo.addProfile(aliceProfile)
      advanceUntilIdle()
      repo.addProfile(francisProfile)
      advanceUntilIdle()
      repo.addProfile(charlieProfile)
      advanceUntilIdle()
    }
  }

  /**
   * Adds the sample notifications into the provided repository.
   *
   * @param repo The repository into which the sample notifications should be inserted.
   */
  @OptIn(ExperimentalCoroutinesApi::class)
  private fun addNotifications(repo: NotificationsRepository) {
    runTest {
      repo.addNotification(friendRequestFrancisToBob)
      advanceUntilIdle()
      repo.addNotification(friendRequestAliceToBob)
      advanceUntilIdle()
      repo.addNotification(friendRequestCharlieToBob)
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

      addProfiles(profileRepository)
      addNotifications(notificationsRepository)

      val userId = user.uid
      val mockito = MockitoUtils()
      mockito.chooseCurrentUser(userId)

      val notificationsViewModel =
          NotificationViewModel(
              profileRepository = profileRepository,
              notificationsRepository = notificationsRepository,
              authProvider = { mockito.mockAuth })

      composeTestRule.setContent {
        when (screen) {
          ScreenType.NOTIFICATIONS_SCREEN -> NotificationsScreen(notificationsViewModel)
          ScreenType.FRIEND_REQUESTS_SCREEN -> {} // TODO: Implement FriendRequestsScreen
        }
      }

      environment =
          TestEnvironment(
              notificationsViewModel = notificationsViewModel,
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
}
