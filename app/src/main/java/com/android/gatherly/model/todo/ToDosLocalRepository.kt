package com.android.gatherly.model.todo

import kotlin.String

/** Represents a repository that manages a local list of todos. */
class ToDosLocalRepository(private val limitToUser: String? = null) : ToDosRepository {

  private val todos: MutableList<ToDo> = mutableListOf()

  private var counter = 0

  override fun getNewUid(): String {
    return (counter++).toString()
  }

  override suspend fun getAllTodos(): List<ToDo> {
    return if (limitToUser == null) {
      todos
    } else {
      todos.filter { it.ownerId == limitToUser }
    }
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
