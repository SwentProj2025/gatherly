package com.android.gatherly.model.badge

/** Represents a single [Badge] item within the app. */
enum class Badge(
    val id: String,
    val title: String,
    val description: String,
    val rank: BadgeRank,
    val type: BadgeType,
    val icon: String
) {
    //---------------------- ToDo Created Badge -----------------------
  STARTING_TODOS_CREATED_BADGE(
      "starting_TodosCreated",
      "Starting ToDo Created Badge",
      "You created 1 ToDo!",
      BadgeRank.STARTING,
      BadgeType.TODOS_CREATED,
      "app/src/main/res/drawable/badges/todos/Starting Todo Created.png"),
  BRONZE_TODOS_CREATED_BADGE(
      "bronze_TodosCreated",
      "Bronze ToDo Created Badge",
      "You created 3 ToDos!",
      BadgeRank.BRONZE,
      BadgeType.TODOS_CREATED,
      "app/src/main/res/drawable/badges/todos/Bronze Todo Created.png"),
  SILVER_TODOS_CREATED_BADGE(
      "silver_TodosCreated",
      "Silver ToDo Created Badge",
      "You created 5 ToDos!",
      BadgeRank.SILVER,
      BadgeType.TODOS_CREATED,
      "app/src/main/res/drawable/badges/todos/Silver Todo Created.png"),
  GOLD_TODOS_CREATED_BADGE(
      "gold_TodosCreated",
      "Gold ToDo Created Badge",
      "You created 10 ToDos!",
      BadgeRank.GOLD,
      BadgeType.TODOS_CREATED,
      "app/src/main/res/drawable/badges/todos/Gold Todo Created.png"),
  DIAMOND_TODOS_CREATED_BADGE(
      "diamond_TodosCreated",
      "Diamond ToDo Created Badge",
      "You created 20 ToDos!",
      BadgeRank.DIAMOND,
      BadgeType.TODOS_CREATED,
      "app/src/main/res/drawable/badges/todos/Diamond Todo Created.png"),
    LEGEND_TODOS_CREATED_BADGE(
        "legend_TodosCreated",
        "Legend ToDo Created Badge",
        "You created 30 ToDos!",
        BadgeRank.LEGEND,
        BadgeType.TODOS_CREATED,
        "app/src/main/res/drawable/badges/todos/Legend Todo Created.png"),
    //---------------------- ToDo Completed Badge -----------------------
    STARTING_TODOS_COMPLETED_BADGE(
        "starting_TodosCompleted",
        "Starting ToDo Completed Badge",
        "You completed 1 ToDo!",
        BadgeRank.STARTING,
        BadgeType.TODOS_COMPLETED,
        "app/src/main/res/drawable/badges/todos/Starting Todo Completed.png"),
    BRONZE_TODOS_COMPLETED_BADGE(
        "bronze_TodosCompleted",
        "Bronze ToDo Completed Badge",
        "You completed 3 ToDos!",
        BadgeRank.BRONZE,
        BadgeType.TODOS_COMPLETED,
        "app/src/main/res/drawable/badges/todos/Bronze Todo Completed.png"),
    SILVER_TODOS_COMPLETED_BADGE(
        "silver_TodosCompleted",
        "Silver ToDo Completed Badge",
        "You completed 5 ToDos!",
        BadgeRank.SILVER,
        BadgeType.TODOS_COMPLETED,
        "app/src/main/res/drawable/badges/todos/Silver Todo Completed.png"),
    GOLD_TODOS_COMPLETED_BADGE(
        "gold_TodosCompleted",
        "Gold ToDo Completed Badge",
        "You completed 10 ToDos!",
        BadgeRank.GOLD,
        BadgeType.TODOS_COMPLETED,
        "app/src/main/res/drawable/badges/todos/Gold Todo Completed.png"),
    DIAMOND_TODOS_COMPLETED_BADGE(
        "diamond_TodosCompleted",
        "Diamond ToDo Completed Badge",
        "You completed 20 ToDos!",
        BadgeRank.DIAMOND,
        BadgeType.TODOS_COMPLETED,
        "app/src/main/res/drawable/badges/todos/Diamond Todo Completed.png"),
    LEGEND_TODOS_COMPLETED_BADGE(
        "legend_TodosCompleted",
        "Legend ToDo Completed Badge",
        "You completed 30 ToDos!",
        BadgeRank.LEGEND,
        BadgeType.TODOS_COMPLETED,
        "app/src/main/res/drawable/badges/todos/Legend Todo Completed.png"),
    //---------------------- Event Created Badge -----------------------
    STARTING_EVENTS_CREATED_BADGE(
        "starting_EventsCreated",
        "Starting Event Created Badge",
        "You created 1 Event!",
        BadgeRank.STARTING,
        BadgeType.EVENTS_CREATED,
        "app/src/main/res/drawable/badges/events/Starting Event Created.png"),
    BRONZE_EVENTS_CREATED_BADGE(
        "bronze_EventsCreated",
        "Bronze Event Created Badge",
        "You created 3 Events!",
        BadgeRank.BRONZE,
        BadgeType.EVENTS_CREATED,
        "app/src/main/res/drawable/badges/events/Bronze Event Created.png"),
    SILVER_EVENTS_CREATED_BADGE(
        "silver_EventsCreated",
        "Silver Event Created Badge",
        "You created 5 Events!",
        BadgeRank.SILVER,
        BadgeType.EVENTS_CREATED,
        "app/src/main/res/drawable/badges/events/Silver Event Created.png"),
    GOLD_EVENTS_CREATED_BADGE(
        "gold_EventsCreated",
        "Gold Event Created Badge",
        "You created 10 Events!",
        BadgeRank.GOLD,
        BadgeType.EVENTS_CREATED,
        "app/src/main/res/drawable/badges/events/Gold Event Created.png"),
    DIAMOND_EVENTS_CREATED_BADGE(
        "diamond_EventsCreated",
        "Diamond Event Created Badge",
        "You created 20 Events!",
        BadgeRank.DIAMOND,
        BadgeType.EVENTS_CREATED,
        "app/src/main/res/drawable/badges/events/Diamond Event Created.png"),
    LEGEND_EVENTS_CREATED_BADGE(
        "legend_EventsCreated",
        "Legend Event Created Badge",
        "You created 30 Events!",
        BadgeRank.LEGEND,
        BadgeType.EVENTS_CREATED,
        "app/src/main/res/drawable/badges/events/Legend Event Created.png"),
    //---------------------- Event Participated Badge -----------------------
    STARTING_EVENTS_PARTICIPATED_BADGE(
        "starting_EventsParticipated",
        "Starting Event Participated Badge",
        "You participated in 1 Event!",
        BadgeRank.STARTING,
        BadgeType.EVENTS_PARTICIPATED,
        "app/src/main/res/drawable/badges/events/Starting Events.png"),
    BRONZE_EVENTS_PARTICIPATED_BADGE(
        "bronze_EventsParticipated",
        "Bronze Event Participated Badge",
        "You participated in 3 Events!",
        BadgeRank.BRONZE,
        BadgeType.EVENTS_PARTICIPATED,
        "app/src/main/res/drawable/badges/events/Bronze Events.png"),
    SILVER_EVENTS_PARTICIPATED_BADGE(
        "silver_EventsParticipated",
        "Silver Event Participated Badge",
        "You participated in 5 Events!",
        BadgeRank.SILVER,
        BadgeType.EVENTS_PARTICIPATED,
        "app/src/main/res/drawable/badges/events/Silver Events.png"),
    GOLD_EVENTS_PARTICIPATED_BADGE(
        "gold_EventsParticipated",
        "Gold Event Participated Badge",
        "You participated in 10 Events!",
        BadgeRank.GOLD,
        BadgeType.EVENTS_PARTICIPATED,
        "app/src/main/res/drawable/badges/events/Gold Events.png"),
    DIAMOND_EVENTS_PARTICIPATED_BADGE(
        "diamond_EventsParticipated",
        "Diamond Event Participated Badge",
        "You participated in 20 Events!",
        BadgeRank.DIAMOND,
        BadgeType.EVENTS_PARTICIPATED,
        "app/src/main/res/drawable/badges/events/Diamond Events.png"),
    LEGEND_EVENTS_PARTICIPATED_BADGE(
        "legend_EventsParticipated",
        "Legend Event Participated Badge",
        "You participated in 30 Events!",
        BadgeRank.LEGEND,
        BadgeType.EVENTS_PARTICIPATED,
        "app/src/main/res/drawable/badges/events/Legend Events.png"),
    //---------------------- Friend Badge -----------------------
    STARTING_FRIENDS_BADGE(
        "starting_Friends",
        "Starting Friends Badge",
        "You added 1 Friend!",
        BadgeRank.STARTING,
        BadgeType.FRIENDS_ADDED,
        "app/src/main/res/drawable/badges/friends/Starting Friends.png"),
    BRONZE_FRIENDS_BADGE(
        "bronze_Friends",
        "Bronze Friends Badge",
        "You added 3 Friends!",
        BadgeRank.BRONZE,
        BadgeType.FRIENDS_ADDED,
        "app/src/main/res/drawable/badges/friends/Bronze Friends.png"),
    SILVER_FRIENDS_BADGE(
        "silver_Friends",
        "Silver Friends Badge",
        "You added 5 Friends!",
        BadgeRank.SILVER,
        BadgeType.FRIENDS_ADDED,
        "app/src/main/res/drawable/badges/friends/Silver Friends.png"),
    GOLD_FRIENDS_BADGE(
        "gold_Friends",
        "Gold Friends Badge",
        "You added 10 Friends!",
        BadgeRank.GOLD,
        BadgeType.FRIENDS_ADDED,
        "app/src/main/res/drawable/badges/friends/Gold Friends.png"),
    DIAMOND_FRIENDS_BADGE(
        "diamond_Friends",
        "Diamond Friends Badge",
        "You added 20 Friends!",
        BadgeRank.DIAMOND,
        BadgeType.FRIENDS_ADDED,
        "app/src/main/res/drawable/badges/friends/Diamond Friends.png"),
    LEGEND_FRIENDS_BADGE(
        "legend_Friends",
        "Legend Friends Badge",
        "You added 30 Friends!",
        BadgeRank.LEGEND,
        BadgeType.FRIENDS_ADDED,
        "app/src/main/res/drawable/badges/friends/Legend Friends.png"),
    //---------------------- Focus Session Badge -----------------------
    STARTING_FOCUS_SESSION_BADGE(
        "starting_Focus_Session",
        "Starting Focus Session Badge",
        "You completed 1 Focus Session!",
        BadgeRank.STARTING,
        BadgeType.FOCUS_SESSIONS_COMPLETED,
        "app/src/main/res/drawable/badges/focusSessions/Starting FocusSession.png"),
    BRONZE_FOCUS_SESSION_BADGE(
        "bronze_Focus_Session",
        "Bronze Focus Session Badge",
        "You completed 3 Focus Sessions!",
        BadgeRank.BRONZE,
        BadgeType.FOCUS_SESSIONS_COMPLETED,
        "app/src/main/res/drawable/badges/focusSessions/Bronze FocusSession.png"),
    SILVER_FOCUS_SESSION_BADGE(
        "silver_Focus_Session",
        "Silver Focus Session Badge",
        "You completed 5 Focus Sessions!",
        BadgeRank.SILVER,
        BadgeType.FOCUS_SESSIONS_COMPLETED,
        "app/src/main/res/drawable/badges/focusSessions/Silver FocusSession.png"),
    GOLD_FOCUS_SESSION_BADGE(
        "gold_Focus_Session",
        "Gold Focus Session Badge",
        "You completed 10 Focus Sessions!",
        BadgeRank.GOLD,
        BadgeType.FOCUS_SESSIONS_COMPLETED,
        "app/src/main/res/drawable/badges/focusSessions/Gold FocusSession.png"),
    DIAMOND_FOCUS_SESSION_BADGE(
        "diamond_Focus_Session",
        "Diamond Focus Session Badge",
        "You completed 20 Focus Sessions!",
        BadgeRank.DIAMOND,
        BadgeType.FOCUS_SESSIONS_COMPLETED,
        "app/src/main/res/drawable/badges/focusSessions/Diamond FocusSession.png"),
    LEGEND_FOCUS_SESSION_BADGE(
        "legend_Focus_Session",
        "Legend Focus Session Badge",
        "You completed 30 Focus Sessions!",
        BadgeRank.LEGEND,
        BadgeType.FOCUS_SESSIONS_COMPLETED,
        "app/src/main/res/drawable/badges/focusSessions/Legend FocusSession.png"),

}

/**
 * Represents the rank of a [Badge] item within the app.
 *
 * Blank : the user did not yet made action with his profile Starting : the user did the action once
 * Bronze : the user did the action 3 times Silver : the user did the action 5 times Gold : the user
 * did the action 10 times Diamond : the user did the action 20 times Legend : the user did the
 * action more than 20 times
 */
enum class BadgeRank {
  BLANK,
  STARTING,
  BRONZE,
  SILVER,
  GOLD,
  DIAMOND,
  LEGEND
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
