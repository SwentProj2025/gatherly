package com.android.gatherly.utils

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.Month
import java.util.Calendar
import java.util.Locale

object TestDates {
  private val currentDate = LocalDate.now()

  val currentMonth = currentDate.month.value
  val currentDay = currentDate.dayOfMonth
  val currentYear = currentDate.year

  val pastYear = currentYear - 1
  val futureYear = currentYear + 1

  val futureDate = "$currentDay/$currentMonth/$futureYear"
  val pastDate = "$currentDay/$currentMonth/$pastYear"

  val calendar =
      Calendar.getInstance().apply {
        set(Calendar.YEAR, futureYear)
        set(Calendar.MONTH, currentMonth - 1)
        set(Calendar.DAY_OF_MONTH, currentDay)
      }
  val futureDueDate: Timestamp = Timestamp(calendar.time)

  val calendarPast =
      Calendar.getInstance().apply {
        set(Calendar.YEAR, pastYear)
        set(Calendar.MONTH, currentMonth - 1)
        set(Calendar.DAY_OF_MONTH, currentDay)
      }
  val pastDueDate: Timestamp = Timestamp(calendarPast.time)

  val calendarCurrent =
      Calendar.getInstance().apply {
        set(Calendar.YEAR, currentYear)
        set(Calendar.MONTH, currentMonth - 1)
        set(Calendar.DAY_OF_MONTH, currentDay)
      }
  val currentDateTimestamp: Timestamp = Timestamp(calendarCurrent.time)
}

/**
 * Selects a specific date from the Date Picker component and confirms the selection.
 *
 * @param day The day of the month to select (1-31).
 * @param month The month to select (1-12).
 * @param year The year to select.
 */
fun ComposeTestRule.selectDateFromPicker(day: Int, month: Int, year: Int) {
  val monthString = Month.of(month).name
  navigateInDatePicker(year, monthString, day)
  onNodeWithTag(DatePickerTestTags.DATE_PICKER_SAVE).performClick()
  waitUntil(UI_WAIT_TIMEOUT) {
    onNodeWithTag(DatePickerTestTags.DATE_PICKER_DIALOG).isNotDisplayed()
  }
}

/**
 * Helper function to open the date picker dialog depending on which screen we are
 *
 * @param testTag the date field that we need to click on to open the date picker compose
 */
fun ComposeTestRule.openDatePicker(testTag: String) {
  onNodeWithTag(testTag).performClick()
  waitUntil(UI_WAIT_TIMEOUT) { onAllNodes(isRoot()).fetchSemanticsNodes().size > 1 }
}

/**
 * Helper function: which navigates through the whole DatePicker to choose the specific date
 *
 * @param targetYear year to select
 * @param targetMonth string of the month we want to
 * @param targetDay specific day we want to click on when the correct page month and year is display
 */
fun ComposeTestRule.navigateInDatePicker(targetYear: Int, targetMonth: String, targetDay: Int) {
  val locale = Locale.getDefault()
  val targetMonthInLocale =
      Month.valueOf(targetMonth.uppercase()).getDisplayName(java.time.format.TextStyle.FULL, locale)

  // Click header to open year selector
  onNode(hasClickAction() and hasLiveRegion()).performClick()
  waitForIdle()

  // Select target year
  onAllNodes(hasClickAction() and hasTextMatching("\\b$targetYear\\b")).onFirst().performClick()
  waitForIdle()

  // Navigate to target month
  // Get the current month from header to calculate navigation
  val headerNode = onNode(hasClickAction() and hasLiveRegion()).fetchSemanticsNode()
  val headerText = headerNode.config[SemanticsProperties.Text].first().text
  val currentMonthName = headerText.split(" ").first()

  val currentMonth =
      Month.values().find {
        it.getDisplayName(java.time.format.TextStyle.FULL, locale)
            .equals(currentMonthName, ignoreCase = true)
      } ?: Month.JANUARY

  val targetMonthEnum = Month.valueOf(targetMonth.uppercase())
  val monthsToNavigate = targetMonthEnum.value - currentMonth.value

  // Find navigation buttons
  val nextButtonPattern = if (locale.language == "fr") "suivant" else "next"
  val prevButtonPattern = if (locale.language == "fr") "précédent" else "previous"

  if (monthsToNavigate > 0) {
    repeat(monthsToNavigate) {
      onAllNodes(hasClickAction())
          .filter(hasAnyAncestor(hasTestTag(DatePickerTestTags.DATE_PICKER_DIALOG)))
          .filterToOne(hasTextMatching("(?i).*$nextButtonPattern.*"))
          .performClick()
      waitForIdle()
    }
  } else if (monthsToNavigate < 0) {
    repeat(-monthsToNavigate) {
      onAllNodes(hasClickAction())
          .filter(hasAnyAncestor(hasTestTag(DatePickerTestTags.DATE_PICKER_DIALOG)))
          .filterToOne(hasTextMatching("(?i).*$prevButtonPattern.*"))
          .performClick()
      waitForIdle()
    }
  }

  // Select the day - match both day number AND month name to handle multiple loaded months
  onAllNodes(
          hasClickAction() and
              hasTextMatching("\\b$targetDay\\b") and
              hasTextMatching(".*$targetMonthInLocale.*"))
      .filterToOne(hasAnyAncestor(hasTestTag(DatePickerTestTags.DATE_PICKER_DIALOG)))
      .performClick()
}

/**
 * Custom semantic matcher that checks if a node's text or content description matches a regex
 * pattern. This is useful for locale-agnostic matching where substring matching is too broad.
 *
 * @param pattern Regex pattern to match against
 * @return SemanticsMatcher that tests node text/contentDescription against the pattern
 */
fun hasTextMatching(pattern: String): SemanticsMatcher {
  return SemanticsMatcher("hasTextMatching('$pattern')") { node ->
    // Check Text property
    val textList = node.config.getOrNull(SemanticsProperties.Text)
    val textMatches = textList?.any { it.text.contains(Regex(pattern)) } == true

    // Check ContentDescription property
    val contentDescription = node.config.getOrNull(SemanticsProperties.ContentDescription)
    val descriptionMatches = contentDescription?.any { it.contains(Regex(pattern)) } == true

    textMatches || descriptionMatches
  }
}

fun hasContentDescriptionMatching(pattern: String): SemanticsMatcher {
  return SemanticsMatcher("hasContentDescriptionMatching('$pattern')") { node ->
    val contentDescription = node.config.getOrNull(SemanticsProperties.ContentDescription)
    contentDescription?.any { it.contains(Regex(pattern)) } == true
  }
}

fun hasLiveRegion(): SemanticsMatcher {
  return SemanticsMatcher("hasLiveRegion") { node ->
    node.config.getOrNull(SemanticsProperties.LiveRegion) != null
  }
}
