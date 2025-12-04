package com.android.gatherly.model.profile

/** Indicates whether the user's status was set manually or automatically. */
enum class UserStatusSource(val value: String) {
  MANUAL("manual"),
  AUTOMATIC("automatic");

  /**
   * Converts a string from Firestore into a [UserStatusSource]. Defaults to AUTOMATIC if null or
   * invalid.
   */
  companion object {
    fun fromString(value: String?): UserStatusSource =
        entries.find { it.value == value } ?: AUTOMATIC
  }
}
