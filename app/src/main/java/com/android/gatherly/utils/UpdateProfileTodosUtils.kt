package com.android.gatherly.utils

import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todo.ToDosRepository

/**
 * Function to add a ToDo and update the user's counts and badges accordingly.
 *
 * @param todoRepository The repository to manage ToDos.
 * @param profileRepository The repository to manage Profiles.
 * @param todo The ToDo to be added.
 * @param currentUserId The ID of the current user.
 */
suspend fun addTodo(
    todoRepository: ToDosRepository,
    profileRepository: ProfileRepository,
    todo: ToDo,
    currentUserId: String
) {
  todoRepository.addTodo(todo)
  profileRepository.incrementCreatedTodo(currentUserId)
}

/**
 * Function to edit a ToDo's status and update the user's counts and badges accordingly.
 *
 * @param todoRepository The repository to manage ToDos.
 * @param profileRepository The repository to manage Profiles.
 * @param todoID The ID of the ToDo to be edited.
 * @param newStatus The new status of the ToDo.
 * @param currentUserId The ID of the current user.
 */
suspend fun editTodo(
    todoRepository: ToDosRepository,
    profileRepository: ProfileRepository,
    todoID: String,
    newStatus: ToDoStatus,
    currentUserId: String
) {
  val existing = todoRepository.getTodo(todoID)
  val wasCompleted = existing.status == ToDoStatus.ENDED
  val updatedTodo = existing.copy(status = newStatus)

  todoRepository.editTodo(todoID, updatedTodo)

  val isNowCompleted = newStatus == ToDoStatus.ENDED
  if (!wasCompleted && isNowCompleted) {
    profileRepository.incrementCompletedTodo(currentUserId)
  }
}
