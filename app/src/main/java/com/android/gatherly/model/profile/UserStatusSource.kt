package com.android.gatherly.model.profile

/**
 * Indicates whether the user's status was set manually or automatically.
 *
 * @param value The string value associated with the user status source.
 * @property MANUAL Indicates the status was set manually by the user.
 * @property AUTOMATIC Indicates the status was set automatically by the system.
 */
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
