package com.android.gatherly.model.profile

/**
 * Represents the online/offline/focused status of a user. (focused is when the user uses the focus
 * timer)
 *
 * @param value The string value associated with the profile status.
 * @property ONLINE Indicates the user is online.
 * @property OFFLINE Indicates the user is offline.
 * @property FOCUSED Indicates the user is focused.
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
