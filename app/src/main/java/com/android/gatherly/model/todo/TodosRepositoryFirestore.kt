package com.android.gatherly.model.todo

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.android.gatherly.model.map.Location
import com.android.gatherly.model.todoCategory.ToDoCategory
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.NoSuchElementException
import kotlinx.coroutines.tasks.await

/**
 * Firestore-based implementation of [ToDosRepository].
 *
 * This repository stores each user's ToDo items under: /users/{userId}/todos/{todoId}
 *
 * All methods are asynchronous and must be called from a coroutine scope.
 *
 * @param db A [FirebaseFirestore] instance used for database operations.
 */
class ToDosRepositoryFirestore(private val db: FirebaseFirestore) : ToDosRepository {

  /** Reference to the "todos" collection of the currently signed-in user. */
  private val collection
    get() = db.collection("users").document(currentUserId()).collection("todos")

  /**
   * Returns the UID of the currently authenticated Firebase user.
   *
   * @throws IllegalStateException if no user is signed in.
   */
  private fun currentUserId(): String {
    return Firebase.auth.currentUser?.uid ?: throw IllegalStateException("No signed in user")
  }

  override fun getNewUid(): String {
    return collection.document().id
  }

  /**
   * Fetches all todos belonging to the current user.
   *
   * @return A list of todos owned by the user.
   * @throws IllegalStateException if no user is signed in.
   */
  override suspend fun getAllTodos(): List<ToDo> {
    val snap = collection.get().await() // QuerySnapShot in this case pretty much list of docs.
    return snap.documents.mapNotNull { doc -> snapshotToToDo(doc) }
  }

  /**
   * Retrieves a single todo by ID.
   *
   * @param todoId The unique ID matching the wanted [ToDo].
   * @return The matching [ToDo].
   * @throws NoSuchElementException if the [ToDo] is missing.
   * @throws SecurityException if the document's ownerId differs from the current user.
   */
  override suspend fun getTodo(todoId: String): ToDo {
    val doc = collection.document(todoId).get().await()
    val todo = snapshotToToDo(doc) ?: throw NoSuchElementException("Todo with id=$todoId not found")
    if (todo.ownerId != currentUserId()) {
      throw SecurityException("Todo doesn't belong to signed in user.")
    }
    return todo
  }

  /**
   * Adds a new [ToDo] for the signed-in user.
   *
   * Overwrites the `ownerId` field of the new [ToDo] with the current user's UID.
   *
   * @param toDo The [ToDo] item to create.
   * @throws IllegalStateException if no user is signed in.
   */
  override suspend fun addTodo(toDo: ToDo) {
    val ownedToDo = toDo.copy(ownerId = currentUserId())
    collection.document(ownedToDo.uid).set(todoToMap(ownedToDo)).await()
  }

  /**
   * Updates an existing [ToDo] with new data.
   *
   * @param todoId The unique ID matching the [ToDo] to update.
   * @param newValue The new [ToDo] content to store.
   * @throws NoSuchElementException if no document matches the given ID.
   * @throws SecurityException if the [ToDo] is not owned by the signed-in user.
   */
  override suspend fun editTodo(todoId: String, newValue: ToDo) {
    val doc = collection.document(todoId).get().await()
    val existing =
        snapshotToToDo(doc) ?: throw NoSuchElementException("Todo with id=$todoId not found")

    if (existing.ownerId != currentUserId()) {
      throw SecurityException("Todo does not belong to signed in user")
    }

    val newValue = newValue.copy(ownerId = currentUserId())
    collection.document(todoId).set(todoToMap(newValue)).await()
  }

  /**
   * Deletes a [ToDo] by ID.
   *
   * @param todoId The unique ID matching the [ToDo] to delete.
   * @throws NoSuchElementException if the [ToDo] does not exist.
   * @throws SecurityException if the [ToDo] is not owned by the signed-in user.
   */
  override suspend fun deleteTodo(todoId: String) {
    val doc = collection.document(todoId).get().await()
    val existing =
        snapshotToToDo(doc) ?: throw NoSuchElementException("Todo with id=$todoId not found")

    if (existing.ownerId != currentUserId()) {
      throw SecurityException("Todo does not belong to signed in user")
    }

    collection.document(todoId).delete().await()
  }

  /**
   * Toggles a [ToDo]'s completion status between [ToDoStatus.ONGOING] and [ToDoStatus.ENDED].
   *
   * @param todoId The unique ID matching the [ToDo] to toggle.
   * @throws NoSuchElementException if the [ToDo] does not exist.
   * @throws SecurityException if the [ToDo] is not owned by the signed-in user.
   */
  override suspend fun toggleStatus(todoId: String) {
    val todo = getTodo(todoId)
    val newStatus = if (todo.status == ToDoStatus.ENDED) ToDoStatus.ONGOING else ToDoStatus.ENDED
    editTodo(todoId, todo.copy(status = newStatus))
  }

  /**
   * Fetches all [ToDo]s marked as [ToDoStatus.ENDED].
   *
   * @return A list of completed [ToDo]s of the current user.
   * @throws IllegalStateException if no user is signed in.
   */
  override suspend fun getAllEndedTodos(): List<ToDo> {
    val snap =
        collection
            .whereEqualTo("ownerId", currentUserId())
            .whereEqualTo("status", ToDoStatus.ENDED.name)
            .get()
            .await() // QuerySnapShot in this case pretty much list of docs.
    return snap.documents.mapNotNull { doc -> snapshotToToDo(doc) }
  }

  override suspend fun updateTodosTagToNull(categoryId: String, ownerId: String) {
    val todosToUpdate =
        db.collection("users")
            .document(ownerId)
            .collection("todos")
            .whereEqualTo("tag.id", categoryId)
            .get()
            .await()

    val batch = db.batch()

    for (document in todosToUpdate.documents) {
      batch.update(document.reference, "tag", null)
    }
    batch.commit().await()
  }

  /**
   * Converts a Firestore [DocumentSnapshot] into a [ToDo] object.
   *
   * @param doc The Firestore document representing a [ToDo].
   * @return The constructed [ToDo], or `null` if required fields are missing.
   */
  private fun snapshotToToDo(doc: DocumentSnapshot): ToDo? {
    val uid = doc.getString("uid") ?: return null
    val name = doc.getString("name") ?: return null
    val description = doc.getString("description") ?: return null
    val dueDate = doc.getTimestamp("dueDate")
    val dueTime = doc.getTimestamp("dueTime")
    val locationMap = doc.get("location") as? Map<*, *>
    val location =
        locationMap?.let { locMap ->
          val lat = locMap["latitude"] as? Double
          val lng = locMap["longitude"] as? Double
          val locName = locMap["name"] as? String
          if (lat != null && lng != null && locName != null) {
            Location(lat, lng, locName)
          } else {
            null
          }
        }
    val ownerId = doc.getString("ownerId") ?: return null
    val statusStr = doc.getString("status") ?: ToDoStatus.ONGOING.name
    val status = ToDoStatus.valueOf(statusStr)
    val priorityStr = doc.getString("priority") ?: return null
    val priority = ToDoPriority.valueOf(priorityStr)
    val categoryMap = doc["tag"] as? Map<*, *>
    val tag =
        categoryMap?.let { catMap ->
          val categoryId = catMap["id"] as? String
          val categoryName = catMap["name"] as? String
          val categoryColorInt = catMap["color"] as? Long
          val categoryColor = categoryColorInt?.let { Color(it) }
          val categoryIsDefault = catMap["isDefault"] as? Boolean
          val categoryIsDeleted = catMap["isDeleted"] as? Boolean
          if (categoryId != null &&
              categoryName != null &&
              categoryColor != null &&
              categoryIsDeleted != null &&
              categoryIsDefault != null) {
            ToDoCategory(
                id = categoryId,
                name = categoryName,
                color = categoryColor,
                isDefault = categoryIsDefault,
                isDeleted = categoryIsDeleted,
                ownerId = ownerId)
          } else {
            null
          }
        }

    return ToDo(uid, name, description, dueDate, dueTime, location, status, ownerId, priority, tag)
  }

  /**
   * Converts a [ToDo] into a Firestore-compatible map.
   *
   * Used by [addTodo] and [editTodo] when writing data to Firestore.
   *
   * @param todo The [ToDo] to serialize.
   * @return A map of field names to values compatible with Firestore.
   */
  private fun todoToMap(todo: ToDo): Map<String, Any?> {
    return mapOf(
        "uid" to todo.uid,
        "name" to todo.name,
        "description" to todo.description,
        "dueDate" to todo.dueDate,
        "dueTime" to todo.dueTime,
        "location" to
            todo.location?.let { loc ->
              mapOf("latitude" to loc.latitude, "longitude" to loc.longitude, "name" to loc.name)
            },
        "status" to todo.status.name,
        "ownerId" to todo.ownerId,
        "priority" to todo.priorityLevel.name,
        "tag" to
            todo.tag?.let { category ->
              mapOf(
                  "id" to category.id,
                  "name" to category.name,
                  "color" to category.color.toArgb(),
                  "isDefault" to category.isDefault,
                  "isDeleted" to category.isDeleted,
                  "ownerId" to category.ownerId)
            })
  }
}
