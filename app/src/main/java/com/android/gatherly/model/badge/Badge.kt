package com.android.gatherly.model.badge

/**
 * Represent the user's evolution of a [Badge] action
 *
 * Blank : the user did not yet made action with his profile
 * Starting : the user did the action once
 * Bronze : the user did the action 3 times
 * Silver : the user did the action 5 times
 * Gold : the user did the action 10 times
 * Diamond : the user did the action 20 times
 * Legend : the user did the action more than 20 times
 *
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

/**
 * Represent all the different [Badges] that an user can win during his evolution:
 *
 * addFriends :  Motivate the user to add new friends
 * createTodo : Motivate the user to use the [TODO] functionalities
 *                  to help his work organisation
 * createEvent: Motivate the user to use the [Event] functionalities to create new event
 * participateEvent: Motivate the user to participate to events
 * focusSessionPoint : Motivate the user to use the [Timer] functionalities to finish focus session
 *
 */
data class Badge(
    val addFriends: Rank,
    val createTodo: Rank,
    val createEvent: Rank,
    val participateEvent: Rank,
    val focusSessionPoint: Rank,
)
