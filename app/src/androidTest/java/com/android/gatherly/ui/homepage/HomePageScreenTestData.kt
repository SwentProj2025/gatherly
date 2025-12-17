package com.android.gatherly.ui.homepage

import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileStatus
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.google.firebase.Timestamp

/**
 * Shared test data used by HomePage UI tests.
 *
 * Provides reusable fake profiles and todos to avoid duplication across test cases and improve
 * readability.
 */
object HomePageScreenTestData {

  val friend1 =
      Profile(
          uid = "homePageTests_friend1",
          name = "Alice",
          focusSessionIds = emptyList(),
          participatingEventIds = emptyList(),
          groupIds = emptyList(),
          friendUids = emptyList(),
          status = ProfileStatus.ONLINE)
  val friend2 =
      Profile(
          uid = "homePageTests_friend2",
          name = "Bob",
          focusSessionIds = emptyList(),
          participatingEventIds = emptyList(),
          groupIds = emptyList(),
          friendUids = emptyList(),
          status = ProfileStatus.FOCUSED)

  val todo1 =
      ToDo(
          uid = "todo_1",
          name = "Plan party",
          description = "Buy decorations and invite friends",
          dueDate = Timestamp.now(),
          dueTime = null,
          location = null,
          status = ToDoStatus.ONGOING,
          ownerId = "user1")

  val currentProfile: Profile =
      Profile(
          uid = "0+",
          name = "Current",
          focusSessionIds = emptyList(),
          participatingEventIds = emptyList(),
          groupIds = emptyList(),
          friendUids = listOf(friend1.uid, friend2.uid))
}
