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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.android.gatherly.R
import com.android.gatherly.model.todoCategory.ToDoCategory
import com.android.gatherly.ui.theme.Typography

/** Test tags for CategoriesDropDown composable */
object CategoriesDropDownTestTags {

  const val CATEGORY_DROP_DOWN = "categoryDropDown"
  const val CATEGORY_NONE_ITEM = "categoryNoneItem"

  const val CATEGORY_EDIT_MODE_BUTTON = "categoryEditModeButton"
  const val CATEGORY_CREATE_A_NEW_BUTTON = "categoryCreateANewButton"

  /**
   * Returns a unique test tag for the button representing a given [ToDoCategory] item.
   *
   * @param categoryName The String of [ToDoCategory] item whose test tag will be generated.
   * @return A string uniquely identifying the ToDoCategory item in the UI.
   */
  fun getTestTagForCategoryItem(categoryName: String): String = "CategoryItem$categoryName"
}

/**
 * Drop down menu to select, create or delete a ToDoCategory
 *
 * @param onSelectTag lambda function to execute when the user select a tag
 * @param showCreateTagDialog MutableState<Boolean> to show or not the create tag dialog
 * @param currentTag The current selected ToDoCategory
 * @param showWarningDeleteTagDialog MutableState of ToDoCategory? to show or not the warning dialog
 * @param categoriesList List of ToDoCategory available
 */
@Composable
fun CategoriesDropDown(
    onSelectTag: (ToDoCategory?) -> Unit,
    showCreateTagDialog: MutableState<Boolean>,
    currentTag: ToDoCategory?,
    showWarningDeleteTagDialog: MutableState<ToDoCategory?>,
    categoriesList: List<ToDoCategory>
) {

  val expanded = remember { mutableStateOf(false) }
  var isModeEditOn by remember { mutableStateOf(false) }

  Column {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxHeight()) {
      IconButton(
          modifier =
              Modifier.fillMaxHeight().testTag(CategoriesDropDownTestTags.CATEGORY_DROP_DOWN),
          onClick = { expanded.value = true }) {
            Row {
              Icon(
                  imageVector = Icons.Filled.Folder,
                  modifier =
                      Modifier.size(dimensionResource(R.dimen.icons_size_medium)).fillMaxSize(),
                  contentDescription = "Category icon",
                  tint = currentTag?.color ?: MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
      DropdownMenu(
          expanded = expanded.value,
          onDismissRequest = { expanded.value = false },
          containerColor = MaterialTheme.colorScheme.surfaceVariant) {
            Row(
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(
                            horizontal = dimensionResource(R.dimen.padding_regular),
                            vertical = dimensionResource(R.dimen.padding_small)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                  Text(
                      text = stringResource(R.string.events_dropdown_tag_label),
                      style = MaterialTheme.typography.titleMedium)

                  IconButton(
                      modifier =
                          Modifier.testTag(CategoriesDropDownTestTags.CATEGORY_EDIT_MODE_BUTTON),
                      onClick = { isModeEditOn = !isModeEditOn }) {
                        Icon(
                            Icons.Filled.ModeEdit,
                            contentDescription = "Display show edit",
                            tint =
                                if (isModeEditOn) MaterialTheme.colorScheme.primary
                                else LocalContentColor.current)
                      }
                }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = dimensionResource(R.dimen.padding_small)),
                thickness = dimensionResource(R.dimen.thickness_small),
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (isModeEditOn) {
              DropdownMenuItem(
                  text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                          Text(text = stringResource(R.string.events_create_tag_label))

                          Icon(Icons.Filled.CreateNewFolder, contentDescription = "Add category")
                        }
                  },
                  onClick = { showCreateTagDialog.value = true },
                  modifier =
                      Modifier.testTag(CategoriesDropDownTestTags.CATEGORY_CREATE_A_NEW_BUTTON))

              HorizontalDivider(
                  modifier = Modifier.padding(vertical = dimensionResource(R.dimen.padding_small)),
                  thickness = dimensionResource(R.dimen.thickness_small),
                  color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
            }

            NoneCategoryItem(isModeEditOn, onSelectTag, expanded)

            categoriesList.forEach { category ->
              DropdownMenuItem(
                  text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                          Text(
                              text = category.name,
                              color = MaterialTheme.colorScheme.onSurfaceVariant)

                          ModeEditIcon(isModeEditOn)
                        }
                  },
                  onClick = {
                    onClickItem(
                        isModeEditOn, showWarningDeleteTagDialog, category, onSelectTag, expanded)
                  },
                  modifier =
                      Modifier.testTag(
                          CategoriesDropDownTestTags.getTestTagForCategoryItem(category.name)))
            }
          }
    }
    SubtextTagName(currentTag)
  }
}

/**
 * Helper function: When a category item is clicked on
 *
 * @param isModeEditOn boolean to know if the drop down is inn edit mode or not
 * @param showWarningDeleteTagDialog MutableState of ToDoCategory? to show or not the warning dialog
 * @param category The ToDoCategory item clicked
 * @param onSelectTag lambda function to execute when the user select a tag
 * @param expanded MutableState<Boolean> to control the drop down menu
 */
private fun onClickItem(
    isModeEditOn: Boolean,
    showWarningDeleteTagDialog: MutableState<ToDoCategory?>,
    category: ToDoCategory,
    onSelectTag: (ToDoCategory?) -> Unit,
    expanded: MutableState<Boolean>
) {

  if (isModeEditOn) {
    showWarningDeleteTagDialog.value = category
  } else {
    onSelectTag(category)
    expanded.value = false
  }
}

/**
 * Helper composable function: display the None Tag item
 *
 * @param isModeEditOn boolean to know if the drop down is inn edit mode or not
 * @param onSelectTag lambda function to execute when the user select the None tag
 * @param expanded MutableState<Boolean> to control the drop down menu
 */
@Composable
private fun NoneCategoryItem(
    isModeEditOn: Boolean,
    onSelectTag: (ToDoCategory?) -> Unit,
    expanded: MutableState<Boolean>
) {
  if (!isModeEditOn) {

    DropdownMenuItem(
        text = {
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.events_none_tag_label),
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
        },
        onClick = {
          onSelectTag(null)
          expanded.value = false
        },
        modifier = Modifier.testTag(CategoriesDropDownTestTags.CATEGORY_NONE_ITEM))

    HorizontalDivider(
        modifier = Modifier.padding(vertical = dimensionResource(R.dimen.padding_small)),
        thickness = dimensionResource(R.dimen.thickness_small),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
  }
}

/**
 * Helper composable function: display a subtext in the screen, to let the user know of his choice
 * of tag.
 *
 * @param currentTag The [ToDoCategory] the current user choose
 */
@Composable
private fun SubtextTagName(currentTag: ToDoCategory?) {
  if (currentTag != null) {
    Text(
        text = currentTag.name,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = Typography.bodySmall)
  }
}

/**
 * Helper function: Display an trash Icon special when the Mode edit is on. So that the user know if
 * he can delete this tag or not
 *
 * @param isModeEditOn boolean to know if the drop down is inn edit mode or not
 */
@Composable
private fun ModeEditIcon(isModeEditOn: Boolean) {
  if (isModeEditOn) {
    Icon(Icons.Filled.DeleteForever, contentDescription = "Delete category")
  }
}

object AlertDialogCreateTagTestTags {

  const val ALERT_CREATE_TAG = "alertDialogCreateNewTag"
  const val ALERT_CREATE_TAG_BUTTON = "buttonAlertDialogCreateNewTag"
  const val ALERT_CREATE_TAG_NAME_INPUT = "inputNameAlertDialogTag"

  const val ALERT_CREATE_TAG_COLOR_RANDOM = "randomColorButtonAlertDialogCreateNewTag"
}

/**
 * Alert dialog to create a new tag
 *
 * @param showCreateTagDialog MutableState<Boolean> to show or not the dialog
 * @param onCreateTag lambda function to execute when the user create a new tag
 */
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
        title = {
          Text(
              stringResource(R.string.events_alert_dialog_create_tag_title),
              style = MaterialTheme.typography.titleLarge,
          )
        },
        text = {
          Column {
            OutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                label = {
                  Text(stringResource(R.string.events_alert_dialog_create_tag_name_input))
                },
                isError = categoryName.isBlank(),
                singleLine = true,
                modifier =
                    Modifier.testTag(AlertDialogCreateTagTestTags.ALERT_CREATE_TAG_NAME_INPUT))

            RandomColorBox(
                color = chosenColor, onColorChange = { newColor -> chosenColor = newColor })
          }
        },
        confirmButton = {
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween) {
                Button(
                    onClick = {
                      onCreateTag(categoryName, chosenColor)
                      showCreateTagDialog.value = false
                      categoryName = ""
                    },
                    enabled = isNameValid,
                    modifier =
                        Modifier.testTag(AlertDialogCreateTagTestTags.ALERT_CREATE_TAG_BUTTON)) {
                      Text(stringResource(R.string.events_alert_dialog_create_tag_button_label))
                    }
                Button(onClick = { showCreateTagDialog.value = false }) {
                  Text(stringResource(R.string.events_alert_dialog_cancel_tag_button_label))
                }
              }
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.testTag(AlertDialogCreateTagTestTags.ALERT_CREATE_TAG))
  }
}

/**
 * Alert dialog to warn the user when he is deleting a tag
 *
 * @param showWarningDeleteTagDialog MutableState of ToDoCategory? to show or not the dialog
 * @param onConfirmDelete lambda function to execute when the user confirm the deletion
 */
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
        actions =
            GatherlyAlertDialogActions(
                onDismiss = { showWarningDeleteTagDialog.value = null },
                onConfirm = {
                  onConfirmDelete(showWarningDeleteTagDialog.value!!)
                  showWarningDeleteTagDialog.value = null
                }),
        isImportantWarning = true)
  }
}

/**
 * Composable to display a box with a random color and a button to change the color
 *
 * @param color The current color to display
 * @param onColorChange lambda function to execute when the user want a new random color
 */
@Composable
fun RandomColorBox(color: Color, onColorChange: (Color) -> Unit) {

  Column(
      modifier = Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.padding_regular)),
      horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier =
                Modifier.size(dimensionResource(R.dimen.box_size_large))
                    .clip(
                        RoundedCornerShape(
                            dimensionResource(R.dimen.rounded_corner_shape_medium_large)))
                    .background(color))

        Spacer(Modifier.height(dimensionResource(R.dimen.padding_regular)))

        Button(
            modifier = Modifier.testTag(AlertDialogCreateTagTestTags.ALERT_CREATE_TAG_COLOR_RANDOM),
            onClick = { onColorChange(randomColor()) }) {
              Text(stringResource(R.string.events_alert_dialog_create_tag_color_button_label))
            }
      }
}

/** Helper function: generates a random Color */
fun randomColor(): Color {
  return Color(
      red = Math.random().toFloat(),
      green = Math.random().toFloat(),
      blue = Math.random().toFloat(),
      alpha = 1f)
}
