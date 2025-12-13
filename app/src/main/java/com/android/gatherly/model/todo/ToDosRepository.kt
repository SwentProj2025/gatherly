package com.android.gatherly.model.todo

/**
 * Repository interface that defines all operations for managing [ToDo] items.
 *
 * This abstraction allows different data sources (e.g., Firestore, local DB, or fake data)
 */
interface ToDosRepository {

  /** Generates and returns a new unique identifier for a [ToDo] item. */
  fun getNewUid(): String

  /**
   * Retrieves all [ToDo] items from the repository.
   *
   * @return A list of all [ToDo] items.
   */
  suspend fun getAllTodos(): List<ToDo>

  /**
   * Retrieves a specific [ToDo] item by its unique identifier.
   *
   * @param todoID The unique identifier of the [ToDo] item to retrieve.
   * @return The [ToDo] item with the specified identifier.
   */
  suspend fun getTodo(todoID: String): ToDo

  /**
   * Adds a new [ToDo] item to the repository.
   *
   * @param toDo The [ToDo] item to add.
   */
  suspend fun addTodo(toDo: ToDo)

  /**
   * Edits an existing [ToDo] item in the repository.
   *
   * @param todoID The unique identifier of the [ToDo] item to edit.
   * @param newValue The new value for the [ToDo] item.
   */
  suspend fun editTodo(todoID: String, newValue: ToDo)

  /**
   * Deletes a [ToDo] item from the repository.
   *
   * @param todoID The unique identifier of the [ToDo] item to delete.
   */
  suspend fun deleteTodo(todoID: String)

  /**
   * Toggles the status of a [ToDo] (e.g., between ongoing and ended).
   *
   * @param todoID The identifier of the [ToDo] to toggle.
   */
  suspend fun toggleStatus(todoID: String)

  /**
   * Retrieves all [ToDo] items marked as ended from the repository.
   *
   * @return A list of all [ToDo] items marked as ended.
   */
  suspend fun getAllEndedTodos(): List<ToDo>

  /** Retrieves all [ToDo] items to put the tag to null when this one is deleted */
  suspend fun updateTodosTagToNull(categoryId: String, ownerId: String)
}
