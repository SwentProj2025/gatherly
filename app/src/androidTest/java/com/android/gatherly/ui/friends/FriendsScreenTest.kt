package com.android.gatherly.ui.friends

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.ui.events.EventsScreen
import com.android.gatherly.ui.events.EventsScreenTestTags
import com.android.gatherly.ui.events.EventsViewModel
import com.android.gatherly.ui.friends.FriendsScreen
import com.android.gatherly.ui.friends.FriendsViewModel
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Screen
import com.android.gatherly.utils.FirebaseEmulator
import com.android.gatherly.utils.FirestoreGatherlyProfileTest
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

private const val TIMEOUT = 30_000L
private const val DELAY = 200L

class FriendsScreenTest : FirestoreGatherlyProfileTest() {

    @get:Rule val composeTestRule = createComposeRule()
    private lateinit var currentUserId: String

    @Before
    override fun setUp() {
        super.setUp()
        currentUserId =
            Firebase.auth.currentUser?.uid
                ?: throw IllegalStateException("Firebase user is not authenticated after setUp.")
    }

    /** Helper function: set the content of the composeTestRule without initial events */
    private fun setContent() {
        runTest {
            composeTestRule.setContent {
                FriendsScreen(
                    friendsViewModel = FriendsViewModel(
                        repository = repository,
                        currentUserId = currentUserId
                    )
                )
            }
        }
    }

    /**
     * Test: Verifies that when the user got no friend, all relevant UI components are displayed
     * correctly.
     */
    @Test
    fun testTagsCorrectlySetWhenListAreEmpty() {
        setContent()
        composeTestRule
            .onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
            .assertTextContains("Friends", substring = true, ignoreCase = true)
        composeTestRule.onNodeWithTag(FriendsScreenTestTags.EMPTY_LIST_MSG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FriendsScreenTestTags.SEARCH_FRIENDS_BAR).assertIsNotDisplayed()
        composeTestRule.onNodeWithTag(FriendsScreenTestTags.BUTTON_FIND_FRIENDS).assertIsDisplayed()
    }

    /**
     * Test: Verifies that when the user got no friend he can click on the button to navigate to
     * FindFriends screen.
     */
    @Test
    fun testButtonFindFriendClikable() {
        setContent()
        composeTestRule.onNodeWithTag(FriendsScreenTestTags.EMPTY_LIST_MSG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FriendsScreenTestTags.BUTTON_FIND_FRIENDS)
            .assertIsDisplayed()
            .performClick()
    }

    @Test
    fun testDisplayCorrectlyOneFriend(){
        val auth = FirebaseEmulator.auth
        val friend =
            Profile(
                name = "Alice",
                username = "alice",
                school = "University",
                schoolYear = "Year",
                friendUids = emptyList()
            )
        runBlocking { repository.addProfile(friend) }

        try{
            runBlocking { auth.signInAnonymously() }
            val bobId = auth.currentUser?.uid ?: error("Bob auth failed")
            val bobProfile =
                Profile(
                    uid = bobId,
                    name = "Bob",
                    username = "bob",
                    school = "University",
                    schoolYear = "Year",
                    friendUids = emptyList()
                )

            runBlocking { repository.addProfile(bobProfile) }
            runBlocking { repository.addFriend(friend.username, bobId) }

            composeTestRule.setContent {
                FriendsScreen(
                    friendsViewModel = FriendsViewModel(repository = repository, currentUserId = bobId),
                )
            }
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithTag(FriendsScreenTestTags.BUTTON_FIND_FRIENDS).assertIsDisplayed()
            composeTestRule.onNodeWithTag(FriendsScreenTestTags.SEARCH_FRIENDS_BAR).assertIsDisplayed()
            composeTestRule.onNodeWithTag(FriendsScreenTestTags.EMPTY_LIST_MSG).assertIsNotDisplayed()

        } finally {
            auth.signOut()
        }

    }




}