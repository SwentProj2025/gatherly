package com.android.gatherly.model.todoCategory

import androidx.compose.ui.graphics.Color
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.ui.theme.theme_todo_tag_courses
import com.android.gatherly.ui.theme.theme_todo_tag_default
import com.android.gatherly.ui.theme.theme_todo_tag_homework
import com.android.gatherly.ui.theme.theme_todo_tag_personal
import com.android.gatherly.ui.theme.theme_todo_tag_project

/** Represents the information needed to create a category of a [ToDo] item */
data class ToDoCategory(
    val id: String = "",
    val name: String = "",
    val color: Color = theme_todo_tag_default,
    val ownerId: String = "",
    val isDefault: Boolean = false,
    val isDeleted: Boolean = false,
)

/** Permanent categories proposed to the user */
val TAG_PERSONAL: ToDoCategory =
    ToDoCategory(
        id = "default_personal",
        name = "Personal",
        color = theme_todo_tag_personal,
        isDefault = true)

val TAG_PROJECT: ToDoCategory =
    ToDoCategory(
        id = "default_project", name = "Project", color = theme_todo_tag_project, isDefault = true)

val TAG_HOMEWORK: ToDoCategory =
    ToDoCategory(
        id = "default_homework",
        name = "Homework",
        color = theme_todo_tag_homework,
        isDefault = true)

val TAG_COURSES: ToDoCategory =
    ToDoCategory(
        id = "default_courses", name = "Courses", color = theme_todo_tag_courses, isDefault = true)

val DEFAULT_CATEGORIES = listOf(TAG_PERSONAL, TAG_PROJECT, TAG_HOMEWORK, TAG_COURSES)
