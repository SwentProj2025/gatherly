package com.android.gatherly.model.badge

/** Represents a single [Badge] item within the app. */
data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val rank: BadgeRank,
    val type: BadgeType,
    val icon: String
)

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
