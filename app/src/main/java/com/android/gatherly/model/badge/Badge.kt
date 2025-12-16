package com.android.gatherly.model.badge

import androidx.annotation.DrawableRes
import com.android.gatherly.R

/**
 * Represents a single [Badge] item within the app.
 *
 * @param id The identifier of the badge
 * @param title The displayable title of the badge
 * @param description The description to display with badges obtained
 * @param rank The rank of the badge according to the number of times they did an action
 * @param type The action the user has to do in order to get this badge
 */
enum class Badge(
    val id: String,
    val title: String,
    val description: String,
    val rank: BadgeRank,
    val type: BadgeType,
    @DrawableRes val iconRes: Int
) {
  // ---------------------- ToDo Created Badge -----------------------
  STARTING_TODOS_CREATED_BADGE(
      "starting_TodosCreated",
      "Starting ToDo Created Badge",
      "You created 1 ToDo!",
      BadgeRank.STARTING,
      BadgeType.TODOS_CREATED,
      R.drawable.starting_todo_created),
  BRONZE_TODOS_CREATED_BADGE(
      "bronze_TodosCreated",
      "Bronze ToDo Created Badge",
      "You created 3 ToDos!",
      BadgeRank.BRONZE,
      BadgeType.TODOS_CREATED,
      R.drawable.bronze_todo_created),
  SILVER_TODOS_CREATED_BADGE(
      "silver_TodosCreated",
      "Silver ToDo Created Badge",
      "You created 5 ToDos!",
      BadgeRank.SILVER,
      BadgeType.TODOS_CREATED,
      R.drawable.silver_todo_created),
  GOLD_TODOS_CREATED_BADGE(
      "gold_TodosCreated",
      "Gold ToDo Created Badge",
      "You created 10 ToDos!",
      BadgeRank.GOLD,
      BadgeType.TODOS_CREATED,
      R.drawable.gold_todo_created),
  DIAMOND_TODOS_CREATED_BADGE(
      "diamond_TodosCreated",
      "Diamond ToDo Created Badge",
      "You created 20 ToDos!",
      BadgeRank.DIAMOND,
      BadgeType.TODOS_CREATED,
      R.drawable.diamond_todo_created),
  LEGEND_TODOS_CREATED_BADGE(
      "legend_TodosCreated",
      "Legend ToDo Created Badge",
      "You created 30 ToDos!",
      BadgeRank.LEGEND,
      BadgeType.TODOS_CREATED,
      R.drawable.legend_todo_created),
  // ---------------------- ToDo Completed Badge -----------------------
  STARTING_TODOS_COMPLETED_BADGE(
      "starting_TodosCompleted",
      "Starting ToDo Completed Badge",
      "You completed 1 ToDo!",
      BadgeRank.STARTING,
      BadgeType.TODOS_COMPLETED,
      R.drawable.starting_todo_completed),
  BRONZE_TODOS_COMPLETED_BADGE(
      "bronze_TodosCompleted",
      "Bronze ToDo Completed Badge",
      "You completed 3 ToDos!",
      BadgeRank.BRONZE,
      BadgeType.TODOS_COMPLETED,
      R.drawable.bronze_todo_completed),
  SILVER_TODOS_COMPLETED_BADGE(
      "silver_TodosCompleted",
      "Silver ToDo Completed Badge",
      "You completed 5 ToDos!",
      BadgeRank.SILVER,
      BadgeType.TODOS_COMPLETED,
      R.drawable.silver_todo_completed),
  GOLD_TODOS_COMPLETED_BADGE(
      "gold_TodosCompleted",
      "Gold ToDo Completed Badge",
      "You completed 10 ToDos!",
      BadgeRank.GOLD,
      BadgeType.TODOS_COMPLETED,
      R.drawable.gold_todo_completed),
  DIAMOND_TODOS_COMPLETED_BADGE(
      "diamond_TodosCompleted",
      "Diamond ToDo Completed Badge",
      "You completed 20 ToDos!",
      BadgeRank.DIAMOND,
      BadgeType.TODOS_COMPLETED,
      R.drawable.diamond_todo_completed),
  LEGEND_TODOS_COMPLETED_BADGE(
      "legend_TodosCompleted",
      "Legend ToDo Completed Badge",
      "You completed 30 ToDos!",
      BadgeRank.LEGEND,
      BadgeType.TODOS_COMPLETED,
      R.drawable.legend_todo_completed),
  // ---------------------- Event Created Badge -----------------------
  STARTING_EVENTS_CREATED_BADGE(
      "starting_EventsCreated",
      "Starting Event Created Badge",
      "You created 1 Event!",
      BadgeRank.STARTING,
      BadgeType.EVENTS_CREATED,
      R.drawable.starting_event_created),
  BRONZE_EVENTS_CREATED_BADGE(
      "bronze_EventsCreated",
      "Bronze Event Created Badge",
      "You created 3 Events!",
      BadgeRank.BRONZE,
      BadgeType.EVENTS_CREATED,
      R.drawable.bronze_event_created),
  SILVER_EVENTS_CREATED_BADGE(
      "silver_EventsCreated",
      "Silver Event Created Badge",
      "You created 5 Events!",
      BadgeRank.SILVER,
      BadgeType.EVENTS_CREATED,
      R.drawable.silver_event_created),
  GOLD_EVENTS_CREATED_BADGE(
      "gold_EventsCreated",
      "Gold Event Created Badge",
      "You created 10 Events!",
      BadgeRank.GOLD,
      BadgeType.EVENTS_CREATED,
      R.drawable.gold_event_created),
  DIAMOND_EVENTS_CREATED_BADGE(
      "diamond_EventsCreated",
      "Diamond Event Created Badge",
      "You created 20 Events!",
      BadgeRank.DIAMOND,
      BadgeType.EVENTS_CREATED,
      R.drawable.diamond_event_created),
  LEGEND_EVENTS_CREATED_BADGE(
      "legend_EventsCreated",
      "Legend Event Created Badge",
      "You created 30 Events!",
      BadgeRank.LEGEND,
      BadgeType.EVENTS_CREATED,
      R.drawable.legend_event_created),
  // ---------------------- Event Participated Badge -----------------------
  STARTING_EVENTS_PARTICIPATED_BADGE(
      "starting_EventsParticipated",
      "Starting Event Participated Badge",
      "You participated in 1 Event!",
      BadgeRank.STARTING,
      BadgeType.EVENTS_PARTICIPATED,
      R.drawable.starting_event_participated),
  BRONZE_EVENTS_PARTICIPATED_BADGE(
      "bronze_EventsParticipated",
      "Bronze Event Participated Badge",
      "You participated in 3 Events!",
      BadgeRank.BRONZE,
      BadgeType.EVENTS_PARTICIPATED,
      R.drawable.bronze_event_participated),
  SILVER_EVENTS_PARTICIPATED_BADGE(
      "silver_EventsParticipated",
      "Silver Event Participated Badge",
      "You participated in 5 Events!",
      BadgeRank.SILVER,
      BadgeType.EVENTS_PARTICIPATED,
      R.drawable.silver_event_participated),
  GOLD_EVENTS_PARTICIPATED_BADGE(
      "gold_EventsParticipated",
      "Gold Event Participated Badge",
      "You participated in 10 Events!",
      BadgeRank.GOLD,
      BadgeType.EVENTS_PARTICIPATED,
      R.drawable.gold_event_participated),
  DIAMOND_EVENTS_PARTICIPATED_BADGE(
      "diamond_EventsParticipated",
      "Diamond Event Participated Badge",
      "You participated in 20 Events!",
      BadgeRank.DIAMOND,
      BadgeType.EVENTS_PARTICIPATED,
      R.drawable.diamond_event_participated),
  LEGEND_EVENTS_PARTICIPATED_BADGE(
      "legend_EventsParticipated",
      "Legend Event Participated Badge",
      "You participated in 30 Events!",
      BadgeRank.LEGEND,
      BadgeType.EVENTS_PARTICIPATED,
      R.drawable.legend_event_participated),
  // ---------------------- Friend Badge -----------------------
  STARTING_FRIENDS_BADGE(
      "starting_Friends",
      "Starting Friends Badge",
      "You added 1 Friend!",
      BadgeRank.STARTING,
      BadgeType.FRIENDS_ADDED,
      R.drawable.starting_friends),
  BRONZE_FRIENDS_BADGE(
      "bronze_Friends",
      "Bronze Friends Badge",
      "You added 3 Friends!",
      BadgeRank.BRONZE,
      BadgeType.FRIENDS_ADDED,
      R.drawable.bronze_friends),
  SILVER_FRIENDS_BADGE(
      "silver_Friends",
      "Silver Friends Badge",
      "You added 5 Friends!",
      BadgeRank.SILVER,
      BadgeType.FRIENDS_ADDED,
      R.drawable.silver_friends),
  GOLD_FRIENDS_BADGE(
      "gold_Friends",
      "Gold Friends Badge",
      "You added 10 Friends!",
      BadgeRank.GOLD,
      BadgeType.FRIENDS_ADDED,
      R.drawable.gold_friends),
  DIAMOND_FRIENDS_BADGE(
      "diamond_Friends",
      "Diamond Friends Badge",
      "You added 20 Friends!",
      BadgeRank.DIAMOND,
      BadgeType.FRIENDS_ADDED,
      R.drawable.diamond_friends),
  LEGEND_FRIENDS_BADGE(
      "legend_Friends",
      "Legend Friends Badge",
      "You added 30 Friends!",
      BadgeRank.LEGEND,
      BadgeType.FRIENDS_ADDED,
      R.drawable.legend_friends),
  // ---------------------- Focus Session Badge -----------------------
  STARTING_FOCUS_SESSION_BADGE(
      "starting_Focus_Session",
      "Starting Focus Session Badge",
      "You completed 1 Focus Session!",
      BadgeRank.STARTING,
      BadgeType.FOCUS_SESSIONS_COMPLETED,
      R.drawable.starting_focus_session),
  BRONZE_FOCUS_SESSION_BADGE(
      "bronze_Focus_Session",
      "Bronze Focus Session Badge",
      "You completed 3 Focus Sessions!",
      BadgeRank.BRONZE,
      BadgeType.FOCUS_SESSIONS_COMPLETED,
      R.drawable.bronze_focus_session),
  SILVER_FOCUS_SESSION_BADGE(
      "silver_Focus_Session",
      "Silver Focus Session Badge",
      "You completed 5 Focus Sessions!",
      BadgeRank.SILVER,
      BadgeType.FOCUS_SESSIONS_COMPLETED,
      R.drawable.silver_focus_session),
  GOLD_FOCUS_SESSION_BADGE(
      "gold_Focus_Session",
      "Gold Focus Session Badge",
      "You completed 10 Focus Sessions!",
      BadgeRank.GOLD,
      BadgeType.FOCUS_SESSIONS_COMPLETED,
      R.drawable.gold_focus_session),
  DIAMOND_FOCUS_SESSION_BADGE(
      "diamond_Focus_Session",
      "Diamond Focus Session Badge",
      "You completed 20 Focus Sessions!",
      BadgeRank.DIAMOND,
      BadgeType.FOCUS_SESSIONS_COMPLETED,
      R.drawable.diamond_focus_session),
  LEGEND_FOCUS_SESSION_BADGE(
      "legend_Focus_Session",
      "Legend Focus Session Badge",
      "You completed 30 Focus Sessions!",
      BadgeRank.LEGEND,
      BadgeType.FOCUS_SESSIONS_COMPLETED,
      R.drawable.legend_focus_session),
}

/**
 * Represents the rank of a [Badge] item within the app.
 *
 * Blank : the user did not yet made action with his profile Starting : the user did the action once
 * Bronze : the user did the action 3 times Silver : the user did the action 5 times Gold : the user
 * did the action 10 times Diamond : the user did the action 20 times Legend : the user did the
 * action more than 20 times
 */
enum class BadgeRank(val pointsEarned: Double) {
  BLANK(pointsEarned = 0.0),
  STARTING(pointsEarned = 10.0),
  BRONZE(pointsEarned = 30.0),
  SILVER(pointsEarned = 50.0),
  GOLD(pointsEarned = 100.0),
  DIAMOND(pointsEarned = 200.0),
  LEGEND(pointsEarned = 300.0)
}

/**
 * Represents the type of a [Badge] item within the app.
 *
 * Todos Created : regarding the number of todos created by the user Todos Completed : regarding the
 * number of todos completed by the user Events Created : regarding the number of events created by
 * the user Events Participated : regarding the number of events the user participated in Friends
 * Added : regarding the number of friends added by the user Focus Sessions Completed : regarding
 * the number of focus sessions completed by the user
 */
enum class BadgeType {
  TODOS_CREATED,
  TODOS_COMPLETED,
  EVENTS_CREATED,
  EVENTS_PARTICIPATED,
  FRIENDS_ADDED,
  FOCUS_SESSIONS_COMPLETED
}
