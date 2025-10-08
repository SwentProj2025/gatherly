package com.android.gatherly.viewmodel

import com.android.gatherly.model.todo.BootcampToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.ui.map.Location
import com.google.firebase.Timestamp

object MapViewModelTestsTodos {
  val testLocation1 = Location(latitude = 46.5190, longitude = 6.5668, name = "BC Building")
  val testLocation2 =
      Location(latitude = 46.5186, longitude = 6.5661, name = "Rolex Learning Center")

  val incompleteTodoWithLocation1 =
      BootcampToDo(
          uid = "todo1",
          name = "Study for exam",
          description = "Prepare for CS-311 midterm",
          assigneeName = "Alex",
          dueDate = Timestamp(1730000000, 0),
          location = testLocation1,
          status = ToDoStatus.STARTED,
          ownerId = "user123")

  val incompleteTodoWithLocation2 =
      BootcampToDo(
          uid = "todo5",
          name = "Master unit testing",
          description = "Prepare tests for Map ViewModel",
          assigneeName = "Alex",
          dueDate = Timestamp(1730000000, 0),
          location = testLocation1,
          status = ToDoStatus.STARTED,
          ownerId = "user123")

  val completeTodoWithLocation =
      BootcampToDo(
          uid = "todo2",
          name = "Submit assignment",
          description = "Upload to Moodle",
          assigneeName = "Alex",
          dueDate = Timestamp(1729000000, 0),
          location = testLocation2,
          status = ToDoStatus.ENDED,
          ownerId = "user123")

  val incompleteTodoWithoutLocation =
      BootcampToDo(
          uid = "todo3",
          name = "Read textbook",
          description = "Chapter 5",
          assigneeName = "Alex",
          dueDate = Timestamp(1731000000, 0),
          location = null,
          status = ToDoStatus.CREATED,
          ownerId = "user123")

  val completeTodoWithoutLocation =
      BootcampToDo(
          uid = "todo4",
          name = "Watch lecture",
          description = "Week 3 videos",
          assigneeName = "Alex",
          dueDate = Timestamp(1728000000, 0),
          location = null,
          status = ToDoStatus.ARCHIVED,
          ownerId = "user123")

  val testedTodos: List<BootcampToDo> =
      listOf(
          incompleteTodoWithLocation1,
          incompleteTodoWithLocation2,
          completeTodoWithLocation,
          incompleteTodoWithoutLocation,
          completeTodoWithoutLocation)
}
