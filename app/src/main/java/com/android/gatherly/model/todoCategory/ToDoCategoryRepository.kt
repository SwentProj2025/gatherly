package com.android.gatherly.model.todoCategory

/**
 * Repository interface that defines all operations for managing [ToDoCategory] items.
 *
 * This abstraction allows different data sources (e.g., Firestore, local DB, or fake data)
 */
interface ToDoCategoryRepository {

  /** Generates and returns a new unique identifier for an [ToDoCategory]. */
  fun getNewId(): String

  /** Creates the 4 defaults [ToDoCategory] */
  suspend fun initializeDefaultCategories()

  /**
   * Retrieves all [ToDoCategory]s from the repository.
   *
   * @return A list of all [ToDoCategory]s.
   */
  suspend fun getAllCategories(): List<ToDoCategory>

  /**
   * Adds a new [ToDoCategory] to the repository.
   *
   * @param category The [ToDoCategory] to add.
   */
  suspend fun addToDoCategory(category: ToDoCategory)

  /**
   * Deletes an [ToDoCategory] from the repository.
   *
   * @param categoryId The unique identifier of the [ToDoCategory] to delete.
   */
  suspend fun deleteToDoCategory(categoryId: String)
}
