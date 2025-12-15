package com.android.gatherly.end2end

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.credentials.CredentialManager
import androidx.test.rule.GrantPermissionRule
import com.android.gatherly.GatherlyApp
import com.android.gatherly.ui.authentication.InitProfileScreenTestTags
import com.android.gatherly.ui.authentication.SignInScreenTestTags
import com.android.gatherly.ui.friends.FindFriendsScreenTestTags
import com.android.gatherly.ui.friends.FriendsScreenTestTags
import com.android.gatherly.ui.homePage.HomePageScreenTestTags
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.notifications.FriendRequestsScreenTestTags
import com.android.gatherly.ui.notifications.NotificationsScreenTestTags
import com.android.gatherly.utils.FakeCredentialManager
import com.android.gatherly.utils.FakeJwtGenerator
import com.android.gatherly.utils.FirebaseEmulator
import com.android.gatherly.utils.FirestoreGatherlyTest
import kotlin.test.Test
import org.junit.Before
import org.junit.Rule

class Milestone3End2End : FirestoreGatherlyTest() {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

  val TIMEOUT = 15000L
  private var currentCredentialManager: CredentialManager? = null

  // Credentials
  private lateinit var user1Token: String
  private lateinit var user2Token: String
  private var user1Email = "user1@gmail.com"
  private var user2Email = "user2@gmail.com"

  private fun setAppContent() {
    currentCredentialManager?.let { credentialManager ->
      composeTestRule.setContent { GatherlyApp(credentialManager = credentialManager) }
    }
  }

  @Before
  override fun setUp() {
    super.setUp()
    FirebaseEmulator.auth.signOut()

    user1Token = FakeJwtGenerator.createFakeGoogleIdToken("user1", email = user1Email)
    user2Token = FakeJwtGenerator.createFakeGoogleIdToken("user2", email = user2Email)

    currentCredentialManager = FakeCredentialManager.create(user1Token)
    setAppContent()
  }

  /**
   * Test scenario: We got two users : friend1 and friend2. First, we create an account for friend1
   * Second, we create an account for friend2, he sends a friend request to friend1 Then, we connect
   * back to the friend1 account, he accepts the friend request of friend2 At the end, friend1 and
   * friend2 are friends.
   */
  @Test
  fun testFriendRequestFlow() {
    signInWithGoogle()
    createProfile("friend1", "friend1_name")

    signOutFromHomePage()
    restartAppWithUser(user2Token)

    signInWithGoogle()
    createProfile("friend2", "friend2_name")

    sendFriendRequest("friend1")
    fromFindFriendsToHomePage()

    restartAppWithUser(user1Token)
    signInWithGoogle()

    acceptFriendRequest("friend2")
  }

  // --- PRIVATE HELPER FUNCTIONS ---

  /** Helper test function : Sign in with the current credential to the application */
  private fun signInWithGoogle() {
    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.GOOGLE_BUTTON)
        .assertIsDisplayed()
        .performClick()
  }

  /**
   * Helper test function : Restart the application with a different credential
   *
   * @param userToken credential of the account we want to sign in next
   */
  private fun restartAppWithUser(userToken: String) {
    composeTestRule.waitForIdle()
    FirebaseEmulator.auth.signOut()

    currentCredentialManager = FakeCredentialManager.create(userToken)

    composeTestRule.waitUntil(TIMEOUT) {
      try {
        composeTestRule.onNodeWithTag(SignInScreenTestTags.GOOGLE_BUTTON).isDisplayed()
        true
      } catch (e: Exception) {
        false
      }
    }
  }

  /**
   * Helper test function : Sign out from the current account
   *
   * click on the dropdown profile tab, to navigate to the Profile screen click on the dropdown
   * logout tab, to sign out.
   */
  private fun signOutFromHomePage() {
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()

    composeTestRule.waitUntil(TIMEOUT) {
      composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).isDisplayed()
    }
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.checkProfileScreenIsDisplayed()

    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()

    composeTestRule.waitUntil(TIMEOUT) {
      composeTestRule.onNodeWithTag(NavigationTestTags.LOGOUT_TAB).isDisplayed()
    }
    composeTestRule.onNodeWithTag(NavigationTestTags.LOGOUT_TAB).performClick()

    composeTestRule.checkSignInScreenIsDisplayed()
  }

  /**
   * Helper test function : Sign out from the find friends screen
   *
   * First, navigate back to the Friends Screen, then navigate back again to the Home Page screen
   * Second, check that we are correctly in the home page screen. Then, use the helper function to
   * sign out from the home page
   */
  private fun fromFindFriendsToHomePage() {

    composeTestRule
        .onNodeWithTag(NavigationTestTags.GO_BACK_BUTTON)
        .assertIsDisplayed()
        .performClick()

    composeTestRule.checkFriendsScreenIsDisplayed()

    composeTestRule
        .onNodeWithTag(NavigationTestTags.GO_BACK_BUTTON)
        .assertIsDisplayed()
        .performClick()

    composeTestRule.checkHomePageScreenCorrectlyDisplayed()

    signOutFromHomePage()
  }

  /**
   * Helper test function : Writing all the information wanted in the init profile screen to create
   * the profile
   *
   * @param username : string that represents the username wanted for the new profile
   * @param name : string that represents the name wanted for the new profile
   */
  private fun createProfile(username: String, name: String) {
    composeTestRule.waitUntil(TIMEOUT) {
      composeTestRule.onNodeWithTag(InitProfileScreenTestTags.SAVE_BUTTON).isDisplayed()
    }
    composeTestRule.onNodeWithTag(InitProfileScreenTestTags.USERNAME).performTextInput(username)
    composeTestRule.onNodeWithTag(InitProfileScreenTestTags.NAME_FIELD).performTextInput(name)
    composeTestRule.onNodeWithTag(InitProfileScreenTestTags.SAVE_BUTTON).performClick()
    composeTestRule.waitUntil(TIMEOUT) {
      composeTestRule.onNodeWithTag(HomePageScreenTestTags.FOCUS_BUTTON).isDisplayed()
    }
  }

  /**
   * Helper test function : The goal is to send a friend request.
   *
   * From Home Page, click on Friends Button, Navigate to Friends screen, click on Find New Friends
   * Button, Navigate to Find Friends Screen, click on the wanted user button friend request.
   *
   * @param username the username that we want to send to the friend request.
   */
  private fun sendFriendRequest(username: String) {

    composeTestRule.checkHomePageScreenCorrectlyDisplayed()

    composeTestRule
        .onNodeWithTag(HomePageScreenTestTags.FRIENDS_SECTION)
        .assertIsDisplayed()
        .performClick()

    composeTestRule.checkFriendsScreenIsDisplayed()

    composeTestRule.onNodeWithTag(FriendsScreenTestTags.BUTTON_FIND_FRIENDS).performClick()
    composeTestRule.checkFindFriendsScreenIsDisplayed()
    composeTestRule
        .onNodeWithTag(FindFriendsScreenTestTags.getTestTagForFriendRequestButton(username))
        .assertIsDisplayed()
        .performClick()
  }

  /**
   * Helper test function : the goal is to accept the friend request
   *
   * From Home Page, click on drop down Notification, Navigate to Notification screen click on
   * Friends Request button, Navigates to Friends request screen click on accept friend.
   *
   * @param username : String of the user we want to accept his request
   */
  private fun acceptFriendRequest(username: String) {
    composeTestRule.checkHomePageScreenCorrectlyDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.DROPMENU).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.NOTIFICATIONS_TAB).performClick()
    composeTestRule.checkNotificationScreenIsDisplayed()
    composeTestRule.checkNotificationFriendsRequestIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.FRIEND_REQUESTS_TAB).performClick()
    composeTestRule.checkFriendRequestScreenIsDisplayed()
    composeTestRule.checkFriendRequestItemIsDisplayed(username)

    composeTestRule
        .onNodeWithTag(FriendRequestsScreenTestTags.getTestTagForAcceptButton(username))
        .assertIsDisplayed()
        .performClick()
  }

  // --- Helper functions to verify if the wanted screen is correctly displayed ---

  /** Helper test function : Verifies that the Profile screen is correctly displayed */
  private fun ComposeTestRule.checkProfileScreenIsDisplayed() {
    waitUntil(TIMEOUT) {
      try {
        composeTestRule
            .onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
            .assertTextContains("Your profile", substring = true, ignoreCase = true)
        true
      } catch (e: AssertionError) {
        false
      }
    }
  }

  /** Helper test function : Verifies that the authentication screen is correctly displayed */
  private fun ComposeTestRule.checkSignInScreenIsDisplayed() {
    waitUntil(TIMEOUT) {
      try {
        composeTestRule
            .onNodeWithTag(SignInScreenTestTags.WELCOME_TITLE)
            .assertIsDisplayed()
            .assertTextContains("Welcome to Gatherly,", substring = true, ignoreCase = true)
        true
      } catch (e: AssertionError) {
        false
      }
    }
  }

  /** Helper test function : Verifies that the Friends screen is correctly displayed */
  private fun ComposeTestRule.checkFriendsScreenIsDisplayed() {
    waitUntil(TIMEOUT) {
      try {
        composeTestRule
            .onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
            .assertTextContains("Friends", substring = true, ignoreCase = true)
        composeTestRule.onNodeWithTag(FriendsScreenTestTags.BUTTON_FIND_FRIENDS).assertIsDisplayed()
        true
      } catch (e: AssertionError) {
        false
      }
    }
  }

  /** Helper test function : Verifies that the Notifications screen is correctly displayed */
  private fun ComposeTestRule.checkNotificationScreenIsDisplayed() {
    waitUntil(TIMEOUT) {
      try {
        composeTestRule
            .onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
            .assertTextContains("Notification", substring = true, ignoreCase = true)

        true
      } catch (e: AssertionError) {
        false
      }
    }
  }

  /** Helper test function : Verifies that the Friend Request screen is correctly displayed */
  private fun ComposeTestRule.checkFriendRequestScreenIsDisplayed() {
    waitUntil(TIMEOUT) {
      try {
        composeTestRule
            .onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
            .assertTextContains("Friend Requests", substring = true, ignoreCase = true)
        true
      } catch (e: AssertionError) {
        false
      }
    }
  }

  /** Helper test function : Verifies that the Home page screen is correctly displayed */
  private fun ComposeTestRule.checkHomePageScreenCorrectlyDisplayed() {
    waitUntil(TIMEOUT) {
      try {
        composeTestRule.onNodeWithTag(HomePageScreenTestTags.FRIENDS_SECTION).assertIsDisplayed()
        true
      } catch (e: AssertionError) {
        false
      }
    }
  }

  /** Helper test function : Verifies that the Find Friends screen is correctly displayed */
  private fun ComposeTestRule.checkFindFriendsScreenIsDisplayed() {
    waitUntil(TIMEOUT) {
      try {
        composeTestRule
            .onNodeWithTag(FindFriendsScreenTestTags.SEARCH_FRIENDS_BAR)
            .assertIsDisplayed()
        true
      } catch (e: AssertionError) {
        false
      }
    }
  }

  /**
   * Helper test function : Verifies that the Friends Requests item section is correctly displayed
   */
  private fun ComposeTestRule.checkNotificationFriendsRequestIsDisplayed() {
    waitUntil(TIMEOUT) {
      try {
        composeTestRule
            .onNodeWithTag(NotificationsScreenTestTags.FRIEND_REQUEST_SECTION)
            .assertIsDisplayed()
        true
      } catch (e: AssertionError) {
        false
      }
    }
  }

  /** Helper test function : Verifies that the friend request item is correctly displayed */
  private fun ComposeTestRule.checkFriendRequestItemIsDisplayed(username: String) {
    composeTestRule.waitUntil(TIMEOUT) {
      try {
        composeTestRule
            .onNodeWithTag(FriendRequestsScreenTestTags.getTestTagForFriendRequestItem(username))
            .assertIsDisplayed()
        true
      } catch (e: AssertionError) {
        false
      }
    }
  }
}
