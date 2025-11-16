package com.android.gatherly.model.badge

/**
 * Represent the user's evolution of a [Badge] action
 *
 * Blank : the user did not yet made action with his profile Starting : the user did the action once
 * Bronze : the user did the action 3 times Silver : the user did the action 5 times Gold : the user
 * did the action 10 times Diamond : the user did the action 20 times Legend : the user did the
 * action more than 20 times
 */
enum class Rank {
  BLANK,
  STARTING,
  BRONZE,
  SILVER,
  GOLD,
  DIAMOND,
  LEGEND
}

/** Represent all the different [Badges] that an user can win during his evolution */
data class Badge(
    val addFriends: Rank,
    val createTodo: Rank,
    val createEvent: Rank,
    val participateEvent: Rank,
    val focusSessionPoint: Rank,
) {
  /** object : default Badge where every badges are set to blank rank. */
  companion object {
    val blank =
        Badge(
            addFriends = Rank.BLANK,
            createTodo = Rank.BLANK,
            createEvent = Rank.BLANK,
            participateEvent = Rank.BLANK,
            focusSessionPoint = Rank.BLANK)
  }
}
