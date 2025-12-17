package com.android.gatherly.model.todoCategory

import androidx.compose.ui.graphics.Color
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.ui.theme.theme_todo_tag_courses
import com.android.gatherly.ui.theme.theme_todo_tag_default
import com.android.gatherly.ui.theme.theme_todo_tag_homework
import com.android.gatherly.ui.theme.theme_todo_tag_personal
import com.android.gatherly.ui.theme.theme_todo_tag_project

/**
 * Represents a category assigned to a [ToDo] item.
 *
 * Categories are used to group todos and define their visual representation through an associated
 * color.
 *
 * @param id Unique identifier of the category.
 * @param name Display name of the category.
 * @param color Color associated with the category.
 * @param ownerId Identifier of the user who owns this category.
 * @param isDefault Whether this category is a system-provided default.
 * @param isDeleted Whether this category has been soft-deleted.
 */
data class ToDoCategory(
    val id: String = "",
    val name: String = "",
    val color: Color = theme_todo_tag_default,
    val ownerId: String = "",
    val isDefault: Boolean = false,
    val isDeleted: Boolean = false,
)

/**
 * Default "Personal" category.
 *
 * System-provided category intended for personal tasks.
 */
val TAG_PERSONAL: ToDoCategory =
    ToDoCategory(
        id = "default_personal",
        name = "Personal",
        color = theme_todo_tag_personal,
        isDefault = true)

/**
 * Default "Project" category.
 *
 * System-provided category intended for project-related tasks.
 */
val TAG_PROJECT: ToDoCategory =
    ToDoCategory(
        id = "default_project", name = "Project", color = theme_todo_tag_project, isDefault = true)

/**
 * Default "Homework" category.
 *
 * System-provided category intended for homework-related tasks.
 */
val TAG_HOMEWORK: ToDoCategory =
    ToDoCategory(
        id = "default_homework",
        name = "Homework",
        color = theme_todo_tag_homework,
        isDefault = true)

/**
 * Default "Courses" category.
 *
 * System-provided category intended for course-related tasks.
 */
val TAG_COURSES: ToDoCategory =
    ToDoCategory(
        id = "default_courses", name = "Courses", color = theme_todo_tag_courses, isDefault = true)

/**
 * List of all system-provided default todo categories.
 *
 * These categories are always available to the user and cannot be removed.
 */
val DEFAULT_CATEGORIES = listOf(TAG_PERSONAL, TAG_PROJECT, TAG_HOMEWORK, TAG_COURSES)
