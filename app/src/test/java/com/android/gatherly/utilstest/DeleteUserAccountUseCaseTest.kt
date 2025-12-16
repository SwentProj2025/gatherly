package com.android.gatherly.utilstest

import com.android.gatherly.model.event.EventsLocalRepository
import com.android.gatherly.model.focusSession.FocusSessionsLocalRepository
import com.android.gatherly.model.group.GroupsLocalRepository
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.todo.ToDosLocalRepository
import com.android.gatherly.utils.DeleteUserAccountUseCase
import com.android.gatherly.utilstest.DeleteUserAccountUseCaseTestData.CURRENT_USER_ID
import com.android.gatherly.utilstest.DeleteUserAccountUseCaseTestData.FRIEND_1_ID
import com.android.gatherly.utilstest.DeleteUserAccountUseCaseTestData.currentUserProfile
import com.android.gatherly.utilstest.DeleteUserAccountUseCaseTestData.focusSession
import com.android.gatherly.utilstest.DeleteUserAccountUseCaseTestData.friend1Profile
import com.android.gatherly.utilstest.DeleteUserAccountUseCaseTestData.otherUserGroup
import com.android.gatherly.utilstest.DeleteUserAccountUseCaseTestData.ownedEvent
import com.android.gatherly.utilstest.DeleteUserAccountUseCaseTestData.ownedGroup
import com.android.gatherly.utilstest.DeleteUserAccountUseCaseTestData.sharedEvent
import com.android.gatherly.utilstest.DeleteUserAccountUseCaseTestData.sharedGroup
import com.android.gatherly.utilstest.DeleteUserAccountUseCaseTestData.todo
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [com.android.gatherly.utils.DeleteUserAccountUseCase] using local in-memory
 * repositories.
 *
 * These tests verify that all user-owned data is deleted and that shared data is correctly updated
 * when a user deletes their account.
 */
class DeleteUserAccountUseCaseTest {

  private lateinit var profileRepo: ProfileLocalRepository
  private lateinit var groupsRepo: GroupsLocalRepository
  private lateinit var eventsRepo: EventsLocalRepository
  private lateinit var focusRepo: FocusSessionsLocalRepository
  private lateinit var todosRepo: ToDosLocalRepository
  private lateinit var useCase: DeleteUserAccountUseCase
  private lateinit var testDispatcher: TestDispatcher
  private val testTimeout = 120.seconds

  @OptIn(ExperimentalCoroutinesApi::class)
  @Before
  fun setup() {
    profileRepo = ProfileLocalRepository()
    groupsRepo = GroupsLocalRepository()
    eventsRepo = EventsLocalRepository()
    focusRepo = FocusSessionsLocalRepository()
    todosRepo = ToDosLocalRepository()
    testDispatcher = UnconfinedTestDispatcher()

    useCase =
        DeleteUserAccountUseCase(
            profileRepository = profileRepo,
            groupsRepository = groupsRepo,
            eventsRepository = eventsRepo,
            focusSessionsRepository = focusRepo,
            todosRepository = todosRepo)
  }

  @Test
  fun deleteUserAccount_deletesOwnedGroups() =
      runTest(testDispatcher, testTimeout) {
        groupsRepo.addGroup(ownedGroup)

        useCase.deleteUserAccount(CURRENT_USER_ID)

        assertTrue(groupsRepo.getAllGroups().isEmpty())
      }

  @Test
  fun deleteUserAccount_removesUserFromMemberGroups() =
      runTest(testDispatcher, testTimeout) {
        groupsRepo.addGroup(sharedGroup)

        useCase.deleteUserAccount(CURRENT_USER_ID)

        val updatedGroup = groupsRepo.getAllGroups().single()
        assertFalse(updatedGroup.memberIds.contains(CURRENT_USER_ID))
        assertTrue(updatedGroup.memberIds.contains(FRIEND_1_ID))
      }

  @Test
  fun deleteUserAccount_deletesOwnedEvents() =
      runTest(testDispatcher, testTimeout) {
        eventsRepo.addEvent(ownedEvent)

        useCase.deleteUserAccount(CURRENT_USER_ID)

        assertTrue(eventsRepo.getAllEvents().isEmpty())
      }

  @Test
  fun deleteUserAccount_removesUserFromParticipatingEvents() =
      runTest(testDispatcher, testTimeout) {
        eventsRepo.addEvent(sharedEvent)

        useCase.deleteUserAccount(CURRENT_USER_ID)

        val updatedEvent = eventsRepo.getAllEvents().single()
        assertFalse(updatedEvent.participants.contains(CURRENT_USER_ID))
        assertTrue(updatedEvent.participants.contains(FRIEND_1_ID))
      }

  @Test
  fun deleteUserAccount_deletesUserFocusSessions() =
      runTest(testDispatcher, testTimeout) {
        focusRepo.addFocusSession(focusSession)

        useCase.deleteUserAccount(CURRENT_USER_ID)

        assertTrue(focusRepo.getAllFocusSessions().isEmpty())
      }

  @Test
  fun deleteUserAccount_deletesUserTodos() =
      runTest(testDispatcher, testTimeout) {
        todosRepo.addTodo(todo)

        useCase.deleteUserAccount(CURRENT_USER_ID)

        assertTrue(todosRepo.getAllTodos().isEmpty())
      }

  @Test
  fun deleteUserAccount_deletesProfileLast() =
      runTest(testDispatcher, testTimeout) {
        profileRepo.addProfile(currentUserProfile)

        useCase.deleteUserAccount(CURRENT_USER_ID)

        assertNull(profileRepo.getProfileByUid(CURRENT_USER_ID))
      }

  @Test
  fun deleteUserAccount_doesNotAffectOtherUsersData() =
      runTest(testDispatcher, testTimeout) {
        profileRepo.addProfile(currentUserProfile)
        profileRepo.addProfile(friend1Profile)
        groupsRepo.addGroup(otherUserGroup)

        useCase.deleteUserAccount(CURRENT_USER_ID)

        assertNotNull(profileRepo.getProfileByUid(FRIEND_1_ID))
        assertEquals(1, groupsRepo.getAllGroups().size)
      }
}
