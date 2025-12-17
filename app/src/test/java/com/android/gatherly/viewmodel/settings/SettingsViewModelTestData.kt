package com.android.gatherly.viewmodel.settings

import com.android.gatherly.model.event.Event
import com.android.gatherly.model.event.EventState
import com.android.gatherly.model.event.EventStatus
import com.android.gatherly.model.focusSession.FocusSession
import com.android.gatherly.model.group.Group
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoPriority
import com.android.gatherly.model.todo.ToDoStatus
import com.google.firebase.Timestamp
import kotlin.time.Duration.Companion.minutes

object SettingsViewModelTestData {

  const val TEST_USER_ID = "test_user"
  const val OTHER_USER_ID = "other_user"

  // ------------------------------------------------------------------------
  // GROUP
  // ------------------------------------------------------------------------

  val ownedGroup =
      Group(
          gid = "group_owned",
          creatorId = TEST_USER_ID,
          name = "Owned Group",
          memberIds = listOf(TEST_USER_ID),
          adminIds = listOf(TEST_USER_ID))

  val memberGroup =
      Group(
          gid = "group_member",
          creatorId = OTHER_USER_ID,
          name = "Member Group",
          memberIds = listOf(TEST_USER_ID, OTHER_USER_ID),
          adminIds = listOf(OTHER_USER_ID))

  // ------------------------------------------------------------------------
  // EVENT
  // ------------------------------------------------------------------------

  val ownedEvent =
      Event(
          id = "event_owned",
          title = "Owned Event",
          description = "Event created by test user",
          creatorName = "Test User",
          location = null,
          date = Timestamp.now(),
          startTime = Timestamp.now(),
          endTime = Timestamp.now(),
          creatorId = TEST_USER_ID,
          participants = listOf(TEST_USER_ID),
          status = EventStatus.UPCOMING,
          state = EventState.PUBLIC)

  val participatingEvent =
      Event(
          id = "event_participating",
          title = "Participating Event",
          description = "Event created by another user",
          creatorName = "Other User",
          location = null,
          date = Timestamp.now(),
          startTime = Timestamp.now(),
          endTime = Timestamp.now(),
          creatorId = OTHER_USER_ID,
          participants = listOf(TEST_USER_ID),
          status = EventStatus.UPCOMING,
          state = EventState.PUBLIC)

  // ------------------------------------------------------------------------
  // FOCUS SESSION
  // ------------------------------------------------------------------------

  val focusSession =
      FocusSession(focusSessionId = "focus_1", creatorId = TEST_USER_ID, duration = 30.minutes)

  // ------------------------------------------------------------------------
  // TODO
  // ------------------------------------------------------------------------

  val todo =
      ToDo(
          uid = "todo_1",
          name = "Test Todo",
          description = "Todo owned by test user",
          dueDate = null,
          dueTime = null,
          location = null,
          status = ToDoStatus.ONGOING,
          ownerId = TEST_USER_ID,
          priorityLevel = ToDoPriority.NONE)
}
