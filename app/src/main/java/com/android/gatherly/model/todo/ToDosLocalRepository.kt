package com.android.gatherly.model.todo

import kotlin.String

/** Represents a repository that manages a local list of todos. */
class ToDosLocalRepository : ToDosRepository {

  private val todos: MutableList<ToDo> = mutableListOf()

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

  override suspend fun updateTodosTagToNull(categoryId: String, ownerId: String) {
    val todosToUpdate =
        todos.filter {
          val tag = it.tag
          tag != null && tag.id == categoryId
        }
    todosToUpdate.forEach { todo ->
      val index = todos.indexOf(todo)
      if (index != -1) {
        todos[index] = todo.copy(tag = null)
      }
    }
  }
}
