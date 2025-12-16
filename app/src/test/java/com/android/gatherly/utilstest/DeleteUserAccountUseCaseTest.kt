package com.android.gatherly.utils

import com.android.gatherly.model.event.EventsLocalRepository
import com.android.gatherly.model.focusSession.FocusSessionsLocalRepository
import com.android.gatherly.model.group.GroupsLocalRepository
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.todo.ToDosLocalRepository
import com.android.gatherly.utilstest.DeleteUserAccountUseCaseTestData
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [DeleteUserAccountUseCase] using local in-memory repositories.
 *
 * These tests verify that all user-owned data is deleted and that shared data is correctly updated
 * when a user deletes their account.
 */
class DeleteUserAccountUseCaseTest {

  private val userId = "userA"
  private val otherUserId = "userB"

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
        groupsRepo.addGroup(DeleteUserAccountUseCaseTestData.ownedGroup)

        useCase.deleteUserAccount(DeleteUserAccountUseCaseTestData.CURRENT_USER_ID)

        assertTrue(groupsRepo.getAllGroups().isEmpty())
      }

  @Test
  fun deleteUserAccount_removesUserFromMemberGroups() =
      runTest(testDispatcher, testTimeout) {
        groupsRepo.addGroup(DeleteUserAccountUseCaseTestData.sharedGroup)

        useCase.deleteUserAccount(DeleteUserAccountUseCaseTestData.CURRENT_USER_ID)

        val updatedGroup = groupsRepo.getAllGroups().single()
        assertFalse(
            updatedGroup.memberIds.contains(DeleteUserAccountUseCaseTestData.CURRENT_USER_ID))
        assertTrue(updatedGroup.memberIds.contains(DeleteUserAccountUseCaseTestData.FRIEND_1_ID))
      }

  @Test
  fun deleteUserAccount_deletesOwnedEvents() =
      runTest(testDispatcher, testTimeout) {
        eventsRepo.addEvent(DeleteUserAccountUseCaseTestData.ownedEvent)

        useCase.deleteUserAccount(DeleteUserAccountUseCaseTestData.CURRENT_USER_ID)

        assertTrue(eventsRepo.getAllEvents().isEmpty())
      }

  @Test
  fun deleteUserAccount_removesUserFromParticipatingEvents() =
      runTest(testDispatcher, testTimeout) {
        eventsRepo.addEvent(DeleteUserAccountUseCaseTestData.sharedEvent)

        useCase.deleteUserAccount(DeleteUserAccountUseCaseTestData.CURRENT_USER_ID)

        val updatedEvent = eventsRepo.getAllEvents().single()
        assertFalse(
            updatedEvent.participants.contains(DeleteUserAccountUseCaseTestData.CURRENT_USER_ID))
        assertTrue(updatedEvent.participants.contains(DeleteUserAccountUseCaseTestData.FRIEND_1_ID))
      }

  @Test
  fun deleteUserAccount_deletesUserFocusSessions() =
      runTest(testDispatcher, testTimeout) {
        focusRepo.addFocusSession(DeleteUserAccountUseCaseTestData.focusSession)

        useCase.deleteUserAccount(DeleteUserAccountUseCaseTestData.CURRENT_USER_ID)

        assertTrue(focusRepo.getAllFocusSessions().isEmpty())
      }

  @Test
  fun deleteUserAccount_deletesUserTodos() =
      runTest(testDispatcher, testTimeout) {
        todosRepo.addTodo(DeleteUserAccountUseCaseTestData.todo)

        useCase.deleteUserAccount(DeleteUserAccountUseCaseTestData.CURRENT_USER_ID)

        assertTrue(todosRepo.getAllTodos().isEmpty())
      }

  @Test
  fun deleteUserAccount_deletesProfileLast() =
      runTest(testDispatcher, testTimeout) {
        profileRepo.addProfile(DeleteUserAccountUseCaseTestData.currentUserProfile)

        useCase.deleteUserAccount(DeleteUserAccountUseCaseTestData.CURRENT_USER_ID)

        assertNull(profileRepo.getProfileByUid(DeleteUserAccountUseCaseTestData.CURRENT_USER_ID))
      }

  @Test
  fun deleteUserAccount_doesNotAffectOtherUsersData() =
      runTest(testDispatcher, testTimeout) {
        profileRepo.addProfile(DeleteUserAccountUseCaseTestData.currentUserProfile)
        profileRepo.addProfile(DeleteUserAccountUseCaseTestData.friend1Profile)
        groupsRepo.addGroup(DeleteUserAccountUseCaseTestData.otherUserGroup)

        useCase.deleteUserAccount(DeleteUserAccountUseCaseTestData.CURRENT_USER_ID)

        assertNotNull(profileRepo.getProfileByUid(DeleteUserAccountUseCaseTestData.FRIEND_1_ID))
        assertEquals(1, groupsRepo.getAllGroups().size)
      }
}
