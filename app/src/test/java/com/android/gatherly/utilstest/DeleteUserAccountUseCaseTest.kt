package com.android.gatherly.utils

import com.android.gatherly.model.event.Event
import com.android.gatherly.model.event.EventStatus
import com.android.gatherly.model.event.EventsLocalRepository
import com.android.gatherly.model.focusSession.FocusSession
import com.android.gatherly.model.focusSession.FocusSessionsLocalRepository
import com.android.gatherly.model.group.Group
import com.android.gatherly.model.group.GroupsLocalRepository
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todo.ToDosLocalRepository
import com.google.firebase.Timestamp
import kotlin.time.Duration
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

  @Before
  fun setup() {
    profileRepo = ProfileLocalRepository()
    groupsRepo = GroupsLocalRepository()
    eventsRepo = EventsLocalRepository()
    focusRepo = FocusSessionsLocalRepository()
    todosRepo = ToDosLocalRepository()

    useCase =
        DeleteUserAccountUseCase(
            profileRepository = profileRepo,
            groupsRepository = groupsRepo,
            eventsRepository = eventsRepo,
            focusSessionsRepository = focusRepo,
            todosRepository = todosRepo)
  }

  @Test
  fun deleteUserAccount_deletesOwnedGroups() = runTest {
    val group =
        Group(
            gid = groupsRepo.getNewId(),
            name = "Owned group",
            creatorId = userId,
            adminIds = listOf(userId),
            memberIds = listOf(userId))
    groupsRepo.addGroup(group)

    useCase.deleteUserAccount(userId)

    assertTrue(groupsRepo.getAllGroups().isEmpty())
  }

  @Test
  fun deleteUserAccount_removesUserFromMemberGroups() = runTest {
    val group =
        Group(
            gid = groupsRepo.getNewId(),
            name = "Shared group",
            creatorId = otherUserId,
            adminIds = listOf(otherUserId),
            memberIds = listOf(userId, otherUserId))
    groupsRepo.addGroup(group)

    useCase.deleteUserAccount(userId)

    val updatedGroup = groupsRepo.getAllGroups().single()
    assertFalse(updatedGroup.memberIds.contains(userId))
    assertTrue(updatedGroup.memberIds.contains(otherUserId))
  }

  @Test
  fun deleteUserAccount_deletesOwnedEvents() = runTest {
    val event =
        Event(
            id = eventsRepo.getNewId(),
            title = "Owned event",
            description = "",
            creatorName = "",
            location = null,
            date = Timestamp(1700000000, 0),
            startTime = Timestamp(1700000000, 0),
            endTime = Timestamp(1700000000 + 3600, 0),
            creatorId = userId,
            participants = listOf(userId),
            status = EventStatus.UPCOMING)
    eventsRepo.addEvent(event)

    useCase.deleteUserAccount(userId)

    assertTrue(eventsRepo.getAllEvents().isEmpty())
  }

  @Test
  fun deleteUserAccount_removesUserFromParticipatingEvents() = runTest {
    val event =
        Event(
            id = eventsRepo.getNewId(),
            title = "Shared event",
            description = "",
            creatorName = "",
            location = null,
            date = Timestamp(1700000000, 0),
            startTime = Timestamp(1700000000, 0),
            endTime = Timestamp(1700000000 + 3600, 0),
            creatorId = otherUserId,
            participants = listOf(userId, otherUserId),
            status = EventStatus.UPCOMING)
    eventsRepo.addEvent(event)

    useCase.deleteUserAccount(userId)

    val updatedEvent = eventsRepo.getAllEvents().single()
    assertFalse(updatedEvent.participants.contains(userId))
    assertTrue(updatedEvent.participants.contains(otherUserId))
  }

  @Test
  fun deleteUserAccount_deletesUserFocusSessions() = runTest {
    val session =
        FocusSession(
            focusSessionId = focusRepo.getNewId(), creatorId = userId, duration = Duration.ZERO)
    focusRepo.addFocusSession(session)

    useCase.deleteUserAccount(userId)

    assertTrue(focusRepo.getAllFocusSessions().isEmpty())
  }

  @Test
  fun deleteUserAccount_deletesUserTodos() = runTest {
    val todo =
        ToDo(
            uid = todosRepo.getNewUid(),
            name = "Todo",
            description = "",
            dueDate = null,
            dueTime = null,
            location = null,
            status = ToDoStatus.ONGOING,
            ownerId = userId)
    todosRepo.addTodo(todo)

    useCase.deleteUserAccount(userId)

    assertTrue(todosRepo.getAllTodos().isEmpty())
  }

  @Test
  fun deleteUserAccount_deletesProfileLast() = runTest {
    profileRepo.addProfile(Profile(uid = userId, name = "Alice"))

    useCase.deleteUserAccount(userId)

    assertNull(profileRepo.getProfileByUid(userId))
  }

  @Test
  fun deleteUserAccount_doesNotAffectOtherUsersData() = runTest {
    profileRepo.addProfile(Profile(uid = userId, name = "Alice"))
    profileRepo.addProfile(Profile(uid = otherUserId, name = "Bob"))

    val group =
        Group(
            gid = groupsRepo.getNewId(),
            name = "Bob group",
            creatorId = otherUserId,
            adminIds = listOf(otherUserId),
            memberIds = listOf(otherUserId))
    groupsRepo.addGroup(group)

    useCase.deleteUserAccount(userId)

    assertNotNull(profileRepo.getProfileByUid(otherUserId))
    assertEquals(1, groupsRepo.getAllGroups().size)
  }
}
