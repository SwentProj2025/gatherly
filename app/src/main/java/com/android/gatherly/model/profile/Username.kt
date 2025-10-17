package com.android.gatherly.model.profile

/**
 * Utility for normalizing and validating usernames.
 *
 * Usernames must match the pattern `[a-z0-9._-]{3,20}` and are normalized to lowercase.
 */
object Username {

  /**
   * Normalizes the given str by trimming and converting it to lowercase.
   *
   * @param str The string to normalize.
   * @return The normalized lowercase username.
   */
  fun normalize(str: String): String {
    return str.trim().lowercase()
  }

  /** Regular expression pattern for valid usernames. */
  private val allowedPattern = Regex("^[a-z0-9._-]{3,20}$")

  /**
   * Validates whether a given string is a valid username.
   *
   * @param str The string to validate.
   * @return true if valid, false otherwise.
   */
  fun isValid(str: String): Boolean {
    return allowedPattern.matches(normalize(str))
  }
}
