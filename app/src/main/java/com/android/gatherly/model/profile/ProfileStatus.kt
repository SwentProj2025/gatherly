package com.android.gatherly.model.profile

/**
 * Represents the online/offline/focused status of a user. (focused is when the user uses the focus
 * timer)
 *
 * @property value The string representation stored in Firestore ("online" or "offline" or
 *   "focused").
 */
enum class ProfileStatus(val value: String) {
  ONLINE("online"),
  OFFLINE("offline"),
  FOCUSED("focused");

  companion object {

    /**
     * Converts a string from Firestore into a [ProfileStatus].
     *
     * If the string is null or doesn't match any known status, defaults to [OFFLINE].
     *
     * @param value The string value from Firestore.
     * @return The corresponding [ProfileStatus].
     */
    fun fromString(value: String?): ProfileStatus =
        ProfileStatus.entries.find { it.value == value } ?: OFFLINE
  }
}
