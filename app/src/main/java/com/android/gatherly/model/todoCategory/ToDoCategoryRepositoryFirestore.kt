package com.android.gatherly.model.todoCategory

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.android.gatherly.ui.theme.theme_todo_tag_default
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

/**
 * Firestore-based implementation of [ToDoCategoryRepository].
 *
 * This repository stores each user's ToDoCategoryRepository items under:
 * /users/{userId}/todoCategories/{todoCategoriesId}
 *
 * All methods are asynchronous and must be called from a coroutine scope.
 *
 * @param db A [FirebaseFirestore] instance used for database operations.
 */
class ToDoCategoryRepositoryFirestore(private val db: FirebaseFirestore) : ToDoCategoryRepository {

  /** Reference to the "todoCategories" collection of the currently signed-in user. */
  private val collection
    get() = db.collection("users").document(currentUserId()).collection("todoCategories")

  /**
   * Returns the user ID of the currently signed-in user, or throws an exception if no user is
   * signed in.
   */
  private fun currentUserId(): String {
    return Firebase.auth.currentUser?.uid ?: throw IllegalStateException("No signed in user")
  }

  /**
   * Converts a Firestore [DocumentSnapshot] into a [ToDoCategory] object.
   *
   * @param doc The Firestore [DocumentSnapshot] to convert.
   * @return A [ToDoCategory] if all required fields are present; `null` otherwise.
   */
  private fun snapshotToCategory(doc: DocumentSnapshot): ToDoCategory? {
    val id = doc.getString("id") ?: return null
    val name = doc.getString("name") ?: return null
    val color =
        try {
          val colorValue = (doc.data?.get("color") as? Long) ?: 0xFFFFFFFFF
          Color(colorValue)
        } catch (e: Exception) {
          theme_todo_tag_default
        }
    val ownerId = doc.getString("ownerId") ?: return null
    val isDefault = doc.getBoolean("isDefault") ?: false
    val isDeleted = doc.getBoolean("isDeleted") ?: false

    return ToDoCategory(id, name, color, ownerId, isDefault, isDeleted)
  }

  /**
   * Converts a [ToDoCategory] object into a [Map] suitable for storing in Firestore.
   *
   * @param category The [ToDoCategory] to convert.
   * @return A [Map] containing the category fields suitable for Firestore.
   */
  private fun categoryToMap(category: ToDoCategory): Map<String, Any> {
    return mapOf(
        "id" to category.id,
        "name" to category.name,
        "color" to category.color.toArgb(),
        "ownerId" to category.ownerId,
        "isDefault" to category.isDefault,
        "isDeleted" to category.isDeleted)
  }

  /** Returns a new unique ID for a category. */
  override fun getNewId(): String {
    return collection.document().id
  }

  /** Creates the 4 defaults [ToDoCategory] */
  override suspend fun initializeDefaultCategories() {
    val existingCategories = getAllCategories()
    if (existingCategories.isEmpty()) {
      for (defaultCategory in DEFAULT_CATEGORIES) {
        val category =
            defaultCategory.copy(
                ownerId = currentUserId(),
                id = if (defaultCategory.isDefault) defaultCategory.id else getNewId())
        collection.document(category.id).set(categoryToMap(category)).await()
      }
    }
  }

  /**
   * Fetches all todoCategories belonging to the current user.
   *
   * @return A list of [ToDoCategory] owned by the user.
   * @throws IllegalStateException if no user is signed in.
   */
  override suspend fun getAllCategories(): List<ToDoCategory> {
    val snap = collection.whereEqualTo("isDeleted", false).get().await()
    return snap.documents.mapNotNull { doc -> snapshotToCategory(doc) }
  }

  /**
   * Adds a new [ToDoCategory] for the signed-in user.
   *
   * Overwrites the `ownerId` field of the new [ToDoCategory] with the current user's UID.
   *
   * @param category The [ToDoCategory] item to create.
   * @throws IllegalStateException if no user is signed in.
   */
  override suspend fun addToDoCategory(category: ToDoCategory) {
    val newCategory = category.copy(ownerId = currentUserId(), id = getNewId(), isDefault = false)
    collection.document(newCategory.id).set(categoryToMap(newCategory)).await()
  }

  /**
   * Deletes a [ToDoCategory] by ID.
   *
   * @param categoryId The unique ID matching the [ToDoCategory] to delete.
   * @throws NoSuchElementException if the [ToDoCategory] does not exist.
   * @throws SecurityException if the [ToDoCategory] is not owned by the signed-in user.
   */
  override suspend fun deleteToDoCategory(categoryId: String) {
    val docSnap = collection.document(categoryId).get().await()
    val existing = snapshotToCategory(docSnap) ?: throw NoSuchElementException("Category not found")

    if (existing.ownerId != currentUserId()) {
      throw SecurityException("Category does not belong to user")
    }

    if (existing.isDefault) {
      collection.document(categoryId).update("isDeleted", true).await()
    } else {
      collection.document(categoryId).delete().await()
    }
  }
}
