package com.android.gatherly.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.gatherly.R
import com.android.gatherly.model.todo.ToDoPriority
import com.android.gatherly.ui.theme.Typography
import com.android.gatherly.ui.theme.theme_todo_priority_level_high
import com.android.gatherly.ui.theme.theme_todo_priority_level_low
import com.android.gatherly.ui.theme.theme_todo_priority_level_medium

object PriorityDropDownTestTags {
  const val PRIORITY_LEVEL_DROP_DOWN = "priorityLevelDropDown"
  const val PRIORITY_NONE_ITEM = "priorityLevelNoneItem"
  const val PRIORITY_LOW_ITEM = "priorityLevelLowItem"
  const val PRIORITY_MEDIUM_ITEM = "priorityLevelMediumItem"
  const val PRIORITY_HIGH_ITEM = "priorityLevelHighItem"
}

/**
 * Helper composable function: The user can assign to his task a specif priority level Displayed in
 * Add, Edit ToDo screen
 *
 * @param onSelectPriorityLevel : function when the user choose a level
 * @param currentPriorityLevel: the level that the user already choose.
 */
@Composable
fun PriorityDropDown(
    onSelectPriorityLevel: (ToDoPriority) -> Unit,
    currentPriorityLevel: ToDoPriority,
) {

  var expanded by remember { mutableStateOf(false) }

  val priorityItems =
      listOf(
          PriorityItem(
              level = ToDoPriority.NONE,
              label = R.string.events_priority_level_none_label,
              testTag = PriorityDropDownTestTags.PRIORITY_NONE_ITEM,
              addDividerBelow = true),
          PriorityItem(
              level = ToDoPriority.LOW,
              label = R.string.events_priority_level_low_label,
              testTag = PriorityDropDownTestTags.PRIORITY_LOW_ITEM),
          PriorityItem(
              level = ToDoPriority.MEDIUM,
              label = R.string.events_priority_level_medium_label,
              testTag = PriorityDropDownTestTags.PRIORITY_MEDIUM_ITEM),
          PriorityItem(
              level = ToDoPriority.HIGH,
              label = R.string.events_priority_level_high_label,
              testTag = PriorityDropDownTestTags.PRIORITY_HIGH_ITEM))

  Column {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxHeight()) {
      IconButton(
          modifier =
              Modifier.fillMaxHeight().testTag(PriorityDropDownTestTags.PRIORITY_LEVEL_DROP_DOWN),
          onClick = { expanded = true },
      ) {
        Icon(
            imageVector = Icons.Filled.Error,
            modifier = Modifier.size(30.dp).fillMaxSize(),
            contentDescription = "Priority level icon",
            tint = priorityLevelColor(currentPriorityLevel))
      }

      DropdownMenu(
          expanded = expanded,
          onDismissRequest = { expanded = false },
          containerColor = MaterialTheme.colorScheme.surfaceVariant) {
            priorityItems.forEach { item ->
              PriorityDropdownItem(
                  item = item,
                  isSelected = item.level == currentPriorityLevel,
                  onSelect = {
                    onSelectPriorityLevel(item.level)
                    expanded = false
                  })
            }
          }
    }

    if (currentPriorityLevel != ToDoPriority.NONE) {
      currentPriorityLevel.displayName?.let {
        Text(
            text = it,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = Typography.bodySmall)
      }
    }
  }
}

/** Helper data class: help to display the PriorityDropDownItem for each level */
private data class PriorityItem(
    val level: ToDoPriority,
    val label: Int,
    val testTag: String,
    val addDividerBelow: Boolean = false,
)

/**
 * Helper composable function: Display a dropdown item for each level
 *
 * @param item: The data priority level to display
 * @param isSelected: boolean to true if the user already selected this priority level
 * @param onSelect function to call when the user click on the item
 */
@Composable
private fun PriorityDropdownItem(item: PriorityItem, isSelected: Boolean, onSelect: () -> Unit) {
  DropdownMenuItem(
      text = {
        Text(text = stringResource(item.label), color = MaterialTheme.colorScheme.onSurfaceVariant)
      },
      onClick = onSelect,
      modifier = Modifier.testTag(item.testTag),
      trailingIcon = {
        if (isSelected) {
          Icon(Icons.Default.Check, contentDescription = "Selected")
        }
      })

  if (item.addDividerBelow) {

    HorizontalDivider(
        modifier = Modifier.padding(vertical = 8.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
  }
}

/** Helper function: assign to each priority level a color. */
@Composable
fun priorityLevelColor(level: ToDoPriority): Color {
  return when (level) {
    ToDoPriority.NONE -> MaterialTheme.colorScheme.onSurfaceVariant
    ToDoPriority.LOW -> theme_todo_priority_level_low
    ToDoPriority.MEDIUM -> theme_todo_priority_level_medium
    ToDoPriority.HIGH -> theme_todo_priority_level_high
  }
}
