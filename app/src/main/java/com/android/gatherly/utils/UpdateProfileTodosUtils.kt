package com.android.gatherly.utils

import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todo.ToDosRepository


/**
 * Function to add a ToDo and update the user's badges accordingly.
 * @param todoRepository The repository to manage ToDos.
 * @param profileRepository The repository to manage Profiles.
 * @param todo The ToDo to be added.
 * @param currentUserId The ID of the current user.
 */
suspend fun addTodo_updateBadges(
    todoRepository: ToDosRepository,
    profileRepository: ProfileRepository,
    todo: ToDo,
    currentUserId: String
) {
  todoRepository.addTodo(todo)
  val currentProfile: Profile = profileRepository.getProfileByUid(currentUserId) ?: return

  profileRepository.updateBadges(currentProfile)
}

/**
 * Function to delete a ToDo and update the user's badges accordingly.
 * @param todoRepository The repository to manage ToDos.
 * @param profileRepository The repository to manage Profiles.
 * @param todoID The ID of the ToDo to be deleted.
 * @param currentUserId The ID of the current user.
 */
suspend fun deleteTodo_updateBadges(
    todoRepository: ToDosRepository,
    profileRepository: ProfileRepository,
    todoID: String,
    currentUserId: String
) {
  todoRepository.deleteTodo(todoID = todoID)
  val currentProfile = profileRepository.getProfileByUid(currentUserId) ?: return
  profileRepository.updateBadges(currentProfile)
}


/**
 * Function to edit a ToDo's status and update the user's badges accordingly.
 * @param todoRepository The repository to manage ToDos.
 * @param profileRepository The repository to manage Profiles.
 * @param todoID The ID of the ToDo to be edited.
 * @param newStatus The new status of the ToDo.
 * @param currentUserId The ID of the current user.
 */
suspend fun editTodo_updateBadges(
    todoRepository: ToDosRepository,
    profileRepository: ProfileRepository,
    todoID: String,
    newStatus: ToDoStatus,
    currentUserId: String
) {
  val updatedTodo = todoRepository.getTodo(todoID).copy(status = newStatus)
  todoRepository.editTodo(todoID, updatedTodo)
  val currentProfile = profileRepository.getProfileByUid(currentUserId) ?: return
  profileRepository.updateBadges(currentProfile)
}
