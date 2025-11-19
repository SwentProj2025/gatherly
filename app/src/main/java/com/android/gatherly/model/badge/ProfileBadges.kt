package com.android.gatherly.model.badge

/**
 * Represent the user's evolution of a [ProfileBadges] action
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
data class ProfileBadges(
    val addFriends: Rank = Rank.BLANK,
    val createdTodos: Rank = Rank.BLANK,
    val completedTodos: Rank = Rank.BLANK,
    val createEvent: Rank = Rank.BLANK,
    val participateEvent: Rank = Rank.BLANK,
    val focusSessionPoint: Rank = Rank.BLANK,
) {
  /** object : default Badge where every badges are set to blank rank. */
  companion object {
    val blank =
        ProfileBadges(
            addFriends = Rank.BLANK,
            createdTodos = Rank.BLANK,
            completedTodos = Rank.BLANK,
            createEvent = Rank.BLANK,
            participateEvent = Rank.BLANK,
            focusSessionPoint = Rank.BLANK)
  }
}
