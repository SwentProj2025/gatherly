package com.android.gatherly.viewmodel

import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todo.ToDosRepository

// This code was taken from the CS-311 (EPFL) bootcamp and modified.

/** Represents a repository that manages a local list of todos. */
class FakeToDosRepositoryLocal : ToDosRepository {
  private var todos = mutableListOf<ToDo>()
  private var counter = 0

  override fun getNewUid(): String {
    return (counter++).toString()
  }

  override suspend fun getAllTodos(): List<ToDo> {
    return todos.toList() // copy to prevent external modification
  }

  override suspend fun getTodo(todoID: String): ToDo {
    for (currTodo in todos) if (currTodo.uid == todoID) return currTodo
    throw Exception("Todo with ID $todoID not found")
  }

  override suspend fun addTodo(toDo: ToDo) {
    todos.add(toDo)
    return
  }

  override suspend fun editTodo(todoID: String, newValue: ToDo) {
    for (currTodo in todos) if (currTodo.uid == todoID) {
      todos.remove(currTodo)
      todos.add(newValue)
      return
    }
    throw Exception("Todo with ID $todoID not found")
  }

  override suspend fun deleteTodo(todoID: String) {
    for (currTodo in todos) if (currTodo.uid == todoID) {
      todos.remove(currTodo)
      return
    }
    throw Exception("Todo with ID $todoID not found")
  }

  override suspend fun toggleStatus(todoID: String) {
    val index = todos.indexOfFirst { it.uid == todoID }
    if (index != -1) {
      val todo = todos[index]
      val newStatus =
          if (todo.status == ToDoStatus.ONGOING) {
            ToDoStatus.ENDED
          } else {
            ToDoStatus.ONGOING
          }
      todos[index] = todo.copy(status = newStatus)
    }
  }

  override suspend fun getAllEndedTodos(): List<ToDo> {
    return todos.filter { it.status == ToDoStatus.ENDED }
  }
}
