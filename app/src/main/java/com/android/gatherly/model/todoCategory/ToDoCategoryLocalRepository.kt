package com.android.gatherly.model.todoCategory

class ToDoCategoryLocalRepository : ToDoCategoryRepository {
  private val categories: MutableList<ToDoCategory> = mutableListOf()
  private var counter = 0

  override fun getNewId(): String {
    return (counter++).toString()
  }

  override suspend fun initializeDefaultCategories() {
    for (defaultCategory in DEFAULT_CATEGORIES) {
      categories.add(defaultCategory)
    }
  }

  override suspend fun getAllCategories(): List<ToDoCategory> {
    return categories
  }

  override suspend fun addToDoCategory(category: ToDoCategory) {
    categories.add(category)
  }

  override suspend fun deleteToDoCategory(categoryId: String) {
    val index = categories.indexOfFirst { it.id == categoryId }
    if (index != -1) {
      categories.removeAt(index)
    } else {
      throw Exception("ToDoCategoryLocalRepository: ToDoCategory not found")
    }
  }
}
