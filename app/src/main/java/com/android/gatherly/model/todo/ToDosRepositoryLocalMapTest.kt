package com.android.gatherly.model.todo

import com.android.gatherly.model.map.Location
import com.google.firebase.Timestamp
import kotlin.String


/** Represents a repository that manages a local list of todos. */
class ToDosRepositoryLocalMapTest : ToDosRepository {

    private val todos = mutableListOf(
            ToDo(
                uid = getNewUid(),
                name = "Buy groceries",
                description = "Milk, Bread, Eggs, Butter",
                assigneeName = "Alice",
                dueDate = Timestamp.now(),
                dueTime = null,
                location = Location(46.5238, 6.5627, "Bassenges"),
                status = ToDoStatus.ONGOING,
                ownerId = "user1"
            ),
            ToDo(
                uid = getNewUid(),
                name = "Finish swent",
                description = "FInish map ui",
                assigneeName = "Colombe",
                dueDate = Timestamp.now(),
                dueTime = null,
                location = Location(46.5197, 6.5663, "EPFL"),
                status = ToDoStatus.ONGOING,
                ownerId = "user1"
            ))

    private var counter = 0

    override fun getNewUid(): String {
        return (counter++).toString()
    }

    override suspend fun getAllTodos(): List<ToDo> {
        return todos
    }

    override suspend fun getTodo(todoID: String): ToDo {
        return todos.find { it.uid == todoID }
            ?: throw Exception("ToDosRepositoryLocal: ToDo not found")
    }

    override suspend fun addTodo(toDo: ToDo) {
        todos.add(toDo)
    }

    override suspend fun editTodo(todoID: String, newValue: ToDo) {
        val index = todos.indexOfFirst { it.uid == todoID }
        if (index != -1) {
            todos[index] = newValue
        } else {
            throw Exception("ToDosRepositoryLocal: ToDo not found")
        }
    }

    override suspend fun deleteTodo(todoID: String) {
        val index = todos.indexOfFirst { it.uid == todoID }
        if (index != -1) {
            todos.removeAt(index)
        } else {
            throw Exception("ToDosRepositoryLocal: ToDo not found")
        }
    }

    override suspend fun toggleStatus(todoID: String) {
        TODO("Not yet implemented")
    }

    override suspend fun getAllEndedTodos(): List<ToDo> {
        TODO("Not yet implemented")
    }

}


