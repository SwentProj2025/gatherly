package com.android.gatherly.utils

import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todo.ToDosRepository

// Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the
// SwEnt staff.

/**
 * Superclass for all local tests, which sets up a local repository before each test and restores
 * the original repository after each test.
 */
open class InMemoryGatherlyTest() : GatherlyTest() {
  override fun createInitializedRepository(): ToDosRepository {
    return InMemoryToDosRepository()
  }

  class InMemoryToDosRepository(val todoList: MutableList<ToDo> = mutableListOf<ToDo>()) :
      ToDosRepository {
    override suspend fun addTodo(toDo: ToDo) {
      todoList.add(toDo)
    }

    override suspend fun editTodo(todoID: String, newValue: ToDo) {
      todoList.replaceAll { if (it.uid == todoID) newValue else it }
    }

    override suspend fun deleteTodo(todoID: String) {
      todoList.removeIf { it.uid == todoID }
    }

    override fun getNewUid(): String {
      return "${todoList.size}"
    }

    override suspend fun getAllTodos(): List<ToDo> {
      return todoList
    }

    override suspend fun getTodo(todoID: String): ToDo {
      return todoList.first<ToDo> { it.uid == todoID }
    }

    override suspend fun toggleStatus(todoID: String) {
      todoList.replaceAll {
        if (it.uid == todoID) {
          it.copy(
              status = if (it.status == ToDoStatus.ENDED) ToDoStatus.ONGOING else ToDoStatus.ENDED)
        } else it
      }
    }

    override suspend fun getAllEndedTodos(): List<ToDo> {
      return todoList.filter { it.status == ToDoStatus.ENDED }
    }
  }
}
