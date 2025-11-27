package com.android.gatherly.utils

import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.Month
import java.util.Calendar

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
  val currentYear = LocalDate.now().year
  val monthYearFormatter = targetMonth.lowercase().replaceFirstChar { it.uppercase() }
  val headerSubstring = "$monthYearFormatter $currentYear"

  onNodeWithContentDescription(headerSubstring).performClick()
  onNode(hasText("Navigate to year $targetYear") and hasClickAction(), useUnmergedTree = true)
      .performClick()

  val dayTextFragment = "$monthYearFormatter $targetDay, $targetYear"

  onAllNodes(hasText(dayTextFragment, substring = true), useUnmergedTree = true)
      .onFirst()
      .performClick()
}
