package com.android.gatherly.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.ModeEdit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.gatherly.R
import com.android.gatherly.model.todoCategory.ToDoCategory
import com.android.gatherly.ui.theme.Typography
import com.android.gatherly.ui.todo.AddToDoScreenTestTags

@Composable
fun CategoriesDropDown(
    onSelectTag: (ToDoCategory?) -> Unit,
    showCreateTagDialog: MutableState<Boolean>,
    currentTag: ToDoCategory?,
    showWarningDeleteTagDialog: MutableState<ToDoCategory?>,
    categoriesList: List<ToDoCategory>
) {

  var expanded by remember { mutableStateOf(false) }
  var isModeEditOn by remember { mutableStateOf(false) }

  Column {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxHeight()) {
      IconButton(
          modifier =
              Modifier.fillMaxHeight().testTag(AddToDoScreenTestTags.DROP_DOWN_PRIORITY_LEVEL),
          onClick = { expanded = true },
      ) {
        Row {
          Icon(
              imageVector = Icons.Filled.Folder,
              modifier = Modifier.size(30.dp).fillMaxSize(),
              contentDescription = "Category icon",
              tint = currentTag?.color ?: MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
      DropdownMenu(
          expanded = expanded,
          onDismissRequest = { expanded = false },
          containerColor = MaterialTheme.colorScheme.surfaceVariant) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                  Text("Categories", style = MaterialTheme.typography.titleMedium)

                  IconButton(onClick = { isModeEditOn = !isModeEditOn }) {
                    Icon(
                        Icons.Filled.ModeEdit,
                        contentDescription = "Display show edit",
                        tint =
                            if (isModeEditOn) MaterialTheme.colorScheme.primary
                            else LocalContentColor.current)
                  }
                }

            Divider(
                color = MaterialTheme.colorScheme.onSurface,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp))

            if (isModeEditOn) {
              DropdownMenuItem(
                  text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                          Text(text = "Add a new category")

                          Icon(Icons.Filled.CreateNewFolder, contentDescription = "Add category")
                        }
                  },
                  onClick = { showCreateTagDialog.value = true })
              Divider(
                  color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                  thickness = 1.dp,
                  modifier = Modifier.padding(vertical = 8.dp))
            }

            if (!isModeEditOn) {

              DropdownMenuItem(
                  text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                          Text("None")
                        }
                  },
                  onClick = {
                    onSelectTag(null)
                    expanded = false
                  })

              Divider(
                  color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                  thickness = 1.dp,
                  modifier = Modifier.padding(vertical = 8.dp))
            }

            categoriesList.forEach { category ->
              DropdownMenuItem(
                  text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                          Text(category.name)

                          if (isModeEditOn) {
                            Icon(Icons.Filled.DeleteForever, contentDescription = "Delete category")
                          }
                        }
                  },
                  onClick = {
                    if (isModeEditOn) {
                      showWarningDeleteTagDialog.value = category
                    } else {
                      onSelectTag(category)
                      expanded = false
                    }
                  })
            }
          }
    }

    if (currentTag != null) {
      Text(
          text = currentTag.name,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          style = Typography.bodySmall)
    }
  }
}

object AlertDialogCreateTagTestTags {

  const val ALERT_CREATE_TAG = "alertDialogCreateNewTag"
  const val ALERT_CREATE_TAG_BUTTON = "buttonAlertDialogCreateNewTag"

  const val ALERT_CREATE_TAG_CANCEL_BUTTON = "cancelButtonAlertDialogCreateNewTag"
}

@Composable
fun AlertDialogCreateTag(
    showCreateTagDialog: MutableState<Boolean>,
    onCreateTag: ((String, Color) -> Unit)
) {
  var categoryName by rememberSaveable { mutableStateOf("") }
  var chosenColor by remember { mutableStateOf(randomColor()) }
  val isNameValid = categoryName.isNotBlank()

  if (showCreateTagDialog.value) {

    AlertDialog(
        onDismissRequest = { showCreateTagDialog.value = false },
        title = { Text("Create a new todo category") },
        text = {
          Column {
            OutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                label = { Text("Name") },
                isError = categoryName.isBlank(),
                singleLine = true)

            RandomColorBox(
                color = chosenColor, onColorChange = { newColor -> chosenColor = newColor })
          }
        },
        confirmButton = {
          Button(
              onClick = {
                onCreateTag(categoryName, chosenColor)
                showCreateTagDialog.value = false
                categoryName = ""
              },
              enabled = isNameValid,
              modifier = Modifier.testTag(AlertDialogCreateTagTestTags.ALERT_CREATE_TAG_BUTTON)) {
                Text("Create")
              }
        },
        dismissButton = {
          Button(
              onClick = { showCreateTagDialog.value = false },
              modifier =
                  Modifier.testTag(AlertDialogCreateTagTestTags.ALERT_CREATE_TAG_CANCEL_BUTTON)) {
                Text("Cancel")
              }
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.testTag(AlertDialogCreateTagTestTags.ALERT_CREATE_TAG))
  }
}

@Composable
fun AlertDialogWarningDeleteTag(
    showWarningDeleteTagDialog: MutableState<ToDoCategory?>,
    onConfirmDelete: (ToDoCategory) -> Unit
) {

  if (showWarningDeleteTagDialog.value != null) {
    GatherlyAlertDialog(
        titleText =
            "Warning ! You are deleting \n the category:  ${showWarningDeleteTagDialog.value!!.name}",
        bodyText = stringResource(R.string.todos_delete_warning_text),
        dismissText = stringResource(R.string.cancel),
        confirmText = stringResource(R.string.delete),
        onDismiss = { showWarningDeleteTagDialog.value = null },
        onConfirm = {
          onConfirmDelete(showWarningDeleteTagDialog.value!!)
          showWarningDeleteTagDialog.value = null
        },
        isImportantWarning = true)
  }
}

@Composable
fun RandomColorBox(color: Color, onColorChange: (Color) -> Unit) {

  Column(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(150.dp).clip(RoundedCornerShape(16.dp)).background(color))

        Spacer(Modifier.height(16.dp))

        Button(onClick = { onColorChange(randomColor()) }) { Text("New random color") }
      }
}

fun randomColor(): Color {
  return Color(
      red = Math.random().toFloat(),
      green = Math.random().toFloat(),
      blue = Math.random().toFloat(),
      alpha = 1f)
}
