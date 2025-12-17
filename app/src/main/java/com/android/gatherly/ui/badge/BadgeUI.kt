package com.android.gatherly.ui.badge

import androidx.annotation.DrawableRes
import com.android.gatherly.R

/**
 * UI model for a badge displayed in the Badge screen.
 *
 * @param title The title of the badge.
 * @param description The description of the badge.
 * @param icon The drawable resource ID for the badge icon.
 */
data class BadgeUI(
    val title: String = "",
    val description: String = "",
    @DrawableRes val icon: Int = 0,
)

/** Badge for creating the first Todo. */
val TODOS_CREATED_BADGE =
    BadgeUI(
        "Blank Todo Created Badge",
        "Create your first Todo to get a Badge!",
        R.drawable.blank_todo_created)

/** Badge for completing the first Todo. */
val TODOS_COMPLETED_BADGE =
    BadgeUI(
        "Blank Todo Completed Badge",
        "Complete your first Todo to get a Badge!",
        R.drawable.blank_todo_completed)

/** Badge for creating the first Event. */
val EVENTS_CREATED_BADGE =
    BadgeUI(
        "Blank Event Created Badge",
        "Create your first Event to get a Badge!",
        R.drawable.blank_event_created)

/** Badge for participating in the first Event. */
val EVENTS_PARTICIPATED_BADGE =
    BadgeUI(
        "Blank Event Participated Badge",
        "Participate to your first Todo to get a Badge!",
        R.drawable.blank_event_participated)

/** Badge for adding the first Friend. */
val FRIENDS_ADDED_BADGE =
    BadgeUI("Blank Friend Badge", "Add your first Friend to get a Badge!", R.drawable.blank_friends)

/** Badge for completing the first Focus Session. */
val FOCUS_SESSIONS_COMPLETED_BADGE =
    BadgeUI(
        "Blank Focus Session Badge",
        "Complete your first Focus Session to get a Badge!",
        R.drawable.blank_focus_session)
