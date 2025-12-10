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
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.unit.dp
import com.android.gatherly.model.todo.ToDoPriority
import com.android.gatherly.ui.theme.Typography
import com.android.gatherly.ui.theme.theme_todo_priority_level_high
import com.android.gatherly.ui.theme.theme_todo_priority_level_low
import com.android.gatherly.ui.theme.theme_todo_priority_level_medium
import com.android.gatherly.ui.todo.AddToDoScreenTestTags

@Composable
fun PriorityDropDown(
    onSelectPriorityLevel: ((ToDoPriority) -> Unit),
    currentPriorityLevel: ToDoPriority
) {

  var expanded by remember { mutableStateOf(false) }

  Column {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxHeight()) {
      IconButton(
          modifier =
              Modifier.fillMaxHeight().testTag(AddToDoScreenTestTags.DROP_DOWN_PRIORITY_LEVEL),
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
            DropdownMenuItem(
                text = { Text(text = "None", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                onClick = {
                  onSelectPriorityLevel(ToDoPriority.NONE)
                  expanded = false
                },
                trailingIcon = {
                  if (currentPriorityLevel == null) {
                    Icon(Icons.Default.Check, contentDescription = "Selected")
                  }
                })

            Divider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp))

            DropdownMenuItem(
                text = { Text(text = "Low", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                onClick = {
                  onSelectPriorityLevel(ToDoPriority.LOW)
                  expanded = false
                },
                trailingIcon = {
                  if (currentPriorityLevel == ToDoPriority.LOW) {
                    Icon(Icons.Default.Check, contentDescription = "Selected")
                  }
                })
            DropdownMenuItem(
                text = {
                  Text(text = "Medium", color = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                onClick = {
                  onSelectPriorityLevel(ToDoPriority.MEDIUM)
                  expanded = false
                },
                trailingIcon = {
                  if (currentPriorityLevel == ToDoPriority.MEDIUM) {
                    Icon(Icons.Default.Check, contentDescription = "Selected")
                  }
                })
            DropdownMenuItem(
                text = { Text(text = "High", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                onClick = {
                  onSelectPriorityLevel(ToDoPriority.HIGH)
                  expanded = false
                },
                trailingIcon = {
                  if (currentPriorityLevel == ToDoPriority.HIGH) {
                    Icon(Icons.Default.Check, contentDescription = "Selected")
                  }
                })
          }
    }

    if (currentPriorityLevel != ToDoPriority.NONE) {
      currentPriorityLevel?.displayName?.let {
        Text(
            text = it,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = Typography.bodySmall)
      }
    }
  }
}

@Composable
fun priorityLevelColor(level: ToDoPriority): Color {
  return when (level) {
    ToDoPriority.NONE -> MaterialTheme.colorScheme.onSurfaceVariant
    ToDoPriority.LOW -> theme_todo_priority_level_low
    ToDoPriority.MEDIUM -> theme_todo_priority_level_medium
    ToDoPriority.HIGH -> theme_todo_priority_level_high
  }
}
