package com.android.gatherly.model.todo

import kotlin.String

/**
 * In-memory implementation of [ToDosRepository].
 *
 * This repository is used for unit tests and local execution, without any persistence layer.
 *
 * @param limitToUser If non-null, restricts all operations to todos owned by the specified user.
 */
class ToDosLocalRepository(private val limitToUser: String? = null) : ToDosRepository {

  /** In-memory storage for todos. */
  private val todos: MutableList<ToDo> = mutableListOf()

  /** ID counter for generating unique todo IDs. */
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

  override suspend fun getTodo(todoId: String): ToDo {
    return todos.find { it.uid == todoId }
        ?: throw Exception("ToDosRepositoryLocal: ToDo not found")
  }

  override suspend fun addTodo(toDo: ToDo) {
    todos.add(toDo)
  }

  override suspend fun editTodo(todoId: String, newValue: ToDo) {
    val index = todos.indexOfFirst { it.uid == todoId }
    if (index != -1) {
      todos[index] = newValue
    } else {
      throw Exception("ToDosRepositoryLocal: ToDo not found")
    }
  }

  override suspend fun deleteTodo(todoId: String) {
    val index = todos.indexOfFirst { it.uid == todoId }
    if (index != -1) {
      todos.removeAt(index)
    } else {
      throw Exception("ToDosRepositoryLocal: ToDo not found")
    }
  }

  override suspend fun toggleStatus(todoId: String) {}

  override suspend fun getAllEndedTodos(): List<ToDo> {
    return emptyList()
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
