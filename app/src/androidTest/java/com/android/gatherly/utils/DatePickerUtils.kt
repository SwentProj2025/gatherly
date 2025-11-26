package com.android.gatherly.utils

import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.gatherly.ui.todo.AddToDoScreenTestTags
import java.time.LocalDate
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.onFirst
import java.time.Month

fun ComposeTestRule.selectDateFromPicker(day: Int, month: Int, year: Int) {
    openDatePicker()
    val monthString = Month.of(month).name
    navigateInDatePicker( year, monthString, day)
    onNodeWithTag(DatePickerTestTags.DATE_PICKER_SAVE).performClick()
    waitUntil(UI_WAIT_TIMEOUT) {
        onNodeWithTag(DatePickerTestTags.DATE_PICKER_DIALOG).isNotDisplayed()
    }
}

fun ComposeTestRule.openDatePicker() {
    onNodeWithTag(AddToDoScreenTestTags.INPUT_TODO_DATE).performClick()
    waitUntil(UI_WAIT_TIMEOUT) {
        onAllNodes(isRoot()).fetchSemanticsNodes().size > 1
    }
}

fun ComposeTestRule.closeDatePicker() {
    onNodeWithText("CANCEL", ignoreCase = true).performClick()
    waitUntil(UI_WAIT_TIMEOUT) {
        onAllNodesWithTag(DatePickerTestTags.DATE_PICKER_DIALOG)
            .fetchSemanticsNodes().isEmpty()
    }
}

fun ComposeTestRule.navigateInDatePicker(targetYear: Int, targetMonth: String, targetDay: Int) {
    val currentYear = LocalDate.now().year
    val monthYearFormatter = targetMonth.lowercase().replaceFirstChar { it.uppercase() }
    val headerSubstring = "$monthYearFormatter $currentYear"

    onNodeWithContentDescription(headerSubstring).performClick()
    onNode(
        hasText("Navigate to year $targetYear") and hasClickAction(),
        useUnmergedTree = true
    ).performClick()

    val dayTextFragment = "$monthYearFormatter $targetDay, $targetYear"

    onAllNodes(hasText(dayTextFragment, substring = true), useUnmergedTree = true)
        .onFirst()
        .performClick()
}