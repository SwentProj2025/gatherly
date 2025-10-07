package com.android.gatherly.model.todo

// This code was taken from the CS-311 (EPFL) bootcamp.

/** Represents a repository that manages a local list of todos. */
class BootcampToDosRepositoryLocal : BootcampToDosRepository {
  private var todos = mutableListOf<BootcampToDo>()
  private var counter = 0

  override fun getNewUid(): String {
    return (counter++).toString()
  }

  override suspend fun getAllTodos(): List<BootcampToDo> {
    return todos.toList() // copy to prevent external modification
  }

  override suspend fun getTodo(todoID: String): BootcampToDo {
    for (currTodo in todos) if (currTodo.uid == todoID) return currTodo
    throw Exception("Todo with ID $todoID not found")
  }

  override suspend fun addTodo(toDo: BootcampToDo) {
    todos.add(toDo)
    return
  }

  override suspend fun editTodo(todoID: String, newValue: BootcampToDo) {
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
}
