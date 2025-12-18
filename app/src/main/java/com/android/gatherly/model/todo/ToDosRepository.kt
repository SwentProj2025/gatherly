package com.android.gatherly.model.todo

import com.android.gatherly.model.todoCategory.ToDoCategory

/**
 * Repository interface that defines all operations for managing [ToDo] items.
 *
 * This abstraction allows different implementations (e.g., Firestore or in-memory repositories)
 */
interface ToDosRepository {

  /** Generates and returns a new unique identifier for a [ToDo] item. */
  fun getNewUid(): String

  /**
   * Retrieves all [ToDo] items owned by the current signed-in user.
   *
   * @return A list of all the user's [ToDo] items.
   */
  suspend fun getAllTodos(): List<ToDo>

  /**
   * Retrieves a specific [ToDo] item by its unique identifier.
   *
   * @param todoId The unique identifier of the [ToDo] item to retrieve.
   * @return The corresponding [ToDo].
   */
  suspend fun getTodo(todoId: String): ToDo

  /**
   * Adds a new [ToDo] item to the repository.
   *
   * @param toDo The [ToDo] item to add.
   */
  suspend fun addTodo(toDo: ToDo)

  /**
   * Edits an existing [ToDo] item in the repository.
   *
   * @param todoId The unique identifier of the [ToDo] item to edit.
   * @param newValue The new value for the [ToDo] item.
   */
  suspend fun editTodo(todoId: String, newValue: ToDo)

  /**
   * Deletes a [ToDo] item from the repository.
   *
   * @param todoId The unique identifier of the [ToDo] item to delete.
   */
  suspend fun deleteTodo(todoId: String)

  /**
   * Toggles the status of a [ToDo] between [ToDoStatus.ONGOING] and [ToDoStatus.ENDED].
   *
   * @param todoId The identifier of the [ToDo] to toggle.
   */
  suspend fun toggleStatus(todoId: String)

  /**
   * Retrieves all [ToDo] items marked as ended from the repository.
   *
   * @return A list of all [ToDo] items marked as ended.
   */
  suspend fun getAllEndedTodos(): List<ToDo>

  /**
   * Clears the tag reference for all [ToDo] items using a given category.
   *
   * This is typically called when a [ToDoCategory] is deleted to avoid dangling references.
   *
   * @param categoryId The identifier of the deleted category.
   * @param ownerId The owner of the affected [ToDo] items.
   */
  suspend fun updateTodosTagToNull(categoryId: String, ownerId: String)
}
