package com.android.gatherly.utilstest

import com.android.gatherly.model.event.Event
import com.android.gatherly.model.event.EventStatus
import com.android.gatherly.model.focusSession.FocusSession
import com.android.gatherly.model.group.Group
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.utils.DeleteUserAccountUseCase
import com.google.firebase.Timestamp
import kotlin.time.Duration

/**
 * Contains test data fixtures for [DeleteUserAccountUseCase] tests.
 *
 * Provides sample groups, todos, and events for testing various user scenarios.
 */
object DeleteUserAccountUseCaseTestData {
  // User IDs
  const val CURRENT_USER_ID = "userA"
  const val FRIEND_1_ID = "userB"

  // Profiles
  val currentUserProfile = Profile(uid = CURRENT_USER_ID, name = "Current User")

  val friend1Profile = Profile(uid = FRIEND_1_ID, name = "Friend 1")

  // Groups
  val ownedGroup =
      Group(
          gid = "group_owned",
          name = "Owned Group",
          creatorId = CURRENT_USER_ID,
          adminIds = listOf(CURRENT_USER_ID),
          memberIds = listOf(CURRENT_USER_ID))

  val sharedGroup =
      Group(
          gid = "group_shared",
          name = "Shared Group",
          creatorId = FRIEND_1_ID,
          adminIds = listOf(FRIEND_1_ID),
          memberIds = listOf(CURRENT_USER_ID, FRIEND_1_ID))

  val otherUserGroup =
      Group(
          gid = "group_other",
          name = "Other User Group",
          creatorId = FRIEND_1_ID,
          adminIds = listOf(FRIEND_1_ID),
          memberIds = listOf(FRIEND_1_ID))

  // Events
  val ownedEvent =
      Event(
          id = "event_owned",
          title = "Owned Event",
          description = "",
          creatorName = "",
          location = null,
          date = Timestamp(1700000000, 0),
          startTime = Timestamp(1700000000, 0),
          endTime = Timestamp(1700003600, 0),
          creatorId = CURRENT_USER_ID,
          participants = listOf(CURRENT_USER_ID),
          status = EventStatus.UPCOMING)

  val sharedEvent =
      Event(
          id = "event_shared",
          title = "Shared Event",
          description = "",
          creatorName = "",
          location = null,
          date = Timestamp(1700000000, 0),
          startTime = Timestamp(1700000000, 0),
          endTime = Timestamp(1700003600, 0),
          creatorId = FRIEND_1_ID,
          participants = listOf(CURRENT_USER_ID, FRIEND_1_ID),
          status = EventStatus.UPCOMING)

  // Focus session
  val focusSession =
      FocusSession(
          focusSessionId = "focus_1", creatorId = CURRENT_USER_ID, duration = Duration.ZERO)

  // Todo
  val todo =
      ToDo(
          uid = "todo_1",
          name = "Todo",
          description = "",
          dueDate = null,
          dueTime = null,
          location = null,
          status = ToDoStatus.ONGOING,
          ownerId = CURRENT_USER_ID)
}
