package com.android.gatherly.model.todo

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.util.NoSuchElementException
import kotlinx.coroutines.tasks.await


class ToDosRepositoryFirestore(private val db: FirebaseFirestore) : ToDosRepository {

    private val collection = db.collection("todos")

    private fun currentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("No signed in user")
    }

    override fun getNewUid(): String {
        // Important: document() doesn’t contact Firestore yet. It just creates a reference.
        return collection.document().id
    }

    override suspend fun getAllTodos(): List<ToDo> {
        val snap =
            collection
                .whereEqualTo("ownerId", currentUserId())
                .get()
                .await() // QuerySnapShot in this case pretty much list of docs.
        return snap.documents.mapNotNull { doc -> snapshotToToDo(doc) }
    }

    override suspend fun getTodo(todoID: String): ToDo {
        val doc = collection.document(todoID).get().await()
        val todo = snapshotToToDo(doc) ?: throw NoSuchElementException("Todo with id=$todoID not found")
        if (todo.ownerId != currentUserId()) {
            throw SecurityException("Todo doesn't belong to signed in user.")
        }
        return todo
    }

    override suspend fun addTodo(toDo: ToDo) {
        val ownedToDo = toDo.copy(ownerId = currentUserId())
        collection.document(ownedToDo.uid).set(todoToMap(ownedToDo)).await() // set takes a Map
    }

    override suspend fun editTodo(todoID: String, newValue: ToDo) {
        val doc = collection.document(todoID).get().await()
        val existing =
            snapshotToToDo(doc) ?: throw NoSuchElementException("Todo with id=$todoID not found")

        if (existing.ownerId != currentUserId()) {
            throw SecurityException("Todo does not belong to signed in user")
        }

        val newValue = newValue.copy(ownerId = currentUserId())
        collection.document(todoID).set(todoToMap(newValue)).await()
    }

    override suspend fun deleteTodo(todoID: String) {
        val doc = collection.document(todoID).get().await()
        val existing =
            snapshotToToDo(doc) ?: throw NoSuchElementException("Todo with id=$todoID not found")

        if (existing.ownerId != currentUserId()) {
            throw SecurityException("Todo does not belong to signed in user")
        }

        collection.document(todoID).delete().await()
    }

    private fun snapshotToToDo(doc: DocumentSnapshot): ToDo? {
        val uid = doc.getString("uid") ?: return null
        val name = doc.getString("name") ?: return null
        val description = doc.getString("description") ?: return null
        val assigneeName = doc.getString("assigneeName") ?: return null
        val dueDate = doc.getTimestamp("dueDate") ?: return null
        val ownerId = doc.getString("ownerId") ?: return null
        val statusStr = doc.getString("status") ?: ToDoStatus.CREATED.name
        val status = ToDoStatus.valueOf(statusStr)

        return ToDo(uid, name, description, assigneeName, dueDate, status, ownerId)
    }

    private fun todoToMap(todo: ToDo): Map<String, Any?> {
        return mapOf(
            "uid" to todo.uid,
            "name" to todo.name,
            "description" to todo.description,
            "assigneeName" to todo.assigneeName,
            "dueDate" to todo.dueDate,
            "status" to todo.status.name,
            "ownerId" to todo.ownerId)
    }
}
