package com.android.gatherly.model.todoCategory

import androidx.compose.ui.graphics.Color

/** Shared test data for ToDoCategory repository tests. */
object ToDoCategoryLocalRepositoryTestData {

  val customCategory =
      ToDoCategory(
          id = "test_category_1",
          name = "Test Category",
          color = Color.Red,
          ownerId = "test_user",
          isDefault = false,
          isDeleted = false)
}
