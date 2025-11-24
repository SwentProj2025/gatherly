package com.android.gatherly.utils

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility for parsing dates in the "dd/MM/yyyy" format. Returns a [Date] if valid, or `null` for
 * invalid or mismatched input.
 */
object DateParser {

  private val dateRegex = Regex("""^\d{2}/\d{2}/\d{4}$""")
  private val dateFormat =
      SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).let {
        it.isLenient = false
        it
      }

  /**
   * Parses the given [str] as a date in "dd/MM/yyyy" format.
   *
   * @param str the string to parse
   * @return a [Date] object if [str] is valid; `null` if it is invalid or does not match the format
   *
   * Example:
   * ```kotlin
   * val validDate = DateParser.parse("14/10/2025")  // Returns Date
   * val invalid = DateParser.parse("2025-10-14")   // Returns null
   * ```
   */
  fun parse(str: String): Date? {
    if (!dateRegex.matches(str)) {
      return null
    }

    return runCatching { dateFormat.parse(str) }.getOrNull()
  }


  /**
   * Formats a [Timestamp] to a string in "HH:mm" format.
   *
   * @param timestamp The [Timestamp] to format.
   * @return A string representing the time in "HH:mm" format.
   */
  fun timeToString(timestamp: Timestamp): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(timestamp.toDate())
  }

    /**
     * Formats a [Timestamp] to a string in "dd/MM/yyyy" format.
     *
     * @param timestamp The [Timestamp] to format.
     * @return A string representing the date in "dd/MM/yyyy" format.
     */
  fun dateToString(timestamp: Timestamp): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(timestamp.toDate())
  }
}
